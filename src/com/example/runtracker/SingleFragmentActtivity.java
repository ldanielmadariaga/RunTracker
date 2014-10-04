package com.example.runtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.runtracker.R;

public abstract class SingleFragmentActtivity extends FragmentActivity {

	protected abstract Fragment createFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());
		initializeFragments();
	}

	protected int getLayoutResourceId() {
		return R.layout.activity_fragment;
	}

	private void initializeFragments() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

		if (fragment == null) {
			fragment = createFragment();
			fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();

		}
	}
}
