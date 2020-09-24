package com.example.driverlog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MainFragment extends Fragment {
    TextView tv_welcome;
    Button mMainButton;
    SharedPreferencesHelper mSharedPreferencesHelper;

    boolean isSocketConnect;
    String deviceAddress;
    BluetoothSocket socket;
    HashMap<Integer, Boolean> hashMap;
    HashMap<String, String> outHashMap;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_ma_main, container, false);
        tv_welcome = v.findViewById(R.id.tv_welcome);
        outHashMap = new HashMap<String, String>();
        mSharedPreferencesHelper = new SharedPreferencesHelper(Objects.requireNonNull(getActivity()));
        isSocketConnect =false;

        mMainButton = v.findViewById(R.id.main_button);
        mMainButton.setOnClickListener(Main_Button_CL);

        if (mSharedPreferencesHelper.getFIO().equals(""))
            tv_welcome.setText("Добро пожаловать!");
        else
            tv_welcome.setText("Добро пожаловать,\n" + mSharedPreferencesHelper.getFIO() + "!");
        return v;
    }

    private void ShowToast (String str){
        Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
    };

    View.OnClickListener Main_Button_CL = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //ConnectToELM327-------------------------------------------------------------------
            deviceAddress = mSharedPreferencesHelper.getAddress();
            if (deviceAddress.equals("")){
                ShowToast("Проверьте подключение к ELM 327 по Bluetooth");
                return;
            }

            if (socket == null) {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                } catch (Exception e) {
                    ShowToast("Не удалось создать сокет");
                    return;
                }
                try {
                    socket.connect();
                }
                catch (IOException e) {
                    Log.e("first_connect_BT", "Подключение к " + deviceAddress + " не удалось");
                    try {
                        /*socket =(BluetoothSocket) device.getClass()
                            .getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);*/
                        socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                        socket.connect();
                        Log.i("second_connect_BT","Подключено к "+ deviceAddress);
                    }
                    catch (Exception e2) {
                        Log.e("second_connect_BT", "Подключение к " + deviceAddress + " не удалось");
                        ShowToast("Подключение к " + deviceAddress + " не удалось");
                        return;
                    }
                }
            }

            try{
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.ISO_14230_4_KWP_FAST)
                        .run(socket.getInputStream(), socket.getOutputStream());
            } catch (Exception e) {
                Log.e("ODB2", e.toString());
                ShowToast("Не удалось инициализировать OBDII адаптер");
                return;
            }

            //--------------------------------------------------------------------------------------
            hashMap = mSharedPreferencesHelper.getHashMapSwitch();
            outHashMap.clear();
            outHashMap.put("MODE","insert");
            outHashMap.put("ID",mSharedPreferencesHelper.getId());

            try {
                if (hashMap.get(R.id.sw_speed)) {
                    SpeedCommand speedCommand = new SpeedCommand();
                    try {
                        speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                        outHashMap.put("SPEED",speedCommand.getFormattedResult());
                    } catch (IOException | InterruptedException e) {
                        ShowToast("Не удалось получить скорость");
                    }
                }
                if (hashMap.get(R.id.sw_rpm)) {
                    RPMCommand engineRpmCommand = new RPMCommand();
                    try {
                        engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                        outHashMap.put("RPM",engineRpmCommand.getFormattedResult());
                    } catch (IOException | InterruptedException e) {
                        ShowToast("Не удалось получить обороты двигателя");
                    }
                }
            }
            catch (Exception e){
                Log.e("hashMapError", e.toString());
                ShowToast("Настройте параметры диагностики");
                return;
            }
            SendDatabase();
        }
    };

    public void SendDatabase (){
        if (outHashMap.size()==0) {
            Log.i("Data", "Нет данных для отправки");
            ShowToast("Нет данных для отправки");
            return;
        }
        AsyncHttpPost asyncHttpPost = new AsyncHttpPost(outHashMap);
        asyncHttpPost.setListener(new AsyncHttpPost.Listener() {
            @Override
            public void onResult(String result) {
                if (result.equals("ok")) {
                    Log.i("Data", "Данные отправлены");
                    ShowToast("Отправлено:\nСкорость - " + outHashMap.get("SPEED") + "\nRPM - " + outHashMap.get("RPM"));
                }
                if (result.equals("")) {
                    Log.i("Data", "Данные не были отправлены. Проверьте подключение к серверу");
                    ShowToast("Данные не были отправлены. Проверьте подключение к серверу");
                }
                if (result.equals("no_id")) {
                    Log.i("Data", "Данные не были отправлены. Проверьте правильность идентификатора");
                    ShowToast("Данные не были отправлены. Проверьте правильность идентификатора");
                }
            }
        });
        asyncHttpPost.execute("http://" + mSharedPreferencesHelper.getIP() + "/push_data.php");
    }
}
