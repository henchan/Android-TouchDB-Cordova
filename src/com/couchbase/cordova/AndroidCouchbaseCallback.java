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
package com.couchbase.cordova;

import java.io.IOException;

import org.apache.cordova.DroidGap;

import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;

/**
 * Avoid making changes to this class.  If you find the need, please
 * make suggestions here:  https://groups.google.com/forum/#!forum/mobile-couchbase
 */

public class AndroidCouchbaseCallback extends DroidGap
{
    public static final String TAG = AndroidCouchbaseCallback.class.getName();
    public static final String COUCHBASE_DATABASE_SUFFIX = ".couch";
    public static final String WELCOME_DATABASE = "welcome";
    public static final String DEFAULT_ATTACHMENT = "/index.html";
    private CouchbaseMobile couchbaseMobile;
    private ServiceConnection couchbaseService;
    private String couchappDatabase;

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
            loadUrl("file:///android_asset/www/error.html", 30000);
        }

        // increase the default timeout
        super.setIntegerProperty("loadUrlTimeoutValue", 60000);

        couchbaseMobile = new CouchbaseMobile(getBaseContext(), couchCallbackHandler);
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
        }

        // start couchbase
        couchbaseService = couchbaseMobile.startCouchbase();
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
    @Override
    public void onDestroy() {
        if(couchbaseService != null) {
            unbindService(couchbaseService);
        }
        super.onDestroy();
    }

    /**
     * Implementation of the ICouchbaseDelegat inerface
     */
    private final ICouchbaseDelegate couchCallbackHandler = new ICouchbaseDelegate() {

        /**
         * Once Couchbase has started, load the couchapp, or the instructions if no couchapp is present
         */
        @Override
        public void couchbaseStarted(String host, int port) {
            if(showSplashScreen()) {
                //stop the load that we started to display the splash screen
                cancelLoadUrl();
            }
            if(couchappDatabase != null) {
                AndroidCouchbaseCallback.this.loadUrl(getCouchAppURL(host, port));
            }
            else {
                AndroidCouchbaseCallback.this.loadUrl(getWelcomeAppURL(host, port));
            }

            AndroidCouchbaseCallback.this.couchbaseStarted(host, port);
        }

        @Override
        public void exit(String error) {}
    };
}

