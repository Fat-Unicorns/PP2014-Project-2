package fat_unicorns.pp2014_project_2;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements ConnectionCallbacks,OnConnectionFailedListener, GooglePlayServicesClient.ConnectionCallbacks {

    private final String TAG = "MapsActivity"; // tag for debugging
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =  MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Context mContext;
    private PendingIntent mActivityRecognitionPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private Marker me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Activity Recognition Display Overlay
        ((Fragment) getFragmentManager().findFragmentById(R.id.ar_fragment)).getView().setBackgroundResource(android.R.color.transparent);
        mContext = getApplicationContext();

        // Activity Recognition
        mActivityRecognitionClient = new ActivityRecognitionClient(mContext, this, this);
        Intent intent = new Intent(mContext, ActivityRecognition.class);
        mActivityRecognitionPendingIntent = PendingIntent.getService(mContext, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mInProgress = false;
        startUpdates();

        // Google Maps
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

    }

    public void startUpdates() {

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            if (!mInProgress) {
                mInProgress = true;
                mActivityRecognitionClient.connect();
                //
            } else {
                mActivityRecognitionClient.disconnect();
                mActivityRecognitionClient.connect();
                mInProgress = true;
            }
        }
    }


    /**
     * Initializes the Google Location API Client and registers callback-handlers.
     * Checks that Google Play Services is available first.
     */
    private void startLocationTracking() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mLocationClient = new LocationClient(mContext,this,this);
            mLocationClient.connect();
        }
    }

    /**
     * Callback-handler for when the connection to the Location API is established.
     * This method will be invoked asynchronously when the connect request has successfully completed.
     */
    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(100).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "Received a new location " + location);
                LatLng pos = new LatLng(location.getLatitude(),location.getLongitude());
                if(mMap != null){

                    me = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title("Here I am!"));


                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(pos)      	        // Sets the center of the map to the user's location
                            .zoom(15)                    // Sets the zoom
                            .bearing(0)                 // Sets the orientation of the camera to east
                            .tilt(15)                   // Sets the tilt of the camera to north
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                }
            }
        };
        // Request location updates
        if(mLocationClient.isConnected()) mLocationClient.requestLocationUpdates(locationRequest,mLocationListener);

        // Request activity updates
        if(mActivityRecognitionClient.isConnected()) mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, mActivityRecognitionPendingIntent);
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        // Location Trackting
        startLocationTracking();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Delete the client
        mActivityRecognitionClient = null;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "ConnectionFailed");

        // Turn off the request flag
        mInProgress = false;
        /*
         * If the error has a resolution, start a Google Play services
         * activity to resolve it.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
            // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                /*
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(),"Activity Recognition");
                */
            }
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
