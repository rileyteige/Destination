package com.destination.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String TABLE_DESTINATIONS = "destinations";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_STREET_ADDRESS = "streetAddress";
	public static final String COLUMN_CITY = "city";
	public static final String COLUMN_STATE = "state";
	public static final String COLUMN_ZIPCODE = "zipCode";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	
	private static final String DATABASE_NAME = "destinations.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = "create table "
		+ TABLE_DESTINATIONS + "(" + COLUMN_ID
		+ " integer primary key autoincrement, " + COLUMN_NAME
		+ " text not null, " + COLUMN_STREET_ADDRESS
		+ " text not null, " + COLUMN_CITY
		+ " text not null, " + COLUMN_STATE
		+ " text not null, " + COLUMN_ZIPCODE
		+ " text not null, " + COLUMN_LATITUDE
		+ " real not null, " + COLUMN_LONGITUDE
		+ " real not null);";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database " + DATABASE_NAME + " from version "
				+ oldVersion + " to " + newVersion + ", removing all data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESTINATIONS);
		onCreate(db);
	}
}
