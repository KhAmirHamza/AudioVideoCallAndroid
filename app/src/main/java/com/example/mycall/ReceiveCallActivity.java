package com.example.mycall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycall.view.VideoChatViewActivity;
import com.example.mycall.view.VoiceChatViewActivity;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mycall.Constants.MAIN_URL;

public class ReceiveCallActivity extends AppCompatActivity {
    private static final String TAG = "ReceiveCallActivity";
    TextView txtvCallType, txtvPhoneNumber;

    MaterialButton btnRejectCall, btnAcceptCall;

    String channelName, type;
    //String channelToken;
    int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_call);

        txtvCallType = findViewById(R.id.txtvCallType);
        txtvPhoneNumber = findViewById(R.id.txtvPhoneNumber);
        btnRejectCall = findViewById(R.id.btnRejectCall);
        btnAcceptCall = findViewById(R.id.btnAcceptCall);

        channelName = getIntent().getStringExtra("channelName");
        //channelToken = getIntent().getStringExtra("channelToken");
        type = getIntent().getStringExtra("type");
        uid = getIntent().getIntExtra("uid", 2);

        if (type.equalsIgnoreCase("voice")) {
            txtvCallType.setText("Incoming voice call");
        } else if (type.equalsIgnoreCase("video")) {
            txtvCallType.setText("Incoming video call");
        }
        txtvPhoneNumber.setText(channelName);

        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        //MediaPlayer pla  dcyer = MediaPlayer.create(this, ringtone);
        //player.setLooping(true);
        //player.start();
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), ringtone);
        // r.play();


        btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            generateMyCallToken(channelName, type);
 /*               if (type.equalsIgnoreCase("voice")) {
                    startVoiceCall(channelToken, channelName, type);
                } else if (type.equalsIgnoreCase("video")) {
                    startVideoCall(channelToken, channelName, type);
                }*/
            }
        });



        btnRejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void generateMyCallToken(String channelName, String callType) {
        Log.d(TAG, "generateMyCallToken: called");
        ApiClient.getInstance(MAIN_URL).generateCallToken(channelName, 1, 2)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    String token = jsonObject.getString("token");
                                    Log.d(TAG, "onResponse: my Token OnReceive Call: "+token);
                                    Toast.makeText(ReceiveCallActivity.this, "MyCallToken: "+token, Toast.LENGTH_SHORT).show();
                                    if (callType.equalsIgnoreCase("voice")) {
                                        startVoiceCall(token, channelName, callType);
                                    } else if (callType.equalsIgnoreCase("video")) {
                                        startVideoCall(token, channelName, callType);
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

    private void startVoiceCall(String token, String channel, String type) {
        Intent i = new Intent(ReceiveCallActivity.this, VoiceChatViewActivity.class);
        i.putExtra("channelName",channel);
        i.putExtra("token", token);
        i.putExtra("type", type);
        i.putExtra("uid", 2);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
    }

    private void startVideoCall(String token, String channel, String type) {
        Intent i = new Intent(ReceiveCallActivity.this, VideoChatViewActivity.class);
        i.putExtra("channelName",channel);
        i.putExtra("token", token);
        i.putExtra("type", type);
        i.putExtra("uid", 2);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
    }
}