package com.applus.georeference.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applus.georeference.R;
import com.applus.georeference.entities.ObjectPropertie;
import com.applus.georeference.entities.ObjectProperties;
import com.applus.georeference.entities.ObjectPropertiesData;
import com.applus.georeference.entities.ObjectType;
import com.applus.georeference.entities.ObjectTypes;
import com.applus.georeference.helpers.DbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

public class ObjectActivity extends AppCompatActivity {

    private LinearLayout elements;
    private ObjectTypes objectTypes;
    private ObjectProperties objectPropertiesNames;
    private Vector<EditText> fields;
    private boolean loading = false;
    private Button save;

    private DatePickerDialog fromDatePickerDialog;

    private final String myFormat = "dd/MM/yyyy";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(myFormat, Locale.US);

    private int selected;
    private long objectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);

        save = (Button) findViewById(R.id.buttonSaveProperties);
        elements = (LinearLayout) findViewById(R.id.linearLayoutProperties);
        fields = new Vector<>();
        loadData();
        loadView();

        // Si es una carga, se cargan los datos de la base de datos
        if(loading){
            populateFields();
            // Si cargamos datos, el botón guardar no se habilitará si no se realiza alguna
            // modificación en alguno de los campos (se pulsa una tecla).
            save.setEnabled(false);
            save.setBackgroundColor(ContextCompat.getColor(ObjectActivity.this, R.color.colorButtonDisabled));
        }

        // Listener para el botón de guardar
        if (save != null) {
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = getIntent();
                    // Si el botón no está activo, no se ha hecho ningún cambio
                    if(save.isEnabled()) {
                        setResult(RESULT_OK, i);
                        i.putExtra("DATA", generateObjectData());
                    }else{
                        setResult(RESULT_CANCELED, i);
                    }
                    finish();
                }
            });
        }
    }

    /**
     * Método que recupera los parámetros recibidos por la actividad.
     */
    private void loadData(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("OBJECT_TYPES")) {
                objectTypes = (ObjectTypes) parameters.get("OBJECT_TYPES");
            }
            if (parameters.containsKey("OBJECT_PROPERTIES")) {
                objectPropertiesNames = (ObjectProperties) parameters.get("OBJECT_PROPERTIES");
            }
            if (parameters.containsKey("SELECTED")) {
                selected = (int) parameters.get("SELECTED");
            }
            if (parameters.containsKey("OBJECT_ID")) {
                objectId = (long) parameters.get("OBJECT_ID");
                loading = true;
            }
        }
    }

    /**
     * Método encargado de generar los elementos de la actividad. Añade las etiquetas con los nombres
     * de lo campos y los textfields para mostrar e introducir la información.
     */
    private void loadView(){

        Vector<ObjectPropertie> p = objectPropertiesNames.getProperties().get(selected);

        for(int i = 0; i < p.size(); i++){
            TextView a = new TextView(ObjectActivity.this);
            a.setText(p.get(i).getPropertieName());
            final EditText e = new EditText(ObjectActivity.this);
            // En función del tipo de dato se permite un método de entrada u otro.
            switch (p.get(i).getPropertieType()){
                case 0:
                    e.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case 1:
                    e.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                case 2:
                    e.setInputType(InputType.TYPE_NULL);
                    e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                Calendar newCalendar = Calendar.getInstance();
                                fromDatePickerDialog = new DatePickerDialog(ObjectActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        Calendar newDate = Calendar.getInstance();
                                        newDate.set(year, monthOfYear, dayOfMonth);
                                        String s = dateFormatter.format(newDate.getTime());
                                        e.setText(s);
                                    }
                                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                                fromDatePickerDialog.show();
                            }
                        }
                    });
                    break;
            }
            if(loading){
                e.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Si se modifica el texto de un campo, se restablece y se habilita el botón guardar
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            save.setBackground(getResources().getDrawable(R.drawable.shape, null));
                        }
                        save.setEnabled(true);
                    }
                });
            }
            fields.add(e);
            elements.addView(a);
            elements.addView(e);
        }
    }

    /**
     * Método que genera toda la información de un objeto.
     * @return el objeto completo.
     */
    private ObjectPropertiesData generateObjectData(){
        // Obtenemos el tipo de objetos y las propiedades a guardar
        ObjectType ot = objectTypes.getObjectTypes().get(selected);
        Vector<ObjectPropertie> p = objectPropertiesNames.getProperties().get(selected);
        Vector<String> values = new Vector<>();
        for(int i = 0; i < fields.size(); i++){
            values.add(fields.get(i).getText().toString());
        }
        ObjectPropertiesData opd = new ObjectPropertiesData();
        opd.setIdObject(objectId);
        opd.setOt(ot);
        opd.setP(p);
        opd.setValues(values);
        return opd;
    }

    /**
     * Método que carga la información de las propiedades de la base de datos
     */
    private void populateFields(){
        Vector<String> values = DbHelper.loadObjectProperties(objectId, getApplicationContext());
        for(int i = 0; i < fields.size(); i++){
            fields.get(i).setText(values.get(i));
        }
    }
}
