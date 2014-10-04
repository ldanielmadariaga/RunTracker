package com.example.runtracker;

import android.support.v4.app.Fragment;

public class RunListActivity extends SingleFragmentActtivity {

	@Override
	protected Fragment createFragment() {
		return new RunListFragment();
	}

}
