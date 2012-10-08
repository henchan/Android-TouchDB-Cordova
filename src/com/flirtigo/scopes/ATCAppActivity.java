package com.flirtigo.scopes;

import java.io.IOException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.ReplicationStatus;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import android.os.Bundle;
import android.util.Log;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

/**
 * This is the main activity for your application.
 * 
 * You can safely refactor the class name and package to meet your needs.
 * 
 */
public class ATCAppActivity extends AndroidTouchDBcordova {

	// prepare to auto-sync from Master DBs
	{
		TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TDServer server = null;
		String filesDir = getFilesDir().getAbsolutePath();
		try {
			server = new TDServer(filesDir);
		} catch (IOException e) {
			Log.e(TAG, "Error starting TDServer", e);
		}

		// start TouchDB-Ektorp adapter
		HttpClient httpClient = new TouchDBHttpClient(server);
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

		// create a local database
		CouchDbConnector dbConnector = dbInstance.createConnector(
				"flapp", true);

		// start TouchDB and auto-sync from Master DBs
		// Master app 
		pullMaster(dbInstance, "http://192.168.0.22:5984/flapp", "flapp");
		// Master Culture
		pullMaster(dbInstance, "http://192.168.0.22:5984/def_cult", "def_cult");
	}

	public void pullMaster(CouchDbInstance dbInstance, String source, String target) {

		// start TouchDB and auto-sync from Master DB

		// pull the application database
		ReplicationCommand pullCommand = new ReplicationCommand.Builder()
				.source(source)
				.target(target).continuous(false).build();

		ReplicationStatus status = dbInstance.replicate(pullCommand);
	}


	/**
	 * Override this method to do additional work after Couchbase has started
	 */
	// @Override
	// protected void couchbaseStarted(String host, int port) {
	//
	// }

	/**
	 * Override this method to disable the splash screen
	 */
	// @Override
	// protected boolean showSplashScreen() {
	// return false;
	// }

	/**
	 * Override this method to change the drawable used in the splash screen
	 */
	// @Override
	// protected int getSplashScreenDrawable() {
	// return R.drawable.mySplashScreen;
	// }

	/**
	 * After you've deployed your couchapp you can override this method to
	 * directly return the name of your database, instead of using the default
	 * logic to look for databases in the assets folder
	 */
	// @Override
	// protected String getDatabaseName() {
	// return "mydatabase";
	// }

}
