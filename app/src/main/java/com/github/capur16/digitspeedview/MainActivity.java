package com.github.capur16.digitspeedview;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class    MainActivity extends AppCompatActivity {
    static boolean status;
    LocationManager locationManager;
    static TextView digit_speed_view,distance,time;

    Button btnPause,btnStart,btnStop;
    static long StartTime,EndTime;
    static ProgressDialog progressDialog;
    static int p=0;

    LocationService myService;
    private Button renewalbtn;
    private Button statusbtn;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder)iBinder;
            myService= binder.getService();
            status=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status=false;
        }
    };


    @Override
    protected void onDestroy() {
        if (status==true)
            unbindService();
        super.onDestroy();
    }

    private void unbindService() {
        if (status==false)
            return;
        Intent i =new Intent(getApplicationContext(),LocationService.class);
        unbindService(sc);
        status=false;
    }

    @Override
    public void onBackPressed() {
        if (status==false)
            super.onBackPressed();
        else
            moveTaskToBack(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "DENIED", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digit_speed_view);
        renewalbtn = (Button) findViewById(R.id.renewalbtn);
        renewalbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openVerify();
            }
        });
        statusbtn = (Button) findViewById(R.id.statusbtn);
        statusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openStatus();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },1000);
        }
        distance = (TextView)findViewById(R.id.distance);
        time = (TextView)findViewById(R.id.time);

        btnPause = (Button)findViewById(R.id.btnPause);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnStop);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGPS();
                locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return;
                if(status == false)
                    bindService();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Getting Co-ordinates");
                progressDialog.show();

                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnPause.setText("PAUSE");
                btnStop.setVisibility(View.VISIBLE);
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPause.getText().toString().equalsIgnoreCase("pause")){
                    btnPause.setText("resume");
                    p=1;
                }
                else if (btnPause.getText().toString().equalsIgnoreCase("resume")){
                    checkGPS();
                    locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        return;

                    btnPause.setText("PAUSE");
                    p=0;
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status==true){
                    unbindService();
                }
                btnStart.setVisibility(View.VISIBLE);
                btnPause.setText("pause");
                btnPause.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
            }
        });
    }

    private void checkGPS() {
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlert();
    }

    private void showGPSDisabledAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {
                        Intent intent3 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent3);
                    }
                });
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i1) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void bindService() {
        if (status==true)
            return;
        Intent i = new Intent(getApplicationContext(),LocationService.class);
        bindService(i,sc,BIND_AUTO_CREATE);
        status=true;
        StartTime = System.currentTimeMillis();
    }

    public void openVerify() {
        Intent intent = new Intent(this,Main2Activity.class);
        startActivity(intent);
    }
    public void openStatus() {
        Intent intent2 = new Intent(this,Main3Activity.class);
        startActivity(intent2);
    }
}
