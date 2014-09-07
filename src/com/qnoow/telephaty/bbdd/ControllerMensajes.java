package com.qnoow.telephaty.bbdd;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ControllerMensajes {
	String[] campos;
	final SQLiteDatabase db;
	BBDDMensajes mensajesDB;

	public ControllerMensajes(Context context) {
		mensajesDB = new BBDDMensajes(context, "Mensajes", null, 1);
		db = mensajesDB.getWritableDatabase();
		campos = new String[] { "id", "mac" };
	}

	public boolean insert(String id, String mac) {
		String[] values = new String[] { id, mac };
		Cursor cursor = db.query("Mensajes", campos, "id='" + id
				+ "' AND mac='" + mac + "'", null, null, null, null);
		if (cursor != null && !cursor.isClosed()) {
			// TODO check the cursor 
			if (!cursor.isFirst()) {
				mensajesDB.insert(id, mac);
				return true;
			}
			
		}
		cursor.close();
		return false;

	}
}
