package com.webs.itmexicali.bluino;

import com.google.android.gms.ads.*;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Main extends Activity {
    // Debugging
    public static final String TAG = "BluIno";
    public static final boolean D = false;
    
    
    public SharedPreferences settings;// To save preferences accessible by other Apps, just 
    public SharedPreferences.Editor settingsEditor;
    
    public static int currentContentView=0;

    // Message types sent from the BlueInterfaceService Handler
    public static final int MESSAGE_STATE_CHANGE = 1, MESSAGE_READ = 2, MESSAGE_WRITE = 3, MESSAGE_DEVICE_NAME = 4, MESSAGE_TOAST = 5, SYNC_CONNECTION = 6,
    // Intent request codes
    						REQUEST_CONNECT_DEVICE = 1, REQUEST_ENABLE_BT = 2;
    
    // Key names received from the BlueInterfaceService Handler
    public static final String DEVICE_NAME = "device_name", TOAST = "toast";
    
    // Layout Views
    @SuppressWarnings("unused")
	private PadView PView;
    private EditText mOutEditText;
    private Dialog menu;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    

    //AdMob Advertising
    /** The view to show the ad. */
    private AdView adView;

    /** Your ad unit id. Replace with your actual ad unit id. */
    private static final String AD_UNIT_ID = "ca-app-pub-4741238402050454/9514283406";
    		//this is for package name: com.itmexicali.webs.bluino
    		//"ca-app-pub-4741238402050454/3079300200";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        
        // Set up the window layout
        setContentView(R.layout.main_view);
        //PView=new PadView(this);
        //PView = (PadView) findViewById(R.id.padView);
        
        /*************************************	ADS ADMOB	*********************************************/
        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(AD_UNIT_ID);
        adView.setAdListener(new AdListener() {
        	  @Override
        	  public void onAdOpened() {
        	    // Save app state before going to the ad overlay.
        		  if(Main.D){
        			  Log.d(TAG,"AdView - Opened");
        		  }
        	  }
        	  @Override
        	  public void onAdFailedToLoad(int errorCode){
        		  if(Main.D){
        			  Log.d(TAG,"AdView - FailedToLoad = "+errorCode);
        		  }
        	  }
        	});
        
        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.LayMain);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    	params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layout.addView(adView, params);
        
        
        //LinearLayout layout = (LinearLayout) findViewById(R.id.linLayMain);
        //layout.addView(adView);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device.
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            //.addTestDevice("584586A082596B5844C4E301E1285E95") //My Nexus 4
            .build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        /*************************************	ADS ADMOB	*********************************************/
        

        //get the preferences saved on the file named as TAG
        settings = getSharedPreferences(TAG, 0);					//to read
        settingsEditor = settings.edit();							//to write
        
        
        //Get saved preferences if the Exist
        Options.getInstance().setValues(settings.getInt("Lbar",180), settings.getInt("Rbar", 180));
        
        menu = new Dialog(Main.this);
        menu.setContentView(R.layout.options);
        menu.setTitle(R.string.changeValues);
        menu.setCancelable(true);
        
        Button button = (Button) menu.findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
        @Override
            public void onClick(View v) {
       	 		if(D)
       	 			Log.i(TAG,"Change - OK");
                try {
                	int x1=(int)Double.parseDouble((((EditText)menu.findViewById(R.id.editText1)).getText()).toString());
                	int x2=(int)Double.parseDouble((((EditText)menu.findViewById(R.id.editText2)).getText()).toString());
                	if(x1<=9999 && x2<=9999){
                		Options.getInstance().setValues(x1,x2);
                		Toast.makeText(getApplicationContext(), R.string.confUpdated, Toast.LENGTH_SHORT).show();
                		settingsEditor.putInt("Lbar", x1);
                		settingsEditor.putInt("Rbar", x2);
                		settingsEditor.commit();
                		if (Main.D)
                			Log.d(Main.TAG, "Left value: " + x1+" Right value: "+x2); 
                		if (BluetoothService.mBlueService != null)
                			if (BluetoothService.mBlueService.getState() == BluetoothService.STATE_CONNECTED){
                				BluetoothService.mBlueService.write(('L'+PadView.intToString(x1)+'R'+PadView.intToString(x2)).getBytes());
                				
                			}
                		if(D)
               	 			Log.i(TAG,"SettingsEditor Commited Succesfully");
                		menu.dismiss();
                		
                	}
                	else
                		Toast.makeText(getApplicationContext(), R.string.maxInputValue, Toast.LENGTH_SHORT).show();
				} catch (Throwable e) {	
					if(D) Log.e(TAG,e.getMessage()+" - Click");
					Toast.makeText(getApplicationContext(), R.string.intOnly, Toast.LENGTH_SHORT).show();
				}
            }
        });
        
        button = (Button) menu.findViewById(R.id.button2);
        button.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View v) {
           	 		if(D)
           	 			Log.i(TAG,"Change - Default");
                    try {
                    	Options.getInstance().setValues(180,180);
                    	((EditText)menu.findViewById(R.id.editText1)).setText(Integer.toString(Options.getInstance().getLBarValue()));
                        ((EditText)menu.findViewById(R.id.editText2)).setText(Integer.toString(Options.getInstance().getRBarValue()));
                    	Toast.makeText(getApplicationContext(), R.string.confUpdated, Toast.LENGTH_SHORT).show();
                    	menu.dismiss();
    				} catch (Throwable e) {	if(D) 		Log.e(TAG,e.getMessage()+" - Click");}
                }
            });
        
        button = (Button) menu.findViewById(R.id.button3);
        button.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View v) {
           	 		if(D)
           	 			Log.i(TAG,"Change - CANCEL");
                    try {
                    	menu.dismiss();
    				} catch (Throwable e) {	if(D) 		Log.e(TAG,e.getMessage()+" - Click");}
                }
            });
        
        
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    	
    }


    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (BluetoothService.mBlueService == null) setupChat();
        }
        
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (BluetoothService.mBlueService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (BluetoothService.mBlueService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
              BluetoothService.mBlueService.start();
            }
        }
        
        //AdMob Advertising
        if (adView != null) {
            adView.resume();
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        
        // Initialize the BlueInterfaceService to perform bluetooth connections
        BluetoothService.mBlueService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
    	if(D) Log.e(TAG, "- ON PAUSE -");
    	
    	//AdMob Advertising
    	if (adView != null) {
    	      adView.pause();
    	}
        
    	super.onPause();
        
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (BluetoothService.mBlueService != null){
        	BluetoothService.mBlueService.stop();
        }
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=  BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (BluetoothService.mBlueService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BlueInterfaceService to write
            byte[] send = message.getBytes();
            BluetoothService.mBlueService.write(send);
            if(D)
            	Log.d(TAG,"Message Sent: "+message);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }


    // The Handler that gets information back from the BlueInterfaceService
    private final Handler mHandler = new Handler() {    	
    @Override
    public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    //Let arduino Know the ratio we're going to use.
                    BluetoothService.mBlueService.write(('L'+PadView.intToString(Options.getInstance().getLBarValue())
                    		+'R'+PadView.intToString(Options.getInstance().getLBarValue())).getBytes());
                    break;
                case BluetoothService.STATE_CONNECTING:
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    break;
                }
                break;
            case SYNC_CONNECTION:
            	if(D)
        			Log.i(TAG,"SYNCED Connection: "+msg.obj);
            	break;
            case MESSAGE_WRITE:
                //byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
               // String writeMessage = new String(writeBuf);
                try {
            		
            	}catch(NumberFormatException e) {}
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                if(msg.arg1>0) {
                	String readMessage = new String(readBuf, 0, msg.arg1);
                	try {
                		if(D)
                			Log.i(TAG,"RemoteMsg: \""+readMessage+"\"");
                	}catch(NumberFormatException e) {}//tv.setText(readMessage);}
                }
                break;
                
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };


    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device=null ;
                try{
                	device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                }catch(IllegalArgumentException e){ 
                	Log.e(TAG,"@Main-getRemoteDevice(address)",e);
                	Toast.makeText(getApplicationContext(), R.string.illegaldevice, Toast.LENGTH_SHORT).show();
                }
               
                if(device!=null)
                	BluetoothService.mBlueService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //if(BluetoothService.mBlueService != null)
        	inflater.inflate(R.menu.option_menu_offline, menu);
        //else
        	//inflater.inflate(R.menu.option_menu_offline, menu);
        return true;
    }
    
    public boolean onPrepareOptionsMenu(Menu menu){
    	menu.clear();
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(BluetoothService.mBlueService != null && mBluetoothAdapter.isEnabled() ? 
        		R.menu.option_menu:R.menu.option_menu_offline, menu);
        return true;    	
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        case R.id.options:
        	//show options menu dialog
        	try{
        		((EditText)menu.findViewById(R.id.editText1)).setText(Integer.toString(Options.getInstance().getLBarValue()));
                ((EditText)menu.findViewById(R.id.editText2)).setText(Integer.toString(Options.getInstance().getRBarValue()));
                menu.show();
        	}catch(Exception ex){if(D)Log.e(TAG,ex.getMessage());}
        	return true;
        case R.id.exit:
        	finish();
        	return true;
        }
        return false;
    }

    /** Called Before onPause(). */
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
      	if(D){
    		Log.i(TAG,"**OnSaveInstanceState**");
    	}
    	  // Save UI state changes to the savedInstanceState.
    	  // This bundle will be passed to onCreate if the process is
    	  // killed and restarted.
    	  savedInstanceState.putBoolean("MyBoolean", true);
    	  savedInstanceState.putDouble("myDouble", 1.9);
    	  //savedInstanceState.putInt("timesClosed", ++i);
    	  savedInstanceState.putString("MyString", "Welcome back to Android");
    	  // etc.
    	}

    /** Called Before onResume */
    @SuppressWarnings("unused")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	  super.onRestoreInstanceState(savedInstanceState);
    	  if(D){
      		Log.i(TAG,"*OnRestoreInstanceState*");
      	}
    	  // Restore UI state from the savedInstanceState.
    	  // This bundle has also been passed to onCreate.
    	  boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
    	  double myDouble = savedInstanceState.getDouble("myDouble");
    	  //i = savedInstanceState.getInt("timesClosed");
    	  String myString = savedInstanceState.getString("MyString");
    	}

    
}