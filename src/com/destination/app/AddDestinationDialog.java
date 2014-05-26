package com.destination.app;

import com.destination.models.Destination;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddDestinationDialog extends DialogFragment {

	private DestinationListener listener;
	private Destination oldDestination;
	
	public AddDestinationDialog(DestinationListener parent) {
		listener = parent;
		oldDestination = null;
	}
	
	public AddDestinationDialog(DestinationListener parent, Destination dest) {
		listener = parent;
		oldDestination = dest;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (listener == null) {
			return null;
		}
		
		final View view = inflater.inflate(R.layout.dialog_add_destination, container, false);
		
		final Button submitButton = (Button)view.findViewById(R.id.dialog_add_destination_button_submit);
		
		if (oldDestination != null) {
			getDialog().setTitle(R.string.update_destination);
			((EditText)view.findViewById(R.id.edittext_name)).setText(oldDestination.getName());
			((EditText)view.findViewById(R.id.edittext_street_address)).setText(oldDestination.getStreetAddress());
			((EditText)view.findViewById(R.id.edittext_city)).setText(oldDestination.getCity());
			((EditText)view.findViewById(R.id.edittext_state)).setText(oldDestination.getState());
			((EditText)view.findViewById(R.id.edittext_zipcode)).setText(oldDestination.getZipCode());
			
			submitButton.setOnClickListener(new OnClickListener() {
				public void onClick(View clickedView) {
					promptListener(view, false);
				}
			});
		} else {
			getDialog().setTitle(R.string.add_destination);
			
			submitButton.setOnClickListener(new OnClickListener() {
				public void onClick(View clickedView) {
					promptListener(view, true);
				}
			});
		}
		
		return view;
	}
	
	private void promptListener(View parent, boolean isNewDestination) {
		String name = ((EditText)parent.findViewById(R.id.edittext_name)).getText().toString();
		String streetAddress = ((EditText)parent.findViewById(R.id.edittext_street_address)).getText().toString();
		String city = ((EditText)parent.findViewById(R.id.edittext_city)).getText().toString();
		String state = ((EditText)parent.findViewById(R.id.edittext_state)).getText().toString();
		String zipCode = ((EditText)parent.findViewById(R.id.edittext_zipcode)).getText().toString();
		
		if (isNewDestination) {
			listener.onAddDestination(name, streetAddress, city, state, zipCode);
		} else {
			listener.onUpdateDestination(oldDestination.getId(), name, streetAddress, city, state, zipCode);
		}
		dismiss();
	}
}
