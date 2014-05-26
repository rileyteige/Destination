package com.destination.app;

import android.content.Context;
import android.net.Uri;

import com.destination.common.Utility;
import com.destination.models.Coordinate;

public class MapsLauncher {
	private final Context appContext;
	
	public MapsLauncher(Context appContext) {
		this.appContext = appContext;
	}
	
	public void launchDirectionsSearch(Coordinate src, Coordinate dest) {
		if (src == null || dest == null) {
			Utility.warn("Received a null coordinate.");
			return;
		}
		
		IntentStarter starter = new IntentStarter(this.appContext);
		Uri googleMapsUri = generateMapsUri(src, dest);
		
		starter.StartIntent(googleMapsUri);
	}
	
	private Uri generateMapsUri(Coordinate src, Coordinate dest) {
		String url = "http://maps.google.com/maps?" +
					"saddr=" + stringifyCoordinates(src) +
					"&daddr=" + stringifyCoordinates(dest);
		return Uri.parse(url);
	}
	
	private String stringifyCoordinates(Coordinate coord) {
		return coord.getLatitude() + "," + coord.getLongitude();
	}
}
