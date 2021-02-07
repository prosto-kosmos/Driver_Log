package com.example.driverlog;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

// AsyncTask<тип входного аргумента, тип прогресса. тип возвращаемого значения>
public class AsyncHttpPost extends AsyncTask<String, Void, String> {
    public static final String TAG = AsyncHttpPost.class.getSimpleName();

    private Listener mListener;
    interface Listener {
        void onResult(String result);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public AsyncHttpPost() {
        //todo
    }

    @Override
    protected void onPreExecute() {// выполнится до doInBackground
        Log.i(TAG, "onPreExecute: ");
    }

    @Override
    protected String doInBackground(String... strings) {// выполнится в фоновом потоке
        Log.i(TAG, "doInBackground: request - " + strings[0]);
        try {
            return doGet(strings[0]);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка подключения: " +e.toString());
            return "ServerError";
        }
    }

    @Override
    protected void onPostExecute(String result) { //выполнится после doInBackground
        Log.i(TAG, "onPostExecute: " + result);
        if (mListener != null) {
            mListener.onResult(result);
        }
    }

    public static String doGet(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0" );
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setRequestProperty("Content-Type", "application/json");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        bufferedReader.close();

        Log.i(TAG,"Response string: " + response.toString());
        return response.toString();
    }
}