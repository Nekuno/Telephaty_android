package com.qnoow.telephaty;

import java.sql.Timestamp;

public class Msg {

	private String Mac;
	private String message;
	private Timestamp time, destruction;
	private int privates;
	
	
	
	


	public Msg(String mac, String message, int privates, Timestamp time, Timestamp destruction) {
		super();
		Mac = mac;
		this.message = message;
		this.time = time;
		this.privates = privates;
		this.destruction = destruction;
	}
	
	public Timestamp getDestruction() {
		return destruction;
	}

	public void setDestruction(Timestamp destruction) {
		this.destruction = destruction;
	}

	public int getPrivates() {
		return privates;
	}


	public void setPrivates(int privates) {
		this.privates = privates;
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
