package com.qnoow.telephaty;

import java.sql.Timestamp;

public class Msg {

	private String Mac;
	private String message;
	private Timestamp time;
	
	
	
	public Msg(String mac, String message, Timestamp time) {
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
	
	public Timestamp getTime() {
		return time;
	}
	
	public void setTime(Timestamp time) {
		this.time = time;
	}
	
}
