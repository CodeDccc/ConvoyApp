package edu.temple.convoy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseService  extends FirebaseMessagingService {

    double [] lat;
    double [] lon;
    String [] username;
    String messageUrl;
    String messengerName;

    public FirebaseService(){
        //sendRegistrationToServer("fWzhZlVCQbqgtG_rsSv5ab:APA91bHmGRnx4jhbj9FqiQulu0xZG0qOf50G3_fmpKshiWfruYIABmg314fg4f0yWzlIVfZXCUBxfR5I29F7F3rJ2agajdzMeS3j5R7NkGroypWPz8H-jmemuW58zPXMX0bmdL-mWSCl");
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("TAG", "Refreshed token: " + token);
       // VolleyHelper.getVolleyUpdateFCM(this, token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("myToken", token);
        sendRegistrationToServer("fWzhZlVCQbqgtG_rsSv5ab:APA91bHmGRnx4jhbj9FqiQulu0xZG0qOf50G3_fmpKshiWfruYIABmg314fg4f0yWzlIVfZXCUBxfR5I29F7F3rJ2agajdzMeS3j5R7NkGroypWPz8H-jmemuW58zPXMX0bmdL-mWSCl");
    }

    private void sendRegistrationToServer(String token) {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
           // Log.d("check", "i saw " + remoteMessage.getData().toString());
            // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("payload"));
                JSONArray usersLocations = jsonObject.getJSONArray("data");
                messageUrl = jsonObject.getString("message_file");
                messengerName = jsonObject.getString("username");
                Log.d("for","RECEIVED MESS3 " + messageUrl);
                Log.d("for","RECEIVED MESS6543 object " + jsonObject);
                final int arraySize = usersLocations.length();
                lat = new double[arraySize];
                lon = new double[arraySize];
                username = new String[arraySize];
                for(int i = 0; i < arraySize; i++){
                    JSONObject object = usersLocations.getJSONObject(i);
                    lat[i] = object.getDouble("latitude");
                    lon[i] = object.getDouble("longitude");
                    username[i] = object.getString("username");
                    //messageUrl[i] = object.getString("message_url");
                   // Log.d("my", "WATCH " + username[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent("edu.temple.convoy_FCM");
            intent.putExtra("username", username);
            intent.putExtra("lat", lat);
            intent.putExtra("lon", lon);
            Log.d("for","RECEIVED MESS4 " + messageUrl);
            intent.putExtra("message_url", messageUrl);
            intent.putExtra("username", messengerName);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);
        }
     }

}
