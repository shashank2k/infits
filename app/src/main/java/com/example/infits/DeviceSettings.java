package com.example.infits;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;
import com.polidea.rxandroidble3.RxBleClient;
import com.polidea.rxandroidble3.scan.ScanSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.rxjava3.disposables.Disposable;
import zhan.rippleview.RippleView;

public class DeviceSettings extends AppCompatActivity {
    SwitchCompat bluetoothSwitch;
    ImageView scan_for_device;
    RecyclerView bluetooth_list;
    Disposable scanSubscription;
    RxBleClient rxBleClient;
    ArrayList<String> deviceName = new ArrayList<>();
    ArrayList<String> deviceAddress = new ArrayList<>();
    RippleView rippleView;
    RipplePulseLayout mRipplePulseLayout;
    TextView number_of_devices, selected_title;
    Button connect_div;
    GetMacInterface getMacInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            Toast.makeText(this, "yes", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "no", Toast.LENGTH_LONG).show();
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothSwitch = findViewById(R.id.bluetoothSwitch);
        scan_for_device = findViewById(R.id.scan_for_device);
        bluetooth_list = findViewById(R.id.bluetooth_list);
        rxBleClient = RxBleClient.create(getApplicationContext());
        number_of_devices = findViewById(R.id.number_of_devices);
        selected_title = findViewById(R.id.select_title);
        connect_div = findViewById(R.id.connect_div);
        bluetooth_list.setAdapter(new DeviceListAdapter(getApplicationContext(), deviceName, deviceAddress, getMacInterface));
//        rippleView  = findViewById(R.id.ripple);
//        rippleView.startRipple();
        mRipplePulseLayout = findViewById(R.id.layout_ripplepulse);
        if (mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplication(), "Bluetooth is ON", Toast.LENGTH_LONG).show();
            bluetoothSwitch.setChecked(true);
        }
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    Log.d("DS", "checked");
                    if (ActivityCompat.checkSelfPermission(DeviceSettings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("DS", "no permission");
                        checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                        return;
                    }
                    startActivityForResult(enableBtIntent, 1);
                } else {
                    mBluetoothAdapter.disable();
                }
            }
        });

        scan_for_device.setOnClickListener(v -> {
            connect_div.setVisibility(View.GONE);
            number_of_devices.setVisibility(View.VISIBLE);
            number_of_devices.setText("Searching for devices...");
            selected_title.setText("It is going to take only few seconds");
            deviceName.removeAll(deviceName);
            deviceAddress.removeAll(deviceAddress);
            bluetooth_list.setAdapter(null);
            scanDiv();
            scan_for_device.setVisibility(View.GONE);
            mRipplePulseLayout.startRippleAnimation();
        });
        getMacInterface = new GetMacInterface() {
            @Override
            public void getMac(String mac) {
                DataFromDatabase.macAddress = mac;
                System.out.println(mac);
            }
        };
        connect_div.setOnClickListener(v -> {
//            getMacInterface = new GetMacInterface() {
//                @Override
//                public void getMac(String mac) {
//                    DataFromDatabase.macAddress = mac;
//                    System.out.println(mac);
//                }
//            };
        });
    }

    void scanDiv() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanSubscription.dispose();
                scan_for_device.setVisibility(View.VISIBLE);
                connect_div.setVisibility(View.VISIBLE);
                if (deviceName.size() == 1) {
                    number_of_devices.setText("Found 1 device");
                    selected_title.setText("Select device and click connect button");
                } else if (deviceName.size() == 0) {
                    number_of_devices.setText("No device Found");
                    selected_title.setText("Click search for devices button to refresh");
                } else {
                    number_of_devices.setText(String.format("Found %d device", deviceAddress.size()));
                    selected_title.setText("Select device and click connect button");
                }
                bluetooth_list.setAdapter(new DeviceListAdapter(getApplicationContext(), deviceName, deviceAddress, getMacInterface));
                bluetooth_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRipplePulseLayout.stopRippleAnimation();
            }
        }, 10000);
        scanSubscription = rxBleClient.scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                                .build()
                )
                .subscribe(
                        scanResult -> {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            if (scanResult.getBleDevice().getBluetoothDevice().getName() != null) {
                                deviceName.add(scanResult.getBleDevice().getBluetoothDevice().getName());
                                deviceAddress.add(scanResult.getBleDevice().getMacAddress());
                                Set<String> s = new HashSet<String>();

                                for (String name : deviceName) {
                                    if (!s.add(name)) {
                                        System.out.println(name + "is duplicated");
                                        deviceName.remove(scanResult.getBleDevice().getBluetoothDevice().getName());
                                        deviceAddress.remove(scanResult.getBleDevice().getMacAddress());
                                    }
                                }
                            }
                                },
                                throwable -> {
                                    // Handle an error here.
                                    System.out.println(throwable);
                                }
                        );
    }

    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(DeviceSettings.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DeviceSettings.this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, requestCode);
        }
        else {
            Toast.makeText(DeviceSettings.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                Toast.makeText(DeviceSettings.this, "Bluetooth Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(DeviceSettings.this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}