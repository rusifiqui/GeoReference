package com.applus.georeference.editor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.applus.georeference.R;

public class ObjectDetailActivity extends AppCompatActivity {

    private EditText objectName;
    // TODO Crear en la vista el selector de tipo de objeto que se va a crear.
    private Spinner objectType;
    private Spinner objectColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detail);

        objectName = (EditText) findViewById(R.id.editTextObjectName);
        //objectType = (Spinner) findViewById(R.id.spinnerObjectType);
        objectColor = (Spinner) findViewById(R.id.spinnerObjectColor);

        populateSpinners();

    }

    // Método encargado de rellenar los combos.
    private void populateSpinners(){
        populateSpinnerType();
        populateSpinnerColor();
    }

    // Método encargado de rellenar el combo de tipos de objeto.
    private void populateSpinnerType(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.object_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        objectType.setAdapter(adapter);
    }

    // Método encargado de rellenar el combo de colores de objeto.
    private void populateSpinnerColor(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.object_colors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        objectColor.setAdapter(adapter);
    }
}
