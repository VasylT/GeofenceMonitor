# GeofenceMonitor
Test application for configuring geofences and monitoring user enter-exit.

# Setup

Import as gradle project.

For compiling you will need a maps API key set in AndroidManifest.xml file:

android:value="@string/map_api_key"

Key can be obtained from your Developers Console:
https://console.developers.google.com/apis/library

Section "Google Maps Android API"

The key usually begins with "AIza".

# Usage

Before launching turn both wifi/3g and location on.

Main controls menu is hidden under yellow button with hand icon:

1. "Get marker location" — update coordinates fields with marker's latitude & longitude.

2. "Set geofence" — create geofence with coordinates and radius specified.

3. "Set wifi spot" — change geofence-related wifi (by-default its the one you're connected too on app first launch)

Also you can set latitude, longitude and radius directly via edit fields.

Status of user position in relation to the geofence is visualized with specific bar with text, that chages color accordingly.
