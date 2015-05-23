package com.gpsgoogleservices;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.serialization.MarshalFloat;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
 

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
 
public class MainActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {
 
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
 
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
 
    private Location mLastLocation;
 
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
 
    // Flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
 
    private LocationRequest mLocationRequest;
 
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
 
    // UI elements
    private TextView lblLocation, lblAnotherAddress;
    private Button btnShowLocation, btnStartLocationUpdates, btnGetListAddress;
    private ListView lvAddress;
    String URL = "http://gpsdemo.somee.com/gpsws.asmx?WSDL";
    ArrayList<String> arrAdr=new ArrayList<String>();
	ArrayAdapter<String> adapter=null;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lblLocation = (TextView) findViewById(R.id.lblLocation);
        lblAnotherAddress = (TextView) findViewById(R.id.lblAnotherAddress);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);
        btnGetListAddress = (Button) findViewById(R.id.btnGetListAddress);
//        btnGetListAddress.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
////				doGetList();
////				calculate();
//			}
//		});
        
        adapter=new ArrayAdapter<String>
		(this, android.R.layout.simple_list_item_1, arrAdr);
        lvAddress.setAdapter(adapter);
 
        // First we need to check availability of play services
        if (checkPlayServices()) {
 
            // Building the GoogleApi client
            buildGoogleApiClient();
 
            createLocationRequest();
        }
 
        // Show location button click listener
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });
 
        // Toggling the periodic location updates
        btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });
 
    }
 
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
 
    @Override
    protected void onResume() {
        super.onResume();
 
        checkPlayServices();
 
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
 
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
 
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
 
    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
 
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
 
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            double OpLong = 106.7704198;
        	double OpLat = 10.8607147;
        	double d = 0;
        	
        	d = Math.sqrt(Math.pow((OpLong - longitude), 2) + Math.pow((OpLat - latitude), 2));
            String I = String.valueOf(d);
            lblAnotherAddress.setText(I);
            
            lblLocation.setText(latitude + ", " + longitude);
 
        } else {
 
            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }
 
    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_stop_location_updates));
 
            mRequestingLocationUpdates = true;
 
            // Starting the location updates
            startLocationUpdates();
 
            Log.d(TAG, "Periodic location updates started!");
 
        } else {
            // Changing the button text
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_start_location_updates));
 
            mRequestingLocationUpdates = false;
 
            // Stopping the location updates
            stopLocationUpdates();
 
            Log.d(TAG, "Periodic location updates stopped!");
        }
    }
 
    /**
     * Create google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
 
    /**
     * Create location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }
 
    /**
     * Verify google play services
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }
 
    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
 
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
 
    }
 
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
 
    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }
 
    @Override
    public void onConnected(Bundle arg0) {
 
        // Once connected with google api, get the location
        displayLocation();
 
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
 
    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
 
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
 
        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();
 
        // Displaying the new location on UI
        displayLocation();
    }
    public void calculate() {
    	double OpLong = 106.7704198;
    	double OpLat = 10.8607147;
    	double d = 0;
    	mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
 
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            d = Math.sqrt(Math.pow((OpLong - longitude), 2) + Math.pow((OpLat - latitude), 2));
            String I = String.valueOf(d);
            lblAnotherAddress.setText(I);
            
        } else {
 
            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    	
    }
    public void doGetList()	{
		try{final String NAMESPACE="http://nhanau.com/";
			final String METHOD_NAME="getListAddress";
			final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
			SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope=
					new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet=true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal=new MarshalFloat();
			marshal.register(envelope);
			HttpTransportSE androidHttpTransport=
					new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);
			//Get Array Catalog into soapArray
			SoapObject soapArray=(SoapObject) envelope.getResponse();
			arrAdr.clear();
			//soapArray.getPropertyCount() return number of 
			//element in soapArray
			for(int i=0; i<soapArray.getPropertyCount(); i++)
			{
			   //(SoapObject) soapArray.getProperty(i) get item at position i
			   SoapObject soapItem =(SoapObject) soapArray.getProperty(i);
			   String longtitude= soapItem.getProperty("longtitude").toString();
			   String lattitude=soapItem.getProperty("lattitude").toString();
			   arrAdr.add(longtitude+" - "+lattitude);
			   lblAnotherAddress.setText(longtitude+" - "+lattitude);
			}
			adapter.notifyDataSetChanged();
		}
		catch(Exception e){}}
 
}