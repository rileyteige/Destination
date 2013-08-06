package com.destination.app;

import java.io.IOException;
import java.util.List;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		log("Compiling address...");
		Destination dest = compileEnteredAddress();
		if (!dest.hasAllFields()) {
			return;
		}
		
		log("Translating address...");
		List<Address> possibleAddresses = null;
		Geocoder geocoder = new Geocoder(this);
		try {
			possibleAddresses = geocoder.getFromLocationName(dest.getFullAddress(), 1);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if (possibleAddresses == null || possibleAddresses.size() == 0) {
			return;
		}
		log("Polling coordinates...");
		Address destAddress = possibleAddresses.get(0);
		if (!(destAddress.hasLatitude() && destAddress.hasLongitude())) {
			return;
		}
		
		dest.setCoordinates(destAddress.getLatitude(), destAddress.getLongitude());
		log("Coordinates: (" + dest.getLatitude() + "," + dest.getLongitude() + ")");
	}

	private Destination compileEnteredAddress() {
		return new Destination(
			getTextFromId(R.id.edittext_name),
			getTextFromId(R.id.edittext_street_address),
			getTextFromId(R.id.edittext_city),
			getTextFromId(R.id.edittext_state),
			getTextFromId(R.id.edittext_zipcode)
		);
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
