package com.app.tubatuber;

public class MediaLink{
	private String link;
	private String type;
	private int format;
	private int duration;
	
	public String getLink() {
		return link;
	}
	public String getType() {
		return type;
	}
	public int getFormat() {
		return format;
	}
	public int getDuration() {
		return duration;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
}
