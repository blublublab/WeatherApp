package com.utilitydevs34.luckyweather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class LocationGPS {
    private Location locationObj;
    private final double[] locationGPS = new double[2];
    private static final int TIME_OF_UPDATE = 30;
    private static final int PERMISSIONS_FINE_LOCATION = 25;
    private final int MAX_WAITING_TIME = 5;
    //Google API for location services
    private FusedLocationProviderClient fusedLocationProviderClient;

    public Location updatePGS() {
        //Location request config file
        LocationRequest locationRequest = LocationRequest.create().setFastestInterval(1000 * TIME_OF_UPDATE).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // get  permissions form the user to track GPS
        // get  the current location from the fused client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.getmInstanceActivity());
        if (ActivityCompat.checkSelfPermission(MainActivity.getmInstanceActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //user provided permission

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.getmInstanceActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        locationGPS[0] = location.getLatitude();
                        locationGPS[1] = location.getLongitude();
                        locationObj = location;
                        Log.i("location", "Location get succeeded \nLongtitude: " + locationObj.getLongitude() + "\nLatitude: " + locationObj.getLatitude());

                    }
                }
            }).addOnFailureListener(MainActivity.getmInstanceActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("location", "Location on failure listener");
                    updatePGS();
                }
            });
        } else {
            //request permission
            if (!(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)) {
                Permissions.Request_FINE_LOCATION(MainActivity.getmInstanceActivity(), PERMISSIONS_FINE_LOCATION);
                Log.i("location", "Location is not available");

            }
        }

        return locationObj;
    }
}



