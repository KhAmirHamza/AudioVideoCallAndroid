package com.example.mycall;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";
    //MediaPlayer player;

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        //sendRegistrationToServer(token);

        SharedPreferences sp = getSharedPreferences("Notification", Context.MODE_PRIVATE);
        String phoneNumber = sp.getString("phoneNumber", null);
        if (phoneNumber == null) return;
        
        updateNotificationToken(phoneNumber, token);
        
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        String type = remoteMessage.getData().get("type");
        String channel = remoteMessage.getData().get("channel_name");
        String channelToken = remoteMessage.getData().get("channelToken");

        if (type != null && channel != null) {
            receiveCall(channel, type, channelToken);
        }
        Log.d(TAG, "onMessageReceived: "+remoteMessage.getData());
        
    }



    private void receiveCall( String channel, String type, String channelToken) {
        Intent i = new Intent();
        i.putExtra("channelName",channel);
        i.putExtra("channelToken",channelToken);
        i.putExtra("type", type);
        i.putExtra("uid", 2);
        i.setClassName(getPackageName(), getPackageName()+".ReceiveCallActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        //MediaPlayer player = MediaPlayer.create(this, ringtone);
        //player.setLooping(true);
        //player.start();
       // Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), ringtone);
        //r.play();

        getApplicationContext().startActivity(i);
    }

    public void updateNotificationToken(String phoneNumber, String newToken){
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
