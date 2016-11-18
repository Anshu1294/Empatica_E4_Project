package com.empatica.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate, TabHost.OnTabChangeListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long STREAMING_TIME = 100000; // Stops streaming 10 seconds after connection

    private static final String EMPATICA_API_KEY = "1482f602113740c0aac7310e724e3a92";  // TODO insert your API Key here

    private EmpaDeviceManager deviceManager;

    private TextView accel_xLabel;
    private TextView accel_yLabel;
    private TextView accel_zLabel;
    private TextView bvpLabel;
    private TextView edaLabel;
    private TextView ibiLabel;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private RelativeLayout dataCnt;
    private TabHost tabHost;
    private RelativeLayout LayOutGraph_BVP;
    private RelativeLayout LayOutGraph_EDA;
    private LineChart mChart1;
    private LineChart mChart2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize vars that reference UI components
        //region Declare UI Components
        statusLabel = (TextView) findViewById(R.id.status);
        dataCnt = (RelativeLayout) findViewById(R.id.dataArea);
        accel_xLabel = (TextView) findViewById(R.id.accel_x);
        accel_yLabel = (TextView) findViewById(R.id.accel_y);
        accel_zLabel = (TextView) findViewById(R.id.accel_z);
        bvpLabel = (TextView) findViewById(R.id.bvp);
        edaLabel = (TextView) findViewById(R.id.eda);
        ibiLabel = (TextView) findViewById(R.id.ibi);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        tabHost = (TabHost) findViewById(R.id.tabMain);
        LayOutGraph_BVP = (RelativeLayout) findViewById(R.id.GraphLayout_BVP);
        LayOutGraph_EDA = (RelativeLayout) findViewById(R.id.GraphLayout_EDA);
        mChart1 = new LineChart(this);
        mChart2 = new LineChart(this);

        mChart1.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mChart2.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        //endregion

        //region Setup Graph - mChart1 (BVP)
        LayOutGraph_BVP.addView(mChart1);
        mChart1.setDescription("BVP Chart");
        mChart1.setNoDataTextDescription("No Data at the moment");
        mChart1.setHighlightPerTapEnabled(true);
        mChart1.setTouchEnabled(true);
        mChart1.setDragEnabled(true);
        mChart1.setScaleEnabled(true);
        mChart1.setDrawGridBackground(true);

        //enable pinch zoom to avoid scaling axis
        mChart1.setPinchZoom(true);
        //colour background
        mChart1.setBackgroundColor(Color.GRAY);

        LineData data_G1 = new LineData();
        data_G1.setValueTextColor(Color.WHITE);
        mChart1.setData(data_G1);

        Legend L1_G1 = mChart1.getLegend();
        L1_G1.setForm(Legend.LegendForm.LINE);
        L1_G1.setTextColor(Color.BLACK);

        XAxis x1_G1 = mChart1.getXAxis();
        x1_G1.setTextColor(Color.BLACK);
        x1_G1.setDrawGridLines(false);
        x1_G1.setAvoidFirstLastClipping(true);

        YAxis y1_G1 = mChart1.getAxisLeft();
        y1_G1.setTextColor(Color.BLACK);
        y1_G1.setDrawGridLines(true);
        y1_G1.setAxisMaxValue(120f);
        y1_G1.setAxisMinValue(-120f);

        YAxis y1_2_G1 = mChart1.getAxisRight();
        y1_2_G1.setEnabled(false);

        //endregion

        //region Setup Graph - mChart2 (EDA)
        LayOutGraph_EDA.addView(mChart2);
        mChart2.setDescription("EDA Chart");
        mChart2.setNoDataTextDescription("No Data at the moment");
        mChart2.setHighlightPerTapEnabled(true);
        mChart2.setTouchEnabled(true);
        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        mChart2.setDrawGridBackground(true);

        //enable pinch zoom to avoid scaling axis
        mChart2.setPinchZoom(true);
        //colour background
        mChart2.setBackgroundColor(Color.GRAY);

        LineData data_G2 = new LineData();
        data_G2.setValueTextColor(Color.WHITE);
        mChart2.setData(data_G2);

        Legend L1_G2 = mChart2.getLegend();
        L1_G2.setForm(Legend.LegendForm.LINE);
        L1_G2.setTextColor(Color.BLACK);

        XAxis x1_G2 = mChart2.getXAxis();
        x1_G2.setTextColor(Color.BLACK);
        x1_G2.setDrawGridLines(false);
        x1_G2.setAvoidFirstLastClipping(true);

        YAxis y1_G2 = mChart2.getAxisLeft();
        y1_G2.setTextColor(Color.BLACK);
        y1_G2.setDrawGridLines(true);
        y1_G2.setAxisMaxValue(5f);
        y1_G2.setAxisMinValue(-5f);

        YAxis y1_2_G2 = mChart2.getAxisRight();
        y1_2_G2.setEnabled(false);
        //endregion

        //region Initialize Tabs
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Data");
        tabSpec.setContent(R.id.tabdata);
        tabSpec.setIndicator("Data");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("BVP Graph");
        tabSpec.setContent(R.id.tabGraph);
        tabSpec.setIndicator("BVP Graph");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("EDA Graph");
        tabSpec.setContent(R.id.tabGraph2);
        tabSpec.setIndicator("EDA Graph");
        tabHost.addTab(tabSpec);

        tabHost.setOnTabChangedListener(this);
        //endregion

        //region Initialize EmapDeviceManager
        // Create a new EmpaDeviceManager. MainActivity is both its data_G1 and status delegate.
        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        // Initialize the Device Manager using your API key. You need to have Internet access at this point.
        deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        //endregion
    }
    @Override
    protected void onPause() {
        super.onPause();
        deviceManager.stopScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.cleanUp();
    }

    @Override
    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        // No need to implement this right now
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
        // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
            // Stop streaming after STREAMING_TIME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataCnt.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Disconnect device
                            deviceManager.disconnect();
                        }
                    }, STREAMING_TIME);
                }
            });
        // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "");
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        updateLabel(bvpLabel, "" + bvp);

        //region Set Chart 1 View
        LineData data_G1 = mChart1.getData();
        if (data_G1 != null)
        {
            LineDataSet set_g1 = (LineDataSet) data_G1.getDataSetByIndex(0);

            if(set_g1 == null) {
                set_g1 = CreateBVPSet();
                data_G1.addDataSet(set_g1);
            }

            data_G1.addXValue("");
            data_G1.addEntry(new Entry(bvp,set_g1.getEntryCount()),0);
            //notify chart data has changed
            mChart1.notifyDataSetChanged();
            mChart1.setVisibleXRange(1,10);
            mChart1.moveViewToX(data_G1.getXValCount() - 5);


        }
        //endregion
    }

    //region CreateSets
    private LineDataSet CreateBVPSet()
    {
        LineDataSet BVPGraph = new LineDataSet(null,"something"); 
        BVPGraph.setDrawCubic(true);
        BVPGraph.setCubicIntensity(0.2f);
        BVPGraph.setAxisDependency(YAxis.AxisDependency.LEFT);
        BVPGraph.setColor(ColorTemplate.getHoloBlue());
        BVPGraph.setCircleColor(ColorTemplate.getHoloBlue());
        BVPGraph.setLineWidth(1f);
        BVPGraph.setFillAlpha(60);
        BVPGraph.setFillColor(ColorTemplate.getHoloBlue());
        BVPGraph.setHighLightColor(Color.rgb(244,177,177));
        BVPGraph.setValueTextColor(Color.BLACK);
        BVPGraph.setValueTextSize(7.5f);

        return BVPGraph;
    }

    private LineDataSet CreateEDASet()
    {
        LineDataSet EDAGraph = new LineDataSet(null,"something");
        EDAGraph.setDrawCubic(true);
        EDAGraph.setCubicIntensity(0.2f);
        EDAGraph.setAxisDependency(YAxis.AxisDependency.LEFT);
        EDAGraph.setColor(ColorTemplate.getHoloBlue());
        EDAGraph.setCircleColor(ColorTemplate.getHoloBlue());
        EDAGraph.setLineWidth(1f);
        EDAGraph.setFillAlpha(60);
        EDAGraph.setFillColor(ColorTemplate.getHoloBlue());
        EDAGraph.setHighLightColor(Color.rgb(244,177,177));
        EDAGraph.setValueTextColor(Color.BLACK);
        EDAGraph.setValueTextSize(7.5f);

        return EDAGraph;
    }
    //endregion


    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {

        updateLabel(edaLabel, "" + gsr);

        //region Set Chart 2 View
        LineData data_G2 = mChart2.getData();
        if (data_G2 != null)
        {
            LineDataSet set_g2 = (LineDataSet) data_G2.getDataSetByIndex(0);

            if(set_g2 == null) {
                set_g2 = CreateEDASet();
                data_G2.addDataSet(set_g2);
            }

            data_G2.addXValue("");
            data_G2.addEntry(new Entry(gsr,set_g2.getEntryCount()),0);
            //notify chart data has changed
            mChart2.notifyDataSetChanged();
            mChart2.setVisibleXRange(1,10);
            mChart2.moveViewToX(data_G2.getXValCount() - 5);


        }
        //endregion
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        updateLabel(ibiLabel, "" + ibi);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, "" + temp);
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }


    @Override
    public void onTabChanged(String s) {
        tabHost = (TabHost)findViewById(R.id.tabMain);
        Toast.makeText(getApplicationContext(), tabHost.getCurrentTabTag(), Toast.LENGTH_LONG).show();
    }
}
