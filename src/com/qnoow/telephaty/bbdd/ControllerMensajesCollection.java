package com.qnoow.telephaty.bbdd;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qnoow.telephaty.Msg;
import com.qnoow.telephaty.Bluetooth.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ControllerMensajesCollection {
	SQLiteDatabase db;
	BBDDMensajes mensajesDB;
	List<Msg> items = new ArrayList<Msg>(); 

	public ControllerMensajesCollection(Context context) {
		mensajesDB = new BBDDMensajes(context, "MensajesCollection", null, 1);
	}

	public void insert(Msg msg) {
		db = mensajesDB.getWritableDatabase();
			db.execSQL("INSERT INTO MensajesCollection (mac, msg, privates, time, destruction) " + "VALUES ('" + msg.getMac() + "', '" + msg.getMessage() +  "', " + msg.getPrivates() +", '" + msg.getTime() + "', '" + msg.getDestruction() + "' )");
			db.close();
	}

	


	public List<Msg> search() {
		db = mensajesDB.getWritableDatabase();
		Cursor c = db.query("MensajesCollection", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				items.add(new Msg(c.getString(0), c.getString(1), c.getInt(2), Timestamp.valueOf(c.getString(3)), Timestamp.valueOf(c.getString(4))));
			} while (c.moveToNext());
		}
		db.close();
		return items;
	}

	
	public boolean search(Timestamp time) {
		db = mensajesDB.getWritableDatabase();
		Cursor c = db.query("MensajesCollection", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Log.d("BBDD time",Timestamp.valueOf(c.getString(4)).toGMTString());
				Log.d("BBDD bd",Timestamp.valueOf(c.getString(4)).toGMTString());
				if (Timestamp.valueOf(c.getString(4)).toGMTString().equals(time.toGMTString()) ) {
					delete(new Msg(c.getString(0), c.getString(1), c.getInt(2), Timestamp.valueOf(c.getString(3)), Timestamp.valueOf(c.getString(4))));
				}
			} while (c.moveToNext());
		}
		db.close();
		
		return false;
	}
	
	
	public void delete(Msg msg){
		db = mensajesDB.getWritableDatabase();
		db.delete("MensajesCollection", "mac = '" + msg.getMac() + "' AND msg = '" + msg.getMessage() + "' AND time = '" + msg.getTime() + "'", null);
		db.close();
	}
	

}
