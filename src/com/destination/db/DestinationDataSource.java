package com.destination.db;

import java.util.ArrayList;
import java.util.List;

import com.destination.models.Destination;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DestinationDataSource {
		private SQLiteDatabase database;
		private DatabaseHelper dbHelper;
		private String[] allColumns = {
			DatabaseHelper.COLUMN_ID,
			DatabaseHelper.COLUMN_NAME,
			DatabaseHelper.COLUMN_STREET_ADDRESS,
			DatabaseHelper.COLUMN_CITY,
			DatabaseHelper.COLUMN_STATE,
			DatabaseHelper.COLUMN_ZIPCODE,
			DatabaseHelper.COLUMN_LATITUDE,
			DatabaseHelper.COLUMN_LONGITUDE
		};
		
		public DestinationDataSource(Context context) {
			dbHelper = new DatabaseHelper(context);
		}
	
		public void open() throws SQLException {
			database = dbHelper.getWritableDatabase();
		}
		
		public void close() {
			dbHelper.close();
		}
		
		public Destination createDestination(String name,
				String streetAddress,
				String city,
				String state,
				String zipCode,
				double latitude,
				double longitude) {
			
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.COLUMN_NAME, name);
			values.put(DatabaseHelper.COLUMN_STREET_ADDRESS, streetAddress);
			values.put(DatabaseHelper.COLUMN_CITY, city);
			values.put(DatabaseHelper.COLUMN_STATE, state);
			values.put(DatabaseHelper.COLUMN_ZIPCODE, zipCode);
			values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
			values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);
			
			long id = database.insert(DatabaseHelper.TABLE_DESTINATIONS, null, values);
			Cursor cursor = database.query(DatabaseHelper.TABLE_DESTINATIONS, allColumns, DatabaseHelper.COLUMN_ID + " = " + id, null, null, null, null);
			cursor.moveToFirst();
			Destination dest = destinationFromCursor(cursor);
			cursor.close();
			return dest;
		}
		
		public void deleteDestination(Destination dest) {
			long id = dest.getId();
			Log.w(DestinationDataSource.class.getName(), "Comment deleted with id: " + id);
			database.delete(DatabaseHelper.TABLE_DESTINATIONS, DatabaseHelper.COLUMN_ID + " = " + id, null);
		}
		
		public List<Destination> getAllDestinations() {
			List<Destination> destinations = new ArrayList<Destination>();
			
			Cursor cursor = database.query(DatabaseHelper.TABLE_DESTINATIONS, allColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				Destination dest = destinationFromCursor(cursor);
				destinations.add(dest);
				cursor.moveToNext();
			}
			cursor.close();
			
			return destinations;
		}
		
		private Destination destinationFromCursor(Cursor cursor) {
			return new Destination(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getString(3),
				cursor.getString(4),
				cursor.getString(5),
				cursor.getDouble(6), 
				cursor.getDouble(7)
			);
		}
}
