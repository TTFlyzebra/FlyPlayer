package com.jancar.usbmedia;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jancar.usbmedia.service.FlyMediaService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent scanService = new Intent(this, FlyMediaService.class);
        startService(scanService);

    }
}
