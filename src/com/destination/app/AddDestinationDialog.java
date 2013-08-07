package com.destination.app;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AddDestinationDialog extends DialogFragment {

	private OnAddDestinationListener listener;
	
	public AddDestinationDialog(OnAddDestinationListener parent) {
		listener = parent;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (listener == null) {
			return null;
		}
		
		final View view = inflater.inflate(R.layout.dialog_add_destination, container, false);
		
		getDialog().setTitle(R.string.add_destination);
		
		Button submitButton = (Button)view.findViewById(R.id.dialog_add_destination_button_submit);
		submitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View clickedView) {
				String name = ((TextView)view.findViewById(R.id.edittext_name)).getText().toString();
				String streetAddress = ((TextView)view.findViewById(R.id.edittext_street_address)).getText().toString();
				String city = ((TextView)view.findViewById(R.id.edittext_city)).getText().toString();
				String state = ((TextView)view.findViewById(R.id.edittext_state)).getText().toString();
				String zipCode = ((TextView)view.findViewById(R.id.edittext_zipcode)).getText().toString();
				
				listener.onAddDestination(name, streetAddress, city, state, zipCode);
				dismiss();
			}
		});
		
		return view;
	}
}
