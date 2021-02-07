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

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Objects;

public class MainFragment extends Fragment {
    public static final String TAG = MainFragment.class.getSimpleName();
    TextView tv_welcome;
    Button mStartButton;
    Button mStopButton;
    Button mSendDataButton;
    Button mDeleteDataButton;
    SharedPreferencesHelper mSharedPreferencesHelper;

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
                Objects.requireNonNull(getActivity()).startService(intent);
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GetDataService.class);
                Objects.requireNonNull(getActivity()).stopService(intent);
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
                if (mSharedPreferencesHelper.getDataSet().size() == 0){
                    Toast.makeText(getActivity(), "Нет данных для отправки", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), SendDataService.class);
                Objects.requireNonNull(getActivity()).startService(intent);
            }
        });

        if (mSharedPreferencesHelper.getFIO().equals(""))
            tv_welcome.setText("Добро пожаловать!");
        else
            tv_welcome.setText("Добро пожаловать,\n" + mSharedPreferencesHelper.getFIO() + "!");
        return v;
    }
}
