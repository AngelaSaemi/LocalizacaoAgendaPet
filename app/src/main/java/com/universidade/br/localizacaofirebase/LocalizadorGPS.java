package com.universidade.br.localizacaofirebase;

/**
 * Created by SAEMI on 15/11/2016.
 */


import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


public class LocalizadorGPS extends Service implements LocationListener {

    private final Context mContext;

    boolean isGPSEnabled = false;

    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;
    boolean sim = true;
    Location location; // Localização
    double latitude; // Latitude
    double longitude; // Longitude


    // A distância mínima para atualizações de localização em metros
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metros

    // O tempo mínimo para atualização em milisegundos
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minuto

    protected LocationManager locationManager;

    public LocalizadorGPS(Context context) {
        this.mContext = context;
        getLocation();
    }


    public Location getLocation() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
// TODO: Consider calling
// ActivityCompat#requestPermissions
// here to request the missing permissions, and then overriding
// public void onRequestPermissionsResult(int requestCode, String[] permissions,
// int[] grantResults)
// to handle the case where the user grants the permission. See the documentation
// for ActivityCompat#requestPermissions for more details.
        }
        try {

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);


            // Pegando o status do GPS
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Pegando o status da rede de internet
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                this.canGetLocation = true;


                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Internet", "Internet");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // Se o GPS está habilitado, pegar latitude/longitude usando serviços de GPS
                if (isGPSEnabled) {
                    if (location == null) {

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Habilitado", "GPS Habilitado");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();

        }
        return location;

    }


    public void configurarAltaPrecisao() {
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER);

        if (gpsEnabled) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
        } else {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 3);
        }
    }


    /**
     * esta função removerá o uso do GPS
     */
    public void stopUsingGPS() {
        if (locationManager != null) {

            locationManager.removeUpdates(LocalizadorGPS.this);
        }
    }


    /**
     * Método que pega a latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // retorna a latitude
        return latitude;
    }


    /**
     * Método que pega a longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // retorna a longitude
        return longitude;
    }


    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    /**
     * Exibe uma janela de alerta para configuração
     */
    public boolean habilitarInternet() {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("Configuração de Internet ");

        // Aviso para habilitar o Internet
        alertDialog.setMessage("Internet está desabilitada. Você quer habilitá-la?");

        alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent dialogIntent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);

                mContext.startActivity(dialogIntent);

            }
        });

        alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sim = false;
                dialog.cancel();
            }
        });

        alertDialog.show();

        return sim;
    }


    /**
     * Exibe uma janela de alerta para configuração
     */
    public void habilitarGPS() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("Configuração do GPS ");

        // Aviso para habilitar o GPS
        alertDialog.setMessage("GPS está desabilitado. Você quer habilitá-lo?");

        alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
    }


    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}