package com.android.plumber;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class UploadActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textview = new TextView(this);
		textview.setText("Upload Activity");
		setContentView(textview);
	}
	

}
