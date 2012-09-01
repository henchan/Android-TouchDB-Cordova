## Android Couchbase Callback

This application provides the fastest way to deploy a <a href="http://couchapp.org/">CouchApp</a> to an Android device using <a href="http://couchbase.org/">Couchbase Mobile</a> and <a href="http://incubator.apache.org/projects/callback.html">Apache Callback (formerly PhoneGap)</a>.

## Requirements

This project requires the latest version of the Android SDK. If you already have the SDK tools, you can upgrade by running `android update sdk`, if you don't have them, you can [install via this link](http://developer.android.com/sdk/installing.html)

## Getting Started

These instructions are divided into two sections, the first describes the development mode.  In this mode you can continually couchapp push your changes in for test.  The second describes distribution mode where you package your application for distribution.

### Development (assumes using Eclipse)
1.  Clone the couchbaselabs/TouchDB-Android repository

	This application has been tested with commit/59ddf37d6e7bb819f969909971c202eab8c44119 

	run the TouchDB-Android-TestApp

2.  Clone this repository

3.  Build this application, using Eclipse 

    Debug As --> Android Application

4.  Install/Launch this application on your device/emulator

    The app should now be installed, waiting on a debugger breakpoint 

5.  TouchDB is now active on the device, so you can install your couchapp.

	You will need to know the ip address of your device.
	If a Terminal Emulator is installed on the device type:
		netstat
		
	You should see the LISTEN process running on port 8888 and other services on the ip address of your device

7.  From within your CouchApp project directory, run the following command to install your couchapp on the device.

	cd path_to_project/Android-TouchDB-Cordova/examples/CordovaCouchApp/couchapp
	
    couchapp push http://ip_address_of_your_device:8888/ccap
    	e.g. couchapp push http://192.168.0.6:8888/ccap
    
    Note: ddoc name and db specified in res/raw/cordovacouchapp.properties. 
    If you are using a different couchapp, check its ddoc name in your couchapp's _id file 

8.  In a browser, test the couchapp installation went OK

	http://192.168.0.6:8888/ccap/_design/cordovacouchapp/index.html

9.  Allow eclipse debug to complete. Re-run the android app from eclipse or directly on the device

	This should now work.
	It is not necessary to use debug and install the couchapp after the first time, since the couchapp is persistent.

### Distribution (Note : this section is not tested for Android-TouchDB-Cordova)

1.  Compact your database

    curl -X POST -H "Content-Type: application/json"  http://localhost:8984/couchapp/_compact

2.  Copy the database off the device and into this Android application's assets directory:

    adb pull /mnt/sdcard/Android/data/com.couchbase.callback/db/couchapp.couch assets

3.  Repackage your application with the database file included

    ant debug

4.  Reinstall the application to launch the CouchApp

    adb uninstall com.couchbase.callback

    adb install bin/AndroidCouchbaseCallback-debug.apk

    adb shell am start -n com.couchbase.callback/.AndroidCouchbaseCallback

## License

Portions under Apache, Erlang, and other licenses.

The overall package is released under the Apache license, 2.0.

Copyright 2011-2012, Couchbase, Inc.
