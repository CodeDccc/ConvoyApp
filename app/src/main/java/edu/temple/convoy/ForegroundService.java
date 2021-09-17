package edu.temple.convoy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.temple.convoy.LoggedInActivity;
import edu.temple.convoy.R;


public class ForegroundService extends Service {
    LatLng latLng;
    float latitude;
    float longitude;
    Notification notification;
    LocationManager locationManager;
    LocationListener locationListener;
    Location myLocation;
    private final IBinder myBinder = new MyLocalBinder();
    //Context context;
    private Update parentActivity;

    public ForegroundService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (location != null) {
                      latLng = new LatLng(location.getLatitude(), location.getLongitude());
                      try{
                          parentActivity.updateLocation(latLng);

                      } catch (Exception e) {
                          e.printStackTrace();
                          Log.d("Print", String.valueOf(e));
                      }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
    }

    public class MyLocalBinder extends Binder{
        ForegroundService getService(){
            return ForegroundService.this;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notification = (new NotificationCompat.Builder(this, "convoy"))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setChannelId("convoy")
                .setContentTitle("Convoy started")
                .setContentText("You have just started a convoy")
                .build();

        startForeground(333, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    public void registerActivity(Context context){
        parentActivity = (LoggedInActivity) context;
       // LatLng latLng = null;
        //parentActivity.updateLocation(latLng);
    }

    interface Update{
        void updateLocation(LatLng latLng);
    }
}
/*private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationService = ((ForegroundLocationService.LocalBinder) service).getService();
            locationService.registerActivity(ConvoyActivity.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
            isBound = false;
        }
    };

    public void startLocationService() {
        if (!isBound) {
            Intent serviceIntent = new Intent(this, ForegroundLocationService.class);
            startService(serviceIntent);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void stopLocationService() {
        if (isBound) {
            Intent serviceIntent = new Intent(this, ForegroundLocationService.class);
            unbindService(serviceConnection);
            stopService(serviceIntent);
            isBound = false;
        }
    }
and then I also have these in my service which are used in the binding:
@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return the local binder
        isBound = true;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    public void registerActivity(Context activity) {
        boundActivity = activity;
    }

    public class LocalBinder extends Binder {
        //return the service itself
        ForegroundLocationService getService() {
            return ForegroundLocationService.this;
        }
    }*/