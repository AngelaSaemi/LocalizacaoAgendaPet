package com.universidade.br.localizacaofirebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    //Declaração de variável
    Button btnPerfil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        //Vinculando o botão ir para o perfil


        btnPerfil = (Button) findViewById(R.id.btnPerfil);

        //Adicionando evento ao botão
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Criando uma Intent para navegar até a tela PerfilActivity
                Intent intencao = new Intent(getApplicationContext(),
                        PerfilActivity.class);
                //Iniciar a Intent
                startActivity(intencao);
            }
        });
    }
}
