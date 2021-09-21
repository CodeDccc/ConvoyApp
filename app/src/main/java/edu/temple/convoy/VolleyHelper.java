
package edu.temple.convoy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class VolleyHelper {

    /**this function uses volley to log in a user*/
    public static void getVolleyLogIn(Context context, String action, String actionType, String username, String password){

        final String URL = "https://kamorris.com/lab/convoy/account.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");
                        if(result.equals("SUCCESS")){
                            String sessionKey = jsonObject.getString("session_key");
                            SharedPreferences sharedPref =  context.getSharedPreferences("myPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("username", username );
                            editor.putString("sessionKey", sessionKey );
                            editor.apply();
                           // Log.d("TAG", "resultKey " + sessionKey);
                          context.startActivity(new Intent(context, LoggedInActivity.class));
                        }
                       // Log.d(String.valueOf(context), "registeryyyyy: " + result);
                        Toast toast =  Toast.makeText(context, result, Toast.LENGTH_LONG);
                        toast.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**this function uses volley to register a user*/
    public static void getVolleyRegister(Context context, String action, String actionType, String firstname, String lastname, String username, String password){

        final String URL = "https://kamorris.com/lab/convoy/account.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                            String sessionKey = jsonObject.getString("session_key");
                            SharedPreferences sharedPref =  context.getSharedPreferences("myPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("firstname", firstname);
                            editor.putString("lastname", lastname);
                            editor.putString("username", username);
                            editor.putString("sessionKey", sessionKey);
                            editor.apply();
                            context.startActivity(new Intent(context, LoggedInActivity.class));
                        }
                        try {
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**this function uses volley to start a convoy for a user*/
    public static void getVolleyStartConvoy(Context context, String action, String actionType, String username, String sessionKey, TextView edit){

        final String URL = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                           // Log.d("tag", "SUCCESSS " + "SUCCCUSEE");
                            String convoyId = jsonObject.getString("convoy_id");
                            edit.setText("Convoy Id: " + convoyId);
                            FirebaseMessaging.getInstance().subscribeToTopic(convoyId);
                            SharedPreferences sharedPref =  context.getSharedPreferences("myPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("convoyId", convoyId);
                            editor.apply();
                            Log.d("TAG", "convoyId: " + convoyId);
                        }
                       // Log.d("TAG", "StartConvoy: " + result);
                        try {
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("username", username);
                params.put("session_key", sessionKey);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**this function uses volley to end a convoy for a user*/
    public static void getVolleyEndConvoy(Context context, String action, String actionType){
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        String sessionKey = sharedPref.getString("sessionKey", null);
        String convoyId = sharedPref.getString("convoyId", null);

        final String URL = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                            Toast.makeText(context, "Leaving Convoy... ", Toast.LENGTH_LONG).show();
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(convoyId);
                        }
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("username", username);
                params.put("session_key", sessionKey);
                params.put("convoy_id", convoyId);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**this function uses volley to join a convoy for a user*/
    public static void getVolleyJoinConvoy(Context context, String action, String actionType, String convoyValue){
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        String sessionKey = sharedPref.getString("sessionKey", null);
       // String convoyId = sharedPref.getString("convoyId", null);

        final String URL = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                            Toast.makeText(context, "Joining Convoy... ", Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("convoyId", convoyValue);
                            editor.apply();
                            FirebaseMessaging.getInstance().subscribeToTopic(convoyValue);
                        }
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("username", username);
                params.put("session_key", sessionKey);
                params.put("convoy_id", convoyValue);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**this function uses volley to leave a convoy for a user*/
    public static void getVolleyLeaveConvoy(Context context, String action, String actionType){
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        String sessionKey = sharedPref.getString("sessionKey", null);
        String convoyId = sharedPref.getString("convoyId", null);

        final String URL = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                            Toast.makeText(context, "Leaving Convoy... ", Toast.LENGTH_LONG).show();
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(convoyId);
                        }
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("username", username);
                params.put("session_key", sessionKey);
                params.put("convoy_id", convoyId);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**this function uses volley to update user location*/
    public static void getVolleyUpdate(Context context, String action, String actionType, String lat, String lon){
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        String sessionKey = sharedPref.getString("sessionKey", null);
        String convoyId = sharedPref.getString("convoyId", null);

        final String URL = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                            Toast.makeText(context, "Leaving Convoy... ", Toast.LENGTH_LONG).show();
                        }
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error, Please try again " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(context, "Error, Please try again" + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(action, actionType);
                params.put("username", username);
                params.put("session_key", sessionKey);
                params.put("convoy_id", convoyId);
                params.put("latitude", lat);
                params.put("longitude", lon);
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}

