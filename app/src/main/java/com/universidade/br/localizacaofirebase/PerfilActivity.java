package com.universidade.br.localizacaofirebase;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity
        implements View.OnClickListener {

    //Declaração de variáveis privadas
    private Button btnCadastrar;
    private Button btnAtualizar;
    private Button btnRemover;
    private Button btnPlotar;
    private EditText txtID;
    private EditText txtNome;
    private EditText txtLatitude;
    private EditText txtLongitude;
    private EditText txtServico;
    private Localizacao localizacao;

    //Declaração de variáveis privadas
    private DatabaseReference db;
    private ListView listaView;
    private ArrayList<Usuario> lista;
    private ArrayAdapter<Usuario> adaptador;
    private Usuario usuario;
    private GeoLocation geoLocation;
    private GeoFire geoFire;
    private String id_busca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //Instanciando o banco de dados Firebase
        db = FirebaseDatabase.getInstance().getReference().child("AgendaPet");

        //vinculando os botões da interface
        btnCadastrar = (Button) findViewById(R.id.btnCadastrar);
        btnAtualizar = (Button) findViewById(R.id.btnAtualizar);
        btnRemover = (Button) findViewById(R.id.btnRemover);
        btnPlotar = (Button) findViewById(R.id.btnPlotar);

        //Vinculando os campos de texto
        txtID = (EditText) findViewById(R.id.id);
        txtNome = (EditText) findViewById(R.id.nome);
        txtLatitude = (EditText) findViewById(R.id.latitude);
        txtLongitude = (EditText) findViewById(R.id.longitude);
        txtServico = (EditText) findViewById(R.id.servico);

        //Vinculando o ListView
        listaView = (ListView) findViewById(R.id.lista);

        //Chamando o método listarDados()
        listarDados();

        //adicionando evento ao ListView
        listaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            //Método que é chamado ao clicar na lista
            @Override
            public void onItemClick(AdapterView<?> adaptador,
                                    View view, int i, long l) {
                //Declaração de variáveis
                String nome, latitude, longitude;
                //Ao selecionar um usuario da lista,
                //obtem-se o usuário selecionado adicionando a
                //uma variável do tipo Usuario
                usuario = (Usuario) adaptador.getItemAtPosition(i);

                //tornando o campo id visível
                txtID.setVisibility(View.VISIBLE);
                //desabilita o campo id para edição
                txtID.setEnabled(false);
                //atualizando os campos de textos com
                //valores como id,nome, latitude e longitude
                txtID.setText(usuario.getId());
                txtNome.setText(usuario.getNome());
                localizacao = buscarLocalizacao(usuario.getId());
                txtLatitude.setText(String.valueOf(localizacao.getGeoLocation().latitude));
                txtLongitude.setText(String.valueOf(localizacao.getGeoLocation().longitude));
            }
        });

        //implementando evento nos botões plotar,
        //remover, cadastrar e atualizar
        btnPlotar.setOnClickListener(this);
        btnRemover.setOnClickListener(this);
        btnCadastrar.setOnClickListener(this);
        btnAtualizar.setOnClickListener(this);
    }

    //Método chamado ao clicar os botões
    //da tela de perfil
    @Override
    public void onClick(View v) {
        //Se clicar o botão plotar
        if (v == btnPlotar) {
            //Instancia uma Intent passando como argumento
            //a activity atual e a activity que quer abrir
            Intent intencao = new Intent(PerfilActivity.this, MapsActivity.class);

            ///iniciando a activity*/
            startActivity(intencao);
            //finalizando a activity atual
            finish();
        }
        //Caso o botão seja cadastrar
        if (v == btnCadastrar) {
            //chama o método cadastrarUsuario()
            cadastrarUsuario();
            //chama o método listarDados()
            listarDados();
            //Chama o método apagarCampos
            apagarCampos();
        }

        //Caso o  botão seja atualizar
        if (v == btnAtualizar) {
            //Obtem o valor dos campos id, nome, latitude
            // e longitude

            String chave = db.push().getKey();
            String id = txtID.getText().toString();
            String nome = txtNome.getText().toString();
            double latitude = Double.parseDouble(txtLatitude.getText().toString());
            double longitude = Double.parseDouble(txtLongitude.getText().toString());

            String servico = txtServico.getText().toString().trim();
            GeoLocation localizacao = new GeoLocation(latitude, longitude);

            //Instancia um construtor com parâmetros como
            // chave, nome,latitude e longitude
            Usuario usuario = new Usuario(chave, nome, servico);
            //chama o método atualizarUsuario(usuario)
            //passando o usuário como argumento
            atualizarUsuario(usuario);
            atualizaLocalizacaoDoUsuario(chave, localizacao);
            //chama o método listarDados()
            listarDados();
            //chama o método apagarCampos()
            apagarCampos();
        }


        //Caso o botão selecionado seja remover
        if (v == btnRemover) {
            //obtem o valor do campo de texto id
            //e adiciona o valor na variável id
            String id = txtID.getText().toString();
            //Chama o método removerUsuario(id)
            //passando o id como argumento
            removerUsuario(id);
            removerLocalizacaoUsuario(id);
            //chama o método listarDados()
            listarDados();
            //chama o método apagarCampos
            apagarCampos();
        }
    }

    //Método que apaga os campos de textos
    public void apagarCampos() {
        txtID.getText().clear();
        txtNome.getText().clear();
        txtLatitude.getText().clear();
        txtLongitude.getText().clear();
        txtServico.getText().clear();
    }

    //Método que remove o usuário a partir do id
    public void removerUsuario(String id) {
        //Caso o id não seja nulo
        if (id != null) {
            //atualiza o valor do nó Usuário como nulo a partir do id
            db.child("usuario/" + id).setValue(null);
            //Exibe uma janela Toast avisando que o usuário foi removido
            Toast.makeText(getApplicationContext(), "Usuario removido com sucesso!",
                    Toast.LENGTH_LONG).show();
        }//chama o método listarDados()
        listarDados();
    }

    public void removerLocalizacaoUsuario(String id) {
        //Caso o id não seja nulo
        if (id != null) {
            //atualiza o valor do nó Usuário como nulo a partir do id
            db.child("localizacao/" + id).setValue(null);
            //Exibe uma janela Toast avisando que o usuário foi removido
            Toast.makeText(getApplicationContext(), "Localizacao do Usuario removido com sucesso!",
                    Toast.LENGTH_LONG).show();
        }//chama o método listarDados()
        listarDados();
    }

    //Método que atualiza o usuário a partir do argumento usuário
    public void atualizarUsuario(Usuario usuario) {
        //caso o id do usuário não seja nulo
        if (usuario.getId() != null) {
            //atualiza o valor do nó Usuário a partir do id
            db.child("usuario/" + usuario.getId()).setValue(usuario);
        }
    }

    public void atualizaLocalizacaoDoUsuario(String id_usuario, GeoLocation localizacao) {
        //Cria uma nova instância do Firebase
        db = FirebaseDatabase.getInstance().getReference().child("AgendaPet");;
        //Adiciona a chave que irá ser inserida a uma variável chave

        geoFire = new GeoFire(db);

        geoFire.setLocation("localizacao/"+id_usuario, localizacao, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

                Toast.makeText(getApplicationContext(), "erro no banco de dados", Toast.LENGTH_LONG).show();
            }
        });

    }


    //Método que cadastra o usuário
    private void cadastrarUsuario() {
        //Cria uma nova instância do Firebase
        db = FirebaseDatabase.getInstance().getReference().child("AgendaPet").child("usuario");
        //Adiciona a chave que irá ser inserida a uma variável chave
        String chave = db.push().getKey();
        //Obtem os valores dos campos de textos e
        //adiciona-se o valor as respectivas variáveis
        String nome = txtNome.getText().toString().trim();
        String servico = txtServico.getText().toString().trim();

        Usuario usuario = new Usuario(chave, nome, servico);
        // cadastra o valor do nó Usuário passando como argumento
        //o usuário

        db.child(chave).setValue(usuario);

        //Obtem os valores dos campos de textos e
        //adiciona-se o valor as respectivas variáveis
        double latitude = Double.parseDouble(txtLatitude.getText().toString().trim());
        double longitude = Double.parseDouble(txtLongitude.getText().toString().trim());

        //Instancia um construtor com parâmetros como
        // chave, nome,latitude e longitude

        GeoLocation location = new GeoLocation(latitude, longitude);

        this.cadastrarLocalizacaoDoUsuario(chave, location);
        //chama o método listarDados()
        listarDados();
        //Exibe uma janela Toast avisando que o usuário foi cadastrado
        Toast.makeText(getApplicationContext(), "Usuario cadastrado com sucesso!",
                Toast.LENGTH_LONG).show();
        ;
    }

    public void cadastrarLocalizacaoDoUsuario(String id_usuario, GeoLocation localizacao) {
        //Cria uma nova instância do Firebase
        db = FirebaseDatabase.getInstance().getReference().child("AgendaPet").child("localizacao");
        //Adiciona a chave que irá ser inserida a uma variável chave

        geoFire = new GeoFire(db);

        geoFire.setLocation(id_usuario, localizacao, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

                Toast.makeText(getApplicationContext(), "erro no banco de dados", Toast.LENGTH_LONG).show();
            }
        });

    }


    //Método que busca a localização pelo id do usuário
    public Localizacao buscarLocalizacao(String id_usuario) {
        //Instancia um banco de dados Firebase
        db = FirebaseDatabase.getInstance().getReference();

        //classe Query é utilizada para ler dados
        //a partir do nó usuários
        Query query = db.child("localizacao");

        id_busca = id_usuario;
        //Adiciona um listener no objeto query
        //para "ouvir" mudança de dados
        query.addValueEventListener(new ValueEventListener() {


            //método que é chamado quando ocorre uma mudança
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //verifica se um objeto dataSnapshot existe
                if (dataSnapshot.exists()) {
                    //Em caso positivo, percorre os nós
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                       localizacao = ds.child("localizacao").child(id_busca).getValue(Localizacao.class);


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return localizacao;
    }




    //Método que lista os dados
    public void listarDados() {
        //Instancia um banco de dados Firebase
        db = FirebaseDatabase.getInstance().getReference().child("AgendaPet");
        //Instancia uma ArrayList de Usuario
        lista = new ArrayList<Usuario>();
        //classe Query é utilizada para ler dados
        //a partir do nó usuários

        Query query = db.child("usuario");
        //Adiciona um listener no objeto query
        //para "ouvir" mudança de dados
        query.addValueEventListener(new ValueEventListener() {


            //método que é chamado quando ocorre uma mudança
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //verifica se um objeto dataSnapshot existe
                if (dataSnapshot.exists()) {
                    //Em caso positivo, percorre os nós
                    // for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        usuario = childSnapshot.getValue(Usuario.class);
                      //caso o nome do usuário não seja nulo

                        if (usuario.getNome() != null) {

                            lista.add(usuario);
                        }
                    }
                    //adicionando a lista de usuários a um adaptador
                    adaptador = new ArrayAdapter<Usuario>(getApplicationContext(),
                            android.R.layout.simple_dropdown_item_1line, lista);
                    //passando o adaptador para o ListView
                    listaView.setAdapter(adaptador);
                }
            }

            //Método obrigatório do ValueEventListener
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
