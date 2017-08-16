package com.applus.georeference.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.applus.georeference.R;
import com.applus.georeference.entities.Line;
import com.applus.georeference.helpers.DbHelper;

/**
 * Clase que corresponde a la actividad de los datos de las lineas.
 *
 * 14/08/2017 - EVM - Se añade una nueva funcionalidad para que se puedean añadir puntos a lineas
 *                    existentes.
 */
public class LineActivity extends AppCompatActivity {

    private EditText lineName;
    private EditText lineDesc;
    private EditText lineLength;
    private TextView linePointsNum;

    private boolean loading = false;
    private boolean existingLine = false;
    private Button saveLine;
    private Line line;
    private Line recoveredLine;

    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);

        lineName = (EditText) findViewById(R.id.editTextLineName);
        lineDesc = (EditText) findViewById(R.id.editTextLineDesc);
        lineLength = (EditText) findViewById(R.id.editTextLineLength);
        linePointsNum =(TextView) findViewById(R.id.textViewLinePointsNumber);
        saveLine = (Button) findViewById(R.id.buttonSaveLine);

        getData();
        recoveredLine = DbHelper.validateLineExistence(line, getApplicationContext());
        loadData();

        // Si la linea ya existía previamente, se deshabilitan los campos.
        if(existingLine){
            lineName.setEnabled(false);
            lineDesc.setEnabled(false);
            lineLength.setEnabled(false);
            linePointsNum.setEnabled(false);
        }

        if(loading && saveLine != null){
            saveLine.setEnabled(false);
            saveLine.setBackgroundColor(ContextCompat.getColor(LineActivity.this, R.color.colorButtonDisabled));

        }

        lineName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Si se modifica el texto de un campo, se restablece y se habilita el botón guardar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    saveLine.setBackground(getResources().getDrawable(R.drawable.shape, null));
                }
                saveLine.setEnabled(true);
            }
        });

        lineDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Si se modifica el texto de un campo, se restablece y se habilita el botón guardar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    saveLine.setBackground(getResources().getDrawable(R.drawable.shape, null));
                }
                saveLine.setEnabled(true);
            }
        });

        lineLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Si se modifica el texto de un campo, se restablece y se habilita el botón guardar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    saveLine.setBackground(getResources().getDrawable(R.drawable.shape, null));
                }
                saveLine.setEnabled(true);
            }
        });

        if (saveLine != null) {
            saveLine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                }
            });
        }
    }

    /**
     * Método que recupera la información de la linea enviada a la actividad
     */
    private void getData(){
        i = getIntent();

        Bundle parameters = i.getExtras();
        if (parameters != null) {
            if (parameters.containsKey("LINE")) {
                line = (Line) parameters.get("LINE");
            }else{
                // Si no hay datos, se trata de un error.
                setResult(RESULT_CANCELED, i);
                finish();
            }
            if (parameters.containsKey("LOADING")) {
                loading = (boolean) parameters.get("LOADING");
            }
        }
    }

    /**
     * Método que carga la información en los campos de la pantalla
     */
    private void loadData(){

        if(loading || recoveredLine == null) {
            if (line.getName() != null && line.getName().length() > 0) {
                lineName.setText(line.getName());
            }
            if (line.getDesc() != null && line.getDesc().length() > 0) {
                lineDesc.setText(line.getDesc());
            }
            if (line.getLength() > 0) {
                lineLength.setText(String.valueOf(line.getLength()));
            }
            if (line.getPoints() != null && line.getPoints().size() > 0) {
                String t = getResources().getString(R.string.line_points_number) + ": " + line.getPoints().size();
                linePointsNum.setText(t);
            }
        }else{
            existingLine = true;
            lineName.setText(recoveredLine.getName());
            lineDesc.setText(recoveredLine.getDesc());
            lineLength.setText(String.valueOf(recoveredLine.getLength() + line.getLength()));
            String t = getResources().getString(R.string.line_points_number) + ": " + (recoveredLine.getPoints().size()-1 + line.getPoints().size());
            linePointsNum.setText(t);
        }
    }

    /**
     * Método que almacena las propiedades introducidas por el usuario y devuelve el control al mapa.
     */
    private void saveData(){
        // Si se ha recuperado una linea, se debe utilizar este id.
        if(recoveredLine != null)
            line.setIdLine(recoveredLine.getIdLine());

        line.setName(lineName.getText().toString());
        line.setDesc(lineDesc.getText().toString());
        line.setLength(Float.valueOf(lineLength.getText().toString()));
        i.putExtra("LINE", line);
        setResult(RESULT_OK, i);
        finish();
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
