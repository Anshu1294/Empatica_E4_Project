package com.empatica.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.*;



public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate, TabHost.OnTabChangeListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long STREAMING_TIME = 100000; // Stops streaming 10 seconds after connection

    private static final String EMPATICA_API_KEY = "1482f602113740c0aac7310e724e3a92";  // TODO insert your API Key here

    private EmpaDeviceManager deviceManager;

    private TextView accel_xLabel;
    private TextView accel_yLabel;
    private TextView accel_zLabel;
    private TextView bvpLabel;
    private TextView bvpfilter;
    private TextView edaLabel;
    private TextView ibiLabel;
    private TextView ibifiltered;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private RelativeLayout dataCnt;
    private TabHost tabHost;
    private RelativeLayout LayOutGraph_EDA;
    private RelativeLayout LayOutGraph_BVP;
    private RelativeLayout LayoutGraph_HR;
    private RelativeLayout LayoutGraph_IBI;
    private RelativeLayout LayOutGraph_BVP_Raw;
    private LineChart mChart1;
    private LineChart mChart2;
    private LineChart mChart3;
    private LineChart mChart4;
    private LineChart mChart5;
    private float ibiData=0;
    private float hrData =0;
    private boolean ToastNow = false;
    private int count = 0;
    private int ibi_counter = 0;
    private float bvp_total = 0;
    private float bvp_filtered = 0;
    private float[] filtered_array = new float[20];
    private double[] filteredtimestamp_array = new double[20];
    public String[] session_list;
    public Workbook wb = new HSSFWorkbook();
    private float calculated_ibi = 0;



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
        bvpfilter = (TextView) findViewById(R.id.bvpfiltered);
        ibifiltered = (TextView) findViewById(R.id.ibi_filtered);
        edaLabel = (TextView) findViewById(R.id.eda);
        ibiLabel = (TextView) findViewById(R.id.ibi);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        tabHost = (TabHost) findViewById(R.id.tabMain);

        LayOutGraph_BVP = (RelativeLayout) findViewById(R.id.GraphLayout_BVP);
        LayOutGraph_EDA = (RelativeLayout) findViewById(R.id.GraphLayout_EDA);
        LayoutGraph_HR = (RelativeLayout) findViewById(R.id.GraphLayout_HeartRate);
        LayoutGraph_IBI = (RelativeLayout) findViewById(R.id.GraphLayout_IBI);
        LayOutGraph_BVP_Raw = (RelativeLayout) findViewById(R.id.GraphLayout_BVP_Raw);

        mChart1 = new LineChart(this);
        mChart2 = new LineChart(this);
        mChart3 = new LineChart(this);
        mChart4 = new LineChart(this);
        mChart5 = new LineChart(this);


        mChart1.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mChart2.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mChart3.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mChart4.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mChart5.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
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
        x1_G1.setDrawGridLines(true);
        x1_G1.setAvoidFirstLastClipping(true);

        YAxis y1_G1 = mChart1.getAxisLeft();
        y1_G1.setTextColor(Color.BLACK);
        y1_G1.setDrawGridLines(true);
        y1_G1.setAxisMaxValue(150);
        y1_G1.setAxisMinValue(-150);

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
        x1_G2.setDrawGridLines(true);
        x1_G2.setAvoidFirstLastClipping(true);

        YAxis y1_G2 = mChart2.getAxisLeft();
        y1_G2.setTextColor(Color.BLACK);
        y1_G2.setDrawGridLines(true);
        y1_G2.setAxisMaxValue(5);
        y1_G2.setAxisMinValue(-5);

        YAxis y1_2_G2 = mChart2.getAxisRight();
        y1_2_G2.setEnabled(false);
        //endregion
        //region Setup Graph - mChart3 (HR)
        LayoutGraph_HR.addView(mChart3);
        mChart3.setDescription("HR Chart");
        mChart3.setNoDataTextDescription("No Calculations at the moment");
        mChart3.setHighlightPerTapEnabled(true);
        mChart3.setTouchEnabled(true);
        mChart3.setDragEnabled(true);
        mChart3.setScaleEnabled(true);
        mChart3.setDrawGridBackground(true);

        //enable pinch zoom to avoid scaling axis
        mChart3.setPinchZoom(true);
        //colour background
        mChart3.setBackgroundColor(Color.GRAY);

        LineData data_G3 = new LineData();
        data_G3.setValueTextColor(Color.WHITE);
        mChart3.setData(data_G3);

        Legend L1_G3 = mChart3.getLegend();
        L1_G3.setForm(Legend.LegendForm.LINE);
        L1_G3.setTextColor(Color.BLACK);

        XAxis x1_G3 = mChart3.getXAxis();
        x1_G3.setTextColor(Color.BLACK);
        x1_G3.setDrawGridLines(true);
        x1_G3.setAvoidFirstLastClipping(true);

        YAxis y1_G3 = mChart3.getAxisLeft();
        y1_G3.setTextColor(Color.BLACK);
        y1_G3.setDrawGridLines(true);
        y1_G3.setAxisMaxValue(140);
        y1_G3.setAxisMinValue(60);

        YAxis y1_2_G3 = mChart3.getAxisRight();
        y1_2_G3.setEnabled(false);
        //endregion -
        //region Setup Graph - mChart4 (IBI)
        LayoutGraph_IBI.addView(mChart4);
        mChart4.setDescription("IBI Chart");
        mChart4.setNoDataTextDescription("No Calculations at the moment");
        mChart4.setHighlightPerTapEnabled(true);
        mChart4.setTouchEnabled(true);
        mChart4.setDragEnabled(true);
        mChart4.setScaleEnabled(true);
        mChart4.setDrawGridBackground(true);

        //enable pinch zoom to avoid scaling axis
        mChart4.setPinchZoom(true);
        //colour background
        mChart4.setBackgroundColor(Color.GRAY);

        LineData data_G4 = new LineData();
        data_G4.setValueTextColor(Color.WHITE);
        mChart4.setData(data_G4);

        Legend L1_G4 = mChart4.getLegend();
        L1_G4.setForm(Legend.LegendForm.LINE);
        L1_G4.setTextColor(Color.BLACK);

        XAxis x1_G4 = mChart4.getXAxis();
        x1_G4.setTextColor(Color.BLACK);
        x1_G4.setDrawGridLines(true);
        x1_G4.setAvoidFirstLastClipping(true);

        YAxis y1_G4 = mChart4.getAxisLeft();
        y1_G4.setTextColor(Color.BLACK);
        y1_G4.setDrawGridLines(true);
        y1_G4.setAxisMaxValue(2);
        y1_G4.setAxisMinValue(0);

        YAxis y1_2_G4 = mChart4.getAxisRight();
        y1_2_G4.setEnabled(false);
        //endregion
        //region Setup Graph - mChart5 (BVP_RAW)
        LayOutGraph_BVP_Raw.addView(mChart5);
        mChart5.setDescription("BVP Raw Chart");
        mChart5.setNoDataTextDescription("No Calculations at the moment");
        mChart5.setHighlightPerTapEnabled(true);
        mChart5.setTouchEnabled(true);
        mChart5.setDragEnabled(true);
        mChart5.setScaleEnabled(true);
        mChart5.setDrawGridBackground(true);

        //enable pinch zoom to avoid scaling axis
        mChart5.setPinchZoom(true);
        //colour background
        mChart5.setBackgroundColor(Color.GRAY);

        LineData data_G5 = new LineData();
        data_G5.setValueTextColor(Color.WHITE);
        mChart5.setData(data_G5);

        Legend L1_G5 = mChart5.getLegend();
        L1_G5.setForm(Legend.LegendForm.LINE);
        L1_G5.setTextColor(Color.BLACK);

        XAxis x1_G5 = mChart5.getXAxis();
        x1_G5.setTextColor(Color.BLACK);
        x1_G5.setDrawGridLines(true);
        x1_G5.setAvoidFirstLastClipping(true);

        YAxis y1_G5 = mChart5.getAxisLeft();
        y1_G5.setTextColor(Color.BLACK);
        y1_G5.setDrawGridLines(true);
        y1_G5.setAxisMaxValue(120);
        y1_G5.setAxisMinValue(-120);

        YAxis y1_2_G5 = mChart5.getAxisRight();
        y1_2_G5.setEnabled(false);
        //endregion

        //region Initialize Tabs
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Data");
        tabSpec.setContent(R.id.tabdata);
        tabSpec.setIndicator("Data");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("BVP Raw");
        tabSpec.setContent(R.id.tabBVPRaw);
        tabSpec.setIndicator("BVP Raw");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("BVP Filtered");
        tabSpec.setContent(R.id.tabGraph);
        tabSpec.setIndicator("BVP Filtered");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("EDA Graph");
        tabSpec.setContent(R.id.tabGraph2);
        tabSpec.setIndicator("EDA Graph");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Heart Rate");
        tabSpec.setContent(R.id.tabHeartRate);
        tabSpec.setIndicator("Heart Rate");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("IBI");
        tabSpec.setContent(R.id.tabIBI);
        tabSpec.setIndicator("IBI");
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


    public void saveSession(int n) throws IOException{
        if (n==0) {

            String folder_main = "CheckUp";

            File dir = new File(Environment.getExternalStorageDirectory(), folder_main);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (dir.list().length != 0){
                File myFile = new File(dir, "Session" + String.valueOf(dir.list().length+1)+".xls");
                FileOutputStream fileOut = new FileOutputStream(myFile);
                wb.write(fileOut);
                fileOut.close();
            }else{
                File myFile = new File(dir, "Session1.xls");
                FileOutputStream fileOut = new FileOutputStream(myFile);
                wb.write(fileOut);
                fileOut.close();
            }

        }
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
            try{
                saveSession(0);
            }catch (IOException ex){
                return;
            }

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
        UpdateHRGraph(bvp,timestamp);
        filteredBVP(bvp,timestamp);

        //region Set Chart 1 View
        LineData data_G5 = mChart5.getData();
        if (data_G5 != null)
        {
            LineDataSet set_g5 = (LineDataSet) data_G5.getDataSetByIndex(0);

            if(set_g5 == null) {
                // creation of data set if there is not data
                set_g5 = CreateBVPSet();
                data_G5.addDataSet(set_g5);
            }
            // adding x value to the data set
            data_G5.addXValue("");
            //adding new x value to the data set
            data_G5.addEntry(new Entry(bvp,set_g5.getEntryCount()),0);
            //notify chart data has changed
            mChart5.notifyDataSetChanged();
            mChart5.setVisibleXRange(1, 100);
            mChart5.moveViewToX(data_G5.getXValCount() - 5);
        }
        //endregion
    }

    public void filteredBVP(float bvp, double timestamp) {
        if (count < 10) {
            bvp_total = bvp + bvp_total;
            count+=1;
        } else {
            bvp_filtered = (bvp_total/10);
            double timestamp2 = System.currentTimeMillis() / 1000;
            filteredIBI(bvp_filtered, timestamp2);
            updateLabel(bvpfilter, "" + bvp_filtered);

            LineData data_G1 = mChart1.getData();
            if (data_G1 != null)
            {
                LineDataSet set_g1 = (LineDataSet) data_G1.getDataSetByIndex(0);

                if(set_g1 == null) {
                    // creation of data set if there is not data
                    set_g1 = CreateBVPFilteredSet();
                    data_G1.addDataSet(set_g1);
                }
                // adding x value to the data set
                data_G1.addXValue("");
                //adding new x value to the data set
                data_G1.addEntry(new Entry(bvp_filtered,set_g1.getEntryCount()),0);
                //notify chart data has changed
                mChart1.notifyDataSetChanged();
                mChart1.setVisibleXRange(1, 25);
                mChart1.moveViewToX(data_G1.getXValCount() - 5);
            }
            count = 0;
        }
    }

    public void filteredIBI(float bvp, double timestamp) {
        filtered_array[ibi_counter] = bvp;
        filteredtimestamp_array[ibi_counter] = timestamp;
        double temp_ibi = 0;
        int indexMax1 = 0;   float max1=0;
        int indexMax2 = 0;   float max2=0;
        if (ibi_counter >= 19) {
            //for case 1 and 2, where the array end maxes
            for(int i = 0; i <=filtered_array.length-1;i++) {
                if(filtered_array[i] > max1) {
                    max1 = filtered_array[i];
                    indexMax1 = i;
                }
            }
            //case 1
            if(indexMax1 == 0)
            {
                int visited_1 =0;
                for(int j=1; j <= filtered_array.length-1; j++) {
                    if(j!=filtered_array.length-1) {
                        for(int k=1; k < filtered_array.length-2; k++){
                            if((filtered_array[k] > filtered_array[k-1]) && (filtered_array[k] > filtered_array[k+1])) {
                                max2 = filtered_array[k];
                                indexMax2 = k;
                                visited_1 ++;
                                break;
                            }
                        }
                    }
                    if(visited_1 > 0)
                        break;
                    if (j == filtered_array.length-1) {
                        visited_1 = 0;
                        max2 = filtered_array[j];
                        indexMax2 = j;
                        for(int k=1; k <= filtered_array.length-2; k++)
                        {
                            if(filtered_array[k] > filtered_array[j]) {
                                max2 = filtered_array[k];
                                indexMax2 = k;
                                visited_1 ++;
                                break;
                            }
                        }
                    }
                    if(visited_1 == 0){
                        break;
                    }
                }

            }
            //case 2
            if(indexMax1 == (filtered_array.length-1))
            {
                int visited_2 = 0;
                for(int j=0; j <= filtered_array.length-2; j++)
                {
                    if(j == 0) {
                        indexMax2 = j;
                        max2 = filtered_array[j];
                        for(int k=1; k <= filtered_array.length-2; k++)
                        {
                            if(filtered_array[k] > filtered_array[j]) {
                                max2 = filtered_array[j];
                                indexMax2 = j;
                                visited_2 ++;
                                break;
                            }
                        }
                    }
                    if(visited_2 == 0)
                        break;
                    if(j>0)
                    {
                        visited_2 = 0;
                        for(int k=1; k <= filtered_array.length-2; k++){
                            if((filtered_array[k] > filtered_array[k-1]) && (filtered_array[k] > filtered_array[k+1])) {
                                max2 = filtered_array[k];
                                indexMax2 = k;
                                visited_2 ++;
                                break;
                            }
                        }
                    }
                    if(visited_2 > 0) {
                        break;
                    }
                }
            }
            //case 3
            if(indexMax1 != 0 && indexMax1 != filtered_array.length-1) {
                int visited = 0;
                for (int k=0; k <= filtered_array.length-1;k++) {
                    if (k==0) {
                        max2 = filtered_array[k];
                        indexMax2 = k;
                        for(int l=1; l <= filtered_array.length-1; l++)
                        {
                            if(filtered_array[l] > filtered_array[k]) {
                                max2 = filtered_array[l];
                                indexMax2 = l;
                                visited++;
                                break;
                            }
                        }
                    }
                    if(visited == 0)
                        break;
                    if(k>0)
                    {
                        visited = 0;
                        for(int l=1; l < filtered_array.length-2; l++){
                            if(l != indexMax1){
                                if((filtered_array[l] > filtered_array[l-1]) && (filtered_array[l] > filtered_array[l+1])) {
                                    max2 = filtered_array[l];
                                    indexMax2 = l;
                                    visited ++;
                                    break;
                                }
                            }
                        }
                    }
                    if(visited > 0)
                        break;
                    if (k==filtered_array.length-1) {
                        visited = 0;
                        for(int l=0; l < filtered_array.length-2; l++)
                        {
                            max2 = filtered_array[k];
                            indexMax2 = k;
                            if(filtered_array[l] > filtered_array[k]) {
                                max2 = filtered_array[l];
                                indexMax2 = l;
                                visited ++;
                                break;
                            }
                        }
                    }
                    if(visited == 0) {
                        break;
                    }

                }
            }
            if (indexMax1 > indexMax2) {
                temp_ibi = filteredtimestamp_array[indexMax1] - filteredtimestamp_array[indexMax2];
                calculated_ibi = (float)temp_ibi;
                /*if(calculated_ibi > 1.2 || calculated_ibi < 0.6) {
                    double random = Math.random() * 1.1 + 0.7;
                    calculated_ibi = (float)random;
                }*/
                updateLabel(ibifiltered, "" + String.format("%.2f", calculated_ibi));
            } else {
                temp_ibi = filteredtimestamp_array[indexMax2] - filteredtimestamp_array[indexMax1];
                calculated_ibi = (float)temp_ibi;
                /*if(calculated_ibi > 1.2 || calculated_ibi < 0.6) {
                    double random = Math.random() * 1.1 + 0.7;
                    calculated_ibi = (float)random;
                }*/
                updateLabel(ibifiltered, "" + String.format("%.2f", calculated_ibi));
            }
            ibi_counter = 0;
        }
        ibi_counter ++;
    }
    //region CreateSets
    private LineDataSet CreateBVPSet()
    {
        LineDataSet BVPGraph = new LineDataSet(null,"BVP Value");
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
    private LineDataSet CreateBVPFilteredSet()
    {
        LineDataSet BVPGraph = new LineDataSet(null,"BVP Value");
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
        LineDataSet EDAGraph = new LineDataSet(null,"GSR Value");
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

    private LineDataSet CreateHRset()
    {
        LineDataSet HRgraph = new LineDataSet(null,"BPM");
        HRgraph.setDrawCubic(true); // sets drawing mode to cubic
        HRgraph.setCubicIntensity(0.2f);
        HRgraph.setAxisDependency(YAxis.AxisDependency.LEFT);
        HRgraph.setColor(ColorTemplate.getHoloBlue());//setting the line color theme to blue
        HRgraph.setCircleColor(ColorTemplate.getHoloBlue());// each plotting point color
        HRgraph.setLineWidth(1f);
        HRgraph.setFillAlpha(60);
        HRgraph.setFillColor(ColorTemplate.getHoloBlue());// fill of the line
        HRgraph.setHighLightColor(Color.rgb(244,177,177));
        HRgraph.setValueTextColor(Color.BLACK);
        HRgraph.setValueTextSize(7.5f);

        return HRgraph;
    }

    private LineDataSet CreateIBIset()
    {
        LineDataSet IBIgraph = new LineDataSet(null, "IBI");
        IBIgraph.setDrawCubic(true); //cubic drawing mode
        IBIgraph.setCubicIntensity(0.2f);
        IBIgraph.setAxisDependency(YAxis.AxisDependency.LEFT);
        IBIgraph.setColor(ColorTemplate.getHoloBlue());//setting the line color theme to blue
        IBIgraph.setCircleColor(ColorTemplate.getHoloBlue());// each plotting point color
        IBIgraph.setLineWidth(1f);
        IBIgraph.setFillAlpha(60);
        IBIgraph.setFillColor(ColorTemplate.getHoloBlue());// fill of the line
        IBIgraph.setHighLightColor(Color.rgb(244,177,177));
        IBIgraph.setValueTextColor(Color.BLACK);
        IBIgraph.setValueTextSize(7.5f);

        return IBIgraph;
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
                // creation of data set if there is not data
                set_g2 = CreateEDASet();
                data_G2.addDataSet(set_g2);
            }
            // adding x value to the data set
            data_G2.addXValue(String.valueOf(timestamp));
            //adding new x value to the data set
            data_G2.addEntry(new Entry(gsr,set_g2.getEntryCount()),0);
            //notify chart data has changed
            mChart2.notifyDataSetChanged();
            mChart2.setVisibleXRange(1,20);
            mChart2.moveViewToX(data_G2.getXValCount() - 5);
        }
        //endregion
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        updateLabel(ibiLabel, "" + ibi);
        ibiData = ibi;
        hrData = ((1/ibi)*60);

        LineData data_G4 = mChart4.getData();
        if (data_G4 != null)
        {
            LineDataSet set_g4 = (LineDataSet) data_G4.getDataSetByIndex(0);

            if(set_g4 == null) {
                // creation of data set if there is not data
                set_g4 = CreateIBIset();
                data_G4.addDataSet(set_g4);
            }
            // adding x value to the data set
            data_G4.addXValue(String.valueOf(timestamp));
            //adding new x value to the data set
            data_G4.addEntry(new Entry(ibi,set_g4.getEntryCount()),0);
            //notify chart data has changed
            mChart4.notifyDataSetChanged();
            mChart4.setVisibleXRange(1,20);
            mChart4.moveViewToX(data_G4.getXValCount() - 5);
        }
    }
    public void UpdateHRGraph(float rawBVPval, double timestamp)
    {

        // region set heart rate Values
        LineData data_G3 = mChart3.getData();
        if (data_G3 != null)
        {
            LineDataSet set_g3 = (LineDataSet) data_G3.getDataSetByIndex(0);
            if(set_g3 == null)
            {
                //creation of data set if there is no data
                set_g3 = CreateHRset();
                data_G3.addDataSet(set_g3);
            }
            //add xvalue
            if(hrData <=59)
            {
                data_G3.addXValue(String.valueOf(timestamp));
                data_G3.addEntry(new Entry(60, set_g3.getEntryCount()), 0);
                tabHost = (TabHost)findViewById(R.id.tabMain);
                if(tabHost.getCurrentTabTag() == "Heart Rate" && ToastNow == true)
                {
//                    Toast.makeText(getApplicationContext(), "HR is currently being Calculated", Toast.LENGTH_LONG).show();
//                    ToastNow = false;
                }
            }
            else {
                data_G3.addXValue(String.valueOf(timestamp));
                data_G3.addEntry(new Entry(hrData, set_g3.getEntryCount()), 0);
            }
            //notify chart has changed
            mChart3.notifyDataSetChanged();
            // range of numbers to be shown on the graph
            mChart3.setVisibleXRange(1,100);
            //keep scrolling in the x to the latest entry // why 5 though....
            mChart3.moveViewToX(data_G3.getXValCount()-5);
        }
        //endregion

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
        if(tabHost.getCurrentTabTag() == "Heart Rate")
        {
            ToastNow = true;
        }
        else
        {
            ToastNow = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.past_sessions) {
            startActivity (new Intent (this, PastSessions.class));
            return true;
        }

        if (id == R.id.hr_calc) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.pubnubheartrate");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
