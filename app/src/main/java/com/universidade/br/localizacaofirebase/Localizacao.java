package com.universidade.br.localizacaofirebase;


import com.firebase.geofire.GeoLocation;

public class Localizacao {

    private String id_localizacao;
    private GeoLocation geoLocation;

    public Localizacao(String id_localizacao,GeoLocation geoLocation){

        this.id_localizacao = id_localizacao;
        this.geoLocation = geoLocation;
    }

    public String getId_localizacao() {
        return id_localizacao;
    }

    public void setId_localizacao(String id_localizacao) {
        this.id_localizacao = id_localizacao;
    }

    public GeoLocation getGeoLocation() {

        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
