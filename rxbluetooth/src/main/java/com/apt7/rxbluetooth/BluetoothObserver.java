package com.apt7.rxbluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

/**
 * Created by Raviteja on 10/8/2016. RxBluetooth
 */

public class BluetoothObserver {
    private ObservableEmitter<Boolean> bluetoothStatusEmitter = null;
    private static BluetoothObserver bluetoothObserver;
    private BluetoothAdapter mBluetoothAdapter;

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
                        System.out.println("Dispose");
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

    public Observable<String> observeDiscovery(final Context context) {
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
                        System.out.println("Dispose");
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
}