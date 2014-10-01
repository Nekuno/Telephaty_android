package com.qnoow.telephaty;

public class Msg {

	private String Mac;
	private String message;
	private String time;
	
	
	
	public Msg(String mac, String message, String time) {
		super();
		Mac = mac;
		this.message = message;
		this.time = time;
	}
	
	
	public String getMac() {
		return Mac;
	}
	
	public void setMac(String mac) {
		Mac = mac;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
}
