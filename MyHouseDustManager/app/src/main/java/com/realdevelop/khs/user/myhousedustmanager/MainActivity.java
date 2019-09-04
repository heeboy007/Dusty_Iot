package com.realdevelop.khs.user.myhousedustmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    Button connect, refresh, reconfig;

    boolean is_connected = false;

    MqttAndroidClient mqttAndroidClient;
    MqttCallback callback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            info.setText("Connection Lost");
            is_connected = false;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            /*String str = new String(message.getPayload());
            dust = Float.parseFloat(str.split(",")[0]);
            celcius = Integer.parseInt(str.split(",")[1]);
            humid = Integer.parseInt(str.split(",")[2]);*/
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    float dust, celcius, humid;

    public static String serverUri = "tcp://mosquitto:k4BXSjCX48KyGYLT@61.101.2.103:1883";

    String clientId = "d:dusty:AndroidDevice:1c39476b8dd0";
    final String subscriptionTopic = "tester/myhome/dust";

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
        refresh = findViewById(R.id.btn_refresh);
        reconfig = findViewById(R.id.btn_ipconf);

        connect.setOnClickListener(listener);
        refresh.setOnClickListener(listener);
        reconfig.setOnClickListener(listener);
    }

    void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    info.setText("Sub Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    info.setText("Sub Failed");
                }
            });

            // THIS DOES WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String str = new String(message.getPayload());
                    dust = (float)Float.parseFloat(str.split(",")[0]);
                    celcius = (float)Float.parseFloat(str.split(",")[1]);
                    humid = (float)Float.parseFloat(str.split(",")[2]);
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
                        mqttAndroidClient.setCallback(callback);
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
                case R.id.btn_refresh:
                    if(is_connected){
                        updateDisplayers();
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
            updateDisplayers();
            sendEmptyMessage(0);
        }
    };

    PermissionListener perlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() { }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) { finish(); }
    };

    /*@Override
    protected void onCreateMenu(){

    }*/
}
