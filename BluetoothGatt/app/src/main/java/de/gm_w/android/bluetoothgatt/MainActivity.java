package de.gm_w.android.bluetoothgatt;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
//public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothGattActivity";

    private static final String DEVICE_NAME = "MiBand";

    /* Running Step */
    private static final UUID REALTIME_STEPS = UUID.fromString("0000ff06-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private TextView mSteps;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarIndeterminate(true);

        // display the results in text field
        mSteps = (TextView) findViewById(R.id.editTextSteps);

        // Bluetooth in Android 4.3 is accessed via the BluetoothManager,
        // rather than the old static BluetoothAdapter.getInstance()
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        // A progress dialog will be needed while the connection process is taking place
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It needs to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Bluetooth is disabled
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
            finish();
            return;
        }

        /**
         * Check for Bluetooth LE support.
         * In production, manifest entry will keep this from installing on these devices,
         * but this will allow test devices or other sideloads to report whether or not
         * the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        clearDisplayValues();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // make sure dialog is hidden
        mProgress.dismiss();
        // cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.main, menu);
        // add any device elements we've discovered to the overflow menu
        for(int i = 0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                // obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());

                // make a connection with the device using the special LE-specific
                // connectGatt() method, passing in a callback for GATT events
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                // display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to "+device.getName()+"..."));
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearDisplayValues() {
        mSteps.setText("---");
    }
}
