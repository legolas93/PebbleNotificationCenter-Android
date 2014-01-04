package com.matejdro.pebblenotificationcenter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.matejdro.pebblenotificationcenter.R;

public class PerAppActivity extends ActionBarActivity {
	public static String appName; //Package considered
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
	    appName = intent.getStringExtra("PER_APP_PACKAGE_NAME");
	    
		setContentView(R.layout.activity_per_app);
	}
}
