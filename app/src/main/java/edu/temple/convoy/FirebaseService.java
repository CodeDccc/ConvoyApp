package edu.temple.convoy;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseService  extends FirebaseMessagingService {

    double [] lat;
    double [] lon;
    String [] username;

    public FirebaseService(){
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("TAG", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String refreshedToken) {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            Log.d("check", "i saw");
            // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData());
                JSONArray usersLocations = jsonObject.getJSONArray("data");
                for(int i = 0; i < usersLocations.length(); i++){
                    JSONObject object = usersLocations.getJSONObject(i);
                    lat[i] = object.getDouble("latitude");
                    lon[i] = object.getDouble("longitude");
                    username[i] = object.getString("username");
                    Log.d("my", "WATCH" + username[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent("edu.temple.convoy_FCM");
            intent.putExtra("username", username);
            intent.putExtra("lat", lat);
            intent.putExtra("lon", lon);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);
        }
     }

}
