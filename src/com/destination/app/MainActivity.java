package com.destination.app;

import java.io.IOException;
import java.util.List;

import com.destination.db.DestinationDataSource;
import com.destination.models.Destination;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private DestinationDataSource dataSource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dataSource = new DestinationDataSource(this);
		dataSource.open();
		
		loadSavedDestinations();
		setupButtons();
		setupTestCase();
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
	
	private void loadSavedDestinations() {
		List<Destination> destinations = dataSource.getAllDestinations();
		for (Destination dest: destinations) {
			log(dest.getId() + " (Saved): " + dest.getFullAddress());
		}
	}
	
	private void setupButtons() {
		((Button)findViewById(R.id.button_add_address)).setOnClickListener(this);
	}
	
	private void setupTestCase() {
		setTextForId(R.id.edittext_name, "Test");
		setTextForId(R.id.edittext_street_address, "16494 162nd St Se");
		setTextForId(R.id.edittext_city, "Monroe");
		setTextForId(R.id.edittext_state, "WA");
		setTextForId(R.id.edittext_zipcode, "98272");
	}
	
	private void addAddress() {
		Destination dest = createDestinationFromFields();
		if (dest == null) {
			return;
		}
		
		log(dest.getId() + ": " + dest.getFullAddress());
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
		
		return dataSource.createDestination(getTextFromId(R.id.edittext_name), streetAddress, city, state, zipCode, latitude, longitude);
	}
	
	private String getTextFromId(int id) {
		return ((EditText)findViewById(id)).getText().toString();
	}
	
	private void setTextForId(int id, String str) {
		((EditText)findViewById(id)).setText(str);
	}
	
	private void log(String str) {
		TextView captionsView = (TextView)findViewById(R.id.textview_captions);
		String currentLog = captionsView.getText().toString();
		((TextView)findViewById(R.id.textview_captions)).setText(currentLog + "\n" + str);
	}
}
