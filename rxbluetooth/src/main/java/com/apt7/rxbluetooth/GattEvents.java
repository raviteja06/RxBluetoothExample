package com.apt7.rxbluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Raviteja on 10/9/2016. RxBluetoothExample
 */

public interface GattEvents {
    void deviceConnectState(boolean isConnected);

    void onServicesDiscovered(BluetoothGatt gatt, boolean discovered);

    void characteristicReadState(BluetoothGattCharacteristic characteristic, boolean success);

    void characteristicWriteState(BluetoothGattCharacteristic characteristic, boolean success);

    void characteristicDataChange(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
}