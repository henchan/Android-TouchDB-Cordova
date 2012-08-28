/**
 *     Copyright 2011 Couchbase, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.touchdb.cordova;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.javascript.TDJavaScriptViewCompiler;
import com.couchbase.touchdb.listener.TDListener;

import org.apache.cordova.*;

/**
 * Avoid making changes to this class.  If you find the need, please
 * make suggestions here:  https://groups.google.com/forum/#!forum/mobile-couchbase
 */

public class AndroidTouchDBcordova extends DroidGap
{
    public static final String TAG = AndroidTouchDBcordova.class.getName();
    public static final String COUCHBASE_DATABASE_SUFFIX = ".couch";
    public static final String WELCOME_DATABASE = "welcome";
    public static final String DEFAULT_ATTACHMENT = "/index.html";
    private ServiceConnection couchbaseService;
    private String couchappDatabase;
    private TDListener listener;
    private ProgressDialog progressDialog;
    private Handler uiHandler;
    static Handler myHandler;

    protected boolean installWelcomeDatabase() {
        return true;
    }

    protected boolean showSplashScreen() {
        return true;
    }

    protected int getSplashScreenDrawable() {
        return R.drawable.splash;
    }

    protected String getDatabaseName() {
        return findCouchApp();
    }

    protected String getDesignDocName() {
        return findCouchApp();
    }

    protected String getAttachmentPath() {
        return DEFAULT_ATTACHMENT;
    }

    protected String getCouchAppURL(String host, int port) {
        return "http://" + host + ":" + port + "/" + getDatabaseName() + "/_design/" + getDesignDocName() + getAttachmentPath();
    }

    protected String getWelcomeAppURL(String host, int port) {
        return "http://" + host + ":" + port + "/" + WELCOME_DATABASE + "/_design/" + WELCOME_DATABASE + DEFAULT_ATTACHMENT;
    }

    protected void couchbaseStarted(String host, int port) {

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(showSplashScreen()) {
            // show the splash screen
            // NOTE: Callback won't show the splash until we try to load a URL
            //       so we start a load, with a wait time we should never exceed
            setIntegerProperty("splashscreen", getSplashScreenDrawable());
            //loadUrl("file:///android_asset/www/error.html", 60000);
        }

        // increase the default timeout
        super.setIntegerProperty("loadUrlTimeoutValue", 90000);
        
        String filesDir = getFilesDir().getAbsolutePath();

        Properties properties = new Properties();

        try {
        	InputStream rawResource = getResources().openRawResource(R.raw.cordovacouchapp);
        	properties.load(rawResource);
        } catch (Resources.NotFoundException e) {
        	System.err.println("Did not find raw resource: " + e);
        } catch (IOException e) {
        	System.err.println("Failed to open microlog property file");
        }
        
        TDServer server;
        try {
            server = new TDServer(filesDir);
            
            listener = new TDListener(server, 8888);
            listener.start();
            
            TDView.setCompiler(new TDJavaScriptViewCompiler());

        } catch (IOException e) {
            Log.e(TAG, "Unable to create TDServer", e);
        }
        
        /*couchbaseMobile = new CouchbaseMobile(getBaseContext(), couchCallbackHandler);
        try {
            if(installWelcomeDatabase()) {
                couchbaseMobile.installDatabase(WELCOME_DATABASE + COUCHBASE_DATABASE_SUFFIX);
            }

            // look for a .couch file in the assets folder
            couchappDatabase = getDatabaseName();
            if(couchappDatabase != null) {
                // if we found one, install it
                couchbaseMobile.installDatabase(couchappDatabase + COUCHBASE_DATABASE_SUFFIX);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error installing database", e);
        }*/

        // start couchbase
        //couchbaseService = couchbaseMobile.startCouchbase();
        String ipAddress = "0.0.0.0";
        Log.d(TAG, ipAddress);
		String host = ipAddress;
		int port = 8888;
		String url = "http://" + host + ":" + Integer.toString(port) + "/";
		
        uiHandler = new Handler();
        String appDb = properties.getProperty("app_db");
	    File destination = new File(filesDir + File.separator + appDb + ".touchdb");
	    String couchAppUrl = url + properties.getProperty("couchAppInstanceUrl");
	    Log.d(TAG, "Checking for touchdb at " + filesDir + File.separator + appDb + ".touchdb");
	    if (!destination.exists()) {
	    	Log.d(TAG, "Touchdb does not exist. Unzipping files.");
	    	// must be in the assets directory
	    	try {
	    		// This is the touchdb
	        	String destinationFilename = extractFromAssets(this.getApplicationContext(), appDb + ".touchdb", filesDir);	
	        	File destFile = new File(destinationFilename);
	    		// These are the attachments
	    		destinationFilename = extractFromAssets(this.getApplicationContext(), appDb + ".zip", filesDir);	
	        	destFile = new File(destinationFilename);
	    		unzipFile(destFile);
                //loadWebview();
	    		AndroidTouchDBcordova.this.loadUrl(couchAppUrl);
			} catch (Exception e) {
				e.printStackTrace();
				String errorMessage = "There was an error extracting the database.";
				displayLargeMessage(errorMessage, "big");
				Log.d(TAG, errorMessage);
				progressDialog.setMessage(errorMessage);
				//this.setCouchAppUrl("/");
				AndroidTouchDBcordova.this.loadUrl(url);
			}
	    } else {
	    	Log.d(TAG, "Touchdb exists. Loading WebView.");
	    	//loadWebview();
	    	AndroidTouchDBcordova.this.loadUrl(couchAppUrl);
	    }
    }

