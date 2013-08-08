package com.destination.app;

import java.io.IOException;
import java.util.List;

import com.destination.common.Utility;
import com.destination.db.DestinationDataSource;
import com.destination.models.Destination;
import com.google.android.gms.maps.model.LatLng;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener, DestinationListener {
	
	private DestinationDataSource dataSource;
	private DestinationAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dataSource = new DestinationDataSource(this);
		dataSource.open();
		
		loadSavedDestinations(false);
		setupListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.getItem(Menu.FIRST - 1);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem item) {
				showAddDestinationDialog();
				return true;
			}
		});
		return super.onPrepareOptionsMenu(menu);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{		
		Destination dest = (Destination) adapter.getItem(position);
		LatLng currentCoordinates = findCurrentCoordinates();
		
		getDirections(currentCoordinates, new LatLng(dest.getLatitude(), dest.getLongitude()));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final Destination dest = adapter.getItem(position);
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int pressed) {
				switch (pressed) {
				// Update
				case DialogInterface.BUTTON_POSITIVE:
					tryUpdate(dest);
					break;
				
				// Delete
				case DialogInterface.BUTTON_NEGATIVE:
					tryDelete(dest);
					break;
				
				// Cancel
				case DialogInterface.BUTTON_NEUTRAL:
					break;
				}
			}
		};

		AlertDialog dialog = new AlertDialog.Builder(this)
								.setMessage(dest.getName())
								.setPositiveButton(getString(R.string.update), dialogClickListener)
								.setNegativeButton(getString(R.string.delete), dialogClickListener)
								.setNeutralButton(R.string.cancel, dialogClickListener)
								.show();
		TextView textViewMessage = (TextView)dialog.findViewById(android.R.id.message);
		textViewMessage.setTextSize(this.getResources().getDimension(R.dimen.dialog_message_text_size));
		return true;
	}
	
	public void onAddDestination(
			final String name,
			final String streetAddress,
			final String city,
			final String state,
			final String zipCode) {
		final MainActivity activity = this;
		new AsyncTask<Void, Void, Destination>()
		{
			public Destination doInBackground(Void... args) {
				return createDestination(name, streetAddress, city, state, zipCode);
			}
			
			public void onPostExecute(Destination dest) {
				if (dest == null) {
					Utility.warn("Received null destination.");
					Toast.makeText(activity, R.string.add_destination_failure, Toast.LENGTH_SHORT).show();
				} else {
					addAddress(dest);
				}
			}
		}.execute();
	}
	
	public void onUpdateDestination(
			final long id,
			final String name,
			final String streetAddress,
			final String city,
			final String state,
			final String zipCode) {
		final MainActivity activity = this;
		new AsyncTask<Void, Void, Destination>()
		{
			public Destination doInBackground(Void... args) {
				return updateSavedDestination(id, name, streetAddress, city, state, zipCode);
			}
			
			public void onPostExecute(Destination dest) {
				if (dest == null) {
					Utility.warn("Null destination.");
					Toast.makeText(activity,  R.string.update_destination_failure,  Toast.LENGTH_SHORT).show();
				} else {
					updateDestination(dest);
				}
			}
		}.execute();
		
	}
	
	private void tryUpdate(final Destination dest) {
		AddDestinationDialog dialog = new AddDestinationDialog(this, dest);
		dialog.show(getFragmentManager().beginTransaction(), MainActivity.class.getName());
	}
	
	private void tryDelete(final Destination dest) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int pressed) {
				switch (pressed) {
				case DialogInterface.BUTTON_POSITIVE:
					adapter.remove(dest);
					dataSource.deleteDestination(dest);
					break;
				
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};

		new AlertDialog.Builder(this)
			.setMessage(this.getString(R.string.confirm_delete))
			.setPositiveButton(getString(R.string.yes), dialogClickListener)
			.setNegativeButton(getString(R.string.no), dialogClickListener)
			.show();
	}
	
	private void loadSavedDestinations(boolean shouldDelete) {
		List<Destination> destinations = dataSource.getAllDestinations(true);
		if (shouldDelete) {
			for (Destination dest: destinations) {
				dataSource.deleteDestination(dest);
			}
			destinations.clear();
		}
		DestinationAdapter adapter = new DestinationAdapter(this, R.layout.row_destination, destinations);
		setListAdapter(adapter);
		this.adapter = adapter;
	}
	
	private void setupListeners() {
		//((Button)findViewById(R.id.button_add_address)).setOnClickListener(this);
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);
	}
	
	private void showAddDestinationDialog() {
		AddDestinationDialog dialog = new AddDestinationDialog(this);
		dialog.show(getFragmentManager().beginTransaction(), MainActivity.class.getName());
	}
	
	private void addAddress(Destination dest) {
		if (dest == null) {
			Utility.warn("Received null destination.");
			return;
		}
		
		Destination.DestinationComparator comparator = new Destination.DestinationComparator();
		
		int i = 0;
		while (comparator.compare(dest, adapter.getItem(i)) >= 0 && i < adapter.getCount()) { i++; }
		
		adapter.insert(dest, i);
	}

	private void updateDestination(Destination newDestination) {
		if (newDestination == null) {
			Utility.warn("Null destination.");
			return;
		}
		
		long id = newDestination.getId();
		int i = 0;
		Destination oldDestination = null;
		while (i < adapter.getCount()) {
			oldDestination = adapter.getItem(i);
			if (oldDestination.getId() == id) {
				break;
			}
			i++;
		}
		
		adapter.remove(oldDestination);
		adapter.insert(newDestination, i);
	}
	
	private Destination updateSavedDestination(long id, String name, String streetAddress, String city, String state, String zipCode) {
		if (Utility.isNullOrEmpty(name) ||
			Utility.isNullOrEmpty(streetAddress) ||
			Utility.isNullOrEmpty(city) ||
			Utility.isNullOrEmpty(state) ||
			Utility.isNullOrEmpty(zipCode)) {
			
			Utility.warn("Received empty parameter.");
			return null;
		}
		
		LatLng coord = geocode(streetAddress, city, state, zipCode);
		if (coord == null) {
			Utility.warn("Could not geocode address.");
			return null;
		}
		Destination newDestination = new Destination(id, name, streetAddress, city, state, zipCode, coord.latitude, coord.longitude);
		int rowsChanged = dataSource.updateDestination(newDestination);
		
		return rowsChanged > 0 ? newDestination : null;
	}
	
	private Destination createDestination(String name, String streetAddress, String city, String state, String zipCode) {
		if (Utility.isNullOrEmpty(name) ||
			Utility.isNullOrEmpty(streetAddress) ||
			Utility.isNullOrEmpty(city) ||
			Utility.isNullOrEmpty(state) ||
			Utility.isNullOrEmpty(zipCode)) {
			
			Utility.warn("Received empty parameter.");
			return null;
		}
		
		LatLng coord = geocode(streetAddress, city, state, zipCode);
		
		if (coord == null) {
			Utility.warn("Could not get coordinates for address on " + streetAddress);
			return null;
		}
		
		return dataSource.createDestination(name, streetAddress, city, state, zipCode, coord.latitude, coord.longitude);
	}
	
	private LatLng geocode(String streetAddress, String city, String state, String zipCode) {
		final String fullAddress = streetAddress + ", " + city + ", " + state + " " + zipCode;
		
		double latitude = 0.0;
		double longitude = 0.0;
		
		try {
			Geocoder geocoder = new Geocoder(this);
			List<Address> possibleAddresses = geocoder.getFromLocationName(fullAddress, 1);
			if (possibleAddresses == null || possibleAddresses.size() == 0) {
				return null;
			}
			
			Address destAddress = possibleAddresses.get(0);
			if (!(destAddress.hasLatitude() && destAddress.hasLongitude())) {
				Utility.warn("Destination address has bad coordinates.");
				return null;
			}
			
			latitude = destAddress.getLatitude();
			longitude = destAddress.getLongitude();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return new LatLng(latitude, longitude);
	}
	
	private void getDirections(LatLng src, LatLng dest) {
		if (src == null || dest == null) {
			Utility.warn("Received a null coordinate.");
			return;
		}
		Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
				generateMapsUri(src, dest));
		startActivity(mapsIntent);
	}
	
	private LatLng findCurrentCoordinates() {
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null) {
			Utility.warn("Could not get valid device location.");
			return null;
		}
		
		return new LatLng(location.getLatitude(), location.getLongitude());
	}
	
	private Uri generateMapsUri(LatLng src, LatLng dest) {
		String url = "http://maps.google.com/maps?" +
					"saddr=" + stringifyCoordinates(src) +
					"&daddr=" + stringifyCoordinates(dest);
		return Uri.parse(url);
	}
	
	private String stringifyCoordinates(LatLng coord) {
		return coord.latitude + "," + coord.longitude;
	}
}
