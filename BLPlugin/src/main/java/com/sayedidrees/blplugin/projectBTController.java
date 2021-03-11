package com.sayedidrees.blplugin;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


public class projectBTController {
    private static final String TAG = "hacker";
    private static final String APP_NAME = "SecretKeySayedIdrees";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler mhandler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int state;

    static final int STATE_NONE = 0;
    static final int STATE_LISTEN = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;

    public projectBTController(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        this.mhandler = handler;
    }

    // Set the current state of the chat connection
    private synchronized void setState(int state) {
        this.state = state;
        mhandler.obtainMessage(HandlerConstants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    // get current connection state
    public synchronized int getState() {
        return state;
    }

    // start service
    public synchronized void startListening() {
        // Cancel any thread
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any running thresd
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    // initiate connection to remote device
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel running thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    // manage Bluetooth connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel running thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // This thread manage the secure connection and perform data transaction
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        // This will send name of the device to main Thread
        Message msg = mhandler.obtainMessage(HandlerConstants.MESSAGE_DEVICE_OBJECT);
        Bundle bundle = new Bundle();
        bundle.putParcelable(HandlerConstants.DEVICE_OBJECT, device);
        msg.setData(bundle);
        mhandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    // stop all threads
    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread conn;
        synchronized (this) {
            if (state != STATE_CONNECTED)
                return;
            conn = connectedThread;
        }
        conn.write(out);
    }

    private void connectionFailed() {
        Message msg = mhandler.obtainMessage(HandlerConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        mhandler.sendMessage(msg);

        // Start the service over to restart listening mode
        projectBTController.this.startListening();
    }

    private void connectionLost() {
        Message msg = mhandler.obtainMessage(HandlerConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        mhandler.sendMessage(msg);

        // Start the service over to restart listening mode
        projectBTController.this.startListening();
    }


    // This thread listen to other devices
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "AcceptThread: (Accept Thread)  Error: " +  e.getMessage());

            }
            serverSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: (Accept Thread)  initiated");
            setName("AcceptThread");
            BluetoothSocket socket;
            while (state != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                    Log.d(TAG, "run: (Accept Thread)  Insecure link is  Established!");
                } catch (IOException e) {
                    Log.d(TAG, "run: (Accept Thread) Error: " +  e.getMessage());
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (projectBTController.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Success connected and now proceed to the Connected Thread
                                connected(socket, socket.getRemoteDevice());
                                Log.d(TAG, "run: (Accept Thread)  Accept Thread Connected!");
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //Closing socket to reserve Energy and resources
                                try {
                                    socket.close();
                                    Log.d(TAG, "run: (Accept Thread)  Accept Thread Closed (To leave Resources)!");
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
            Log.d(TAG, "run: (Accept Thread)  Accept Thread Ended!");
        }

        public void cancel() {
            try {
                serverSocket.close();
                Log.d(TAG, "run: (Accept Thread)  Accept Thread Forced Closed !");
            } catch (IOException e) {
            }
        }
    }

    // This Thread try to create connection with other devices
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "ConnectThread: Started!");
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                Log.d(TAG, "ConnectThread: Initializing RFCOMMS!");
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "ConnectThread: Error : " + e.getMessage());
            }
            Log.d(TAG, "ConnectThread: RFCOMM Successfully configured!");
            socket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // this is done for a reason because
            // it will run in background forever and will use more of the eneryg  : hell yeah idrees  you are smart
            // also > it reduces speed of connectivity!
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "run: Discovery Disabled to Speedup Connection");

            // Make a connection to the BluetoothSocket
            try {
                Log.d(TAG, "run: (ConnectThread) Trying to create secure connection!");
                socket.connect();
            } catch (IOException e) {
                try {
                    Log.d(TAG, "run: ConnectThread  :  Error :" +e.getMessage());
                    socket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                Log.d(TAG, "run: ConnectThread Closed!");
                return;
            }

            // Reset the ConnectThread to release resources and presever engergy
            synchronized ( projectBTController.this) {
                connectThread = null;
            }

            // Initlaize connected thread
            connected(socket, device);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: (connectThread) Force Close the Connect thread");
                socket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: (connectThread) Error to Close the Connected thread!" );
            }
        }
    }

    // This thread helps to send and receive data
    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(TAG, "ConnectedThread: In and Out Stream initialized!");
            } catch (IOException e) {
                Log.d(TAG, "ConnectedThread: In and Out Stream Failed to Init! Error : " +e.getMessage());
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
            Log.d(TAG, "ConnectedThread: Connected Thread Started Successfully!");
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // This is for keep listening for input data
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mhandler.obtainMessage(HandlerConstants.MESSAGE_READ, bytes, -1,buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    Log.d(TAG, "run: Error sending getting message : "+ e.getMessage());
                    // Restart Listening Mode
                    projectBTController.this.startListening();
                    break;
                }
            }
        }

        // write to OutputStream
        public void write(byte[] myByte) {
            String sendIt = new String(myByte, Charset.defaultCharset());
            try {
                Log.d(TAG, "write: Trying to Send message : " + sendIt);
                outputStream.write(myByte);
                mhandler.obtainMessage(HandlerConstants.MESSAGE_WRITE, -1, -1,
                        myByte).sendToTarget();
            } catch (IOException e) {
                Log.d(TAG, "write: Errror Sending Message");
            }
            Log.d(TAG, "write: Message Send : " + sendIt);
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
