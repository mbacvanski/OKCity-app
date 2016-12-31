package com.okcity.okcity.browsing;

public class FilterOptions {

    public static final String[] TIME_FILTER_STRINGS = new String[]{
            "Past minute", "Past 15 minutes", "Past 30 minutes", "Past hour", "Past day",
            "Past week", "Past month"};
    public static final Long[] TIMES_MILLI = new Long[]{
            6000L,      // Past minute
            90000L,     // Past 15 minutes
            180000L,    // Past 30 minutes
            360000L,    // Past hour
            86400000L,  // Past day
            604800000L, // Past week
            2629743000L // Past month
    };

    private int indexOfFilter;

    public FilterOptions() {
        indexOfFilter = -1;
    }

    public void setIndexOfFilter(int indexOfFilter) {
        this.indexOfFilter = indexOfFilter;
    }

    public int getIndexOfFilter() {
        return indexOfFilter;
    }
}
