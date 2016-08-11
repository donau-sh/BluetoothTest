package de.gm_w.android.blekaviles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

/**
 * Created by blue on 10/08/16.
 */
public class Scanner_BTLE {

    private MainActivity mActivity;
    private BluetoothAdapter mBluethAdapter;
    private boolean mScanning;  // scanning state
    private Handler mHandler;
    private long scanPeriod;
    private int signalStrength;

    public Scanner_BTLE(MainActivity mainActivity, long scanPeriod) {
        mActivity = mainActivity;

        mHandler = new Handler();
        this.scanPeriod = scanPeriod;

        final BluetoothManager bluetoothManager =
                (BluetoothManager)mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluethAdapter = bluetoothManager.getAdapter();
    }

    public Scanner_BTLE(MainActivity mainActivity, long scanPeriod, int signalStrength) {
        mActivity = mainActivity;

        mHandler = new Handler();
        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager)mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluethAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScanning() {
        return mScanning;
    }

    public void start() {
        if(!Utils.checkBluetooth(mBluethAdapter)) {
            Utils.requestUserBluetooth(mActivity);
            mActivity.stopScan();
        } else {
            scanLeDevice(true);
        }
    }

    public void stop() {
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {

        if (enable && !mScanning) {
            Utils.toast(mActivity.getApplicationContext(), "Starting BLE Scan...");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.toast(mActivity.getApplicationContext(), "Stopping BLE Scan...");
                    mScanning = false;
                    mBluethAdapter.stopLeScan(mLeScanCallback);
                    mActivity.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mBluethAdapter.startLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    final int new_rssi = rssi;
                    if(rssi > signalStrength) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.addDevice(device, new_rssi);
                            }
                        });
                    }
                }
            };
}
