package com.example.driverlog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    SharedPreferencesHelper mSharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferencesHelper = new SharedPreferencesHelper(MainActivity.this);
        mSharedPreferencesHelper.addBDB(1);
        // конструкция, которая призапуске программы помещает в контейнер в главном
        // активити (MainActivity) первый фрагмент (MainFragment)
        if (savedInstanceState == null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MainFragment.newInstance())
                    .addToBackStack(MainFragment.class.getName())
                    .commit();

        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 1)
            finish();
        else{
            fragmentManager.popBackStack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //этот метод добавляет меню в активити
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //функционал кнопок в меню
        FragmentManager fragmentManager;
        switch (item.getItemId()){
            case R.id.settings:
                fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 1)
                    fragmentManager.popBackStack();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment.newInstance())
                        .addToBackStack(SettingsFragment.class.getName())
                        .commit();
                break;
            case R.id.params:
                fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 1)
                    fragmentManager.popBackStack();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, ParamsFragment.newInstance())
                        .addToBackStack(ParamsFragment.class.getName())
                        .commit();
                break;
            case R.id.exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}