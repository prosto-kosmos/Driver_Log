package com.example.driverlog;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AsyncHttpPost extends AsyncTask<String, String, String> {
    interface Listener {
        void onResult(String result);
    }
    private Listener mListener;
    private HashMap<String, String> mData = null;// post data

    /**
     * constructor
     */
    public AsyncHttpPost(HashMap<String, String> data) {
        mData = data;
    }
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * background
     */
    @Override
    protected String doInBackground(String... params) {
        byte[] result = null;
        String str = "";
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(params[0]);// in this case, params[0] is URL

        try {
            // set up post data
            ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            Iterator<String> it = mData.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                nameValuePair.add(new BasicNameValuePair(key, mData.get(key)));
            }

            post.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
            HttpResponse response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
                result = EntityUtils.toByteArray(response.getEntity());
                str = new String(result, "UTF-8");
            }
        }
        catch (UnsupportedEncodingException e) {
            Log.e("tag1", e.toString());
        }
        catch (Exception e) {
            Log.e("tag2", e.toString());
        }
        return str;
    }

    /**
     * on getting result
     */
    @Override
    protected void onPostExecute(String result) {
        // something...
        if (mListener != null) {
            mListener.onResult(result);
        }
    }
}