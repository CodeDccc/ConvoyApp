package edu.temple.convoy;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggedInActivity extends AppCompatActivity implements OnMapReadyCallback, ConvoyControlFragment.ConvoyInterface, ForegroundService.Update{
    String[] strings;
    final static String URLL = "https://kamorris.com/lab/convoy/convoy.php";
    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> audioItem = new ArrayList<>();
   // Map<String, String>list = new HashMap<>();
    private FloatingActionButton record;
    MediaRecorder mediaRecorder;
    public static String audioFile = "audio.3gpp";
    public static int AUDIO_OK = 555;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String dateTime;
    RecyclerView recyclerView;

    LocationManager locationManager;
    Location myLocation;
    ForegroundService myService;
    Boolean isConvoy = false;
    private FloatingActionButton endCon;
    private FloatingActionButton cancelRecord;
    private FloatingActionButton sendRecord;
    private Button logOutBtn;
    private String key;
    private TextView convoyText;
    private LatLng latLng;
    private final String NAME = "username";
    private final String KEY = "sessionKey";
    //private final String CONID = "convoyId";
    private ArrayList<String> otherUsers = new ArrayList<>();
    private static final Map<String, Vehicle> param = new HashMap<>();
    private static Vehicle[] vehicles;
    private String name;
    private FragmentManager fragmentManager;
    private ConvoyControlFragment convoyControlFragment;
    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Boolean startedConvoy = false;
    Boolean joinedConvoy = false;
    GoogleMap map;
    Marker marker;
    String message1;
    RecyclerAdapter recyclerAdapter;
    MarkerOptions myMarkerOptions = new MarkerOptions();
    //int[] cars = {R.drawable.blue_covertable, R.drawable.blue_limo, R.drawable.red_limo, R.drawable.yellow_car, R.drawable.yellow405_car};
    public static SupportMapFragment mapFragment;
    private long downloadID;
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                Toast.makeText(LoggedInActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.myColor, null)));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("edu.temple.convoy_FCM"));
        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        locationManager = getSystemService(LocationManager.class);
        convoyText = findViewById(R.id.convoyText);


        recyclerView = findViewById(R.id.recyclerView);
        strings = getResources().getStringArray(R.array.prog);

        recyclerAdapter = new RecyclerAdapter(this, list, audioItem);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        createNotificationChannel();

        //Log.d("that", "LOK HER " + getFilePath());

        record = findViewById(R.id.recordAudio);
        record.setVisibility(View.INVISIBLE);

        cancelRecord = findViewById(R.id.cancelrecord);
        cancelRecord.setVisibility(View.INVISIBLE);

        sendRecord = findViewById(R.id.sendrecord);
        sendRecord.setVisibility(View.INVISIBLE);

        record.setOnClickListener(v -> {
            cancelRecord.setVisibility(View.VISIBLE);
            sendRecord.setVisibility(View.VISIBLE);
            record.setVisibility(View.INVISIBLE);

            try {
               // mediaRecorder.prepare();
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .RECORD_AUDIO}, AUDIO_OK);
                }
                else{
                    getRecorder();
                   // mediaRecorder.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Long dateValueInLong = System.currentTimeMillis();
            Date currentTime = Calendar.getInstance().getTime();
           // Log.d("rel", "lok her333 " + currentTime);
            calendar = Calendar.getInstance();
            simpleDateFormat = new SimpleDateFormat("dd.LLL.yyyy HH:mm:ss");
            dateTime = simpleDateFormat.format(calendar.getTime());
           // Log.d("rel", "lok her333 " + dateTime);
        });

        cancelRecord.setOnClickListener(v -> {
            cancelRecord.setVisibility(View.INVISIBLE);
            sendRecord.setVisibility(View.INVISIBLE);
            record.setVisibility(View.VISIBLE);
            mediaRecorder.stop();
            mediaRecorder.release();
        });

        sendRecord.setOnClickListener(v -> {
            sendRecord.setVisibility(View.INVISIBLE);
            cancelRecord.setVisibility(View.INVISIBLE);
            record.setVisibility(View.VISIBLE);
            mediaRecorder.stop();
            mediaRecorder.release();
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(getFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            list.add(0, dateTime);
            audioItem.add(getFilePath());
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Call smooth scroll
                    recyclerView.smoothScrollToPosition(recyclerAdapter.getItemCount() - 1);
                }
            });
           /* try {
                upLoadFile(getFilePath());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            try {
                convert(getFilePath());
            } catch (IOException | AuthFailureError e) {
                e.printStackTrace();
            }
            // getVolleySendMessage(this, message1);
        });

        //VolleyHelper.getVolleyUploadTokenIfNotAlreadyRegistered(this);

        /**Get a fragment to attach google map*/
        mapFragment = (SupportMapFragment)getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**Attach control fragment to loggedinActivity*/
        fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        if ((fragment = fragmentManager.findFragmentById(R.id.convoyControl)) instanceof ConvoyControlFragment)
            convoyControlFragment = (ConvoyControlFragment) fragment;
        else {
            convoyControlFragment = new ConvoyControlFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.convoyControl, convoyControlFragment)
                    .commit();
        }
        
        /**get username to add to nav bar as a welcome, and get convoy id to show convoy id# when user is in a convoy*/
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        name = sharedPref.getString(NAME, null);
        key = sharedPref.getString(KEY, null);
        setTitle("Hello, " + name);

        /**Log out button, logs the user out*/
        logOutBtn = findViewById(R.id.logOutBtn);
        logOutBtn.setTextColor(getResources().getColor(R.color.myColor, null));
        logOutBtn.setBackgroundColor(Color.LTGRAY);
        logOutBtn.setOnClickListener(V ->{
            if(startedConvoy || joinedConvoy){
                Toast.makeText(this, "Please end or leave convoy before logging out", Toast.LENGTH_LONG).show();
            }
            else{
                editor = sharedPref.edit();
                editor.clear().apply();
                startActivity(new Intent(this, MainActivity.class));
            }
        });

        /**end convoy button, this button is invisible until user have started a convoy*/
        endCon = findViewById(R.id.endCon);
        endCon.setVisibility(View.INVISIBLE);
        endCon.setOnClickListener(v -> {
            if(startedConvoy) {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.endconvoy)
                        .setTitle("Stop Convoy")
                        .setMessage("Do you want to end convoy?")
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            try{
                                VolleyHelper.getVolleyEndConvoy(this, "action", "END");
                                getEndService();
                                endCon.setVisibility(View.INVISIBLE);
                                convoyText.setText("");
                                startedConvoy = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            else{
                Toast.makeText(this, "You did not start a convoy!", Toast.LENGTH_LONG).show();
            }
         });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
        unregisterReceiver(broadcastReceiver);
    }


    private void getRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(getFilePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.prepare();
        mediaRecorder.start();

       // mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.);
    }

    /**a notification is required for a foreground service*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                "convoy"
                , "Start Convoy"
                , NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private String getFilePath(){
        //dateTime
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File audiDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(audiDirectory,  audioFile);
        return file.getPath();
    }
    private File getFile(){//dateTime+
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File audiDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(audiDirectory, audioFile);
        return file;
    }

    private void convert(String path) throws IOException, AuthFailureError {

        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fis.read(b)) != -1;) {
            bos.write(b, 0, readNum);
        }

        byte[] bytes = bos.toByteArray();
        /*Log.d("for","RECEIVED MESS6 " + bytes[0]);
        File file = new File(path);
        //File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream newFile = new FileOutputStream(file);
        newFile.write(bytes);
        newFile.close();*/

        upLoadFile2(bytes);
    }
    private void upLoadFile2(byte[] file) throws IOException, AuthFailureError {
        SharedPreferences sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        String sessionKey = sharedPref.getString("sessionKey", null);
        String convoyId = sharedPref.getString("convoyId", null);
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "MESSAGE");
        params.put("username", username);
        params.put("session_key", sessionKey);
        params.put("convoy_id", convoyId);
        VolleyMultipartRequest.DataPart dataPart = new VolleyMultipartRequest.DataPart("message_file", file, "audio/3gpp");
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(1, URLL,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        // error handling

                        Log.d("not", "did i error " + error);

                    }
                },
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("not", "did " + "came here1");
                        Log.d("not", "did10 " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(String.valueOf(response.data));
                            String result = jsonObject.getString("status");
                           // Log.d("not", "did10 " + response);
                            Log.d("not", "did11 " + result);
                            Log.d("not", "did " + response.data);
                            if(result.equals("SUCCESS")) {
                                Toast.makeText(LoggedInActivity.this, "Message sent... ", Toast.LENGTH_LONG).show();
                            } else if(result.equals("ERROR")){
                                Log.d("not", "did " + response);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, dataPart, params);
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(this);
        requestQueue.add(volleyMultipartRequest);
    }

    private void upLoadFile(byte[] file) throws IOException {
        //File file = new File(String.valueOf(newFile));
        Log.d("for","RECEIVED MESS5 " + file);
        SharedPreferences sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        String sessionKey = sharedPref.getString("sessionKey", null);
        String convoyId = sharedPref.getString("convoyId", null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "MESSAGE");
        params.put("username", username);
        params.put("session_key", sessionKey);
        params.put("convoy_id", convoyId);


        HashMap<String, byte[]> fileParams = new HashMap<>();
        fileParams.put("message_file", file);

        MultipartRequest mMultipartRequest = new MultipartRequest(URLL,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        // error handling

                            Log.d("not", "did i error " + error);

                    }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("status");

                            if(result.equals("SUCCESS")) {
                                Toast.makeText(LoggedInActivity.this, "Message sent... ", Toast.LENGTH_LONG).show();
                            } else if(result.equals("ERROR")){
                                Log.d("not", "did " + response);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, fileParams, params, file
        );
        mMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(mMultipartRequest);
    }
    /**this function uses volley to join a convoy for a user*/
    public void getVolleySendMessage(Context context, String message){
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
                            Toast.makeText(context, "Message sent... ", Toast.LENGTH_LONG).show();

                        }
                        else if(result.equals("ERROR")){
                            Log.d("not", "did " + response);
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
                params.put("action", "MESSAGE");
                params.put("username", username);
                params.put("session_key", sessionKey);
                params.put("convoy_id", convoyId);
                params.put("message_file", message );
                return params;
            }
        };
        RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }




    /**when the map is ready, put a marker at users last known location*/
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap){
        map = googleMap;
        if  (!haveGPSPermission()){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
        else{
            doGPSStuff();
        }

        if(myLocation!=null) {
            if (!startedConvoy) {
                latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            myMarkerOptions.position(latLng).title(name);
            myMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_car_marker));
            marker = map.addMarker(myMarkerOptions);
        }
    }

    /**on start of activity get users last known gps location*/
    @Override
    protected void onStart() {
        super.onStart();
        doGPSStuff();
    }

    /**when this activity stops, clear all stored user information*/
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    /**check if user gave permission*/
    private boolean haveGPSPermission(){
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**if permission is granted, get last known gps location*/
    @SuppressLint("MissingPermission")
    private void doGPSStuff(){
        if (haveGPSPermission())
           myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    /**check the users response for gps permission*/
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            doGPSStuff();
        }
        else if(requestCode == AUDIO_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            try {
                getRecorder();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("wat", "LOK HER3");
        }

    }

    /**this function starts a foreground service and binds LoggedInActivity to the service to enable the passing of info between the two*/
    private void getStartService(){
        Intent intent = new Intent(this, ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**this function ends the foreground service and unbind the service*/
    private void getEndService(){
        Intent intent = new Intent(this, ForegroundService.class);
        unbindService(serviceConnection);
        stopService(intent);
    }

    /**this function join a convoy*/
    @Override
    public void join() {
        /**check whether user started a convoy - can't join another convoy unless you end the one you started*/
        if(startedConvoy){
            Toast.makeText(this, "You already started a convoy", Toast.LENGTH_LONG).show();
            return;
        }
        /**join convoy, if not already joined*/
        if(!joinedConvoy) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
            myDialog.setIcon(R.drawable.joinconvoy);
            myDialog.setTitle("Please enter Convoy Id");
            final EditText convoyInput = new EditText(this);
            convoyInput.setInputType(InputType.TYPE_CLASS_TEXT);
            myDialog.setView(convoyInput);
            myDialog.setPositiveButton("Confirm", (dialog, which) -> {
                String convoyValue = convoyInput.getText().toString().trim();
                if(convoyValue.isEmpty()){
                    Toast.makeText(this, "Convoy Id cannot be empty", Toast.LENGTH_LONG).show();
                }
                else {
                    try{
                        //Toast.makeText(this, "Convoy Input is: " + convoyValue, Toast.LENGTH_LONG).show();
                        //VolleyHelper.getVolleyJoinConvoy(this, "action", "JOIN", convoyValue);
                        getVolleyJoinConvoy(this, "action", "JOIN", convoyValue);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error, please try again...", Toast.LENGTH_LONG).show();
                        return;
                    }

                }

            });
            myDialog.setNegativeButton("Cancel", null);
            myDialog.show();
        }
        else{
            Toast.makeText(this, "You are already in a convoy!", Toast.LENGTH_LONG).show();
        }
    }

    /**this function leaves the convoy*/
    @Override
    public void leave() {
        /**check if convoy was joined*/
        if(startedConvoy){
            Toast.makeText(this, "Sorry you most click the end convoy button to leave...", Toast.LENGTH_LONG).show();
            return;
        }
        /**if convoy joined, then leave convoy*/
        if(joinedConvoy) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
            myDialog.setIcon(R.drawable.endconvoy);
            myDialog.setTitle("Leave Convoy");
            myDialog.setMessage("Are you sure you want to leave convoy?");
            myDialog.setPositiveButton("Confirm", (dialog, which) -> {
                try{
                    VolleyHelper.getVolleyLeaveConvoy(this, "action", "LEAVE");
                    getEndService();
                    joinedConvoy = false;
                    convoyText.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            myDialog.setNegativeButton("Cancel", null);
            myDialog.show();
        }
        else{
            Toast.makeText(this, "You are not in a convoy!", Toast.LENGTH_LONG).show();
        }
    }

    /**this function starts a convoy*/
    @Override
    public void start() {
        /**check if convoy is already started*/
        if(joinedConvoy){
            Toast.makeText(this, "You most leave the convoy you are in before starting one...", Toast.LENGTH_LONG).show();
            return;
        }
        /**if convoy not started, then start, and put in try block to prevent service from starting if error with starting convoy*/
        if(!startedConvoy) {
            //TODO:how to skip code if IllegalArgument exception found - try interface inside volhelper to this class with method call to start service
            try{
               // VolleyHelper.getVolleyStartConvoy(this, "action", "CREATE", name, key, convoyText);
                getVolleyStartConvoy(this, "action", "CREATE", name, key, convoyText);
               // getStartService();
               // startedConvoy = true;
               // endCon.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else{
            Toast.makeText(this, "You already started a convoy!", Toast.LENGTH_LONG).show();
        }
    }

    /**connect to the service*/
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService =  ((ForegroundService.MyLocalBinder) service).getService();
            myService.registerActivity(LoggedInActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**get new locations from ForegroundServices class*/
    @Override
    public void updateLocation(Location value) {
        double lat = value.getLatitude();
        double lon = value.getLongitude();
        LatLng valLatLng = new LatLng(lat, lon);
        try {
            VolleyHelper.getVolleyUpdate(this, "action", "UPDATE", String.valueOf(lat), String.valueOf(lon));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(marker == null){
            myMarkerOptions.position(valLatLng).title("You are here");
            myMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_car_marker));
            myMarkerOptions.rotation(value.getBearing());
            marker = map.addMarker(myMarkerOptions);
        }
        else{
            marker.setPosition(valLatLng);
            marker.setRotation(value.getBearing());
        }

        if(!isConvoy) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(valLatLng, 15));
        }
    }

    /**this function creates a new convoy and also calls the remove method, which removes
     * vehicles that left the convoy from our list
     */
    public void createConvoy(double[] lat, double[] lon, String[] username){
        int numberOfUsers = username.length;
        vehicles = new Vehicle[numberOfUsers];
        for(int i = 0; i < numberOfUsers; i++) {
            if (!checkIfVehicleExist(username[i])) {
                vehicles[i] = new Vehicle(lat[i], lon[i], username[i], map);
                param.put(username[i], vehicles[i]);
            } else {
                param.get(username[i]).updateMarker(lat[i], lon[i]);
            }
        }
        removeVehicle(username);//remove vehicles that left convoy
    }

    /**check if vehicle was already created*/
    public boolean checkIfVehicleExist(String username){
        if(param.containsKey(username)){
            return true;
        }
        return false;
    }

    /**remove vehicles that left the convoy*/
    private void removeVehicle(String[] username){
        int mapSize = param.size();//current convoy size
        int countMapElements = 0;
        String[] mapElementsArray = new String[mapSize];

        /**store new convoy members in a list so I can use "contains" to check
         * if my convoy has old convoy members
         */
        List otherUsersList = Arrays.asList(username);

        for (Map.Entry<String, Vehicle> mapElement : param.entrySet()) {

            //store map elements in an array for easy traversal
            mapElementsArray[countMapElements++] = mapElement.getKey();
        }

        /**loop through the current convoy and remove those members that are not in the new convoy (new users list)*/
        for (int y = 0; y < mapSize; y++) {
            if (!otherUsersList.contains(mapElementsArray[y])) {
                //System.out.println("THIS got removed " + param.remove(mapElementsArray[y]).toString());
            }
        }

    }



    private void updateOthersLocation(double[] lat, double[] lon, String[] username){
       // System.out.println("I CAME HERE first");
        createConvoy(lat, lon, username);

        int userMarkerLength = username.length;
        //TODO: work on isConvoy
        /*if(userMarkerLength != 0){
            isConvoy = true;
        }*/
        Marker[] markers = new Marker[userMarkerLength];
        for (int i = 0; i < userMarkerLength; i++){
            if(param.get(username[i]).getMarker()!=null){
               // if(!param.get(username[i]).getUsername().equals(name)) {
                    Log.d("markename", "NAME IS: " + param.get(username[i]).getUsername());
                    markers[i] = param.get(username[i]).getMarker();
               // }
            }
        }
        /**if there are more than one cars on the map, bound them to see all on map simultaneously*/
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            if(marker!=null) {
                builder.include(marker.getPosition());
            }
        }

        if(markers[0]!=null) {
            Log.d("nullchdk", "NULL CHECK " + markers[0].getPosition());
            builder.include(marker.getPosition());
            LatLngBounds bounds = builder.build();
            if(map == null){
                return;
            }
            try{
                Log.d("cam", "I DID COME HERE == BOUND");
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startDownload(String url) {
       // File file = new File(getExternalFilesDir(null), "New File");

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("New File")
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(getFile()))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

     private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("WHAT IS GOING ON? 1");
            String[] username = intent.getStringArrayExtra("username");
            double[] lat = intent.getDoubleArrayExtra("lat");
            double[] lon = intent.getDoubleArrayExtra("lon");
            String messageUrl = intent.getStringExtra("message_url");
            String messengerName = intent.getStringExtra("username");
            Log.d("for","RECEIVED MESS " + messageUrl);

            if(messageUrl!=null){
                if(messengerName.equals(name)){
                    startDownload(messageUrl);
                }
            }

           // String[] username = {"user1", "user2", "user3"};
           // double[] latt = {39.98122720122026 + counter++, 39.98047908392102+counter++, 49.97994470940666+counter++};
            //double[] lonn = {-75.15802284477685 + counter++, -85.15725036858487+counter++, -75.15726109742086+counter++};
            //updateOthersLocation(lat, lon, username);
            /**TODO: receivedMessage(username, messageUrl)*/

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                if(messageUrl!=null) {
                    Log.d("for","RECEIVED LON IS " + messageUrl);
                    mediaPlayer.setDataSource(messageUrl);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Log.d("for","RECEIVED MESS2 " + messageUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    /**this function uses volley to start a convoy for a user*/
    public void getVolleyStartConvoy(Context context, String action, String actionType, String username, String sessionKey, TextView edit){

        final String URL = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("status");

                        if(result.equals("SUCCESS")) {
                            String convoyId = jsonObject.getString("convoy_id");
                            edit.setText("Convoy Id: " + convoyId);
                            getStartService();
                            startedConvoy = true;
                            endCon.setVisibility(View.VISIBLE);
                            record.setVisibility(View.VISIBLE);
                            // FirebaseMessaging.getInstance().subscribeToTopic(convoyId);
                            FirebaseSubscriptionHelper.subscribe(context, convoyId );
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

    /**this function uses volley to join a convoy for a user*/
    public void getVolleyJoinConvoy(Context context, String action, String actionType, String convoyValue){
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
                            getStartService();
                            joinedConvoy = true;
                            convoyText.setText("Convoy ID: " + convoyValue);
                            //FirebaseMessaging.getInstance().subscribeToTopic(convoyValue);
                            FirebaseSubscriptionHelper.subscribe(context, convoyValue);
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

}