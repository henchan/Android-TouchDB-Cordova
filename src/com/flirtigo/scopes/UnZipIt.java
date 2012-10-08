package com.flirtigo.scopes;

import java.util.Observable;
import java.util.Observer;

import org.apache.cordova.DroidGap;

import android.os.Environment;
import android.util.Log;

public class UnZipIt extends DroidGap implements Observer {

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

		Log.d(TAG, "clean up zip: " );

	}

	void unzipWebFile(String unzipLocation, String filename) {
		Log.d(TAG, "UnzipIt to "+unzipLocation);

	    String filePath = Environment.getExternalStorageDirectory().toString();

	    UnZipper unzipper = new UnZipper(filename, filePath, unzipLocation);
	    unzipper.addObserver(this);
	    unzipper.unzip();
	}

}
