package com.example.mycall;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycall.view.VideoChatViewActivity;
import com.example.mycall.view.VoiceChatViewActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mycall.Constants.MAIN_URL;

public class DialActivity extends AppCompatActivity implements View.OnTouchListener {

    String phoneNumber = "01879820161";
    TextView txtv_phone_number;
    FirebaseFirestore db;


    ImageButton Button1, Button2, Button3, Button4, Button5, Button6,
            Button7, Button8,Button9, Button0, Button_plus,Button_back;

    MaterialButton btnVideoCall, btnVoiceCall;

    String myPhoneNumber;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial);

        Button0 = findViewById(R.id.Button0);
        Button1 = findViewById(R.id.Button1);
        Button2 = findViewById(R.id.Button2);
        Button3 = findViewById(R.id.Button3);
        Button4 = findViewById(R.id.Button4);
        Button5 = findViewById(R.id.Button5);
        Button6 = findViewById(R.id.Button6);
        Button7 = findViewById(R.id.Button7);
        Button8 = findViewById(R.id.Button8);
        Button9 = findViewById(R.id.Button9);
        Button_plus = findViewById(R.id.Button_plus);
        Button_back = findViewById(R.id.Button_back);
        btnVideoCall = findViewById(R.id.btnVideoCall);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        txtv_phone_number = findViewById(R.id.txtv_phone_number);

        Button0.setOnTouchListener(this);
        Button1.setOnTouchListener(this);
        Button2.setOnTouchListener(this);
        Button3.setOnTouchListener(this);
        Button4.setOnTouchListener(this);
        Button5.setOnTouchListener(this);
        Button6.setOnTouchListener(this);
        Button7.setOnTouchListener(this);
        Button8.setOnTouchListener(this);
        Button9.setOnTouchListener(this);
        Button_plus.setOnTouchListener(this);
        Button_back.setOnTouchListener(this);

        db = FirebaseFirestore.getInstance();

        btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //At first get the Notification token using Mobile Number...
                getRemoteUserFcmToken("video");
            }
        });

        btnVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //At first get the Notification token using Mobile Number...
                getRemoteUserFcmToken("voice");
            }
        });
    }

    private void getRemoteUserFcmToken(String callType) {
        DocumentReference docRef = db.collection("tokens").document(phoneNumber);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    NotificationToken notificationToken = documentSnapshot.toObject(NotificationToken.class);
                    sendCallNotificationToRemoteUser(notificationToken.getToken(), callType);
                    generateCallToken(myPhoneNumber, callType);
                }else{
                    Toast.makeText(DialActivity.this, "User Not Found!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendCallNotificationToRemoteUser(String token, String callType) {
        NotificationToken notificationToken = new NotificationToken(new String[]{token});
        ApiClient.getInstance(MAIN_URL)
                .sendCallNotificationToRemoteUser("channelToken" ,myPhoneNumber, callType, notificationToken)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            //if (response.body())

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
    }

    private void generateCallToken(String myPhoneNumber, String callType) {
        ApiClient.getInstance(MAIN_URL).generateCallToken(myPhoneNumber, 1, 1)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    String token = jsonObject.getString("token");
                                    Toast.makeText(DialActivity.this, "MyCallToken: "+token, Toast.LENGTH_SHORT).show();
                                    if (callType.equalsIgnoreCase("voice")) {
                                        startVoiceCall(token, myPhoneNumber, callType);
                                    } else if (callType.equalsIgnoreCase("video")) {
                                        startVideoCall(token, myPhoneNumber, callType);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
    }

    public void onClick(View view){
    }



    private void startVoiceCall(String token, String channel, String type) {
        Intent i = new Intent(DialActivity.this, VoiceChatViewActivity.class);
        i.putExtra("channelName",channel);
        i.putExtra("token", token);
        i.putExtra("type", type);
        i.putExtra("uid", 2);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
    }

    private void startVideoCall(String token, String channel, String type) {
        Intent i = new Intent(DialActivity.this, VideoChatViewActivity.class);
        i.putExtra("channelName",channel);
        i.putExtra("token", token);
        i.putExtra("type", type);
        i.putExtra("uid", 2);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Toast.makeText(this, "Action: "+event.getAction(), Toast.LENGTH_SHORT).show();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackground(getResources().getDrawable(R.drawable.bg_number,null));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setBackground(getResources().getDrawable(R.drawable.circle,null));

            switch (v.getId()){
                case R.id.Button0:{
                    phoneNumber = phoneNumber+"0";
                    break; }
                case R.id.Button1:{
                    phoneNumber = phoneNumber+"1";
                    break; }
                case R.id.Button2:{
                    phoneNumber = phoneNumber+"2";
                    break; }
                case R.id.Button3:{
                    phoneNumber = phoneNumber+"3";
                    break; }
                case R.id.Button4:{
                    phoneNumber = phoneNumber+"4";
                    break; }
                case R.id.Button5:{
                    phoneNumber = phoneNumber+"5";
                    break; }
                case R.id.Button6:{
                    phoneNumber = phoneNumber+"6";
                    break; }
                case R.id.Button7:{
                    phoneNumber = phoneNumber+"7";
                    break; }
                case R.id.Button8:{
                    phoneNumber = phoneNumber+"8";
                    break; }
                case R.id.Button9:{
                    phoneNumber = phoneNumber+"9";
                    break; }
                case R.id.Button_plus:{
                    phoneNumber = phoneNumber+"+";
                    break; }
                case R.id.Button_back:{
                    phoneNumber = phoneNumber.substring(0,phoneNumber.length()-1);
                }

            }
            txtv_phone_number.setText(phoneNumber);


        }
        return false;
    }
}