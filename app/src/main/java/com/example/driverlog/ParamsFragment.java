package com.example.driverlog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

public class ParamsFragment extends Fragment {

    Vector<Switch> SwitchVector;
    HashMap<Integer, Boolean> hashMap;
    SharedPreferencesHelper mSharedPreferencesHelper;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_all;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_speed;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_rpm;

    public static ParamsFragment newInstance() {
        return new ParamsFragment();
    }

    CompoundButton.OnCheckedChangeListener SwitchAllListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            for (int i = 0; i < SwitchVector.size(); i++) {
                if (compoundButton.isChecked()) {
                    SwitchVector.get(i).setChecked(true);
                }
                else {
                    SwitchVector.get(i).setChecked(false);
                }
            }

            hashMap.clear();
            for(int i = 0; i < SwitchVector.size(); i++){
                hashMap.put(SwitchVector.get(i).getId(), SwitchVector.get(i).isChecked());
            }
            mSharedPreferencesHelper.addHashMapSwitch(hashMap);
        }
    };

    CompoundButton.OnCheckedChangeListener SwitchAnyListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            hashMap.clear();
            for(int i = 0; i < SwitchVector.size(); i++){
                hashMap.put(SwitchVector.get(i).getId(), SwitchVector.get(i).isChecked());
            }
            mSharedPreferencesHelper.addHashMapSwitch(hashMap);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_ma_params, container, false);
        SwitchVector = new Vector<Switch>();
        hashMap = new HashMap<Integer, Boolean>();
        mSharedPreferencesHelper = new SharedPreferencesHelper(Objects.requireNonNull(getActivity()));

        sw_all = v.findViewById(R.id.sw_all);
        SwitchVector.add(sw_all);
        sw_speed = v.findViewById(R.id.sw_speed);
        SwitchVector.add(sw_speed);
        sw_rpm = v.findViewById(R.id.sw_rpm);
        SwitchVector.add(sw_rpm);

        if(mSharedPreferencesHelper.getHashMapSwitch().size() != 0){
            for(int i = 0; i < SwitchVector.size(); i++){
                try {
                    boolean k = mSharedPreferencesHelper.getHashMapSwitch().get(SwitchVector.get(i).getId());
                    SwitchVector.get(i).setChecked(k);
                }
                catch (Exception e){
                    Log.e("switch", e.toString());
                }
            }
        }

        sw_all.setOnCheckedChangeListener(SwitchAllListener);
        sw_speed.setOnCheckedChangeListener(SwitchAnyListener);
        sw_rpm.setOnCheckedChangeListener(SwitchAnyListener);
        return v;
    }

}
