package com.example.stunkysgps;

import java.util.Random;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import edu.uci.ics.ics163.gpsdrawupload.Point;
import edu.uci.ics.ics163.gpsdrawupload.StrokeManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Build;

public class MainActivity extends ActionBarActivity implements
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener,
	LocationListener{
	
	private String lastProvider;
	private static String lastLocation;
	private String lastBearing;
	private String lastSpeed;
	private String lastExtra;
	private boolean mLocationClientConnected;
	private static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static StrokeManager strokeManager;
	private static boolean penDown;
	private static String strokeName;
	private static int r;
	private static int g;
	private static int b;

	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static Random rnd = new Random();

	static String randomString( int len ) 
	{
	   StringBuilder sb = new StringBuilder( len );
	   for( int i = 0; i < len; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}
	
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    	mLocationClient = new LocationClient(this, this, this);
    	mLocationRequest = LocationRequest.create();
    	mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    	mLocationRequest.setInterval(UPDATE_INTERVAL);
    	mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    	mLocationClient.connect();
    	penDown = false;
    	r = 0;
    	g = 0;
    	b = 0;
//    	strokeNumber = 0;
    	strokeName = randomString(10);
    	strokeManager = new StrokeManager();
    }

    @Override
    protected void onStart(){
    	super.onStart();
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	
    	mLocationClient.disconnect();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	public static EditText groupName;
    	public static EditText drawingName;
    	public static TextView locationView;
    	public static TextView bufferView;
    	public static Button updateButton;
    	public static RadioGroup radioColorGroup;
    	public static RadioButton radioBlackButton;
    	public static RadioButton radioBrownButton;
    	public static RadioButton radioPurpleButton;
    	public static ToggleButton penDownButton;
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            groupName = (EditText) rootView.findViewById(R.id.editText1);
            drawingName = (EditText) rootView.findViewById(R.id.editText2);
            locationView = (TextView) rootView.findViewById(R.id.textView6);
            bufferView = (TextView) rootView.findViewById(R.id.textView7);
            radioColorGroup = (RadioGroup) rootView.findViewById(R.id.radio_color_group);
            radioBlackButton = (RadioButton) rootView.findViewById(R.id.radio_black);
            radioBrownButton = (RadioButton) rootView.findViewById(R.id.radio_brown);
            radioPurpleButton = (RadioButton) rootView.findViewById(R.id.radio_purple);
            updateButton = (Button) rootView.findViewById(R.id.button1);
            updateButton.setOnClickListener(new View.OnClickListener() {
            	public void onClick(View v) {
            		strokeManager.upload(groupName.getText().toString(), drawingName.getText().toString());
            	}
            });
            penDownButton = (ToggleButton) rootView.findViewById(R.id.toggleButton1);
            penDownButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked) {
						//The toggle is enabled
	            		int selectedId = radioColorGroup.getCheckedRadioButtonId();
	            		if(selectedId == radioPurpleButton.getId())
	            		{
	            			r = 128;
	            			g = 0;
	            			b = 128;
	            		}
	            		else if(selectedId == radioBrownButton.getId())
	            		{
	            			r = 117;
	            			g = 80;
	            			b = 22;
	            		}
	            		else
	            		{
	            			r = 0;
	            			g = 0;
	            			b = 0;
	            		}
            			strokeManager.setStrokeColor(strokeName, r,g,b);
	            		penDown = true;
					} else {
						//The toggle is disabled
						penDown = false;
						strokeName = randomString(10);
					}
				}
			});
            return rootView;
        }
    }
    
    private void updateUI(){
    	runOnUiThread(new Runnable(){
    		@Override
    		public void run() {
    			if((PlaceholderFragment.locationView != null) && (lastLocation != null)){
    				PlaceholderFragment.locationView.setText(lastLocation);
    				PlaceholderFragment.bufferView.setText("Strokes: "+strokeManager.countStrokes()+
    						" Points: "+strokeManager.countPoints());
    			}
    		}
    	});
    }

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			Dialog errorDialog = GooglePlayServicesUtil
					.getErrorDialog(result.getErrorCode(), this,
							CONNECTION_FAILURE_RESOLUTION_REQUEST);
			if (errorDialog != null)
			{
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getFragmentManager(), "Location Updates");
			}
		}
	}


	@Override
	public void onConnected(Bundle dataBundle) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		
		Location mCurrentLocation = mLocationClient.getLastLocation();
		lastProvider = mCurrentLocation.getProvider();
		lastLocation = "("+mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude()+")";
		lastBearing = mCurrentLocation.getBearing()+"";
		lastSpeed = mCurrentLocation.getSpeed()+"";
		lastExtra = "";
		Bundle extras = mCurrentLocation.getExtras();
		if(extras != null){
			for(String s:extras.keySet()){
				lastExtra += s+","+extras.get(s).toString() + "\n";
			}
		}
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		updateUI();
	}


	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Point point = new Point(location.getTime(), location.getLatitude(), location.getLongitude());
		String display = "("+point.getLat()+","+point.getLng()+")";
		lastLocation = display;
		if(penDown)
		{
			strokeManager.addPoint(strokeName, point);
		}
		updateUI();
	}


//	@Override
//	public void onProviderDisabled(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void onProviderEnabled(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//		// TODO Auto-generated method stub
//		
//	}
}
