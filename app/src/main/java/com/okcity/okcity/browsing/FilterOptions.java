package com.okcity.okcity.browsing;

class FilterOptions {

    public static final String[] TIME_FILTER_STRINGS = new String[]{
            "Past minute", "Past 15 minutes", "Past 30 minutes", "Past hour", "Past day",
            "Past week", "Past month"};

    static final Long[] TIMES_MILLI = new Long[]{
            60000L,      // Past minute
            900000L,     // Past 15 minutes
            1800000L,    // Past 30 minutes
            3600000L,    // Past hour
            864000000L,  // Past day
            6048000000L, // Past week
            26297430000L // Past month
    };

    private int indexOfFilter;

    FilterOptions() {
        indexOfFilter = 3; // default
    }

    void setIndexOfFilter(int indexOfFilter) {
        if (indexOfFilter >= 0 && indexOfFilter < TIMES_MILLI.length) {
            this.indexOfFilter = indexOfFilter;
        } else {
            throw new IllegalArgumentException("Index not within range: got " + indexOfFilter);
        }
    }

    int getIndexOfFilter() {
        return indexOfFilter;
    }

    long getMillisSelected() {
        return TIMES_MILLI[indexOfFilter];
    }
}
