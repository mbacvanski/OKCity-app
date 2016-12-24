package com.okcity.okcity;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

import java.io.IOException;

public class RecordFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "RecordFragment";
    private MapView mMapView;
    private GoogleMap googleMap;
    private final int FINE_LOCATION_PERMISSION = 0;
    private final int COARSE_LOCATION_PERMISSION = 1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private Button recordButton;
    private boolean recording = false;
    private MediaRecorder mRecorder = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_record, container, false);

        recordButton = (Button) v.findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordButtonClick();
            }
        });

        mMapView = (MapView) v.findViewById(R.id.mapView);
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
                googleMap = mMap;
                boolean fineLocation = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                boolean coarseLocation = checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                if (!fineLocation) {
                    askForPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                            COARSE_LOCATION_PERMISSION);
                }
                if (!coarseLocation) {
                    askForPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                            FINE_LOCATION_PERMISSION);
                }
                if (fineLocation && coarseLocation) {
                    // We have all the permissions we need
                    moveMarkerToUserPosition(googleMap);
                }
            }
        });

        return v;
    }

    private void recordButtonClick() {
        recording = !recording;
        if (recording) {
            startRecording();
        } else {

        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void askForPermissions(String permission, int requestCode) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {


        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//            LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private void moveMarkerToUserPosition(final GoogleMap googleMap) {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            googleMap.setMyLocationEnabled(true);

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (location != null) {
                handleNewLocation(googleMap, location);
            }
        }
    }

    private void handleNewLocation(GoogleMap googleMap, Location location) {
        LatLng userPosition = new LatLng(location.getLatitude(),
                location.getLongitude());
        // For dropping a marker at a point on the Map
        MarkerOptions marker = new MarkerOptions()
                .position(userPosition)
                .title("Your Location")
                .snippet("Your snippet");

        googleMap.addMarker(marker);

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userPosition).zoom(17).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
    }
}
