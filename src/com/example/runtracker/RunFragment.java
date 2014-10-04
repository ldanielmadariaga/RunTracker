package com.example.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RunFragment extends Fragment {

	private static final String TAG = "RunFragment";
	private static final String ARG_RUN_ID = "RUN_ID";
	private static final int LOAD_RUN = 0;
	private static final int LOAD_LOCATION = 1;

	private Button startButton;
	private Button stopButton;
	private Button mapButton;
	private TextView startedTextView;
	private TextView latitudetTextView;
	private TextView longitudetTextView;
	private TextView altitudeTextView;
	private TextView durationtTextView;

	private Location lastLocation;
	private Run run;
	private RunManager runManager;
	private BroadcastReceiver locationReceiver = new LocationReceiver() {

		@Override
		protected void onLocationReceived(Context context, Location location) {
			if (!runManager.isTrackingRun(run)) {
				return;
			}
			lastLocation = location;
			if (isVisible()) {
				updateUI();
			}
		};

		@Override
		protected void onProviderEnabledChanged(boolean enabled) {
			int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
			Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
		};
	};

	public static RunFragment newInstance(long runId) {
		Bundle arguments = new Bundle();
		arguments.putLong(ARG_RUN_ID, runId);
		RunFragment runFragment = new RunFragment();
		runFragment.setArguments(arguments);
		return runFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		runManager = RunManager.getInstance(getActivity());
		Bundle argumentsBundle = getArguments();
		if (argumentsBundle != null) {
			long runId = argumentsBundle.getLong(ARG_RUN_ID, -1);
			if (runId != -1) {
				LoaderManager loaderManager = getLoaderManager();
				loaderManager.initLoader(LOAD_RUN, argumentsBundle, new RunLoaderCallbacks());
				loaderManager.initLoader(LOAD_LOCATION, argumentsBundle, new LocationLoaderCallbacks());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_run, container, false);

		startedTextView = (TextView) view.findViewById(R.id.run_startedTextView);
		latitudetTextView = (TextView) view.findViewById(R.id.run_latitudeTextView);
		longitudetTextView = (TextView) view.findViewById(R.id.run_longitudeTextView);
		altitudeTextView = (TextView) view.findViewById(R.id.run_altitudeTextView);
		durationtTextView = (TextView) view.findViewById(R.id.run_durationTextView);

		startButton = (Button) view.findViewById(R.id.run_startButton);
		startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (run == null) {
					run = runManager.startNewRun();
				} else {
					runManager.startTrackingRun(run);
				}
				updateUI();
			}
		});

		stopButton = (Button) view.findViewById(R.id.run_stopButton);
		stopButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				runManager.stopRun();
				updateUI();
			}
		});

		mapButton = (Button) view.findViewById(R.id.run_mapButton);
		mapButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), RunMapActivity.class);
				intent.putExtra(RunMapActivity.EXTRA_RUN_ID, run.getId());
				startActivity(intent);
			}
		});

		updateUI();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().registerReceiver(locationReceiver, new IntentFilter(RunManager.ACTION_LOCATION));
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(locationReceiver);
		super.onStop();
	}

	private void updateUI() {
		boolean started = runManager.isTrackingRun();
		boolean trackingThisRun = runManager.isTrackingRun(run);

		if (run != null) {
			startedTextView.setText(run.getStartDate().toString());
		}

		int durationSeconds = 0;
		if (run != null && lastLocation != null) {
			durationSeconds = run.getDurationSeconds(lastLocation.getTime());
			latitudetTextView.setText(Double.toString(lastLocation.getLatitude()));
			longitudetTextView.setText(Double.toString(lastLocation.getLongitude()));
			altitudeTextView.setText(Double.toString(lastLocation.getAltitude()));
			mapButton.setEnabled(true);
		} else {
			mapButton.setEnabled(false);
		}

		durationtTextView.setText(Run.formatDuration(durationSeconds));

		startButton.setEnabled(!started);
		stopButton.setEnabled(started && trackingThisRun);

	}

	private class RunLoaderCallbacks implements LoaderCallbacks<Run> {

		@Override
		public Loader<Run> onCreateLoader(int id, Bundle args) {
			return new RunLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}

		@Override
		public void onLoadFinished(Loader<Run> loader, Run run) {
			RunFragment.this.run = run;
			updateUI();
		}

		@Override
		public void onLoaderReset(Loader<Run> loader) {

		}

	}

	private class LocationLoaderCallbacks implements LoaderCallbacks<Location> {

		@Override
		public Loader<Location> onCreateLoader(int id, Bundle args) {
			return new LastLocationLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}

		@Override
		public void onLoadFinished(Loader<Location> loader, Location location) {
			lastLocation = location;
			updateUI();
		}

		@Override
		public void onLoaderReset(Loader<Location> loader) {

		}
	}
}
