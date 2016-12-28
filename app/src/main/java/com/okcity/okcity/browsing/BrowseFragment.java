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
import com.okcity.okcity.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
                moveMarkerToUserPosition(googleMap);
            }
        });

//        getNearbyReports(null, 5);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        return v;
    }

    private void moveMarkerToUserPosition(final GoogleMap googleMap) {
        Log.i(TAG, "Moving marker to user position");
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            googleMap.setMyLocationEnabled(true);

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (location != null) {
                Log.i(TAG, "Location was not null");
                handleNewLocation(googleMap, location);
            }
        }
    }

    private void handleNewLocation(GoogleMap googleMap, Location location) {
        Log.i(TAG, "Handle new location");
        LatLng userPosition = new LatLng(location.getLatitude(),
                location.getLongitude());

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userPosition).zoom(17).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
        handleNewLocation(googleMap, location);
        getNearbyReports(location, 5);
    }

    public List<Report> getNearbyReports(Location location, int milesRadius) {
        Log.i(TAG, "Get nearby reports");

        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        String radius = String.valueOf(milesRadius);

        String[] params = new String[]{longitude, latitude, radius};
        new RetrieveReportsTask().execute(params);
        return null;
    }

    class RetrieveReportsTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "RetrieveReportsTask";

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "Doing in background");
            double longitude = Double.parseDouble(params[0]);
            double latitude = Double.parseDouble(params[1]);
            double radius = Double.parseDouble(params[2]);

            // -73.856077, 40.848447
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
            Log.i(TAG, "onPostExecute with response " + response);

        }
    }
}
