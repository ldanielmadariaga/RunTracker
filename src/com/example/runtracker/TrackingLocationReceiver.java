package com.example.runtracker;

import android.content.Context;
import android.location.Location;

public class TrackingLocationReceiver extends LocationReceiver {

	@Override
	protected void onLocationReceived(Context context, Location location) {
		RunManager.getInstance(context).insertLocation(location);
	}

}
