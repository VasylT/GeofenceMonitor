package com.quantag.geofenceapp.utilities;

public class Constants {

    public static final String GEOFENCE_KEY             = "MAIN_GEO";
    public static final int    LOCATION_PERMISSIONS_RC  = 101;
    public static final long   GEOFENCE_EXPIRATION_TIME = 10 * 60 * 1000;
    public static final double DEFAULT_LAT              = 50.450917;
    public static final double DEFAULT_LON              = 30.522617;
    public static final float  DEFAULT_RADIUS           = 100;

    public static final String ACTION_GEOFENCE     = "action.geofence";
    public static final String ACTION_CONNECTIVITY = "action.connectivity";
    public static final String ARG_MESSAGE         = "arg.message";
    public static final String ARG_STRING          = "arg.string";
    public static final String PREFS               = "map.preferences";
    public static final String ARG_LAT             = "arg.latitude";
    public static final String ARG_LON             = "arg.longitude";
    public static final int    MSG_ENTER           = 201;
    public static final int    MSG_EXIT            = 202;
    public static final int    MSG_CONNECTED       = 203;
    public static final int    MSG_DISCONNECTED    = 204;
}
