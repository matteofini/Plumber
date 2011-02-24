package com.app.tubatuber;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper {

	private  MySQLiteOpenHelper dbh;
	private  SQLiteDatabase db;
	
	private static final String NAME = "tuberdb";
	private static final int VERSION = 2;
	private static final String VIDEOID = "videoid";
	
	private final Context mCtx;
	
	private static final String DB0_CREATE = 
		"create table global ("+VIDEOID+" text primary key, uri text not null);";	
	
	private static final String DB1_CREATE = 
		"create table favourites ("+VIDEOID+" text unique not null references global ("+VIDEOID+"));";	
	
	private static final String DB2_CREATE = 
		"create table info ("+VIDEOID+" text unique not null references global (videoid)," +
				"title text not null, content text, published text not null, updated text not null," +
				"uri text not null, author_name text not null, author_uri text not null," +
				"count integer not null);";	
	
	private static final String CATS_CREATE = 
		"create table categories ("+VIDEOID+" text not null references global ("+VIDEOID+"), cat text unique not null);";

	private static final String MEDIA_CREATE = 
		"create table media ("+VIDEOID+" text not null references global ("+VIDEOID+"), link text unique not null, type text not null, format integer not null, duration integer not null);";
	
	private static final String THUMBS_CREATE = 
		"create table thumbnails ("+VIDEOID+" text not null references global ("+VIDEOID+"), thumb text unique not null);";
	
	private static final String LINKS_CREATE = 
		"create table links ("+VIDEOID+" text not null references global ("+VIDEOID+"), link text unique not null);";
	
	
	private static final String DB3_CREATE = 
		"create table bookmarks ("+VIDEOID+" text unique not null references global ("+VIDEOID+"), time text default NULL);";
	
	private class MySQLiteOpenHelper extends SQLiteOpenHelper{

		public MySQLiteOpenHelper(Context context) {
			super(context, NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB0_CREATE);
			db.execSQL(DB1_CREATE);	
			db.execSQL(DB2_CREATE);
			db.execSQL(DB3_CREATE);
			db.execSQL(CATS_CREATE);
			db.execSQL(MEDIA_CREATE);
			db.execSQL(THUMBS_CREATE);
			db.execSQL(LINKS_CREATE);
			Log.println(Log.VERBOSE, "MySQLiteOpenHelper", DB0_CREATE+"  "+DB1_CREATE+"  "+DB2_CREATE+"  "+DB3_CREATE+" "+CATS_CREATE+" "+MEDIA_CREATE+" "+THUMBS_CREATE+" "+LINKS_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("MySQLiteOpenHelper", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS favourites");
            db.execSQL("DROP TABLE IF EXISTS info");
            db.execSQL("DROP TABLE IF EXISTS bookmarks");
            db.execSQL("DROP TABLE IF EXISTS global");
            onCreate(db);
		}		
	}
	
	
	public DbHelper(Context ctx) {
		this.mCtx = ctx;
	}
	
	public DbHelper open(){
		Log.println(Log.INFO, "DbHelper", "Open database "+NAME);
		dbh = new MySQLiteOpenHelper(mCtx);
		db = dbh.getWritableDatabase();
		return this;
	}
	
	public void close(){
		Log.println(Log.INFO, "DbHelper", "Close database "+NAME);
		dbh.close();
	}
	
	public void delete(){
		Log.println(Log.INFO, "DbHelper", "Delete database "+NAME);
		db.delete("favourites", "1", null);
		db.delete("info", "1", null);
		db.delete("bookmarks", "1", null);
		db.delete("global", "1", null);
	}
	
	
	public void addGlobal(String id, String uri){
		ContentValues cv = new ContentValues(); 
		cv.put(VIDEOID, id);
		cv.put("uri", uri);
		try{
			db.insertOrThrow("global", null, cv);
		}
		catch(SQLiteConstraintException e){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException e){
			System.out.println("Error inserting "+cv.toString());
		}
	}
	
	public Cursor getGlobals(){
		Cursor c = db.query("global", new String[] {VIDEOID}, null, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	public Cursor getGlobal(String videoid){
		Cursor c = db.query("global", new String[] {VIDEOID}, "videoid="+videoid, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	public void addinfo(Entry e){
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, e.getId());
		cv.put("title", e.getTitle());
		cv.put("content", e.getContent());
		cv.put("published", e.getPublished().toGMTString());
		cv.put("updated", e.getUpdated().toGMTString());
		cv.put("uri", e.getUri());
		cv.put("author_name", e.getAuthor().getName());
		cv.put("author_uri", e.getAuthor().getUri());
		cv.put("count", e.getViewCount());
		
		for(int i=0;i<e.getCats().size();i++){
			addCategory(e.getId(), e.getCats().get(i));
		}
		for(int i=0;i<e.getMedia().size();i++){
			addMedia(e.getId(), e.getMedia().get(i));
		}
		for(int i=0;i<e.getThumbs().size();i++){
			addThumbnails(e.getId(), e.getThumbs().get(i));
		}
		for(int i=0;i<e.getLinks().size();i++){
			addLinks(e.getId(), e.getLinks().get(i));
		}
		
		try{
			db.insertOrThrow("info", null, cv);
		}
		catch(SQLiteConstraintException exc){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException exc){
			System.out.println("Error inserting "+cv.toString());
		}
	}
	
		public Cursor getInfo(String id){
		Cursor c = db.rawQuery("SELECT * FROM info WHERE videoid='"+id+"'", null);
		if (c != null)
			c.moveToFirst();
		return c;		
	}

	public String getTitle(String id){
		Cursor c = db.rawQuery("SELECT title FROM info WHERE videoid='"+id+"'", null);
		if (c != null)
			c.moveToFirst();
		String str = c.getString(0);
		c.close();
		return str;
	}

	public String getContent(String id){
		Cursor c = db.rawQuery("SELECT content FROM info WHERE videoid='"+id+"'", null);
		if (c != null)
			c.moveToFirst();
		String str = c.getString(0);
		c.close();
		return str;
	}

	public Date getPublished(String id){
		Cursor c = db.rawQuery("SELECT published FROM info WHERE videoid='"+id+"'", null);
		if (c != null)
			c.moveToFirst();
		Date str = new Date(c.getString(0));
		c.close();
		return str;
	}

	public Date getUpdated(String id){
		Cursor c = db.rawQuery("SELECT updated FROM info WHERE videoid='"+id+"'", null);
		if (c != null)
			c.moveToFirst();
		Date str = new Date(c.getString(0));
		c.close();
		return str;
	}

	public int getViewCount(String id){
		Cursor c = db.rawQuery("SELECT count FROM info WHERE videoid='"+id+"'", null);
		if (c != null)
			c.moveToFirst();
		String str = c.getString(0);
		c.close();
		return Integer.valueOf(str);
	}

	
	/*************************************************************************
	 * auxiliary functions for addInfo():
	 * insert (links, categories, thumbnails and media) informations 
	 * connected to the info table into the database
	 ************************************************************************/
	 
	 
	private void addLinks(String id, String string) {
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, id);
		cv.put("link", string);
		try{
			db.insertOrThrow("links", null, cv);
		}
		catch(SQLiteConstraintException exc){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException exc){
			System.out.println("Error inserting "+cv.toString());
		}
		
	}
	
	private void addThumbnails(String id, String string) {
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, id);
		cv.put("thumb", string);
		try{
			db.insertOrThrow("thumbnails", null, cv);
		}
		catch(SQLiteConstraintException exc){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException exc){
			System.out.println("Error inserting "+cv.toString());
		}
	}

	private void addMedia(String id, MediaLink mediaLink) {
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, id);
		cv.put("link", mediaLink.getLink());
		cv.put("type", mediaLink.getType());
		cv.put("duration", mediaLink.getDuration());
		cv.put("format", mediaLink.getFormat());
		try{
			db.insertOrThrow("media", null, cv);
		}
		catch(SQLiteConstraintException exc){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException exc){
			System.out.println("Error inserting "+cv.toString());
		}
	}
	
	public MediaLink getMedia(String id, int index){
		MediaLink ml = new MediaLink();
		Cursor c = db.query("media", null, "videoid="+id, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		
		if(c.moveToPosition(index)){
			ml.setLink(c.getString(0));
			ml.setType(c.getString(1));
			ml.setFormat(c.getInt(2));
			ml.setDuration(c.getInt(3));
			return ml;
		}
		else return null;
	}

	private void addCategory(String id, String string) {
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, id);
		cv.put("cat", string);
		try{
			db.insertOrThrow("categories", null, cv);
		}
		catch(SQLiteConstraintException exc){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException exc){
			System.out.println("Error inserting "+cv.toString());
		}
	}

	
	/*************************************************************************
	 * manage favourites and bookmarks tables
	 ************************************************************************/
	
	
	public long addFavourites(String id){
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, id);
		long res = -1;
		try{
			res = db.insertOrThrow("favourites", null, cv);
		}
		catch(SQLiteConstraintException e){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException e){
			System.out.println("Error inserting "+cv.toString());
		}
		return res;
	}

	public Cursor getFavourites() {
		Cursor c = db.query("favourites", new String[]{"videoid"}, null, null, null, null, null);
		if (c != null)
			c.moveToFirst();
		return c;
	}
	
	public int deleteFavourites(String id) {
		int i=0;
		try{
			i=db.delete("favourites", "videoid="+id, null);
		}
		catch(SQLiteException e){
			e.printStackTrace();
		}
		return i;
	}
	
	public long addBookmarks(String id){
		ContentValues cv = new ContentValues();
		cv.put(VIDEOID, id);
		long res = -1;
		try{
			res = db.insertOrThrow("bookmarks", null, cv);
		}
		catch(SQLiteConstraintException e){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException e){
			System.out.println("Error inserting "+cv.toString());
		}
		return res;
	}
	
	public void setTimeBookmark(String id, int h, int m){
		ContentValues cv = new ContentValues();
		cv.put("time", ""+h+":"+m);
		try{
			db.execSQL("UPDATE bookmarks SET time="+h+":"+m+" WHERE videoid="+id);
			//res = db.update("bookmarks", cv, VIDEOID+"="+id, null);
		}
		catch(SQLiteConstraintException e){
			//System.out.println("Error inserting, key already present: "+cv.toString());
		}
		catch(SQLiteException e){
			System.out.println("Error inserting "+cv.toString());
		}
	}
}
