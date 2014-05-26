package com.destination.models;

import java.util.Comparator;

import com.destination.common.Utility;

public class Destination {
	private long id;
	private String name;
	private String streetAddress;
	private String city;
	private String state;
	private String zipCode;
	
	private double latitude;
	private double longitude;
	
	public Destination(long id, String name, String streetAddress, String city, String state, String zipCode, double latitude, double longitude) {
		if (Utility.isNullOrEmpty(name) ||
			Utility.isNullOrEmpty(streetAddress) ||
			Utility.isNullOrEmpty(city) ||
			Utility.isNullOrEmpty(state) ||
			Utility.isNullOrEmpty(zipCode)) {
			
			throw new IllegalArgumentException();
		}
		
		this.id = id;
		this.name = name;
		this.streetAddress = streetAddress;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public long getId() { return id; }
	public String getName() { return name; }
	public String getStreetAddress() { return streetAddress; }	
	public String getCity() { return city; }	
	public String getState() { return state; }	
	public String getZipCode() { return zipCode; }	
	public double getLatitude() { return latitude; }	
	public double getLongitude() { return longitude; }
	
	public String getFullAddress() {
		return streetAddress + ", " + city + ", " + state + " " + zipCode;
	}
	
	public static class DestinationComparator implements Comparator<Destination> {
		public int compare(Destination x, Destination y) {
			// Sort case-insensitive.
			return x.getName().toLowerCase().compareTo(y.getName().toLowerCase());
		}
	}
}
