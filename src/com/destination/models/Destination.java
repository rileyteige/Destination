package com.destination.models;

public class Destination {
	private String name;
	private String streetAddress;
	private String city;
	private String state;
	private String zipCode;
	
	private double latitude;
	private double longitude;
	
	public Destination(String name, String streetAddress, String city, String state, String zipCode) {
		this.name = name;
		this.streetAddress = streetAddress;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.latitude = -1.0;
		this.longitude = -1.0;
	}
	
	public String getName() { return name; }
	public String getStreetAddress() { return streetAddress; }	
	public String getCity() { return city; }	
	public String getState() { return state; }	
	public String getZipCode() { return zipCode; }	
	public double getLatitude() { return latitude; }	
	public double getLongitude() { return longitude; }
	
	public void setCoordinates(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public boolean hasAllFields() {
		return !isNullOrEmpty(name) &&
				!isNullOrEmpty(streetAddress) &&
				!isNullOrEmpty(city) &&
				!isNullOrEmpty(state) &&
				!isNullOrEmpty(zipCode);
	}
	
	public boolean hasCoordinates() {
		return latitude >= 0.0 && longitude >= 0.0;
	}
	
	public String getFullAddress() {
		return streetAddress + ", " + city + ", " + state + " " + zipCode;
	}
	
	private boolean isNullOrEmpty(String str) {
		return str == null || str == "";
	}
}
