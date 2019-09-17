package com.realdevelop.khs.user.myhousedustmanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    DonutProgress dustdisplay;
    TextView temp, hum, info;
    Button connect, disconnect, reconfig;
    String[] fetched_database_info;

    boolean is_connected = false, is_device_active = true;

    MqttAndroidClient mqttAndroidClient;
    MqttCallback callback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            info.setText("Connection Lost");
            Log.i("dust", cause.getMessage());
            //is_connected = false;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception { }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) { }
    };

    float dust, celcius, humid;

    public static String serverUri = "tcp://mosquitto:k4BXSjCX48KyGYLT@61.255.226.47:1883";

    String clientId = "d:dusty2:AndroidDevice:1c39476b8dd0";
    final String normalDataTransfer = "tester/myhome/dht";
    final String deviceDataTransfer = "tester/myhome/dev";
    final String requester = "tester/myhome/req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        dust = -1.0f;
        celcius = -1.0f;
        humid = -1.0f;

        dustdisplay = findViewById(R.id.donut_progress);
        dustdisplay.setMax(100);

        temp = findViewById(R.id.text_temp);
        hum = findViewById(R.id.text_hum);
        info = findViewById(R.id.text_info);

        connect = findViewById(R.id.btn_connect);
        disconnect = findViewById(R.id.btn_disconnect);
        reconfig = findViewById(R.id.btn_ipconf);

        connect.setOnClickListener(listener);
        disconnect.setOnClickListener(listener);
        reconfig.setOnClickListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.menu_stat:
                Intent intent = new Intent(getApplicationContext(), StatChooseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                if(fetched_database_info != null) {
                    intent.putExtra("db_name", fetched_database_info);
                }

                startActivity(intent);
                break;
            case R.id.menu_toggle_activity:
                if(is_connected) {
                    if (is_device_active) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Warning!")
                                .setMessage("Disabling the device will result in data loss while disabled. Continue?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        publishMessage("TurnOff", 2);
                                        is_device_active = false;
                                    }
                                })
                                .setNegativeButton("No", null)
                                .create();

                        alertDialog.show();
                    } else {
                        publishMessage("TurnOn", 2);
                        is_device_active = true;
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Not Connected Yet!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(normalDataTransfer, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    info.setText("Sub Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    info.setText("Nor Sub Failed");
                }
            });

            // THIS DOES WORK!
            mqttAndroidClient.subscribe(normalDataTransfer, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.i("dust", new String(message.getPayload()));
                    String str[] = new String(message.getPayload()).split(",");
                    dust = Float.parseFloat(str[0]);
                    celcius = Float.parseFloat(str[1]);
                    humid = Float.parseFloat(str[2]);

                    handler.sendEmptyMessage(0);
                }
            });

            mqttAndroidClient.subscribe(deviceDataTransfer, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    info.setText("Sub Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    info.setText("Dev Sub Failed");
                }
            });

            // THIS DOES WORK!
            mqttAndroidClient.subscribe(deviceDataTransfer, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }
            });

        } catch (MqttException ex){
            System.err.println("Exception while subscribing");
            ex.printStackTrace();
        }
    }

    private void setPermission(){
        TedPermission.with(MainActivity.this)
                .setDeniedMessage("Permissions are accquired to use this app.")
                .setPermissionListener(perlistener)
                .setPermissions(Manifest.permission.INTERNET,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WAKE_LOCK).check();
    }

    private void checkPermissions(){
        if(Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                setPermission();
            }

            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                setPermission();
            }

            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.ACCESS_NETWORK_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                setPermission();
            }

            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                setPermission();
            }

            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.WAKE_LOCK)
                    != PackageManager.PERMISSION_GRANTED) {
                setPermission();
            }
        }
    }

    void updateDisplayers(){
        if(dust > 0) {
            dustdisplay.setText(String.format("%.2f µg/m³", dust));
            dustdisplay.setProgress(dust);
            if (dust < 15) {
                dustdisplay.setTextColor(getResources().getColor(R.color.blue));
                dustdisplay.setFinishedStrokeColor(getResources().getColor(R.color.blue));
            } else if (dust < 35) {
                dustdisplay.setTextColor(getResources().getColor(R.color.green));
                dustdisplay.setFinishedStrokeColor(getResources().getColor(R.color.green));
            } else if (dust < 75) {
                dustdisplay.setTextColor(getResources().getColor(R.color.yellow));
                dustdisplay.setFinishedStrokeColor(getResources().getColor(R.color.yellow));
            } else {
                dustdisplay.setTextColor(getResources().getColor(R.color.red));
                dustdisplay.setFinishedStrokeColor(getResources().getColor(R.color.red));
            }
        }
        if(celcius > 0 && humid > 0) {
            temp.setText(String.format("%.2f C°", celcius));
            hum.setText(String.format("%.2f %%", humid));
        }
        if(is_device_active){
            info.setText("Status : Active");
        }
        else{
            info.setText("Status : Disabled");
        }
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.btn_connect:
                    try {
                        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                        mqttConnectOptions.setAutomaticReconnect(true);
                        mqttConnectOptions.setCleanSession(false);
                        mqttConnectOptions.setUserName("mosquitto");
                        mqttConnectOptions.setPassword("k4BXSjCX48KyGYLT".toCharArray());

                        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId, new MemoryPersistence());
                        //mqttAndroidClient.setCallback(callback);
                        Log.i("dust", "Settling...");
                        mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                                disconnectedBufferOptions.setBufferEnabled(true);
                                disconnectedBufferOptions.setBufferSize(100);
                                disconnectedBufferOptions.setPersistBuffer(false);
                                disconnectedBufferOptions.setDeleteOldestMessages(false);
                                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                                is_connected = true;
                                subscribeToTopic();
                                handler.sendEmptyMessage(0);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                info.setText("Connection Failed");
                                Log.i("dust", exception.getMessage());
                            }
                        });
                    } catch (MqttException ex){
                        Log.i("dust", ex.getMessage());
                    }
                    break;
                case R.id.btn_disconnect:
                    if(is_connected){
                        try {
                            is_connected = false;
                            mqttAndroidClient.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Not Connected Yet!", Toast.LENGTH_SHORT).show();
                    }
                break;
                case R.id.btn_ipconf:
                    new AddressDialog(MainActivity.this, serverUri).show();
                    break;
            }
        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //don't know why, but if you don't do like this then the whole thing won't work.
            updateDisplayers();
            //sendEmptyMessageDelayed(0, 500);
        }
    };

    public void publishMessage(String str, int qos){
        try {
            if(is_connected) {
                MqttMessage message = new MqttMessage();
                message.setPayload(str.getBytes());
                message.setQos(qos);
                mqttAndroidClient.publish(requester, message);
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    PermissionListener perlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() { }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) { finish(); }
    };

}
