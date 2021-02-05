package com.example.driverlog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class MainFragment extends Fragment {
    TextView tv_welcome;
    Button mStartButton;
    Button mStopButton;
    Button mSendDataButton;
    Button mDeleteDataButton;
    SharedPreferencesHelper mSharedPreferencesHelper;
    String ProtocolString;
    String ServerAddressString;
    String AllDataSetString;
    String IdString;
    String RequestString;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_ma_main, container, false);
        tv_welcome = v.findViewById(R.id.tv_welcome);
        mSharedPreferencesHelper = new SharedPreferencesHelper(Objects.requireNonNull(getActivity()));

        mStartButton = v.findViewById(R.id.start_button);
        mStopButton = v.findViewById(R.id.stop_button);
        mSendDataButton = v.findViewById(R.id.send_data_button);
        mDeleteDataButton = v.findViewById(R.id.delete_data_button);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GetDataService.class);
                getActivity().startService(intent);
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GetDataService.class);
                getActivity().stopService(intent);
            }
        });

        mDeleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Записей удалено: " + mSharedPreferencesHelper.getDataSet().size(), Toast.LENGTH_SHORT).show();
                mSharedPreferencesHelper.delDataSet();
            }
        });

        mSendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSharedPreferencesHelper.getId().equals("")){
                    Toast.makeText(getActivity(), "Вы не авторизованы на сервере", Toast.LENGTH_SHORT).show();
                    return;
                }
                ProtocolString = "http://";
                ServerAddressString = mSharedPreferencesHelper.getIP() + "/api/log/insert?";
                IdString = "ID=" + mSharedPreferencesHelper.getId() + "&";
                AllDataSetString = "DATA=" + mSharedPreferencesHelper.getDataSetJson();
                RequestString = ProtocolString + ServerAddressString + IdString + AllDataSetString;
                AsyncHttpPost asyncHttpPost = new AsyncHttpPost((MainActivity) getActivity());
                asyncHttpPost.setListener(new AsyncHttpPost.Listener() {
                    @Override
                    public void onResult(String result) {
                        if (result.equals("ServerError")) {
                            Toast.makeText(getActivity(), "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (result.equals("Error")) {
                            Toast.makeText(getActivity(), "Данные не были отправлены", Toast.LENGTH_SHORT).show();
                        }
                        if (result.equals("Ok")) {
                            Toast.makeText(getActivity(), "Записей отправлено: " + mSharedPreferencesHelper.getDataSet().size(), Toast.LENGTH_SHORT).show();
                            mSharedPreferencesHelper.delDataSet();
                        }
                    }
                });
                asyncHttpPost.execute(RequestString);
            }
        });

        if (mSharedPreferencesHelper.getFIO().equals(""))
            tv_welcome.setText("Добро пожаловать!");
        else
            tv_welcome.setText("Добро пожаловать,\n" + mSharedPreferencesHelper.getFIO() + "!");
        return v;
    }

//    public void SendDatabase (){
//        AsyncHttpPost asyncHttpPost = new AsyncHttpPost(outHashMap);
//        asyncHttpPost.setListener(new AsyncHttpPost.Listener() {
//            @Override
//            public void onResult(String result) {
//                if (result.equals("ok")) {
//                    Log.i("Data", "Данные отправлены");
//                    ShowToast("Отправлено:\nСкорость - " + outHashMap.get("SPEED") + "\nRPM - " + outHashMap.get("RPM"));
//                }
//                if (result.equals("")) {
//                    Log.i("Data", "Данные не были отправлены. Проверьте подключение к серверу");
//                    ShowToast("Данные не были отправлены. Проверьте подключение к серверу");
//                }
//                if (result.equals("no_id")) {
//                    Log.i("Data", "Данные не были отправлены. Проверьте правильность идентификатора");
//                    ShowToast("Данные не были отправлены. Проверьте правильность идентификатора");
//                }
//            }
//        });
//        //asyncHttpPost.execute("http://" + mSharedPreferencesHelper.getIP() + "/push_data.php");
//        asyncHttpPost.execute();
//    }
}
