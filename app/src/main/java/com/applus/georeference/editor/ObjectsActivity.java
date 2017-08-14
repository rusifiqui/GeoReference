package com.applus.georeference.editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.applus.georeference.R;
import com.applus.georeference.activities.MapActivity;
import com.applus.georeference.entities.ObjectTypes;
import com.applus.georeference.helpers.SyncHelper;

public class ObjectsActivity extends AppCompatActivity {

    private ListView objectsList;
    private ObjectTypes objectTypes;
    private boolean deleting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objects);
        ImageButton addObject = (ImageButton) findViewById(R.id.imageButtonAddObject);
        ImageButton deleteObject = (ImageButton) findViewById(R.id.imageButtonDeleteObject);
        objectsList = (ListView) findViewById(R.id.objectsList);

        // Se recuperan los objetos existentes en la base de datos de la aplicación.
        loadData();

        // Listener para el botón de creación de elementos
        addObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ObjectDetailActivity.class);
                intent.putExtra("CREATE", true);
                startActivity(intent);
            }
        });

        deleteObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se activa o desactiva el borrado de elementos existentes.
                deleting = !deleting;
                // Si se activa el modo de borrado, se indica al usuario que seleccione el elemento a eliminar.
                if(deleting)
                    Toast.makeText(ObjectsActivity.this, R.string.delete_object, Toast.LENGTH_SHORT).show();
            }
        });

        objectsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(deleting){
                    // TODO pasar al método el id, la descripción o lo que sea necesario para eliminar el tipo de objeto seleccionado por el usuario.
                    showDeleteDialog();
                }else{
                    // TODO Llamar a la actividad de edición del elemento.
                }
            }
        });
    }

    /**
     * Método encargado de cargar los datos iniciales de la pantalla.
     */
    private void loadData(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("OBJECT_TYPES")) {
                objectTypes = (ObjectTypes) parameters.get("OBJECT_TYPES");
                populateList();
            }
        }
    }

    /**
     * Método encargado de generar la lista con los objetos existentes.
     */
    private void populateList(){

        String[] objects = new String[objectTypes.getObjectTypes().size()];

        for(int i = 0; i < objectTypes.getObjectTypes().size(); i++){
            objects[i] = objectTypes.getObjectTypes().get(i).getObjectName();
        }
        // ArrayAdapter para la lista de elementos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, objects);
        objectsList.setAdapter(adapter);
    }

    /**
     * Método encargado de validar si el usuario desea eliminar un tipo de objeto.
     */
    private void showDeleteDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ObjectsActivity.this);
        builder.setTitle(R.string.dialog_alert_map).setMessage(getResources().getString(R.string.dialog_delete_object_message))
                .setPositiveButton(R.string.dialog_delete_confirmation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO Eliminar el elemento seleccionado.
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_map, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
