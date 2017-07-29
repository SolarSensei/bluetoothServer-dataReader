package solarsensei.com.gatech.edu.solartracker;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by timothybaba on 5/19/17.
 */
public class SensorActivity extends AppCompatActivity implements SensorEventListener {
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
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationValues = new float[3];
    private TextView connectionStatus;
    private ListView pairedDevices;
    private TextView msg;

    private SensorManager mSensorManager;

    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;

    //Environmental sensors
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private Sensor mRelativeHumidity;
    private Sensor mMagneticField;

    //motion sensors
    private Sensor mRotation;

    //constants
    private final static int REQUEST_ENABLE_BT = 1;

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
        mButton = (Button) findViewById(R.id.startPairing);


        // Gets an instance of the sensor service, and uses that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);





    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        //To Do
        // Data gets sent to solar panels only when accuracy is high
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // Reads in sensor data.
        try {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_PRESSURE:
                    mPressureView.setText(String.format(getString(R.string.displayResult), event.values[0], "mbars"));
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    mTempView.setText(String.format(getString(R.string.displayResult), event.values[0], "°C"));
                    break;
                case Sensor.TYPE_LIGHT:
                    mLightView.setText(String.format(getString(R.string.displayResult), event.values[0], "°lx"));
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    mHumidityView.setText(String.format(getString(R.string.displayResult), event.values[0], "%"));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagneticView.setText(String.format(getString(R.string.displayResult), event.values[0], "μT"));
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    SensorManager.getOrientation(mRotationMatrix, mOrientationValues);
                    azimuthView.setText(String.format(getString(R.string.displayResult), Math.toDegrees(mOrientationValues[0]), "°"));
                    pitchView.setText(String.format(getString(R.string.displayResult), Math.toDegrees(mOrientationValues[1]), "°"));
                    rollView.setText(String.format(getString(R.string.displayResult), Math.toDegrees(mOrientationValues[2]), "°"));
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected void onResume() {
        // Registers a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRelativeHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);

        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkBluetoothStatus();



            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                msg.setText("Scanning for new devices...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){


            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.println("Yes found");
                //bluetooth device found;
                msg.setText("Found devices");
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }


    @Override
    protected void onPause() {
        // Unregisters the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            connectionStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
//            Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
//            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
//            startActivity(i);
        }
    };

    private void checkBluetoothStatus() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!mBtAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                startDialog();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode != 0){
                // bluetooth enabled
                startDialog();

            } else{
                Toast.makeText(getBaseContext(), "You must enable bluetooth to transfer data", Toast.LENGTH_LONG).show();
            }
        }


    }

    private void  startDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(SensorActivity.this);
        alert.setView(R.layout.activity_dialogue);

        alert.setPositiveButton("Scan", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        final AlertDialog dialog = alert.create();


        dialog.show();
        msg = (TextView)dialog.findViewById(R.id.title_paired_devices);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPairedDevicesArrayAdapter.clear();
                mBtAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter();

                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                registerReceiver(mReceiver, filter);


            }
        });

        connectionStatus = (TextView) dialog.findViewById(R.id.connecting);
        connectionStatus.setText(" ");
        connectionStatus.setTextSize(40);

        //               Initialize array adapter for paired devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<>(dialog.getContext(), R.layout.activity_devices);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) dialog.findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
            // dialog.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
            msg.setText("Paired devices");
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
            // dialog.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable


        }
    }


}