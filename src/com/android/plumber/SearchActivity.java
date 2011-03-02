package com.android.plumber;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SearchActivity extends ListActivity {
	private SAXParser sax;
	private List<Entry> l;
	private HashMap<String, SoftReference<Bitmap>> cacheThumbnails;
	private MySearchListAdapter my_ExpListAdpt;
	
	public SearchActivity(){
		cacheThumbnails = new HashMap<String, SoftReference<Bitmap>>(0);
		my_ExpListAdpt = new MySearchListAdapter();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String uri = getIntent().getExtras().getString("uri");
		try {	
			sax = SAXParserFactory.newInstance().newSAXParser();
			YouTubeSearchHandler ytSearchHandler = new YouTubeSearchHandler();
			Log.println(Log.INFO, "SearchActivity.parse", "parse "+uri);
			sax.parse(uri, ytSearchHandler);
			
			/* costruisci lista dei risultati */
			l = ytSearchHandler.getEntryList();
			setListAdapter(my_ExpListAdpt);
			
			DbHelper dbh = new DbHelper(SearchActivity.this);
			dbh.open();
			Entry e;
			for(int i=0;i<l.size();i++){	
				e = l.get(i);
				dbh.addGlobal(e.getId(), e.getUri());
					//	Log.println(Log.INFO, "SearchActivity","\t addGlobal+"+e.toString());
				dbh.addinfo(e);
					//	Log.println(Log.INFO, "SearchActivity","\t addInfo+"+e.toString());
			}
			dbh.close();
			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public class MySearchListAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return l.size();
		}

		@Override
		public Object getItem(int position) {
			return l.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		public View getViewRow(){
			LayoutInflater li = getLayoutInflater();
			RelativeLayout ll = (RelativeLayout) li.inflate(R.layout.listitem_search, null);
            return ll;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout ll = (RelativeLayout) getViewRow();	

			registerForContextMenu(ll);
			ll.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showDialog(getListView().getPositionForView(v));
				}
			});

			TableRow r1 = (TableRow) ll.getChildAt(1);
			TableRow r2 = (TableRow) ll.getChildAt(2);
			
			ImageView img = (ImageView) ll.getChildAt(0);
			TextView text = (TextView) r1.getChildAt(0);
			TextView viewCount = (TextView) r2.getChildAt(0);
			
			String str = ((Entry)l.get(position)).getTitle();
			//if(str.length()>=25)
				//str = str.substring(0, 25);
			text.setText(str+"...");
			
			String uri = ((Entry)l.get(position)).getThumbs().get(1);
			
			BitmapDrawable d = getThumbnail(uri);
			if(d!=null) img.setImageDrawable(d);
			
			Entry e = (Entry) l.get(position);
			int count = e.getViewCount();
			viewCount.setText(viewCount.getText()+" "+count);
			return ll;
		} 	

		public BitmapDrawable getThumbnail(String uri) {
			BitmapDrawable d = null;
			if(cacheThumbnails.get(uri)==null){
				try {
					d = new BitmapDrawable(getResources(), new URL(uri).openStream());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(d!=null)
					cacheThumbnails.put(uri, new SoftReference<Bitmap>(d.getBitmap()));
			}
			else{
				Bitmap bitmap = cacheThumbnails.get(uri).get();
				d = new BitmapDrawable(getResources(), bitmap);
			}
			return d;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog = new Dialog(SearchActivity.this);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		ScrollView v = (ScrollView) getLayoutInflater().inflate(R.layout.listitem_dialog, null);
		dialog.addContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		final Entry e = l.get(id);	// id is the position in the arraylist
		LinearLayout ll = (LinearLayout) v.getChildAt(0);
		dialog.setTitle(e.getTitle().subSequence(0, 20)+"...");
		
		TableRow r1 = (TableRow) ll.getChildAt(0);	// desc
		TableRow r2 = (TableRow) ll.getChildAt(1);	// pub
		TableRow r3 = (TableRow) ll.getChildAt(2);	// upd
		TableRow r4 = (TableRow) ll.getChildAt(3);	// cats
		TableRow r5 = (TableRow) ll.getChildAt(4);	// auth
		
		TextView desc = (TextView) r1.getChildAt(1);
		TextView pub = (TextView) r2.getChildAt(1);
		TextView upd = (TextView) r3.getChildAt(1);
		TextView cats = (TextView) r4.getChildAt(1);
		TextView auth = (TextView) r5.getChildAt(1);		
		
		desc.setText(e.getContent());
		pub.setText(e.getPublished().toString());
		upd.setText(e.getUpdated().toString());
		cats.setText(e.getCats().get(1));
		for(int i=2;i<e.getCats().size();i++)
			cats.setText(cats.getText()+", "+e.getCats().get(i));
		auth.setText(e.getAuthor().getName());
		
		LinearLayout l2 = (LinearLayout) ll.getChildAt(5);
		Button b = (Button) l2.getChildAt(0);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		b = (Button) l2.getChildAt(1);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String link = e.getMedia().get(0).getLink();
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				i.addCategory(Intent.CATEGORY_BROWSABLE);
				i.setData(Uri.parse(link));
				startActivity(i);
				dialog.dismiss();
			}
		});
		
		return dialog;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		int position = getListView().getPositionForView(v);
		menu.add(position, 0, Menu.NONE, "Guarda il video");
		menu.add(position, 1, Menu.NONE, "Aggiungi a preferiti");
		menu.add(position, 2, Menu.NONE, "Aggiungi segnalibro di tempo");
		menu.add(position, 3, Menu.NONE, "Scarica video su SD");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		DbHelper dbh = new DbHelper(SearchActivity.this);
		dbh.open();
		Entry e = (Entry) getListAdapter().getItem(item.getGroupId());
		
		switch(item.getItemId()){
		case 0:
			String link = e.getMedia().get(0).getLink();
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.addCategory(Intent.CATEGORY_BROWSABLE);
			i.setData(Uri.parse(link));
			startActivity(i);
			break;
		case 1:
			dbh.addFavourites(e.getId());
			break;
		case 2:
			showTimePickerDialog(e.getId());
			break;
		case 3:
			Toast.makeText(getApplicationContext(), "da implementare", Toast.LENGTH_LONG).show();
			break;
		}
		dbh.close();
		return super.onContextItemSelected(item);
	}

	private void showTimePickerDialog(final String id) {
		OnTimeSetListener setTimeBookmark = new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				DbHelper dbh = new DbHelper(SearchActivity.this);
				dbh.open();
				Toast.makeText(getApplicationContext(), hourOfDay+":"+minute, Toast.LENGTH_LONG).show();
				dbh.setTimeBookmark(id, hourOfDay, minute);
			}
		};
		TimePickerDialog dialog = new TimePickerDialog(SearchActivity.this, setTimeBookmark, 0, 0, true);
		dialog.show();
	}
}
