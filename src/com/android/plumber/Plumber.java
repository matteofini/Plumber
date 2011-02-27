package com.android.plumber;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Plumber extends TabActivity  {
    /** Called when the activity is first created. */
	
	//public static YouTubeService service;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //service = new YouTubeService("myapp");
        
        TabSpec a_tab = null;
        TabHost tabhost = (TabHost) getTabHost();
        Intent playIntent = new Intent().setClass(this, PlayActivity.class);
        Intent uploadIntent = new Intent(this, UploadActivity.class);
        
        a_tab  = tabhost.newTabSpec("Play").setIndicator("Play").setContent(playIntent);
        tabhost.addTab(a_tab);
        a_tab  = tabhost.newTabSpec("Upload").setIndicator("Upload").setContent(uploadIntent);
        tabhost.addTab(a_tab);
        
        tabhost.setCurrentTab(0);
    } 	
}