package com.example.runtracker;

import java.sql.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class RunDatabaseHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "runs.sqlite";
	private static final int VERSION = 1;
	private static final String TABLE_RUN = "run";
	private static final String COLUMN_RUN_ID = "_id";
	private static final String COLUMN_RUN_START_DATE = "start_date";

	private static final String TABLE_LOCATION = "location";
	private static final String COLUMN_LOCATION_LATITUDE = "latitude";
	private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
	private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
	private static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
	private static final String COLUMN_LOCATION_PROVIDER = "provider";
	private static final String COLUMN_LOCATION_RUN_ID = "run_id";

	public RunDatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table run (_id integer primary key autoincrement, start_date integer)");
		db.execSQL("create table location (timestamp integer, latitude real, longitude real, altitude real, provider varchar(100), run_id integer references run(_id))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

	public long insertRun(Run run) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
		return getWritableDatabase().insert(TABLE_RUN, null, contentValues);
	}

	public long insertLocation(long runId, Location location) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		contentValues.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
		contentValues.put(COLUMN_LOCATION_TIMESTAMP, location.getTime());
		contentValues.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
		contentValues.put(COLUMN_LOCATION_RUN_ID, runId);

		return getWritableDatabase().insert(TABLE_LOCATION, null, contentValues);

	}

	public RunCursor queryRuns() {
		Cursor wrapper = getReadableDatabase().query(TABLE_RUN, null, null, null, null, null,
				COLUMN_RUN_START_DATE + " asc");
		return new RunCursor(wrapper);
	}

	public RunCursor queryRun(long id) {
		Cursor wrapper = getReadableDatabase().query(TABLE_RUN, null, COLUMN_RUN_ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, null, "1");
		return new RunCursor(wrapper);
	}

	public static class RunCursor extends CursorWrapper {

		public RunCursor(Cursor cursor) {
			super(cursor);
		}

		public Run getRun() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;

			}

			Run run = new Run();
			long runId = getLong(getColumnIndex(COLUMN_RUN_ID));
			run.setId(runId);
			long startDate = getLong(getColumnIndex(COLUMN_RUN_START_DATE));
			run.setStartDate(new Date(startDate));
			return run;
		}

	}

	public LocationCursor queryLastLocationForRun(long runId) {
		Cursor wrapper = getReadableDatabase().query(TABLE_LOCATION, null, COLUMN_LOCATION_RUN_ID + " = ?",
				new String[] { String.valueOf(runId) }, null, null, COLUMN_LOCATION_TIMESTAMP + " desc", "1");
		return new LocationCursor(wrapper);
	}

	public LocationCursor queryLocationsForRun(long runId) {
		Cursor wrapper = getReadableDatabase().query(TABLE_LOCATION, null, COLUMN_LOCATION_RUN_ID + " = ?",
				new String[] { String.valueOf(runId) }, null, null, COLUMN_LOCATION_TIMESTAMP + " asc");
		return new LocationCursor(wrapper);
	}

	public static class LocationCursor extends CursorWrapper {

		public LocationCursor(Cursor cursor) {
			super(cursor);
		}

		public Location getLocation() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;
			}

			String provider = getString(getColumnIndex(COLUMN_LOCATION_PROVIDER));
			Location location = new Location(provider);
			location.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_LONGITUDE)));
			location.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_LATITUDE)));
			location.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_ALTITUDE)));
			location.setTime(getLong(getColumnIndex(COLUMN_LOCATION_TIMESTAMP)));
			return location;
		}
	}
}
