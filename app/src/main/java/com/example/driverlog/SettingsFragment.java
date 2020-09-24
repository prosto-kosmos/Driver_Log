package com.example.driverlog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
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

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SettingsFragment extends Fragment {

    Button button_connect_db;
    Button button_connect_elm327;
    EditText et_ip;
    EditText et_id;
    SharedPreferencesHelper mSharedPreferencesHelper;

    String deviceAddress;

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
        et_ip = v.findViewById(R.id.et_ip);
        et_id = v.findViewById(R.id.et_id_driver);
        button_connect_db.setOnClickListener(CL_Connect_db);
        button_connect_elm327.setOnClickListener(CL_Connect_elm327);

        et_id.setText(mSharedPreferencesHelper.getId());
        et_ip.setText(mSharedPreferencesHelper.getIP());
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
                ShowToast("Укажите IP-адрес сервера БД");
                return;
            }
            if (et_id.getText().toString().equals("")){
                ShowToast("Укажите идентификатор");
                return;
            }

            mSharedPreferencesHelper.addBDB(0);
            button_connect_db.setText(R.string.wait);
            button_connect_db.setEnabled(false);

            HashMap<String, String> data = new HashMap<String, String>();
            data.put("MODE", "connect");
            data.put("ID", et_id.getText().toString());
            AsyncHttpPost asyncHttpPost = new AsyncHttpPost(data);
            asyncHttpPost.setListener(new AsyncHttpPost.Listener() {
                @Override
                public void onResult(String result) {
                    button_connect_db.setText(R.string.connect);
                    button_connect_db.setEnabled(true);
                    mSharedPreferencesHelper.addBDB(1);
                    if (result.equals("")) {
                        ShowToast("Сервер не отвечает");
                        mSharedPreferencesHelper.addFIO("");
                        return;
                    }
                    if (result.equals("no_id")) {
                        ShowToast("Пользователя с таким идентификатором не найдено");
                        mSharedPreferencesHelper.addFIO("");
                    } else {
                        mSharedPreferencesHelper.addId(et_id.getText().toString());
                        mSharedPreferencesHelper.addIP(et_ip.getText().toString());
                        mSharedPreferencesHelper.addFIO(result);
                        ShowToast("Вы подключены под именем:\n" + result);
                    }
                }
            });
            asyncHttpPost.execute("http://" + et_ip.getText().toString() + "/push_data.php");
        }
    };

    View.OnClickListener CL_Connect_elm327 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SearchBluetooth();
        }
    };

    public void ShowToast(String string){
        try {
            Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Log.e("Toast", e.toString());
        }
    }

    public void SearchBluetooth (){

        //SearchBluetooth-------------------------------------------------------------------------

        ArrayList<String> deviceStrs = new ArrayList<String>();
        final ArrayList<String> devices = new ArrayList<String>();
        BluetoothAdapter btAdapter;
        Set<BluetoothDevice> pairedDevices;

        try {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            pairedDevices = btAdapter.getBondedDevices();
        }
        catch (Exception e){
            ShowToast("Отсутствует Bluetooth адаптер");
            return;
        }
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }
        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                deviceAddress = devices.get(position);
                mSharedPreferencesHelper.addAddress(deviceAddress);
                ShowToast("Выбрано " + deviceAddress);
            }
        });
        alertDialog.setTitle("Выберите устройство ELM 327");
        alertDialog.show();
    };
}
