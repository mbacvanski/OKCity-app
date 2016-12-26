package com.okcity.okcity.recording;

import android.location.Location;

class Recording {

    private String transcribedText;
    private Location recordedLocation;

    public Recording(String transcribedText, Location recordedLocation) {
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
}
