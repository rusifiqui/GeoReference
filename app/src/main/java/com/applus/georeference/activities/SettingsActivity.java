package com.applus.georeference.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.applus.georeference.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch locationSwitch;
    private Switch gpsSwitch;
    private RadioGroup radioMapType;
    private EditText email;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private boolean changedProperties = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sttings);

        locationSwitch = (Switch) findViewById(R.id.switchLocation);
        gpsSwitch = (Switch) findViewById(R.id.switchGPS);

        radioMapType = (RadioGroup) findViewById(R.id.rbGroupMapType);

        email = (EditText) findViewById(R.id.editTextEmail);

        ImageButton dbProperties = (ImageButton) findViewById(R.id.imageButtonDatabaseProperties);

        // Se recuperan las preferencias
        prefs = getSharedPreferences("georeference", Context.MODE_PRIVATE);

        // Se cargan las preferencias almacenadas por el usuario
        getPreferences();

        // Listener para el indicador de localización del usuario
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveLocation(isChecked);
                if(isChecked){
                    gpsSwitch.setEnabled(true);
                }else{
                    gpsSwitch.setChecked(false);
                    gpsSwitch.setEnabled(false);
                }
            }
        });

        // Listener para el indicador de uso de satélites GPS
        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveGps(isChecked);
            }
        });

        // Listener para los radio buttons que indican los tipos de mapa.
        radioMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveMapType(checkedId);
            }
        });

        // Listener para el botón de ajustes de la base de datos.
        dbProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se llama a la actividad de preferencias de la base de datos.
                Intent intent = new Intent(getApplicationContext(), DatabaseSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Método que recupera valores almacenados
     */
    private void getPreferences(){
        if (prefs != null) {
            if(prefs.contains("location")){
                boolean useLocation = prefs.getBoolean("location", false);
                locationSwitch.setChecked(useLocation);
                if(useLocation){
                    gpsSwitch.setEnabled(true);
                }else{
                    gpsSwitch.setEnabled(false);
                }
            }
            if(prefs.contains("gps")){
                gpsSwitch.setChecked(prefs.getBoolean("gps", false));
            }
            if(prefs.contains("map_type")){
                switch (prefs.getInt("map_type", 0)){
                    case 0:
                        radioMapType.check(R.id.radioButtonNormal);
                        break;
                    case 1:
                        radioMapType.check(R.id.radioButtonHybrid);
                        break;
                    case 2:
                        radioMapType.check(R.id.radioButtonSatellite);
                        break;
                    case 3:
                        radioMapType.check(R.id.radioButtonLand);
                        break;
                }
            }
            if(prefs.contains("email")){
                email.setText(prefs.getString("email", ""));
            }
        }
    }

    /**
     * Método que se encarga de almacenar el estado de la preferencia de localización
     * @param active El estado de activación de la localización
     */
    private void saveLocation(boolean active){
        changedProperties = true;
        editor = prefs.edit();
        editor.putBoolean("location", active);
        editor.apply();
    }

    /**
     * Método que se encarga de almacenar el estado de la preferencia de uso de GPS
     * @param active El estado de activación del GPS
     */
    private void saveGps(boolean active){
        changedProperties = true;
        editor = prefs.edit();
        editor.putBoolean("gps", active);
        editor.apply();
    }

    /**
     * Método que se encarga de almacenar el tipo de mapa a mostrar
     * @param id El identificador del radioButton seleccionado.
     */
    private void saveMapType(int id){
        changedProperties = true;
        editor = prefs.edit();
        switch (id){
            case R.id.radioButtonNormal:
                editor.putInt("map_type", 0);
                break;
            case R.id.radioButtonHybrid:
                editor.putInt("map_type", 1);
                break;
            case R.id.radioButtonSatellite:
                editor.putInt("map_type", 2);
                break;
            case R.id.radioButtonLand:
                editor.putInt("map_type", 3);
                break;
        }
        editor.apply();
    }

    /**
     * Método que se encarga de almacenar el email
     */
    private void saveEmail(){
        changedProperties = true;
        editor = prefs.edit();
        editor.putString("email", email.getText().toString());
        editor.apply();
    }

    /**
     * Métodos sobrescritos para controlar que se guarden las preferencias.
     */
    @Override
    public void onBackPressed() {
        saveEmail();
        if(changedProperties)
            editor.commit();
        super.onBackPressed();
    }
    @Override
    protected void onStop(){
        saveEmail();
        if(changedProperties)
            editor.commit();
        super.onStop();
    }
}
