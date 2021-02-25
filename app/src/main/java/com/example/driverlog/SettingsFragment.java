package com.example.driverlog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class SettingsFragment extends Fragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    Button button_connect_db;
    Button button_connect_elm327;
    Button button_save;
    EditText et_ip;
    EditText et_id;
    EditText et_period;
    SharedPreferencesHelper mSharedPreferencesHelper;

    private String deviceAddress;
    private String ParamString;
    private String RequestString;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_ma_settings, container, false);

        mSharedPreferencesHelper = new SharedPreferencesHelper(Objects.requireNonNull(getActivity()));
        button_connect_db = v.findViewById(R.id.button_connect_db);
        button_connect_elm327 = v.findViewById(R.id.button_connect_elm327);
        button_save = v.findViewById(R.id.button_save);
        et_ip = v.findViewById(R.id.et_ip);
        et_id = v.findViewById(R.id.et_id_driver);
        et_period = v.findViewById(R.id.et_period);
        button_connect_db.setOnClickListener(CL_Connect_db);
        button_connect_elm327.setOnClickListener(CL_Connect_elm327);
        button_save.setOnClickListener(CL_Save);

        et_id.setText(mSharedPreferencesHelper.getId());
        et_ip.setText(mSharedPreferencesHelper.getIP());
//        et_ip.setText(mSharedPreferencesHelper.getPeriod().toString());
        if (mSharedPreferencesHelper.getBDB()==1){
            button_connect_db.setText(R.string.connect);
            button_connect_db.setEnabled(true);
        }
        else{
            button_connect_db.setText(R.string.wait);
            button_connect_db.setEnabled(false);
        }

        return v;
    }

    View.OnClickListener CL_Connect_db = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSharedPreferencesHelper.addId("");
            mSharedPreferencesHelper.addIP("");
            mSharedPreferencesHelper.addFIO("");
            if (et_ip.getText().toString().equals("")){
                Toast.makeText(getActivity(), "Укажите адрес сервера", Toast.LENGTH_LONG).show();
                return;
            }
            if (et_id.getText().toString().equals("")){
                Toast.makeText(getActivity(), "Укажите идентификатор", Toast.LENGTH_LONG).show();
                return;
            }

            mSharedPreferencesHelper.addBDB(0);
            button_connect_db.setText(R.string.wait);
            button_connect_db.setEnabled(false);

            AsyncHttpPost asyncHttpPost = new AsyncHttpPost();

            asyncHttpPost.setListener(new AsyncHttpPost.Listener() {
                @Override
                public void onResult(String result) {
                    button_connect_db.setText(R.string.connect);
                    button_connect_db.setEnabled(true);
                    mSharedPreferencesHelper.addBDB(1);
                    if (result.equals("ServerError")) {
                        Toast.makeText(getActivity(), "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show();
                        mSharedPreferencesHelper.addFIO("");
                        return;
                    }
                    if (result.equals("Error")) {
                        Toast.makeText(getActivity(), "Пользователя с таким идентификатором не найдено", Toast.LENGTH_SHORT).show();
                        mSharedPreferencesHelper.addFIO("");
                    } else {
                        mSharedPreferencesHelper.addId(et_id.getText().toString());
                        mSharedPreferencesHelper.addIP(et_ip.getText().toString());
                        mSharedPreferencesHelper.addFIO(result);
                        Toast.makeText(getActivity(), "Вы подключены под именем:\n" + result, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("ID", et_id.getText().toString());
            } catch (JSONException e) {
                Log.e(TAG, "Не удалось создать поле данных для запроса аутентификации");
            }
            ParamString = jsonObject.toString();
            RequestString = "http://" + et_ip.getText().toString() + "/api/log/connect/";
            asyncHttpPost.execute(RequestString, ParamString);
        }
    };

    View.OnClickListener CL_Connect_elm327 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SearchBluetooth();
        }
    };

    View.OnClickListener CL_Save = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Float period;
            if (et_period.getText().toString().isEmpty()){
                period = mSharedPreferencesHelper.getPeriod();
            }
            else {
                period = Float.parseFloat(et_period.getText().toString());
                mSharedPreferencesHelper.addPeriod(period);
            }
            Intent intent = new Intent(getActivity(), GetDataService.class);
            Toast.makeText(getActivity(), "Период = " + et_period.getText().toString() + " сек", Toast.LENGTH_SHORT).show();
            getActivity().stopService(intent);
        }
    };


    public void SearchBluetooth (){
        ArrayList<String> deviceStr = new ArrayList<String>();
        final ArrayList<String> devices = new ArrayList<String>();
        BluetoothAdapter btAdapter;
        Set<BluetoothDevice> pairedDevices;
        try {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            pairedDevices = btAdapter.getBondedDevices();
        }
        catch (Exception e){
            Toast.makeText(getActivity(), "Отсутствует Bluetooth адаптер", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                deviceStr.add(device.getName());
                devices.add(device.getAddress());
            }
        }
        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_singlechoice, deviceStr.toArray(new String[deviceStr.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                deviceAddress = devices.get(position);
                mSharedPreferencesHelper.addAddress(deviceAddress);
                Toast.makeText(getActivity(), "Выбрано " + deviceAddress, Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setTitle("Выберите устройство ELM 327");
        alertDialog.show();
    };
}
