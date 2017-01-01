package com.gcs.asa.asagcs;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragment(R.id.left_fragment,new LeftMainMenuFragment());
        setFragment(R.id.right_fragment,new HomeFragment());
    }

    public void onConnectButtonClick(View view){

    }

    public void onHomeButtonClick(View view){
        setFragment(R.id.right_fragment,new HomeFragment());
    }

    public void onMonitorButtonClick(View view){
        setFragment(R.id.right_fragment,new ViewFragment());
    }

    public void onFlightSettingButtonClick(View view){
        //TODO create Options
    }

    public void onMissionPlaningButtonClick(View view){
        //TODO create Options
    }

    public void onOptionsButtonClick(View view){
        //TODO create Options
    }

    public void onHelpButtonClick(View view){
        //TODO Create Help Note
    }

    private void setFragment(int fragmentID, Fragment fragment){
//        define fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
//        define fragment transaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        replace new fragment into fragment place
        fragmentTransaction.replace(fragmentID,fragment);
//        execute changes into fragment
        fragmentTransaction.commit();

    }
}
