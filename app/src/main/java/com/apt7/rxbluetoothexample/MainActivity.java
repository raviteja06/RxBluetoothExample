package com.apt7.rxbluetoothexample;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.apt7.rxbluetooth.BluetoothObserver;
import com.apt7.rxpermissions.Permission;
import com.apt7.rxpermissions.PermissionObservable;

import io.reactivex.observers.DisposableObserver;

public class MainActivity extends AppCompatActivity {
    DisposableObserver<BluetoothDevice> disposableObserver;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposableObserver != null) {
            disposableObserver.dispose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionObservable.getInstance().checkThePermissionStatus(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new DisposableObserver<Permission>() {
                    @Override
                    public void onNext(Permission permission) {
                        if (permission.getName().equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            if (permission.getGranted() == Permission.PERMISSION_GRANTED) {
                                startBluetooth();
                            } else {
                                getPermission();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        dispose();
                    }
                });
    }

    private void getPermission() {
        PermissionObservable.getInstance().request(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new DisposableObserver<Permission>() {

                    @Override
                    public void onNext(Permission permission) {
                        if (permission.getName().equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            if (permission.getGranted() == Permission.PERMISSION_GRANTED) {
                                startBluetooth();
                            } else {
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        dispose();
                    }
                });
    }

    private void startBluetooth() {
        BluetoothObserver.getInstance().startBluetooth(this).subscribe(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean value) {
                if (value) {
                    getBluetoothDevices();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private void getBluetoothDevices() {
        disposableObserver = new DisposableObserver<BluetoothDevice>() {
            @Override
            public void onNext(BluetoothDevice bluetoothDevice) {
                startClassic(bluetoothDevice);
                dispose();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        BluetoothObserver.getInstance().observeDevices(this).subscribe(disposableObserver);
        BluetoothObserver.getInstance().observeDiscovery(this).subscribe(new DisposableObserver<String>() {
            @Override
            public void onNext(String value) {
                System.out.println(value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                dispose();
            }
        });
        BluetoothObserver.getInstance().startBluetoothDiscovery();
    }

    private void startClassic(BluetoothDevice bluetoothDevice) {
        BluetoothObserver.getInstance().observeConnectToBLE(MainActivity.this, bluetoothDevice, mBluetoothCallback)
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        System.out.println("Value : " + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    BluetoothGattCallback mBluetoothCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
}
