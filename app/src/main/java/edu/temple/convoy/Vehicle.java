package edu.temple.convoy;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

public class Vehicle {
    private static final int[] cars = {R.drawable.blue_covertable, R.drawable.blue_limo, R.drawable.red_limo, R.drawable.yellow_car, R.drawable.yellow405_car};
   /* private ArrayList<String> otherUsers = new ArrayList<>();
    ArrayList <MarkerOptions> userMarkerOptions = new ArrayList<>();
    ArrayList <Marker> markers = new ArrayList<>();
    ArrayList <Location> locate = new ArrayList<>();*/
    // ArrayList <LatLng> valLatLng = new ArrayList<>();
    //static GoogleMap map;
    private MarkerOptions userMarkerOptions;
    private Random random;
    GoogleMap map;
    private Marker marker; //= new Marker();
    private Location locate;
    private LatLng latLng;
    double lat;
    double lon;
    private String username;



    public Vehicle(double lat, double lon, String username, GoogleMap map){
        this.lat = lat;
        this.lon = lon;
        this.map = map;
        this.username = username;
        //this.locate = locate;
        //locate = new Location(LocationManager.GPS_PROVIDER);
        random = new Random();
        this.userMarkerOptions = userMarkerOptions;
        setMarker(lat, lon);
    }

    public void setMarker(double lat, double lon){
        if (marker == null) {
            latLng = new LatLng(lat, lon);
            userMarkerOptions = new MarkerOptions();
            userMarkerOptions.position(latLng).title(username);
            userMarkerOptions.icon(BitmapDescriptorFactory.fromResource(cars[random.nextInt(cars.length)]));
            locate = new Location(LocationManager.GPS_PROVIDER);
            locate.setLatitude(lat);
            locate.setLongitude(lon);
            userMarkerOptions.rotation(locate.getBearing());
            marker = map.addMarker(userMarkerOptions);
        } else {
            marker.setPosition(latLng);
            marker.setRotation(locate.getBearing());
        }
        setMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    public String getUsername() {
        return username;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Marker getMarker() {
        Log.d("tagie", "I AM MARKER GETMARKER " + marker.getPosition() );
        return marker;
    }
    public void updateMarker(double lat, double lon){
        setLon(lon);
        setLat(lat);
        latLng = new LatLng(lat, lon);
        locate = new Location(LocationManager.GPS_PROVIDER);
        if (marker == null) {
            userMarkerOptions = new MarkerOptions();
            userMarkerOptions.position(latLng).title(username);
            userMarkerOptions.icon(BitmapDescriptorFactory.fromResource(cars[random.nextInt(cars.length)]));
            locate = new Location(LocationManager.GPS_PROVIDER);
            locate.setLatitude(lat);
            locate.setLongitude(lon);
            userMarkerOptions.rotation(locate.getBearing());
            marker = map.addMarker(userMarkerOptions);
        } else {
            marker.setPosition(latLng);
            //locate = new Location(LocationManager.GPS_PROVIDER);
           // locate.setLatitude(lat);
           // locate.setLongitude(lon);
            //userMarkerOptions.rotation(locate.getBearing());
            marker.setRotation(locate.getBearing());
        }
        setMarker(marker);
        Log.d("markss", "UPMARKER IN VEHICLE" + marker.getPosition());
       // map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

}
