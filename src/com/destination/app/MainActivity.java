package com.destination.app;

import java.util.List;

import com.destination.common.Utility;
import com.destination.db.DestinationDataSource;
import com.destination.models.Coordinate;
import com.destination.models.Destination;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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
	private Geolocator geolocator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dataSource = new DestinationDataSource(this);
		try {
			dataSource.open();
		} catch (Exception e) {
			e.printStackTrace();
			Utility.warn("Could not load data source.");
			return;
		}
		
		this.geolocator = new Geolocator(this);
		
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
		
		Coordinate currentCoordinates = this.geolocator.FindCurrentCoordinates();
		Coordinate destinationCoordinates = new Coordinate(dest.getLatitude(), dest.getLongitude());
		
		new MapsLauncher(this).launchDirectionsSearch(currentCoordinates, destinationCoordinates);
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
		List<Destination> destinations = null;
		if (shouldDelete) {
			dataSource.deleteAllDestinations();
		} else {
			destinations = dataSource.getAllDestinations(true);
		}

		DestinationAdapter adapter = new DestinationAdapter(this, R.layout.row_destination, destinations);
		setListAdapter(adapter);
		this.adapter = adapter;
	}
	
	private void setupListeners() {
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
		Coordinate coord = geocode(streetAddress, city, state, zipCode);
		Destination newDestination = new Destination(id, name, streetAddress, city, state, zipCode, coord.getLatitude(), coord.getLongitude());
		int rowsChanged = dataSource.updateDestination(newDestination);
		
		return rowsChanged > 0 ? newDestination : null;
	}
	
	private Destination createDestination(String name, String streetAddress, String city, String state, String zipCode) {
		Coordinate coord = geocode(streetAddress, city, state, zipCode);
		
		try {
			return dataSource.createDestination(name, streetAddress, city, state, zipCode, coord);
		} catch (IllegalArgumentException ex) {
			Utility.warn("Bad parameter passed for creating a destination.");
			return null;
		}
	}
	
	/**
	 * Determines the lat/long coordinates of a given address.
	 * @param streetAddress The street address to be looked up.
	 * @param city The city being located.
	 * @param state The state being located.
	 * @param zipCode The zip code being located.
	 * @return A {@link Coordinate} instance representing the address's location.
	 */
	private Coordinate geocode(String streetAddress, String city, String state, String zipCode) {
		Coordinate addressCoordinates = this.geolocator.geocodeAddress(streetAddress, city, state, zipCode);
		
		if (addressCoordinates == null) {
			Utility.warn("Could not get coordinates for address on " + streetAddress);
			return null;
		}
		
		return addressCoordinates;
	}
}
