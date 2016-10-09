package com.apt7.rxbluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * Created by Raviteja on 10/8/2016. RxBluetooth
 */

public class BluetoothObserver {
    private ObservableEmitter<Boolean> bluetoothStatusEmitter = null;
    private static BluetoothObserver bluetoothObserver;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothClassicHandler bluetoothClassicHandler;
    private BluetoothGatt mBluetoothGatt;
    private final String UUID_BLUETOOTH_CLASSIC = "00001101-0000-1000-8000-00805F9B34FB";
    private List<String> mConnectedDevices = new ArrayList<>();

    public static BluetoothObserver getInstance() {
        if (bluetoothObserver == null) {
            bluetoothObserver = new BluetoothObserver();
        }
        return bluetoothObserver;
    }

    private BluetoothObserver() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Observable<Boolean> startBluetooth(final Context context) {
        if (context == null) {
            return Observable.error(new NullPointerException("Context cant be null"));
        }
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                bluetoothStatusEmitter = e;
                if (!isBluetoothAvailable()) {
                    bluetoothStatusEmitter.onError(new Throwable("Bluetooth not found."));
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        enableBluetooth(context);
                    } else {
                        bluetoothStatusEmitter.onNext(true);
                    }
                }
            }
        });
    }

    private boolean isBluetoothAvailable() {
        return mBluetoothAdapter != null;
    }

    public void stopBluetooth() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.disable();
        }
    }

    private void enableBluetooth(@NonNull Context context) {
        Intent intent = new Intent(context, ShadowActivity.class);
        intent.putExtra("bluetooth", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    void onBluetoothStartCallback(boolean status) {
        if (bluetoothStatusEmitter != null) {
            bluetoothStatusEmitter.onNext(status);
            bluetoothStatusEmitter.onComplete();
        }
    }

    /**
     * Observes Bluetooth devices found while discovering.
     */
    public Observable<BluetoothDevice> observeDevices(final Context context) {
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        return Observable.create(new ObservableOnSubscribe<BluetoothDevice>() {
            @Override
            public void subscribe(final ObservableEmitter<BluetoothDevice> e) throws Exception {
                final BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            e.onNext(device);
                        }
                    }
                };
                context.registerReceiver(receiver, filter);
                e.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        context.unregisterReceiver(receiver);
                    }

                    @Override
                    public boolean isDisposed() {
                        return false;
                    }
                });
            }
        });
    }

    public Observable<String> observeConnectToClassic(final Context context, final BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return Observable.error(new NullPointerException("Bluetooth device cant be null"));
        }
        if (context == null) {
            return Observable.error(new NullPointerException("Context cant be null"));
        }
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> e) throws Exception {
                mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(UUID_BLUETOOTH_CLASSIC));
                if (mBluetoothSocket == null) {
                    e.onError(new NullPointerException("Bluetooth socket cant be null"));
                    return;
                }
                mBluetoothSocket.connect();
                bluetoothClassicHandler = BluetoothClassicHandler.getInstance();
                bluetoothClassicHandler.setBluetoothConnection(mBluetoothSocket);
                e.onNext(true);
            }
        }).flatMap(new Function<Boolean, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Boolean aBoolean) throws Exception {
                return bluetoothClassicHandler.read();
            }
        });
    }

    public Observable<String> observeDiscovery(final Context context) {
        if (context == null) {
            return Observable.error(new NullPointerException("Context cant be null"));
        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                final BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        e.onNext(intent.getAction());
                    }
                };
                context.registerReceiver(receiver, filter);
                e.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        context.unregisterReceiver(receiver);
                    }

                    @Override
                    public boolean isDisposed() {
                        return false;
                    }
                });
            }
        });
    }

    public Observable<Integer> observeBluetoothState(final Context context) {
        if (context == null) {
            return Observable.error(new NullPointerException("Context cant be null"));
        }
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                final BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        e.onNext(mBluetoothAdapter.getState());
                    }
                };
                context.registerReceiver(receiver, filter);
                e.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        context.unregisterReceiver(receiver);
                    }

                    @Override
                    public boolean isDisposed() {
                        return false;
                    }
                });
            }
        });
    }

    public void startBluetoothDiscovery() {
        if (isBluetoothAvailable() && !mBluetoothAdapter.isDiscovering()) {
            System.out.println("start discovering");
            mBluetoothAdapter.startDiscovery();
        }
    }

    public Observable<Boolean> observeConnectToBLE(final Context context, final BluetoothDevice bluetoothDevice
            , final BluetoothGattCallback mGattCallback) {
        if (bluetoothDevice == null) {
            return Observable.error(new NullPointerException("Bluetooth device cant be null"));
        }
        if (context == null) {
            return Observable.error(new NullPointerException("Context cant be null"));
        }
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> e) throws Exception {
                if (mConnectedDevices.contains(bluetoothDevice.getAddress())
                        && mBluetoothGatt != null) {
                    if (mBluetoothGatt.connect()) {
                        e.onNext(true);
                    } else {
                        e.onNext(false);
                    }
                }
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
                if (device == null) {
                    e.onError(new NullPointerException("Device not found."));
                } else {
                    mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
                    mConnectedDevices.add(bluetoothDevice.getAddress());
                    e.onNext(true);
                }
            }
        });
    }
}