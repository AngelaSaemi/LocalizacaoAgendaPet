package com.universidade.br.localizacaofirebase;

import android.location.Location;
import android.webkit.GeolocationPermissions;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

/**
 * Created by SAEMI on 30/10/2017.
 */


public class Usuario {
    //Declaraçao de variaveis privadas
    private String id;
    private String nome;
    private String servico;

    public Usuario(){

    }

       //Construtor com parâmetros
    public Usuario(String id, String n, String se){
        this.id=id;
        this.nome = n;
        this.servico= se;
    }


    public String getServico() {
        return servico;
    }

    public void setServico(String servico) {
        this.servico = servico;
    }

    //Método Gettter
    public String getId() {
        return id;
    }
    // Método setter
    public void setId(String id) {
        this.id = id;
    }

    //Método que retorna um String
    public String toString() {
        return nome ;
    }

    //Método Gettter
    public String getNome() {
        return nome;
    }
    //Método Settter
    public void setNome(String nome) {
        this.nome = nome;
    }


}
