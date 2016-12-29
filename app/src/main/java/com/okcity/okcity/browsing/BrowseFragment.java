package com.okcity.okcity.browsing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.okcity.okcity.R;
import com.okcity.okcity.recording.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BrowseFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "BrowseFragment";

    private MapView mMapView;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private boolean firstTimeZoomingToUserLocation = true;

    public BrowseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse, container, false);
        mMapView = (MapView) v.findViewById(R.id.browseMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds
                .setFastestInterval(1000);     // 1 second

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                Log.i(TAG, "Map is ready!");
                googleMap = mMap;
                googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        Log.i(TAG, "Camera moved!");
                        //                        getNearbyReports(getCurrentLocation(), 5.0);
                    }
                });
            }
        });

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        return v;
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            googleMap.setMyLocationEnabled(true);

            if (firstTimeZoomingToUserLocation) {
                // Move this to somewhere else later
                getNearbyReports(location, 5);

                LatLng userPosition = new LatLng(location.getLatitude(),
                        location.getLongitude());

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(userPosition).zoom(17).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                firstTimeZoomingToUserLocation = false;
            }
        }
    }

    public void getNearbyReports(Location location, double milesRadius) {
        if (location != null) {
            Log.i(TAG, "Get nearby reports");

            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
            String radius = String.valueOf(milesRadius);

            String[] params = new String[]{longitude, latitude, radius};
            new RetrieveReportsTask().execute(params);
        }
    }

    class RetrieveReportsTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "RetrieveReportsTask";

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "Doing in background");
            double longitude = Double.parseDouble(params[0]);
            double latitude = Double.parseDouble(params[1]);
            double radius = Double.parseDouble(params[2]);

            String urlString = "http://104.199.138.179/getNearby/?lon=" + longitude
                    + "&lat=" + latitude
                    + "&radius=" + radius;

            Log.i(TAG, "urlString = " + urlString);

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                Scanner s = new Scanner(inputStream).useDelimiter("\\A"); // sorcery
                String text = s.next();
                Log.i(TAG, "text = " + text);
                return text;
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException!!!!!");
                e.getStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "IOException!!!!!");
                e.printStackTrace();
            }
            return "null";
        }

        protected void onPostExecute(String response) {
            List<Report> reports = new ArrayList<>();
            try {
                JSONArray jarray = new JSONArray(response);
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject reportObject = jarray.getJSONObject(i);
                    String transcript = reportObject.getString("transcript");
                    JSONArray coordinates = reportObject.getJSONObject("location").getJSONArray("coordinates");
                    double longitude = coordinates.getDouble(0);
                    double latitude = coordinates.getDouble(1);

                    Location recordingLocation = new Location("the report's location");
                    recordingLocation.setLongitude(longitude);
                    recordingLocation.setLatitude(latitude);

                    Report newReport = new Report(transcript, recordingLocation, System.currentTimeMillis());
                    reports.add(newReport);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            plotReportsOnMap(reports);
        }
    }


    private void plotReportsOnMap(List<Report> reports) {
        googleMap.clear();
        for (Report each : reports) {
            Location recordedLocation = each.getRecordedLocation();
            LatLng position = new LatLng(recordedLocation.getLatitude(), recordedLocation.getLongitude());

            MarkerOptions marker = new MarkerOptions()
                    .position(position)
                    .title(each.getTranscribedText());

            googleMap.addMarker(marker);
        }
    }

    private Location getCurrentLocation() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        return null;
    }
}
