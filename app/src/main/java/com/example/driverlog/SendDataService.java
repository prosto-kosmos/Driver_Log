package com.example.driverlog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SendDataService extends Service {
    public static final String TAG = SendDataService.class.getSimpleName();

    private ExecutorService mExecutorService;
    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;

    private SharedPreferencesHelper mSharedPreferencesHelper;
    private String ProtocolString;
    private String ServerAddressString;
    private String AllDataSetString;
    private String IdString;
    private String RequestString;

    public SendDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate: ");
        mSharedPreferencesHelper = new SharedPreferencesHelper(this);
        mExecutorService = Executors.newSingleThreadExecutor();
        mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mBuilder = getNotificationBuilder();

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle("Отправка данных на сервер").setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setContentIntent(resultPendingIntent);
        startForeground(2, getNotification("Начинается отправка..."));

        final Future<Integer> future = mExecutorService.submit(
                new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
//                        for (int i = 0; i < 5; i++){
//                            mBuilder.setProgress(5, i, false);
//                            mManager.notify(2, getNotification("Записей отправлено: " + i + " из 5"));
//                            Thread.sleep(2000);
//                        }

                        ProtocolString = "http://";
                        ServerAddressString = mSharedPreferencesHelper.getIP() + "/api/log/insert?";
                        IdString = "ID=" + mSharedPreferencesHelper.getId() + "&";

                        final int CountDataItem = mSharedPreferencesHelper.getDataSet().size();
                        final int[] CountSendItem = {0};

                        for (final HashMap<String, String> i : mSharedPreferencesHelper.getDataSet()){
                            Gson gson = new Gson();
                            AllDataSetString = "DATA=[" + gson.toJson(i) + "]";
                            RequestString = ProtocolString + ServerAddressString + IdString + AllDataSetString;
                            AsyncHttpPost asyncHttpPost = new AsyncHttpPost();
                            asyncHttpPost.setListener(new AsyncHttpPost.Listener() {
                                @Override
                                public void onResult(String result) {
                                    if (result.equals("ServerError")) {
                                        mBuilder.setProgress(CountDataItem, CountSendItem[0], false);
                                        mManager.notify(2, getNotification("Сбой подключения. Отправлено: " + CountSendItem[0] + " из " + CountDataItem));
                                        stopSelf();
                                        return;
                                    }
                                    if (result.equals("Error")) {
                                        mBuilder.setProgress(CountDataItem, CountSendItem[0], false);
                                        mManager.notify(2, getNotification( "Сбой на сервере. Отправлено: " + CountSendItem[0] + " из " + CountDataItem));
                                        stopSelf();
                                        return;
                                    }
                                    if (result.equals("Ok")) {
                                        CountSendItem[0] += 1;
                                        mSharedPreferencesHelper.delItemDataSet(i);
                                        mBuilder.setProgress(CountDataItem, CountSendItem[0], false);
                                        mManager.notify(2, getNotification("Записей отправлено: " + CountSendItem[0] + " из " + CountDataItem));
                                        if (CountDataItem == CountSendItem[0]) {
                                            mBuilder.setProgress(CountDataItem, CountSendItem[0], false);
                                            mManager.notify(2, getNotification("Отправка завершена. Отправлено: " + CountDataItem));
                                            stopSelf();
                                        }
                                    }
                                }
                            });
                            asyncHttpPost.execute(RequestString);
                        }
                        return null;
                    }
                }
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand: ");
        Toast.makeText(this, "Следите за статусом отправки данных в панеле уведомлений", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy: ");
        mExecutorService.shutdownNow();
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return new NotificationCompat.Builder(this);
        }
        else{
            String channel_id = "my_channel_id_1";
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
