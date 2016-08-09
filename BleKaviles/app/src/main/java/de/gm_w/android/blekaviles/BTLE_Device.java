package de.gm_w.android.blekaviles;

import android.bluetooth.BluetoothDevice;

/**
 * Created by blue on 09/08/16.
 * This is a wrapper class for the Bluetooth device objects,
 * and restore the RSSI values of founded devices.
 */
public class BTLE_Device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BTLE_Device(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    public void setRSSI(int rssi) {
        this.rssi = rssi;
    }

    public int getRSSI() {
        return rssi;
    }
}
