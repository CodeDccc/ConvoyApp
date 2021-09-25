package edu.temple.convoy;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Random;

public class LoggedInActivity extends AppCompatActivity implements OnMapReadyCallback, ConvoyControlFragment.ConvoyInterface, ForegroundService.Update{
    LocationManager locationManager;
    Location myLocation;
    ForegroundService myService;
    private FloatingActionButton endCon;
    private Button logOutBtn;
    private String key;
    private TextView convoyText;
    private LatLng latLng;
    private final String NAME = "username";
    private final String KEY = "sessionKey";
    //private final String CONID = "convoyId";
    private String name;
    private FragmentManager fragmentManager;
    private ConvoyControlFragment convoyControlFragment;
    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Boolean startedConvoy = false;
    Boolean joinedConvoy = false;
    GoogleMap map;
    Marker marker;
    MarkerOptions myMarkerOptions = new MarkerOptions();
    int[] cars = {R.drawable.blue_covertable, R.drawable.blue_limo, R.drawable.red_limo, R.drawable.yellow_car, R.drawable.yellow405_car};
    public static SupportMapFragment mapFragment;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.myColor, null)));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("edu.temple.convoy_FCM"));

        locationManager = getSystemService(LocationManager.class);
        convoyText = findViewById(R.id.convoyText);

        createNotificationChannel();

        //FirebaseMessaging.getInstance().subscribeToTopic("worker");
        //FirebaseSubscriptionHelper.subscribe(this, "worker");

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
        try {
            editor.clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            doGPSStuff();
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
                        VolleyHelper.getVolleyJoinConvoy(this, "action", "JOIN", convoyValue);
                        getStartService();
                        joinedConvoy = true;
                        convoyText.setText("Convoy ID: " + convoyValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error, please try again...", Toast.LENGTH_LONG).show();
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
            //TODO:how to skip code if illegal exception found
            try{
                VolleyHelper.getVolleyStartConvoy(this, "action", "CREATE", name, key, convoyText);
                getStartService();
                startedConvoy = true;
                endCon.setVisibility(View.VISIBLE);
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
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(valLatLng, 15));
    }

    private void updateOthersLocation(double[] lat, double[] lon, String[] username) {
        Random random = new Random();
        int numberOfObjects = lat.length;
        MarkerOptions[] userMarkerOptions = new MarkerOptions[numberOfObjects];
        Marker[] markers = new Marker[numberOfObjects];
        Location[] locate = new Location[numberOfObjects];
        LatLng[] valLatLng = new LatLng[numberOfObjects];
        for (int i = 0; i < numberOfObjects ; i++) {
            if (markers[i] == null) {
                valLatLng[i] = new LatLng(lat[i], lon[i]);
                userMarkerOptions[i] = new MarkerOptions();
                userMarkerOptions[i].position(valLatLng[i]).title(username[i]);
                userMarkerOptions[i].icon(BitmapDescriptorFactory.fromResource(cars[random.nextInt(cars.length)]));
                locate[i] = new Location(LocationManager.GPS_PROVIDER);
                locate[i].setLatitude(lat[i]);
                locate[i].setLongitude(lon[i]);
                userMarkerOptions[i].rotation(locate[i].getBearing());
                markers[i] = map.addMarker(userMarkerOptions[i]);
            } else {
                markers[i].setPosition(valLatLng[i]);
                markers[i].setRotation(locate[i].getBearing());
            }
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
           // Log.d("maker", "marker" + marker.getPosition());
        }
        if(markers[0]!=null) {
           // builder.include(marker.getPosition());
            Log.d("maker", "I CAME HERE" + markers[0].getPosition());
            LatLngBounds bounds = builder.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
        }
    }

     private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] username = intent.getStringArrayExtra("username");
            double[] lat = intent.getDoubleArrayExtra("lat");
            double[] lon = intent.getDoubleArrayExtra("lon");
            updateOthersLocation(lat, lon, username);
        }
    };
}