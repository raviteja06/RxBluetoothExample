package com.apt7.rxbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by Raviteja on 10/9/2016. RxBluetooth
 */

class BluetoothClassicHandler {
    private PublishSubject<String> publishSubject = PublishSubject.create();
    private static BluetoothClassicHandler bluetoothClassicHandler;
    private BluetoothSocket connectedBluetoothSocket;
    private InputStream connectedInputStream;
    private OutputStream connectedOutputStream;
    private Thread thread;

    static BluetoothClassicHandler getInstance() {
        if (bluetoothClassicHandler == null) {
            bluetoothClassicHandler = new BluetoothClassicHandler();
        }
        return bluetoothClassicHandler;
    }

    private BluetoothClassicHandler() {
    }

    void setBluetoothConnection(BluetoothSocket bluetoothSocket) {
        if (bluetoothSocket == null) {
            throw new NullPointerException("Bluetooth socket cant be null");
        }
        connectedBluetoothSocket = bluetoothSocket;
        try {
            connectedInputStream = bluetoothSocket.getInputStream();
            connectedOutputStream = bluetoothSocket.getOutputStream();
            setInputStreamObserver();
        } catch (IOException e) {
            connectedInputStream = null;
            connectedOutputStream = null;
            publishSubject.onError(e);
        }
    }

    private void setInputStreamObserver() {
        thread = new Thread() {

            @Override
            public void run() {
                boolean run = true;
                byte[] buffer = new byte[1024];
                int bytes;

                while (run) {
                    try {
                        bytes = connectedInputStream.read(buffer);
                        publishSubject.onNext(new String(buffer, 0, bytes));
                    } catch (IOException e) {
                        publishSubject.onError(e);
                        run = false;
                    }
                }
            }
        };
    }

    void write(String data) {
        if (bluetoothClassicHandler == null) {
            throw new NullPointerException("Handler cant be null");
        }
        if (connectedOutputStream == null) {
            throw new NullPointerException("Output stream is null");
        }
        try {
            connectedOutputStream.write(data.getBytes());
        } catch (IOException e) {
            publishSubject.onError(e);
        }
    }

    PublishSubject<String> read(){
        return publishSubject;
    }

    void destroy(){
        thread.interrupt();
    }
}
