package solarsensei.com.gatech.edu.solartracker;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by timothybaba on 5/19/17.
 */
public class SensorActivity extends AppCompatActivity {
    /**
     * widgets
     */
    private TextView mPressureView;
    private TextView mTempView;
    private TextView mLightView;
    private TextView mHumidityView;
    private TextView mMagneticView;
    private TextView azimuthView;
    private TextView pitchView;
    private TextView rollView;
    private TextView directionView;
    private  TextView transmitView;
    private  Button acceptButton;
    private ProgressDialog mProgressDialog;


    //constants
    private final int handlerState = 0;
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    //Bluetooth accessories
    private Handler bluetoothIn;
    private BluetoothAdapter mBtAdapter;


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
        directionView = (TextView) findViewById(R.id.direction);
        transmitView = (TextView) findViewById(R.id.transmitStatus);
        acceptButton = (Button) findViewById(R.id.accept);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {	//if message is what we want
                    String readMessage = (String) msg.obj;
                    if (readMessage.length() > 0) {

                         String[] splittedwords  = readMessage.split("\\s+");
                        for (String data: splittedwords) {
                            char firstChar = data.charAt(0);
                            switch (firstChar) {
                                case '1':
                                    mPressureView.setText(data.substring(1));
                                    break;
                                case '2':
                                    mTempView.setText(data.substring(1));
                                    directionView.setText(R.string.auto);
                                    break;
                                case '3':
                                    mLightView.setText(data.substring(1));
                                    directionView.setText(R.string.auto);
                                    break;
                                case '4':
                                    mHumidityView.setText(data.substring(1));
                                    directionView.setText(R.string.auto);
                                    break;
                                case '5':
                                    mMagneticView.setText(data.substring(1));
                                    directionView.setText(R.string.auto);
                                    break;
                                case '6':
                                    String[] segMent  =  data.split("p");
                                    azimuthView.setText(segMent[0].substring(1));
                                    pitchView.setText(segMent[1]);
                                    rollView.setText(segMent[2]);
                                    break;
                                case 'R':
                                    directionView.setText(data.substring(1));
                                    break;
                                case 'L':
                                    directionView.setText(data.substring(1));
                                    break;
                                case 'U':
                                    directionView.setText(data.substring(1));
                                    break;
                                case 'D':
                                    directionView.setText(data.substring(1));
                                    break;

                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        };

        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new bluetoothAcceptTask().execute();
            }
        });

    }

    private  class bluetoothAcceptTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            if (SensorActivity.this.mProgressDialog == null) {
                SensorActivity.this.mProgressDialog = new ProgressDialog(SensorActivity.this);
                SensorActivity.this.mProgressDialog.setMessage("Waiting for connection...");
                SensorActivity.this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }

            SensorActivity.this.mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            AcceptThread accept = new AcceptThread();
            accept.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            SensorActivity.this.mProgressDialog.dismiss();
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private BluetoothSocket connectSocket;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            this.connectSocket = socket;

            try {
                //Create input stream for connection
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
        }


        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;

            // Keeps looping to listen for received messages
            while (connectSocket != null) {
                try {
                    bytes = mmInStream.read(buffer);  //reads bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    System.out.println("message: " + readMessage);
                    if (bluetoothIn != null) {
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                        if (readMessage.equals("x")) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast toast =  Toast.makeText(SensorActivity.this, "Accepted Connection!",
                                            Toast.LENGTH_LONG);
                                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    v.setTextColor(Color.GREEN);
                                    mProgressDialog.dismiss();
                                    toast.show();
                                }
                            });
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                transmitView.setText(R.string.receive);
                                acceptButton.setVisibility(View.INVISIBLE);
                            }
                        });


                    }

                } catch (IOException e) {
                    mProgressDialog.dismiss();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast =  Toast.makeText(SensorActivity.this, "Connection Failure",
                                    Toast.LENGTH_SHORT);
                            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            v.setTextColor(Color.RED);
                            toast.show();
                            mPressureView.setText("");
                            mTempView.setText("");
                            mLightView.setText("");
                            mHumidityView.setText("");
                            mMagneticView.setText("");
                            azimuthView.setText("");
                            pitchView.setText("");
                            rollView.setText("");
                            directionView.setText("");
                            transmitView.setText("");
                            acceptButton.setVisibility(View.VISIBLE);
                        }
                    });

                    break;
                }
            }
        }

        //This method when stop accepting data button is called.
        public void cancel() {
            try {
                connectSocket.close();
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast =  Toast.makeText(SensorActivity.this, "Could not close the connect socket",
                                Toast.LENGTH_LONG);
                        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        v.setTextColor(Color.RED);
                        toast.show();
                        transmitView.setText("");
                    }
                });
            }
        }

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBtAdapter.listenUsingRfcommWithServiceRecord("SecureConnection", BT_MODULE_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                   // Log.e(TAG, "Socket's accept() method failed", e);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast =  Toast.makeText(SensorActivity.this, "Socket's accept() method failed",
                                    Toast.LENGTH_LONG);
                            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            v.setTextColor(Color.RED);
                            toast.show();
                            transmitView.setText("");
                        }
                    });

                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    cancel();
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        private void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast =  Toast.makeText(SensorActivity.this, "Could not close the connect socket",
                                Toast.LENGTH_LONG);
                        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        v.setTextColor(Color.RED);
                        toast.show();
                        transmitView.setText("");
                    }
                });
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket btSocket) {
        ConnectedThread mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        mConnectedThread.run();
    }
}

