package com.quantag.geofenceapp;

/**
 * Created by Fromitt on 12.07.2017.
 */

public class Constants {

    public static final String GEOFENCE_KEY             = "MAIN_GEO";
    public static final int    LOCATION_PERMISSIONS_RC  = 101;
    public static final long   GEOFENCE_EXPIRATION_TIME = 10 * 60 * 1000;
    public static final double DEFAULT_LAT              = 50.437757;
    public static final double DEFAULT_LON              = 30.520257;
    public static final float  DEFAULT_RADIUS           = 100;

    public static final String ACTION_GEOFENCE = "action.geofence";
    public static final String ARG_MESSAGE     = "arg.message";
    public static final int    MSG_ENTER       = 201;
    public static final int    MSG_EXIT        = 202;
}
