package com.universidade.br.localizacaofirebase;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by SAEMI on 21/11/2016.
 */

public class LocationService  implements LocationListener {

    // Flag para o status do GPS
    boolean isGPSEnabled = false;

    public final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;

    // Flag para o status da rede
    boolean isNetworkEnabled = false;

    boolean locationServiceAvailable = false;

    // Flag para o status do GPS
    boolean canGetLocation = false;

    //Mínimo da distancia em metros
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    //Minimo de tempo de atualização em milisegundos
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minuto

    private final static boolean forceNetwork = false;

    private static LocationService instance = null;

    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;


    public static LocationService getLocationManager(Context context)     {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * constructor
     */
    private LocationService( Context context )     {

        initLocationService(context);
       Log.e("Log","Serviço de localização criado");
    }



    /**
     * Configura o serviço de localização depois que as permissões são concedidas
     */
    @TargetApi(23)
    private void initLocationService(Context context) {


        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            //
            //Obtem o status do GPS e da rede
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (forceNetwork) isGPSEnabled = false;

            if (!isNetworkEnabled && !isGPSEnabled)    {

                this.locationServiceAvailable = false;
            }

            {
                this.locationServiceAvailable = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null)   {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }
                }

                if (isGPSEnabled)  {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)  {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    }
                }
            }
        } catch (Exception ex)  {
            Log.e( "Log","Error creating location service: " );

        }
    }


    @Override
    public void onLocationChanged(Location location)     {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}