package com.qnoow.telephaty.bbdd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ControllerMensajes {
	SQLiteDatabase db;
	BBDDMensajes mensajesDB;
	String[] campos = new String[] { "id", "mac" };

	public ControllerMensajes(Context context) {
		mensajesDB = new BBDDMensajes(context, "Mensajes", null, 1);
	}

	public boolean insert(String id, String mac) {
		db = mensajesDB.getWritableDatabase();
		if (db != null && !internalsearch(id, mac)) {
			Log.w("BBDD", "Inseertamos  id = " + id + "mac = " + mac);
			db.execSQL("INSERT INTO Mensajes (id, mac) " + "VALUES ('" + id
					+ "', '" + mac + "')");
			db.close();
			return true;
		}
		return false;

	}


	private boolean internalsearch(String id, String mac) {

		Cursor c = db.query("Mensajes", campos, null, null, null, null, null);

		if (c.moveToFirst()) {
			do {
				if (c.getString(0).equals(id) && c.getString(1).equals(mac)){
					Log.w("BBDD", "YA TENEMOS A  id = " + id + "mac = " + mac);
					return true;
				}
			} while (c.moveToNext());
		}
		Log.w("BBDD", "No tenemos a id = " + id + "mac = " + mac);
		return false;
	}

	public boolean search(String id, String mac) {
		db = mensajesDB.getWritableDatabase();
		Cursor c = db.query("Mensajes", campos, null, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				if (c.getString(0).equals(id) && c.getString(1).equals(mac)){
					Log.w("BBDD", "YA TENEMOS A  id = " + id + "mac = " + mac);
					return true;
				}
			} while (c.moveToNext());
		}
		db.close();
		Log.w("BBDD", "No tenemos a id = " + id + "mac = " + mac);
		return false;
	}

}
