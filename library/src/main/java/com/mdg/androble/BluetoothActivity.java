package com.mdg.androble;

/**
 * Created by this pc on 02-08-2016.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observer;


public abstract class BluetoothActivity extends AppCompatActivity {

    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private static final int Finished_Activity = 3;
    private static final int DISCOVERABLE_DURATION = 300;
    static BluetoothAdapter bluetoothAdapter;
    private boolean discoveryMode =false;
    private DeviceList deviceList;

    protected void enableBluetooth() {
        deviceList =new DeviceList();
        deviceList.addObserver((Observer)BluetoothManager.device_list);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            // Bluetooth successfully enabled!
            if (resultCode == Activity.RESULT_OK) {
                deviceList.call("bluetooth enabled");
                Toast.makeText(getApplicationContext(), "Bluetooth enabled." + "\n" + "Scanning for peers", Toast.LENGTH_SHORT).show();

                makeDiscoverable();
                discoverDevices();

            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == DISCOVERABLE_BT_REQUEST_CODE) {
            if (resultCode == DISCOVERABLE_DURATION) {
                discoveryMode =true;
                Toast.makeText(getApplicationContext(), "Your device is now discoverable", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Fail to enable discoverable mode.", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Finished_Activity) {
            bluetoothAdapter.disable();

        }
    }

    private String discoverDevices() {
        if (bluetoothAdapter.startDiscovery()) {
            return "Discovering peers";
        } else {
            return "BluetoothActivity failed to start.";
        }
    }

    protected void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.call(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
            }
        }
    };
}
