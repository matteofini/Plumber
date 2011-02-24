package com.app.tubatuber;

import javax.xml.parsers.FactoryConfigurationError;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

public class PlayActivity extends Activity {

	Cursor c_fav;
	TableLayout tl1 = null;
	TableLayout tl2 = null;
	DbHelper dbh;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);
		dbh = new DbHelper(PlayActivity.this);
		dbh.open();
        
		final AutoCompleteTextView edit = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextView01);
		edit.setSelectAllOnFocus(true);
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
		startManagingCursor(c_fav);
		// TODO: quante linee nel cursore??
		System.out.println("\n\t"+c_fav.getCount());
		
		//	setListAdapter(new MyPlayListAdapter(c_fav));
        	// TODO: rimane in loop da qualche parte??
        System.out.println("\n\t ListAdapter settato!");
        dbh.close();
	}

	/*
	public class MyPlayListAdapter extends BaseAdapter{
		
		private Cursor mCursor;
		
		public MyPlayListAdapter(Cursor c){
			mCursor = c;
			Log.println(Log.INFO, "MyPlayListAdapter", "MyPlayListAdapter oggetto inizializzato");
		}

		@Override
		public int getCount() {
			return mCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			mCursor.moveToPosition(position);
			// return MediaLink row
			String id = mCursor.getString(0);
			MediaLink media = dbh.getMedia(id, 0);
			return media;
		}

		@Override
		public long getItemId(int position) {
			return position;	// medialink position in the cursor eg: mCursor.moveTo(position).getStringAt(coloumn)
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout v = (RelativeLayout) getLayoutInflater().inflate(R.layout.listitem_play, null);
			TextView title = (TextView) v.getChildAt(0);
			TextView count = (TextView) v.getChildAt(1);
			
			Button show = (Button) v.getChildAt(2);
			Button delete = (Button) v.getChildAt(3);
			
			mCursor.moveToPosition(position);
			final String id = mCursor.getString(0);
			title.setText(dbh.getTitle(id));
			count.setText(dbh.getViewCount(id)+"");
			
			show.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MediaLink media = dbh.getMedia(id, 0);	// link web
					Intent i = new Intent();
					i.setAction(Intent.ACTION_VIEW);
					i.addCategory(Intent.CATEGORY_BROWSABLE);
					i.setData(Uri.parse(media.getLink()));
				}
			});
			
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(dbh.deleteFavourites(id)<=0)
						Log.println(Log.DEBUG, "DeleteFavourites", "deleted 0 rows or an error occurred");
				}
			});
			
			return v;
		}
		
	}
	*/
}
