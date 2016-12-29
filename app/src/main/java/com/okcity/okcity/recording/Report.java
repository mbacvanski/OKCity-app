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
    private long posixTime;
    private String _id;

    public Report() {
        transcribedText = "";
        recordedLocation = null;
        posixTime = System.currentTimeMillis();
        _id = null;
    }

    public Report(String transcribedText, Location recordedLocation, long posixTime) {
        this.transcribedText = transcribedText;
        this.recordedLocation = recordedLocation;
        this.posixTime = posixTime;
    }

    public Report(String transcribedText, Location recordedLocation, long posixTime, String _id) {
        this.transcribedText = transcribedText;
        this.recordedLocation = recordedLocation;
        this.posixTime = posixTime;
        this._id = _id;
    }

    public void setRecordedLocation(Location recordedLocation) {
        this.recordedLocation = recordedLocation;
    }

    public void setPosixTime(long posixTime) {
        this.posixTime = posixTime;
    }

    public long getPosixTime() {
        return posixTime;
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

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void sendRecording() {
        if (recordedLocation != null && transcribedText != null && !transcribedText.equals("")) {
            double longitude = recordedLocation.getLongitude();
            double latitude = recordedLocation.getLatitude();
            new SendRecordingTask().execute(longitude, latitude);
        } else {
            throw new IllegalStateException("No location or text given");
        }
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
                    + "&transcript=" + transcribedText
                    + "&timestamp=" + posixTime;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());

                wr.write(postData);
                wr.flush();

                statusCode = urlConnection.getResponseCode();
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
        if (recordedLocation != null) {
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
        return "Report: {"
                + "Transcription: "
                + transcribedText
                + "}";

    }
}
