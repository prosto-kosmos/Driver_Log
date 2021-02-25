package com.example.driverlog;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GetDataService extends Service {
    public static final String TAG = GetDataService.class.getSimpleName();

    private ScheduledExecutorService mScheduledExecutorService;
    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;
    SharedPreferencesHelper mSharedPreferencesHelper;

    BluetoothAdapter btAdapter;
    BluetoothDevice device;
    UUID uuid;
    String deviceAddress;
    BluetoothSocket socket;
    HashMap<String, String> DataMap;
    HashSet<HashMap<String, String>> AllDataSet;
    Date currentDate;


    public GetDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate: ");
        mSharedPreferencesHelper = new SharedPreferencesHelper(this);
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mBuilder = getNotificationBuilder();
        DataMap = new HashMap<String, String>();
        AllDataSet = new HashSet<HashMap<String, String>>();
        currentDate = new Date();

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle("Получение данных с ELM 327").setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setContentIntent(resultPendingIntent);
        startForeground(1, getNotification("Подключение к ELM 327..."));


        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null) {
                        deviceAddress = mSharedPreferencesHelper.getAddress();
                        btAdapter = BluetoothAdapter.getDefaultAdapter();
                        if(!btAdapter.isEnabled()){
                            mManager.notify(1, getNotification("Bluetooth выключен"));
                            Log.i(TAG, "Bluetooth выключен");
                            return;
                        }
                        if (deviceAddress.equals("")){
                            Log.i(TAG, "Проверьте Bluetooth подключение к ELM 327");
                            mManager.notify(1, getNotification("Проверьте Bluetooth подключение к ELM 327"));
                            return;
                        }
                        device  = btAdapter.getRemoteDevice(deviceAddress);
                        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                        try {
                            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                            Log.d(TAG,"Сокет создан");
                        } catch (Exception e) {
                            Log.e(TAG,"Не удалось создать сокет");
                            mManager.notify(1, getNotification("Не удалось создать сокет"));
                            return;
                        }
                    }
                    if(!socket.isConnected()) {
                        try {
                            socket.connect();
                            Log.i(TAG, "Подключено к " + deviceAddress);
                        } catch (IOException e) {
                            Log.e(TAG, "Подключение к " + deviceAddress + " не удалось" + e);
                            try {
                                socket.connect();
                                Log.i(TAG, "Подключено к " + deviceAddress);
                            } catch (Exception e2) {
                                Log.e(TAG, "Повторное подключение к " + deviceAddress + " не удалось" + e2);
                                mManager.notify(1, getNotification("Устройство ELM 327 недоступно"));
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    mManager.notify(1, getNotification("Подключение не удалось"));
                    return;
                }

                try{
                    new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                    new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                    new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO)
                            .run(socket.getInputStream(), socket.getOutputStream());
                    Log.d(TAG, "OBDII адаптер инициализирован");
                } catch (Exception e) {
                    Log.e(TAG, "Не удалось инициализировать OBDII адаптер" + e.toString());
                    mManager.notify(1, getNotification("OBDII не найден"));
                    try {
                        socket.close();
                        socket = null;
                    } catch (Exception ex) {
                        Log.e(TAG, "Не удалось закрыть сокет");
                    }
                    return;
                }

                try {
                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateText = df.format(Calendar.getInstance().getTime());

                    SpeedCommand speedCommand = new SpeedCommand();
                    RPMCommand engineRpmCommand = new RPMCommand();
                    EngineCoolantTemperatureCommand engineCoolantTemperatureCommand = new EngineCoolantTemperatureCommand();
                    MassAirFlowCommand massAirFlowCommand = new MassAirFlowCommand();
                    ThrottlePositionCommand throttlePositionCommand = new ThrottlePositionCommand();

                    speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                    engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                    engineCoolantTemperatureCommand.run(socket.getInputStream(), socket.getOutputStream());
                    massAirFlowCommand.run(socket.getInputStream(), socket.getOutputStream());
                    throttlePositionCommand.run(socket.getInputStream(), socket.getOutputStream());
                    DataMap.clear();
                    DataMap.put("DateTime", dateText);
                    DataMap.put("Speed",speedCommand.getFormattedResult());
                    DataMap.put("Rpm",engineRpmCommand.getFormattedResult());
                    DataMap.put("CoolantTemp",engineCoolantTemperatureCommand.getFormattedResult());
                    DataMap.put("MassAirFlow",massAirFlowCommand.getFormattedResult());
                    String FormattedResult = throttlePositionCommand.getFormattedResult();
                    DataMap.put("ThrottlePosition",FormattedResult.substring(0, FormattedResult.length() - 1));
                    mSharedPreferencesHelper.addDataSet(DataMap);
                    Log.i(TAG, "Получены данные за " + dateText);
                    mManager.notify(1, getNotification("Идет получение. Получено: " + mSharedPreferencesHelper.getDataSet().size()));
                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "Не удалось получить данные" + e.toString());
                    return;
                }
            }
        }, 1000, (long) (mSharedPreferencesHelper.getPeriod() * 1000), TimeUnit.MILLISECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand: ");
        Toast.makeText(this, "Следите за статусом получения данных в панеле уведомлений", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy: ");
        try {
            mScheduledExecutorService.shutdownNow();
            socket.close();
            socket = null;
            Toast.makeText(this, "Получение данных приостановлено", Toast.LENGTH_SHORT).show();
            mManager.cancel(1);
        } catch (Exception e) {
            Log.e(TAG, "Не удалось закрыть сокет");
        }
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return new NotificationCompat.Builder(this);
        }
        else{
            String channel_id = "my_channel_id";
            if(mManager.getNotificationChannel(channel_id) == null){
                NotificationChannel channel = new NotificationChannel(channel_id, "Text_for_user", NotificationManager.IMPORTANCE_LOW);
                mManager.createNotificationChannel(channel);
            }

            return new NotificationCompat.Builder(this, channel_id);
        }
    };

    private Notification getNotification(String contentText){
        return mBuilder.setContentText(contentText).build();
    }
}
