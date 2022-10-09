package com.example.mycall;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycall.view.VideoChatViewActivity;
import com.example.mycall.view.VoiceChatViewActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mycall.Constants.MAIN_URL;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "MainActivity";
    String remoteUserPhoneNumber = "";
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
        setContentView(R.layout.activity_main);

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

        myPhoneNumber = getIntent().getStringExtra("phoneNumber");

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



        generateMyFCMToken();
    }

    private void getRemoteUserFcmToken(String callType) {
        if (remoteUserPhoneNumber.isEmpty()) {
            Toast.makeText(this, "Valid Number Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference docRef = db.collection("tokens").document(remoteUserPhoneNumber);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    NotificationToken remoteUserNotificationToken = documentSnapshot.toObject(NotificationToken.class);
                    if (remoteUserNotificationToken == null) {
                        Toast.makeText(MainActivity.this, "The number you dial is not Registered!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Remote User FCM Token: "+remoteUserNotificationToken.getToken(), Toast.LENGTH_SHORT).show();
                    generateMyCallToken(myPhoneNumber, callType, remoteUserNotificationToken);
                }else{
                    Toast.makeText(MainActivity.this, "User Not Found!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void generateMyCallToken(String myPhoneNumber, String callType, NotificationToken remoteUserNotificationToken) {
        Log.d(TAG, "generateMyCallToken: called");
        ApiClient.getInstance(MAIN_URL).generateCallToken(myPhoneNumber, 1, 1)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    String token = jsonObject.getString("token");
                                    Toast.makeText(MainActivity.this, "MyCallToken: "+token, Toast.LENGTH_SHORT).show();
                                    if (callType.equalsIgnoreCase("voice")) {
                                        startVoiceCall(token, myPhoneNumber, callType, remoteUserNotificationToken);
                                    } else if (callType.equalsIgnoreCase("video")) {
                                        startVideoCall(token, myPhoneNumber, callType, remoteUserNotificationToken);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Log.d(TAG, "onResponse: generateMyCallToken: null");
                            }
                        }else {
                            Log.d(TAG, "onResponse: generateMyCallToken: "+response.errorBody().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.d(TAG, "onFailure: generateMyCallToken: "+t.getLocalizedMessage());
                    }
                });
    }

    public void onClick(View view){
    }



    private void startVoiceCall(String token, String channel, String type, NotificationToken remoteUserNotificationToken) {
        Intent i = new Intent(MainActivity.this, VoiceChatViewActivity.class);
        i.putExtra("channelName",channel);
        i.putExtra("token", token);
        i.putExtra("remoteFcmToken", remoteUserNotificationToken.getToken());
        i.putExtra("type", type);
        i.putExtra("uid", 1);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
    }

    private void startVideoCall(String token, String channel, String type,  NotificationToken remoteUserNotificationToken) {
        Intent i = new Intent(MainActivity.this, VideoChatViewActivity.class);
        i.putExtra("channelName",channel);
        i.putExtra("token", token);
        i.putExtra("remoteFcmToken", remoteUserNotificationToken.getToken());
        i.putExtra("type", type);
        i.putExtra("uid", 1);
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
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"0";
                    break; }
                case R.id.Button1:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"1";
                    break; }
                case R.id.Button2:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"2";
                    break; }
                case R.id.Button3:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"3";
                    break; }
                case R.id.Button4:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"4";
                    break; }
                case R.id.Button5:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"5";
                    break; }
                case R.id.Button6:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"6";
                    break; }
                case R.id.Button7:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"7";
                    break; }
                case R.id.Button8:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"8";
                    break; }
                case R.id.Button9:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"9";
                    break; }
                case R.id.Button_plus:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber +"+";
                    break; }
                case R.id.Button_back:{
                    remoteUserPhoneNumber = remoteUserPhoneNumber.substring(0, remoteUserPhoneNumber.length()-1);
                }

            }
            txtv_phone_number.setText(remoteUserPhoneNumber);


        }
        return false;
    }


    public void generateMyFCMToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                        DocumentReference docRef = db.collection("tokens").document(myPhoneNumber);
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get(myPhoneNumber) != null) {
                                    //update...
                                    updateNotificationToken(myPhoneNumber, token);
                                }else{
                                    //add
                                    NotificationToken notificationToken = new NotificationToken(myPhoneNumber, token);
                                    db.collection("tokens").document(myPhoneNumber).set(notificationToken);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });





                    }
                });
    }

    public void updateNotificationToken(String phoneNumber, String newToken){
        Log.d(TAG, "updateNotificationToken: PhoneNumber: "+phoneNumber);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tokenRef = db.collection("tokens").document(phoneNumber);

        tokenRef
                .update("token", newToken)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Token DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating Token document", e);
                    }
                });
    }

}