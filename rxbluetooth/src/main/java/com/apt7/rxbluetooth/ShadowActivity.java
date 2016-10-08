package com.apt7.rxbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShadowActivity extends Activity {
    int REQUEST_ENABLE_BT = 38;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    // Read new intent when received.
    private void handleIntent(Intent intent) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally defined integer (which must be greater than 0), that the system passes back to you in your onActivityResult()
            // implementation as the requestCode parameter.
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            BluetoothObserver.getInstance().onBluetoothStartCallback(resultCode == RESULT_OK);
        }
        finish();
    }
}