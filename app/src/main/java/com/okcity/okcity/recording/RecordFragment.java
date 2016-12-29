package com.okcity.okcity.recording;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class RecordFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SpeechListener {

    private static final String TAG = "RecordFragment";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 100;

    private EditText recognizedText;
    private MapView mMapView;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private RecorderRecognizer recorderRecognizer;
    private Report currentReport;

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_record, container, false);

        recognizedText = (EditText) v.findViewById(R.id.voiceRecognizedText);

        Button recordButton = (Button) v.findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        mMapView = (MapView) v.findViewById(R.id.recordMapView);
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
                moveMarkerToUserPosition(googleMap);
            }
        });

        recorderRecognizer = new RecorderRecognizer(getContext(), this);

        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
        askForPermissions(permissions, 7);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<String> textMatchList = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                Log.d(TAG, "textMatchList = " + textMatchList.toString());
            } else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
                Log.d(TAG, "Audio Error");
            } else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
                Log.d(TAG, "Client Error");
            } else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
                Log.d(TAG, "Network Error");
            } else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
                Log.d(TAG, "No Match");
            } else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
                Log.d(TAG, "Server Error");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startRecording() {
        // Check for voice recognizer
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            Toast.makeText(getActivity(), "Voice recognizer not present",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Voice recognition not present");
        } else {
            recorderRecognizer.startRecordingIntention();
        }
    }

    private void askForPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(getActivity(), permissions, requestCode);
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
        recorderRecognizer.destroyEverything(); // Kaboom
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

    @Override
    public void onResults(List<String> results) {
        recognizedText.setText(results.get(0));
        currentReport = new Report(results.get(0), getCurrentLocation());
        recognizedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Text changed!");
                currentReport.setTranscribedText(s.toString());
                sendRecording();
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "After text changed");
            }
        });
    }

    private void sendRecording() {
        currentReport.sendRecording(getCurrentLocation());
    }

    private Location getCurrentLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        return null;
    }
}
