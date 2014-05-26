package com.destination.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentStarter {
	private Context activity;
	
	public IntentStarter(Context activity) {
		this.activity = activity;
	}
	
	public Intent StartIntent(Uri intentUri) {
		Intent startingIntent = new Intent(android.content.Intent.ACTION_VIEW, intentUri);
		this.activity.startActivity(startingIntent);
		return startingIntent;
	}
}
