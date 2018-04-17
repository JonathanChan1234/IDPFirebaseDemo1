package com.jonathan.idpdemo2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ExtendActitvity2 extends AppCompatActivity {
    String meterId;
    Button addButton, minusButton, confirmExtendButton;
    TextView carSpotText, extendPeriodText, remainingTimeText;
    String startTimeText;
    int duration;
    int secondDifference;
    private DatabaseReference mDatabase;
    int remaining;

    TextView warningExtendText;

    String[] durationTime = {"00:00", "00:15", "00:30", "00:45", "01:00", "01:15", "01:30", "01:45", "02:00"};
    int durationTimeSelected = 0;
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend_actitvity2);
        addButton = (Button) findViewById(R.id.addTimeButton);
        minusButton = (Button) findViewById(R.id.minusTimeButton);
        confirmExtendButton = (Button) findViewById(R.id.confrimExtendButton);

        addButton.setOnClickListener(addTime);
        minusButton.setOnClickListener(minusTime);
        confirmExtendButton.setOnClickListener(confirmExtend);

        extendPeriodText = (TextView) findViewById(R.id.extendPeriodText);
        carSpotText = (TextView) findViewById(R.id.carSpotText);
        remainingTimeText = (TextView) findViewById(R.id.remainingTimeText);
        warningExtendText = (TextView) findViewById(R.id.warningExtendText);

    }

    View.OnClickListener addTime = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            durationTimeSelected= (durationTimeSelected + 1 ) % 9;
            extendPeriodText.setText(durationTime[durationTimeSelected]);
        }
    };

    View.OnClickListener minusTime = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            durationTimeSelected--;
            if(durationTimeSelected < 0){
                durationTimeSelected = 8;
            }
            extendPeriodText.setText(durationTime[durationTimeSelected]);

        }
    };

    View.OnClickListener confirmExtend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("duration").getValue() != null){
                        if(dataSnapshot.child("duration").getValue().toString() != "0"){
                            int tryremaining = 0;
                            if(remaining <= 15){
                                tryremaining = durationTimeSelected * 15;
                            }
                            else{
                                tryremaining = remaining + durationTimeSelected * 15;
                            }

                            if(tryremaining <= 120){
                                remaining = tryremaining;
                            }
                            else{
                                remaining = 120;
                            }
                            try{
                                Date currentTime = Calendar.getInstance().getTime();
                                String currentTimeText = format.format(currentTime);
                                mDatabase.child("date").setValue(currentTimeText);
                                mDatabase.child("duration").setValue(remaining);
                            }
                            catch(Exception e){
                                Toast.makeText(ExtendActitvity2.this, "Please check your Internet Connection", Toast.LENGTH_LONG).show();
                                Log.d("Access", "Fail");
                            }
                        }
                        else{
                            warningExtendText.setText("The parking period is currently expired");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    };



    public void onResume(){
        super.onResume();
        Intent intent = getIntent();
        meterId = intent.getStringExtra("METER_ID");
        carSpotText.setText("Car Spot ID: " + meterId);
        try{
            mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("date").getValue() != null){
                        startTimeText = dataSnapshot.child("date").getValue().toString();
                        Log.d("startTime", startTimeText);
                        duration = Integer.parseInt(dataSnapshot.child("duration").getValue().toString());
                        Log.d("Counter", duration + "");
                        Date startTime;
                        try{
                            startTime = format.parse(startTimeText);

                        }
                        catch(Exception ParseException){
                            startTime =Calendar.getInstance().getTime();
                            Log.d("Parsing Error", "Unable to parse the value");
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(startTime);
                        calendar.add(Calendar.SECOND, duration);
                        Date endTime = calendar.getTime();
                        Date currentTime = Calendar.getInstance().getTime();
                        Log.d("Current Time", format.format(currentTime));
                        Log.d("EndTime", format.format(endTime));
                        long difference = endTime.getTime() - currentTime.getTime();
                        secondDifference = (int) (difference/1000);
                        Log.d("Time Difference", secondDifference + "");
                        new CountDownTimer(secondDifference*1000, 1000) {
                            @Override
                            public void onTick(long l) {
                                remaining = (int) l;
                                remainingTimeText.setText("Remaining: " + (l/1000));
                            }

                            @Override
                            public void onFinish() {
                                SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putBoolean(MainActivity.hasLoggedInField, false);
                                editor.commit();

                            }
                        }.start();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


}
