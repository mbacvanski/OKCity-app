package com.okcity.okcity.browsing;

import com.google.android.gms.maps.model.Marker;
import com.okcity.okcity.recording.Report;

public class ReportOnMap {

    private Report report;
    private Marker marker;

    public ReportOnMap(Report report, Marker marker) {
        this.report = report;
        this.marker = marker;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
