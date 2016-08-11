package de.gm_w.android.blekaviles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener{

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BTLE_Device> mBTDeviceHashMap;
    private ArrayList<BTLE_Device> mBTDeviceArrayList;
    private ListAdapter_BTLE_Devices adapter;

    private Button btn_Scan;
    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        //mBTLeScanner = new Scanner_BTLE(this, 7500, -75);
        mBTLeScanner = new Scanner_BTLE(this, 10000);

        mBTDeviceHashMap = new HashMap<>();
        mBTDeviceArrayList = new ArrayList<>();

        adapter = new ListAdapter_BTLE_Devices(
                this, R.layout.btle_device_list_item, mBTDeviceArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        ((ScrollView)findViewById(R.id.scrollView)).addView(listView);

        btn_Scan = (Button)findViewById(R.id.btn_scan);
        findViewById(R.id.btn_scan).setOnClickListener(this);
    }

    @Override
    protected void onStart() {

        super.onStart();
        registerReceiver(mBTStateUpdateReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // make sure the request was successful
            if (resultCode == RESULT_OK) {
                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            } else {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }

    /**
     * Called when an item in the ListView is clicked.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * Called when the scan button is clicked.
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_scan:
                Utils.toast(getApplicationContext(), "Scan Button Pressed");

                if(!mBTLeScanner.isScanning()) {
                    startScan();
                } else {
                    stopScan();
                }

                break;
            default:
                break;
        }

    }

    public void addDevice(BluetoothDevice device, int new_rssi) {

        String address = device.getAddress();

        if(!mBTDeviceHashMap.containsKey(address)) {
            BTLE_Device btle_device = new BTLE_Device(device);
            btle_device.setRSSI(new_rssi);
            mBTDeviceHashMap.put(address, btle_device);
            mBTDeviceArrayList.add(btle_device);
        } else {
            mBTDeviceHashMap.get(address).setRSSI(new_rssi);
        }
        adapter.notifyDataSetChanged();

    }

    public void startScan() {
        btn_Scan.setText("Scanning...");

        mBTDeviceArrayList.clear();
        mBTDeviceHashMap.clear();

        adapter.notifyDataSetChanged();
        mBTLeScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Scan Again");

        mBTLeScanner.stop();
    }
}
