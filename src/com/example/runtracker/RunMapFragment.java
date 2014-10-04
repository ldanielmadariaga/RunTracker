package com.example.runtracker;

import java.util.Date;

import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.runtracker.RunDatabaseHelper.LocationCursor;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RunMapFragment extends SupportMapFragment implements LoaderCallbacks<Cursor> {

	private static final String ARG_RUN_ID = "RUN_ID";
	private static final int LOAD_LOCATIONS = 0;

	private GoogleMap googleMap;
	private LocationCursor locationCursor;

	public static RunMapFragment newInstance(long runId) {
		Bundle argumentBundle = new Bundle();
		argumentBundle.putLong(ARG_RUN_ID, runId);
		RunMapFragment runMapFragment = new RunMapFragment();
		runMapFragment.setArguments(argumentBundle);
		return runMapFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			long runId = args.getLong(ARG_RUN_ID, -1);
			if (runId != -1) {
				LoaderManager loaderManager = getLoaderManager();
				loaderManager.initLoader(LOAD_LOCATIONS, args, this);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		googleMap = getMap();
		googleMap.setMyLocationEnabled(true);
		return view;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long runId = args.getLong(ARG_RUN_ID, -1);
		return new LocationCursorLoader(getActivity(), runId);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		locationCursor = (LocationCursor) cursor;
		upadteUI();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		locationCursor.close();
		locationCursor = null;
	}

	private void upadteUI() {
		if (googleMap == null || locationCursor == null) {
			return;
		}

		PolylineOptions line = new PolylineOptions();
		LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
		locationCursor.moveToFirst();
		while (!locationCursor.isAfterLast()) {
			Location location = locationCursor.getLocation();
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			Resources resources = getResources();

			if (locationCursor.isFirst()) {

				String startDate = new Date(location.getTime()).toString();
				MarkerOptions startMarkerOptions = new MarkerOptions().position(latLng)
						.title(resources.getString(R.string.run_start))
						.snippet(resources.getString(R.string.run_started_at_format, startDate));
				googleMap.addMarker(startMarkerOptions);

			} else if (locationCursor.isLast()) {

				String endDate = new Date(location.getTime()).toString();
				MarkerOptions finishMarkerOptions = new MarkerOptions().position(latLng)
						.title(resources.getString(R.string.run_finish))
						.snippet(resources.getString(R.string.run_finished_at_format, endDate));
				googleMap.addMarker(finishMarkerOptions);
			}

			line.add(latLng);
			latLngBuilder.include(latLng);
			locationCursor.moveToNext();
		}
		googleMap.addPolyline(line);
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		LatLngBounds latLngBounds = latLngBuilder.build();
		@SuppressWarnings("deprecation")
		CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBounds, display.getWidth(),
				display.getHeight(), 15);
		googleMap.moveCamera(movement);
	}
}
