package com.sayedidrees.blplugin;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;

import static com.sayedidrees.blplugin.HandlerConstants.DEVICE_OBJECT;

public class BLC_Main {


    private String TAG = "hacker";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;

    private BluetoothAdapter mbluetoothAdapter;
    private Context mContext;
    public ArrayList<BluetoothDevice> mNewBTDevices = new ArrayList<BluetoothDevice>() ;
    public ArrayList<BluetoothDevice> mBondedBTDevices = new ArrayList<BluetoothDevice>() ;

    private BluetoothDevice conDevice;

    private projectBTController myConnectorManager;
    private responder myResponder;
    public BLC_Main(Context context)
    {
        Log.d(TAG, "BLC_Main: Plugin Starts");
        mContext = context;
        mbluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        mNewBTDevices = new ArrayList<BluetoothDevice>() ;
        mBondedBTDevices = new ArrayList<BluetoothDevice>() ;
        myConnectorManager = new projectBTController(context,mhandler);
        myResponder = new responder();
        if (mbluetoothAdapter == null) {
            Log.d(TAG, "Error : Bluetooth Not Available!");
            return;
        }

    }

    private Handler mhandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case projectBTController.STATE_CONNECTED:
                            setStatus("Connected to: " + conDevice.getName());
                            myResponder.onDeviceConnected(conDevice.getName());
                            break;
                        case projectBTController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            break;
                        case projectBTController.STATE_LISTEN:
                        case projectBTController.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "handleMessage: MEssage  WRITE : " + writeMessage);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "handleMessage: Message  READ : " + readMessage);
                    myResponder.onDataReceive(readMessage);
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    conDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    String deviceNAme = conDevice.getName();
                    Log.d(TAG, "handleMessage: Device Connected : " + deviceNAme);
                    myResponder.onDeviceConnected(deviceNAme);
                    // this is called when device is connected
                    break;
                case MESSAGE_TOAST:
                    String msgToast = msg.getData().getString("toast");
                    break;
            }
            return false;
        }
    });

    private void initiate_Discoverable() {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        mContext.startActivity(discoverableIntent);
        IntentFilter intentFilter = new IntentFilter(mbluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        mContext.registerReceiver(Discoverable_Server,intentFilter);
    }

    private void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "connectToDevice: Trying to Connected to Device!");
        mbluetoothAdapter.cancelDiscovery();
        myConnectorManager.connect(device);
    }

    //region  Methods Which is Requested by Unity

    private void connectBLE(String deviceName)
    {
        Log.d(TAG, "Unity Request for Connection to :" + deviceName);
        for (int i = 0; i < mNewBTDevices.size(); i++) {
            BluetoothDevice dd =   mNewBTDevices.get(i);
            if (deviceName.equals(dd.getName()))
            {
                connectToDevice(dd);
                break;
            }
        }
    }

    private void StartServer()
    {
        if (mbluetoothAdapter == null) {
            Log.d(TAG, "StartServer: Error : Bluetooth Not Available!");
            return;
        }

        // this was used to change the name of the device but will use later
        /*
        String btName = "Yoshi"; // bt must be enabled
        mbluetoothAdapter.setName(btName);
        while(true)
        {
            if (mbluetoothAdapter.getName()==btName)
                Log.d("hacker", "StartServer: Bluetooth Name Changed");
                break;
        }*/


        Log.d(TAG, "StartServer: Checking Bleutoth$$$");
        if (!mbluetoothAdapter.isEnabled()) {
            Log.d(TAG, "StartServer: Bluetooth is Disabled");
            mbluetoothAdapter.enable();
        }

        if (!mbluetoothAdapter.isEnabled())
        {  mbluetoothAdapter.enable();
        }else
        {
            myConnectorManager = new projectBTController(mContext, mhandler);
        }


        initiate_Discoverable();

        if (myConnectorManager != null) {
            if (myConnectorManager.getState() == projectBTController.STATE_NONE) {
                Log.d(TAG, "StartServer: Server Started Listening!");
                myConnectorManager.startListening();
            }else
            {
                Log.d(TAG, "StartServer: Error : State is not NONE");
            }
        }else
        {
            Log.d(TAG, "StartServer: Error connection manager is null");
        }

    }

    private void scanDevices()
    {
        if (!mbluetoothAdapter.isEnabled()) {
            mbluetoothAdapter.enable();
        }

        Log.d("hacker", "StartClient Requested");
        if (mbluetoothAdapter.isDiscovering()) {
            mbluetoothAdapter.cancelDiscovery();
        }
        Log.d(TAG, "scanDevices: Started Scanning");
        mbluetoothAdapter.startDiscovery();

        // this is used to get paired devices
        Set<BluetoothDevice> pairedDevices = mbluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "scanDevices: Already Bonded Devices found: " + device.getName());
                mBondedBTDevices.add(device);
            }
        }
        // Else no bonded device found!

        Log.d(TAG, "scanDevices: Searching for more devices");
        // Broadcast for when new device is found
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(discoveryFinishReceiver, filter);

        // broadcast when the discover scanning is finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(discoveryFinishReceiver, filter);
    }

    private void sendData(String sendTxt)
    {
        Log.d(TAG, "Unity Request to send data of : " + sendTxt);
        byte[] bytes = sendTxt.getBytes((Charset.defaultCharset()));
        myConnectorManager.write(bytes);
    }

    private void setStatus(String txt)
    {
        Log.d(TAG, "setStatus: Status : " + txt);

    }

    private boolean checkBluetoothAvailability()
    {
        Log.d(TAG, "checkBluetoothAvailability: REquested by Unity");
        boolean isEnabled = mbluetoothAdapter.isEnabled();
        myResponder.onCheck_BluetoothStatus(isEnabled);
        Log.d(TAG, "checkBluetoothAvailability: REsult : " +isEnabled);
        return isEnabled;
    }

    private void closeAll()
    {
        Log.d(TAG, "closeConn:  Server All connection Closed" );
       myConnectorManager.stop();

    }


    //endregion


    //region  BroadCast Receivers

    // this is called when new device is found or when scanning is finished!
    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName()==null || device.getName().isEmpty()){
                    Log.d(TAG, "onReceive: Null pointer Controlled");
                    return;}

                mNewBTDevices.add(device);
                Log.d(TAG, "onReceive: Device Found!! Name : " + device.getName());
                myResponder.onDeviceFound(device);
                // this is when you want to separate the Bonded devices from the New Devices.
                /* if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewBTDevices.add(device);
                    Log.d(TAG, "onReceive: New Device Found! Name : " + device.getName());
                    myResponder.onDeviceFound(device);
                }*/
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                if (mNewBTDevices.size() == 0) {
                    // no device found
                    Log.d(TAG, "onReceive: No New Device FOund!");
                }else
                {
                    Log.d(TAG, "onReceive: Scanning completed and Finished");
                }
            }
        }
    };


    //  This is called when discover mode is changed
    private final BroadcastReceiver Discoverable_Server = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mbluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                final int mode= intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,mbluetoothAdapter.ERROR);
                switch (mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("hacker", "onReceive: Discoverability Enabled for n Seconds!");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("hacker", "onReceive: Able to Receive Connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("hacker", "onReceive: Disable Discovery!");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("hacker", "onReceive: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("hacker", "onReceive: Connected! ");
                        break;
                }
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (myConnectorManager != null)
            myConnectorManager.stop();
    }


    //endregion

}
