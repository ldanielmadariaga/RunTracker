package com.example.runtracker;

import android.content.Context;
import android.database.Cursor;

public class LocationCursorLoader extends SQLiteCursorLoader {

	private long runId;

	public LocationCursorLoader(Context context, long runId) {
		super(context);
		this.runId = runId;
	}

	@Override
	protected Cursor loadCursor() {
		return RunManager.getInstance(getContext()).queryLocationsForRun(runId);
	}

}
