package com.android.plumber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Entry{
	
	public Entry() {
		cats = new ArrayList<String>(0);
		media = new ArrayList<MediaLink>(0);
		thumbs = new ArrayList<String>(0);
		author = new Author();
		links = new ArrayList<String>(0);
	}
	
	private String id;
	private String title;
	private String content;
	private Date published;
	private Date updated;
	private List<String> cats;
	private List<String> links;
	private String uri;
	private Author author;
	private List<MediaLink> media;
	private List<String> thumbs;
	private int viewCount;
	
	public String getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getContent() {
		return content;
	}
	public Date getPublished() {
		return published;
	}
	public Date getUpdated() {
		return updated;
	}
	public List<String> getCats() {
		return cats;
	}
	public String getUri() {
		return uri;
	}
	public Author getAuthor() {
		return author;
	}
	public List<MediaLink> getMedia() {
		return media;
	}
	public List<String> getThumbs() {
		return thumbs;
	}
	public int getViewCount() {
		return viewCount;
	}
	public List<String> getLinks() {
		return links;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setPublished(Date published) {
		this.published = published;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public void setCats(List<String> cats) {
		this.cats = cats;
	}
	public void setLinks(List<String> links) {
		this.links = links;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	public void setMedia(List<MediaLink> media) {
		this.media = media;
	}
	public void setThumbs(List<String> thumbs) {
		this.thumbs = thumbs;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	
}
