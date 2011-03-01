package com.android.plumber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class YouTubeSearchHandler extends DefaultHandler {
	
	private String reading;
	private boolean in_entry;
	private boolean in_media;	
	public List<Entry> entrylist;

	
	public YouTubeSearchHandler(){
		entrylist = new ArrayList<Entry>(0);
		in_entry = in_media = false;
		reading="";
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		reading = localName;
		if(reading.equals("entry")){
			in_entry = true;
			Entry e = new Entry();
			entrylist.add(e);
		}
		else if(in_entry){
			Entry e = (Entry) entrylist.get(entrylist.size()-1);
			/* category is an empty element with only attributes */
			if(reading.equals("category")){
				String term = attributes.getValue("term");
				if(term!=null)
					e.getCats().add(term);
			}
			else if(reading.equals("link")){
				Link l = new Link();	
				String rel = attributes.getValue("rel");
				String href = attributes.getValue("href");
				String type = attributes.getValue("type");
				if(rel.equals("self"))
					e.setUri(href);
				else{
					l.setRel(rel);
					l.setHref(href);
					l.setType(type);
					e.getLinks().add(l);
				}
			}
			else if(reading.equals("group")){	// media:group
				in_media = true;
			}
			else if(reading.equals("content") && in_media){
				MediaLink ml = new MediaLink();
				ml.setLink(attributes.getValue("url"));
				ml.setDuration(Integer.valueOf(attributes.getValue("duration")));
				String str = attributes.getValue("http://gdata.youtube.com/schemas/2007", "format");
				ml.setFormat(Integer.valueOf(str));	
				ml.setType(attributes.getValue("type"));
				e.getMedia().add(ml);
			}
			else if(reading.equals("thumbnail") && in_media){
				e.getThumbs().add(attributes.getValue("url"));
			}
			else if(reading.equals("author")){
				Author author = new Author();
				e.setAuthor(author);
			}
			else if(reading.equals("statistics")){
				e.setViewCount(Integer.valueOf(attributes.getValue("viewCount")));
			}
		}
	}

	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.equals("entry"))
			in_entry = false;
		else if(localName.equals("group"))	// media:group
			in_media = false;
	}

	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		
		/* add a new entry in the list */
		if(in_entry){
			Entry e = (Entry) entrylist.get(entrylist.size()-1);
			
			/* title */
			if(reading.equals("title"))
				e.setTitle(String.copyValueOf(ch, start, length));	
			/* id */
			else if(reading.equals("id")){
				String uri = String.copyValueOf(ch, start, length);
				String[] split = uri.split("/");
				String id = split[split.length-1];
				e.setId(id);
			}
			/* published */
			else if(reading.equals("published")){
				String str = String.copyValueOf(ch, start, length);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date date = null;
				try {
					date = sdf.parse(str);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				e.setPublished(date);
			}
			/* updated */
			else if(reading.equals("updated")){
				String str = String.copyValueOf(ch, start, length);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date date = null;
				try {
					date = sdf.parse(str);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				e.setUpdated(date);
			}
			else if(reading.equals("name")){
				e.getAuthor().setName(String.copyValueOf(ch, start, length));
			}
			else if(reading.equals("uri")){
				e.getAuthor().setUri(String.copyValueOf(ch, start, length));
			}
			/* content */
			else if(reading.equals("content"))
				e.setContent(String.copyValueOf(ch, start, length));
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
	
	public Entry getEntry(int i){
		return (Entry) entrylist.get(i);
	}
	
	public List<Entry> getEntryList(){
		return entrylist;
	}
	
	public int results(){
		return entrylist.size();
	}
}
