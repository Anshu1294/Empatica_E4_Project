package com.empatica.sample;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.ListView;


public class PastSessions extends AppCompatActivity {
    public String[] session_list;
    public String[] files1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_sessions);

        loadSessions();

    }

    public void loadSessions() {
        try{
            session_list = getAssets().list("Sessions");
        } catch (IOException ex){
            return;
        }

        String folder_main = "CheckUp";

        File dir = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        List<String> list = new ArrayList<String>();
        if (dir.isDirectory()) {
            files1 = dir.list();
            Pattern p = Pattern.compile("^(.*?)\\.xlsx$");
            for (String file : files1) {
                Matcher m = p.matcher(file);
                if (m.matches()) {
                    list.add(m.group(1));
                }
            }
        }

        ArrayList<String> stringList = new ArrayList<String>(Arrays.asList(files1));
        //instantiate custom adapter
        MyCustomAdapter adapter = new MyCustomAdapter(stringList, this);

        ListView lView = (ListView) findViewById(R.id.session_area);
        lView.setAdapter(adapter);


    }

}
