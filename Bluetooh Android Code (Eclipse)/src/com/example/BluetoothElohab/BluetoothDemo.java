/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.BluetoothElohab;

import com.example.BluetoothElohab.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
//import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
//import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothDemo extends Activity {
    // Debugging
    //private static final String TAG = "BluetoothDemo";
    //private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Gerekli degisken tanýmlamalarý
    private TextView mTitle;
    private ListView mConversationView;
    //private EditText mOutEditText;
    //private Button mSendButton;
    private Button Led1Buton;
    private Button Led2Buton;
    private RadioButton Led1;
    private RadioButton Led2;
    private ProgressBar sicaklik;
    private ToggleButton durum;
    private TextView isidegeri;
    private boolean LED1check=false;
    private boolean LED2check=false;
    private byte[] veri = new byte[1];	//1 byte veri gönderme deðiþkeni
    

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    //private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothDemoService mChatService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        //if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothDemoService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }
 
    private void setupChat() {
        //Log.d(TAG, "setupChat()");
 
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        //mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
       /* mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });*/
        //Buton1'e basýldýðýnda LED1 in durumuna göre LED1 yakýlýr veya söndürülür.
        Led1Buton = (Button) findViewById(R.id.button1);
        Led1Buton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(LED1check){
            	veri[0] &= ~(0x01);}
            	else
            	{veri[0] |= 0x01;}
            	sendData(veri);
            }
        });
		//Buton2'ye basýldýðýnda LED2 in durumuna göre LED2 yakýlýr veya söndürülür.
        Led2Buton = (Button) findViewById(R.id.button2);
        Led2Buton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {           	
            	if(LED2check){
            		veri[0] &= ~(0x02);}                	
                	else
                	{veri[0] |= 0x02;}
            	sendData(veri);
            }
        });
        
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothDemoService(this, mHandler);

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        //if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        //if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        //if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        //if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
  private void sendData(byte[] send){
	  if (mChatService.getState() != BluetoothDemoService.STATE_CONNECTED) {
          Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
          return;
      }
	  
	  
	  if(send.length>0)
		  mChatService.write(send);
  }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    /*private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }*/

    // The action listener for the EditText widget, to listen for the return key
   /* private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };*/

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                //if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothDemoService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothDemoService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothDemoService.STATE_LISTEN:
                case BluetoothDemoService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                } 
                break;
            /*case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                //Toast.makeText(getApplicationContext(), "Bilgi gönderildi",Toast.LENGTH_SHORT).show();
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;*/
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                Led1 = (RadioButton) findViewById(R.id.LED1);
                Led2 = (RadioButton) findViewById(R.id.LED2);
                sicaklik = (ProgressBar) findViewById(R.id.progressBar1);
                durum = (ToggleButton) findViewById(R.id.toggleButton1);
                isidegeri = (TextView) findViewById(R.id.sicaklik);
                //Gelen verinin 8. biti 1 ise veri LED ve Buton durum bilgisi olarak deðerlendirilir.
                if((readBuf[0] & 0x80) == 0x80){
				//Gelen verinin 1. bitini durumuna göre LED1 göstergesi düzenlenir.
                if((readBuf[0] & 0x01) == 0x01){
                Led1.setChecked(true);
                LED1check=true;}
                else {
                Led1.setChecked(false);
                LED1check=false;}
                //Gelen verinin 2. bitinin durumuna göre LED2 göstergesi düzenlenir.
                if((readBuf[0] & 0x02)==0x02){
                Led2.setChecked(true);
                LED2check=true;}
                else{
                Led2.setChecked(false);
                LED2check=false;}
                //Gelen verinin 3. bitinin durumuna göre Buton durum göstergesi düzenlenir.
                if((readBuf[0] & 0x04)==0x04)
                	durum.setChecked(true);
                    else
                    durum.setChecked(false);}
				//Gelen verinin 8. biti sýfýr ise gelen veri sýcaklýk bilgisi olarak deðerlendirilir.
                else {
            	if(readBuf[0]>100) readBuf[0]=100;		//Sýcaklýk deðeri max 100 dereceyi aþmýcak þekilde ayarlanýr.
				//Sýcaklýk bilgisi görüntülenir.
				sicaklik.setProgress((int)readBuf[0]);
                isidegeri.setText("Sýcaklýk="+readBuf[0] + "°C" );
                
 //              setContentView(R.layout.main);
 
                if(readBuf[0]>14 & readBuf[0]<=16){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther1);}
               
                else if(readBuf[0]>16 & readBuf[0]<=18){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther2);}
                
                else if(readBuf[0]>18 & readBuf[0]<=20){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther3);}
                
                else if(readBuf[0]>20 & readBuf[0]<=22){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther5);}
                
                else if(readBuf[0]>22 & readBuf[0]<=24){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther6);}
                
                else if(readBuf[0]>24 & readBuf[0]<=26){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther7);}
                
                else if(readBuf[0]>26 & readBuf[0]<=28){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther8);}
                
                else if(readBuf[0]>28 & readBuf[0]<=30){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther9);}
                
                else if(readBuf[0]>30 & readBuf[0]<=32){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther10);}
                
                else if(readBuf[0]>32 & readBuf[0]<=34){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther11);}
                
                else if(readBuf[0]>34 & readBuf[0]<=36){
                	findViewById(R.id.main).setBackgroundResource(R.drawable.ther12);}
  
                }			
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                //Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
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
        }
        return false;
    }

}