package com.jonathan.idpdemo2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button extendButton, checkEmptySpotButton;
    public SharedPreferences setting;
    public static String data = "DATA";
    public static String hasLoggedInField = "HASLOGGEDIN";
    public static String meterIDField;
    boolean hasLoggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        extendButton = (Button) findViewById(R.id.extendButton);
        checkEmptySpotButton = (Button) findViewById(R.id.checkEmptySpotButton);

        extendButton.setOnClickListener(extend);
        checkEmptySpotButton.setOnClickListener(checkEmptySpot);

//        setting = getApplication().getSharedPreferences(data, 0);
//        SharedPreferences.Editor editor = setting.edit();
//        editor.putBoolean(MainActivity.hasLoggedInField,  false);
//        editor.commit();


    }

    View.OnClickListener extend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            setting = getApplication().getSharedPreferences(data, 0);
            hasLoggedIn =  setting.getBoolean(hasLoggedInField, false);
            Log.d("Logged In", hasLoggedIn + "");
            if(!hasLoggedIn){
                intent.setClass(MainActivity.this, ExtendActivity.class);
            }
            else{
                intent.putExtra("METER_ID",setting.getString(MainActivity.meterIDField,""));
                intent.setClass(MainActivity.this, ExtendActitvity2.class);
            }
            startActivity(intent);
        }
    };

    View.OnClickListener checkEmptySpot = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
}
