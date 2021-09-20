package edu.temple.convoy;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {
    Notification notification;
    LocationManager locationManager;
    LocationListener locationListener;
    private final IBinder myBinder = new MyLocalBinder();
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
                if (location != null){
                      try{
                          parentActivity.updateLocation(location);

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

    /**this class is to return the service*/
    public class MyLocalBinder extends Binder{
        ForegroundService getService(){
            return ForegroundService.this;
        }
    }

    /**this function is called whenever this service is called*/
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

    /**stop updating location when service is destroyed*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    /**add context to parentactivity*/
    public void registerActivity(Context context){
        parentActivity = (LoggedInActivity) context;
    }

    /** interface to pass info from service to loggedinactivity*/
    interface Update{
        void updateLocation(Location latLng);
    }
}
