package com.okcity.okcity.recording;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Report {

    private static final String TAG = "Report";

    private String transcribedText;
    private Location recordedLocation;

    public Report(String transcribedText, Location recordedLocation) {
        this.transcribedText = transcribedText;
        this.recordedLocation = recordedLocation;
    }

    void setTranscribedText(String newText) {
        transcribedText = newText;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public Location getRecordedLocation() {
        return recordedLocation;
    }

    public void sendRecording(Location currentLocation) {
        Log.i(TAG, "Sending recording");
        double longitude = currentLocation.getLongitude();
        double latitude = currentLocation.getLatitude();
        new SendRecordingTask().execute(longitude, latitude);
    }

    private class SendRecordingTask extends AsyncTask<Double, String, Integer> {

        private static final String TAG = "SendRecordingTask";

        @Override
        protected Integer doInBackground(Double... params) {
            int statusCode = -1;

            String urlString = "http://104.199.138.179/addReport/"; // URL to call
            double longitude = params[0];
            double latitude = params[1];
            String postData = "lon=" + longitude
                    + "&lat=" + latitude
                    + "&transcript=" + getTranscribedText();

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());

                wr.write(postData);
                wr.flush();

                statusCode = urlConnection.getResponseCode();
                Log.i(TAG, "Done with post request");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            return statusCode;
        }

        @Override
        protected void onPostExecute(Integer statusCode) {
            Log.i(TAG, "OnPostExecute with status code " + statusCode);
        }
    }

    @Override
    public String toString() {
        return "Report: {"
                + "Location: {"
                + "Latitude: "
                + recordedLocation.getLatitude()
                + ", Longitude: "
                + recordedLocation.getLongitude()
                + "}, Transcription: "
                + transcribedText
                + "}";
    }
}
