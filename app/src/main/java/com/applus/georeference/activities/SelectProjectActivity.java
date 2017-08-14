package com.applus.georeference.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.applus.georeference.R;
import com.applus.georeference.entities.ObjectPropertie;
import com.applus.georeference.entities.ObjectProperties;
import com.applus.georeference.entities.ObjectType;
import com.applus.georeference.entities.ObjectTypes;
import com.applus.georeference.entities.Project;
import com.applus.georeference.helpers.DbHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import static com.applus.georeference.helpers.DbHelper.DATABASE_NAME;
import static com.applus.georeference.helpers.DbHelper.DATABASE_VERSION;

public class SelectProjectActivity extends AppCompatActivity {

    // Longitud de la descripción del proyecto
    private static final int DESCRITION_LENGTH = 25;

    private ListView projectList;
    private Vector<Long> projectIds;
    private Vector<String> projectNames;
    private ObjectTypes objectTypes;
    private ObjectProperties objectPropertiesNames;
    private String email = "";

    // Variable para indicar si la actividad ha sido creada en modo de exportación de proyecto.
    private boolean exportMode = false;

    // Variable para almacenar el id del proyecto seleccionado por el usuario.
    private long selectedProjectId;

    // Variable para almacenar el nombre del proyecto seleccionado por el usuario.
    private String selectedProjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_project);
        projectList = (ListView) findViewById(R.id.listViewSelectProyect);

        loadData();

        // Si la actividad está en modo de exportación, se modifica el título.
        if(exportMode){
            this.setTitle(getResources().getString(R.string.title_activity_export_project));
            if(email.equals("")){
                showNoEmailDialog();
            }
        }

        final Vector<Project> projects = DbHelper.getExistingProjects(getApplicationContext());
        if(projects != null)
            populateList(projects);

        // Se guarda el formulario seleccionado y se indica que se ha realizado una selección.
        projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedProjectId = projectIds.get(position);
                selectedProjectName = projectNames.get(position);

                if((projects != null ? projects.get(position) : null) != null) {
                    if(!exportMode) {
                        showSelectionDialog(projects.get(position));
                    }else{
                        showExportDialog(projects.get(position));
                    }
                }
            }
        });
    }

    /**
     * Método encargado de generar la lista con los proyectos existentes
     * @param p Vector con los proyectos recuperados de la base de datos
     */
    private void populateList(Vector<Project> p){
        projectIds = new Vector<>();
        projectNames = new Vector<>();
        String[] projects = new String[p.size()];

        for(int i = 0; i < p.size(); i++){
            projectIds.add(p.get(i).getProjectId());
            projectNames.add(p.get(i).getProjectName());
            String projectText = getResources().getText(R.string.date) + ": " + p.get(i).getCreateDate() + "\n" + getResources().getText(R.string.name) +
                    ": " + p.get(i).getProjectName() + "\n" + getResources().getText(R.string.desc) + ": ";
            if(p.get(i).getProjectDesc().length() > DESCRITION_LENGTH){
                projectText = projectText.concat(p.get(i).getProjectDesc().substring(0, DESCRITION_LENGTH-1) + "...");
            }else{
                projectText = projectText.concat(p.get(i).getProjectDesc());
            }
            projects[i] = projectText;
        }
        // ArrayAdapter para la lista de elementos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, projects);
        projectList.setAdapter(adapter);
    }

    /**
     * Método que muestra un mensaje al usuario indicando que se va a cargar el proyecto
     * @param p Proyecto a cargar
     */
    private void showSelectionDialog(Project p){
        final AlertDialog.Builder builder = new AlertDialog.Builder(SelectProjectActivity.this);
        builder.setTitle(R.string.dialog_alert).setMessage(getResources().getString(R.string.dialog_text) + ": " + p.getProjectDesc())
                .setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Se llama a la vista del mapa enviando como parámetro el identificador del proyecto
                        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                        intent.putExtra("ID_PROJECT", selectedProjectId);
                        intent.putExtra("OBJECT_TYPES", objectTypes);
                        intent.putExtra("OBJECT_PROPERTIES", objectPropertiesNames);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Método que muestra un mensaje al usuario indicando que se va a exportar el proyecto
     * @param p Proyecto a exportar
     */
    private void showExportDialog(Project p){
        final AlertDialog.Builder builder = new AlertDialog.Builder(SelectProjectActivity.this);
        builder.setTitle(R.string.dialog_alert).setMessage(getResources().getString(R.string.dialog_export_text) + ": " + p.getProjectDesc())
                .setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        generateExcelFiles();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Método que muestra un mensaje al usuario indicando que se va a exportar el proyecto
     */
    private void showNoEmailDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(SelectProjectActivity.this);
        builder.setTitle(R.string.dialog_alert).setMessage(getResources().getString(R.string.no_email))
                .setPositiveButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Método que carga los parámentros recibidos por la actividad.
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
            if (parameters.containsKey("EXPORT_DATA_MODE")) {
                exportMode = (boolean) parameters.get("EXPORT_DATA_MODE");
            }
        }
        SharedPreferences prefs = getSharedPreferences("georeference", Context.MODE_PRIVATE);
        if (prefs != null) {
            if (prefs.contains("email")) {
                email = prefs.getString("email", "");
            }
        }
    }

    /**
     * Método encargado de la exportación de proyectos
     */
    private void generateExcelFiles(){
        Vector<ObjectType> ot = objectTypes.getObjectTypes();
        ArrayList<Uri> paths = new ArrayList<>();
        DbHelper dbHelper = new DbHelper(getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SelectProjectActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return;
        }

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/georeference/" + selectedProjectName.trim());

        boolean existDir = true;
        // Si el directorio no existe, se crea
        if(!directory.isDirectory()){
            existDir = directory.mkdirs();
        }

        if(existDir) {
            // Se creará un archivo por cada tipo de objeto
            for (int i = 0; i < ot.size(); i++) {
                String fileName = ot.get(i).getObjectName() + ".xls";
                File file = new File(directory, fileName);
                paths.add(Uri.fromFile(file));
                int row = 0;

                WorkbookSettings wbSettings = new WorkbookSettings();
                wbSettings.setLocale(new Locale("en", "EN"));
                WritableWorkbook workbook;
                try {
                    // Se crea un libro
                    workbook = Workbook.createWorkbook(file, wbSettings);
                    // Se crea una hoja
                    WritableSheet sheet = workbook.createSheet(ot.get(i).getObjectName(), 0);

                    try {
                        sheet.addCell(new Label(0, 0, "ID_OBJECT"));
                        sheet.addCell(new Label(1, 0, "FECHA_DE_CREACION"));
                        sheet.addCell(new Label(2, 0, "X"));
                        sheet.addCell(new Label(3, 0, "Y"));
                        // Se genera la cabecera
                        Vector<ObjectPropertie> op = objectPropertiesNames.getProperties().get(i);
                        int j;
                        for (j = 0; j < op.size(); j++) {
                            sheet.addCell(new Label(j + 4, 0, op.get(j).getPropertieName()));
                        }

                        // Se recuperan los elementos del tipo actual
                        // Se recuperan las filas
                        try {
                            c = db.rawQuery("SELECT UO.ID_USER_OBJECT, UO.CREATE_DATE, UO.LAT, UO.LON, UOP.VALUE" +
                                    " FROM user_object_properties uop, user_object uo" +
                                    " where uo.id_user_object = uop.id_user_object" +
                                    " and value is not null and type = " + i + " and uo.id_project = " + selectedProjectId, null);
                            boolean firstLine = true;
                            long currentObject = 0;
                            int lastCol = 0;
                            if (c != null) {
                                while (c.moveToNext()) {
                                    if (firstLine) {
                                        firstLine = false;
                                        currentObject = c.getLong(0);
                                        row++;
                                        sheet.addCell(new Label(0, row, c.getString(0)));
                                        sheet.addCell(new Label(1, row, c.getString(1)));
                                        sheet.addCell(new Label(2, row, c.getString(2)));
                                        sheet.addCell(new Label(3, row, c.getString(3)));
                                        sheet.addCell(new Label(4, row, c.getString(4)));
                                        lastCol = 5;
                                    } else {
                                        if (currentObject == c.getLong(0)) {
                                            sheet.addCell(new Label(lastCol, row, c.getString(4)));
                                            lastCol++;
                                        } else {
                                            currentObject = c.getLong(0);
                                            row++;
                                            sheet.addCell(new Label(0, row, c.getString(0)));
                                            sheet.addCell(new Label(1, row, c.getString(1)));
                                            sheet.addCell(new Label(2, row, c.getString(2)));
                                            sheet.addCell(new Label(3, row, c.getString(3)));
                                            sheet.addCell(new Label(4, row, c.getString(4)));
                                            lastCol = 5;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                    workbook.write();
                    try {
                        workbook.close();
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Se exportan las lineas que existen en el proyecto.
            try{
                c = db.rawQuery("SELECT distinct(l.id_line) LINEA, l.Line_name, l.line_length, l.line_desc,\n" +
                        "(select lat from user_object  where id_user_object =" +
                        "   (select id_user_object from points where id_point =" +
                        "       (select min(id_point) from points where id_line = l.id_line))) AS LAT_INI,\n" +
                        "(select lon from user_object  where id_user_object =" +
                        "   (select id_user_object from points where id_point =" +
                        "       (select min(id_point) from points where id_line = l.id_line))) AS LON_INI,\n" +
                        "(select lat from user_object  where id_user_object =" +
                        "   (select id_user_object from points where id_point =" +
                        "       (select max(id_point) from points where id_line = l.id_line))) AS LAT_FIN,\n" +
                        "(select lon from user_object  where id_user_object =" +
                        "   (select id_user_object from points where id_point =" +
                        "       (select max(id_point) from points where id_line = l.id_line))) AS LON_FIN\n" +
                        "FROM lines l where id_project = " + selectedProjectId, null);
                WritableWorkbook workbook = null;
                if (c != null) {
                    String fileName = getResources().getString(R.string.lines_file_name) + ".xls";
                    File file = new File(directory, fileName);
                    paths.add(Uri.fromFile(file));
                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale("en", "EN"));
                    // Se crea un libro
                    workbook = Workbook.createWorkbook(file, wbSettings);
                    // Se crea una hoja
                    WritableSheet sheet = workbook.createSheet(getResources().getString(R.string.lines_file_name), 0);
                    // Se genera la cabecera del fichero
                    sheet.addCell(new Label(0, 0, "ID_LINE"));
                    sheet.addCell(new Label(1, 0, "NOMBRE"));
                    sheet.addCell(new Label(2, 0, "LONGITUD"));
                    sheet.addCell(new Label(3, 0, "DESC"));
                    sheet.addCell(new Label(4, 0, "LAT_INI"));
                    sheet.addCell(new Label(5, 0, "LON_INI"));
                    sheet.addCell(new Label(6, 0, "LAT_FIN"));
                    sheet.addCell(new Label(7, 0, "LON_FIN"));

                    // Se recorre el resultado de la consulta.
                    int row = 1;
                    while (c.moveToNext()) {
                        sheet.addCell(new Label(0, row, c.getString(0)));
                        sheet.addCell(new Label(1, row, c.getString(1)));
                        sheet.addCell(new Label(2, row, c.getString(2)));
                        sheet.addCell(new Label(3, row, c.getString(3)));
                        sheet.addCell(new Label(4, row, c.getString(4)));
                        sheet.addCell(new Label(5, row, c.getString(5)));
                        sheet.addCell(new Label(6, row, c.getString(6)));
                        sheet.addCell(new Label(7, row, c.getString(7)));
                        row++;
                    }
                }
                workbook.write();
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null)
                    c.close();
                if (db != null)
                    db.close();
            }

            // Se envían los ficheros por correo
            if (paths.size() > 0) {
                final Intent ei = new Intent(Intent.ACTION_SEND_MULTIPLE);
                ei.setType("plain/text");
                ei.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                ei.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.project_export) + " " + selectedProjectName);
                ei.putParcelableArrayListExtra(Intent.EXTRA_STREAM, paths);
                startActivityForResult(Intent.createChooser(ei, getResources().getString(R.string.select_email_app)), 1);
            }
        }
    }

}
