package solarsensei.com.gatech.edu.solartracker;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by timothybaba on 5/19/17.
 */
public class SensorActivity extends AppCompatActivity {
    /**
     * widgets
     */
    private Button mButton;
    private TextView mPressureView;
    private TextView mTempView;
    private TextView mLightView;
    private TextView mHumidityView;
    private TextView mMagneticView;
    private TextView azimuthView;
    private TextView pitchView;
    private TextView rollView;
    private TextView dataView;
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationValues = new float[3];
    private TextView connectionStatus;
    private ListView pairedDevices;
    private TextView msg;


    //motion sensors
    private Sensor mRotation;

    //constants
    private final static int REQUEST_ENABLE_BT = 1;

    Handler bluetoothIn;
    final int handlerState = 0;
    //private BluetoothServerSocket btServerSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private BluetoothAdapter mBtAdapter;

    private ConnectedThread mConnectedThread;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mPressureView = (TextView) findViewById(R.id.pressureReading);
        mTempView = (TextView) findViewById(R.id.tempReading);
        mLightView = (TextView) findViewById(R.id.lightReading);
        mHumidityView = (TextView) findViewById(R.id.rHumidity);
        mMagneticView = (TextView) findViewById(R.id.magneticField);
        azimuthView = (TextView) findViewById(R.id.azimuth);
        pitchView = (TextView) findViewById(R.id.pitch);
        rollView = (TextView) findViewById(R.id.roll);
        dataView = (TextView) findViewById(R.id.data);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();



        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    if (readMessage.length() > 0) {
                        char firstChar = readMessage.charAt(0);
                        switch (firstChar) {
                            case '1': mPressureView.setText(readMessage.substring(1));
                                break;
                            case '2': mTempView.setText(readMessage.substring(1));
                                break;
                            case '3': mLightView.setText(readMessage.substring(1));
                                break;
                            case '4': mHumidityView.setText(readMessage.substring(1));
                                break;
                            case '5': mMagneticView.setText(readMessage.substring(1));
                                break;
                            case '6':
                                System.out.println(readMessage);
                                azimuthView.setText(readMessage.substring(readMessage.indexOf('A') + 1, readMessage.indexOf('P')));
                                pitchView.setText(readMessage.substring(readMessage.indexOf('P') + 1, readMessage.indexOf('R')));
                                rollView.setText(readMessage.substring(readMessage.indexOf('R') + 1));
                                break;
                            default:
                                break;

                        }

                    }
                }
            }
        };


    }
    @Override
    protected void onResume() {
        super.onResume();
        AcceptThread accept = new AcceptThread();
        accept.run();
    }

    private void manageMyConnectedSocket(BluetoothSocket btSocket) {
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.run();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            int i = 0;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    System.out.println(i + "yes");
                    i++;
                    if (bluetoothIn != null) {
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBtAdapter.listenUsingRfcommWithServiceRecord("SecureConnection", BTMODULEUUID);
            } catch (IOException e) {
               // Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                   // Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {

                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}

