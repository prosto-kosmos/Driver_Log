package com.example.driverlog;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
        Log.i(TAG, "doInBackground: ");
        try {
            return doGet(strings[0], strings[1]);
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



    public static String doGet(String myURL, String params) throws Exception {
        byte[] data = null;
        InputStream is = null;

        URL url = new URL(myURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        conn.setRequestProperty("Content-Length", "" + params.getBytes().length);
        OutputStream os = conn.getOutputStream();
        data = params.getBytes(StandardCharsets.UTF_8);
        os.write(data);
        data = null;

        conn.connect();
        int responseCode = conn.getResponseCode();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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