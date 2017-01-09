package com.shenhua.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int SCAN_PERIOD = 20000;
    private boolean mScanning;
    private Handler mHandler;
    private List<BluetoothDevice> mPairedDevices;
    private List<BluetoothDevice> mScannedDevices;
    private MyBluetoothAdapter mPairedAdapter;
    private MyBluetoothAdapter mScannedAdapter;

    @BindView(R.id.sw_switch)
    Switch mSwitchSw;
    @BindView(R.id.layout_open)
    RelativeLayout mOpenLayout;
    @BindView(R.id.sw_open)
    Switch mOpenSw;

    @BindView(R.id.rv_paired)
    RecyclerView mPairedRv;
    @BindView(R.id.rv_scanned)
    RecyclerView mScannedRv;

    @BindView(R.id.btn_search)
    Button mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        updateView(mBluetoothAdapter == null || mBluetoothAdapter.isEnabled());

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (mPairedDevices == null)
                    mPairedDevices = new ArrayList<>();
                System.out.println("shenhua sout:已配对：" + device.getName());
                mPairedDevices.add(device);
            }
        }
        mPairedRv.setLayoutManager(new LinearLayoutManager(this));
        mPairedRv.setHasFixedSize(true);
        mScannedRv.setLayoutManager(new LinearLayoutManager(this));
        mScannedRv.setHasFixedSize(true);
        mPairedAdapter = new MyBluetoothAdapter(this, mPairedDevices);
        mPairedRv.setAdapter(mPairedAdapter);
        mScannedAdapter = new MyBluetoothAdapter(this, mScannedDevices);
        mScannedRv.setAdapter(mScannedAdapter);
    }

    private void updateView(boolean b) {
        if (mBluetoothAdapter == null) {
            return;
        }
        mSearchBtn.setEnabled(b);
        mSwitchSw.setChecked(b);
    }

    @OnClick({R.id.layout_switch, R.id.layout_open, R.id.btn_search})
    void clicks(View v) {
        switch (v.getId()) {
            case R.id.layout_switch:
                if (mBluetoothAdapter == null) {
                    Toast.makeText(this, "蓝牙设备问题", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mBluetoothAdapter.isEnabled()) {// 未打开
                    mBluetoothAdapter.enable();
                } else {
                    mBluetoothAdapter.disable();
                }
                break;
            case R.id.layout_open:
                setDiscoverableTimeOut(SCAN_PERIOD);
                break;
            case R.id.btn_search:
                if (mScannedDevices != null) {
                    mScannedDevices.clear();
                    mScannedAdapter.notifyDataSetChanged();
                }
                scanLeDevice(true);
                break;
        }
    }

    public void setDiscoverableTimeOut(int timeOut) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, timeOut);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.cancelDiscovery();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startDiscovery();
        } else {
            mScanning = false;
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    System.out.println("shenhua sout:扫描到设备-->" + device.getAddress()
                            + "  type:" + device.getType()
                            + "  name:" + device.getName()
                            + "  type::" + device.getBluetoothClass().getDeviceClass());
                    if (mScannedDevices == null)
                        mScannedDevices = new ArrayList<>();
                    mScannedDevices.add(device);
                    mScannedAdapter.setDatas(mScannedDevices);
                }
            }
            // When discovery is started
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                System.out.println("shenhua sout:" + "扫描开始");
                mSearchBtn.setText("扫描开始");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSearchBtn.setText("扫描中...");
                    }
                }, 500);
            }
            // When discovery is finished
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                System.out.println("shenhua sout:" + "扫描完毕");
                mSearchBtn.setText("扫描完毕");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSearchBtn.setText("搜索设备");
                    }
                }, 2000);
            }
            // When changed
            else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:// When open
                        updateView(true);
                        break;
                    case BluetoothAdapter.STATE_OFF:// When off
                        updateView(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:// When opening
                        // Toast.makeText(context, "opening", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:// When offing
                        // Toast.makeText(context, "closing", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
