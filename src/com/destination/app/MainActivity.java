package com.destination.app;

import java.io.IOException;
import java.util.List;

import com.destination.db.DestinationDataSource;
import com.destination.models.Destination;
import com.google.android.gms.maps.model.LatLng;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ListActivity implements OnClickListener, OnItemClickListener {

	private DestinationDataSource dataSource;
	private DestinationAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dataSource = new DestinationDataSource(this);
		dataSource.open();
		
		loadSavedDestinations(true);
		setupListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClick(View clickedView) {
		switch (clickedView.getId()) {
		case R.id.button_add_address:
			addAddress();
			break;
		}
	}

	public void onItemClick(AdapterView<?> adView, View targetView, int position, long id)
	{		
		Destination dest = (Destination) getListAdapter().getItem(position);
		LatLng currentCoordinates = findCurrentCoordinates();
		
		getDirections(currentCoordinates, new LatLng(dest.getLatitude(), dest.getLongitude()));
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
		((Button)findViewById(R.id.button_add_address)).setOnClickListener(this);
		getListView().setOnItemClickListener(this);
	}
	
	private void addAddress() {
		Destination dest = createDestinationFromFields();
		if (dest == null) {
			return;
		}
		adapter.add(dest);
	}

	private Destination createDestinationFromFields() {
		String name = getTextFromId(R.id.edittext_name);
		String streetAddress = getTextFromId(R.id.edittext_street_address);
		String city = getTextFromId(R.id.edittext_city);
		String state = getTextFromId(R.id.edittext_state);
		String zipCode = getTextFromId(R.id.edittext_zipcode);
		String fullAddress = streetAddress + ", " + city + ", " + state + " " + zipCode;
		
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
				return null;
			}
			
			latitude = destAddress.getLatitude();
			longitude = destAddress.getLongitude();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return dataSource.createDestination(name, streetAddress, city, state, zipCode, latitude, longitude);
	}
	
	private void getDirections(LatLng src, LatLng dest) {
		Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW,
				generateMapsUri(src, dest));
		startActivity(mapsIntent);
	}
	
	private LatLng findCurrentCoordinates() {
		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null) {
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
	
	private String getTextFromId(int id) {
		return ((EditText)findViewById(id)).getText().toString();
	}
}
