package com.destination.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.destination.common.Utility;
import com.destination.models.Coordinate;
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
		
		/**
		 * Initializes a new instance of {@link DestinationDataSource}.
		 * @param context The Android context in which this data source is being loaded.
		 */
		public DestinationDataSource(Context context) {
			dbHelper = new DatabaseHelper(context);
		}
	
		/**
		 * Opens the data source.
		 * @throws Exception Thrown if an exception occurs in loading the data store.
		 */
		public void open() throws Exception {
			try {
				database = dbHelper.getWritableDatabase();
			} catch (SQLException ex) {
				throw new Exception("Could not open data source.", ex);
			}
		}
		
		/**
		 * Closes the data source.
		 */
		public void close() {
			dbHelper.close();
		}
		
		/**
		 * Creates a new destination record in the data store.
		 * @param name The name of the destination.
		 * @param streetAddress The street address of the destination.
		 * @param city The city of the destination.
		 * @param state The state of the destination.
		 * @param zipCode The zip code of the destination.
		 * @param coordinates The lat/long coordinates of the location.
		 * @return The created {@link Destination} instance.
		 * @exception IllegalArgumentException Thrown if any argument is null or if any String parameter is empty.
		 */
		public Destination createDestination(
				String name,
				String streetAddress,
				String city,
				String state,
				String zipCode,
				Coordinate coordinates) {
			
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("name");
			} else if (streetAddress == null || streetAddress.isEmpty()) {
				throw new IllegalArgumentException("streetAddress");
			} else if (city == null || city.isEmpty()) {
				throw new IllegalArgumentException("city");
			} else if (state == null || state.isEmpty()) {
				throw new IllegalArgumentException("state");
			} else if (zipCode == null || zipCode.isEmpty()) {
				throw new IllegalArgumentException("zipCode");
			} else if (coordinates == null) {
				throw new IllegalArgumentException("coordinates");
			}
			
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.COLUMN_NAME, name);
			values.put(DatabaseHelper.COLUMN_STREET_ADDRESS, streetAddress);
			values.put(DatabaseHelper.COLUMN_CITY, city);
			values.put(DatabaseHelper.COLUMN_STATE, state);
			values.put(DatabaseHelper.COLUMN_ZIPCODE, zipCode);
			values.put(DatabaseHelper.COLUMN_LATITUDE, coordinates.getLatitude());
			values.put(DatabaseHelper.COLUMN_LONGITUDE, coordinates.getLongitude());
			
			long id = database.insert(DatabaseHelper.TABLE_DESTINATIONS, null, values);
			
			Cursor cursor = database.query(DatabaseHelper.TABLE_DESTINATIONS, allColumns, DatabaseHelper.COLUMN_ID + " = " + id, null, null, null, null);
			cursor.moveToFirst();
			
			Destination dest = destinationFromCursor(cursor);
			
			cursor.close();
			
			return dest;
		}
		
		/**
		 * Updates a destination record in the data store.
		 * @param dest The destination to be updated (Record ID packaged inside).
		 * @return The number of rows affected (should be 1 or 0).
		 */
		public int updateDestination(Destination dest) {
			if (dest == null) {
				Utility.warn("Null destination.");
				return 0;
			}
			
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.COLUMN_NAME, dest.getName());
			values.put(DatabaseHelper.COLUMN_STREET_ADDRESS, dest.getStreetAddress());
			values.put(DatabaseHelper.COLUMN_CITY, dest.getCity());
			values.put(DatabaseHelper.COLUMN_STATE, dest.getState());
			values.put(DatabaseHelper.COLUMN_ZIPCODE, dest.getZipCode());
			values.put(DatabaseHelper.COLUMN_LATITUDE, dest.getLatitude());
			values.put(DatabaseHelper.COLUMN_LONGITUDE, dest.getLongitude());
			
			return database.update(DatabaseHelper.TABLE_DESTINATIONS, values, DatabaseHelper.COLUMN_ID + " = " + dest.getId(), null);
		}
		
		/**
		 * Deletes all {@link Destination} records from the data store.
		 * Cannot be undone.
		 */
		public void deleteAllDestinations() {
			this.deleteManyDestinations(this.getAllDestinations(false));
		}
		
		/**
		 * Deletes a single {@link Destination} record from the data store.
		 * @param dest The {@link Destination} record to be deleted.
		 * @exception IllegalArgumentException Thrown if dest is null.
		 */
		public void deleteDestination(Destination dest) throws IllegalArgumentException {
			if (dest == null) {
				throw new IllegalArgumentException("dest");
			}
			
			long id = dest.getId();
			Log.w(DestinationDataSource.class.getName(), "Destination deleted with id: " + id);
			database.delete(DatabaseHelper.TABLE_DESTINATIONS, DatabaseHelper.COLUMN_ID + " = " + id, null);
		}
		
		/**
		 * Retrieves all destination records from the data store.
		 * @param sort If true, will return the destinations as a sorted collection.
		 * @return All destination records contained in the data store.
		 */
		public List<Destination> getAllDestinations(boolean sort) {
			List<Destination> destinations = new ArrayList<Destination>();
			
			Cursor cursor = database.query(DatabaseHelper.TABLE_DESTINATIONS, allColumns, null, null, null, null, sort ? DatabaseHelper.COLUMN_NAME : null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				Destination dest = destinationFromCursor(cursor);
				destinations.add(dest);
				cursor.moveToNext();
			}
			cursor.close();
			
			if (sort) {
				// Sort case-insensitive into ascending order.
				Collections.sort(destinations, new Destination.DestinationComparator());
			}
			
			return destinations;
		}
		
		/**
		 * Removes a collection of destinations from the data store.
		 * @param destinations The {@link Destination} instances to be deleted.
		 * @throws IllegalArgumentException Thrown if destinations is null.
		 */
		private void deleteManyDestinations(Collection<Destination> destinations) throws IllegalArgumentException {
			if (destinations == null) {
				throw new IllegalArgumentException("destinations");
			}
			
			for (Destination dest: destinations) {
				this.deleteDestination(dest);
			}
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
