package com.applus.georeference.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.applus.georeference.R;
import com.applus.georeference.entities.ObjectProperties;
import com.applus.georeference.entities.ObjectPropertiesData;
import com.applus.georeference.entities.ObjectType;
import com.applus.georeference.entities.ObjectTypes;

import java.util.Vector;

public class SelectObjectActivity extends AppCompatActivity {

    private static final int GET_PROPERTIES = 1;

    private double lat;
    private double lon;
    private int selectedItem;

    private Spinner objectTypes;
    private EditText x;
    private EditText y;

    private ObjectTypes objectTypesNames;
    private ObjectProperties objectPropertiesNames;
    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_object);
        objectTypes = (Spinner) findViewById(R.id.spinnerSelectObject);
        x = (EditText) findViewById(R.id.editTextX);
        y = (EditText) findViewById(R.id.editTextY);
        Button continueButton = (Button) findViewById(R.id.buttonSelectObjectType);
        i = getIntent();

        getExtras();
        populateSpinner(objectTypesNames.getObjectTypes());

        selectedItem = 0;
        objectTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (continueButton != null) {
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), ObjectActivity.class);
                    intent.putExtra("OBJECT_TYPES", objectTypesNames);
                    intent.putExtra("OBJECT_PROPERTIES", objectPropertiesNames);
                    intent.putExtra("SELECTED", selectedItem);
                    startActivityForResult(intent, GET_PROPERTIES);

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PROPERTIES && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if(extras.containsKey("DATA")){
                i.putExtra("DATA", (ObjectPropertiesData)extras.get("DATA"));
            }
            i.putExtra("LAT", lat);
            i.putExtra("LON", lon);
            i.putExtra("SELECTED", selectedItem);
            setResult(RESULT_OK, i);

            // Finalizamos la Activity para volver a la anterior
            finish();

        }
    }

    private void getExtras(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("LAT")) {
                lat = (double) parameters.get("LAT");
                x.setText(String.valueOf(lat));
            }
            if (parameters.containsKey("LON")) {
                lon = (double) parameters.get("LON");
                y.setText(String.valueOf(lon));
            }
            if (parameters.containsKey("OBJECT_TYPES")) {
                objectTypesNames = (ObjectTypes) parameters.get("OBJECT_TYPES");
            }
            if (parameters.containsKey("OBJECT_PROPERTIES")) {
                objectPropertiesNames = (ObjectProperties) parameters.get("OBJECT_PROPERTIES");
            }
        }
    }

    /**
     * Método encargado de cargar los tipos de objetos en el spinner
     * @param o El vector con los objetos.
     */
    private void populateSpinner(Vector<ObjectType> o){

        String[] objects = new String[o.size()];
        for(int i = 0; i < o.size(); i++){
            String objectName = o.get(i).getObjectName();
            objects[i] = objectName;
        }
        // ArrayAdapter para la lista de tipos de objeto
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, android.R.id.text1, objects);
        objectTypes.setAdapter(adapter);
    }

    /**
     * Se controla el botón volver para devolver el control al mapa
     */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, i);
        finish();
    }
}
