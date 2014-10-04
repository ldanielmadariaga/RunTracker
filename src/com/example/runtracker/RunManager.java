package com.example.runtracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.example.runtracker.RunDatabaseHelper.LocationCursor;
import com.example.runtracker.RunDatabaseHelper.RunCursor;

public class RunManager {

	private static final String TAG = "RunManager";
	private static final String PREFS_FILE = "runs";
	private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";

	public static final String ACTION_LOCATION = "com.example.runtracker.ACTION_LOCATION";
	private static RunManager runManager;
	private Context appContext;
	private LocationManager locationManager;
	private RunDatabaseHelper databaseHelper;
	private SharedPreferences sharedPreferences;
	private long currentRunId;

	private RunManager(Context context) {
		this.appContext = context;
		this.locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
		databaseHelper = new RunDatabaseHelper(appContext);
		sharedPreferences = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
		currentRunId = sharedPreferences.getLong(PREF_CURRENT_RUN_ID, -1);
	}

	public static RunManager getInstance(Context context) {
		if (runManager == null) {
			runManager = new RunManager(context.getApplicationContext());
		}
		return runManager;
	}

	public Run startNewRun() {
		Run run = insertRun();
		startTrackingRun(run);
		return run;
	}

	private Run insertRun() {
		Run run = new Run();
		run.setId(databaseHelper.insertRun(run));
		return run;
	}

	public void startTrackingRun(Run run) {
		currentRunId = run.getId();
		sharedPreferences.edit().putLong(PREF_CURRENT_RUN_ID, currentRunId).commit();
		startLocationUpdates();
	}

	public void stopRun() {
		stopLocationUpdates();
		currentRunId = -1;
		sharedPreferences.edit().remove(PREF_CURRENT_RUN_ID).commit();
	}

	public Run getRun(long id) {
		Run run = null;
		RunCursor runCursor = databaseHelper.queryRun(id);
		runCursor.moveToFirst();
		if (!runCursor.isAfterLast()) {
			run = runCursor.getRun();
		}
		runCursor.close();
		return run;
	}

	public boolean isTrackingRun(Run run) {
		return run != null && run.getId() == currentRunId;
	}

	public RunCursor queryRuns() {
		return databaseHelper.queryRuns();
	}

	public LocationCursor queryLocationsForRun(long runId) {
		return databaseHelper.queryLocationsForRun(runId);
	}

	public Location getLastLocationForRun(long runId) {
		Location location = null;
		LocationCursor cursor = databaseHelper.queryLastLocationForRun(runId);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			location = cursor.getLocation();
		}

		cursor.close();
		return location;
	}

	public void insertLocation(Location location) {
		if (currentRunId != -1) {
			databaseHelper.insertLocation(currentRunId, location);
		} else {
			Log.e(TAG, "Location received with no tracking run; ignoring.");
		}
	}

	public void startLocationUpdates() {
		String provider = LocationManager.GPS_PROVIDER;

		Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
		if (lastKnownLocation != null) {
			lastKnownLocation.setTime(System.currentTimeMillis());
			broadcastLocation(lastKnownLocation);
		}

		PendingIntent pendingIntent = getLocationPendingIntent(true);
		locationManager.requestLocationUpdates(provider, 0, 0, pendingIntent);
	}

	private void broadcastLocation(Location location) {
		Intent broadcastIntent = new Intent(ACTION_LOCATION);
		broadcastIntent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		appContext.sendBroadcast(broadcastIntent);
	}

	public void stopLocationUpdates() {
		PendingIntent pendingIntent = getLocationPendingIntent(false);
		if (pendingIntent != null) {
			locationManager.removeUpdates(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean isTrackingRun() {
		return getLocationPendingIntent(false) != null;
	}

	private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
		Intent broadcastIntent = new Intent(ACTION_LOCATION);
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(appContext, 0, broadcastIntent, flags);
	}
}
