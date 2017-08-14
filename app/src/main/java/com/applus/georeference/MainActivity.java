package com.applus.georeference;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.applus.georeference.activities.NewProjectActivity;
import com.applus.georeference.editor.ObjectsActivity;
import com.applus.georeference.activities.SelectProjectActivity;
import com.applus.georeference.activities.SettingsActivity;
import com.applus.georeference.entities.ObjectPropertie;
import com.applus.georeference.entities.ObjectProperties;
import com.applus.georeference.entities.ObjectType;
import com.applus.georeference.entities.ObjectTypes;
import com.applus.georeference.helpers.DbHelper;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Vector<ObjectType> objectTypes;
    private Vector<Vector<ObjectPropertie>> objectPropertiesNames;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Objeto para manejar la base de datos
        DbHelper dbHelper = new DbHelper(getApplicationContext(), DbHelper.DATABASE_NAME, null, DbHelper.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();
        loadData();

        Button newProjectButton = (Button) findViewById(R.id.button_new_project);
        Button continueProjectButton = (Button) findViewById(R.id.button_continue_project);
        Button exportButton = (Button) findViewById(R.id.buttonExport);
        Button settingsButton = (Button) findViewById(R.id.button_settings);
        ImageButton objectEditor = (ImageButton) findViewById(R.id.imageButtonObjectSettings);

        // TODO Se oculta el botón de edición de objetos
        // objectEditor.setVisibility(View.INVISIBLE);

        if (settingsButton != null) {
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (newProjectButton != null) {
            newProjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), NewProjectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            });
        }

        if (continueProjectButton != null) {
            continueProjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), SelectProjectActivity.class);
                    ObjectTypes ots = new ObjectTypes();
                    ots.setObjectTypes(objectTypes);
                    ObjectProperties opts = new ObjectProperties();
                    opts.setProperties(objectPropertiesNames);

                    intent.putExtra("OBJECT_TYPES", ots);
                    intent.putExtra("OBJECT_PROPERTIES", opts);
                    startActivity(intent);
                }
            });
        }

        if (exportButton != null) {
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), SelectProjectActivity.class);
                    ObjectTypes ots = new ObjectTypes();
                    ots.setObjectTypes(objectTypes);
                    ObjectProperties opts = new ObjectProperties();
                    opts.setProperties(objectPropertiesNames);

                    intent.putExtra("OBJECT_TYPES", ots);
                    intent.putExtra("OBJECT_PROPERTIES", opts);
                    intent.putExtra("EXPORT_DATA_MODE", true);
                    startActivity(intent);
                }
            });
        }

        if(objectEditor != null){
            objectEditor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ObjectsActivity.class);
                    ObjectTypes ots = new ObjectTypes();
                    ots.setObjectTypes(objectTypes);
                    ObjectProperties opts = new ObjectProperties();
                    opts.setProperties(objectPropertiesNames);
                    intent.putExtra("OBJECT_TYPES", ots);
                    intent.putExtra("OBJECT_PROPERTIES", opts);
                    startActivity(intent);
                }
            });
        }



        db.close();
    }

    /**
     * Método encargado de recuperar los tipos de objectos existentes y sus propiedades.
     */
    private void loadData(){
        objectTypes = DbHelper.getObjectTypes(getApplicationContext());
        Vector<ObjectPropertie> objectProperties = DbHelper.getObjectProperties(getApplicationContext());
        objectPropertiesNames = new Vector<>();
        long objectNum = objectProperties.get(0).getIdObject();

        Vector<ObjectPropertie> op = new Vector<>();
        for(int i = 0; i < objectProperties.size(); i++){
            if(objectNum == objectProperties.get(i).getIdObject()){
                op.add(objectProperties.get(i));
                if(i == objectProperties.size()-1){
                    objectPropertiesNames.add(op);
                }
            }else{
                objectNum = objectProperties.get(i).getIdObject();
                objectPropertiesNames.add(op);
                op = new Vector<>();
                op.add(objectProperties.get(i));
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(db.isOpen())
            db.close();
    }

    /**
     * Método para controlar el botón de "VOLVER".
     * Se solicita que el usuario haga doble click para salir de la app.
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
