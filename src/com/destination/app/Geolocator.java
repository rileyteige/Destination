package com.destination.app;

import java.io.IOException;
import java.util.List;

import com.destination.common.Utility;
import com.destination.models.Coordinate;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Determines locations of the current device and arbitrary addresses.
 * @author Riley Teige
 * 
 */
public class Geolocator implements LocationListener {
	private final Context context;
	private Location currentLocation;
	
	private final int MIN_MILLISECONDS_BEFORE_LOCATION_UPDATE = 100;
	private final int MIN_DISTANCE_IN_METERS_CHANGED_BEFORE_LOCATION_UPDATE = 50;
	
	public Geolocator(Context context) {
		this.context = context;
		
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		
		currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				MIN_MILLISECONDS_BEFORE_LOCATION_UPDATE,
				MIN_DISTANCE_IN_METERS_CHANGED_BEFORE_LOCATION_UPDATE,
				this);
	}
	
	/**
	 * Determines the lat/long coordinates of a given address.
	 * @param streetAddress The street address to be looked up.
	 * @param city The city being located.
	 * @param state The state being located.
	 * @param zipCode The zip code being located.
	 * @return A {@link Coordinate} instance representing the address's location.
	 */
	public Coordinate geocodeAddress(String streetAddress, String city, String state, String zipCode) {
		if (Utility.isNullOrEmpty(streetAddress) ||
			Utility.isNullOrEmpty(city) ||
			Utility.isNullOrEmpty(state) ||
			Utility.isNullOrEmpty(zipCode)) {
			
			return null;
		}
		
		final String fullAddress = streetAddress + ", " + city + ", " + state + " " + zipCode;
		
		double latitude = 0.0;
		double longitude = 0.0;
		
		try {
			Geocoder geocoder = new Geocoder(this.context);
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
		
		return new Coordinate(latitude, longitude);
	}
	
	/**
	 * Determines the current location of the device.
	 * @return The coordinates describing the device's current location.
	 */
	public Coordinate FindCurrentCoordinates() {
		Location currentLocation = this.GetCurrentLocation();
		if (currentLocation == null) {
			Utility.warn("Could not get valid device location.");
			return null;
		}
		
		return new Coordinate(currentLocation.getLatitude(), currentLocation.getLongitude());
	}
	
	/**
	 * Gets the device's current location.
	 * @return The current location of the device, or null if none could be found.
	 */
	private Location GetCurrentLocation() {
		return this.currentLocation;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.v("onLocationChanged", "Updating location.");
		this.currentLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this.context, "GPS is disabled.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(this.context, "GPS is disabled.", Toast.LENGTH_SHORT).show();
	}
}
