package com.qnoow.telephaty.bbdd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BBDDMensajes extends SQLiteOpenHelper {

	
	String sqlCreate = "CREATE TABLE Mensajes (id TEXT, mac TEXT)";

	public BBDDMensajes(Context contexto, String nombre, CursorFactory factory,
			int version) {
		super(contexto, nombre, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Se ejecuta la sentencia SQL de creación de la tabla
		db.execSQL(sqlCreate);
	}

	public boolean insert(String id, String mac) {

		SQLiteDatabase db = this.getWritableDatabase();

		if (db != null) {
			db.execSQL("INSERT INTO Mensajes (id, mac) "
					+ "VALUES ('" + id + "', '" + mac + "')");
			db.close();
		}
		
		return true;

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versionAnterior,
			int versionNueva) {
		db.execSQL("DROP TABLE IF EXISTS Mensajes");
		// Se crea la nueva versión de la tabla
		db.execSQL(sqlCreate);
	}
}