    /**
     * Look for the first .couch file that is not named "welcome.couch"
     * that can be found in the assets folder
     *
     * @return the name of the database (without the .couch extension)
     * @throws IOException
     */
    public String findCouchApp() {
        String result = null;
        AssetManager assetManager = getAssets();
        String[] assets = null;
        try {
            assets = assetManager.list("");
        } catch (IOException e) {
            Log.e(TAG, "Error listing assets", e);
        }
        if(assets != null) {
            for (String asset : assets) {
                if(!asset.startsWith(WELCOME_DATABASE) && asset.endsWith(COUCHBASE_DATABASE_SUFFIX)) {
                    result = asset.substring(0, asset.length() - COUCHBASE_DATABASE_SUFFIX.length());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Clean up the Couchbase service
     */
    //@Override
   /* public void onDestroy() {
        if(couchbaseService != null) {
            unbindService(couchbaseService);
        }
        super.onDestroy();
    }*/
    
    public static String extractFromAssets(Context ctx, String file, String destinationDirectory) throws IOException, FileNotFoundException {
		final int BUFFER = 2048;
    	BufferedOutputStream dest = null;
    	AssetManager assetManager = ctx.getAssets();
    	InputStream in = assetManager.open(file);	
    	String destinationFilename = destinationDirectory + File.separator + file;
		OutputStream out = new FileOutputStream(destinationFilename);
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
		return destinationFilename;
	}
    
    public void displayLargeMessage( String message, String size ) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = null;
		if (size.equals("big")) {
//			layout = inflater.inflate(R.layout.toast_layout_large,(ViewGroup) findViewById(R.id.toast_layout_large));
		} else {
//			layout = inflater.inflate(R.layout.toast_layout_medium,(ViewGroup) findViewById(R.id.toast_layout_large));
		}
		
		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.android);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(message);
		//uiHandler.post( new ToastMessage( this, message ) );
		/*Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);*/
		uiHandler.post( new ToastMessageBig( this, message, layout ) );
		//toast.show();
	}
    
    public void unzipFile(File zipfile) {
		//installProgress = ProgressDialog.show(CoconutActivity.this, "Extract Zip","Extracting Files...", false, false);
		File zipFile = zipfile;
		displayLargeMessage("Extracting: " + zipfile, "medium");
		String directory = null;
		directory = zipFile.getParent();
		directory = directory + "/";
		myHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// process incoming messages here
				switch (msg.what) {
				case 0:
					// update progress bar
					//installProgress.setMessage("" + (String) msg.obj);
					Log.d(TAG,  (String) msg.obj);
					break;
				case 1:
					//installProgress.cancel();
					//Toast toast = Toast.makeText(getApplicationContext(), "Zip extracted successfully", Toast.LENGTH_SHORT);
					displayLargeMessage(msg.obj + ": Complete.", "medium");
					//toast.show();
					//provider.refresh();
					Log.d(TAG, msg.obj + ":Zip extracted successfully");
					break;
				case 2:
					//installProgress.cancel();
					break;
				}
				super.handleMessage(msg);
			}

		};
		/*Thread workthread = new Thread(new UnZip(myHandler, zipFile, directory));
	    workthread.start();*/
		UnZip unzip = new UnZip(myHandler, zipFile, directory);
		unzip.run();
		Log.d(TAG, "Completed extraction.");
	} 
}

class ToastMessageBig implements Runnable {
	View layout;
	Context ctx;
	String msg;
	
	public ToastMessageBig( Context ctx, String msg, View layout ) {
		this.ctx = ctx;
		this.msg = msg;
		this.layout = layout;
	}
	
	public void run() {
		//Toast.makeText( ctx, msg, Toast.LENGTH_SHORT).show();
		Toast toast = new Toast(ctx);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
}
