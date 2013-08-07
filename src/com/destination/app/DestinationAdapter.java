package com.destination.app;

import java.util.List;

import com.destination.models.Destination;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DestinationAdapter extends ArrayAdapter<Destination>{

	private List<Destination> destinations;
	private int rowViewResourceId;
	
	public DestinationAdapter(Context context, int textViewResourceId, List<Destination> destinations) {
		super(context, textViewResourceId, destinations);
		this.destinations = destinations;
		this.rowViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int destPosition, View convertView, ViewGroup parent) {
		View destRowView = convertView;
		if (destRowView == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			destRowView = inflater.inflate(rowViewResourceId, null);
		}
		Destination dest = destinations.get(destPosition);
		if (dest != null) {
			TextView textViewName = (TextView)destRowView.findViewById(R.id.row_destination_textview_name);
			TextView textViewAddress = (TextView)destRowView.findViewById(R.id.row_destination_textview_address);
			
			textViewName.setText(dest.getName());
			textViewAddress.setText(dest.getFullAddress());
		}
		return destRowView;
	}
}
