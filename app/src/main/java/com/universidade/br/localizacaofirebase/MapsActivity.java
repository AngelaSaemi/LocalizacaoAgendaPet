package com.universidade.br.localizacaofirebase;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMarkerClickListener {

    //Declaração de variáveis
    private GoogleMap mMap;
    private String nome;
    private double latitude;
    private double longitude;
    private LocalizadorGPS gps;
    private SearchView busca;
    private GeoQuery geoQuery;
    private GeoFire geoFire;
    String servico;
    private Location location;
    private LocationManager locationManager;
    Address endereco = null;
    Geocoder geocoder;
    List<Address> enderecos;
    String[] PERMISSIONS = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS};
    int PERMISSION_ALL = 1;
    private DatabaseReference db;
    Button buscar;
    private  Usuario u ;
    private LatLng minhaLocalizacao;
    ArrayList<MarkerOptions> marcadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mostrarUsuario();

        busca = findViewById(R.id.searchView);
        buscar = findViewById(R.id.buscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String b_servico = busca.getQuery().toString();

                buscarServicosProximos(location, b_servico);

            }

        });

    }

    public void mostrarUsuario() {

        gps = new LocalizadorGPS(MapsActivity.this);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (!temPermissao(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {

        }

        if (gps.getLocation() != null) {

            longitude = gps.getLocation().getLongitude();
            latitude = gps.getLocation().getLatitude();


            } else {
            gps.habilitarGPS();
        }
// Obtem suporte e recebe notificações quando o mapa estiver pronto para ser usado.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //método que configura o mapa
    public void onMapReady(final GoogleMap googleMap) {
        //Instancia um mapa
        mMap = googleMap;
        //Cria um Bundle para receber o pacote
        //Bundle enviado pela classe PerfilActivity
        //Bundle b = getIntent().getExtras();

        //obtem o valor dos objetos enviados
        //empacotados no Bundle

        //configura um listener para o marcador no mapa
        googleMap.setOnMarkerClickListener(this);
        //configura o tipo do mapa
        googleMap.setMapType(googleMap.MAP_TYPE_NORMAL);

        //Obtem a localização a partir da latitude e longitude
        minhaLocalizacao = new LatLng(location.getLatitude(), location.getLongitude());
        //Adiciona um marcador da cor Hue_magenta configurado ao mapa
        //Adiciona um titulo (nome) da localização
        //move a câmera com um zoom 10 vezes na localização

//Adiciona o marcador da cor magenta da localização do usuário
        mMap.addMarker(new MarkerOptions().position(minhaLocalizacao).title("Minha localização"))
                .setIcon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(minhaLocalizacao, 10));


    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    public Usuario buscarServicosProximos(Location meu_local, String b_servico) {

        //Instanciando o banco de dados Firebase
       db = FirebaseDatabase.getInstance().getReference().child("AgendaPet").child("localizacao");
        servico = b_servico;

        //Instanciando o GeoFire passando como argumento o banco de dados
        geoFire = new GeoFire(db);
        //Utiliza o método queryAtLocation para localizar locais em um raio de 5000km
        geoQuery = geoFire.queryAtLocation(new GeoLocation(meu_local.getLatitude(), meu_local.getLongitude()), 5000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                DatabaseReference db_user = FirebaseDatabase.getInstance().getReference().child("AgendaPet").child("usuario");

               final String chave = key;
                 geoFire = new GeoFire(db_user);

                 //Seleciona Petshops a partir do serviço
                Query query = db_user.orderByChild("servico").equalTo(servico);

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Passar os dados para a interface grafica

                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            // TODO: handle the post
                            marcadores = new ArrayList<MarkerOptions>();
                            //Verifica se o id da localização é o mesmo que o id o usuário
                            if(chave.equals(postSnapshot.getKey())){
                            //Captura o objeto Usuario
                                Usuario usuario = postSnapshot.getValue(Usuario.class);
                               //Adiciona o marcador azul na lista de marcadores
                                // com as informações de localização
                                //Nome do petshop e o serviço oferecido
                                marcadores.add(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .anchor(0.5f, 0.5f)
                                        .title(usuario.getNome())
                                        .snippet(usuario.getServico())
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            }

                        }

                        for(int i=0;i<marcadores.size();i++){
                        //Adiciona os marcadores da lista de marcadores no mapa
                            mMap.addMarker(marcadores.get(i));
                            //Aplica o zoom no mapa
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude,location.longitude), 100));

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


        return u;
    }

    public static boolean temPermissao(MapsActivity context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }


        }
        return true;
    }

    public Address buscarEndereco(double latitude, double longitude) throws IOException {


        int responseCount = 0;
        do {
            try {
                Geocoder geocoder;
                geocoder = new Geocoder(getApplicationContext());
                enderecos = geocoder.getFromLocation(latitude, longitude, 1);
                if (enderecos.size() > 0) {
                    endereco = enderecos.get(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                responseCount++;
            }
        } while (endereco == null && responseCount <= 3);
        return endereco;
    }


}
