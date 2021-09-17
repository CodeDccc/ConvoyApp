package edu.temple.convoy;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LoggedInActivity extends AppCompatActivity implements OnMapReadyCallback, ConvoyControlFragment.ConvoyInterface, ForegroundService.Update {
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
    private final String CONID = "convoyId";
    private String name;
    private FragmentManager fragmentManager;
    private ConvoyControlFragment convoyControlFragment;
    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Boolean startedConvoy = false;
    Boolean joinedConvoy = false;
    GoogleMap map;
    Marker marker;

    public static SupportMapFragment mapFragment;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.myColor, null)));

        locationManager = getSystemService(LocationManager.class);
        convoyText = findViewById(R.id.convoyText);
        createNotificationChannel();

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
                            VolleyHelper.getVolleyEndConvoy(this, "action", "END");
                            endCon.setVisibility(View.INVISIBLE);
                            getEndService();
                            convoyText.setText("");
                            startedConvoy = false;
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            else{
                Toast.makeText(this, "You did not start a convoy!", Toast.LENGTH_LONG).show();
            }
         });
    }
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
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            map.addMarker(new MarkerOptions().position(latLng).title("You are here"));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        doGPSStuff();
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.clear().apply();
    }
    private boolean haveGPSPermission(){
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressLint("MissingPermission")
    private void doGPSStuff(){
        if (haveGPSPermission())
           myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }


    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            doGPSStuff();
    }

    private void getStartService(){
        Intent intent = new Intent(this, ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void getEndService(){
        Intent intent = new Intent(this, ForegroundService.class);
        unbindService(serviceConnection);
        stopService(intent);
    }


    @Override
    public void join() {
        if(startedConvoy){
            Toast.makeText(this, "You already started a convoy", Toast.LENGTH_LONG).show();
            return;
        }
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
                    joinedConvoy = true;
                    Toast.makeText(this, "Convoy Input is: " + convoyValue, Toast.LENGTH_LONG).show();
                    getStartService();
                    // TODO: convoyText.setText("Convoy ID: " + convoyId);
                }
            });
            myDialog.setNegativeButton("Cancel", null);
            myDialog.show();
        }
        else{
            Toast.makeText(this, "You are already in a convoy!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void leave() {
        if(startedConvoy){
            Toast.makeText(this, "Sorry you most click the end convoy button to leave...", Toast.LENGTH_LONG).show();
            return;
        }
        if(joinedConvoy) {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
            myDialog.setIcon(R.drawable.endconvoy);
            myDialog.setTitle("Leave Convoy");
            myDialog.setMessage("Are you sure you want to leave convoy?");
            myDialog.setPositiveButton("Confirm", (dialog, which) -> {
                getEndService();
                joinedConvoy = false;
                convoyText.setText("");
            });
            myDialog.setNegativeButton("Cancel", null);
            myDialog.show();
        }
        else{
            Toast.makeText(this, "You are not in a convoy!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void start() {
        if(joinedConvoy){
            Toast.makeText(this, "You most leave the convoy you are in before starting one...", Toast.LENGTH_LONG).show();
            return;
        }
        if(!startedConvoy) {
            startedConvoy = true;
            endCon.setVisibility(View.VISIBLE);
            Log.d("TAG", "name: " + name.trim());
            Log.d("TAG", "key: " + key.trim());
            VolleyHelper.getVolleyStartConvoy(this, "action", "CREATE", name, key, convoyText);
            getStartService();
        }
        else{
            Toast.makeText(this, "You already started a convoy!", Toast.LENGTH_LONG).show();
        }
    }


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

    @Override
    public void updateLocation(Location value) {
        LatLng valLatLng = new LatLng(value.getLatitude(), value.getLongitude());
        if(marker == null){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(valLatLng).title("You are here");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_car_marker));
            markerOptions.rotation(value.getBearing());
            marker = map.addMarker(markerOptions);
        }
        else{
            marker.setPosition(valLatLng);
            marker.setRotation(value.getBearing());
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(valLatLng, 17));
    }
}