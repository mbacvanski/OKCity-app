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
