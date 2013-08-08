package com.destination.app;

public interface DestinationListener {
	public void onAddDestination(String name, String streetAddress, String city, String state, String zipCode);
	public void onUpdateDestination(long id, String name, String streetAddress, String city, String state, String zipCode);
}
