package com.example.runtracker;

import android.support.v4.app.Fragment;

public class RunActivity extends SingleFragmentActtivity {

	public static final String EXTRA_RUN_ID = "com.example.runtracker.run_id";

	@Override
	protected Fragment createFragment() {
		long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
		if (runId != -1) {
			return RunFragment.newInstance(runId);
		} else {
			return new RunFragment();
		}

	}

}
