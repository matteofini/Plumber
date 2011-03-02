package com.android.plumber;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.FactoryConfigurationError;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends ListActivity {

	private Cursor c_fav;
	TableLayout tl1 = null;
	TableLayout tl2 = null;
	DbHelper dbh;
	
	public PlayActivity(){
		dbh = new DbHelper(this);
	}
	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);
		dbh.open();
		
		final AutoCompleteTextView edit = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextView01);
		Button button = (Button) findViewById(R.id.SearchButton);

		OnClickListener clk;
		button.setOnClickListener(clk = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = edit.getText().toString();	// TODO: gestire la ricerca di più parole spaziate
				text="burattodanza";
				if(text==""){
					Toast.makeText(PlayActivity.this, "inserisci qualcosa da cercare", Toast.LENGTH_SHORT).show();
					return;
				}
				String uri ="http://gdata.youtube.com/feeds/api/videos?q="+text+"&orderby=viewCount&start-index=1&max-results=20";
				try {
					final Intent i = new Intent();					
					i.setClass(PlayActivity.this, SearchActivity.class);
					i.putExtra("uri", uri);
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							startActivity(i);
						}
					});
					t.run();				
				} catch (FactoryConfigurationError e) {
					e.printStackTrace();
				}
			}
		});
		/*
		 * http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed
		 * http://gdata.youtube.com/feeds/api/users/username/uploads
		 */ 
		c_fav = dbh.getFavourites();
		setListAdapter(new MyPlayListAdapter());
		//startManagingCursor(c_fav);
		dbh.close();
	}
	
	
	public class MyPlayListAdapter extends BaseAdapter{			
		DbHelper dbh;
		
		public MyPlayListAdapter() {
			dbh = new DbHelper(PlayActivity.this);
		}
		
		@Override
		public int getCount() {
			return c_fav.getCount();
		}
	
		@Override
		public Object getItem(int position) {
			c_fav.moveToPosition(position);
			// return MediaLink row
			String id = c_fav.getString(0);
			dbh.open();
			MediaLink media = dbh.getMedia(id, 0);
			dbh.close();
			return media;
		}
	
		@Override
		public long getItemId(int position) {
			return position;	// medialink position in the cursor eg: mCursor.moveTo(position).getStringAt(coloumn)
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			dbh.open();
			LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.listitem_play, null);
			TextView title = (TextView) v.findViewById(R.id.favourites_title);
			TextView count = (TextView) v.findViewById(R.id.favourites_count);
			Button show = (Button) v.findViewById(R.id.buttonShow);
			Button delete = (Button) v.findViewById(R.id.buttonDel);
			ImageView img = (ImageView) v.findViewById(R.id.thumb);
			
			c_fav.moveToPosition(position);
			final String id = c_fav.getString(0);
			
			try {
				String img_uri = dbh.getThumbs(id);
				BitmapDrawable bitmap;
				bitmap = new BitmapDrawable(getResources(), new URL(img_uri).openStream());
				img.setImageDrawable(bitmap);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			title.setText(dbh.getTitle(id));
			count.setText(getString(R.string.viewCountLabel)+": "+dbh.getViewCount(id)+"");
			
			show.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.setAction(Intent.ACTION_VIEW);
					i.addCategory(Intent.CATEGORY_BROWSABLE);
					String uri = "http://m.youtube.com/watch?v="+id;
					i.setData(Uri.parse(uri));
						Log.println(Log.INFO, "PlayActivity", "watch video "+uri);
					startActivity(i);

				}
			});
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DbHelper dbh = new DbHelper(PlayActivity.this);
					dbh.open();
					if(dbh.deleteFavourites(id)<=0)
						Log.println(Log.DEBUG, "DeleteFavourites", "deleted 0 rows or an error occurred");
					dbh.close();
				}
			});
			dbh.close();
			return v;
		}	
	}
}
