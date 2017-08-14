package com.applus.georeference.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.applus.georeference.R;

public class DatabaseSettingsActivity extends AppCompatActivity {

    private EditText server;
    private EditText user;
    private EditText password;

    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;

    private boolean changedProperties = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_settings);

        server = (EditText) findViewById(R.id.editTextServer);
        user = (EditText) findViewById(R.id.editTextDBServer);
        password = (EditText) findViewById(R.id.editTextPassword);

        // Se recuperan las preferencias.
        prefs = getSharedPreferences("georeference", Context.MODE_PRIVATE);

        // Se recuperan las preferencias que había almacenadas.
        getPreferences();
    }

    /**
     * Método que recupera valores almacenados
     */
    private void getPreferences(){
            if(prefs.contains("server")){
                server.setText(prefs.getString("server", ""));
            }
            if(prefs.contains("dbUser")){
                user.setText(prefs.getString("dbUser", ""));
            }
            if(prefs.contains("dbPassword")){
                password.setText(prefs.getString("dbPassword", ""));
            }
    }


    /**
     * Método que se encarga de almacenar el nombre de usuario
     */
    private void saveServer(){
        changedProperties = true;
        editor = prefs.edit();
        editor.putString("server", server.getText().toString());
        editor.apply();
    }

    /**
     * Método que se encarga de almacenar el nombre de usuario
     */
    private void saveUser(){
        changedProperties = true;
        editor = prefs.edit();
        editor.putString("dbUser", user.getText().toString());
        editor.apply();
    }

    /**
     * Método que se encarga de almacenar el nombre de usuario
     */
    private void savePassword(){
        changedProperties = true;
        editor = prefs.edit();
        editor.putString("dbPassword", password.getText().toString());
        editor.apply();
    }

    /**
     * Métodos sobrescritos para controlar que se guarden las preferencias.
     */
    @Override
    public void onBackPressed() {
        saveServer();
        saveUser();
        savePassword();
        if(changedProperties)
            editor.commit();
        super.onBackPressed();
    }
    @Override
    protected void onStop(){
        saveServer();
        saveUser();
        savePassword();
        if(changedProperties)
            editor.commit();
        super.onStop();
    }
}
