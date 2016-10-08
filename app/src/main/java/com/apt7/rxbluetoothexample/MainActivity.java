package com.apt7.rxbluetoothexample;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.apt7.rxbluetooth.BluetoothObserver;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subscribers.DisposableSubscriber;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_GRANTED:
                    startBluetooth();
                    break;
            }
        }
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
                System.out.println(bluetoothDevice.getName());
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
}
