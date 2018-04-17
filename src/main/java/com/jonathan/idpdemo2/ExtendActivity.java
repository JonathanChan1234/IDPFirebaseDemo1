package com.jonathan.idpdemo2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExtendActivity extends AppCompatActivity {

    Button submitButton;
    EditText MeterIDText;
    final Context context = this;
    EditText verificationCodeText;
    TextView warningText;
    TextView warningDialogText;

    private DatabaseReference mDatabase;
    String meterId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend);
        submitButton = (Button) findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(submit);
        MeterIDText = (EditText) findViewById(R.id.MeterIDText);
        warningText = (TextView) findViewById(R.id.warningText);
    }

    View.OnClickListener submit = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
                    meterId = MeterIDText.getText().toString();
                    try{
                        mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("duration").getValue() != null){  // Meter Id is found
                                    if(dataSnapshot.child("verification").getValue().toString() == "0"){    //The parking period is currently expired
                                        warningText.setText("The parking period is currenly expired");
                                    }
                                    else{   //The parking period is not expired
                                        Log.d("Firebase", dataSnapshot.child("duration").getValue().toString() + "");
                                        final Dialog dialog = new Dialog(context);
                                        //dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
                                        dialog.setContentView(R.layout.dialoglayout);
                                        dialog.setTitle(R.string.dialogTitle);
                                        verificationCodeText = (EditText) dialog.findViewById(R.id.verificationCodeText);
                                        warningDialogText = (TextView) dialog.findViewById(R.id.warningTextDialog);
                                        Button confirmButton = (Button) dialog.findViewById(R.id.confirmButton);
                                        confirmButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String verificationCode =  verificationCodeText.getText().toString();
                                                if(TextUtils.isEmpty(verificationCode)){
                                                    warningDialogText.setText("Verification Code cannot be leave blank");
                                                }
                                                else{
                                                    if(verificationCode.equals(dataSnapshot.child("verification").getValue().toString())){
                                                        Log.d("Verification", "Success");
                                                        dialog.dismiss();
                                                        Intent intent = new Intent();
                                                        intent.putExtra("METER_ID", meterId);
                                                        intent.setClass(ExtendActivity.this, ExtendActitvity2.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                                        SharedPreferences.Editor editor = setting.edit();
                                                        editor.putBoolean(MainActivity.hasLoggedInField, true);
                                                        editor.putString(MainActivity.meterIDField, meterId);
                                                        editor.commit();
                                                        startActivity(intent);
                                                    }
                                                    else{
                                                        warningDialogText.setText("Wrong Verification Code");
                                                        Log.d("Verification", "Fail");
                                                    }
                                                }
                                            }
                                        });

                                        dialog.show();
                                    }
                                }

                                else{   //Meter Id is not found
                                    Toast.makeText(getApplicationContext(), "No Meter with this ID found", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {  //Access Error
                                warningText.setText("Database Access Error (Proposed Solution: Check your Internet Connection");
                                Log.d("Firebase", "Fail");
                            }
                        });
                    }
                    catch(Exception e){ //Access Error
                        warningText.setText("Database Access Error (Proposed Solution: Check your Internet Connection");
                        Log.d("Firebase", "Fail");
                    }



        }


    };
}
