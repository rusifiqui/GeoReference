package com.applus.georeference.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.applus.georeference.R;
import com.applus.georeference.entities.Line;
import com.applus.georeference.entities.ObjectPropertie;
import com.applus.georeference.entities.ObjectPropertiesData;
import com.applus.georeference.entities.ObjectType;
import com.applus.georeference.entities.Project;
import com.applus.georeference.entities.UserObject;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;


import uk.me.jstott.jcoord.UTMRef;

/**
 * Clase para la gestión de la base de datos.
 * @author jvilam
 * @version 1
 * @since 02/09/2016
 *
 * 14/08/2017 - EVM - Se modifican los métodos getIdUserObject y setIdUserObject por un error ortográfico en el nombre.
 *                    Creación de los métodos "validateLineExistence" y "getLineData".
 */
public class DbHelper extends SQLiteOpenHelper {

    private final Context c;
    public static final String DATABASE_NAME = "APPLUSGEO.db";
    public static final int DATABASE_VERSION = 1;

    // Tablas

    // Definición de objetos
    private static final String DATABASE_TABLE_OBJECTS = "OBJECTS";
    private static final String DATABASE_TABLE_OBJECT_PROPERTIES = "OBJECT_PROPERTIES";

    // Proyectos
    private static final String DATABASE_TABLE_PROJECT = "PROJECT";

    // Objetos creados
    private static final String DATABASE_TABLE_USER_OBJECT = "USER_OBJECT";
    private static final String DATABASE_TABLE_USER_OBJECT_PROPERTIES = "USER_OBJECT_PROPERTIES";
    private static final String DATABASE_TABLE_LINES = "LINES";
    private static final String DATABASE_TABLE_POINTS = "POINTS";

    // FIN Tablas

    // Atributos

    // Atributos comunes
    private static final String KEY_CREATE_DATE = "CREATE_DATE";

    // Atributos de la tabla PROJECT
    private static final String KEY_ID_PROJECT = "ID_PROJECT";
    private static final String KEY_PROJECT_NAME = "PROJECT_NAME";
    private static final String KEY_PROJECT_DESCRIPTION = "PROJECT_DESCRIPTION";

    // Atributos de la tabla OBJECTS
    private static final String KEY_ID_OBJECT = "ID_OBJECT";
    private static final String KEY_OBJECT_NAME = "OBJECT_NAME";

    // Atributos de la tabla OBJECT_PROPERTIES
    private static final String KEY_ID_OBJECT_PROPERTIE = "ID_OBJECT_PROPERTIE";
    private static final String KEY_OBJECT_PROPERTIE_NAME = "ID_OBJECT_PROPERTIE_NAME";
    private static final String KEY_OBJECT_PROPERTIE_TYPE = "ID_OBJECT_PROPERTIE_TYPE";

    // Atributos de la tabla USER_OBJECT
    private static final String KEY_ID_USER_OBJECT = "ID_USER_OBJECT";
    private static final String KEY_LAT = "LAT";
    private static final String KEY_LON = "LON";
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_DESC = "DESCRIPTION";

    // Atributos de la tabla USER_OBJECT_PROPERTIES
    private static final String KEY_ID_USER_OBJECT_PROPERTIE = "ID_USER_OBJECT_PROPERTIE";
    private static final String KEY_VALUE = "VALUE";

    // Atributos de la tabla LINE
    private static final String KEY_ID_LINE = "ID_LINE";
    private static final String KEY_ID_LINE_NAME = "LINE_NAME";
    private static final String KEY_ID_LINE_LENGTH = "LINE_LENGTH";
    private static final String KEY_ID_LINE_DESC = "LINE_DESC";

    // Atributos de la tabla POINTS
    private static final String KEY_ID_POINT = "ID_POINT";

    //FIN Atributos

    // Sentencia SQL para crear la tabla PROJECT
    private static final String DATABASE_CREATE_PROJECT = "create table " +
            DATABASE_TABLE_PROJECT + " (" + KEY_ID_PROJECT +
            " integer primary key autoincrement, " +
            KEY_CREATE_DATE + " date, " +
            KEY_PROJECT_NAME + " text not null, " +
            KEY_PROJECT_DESCRIPTION + " text not null);";

    // Sentencia SQL para crear la tabla OBJECTS
    private static final String DATABASE_CREATE_OBJECTS = "create table " +
            DATABASE_TABLE_OBJECTS + " (" + KEY_ID_OBJECT +
            " integer primary key autoincrement, " +
            KEY_OBJECT_NAME + " text not null);";


    // Sentencia SQL para crear la tabla OBJECT_PROPERTIES
    private static final String DATABASE_CREATE_OBJECT_PROPERTIES = "create table " +
            DATABASE_TABLE_OBJECT_PROPERTIES + " (" + KEY_ID_OBJECT_PROPERTIE +
            " integer primary key autoincrement, " +
            KEY_ID_OBJECT + " number not null,"+
            KEY_OBJECT_PROPERTIE_NAME + " text not null,"+
            KEY_OBJECT_PROPERTIE_TYPE + " number not null);";

    // Sentencia SQL para crear la tabla USER_OBJECT
    private static final String DATABASE_CREATE_USER_OBJECT = "create table " +
            DATABASE_TABLE_USER_OBJECT + " (" + KEY_ID_USER_OBJECT +
            " integer primary key autoincrement, " +
            KEY_CREATE_DATE + " date, " +
            KEY_ID_PROJECT + " integer not null," +
            KEY_LAT + " text not null, " +
            KEY_LON + " text not null, " +
            KEY_TYPE + " integer not null, " +
            KEY_DESC + " text);";

    // Sentencia SQL para crear la tabla USER_OBJECT_PROPERTIE
    private static final String DATABASE_CREATE_USER_OBJECT_PROPERTIE = "create table " +
            DATABASE_TABLE_USER_OBJECT_PROPERTIES + " (" + KEY_ID_USER_OBJECT_PROPERTIE +
            " integer primary key autoincrement, " +
            KEY_ID_USER_OBJECT + " integer not null," +
            KEY_ID_OBJECT_PROPERTIE + " integer not null," +
            KEY_VALUE + " text not null);";

    // Sentencia SQL para crear la tabla LINES
    private static final String DATABASE_CREATE_LINES = "create table " +
            DATABASE_TABLE_LINES + " (" + KEY_ID_LINE +
            " integer primary key autoincrement, " +
            KEY_ID_PROJECT + " integer not null," +
            KEY_ID_LINE_NAME + " text not null," +
            KEY_ID_LINE_LENGTH + " text not null," +
            KEY_ID_LINE_DESC + " text not null);";

    // Sentencia SQL para crear la tabla POINTS
    private static final String DATABASE_CREATE_POINTS = "create table " +
            DATABASE_TABLE_POINTS + " (" + KEY_ID_POINT +
            " integer primary key autoincrement," +
            KEY_ID_LINE + " integer not null," +
            KEY_ID_USER_OBJECT + " integer not null);";


    public DbHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        c = context;
    }

    /**
     * Método empleado para crear la base de datos cuando esta no existe en el sistema.
     * @param db    La base de datos.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Tablas de los modelos de formularios
        db.execSQL(DATABASE_CREATE_PROJECT);
        db.execSQL(DATABASE_CREATE_OBJECTS);
        db.execSQL(DATABASE_CREATE_OBJECT_PROPERTIES);
        db.execSQL(DATABASE_CREATE_USER_OBJECT);
        db.execSQL(DATABASE_CREATE_USER_OBJECT_PROPERTIE);
        db.execSQL(DATABASE_CREATE_LINES);
        db.execSQL(DATABASE_CREATE_POINTS);

        populateDataBase(db);

        Toast toast = Toast.makeText(c, R.string.dbCreated, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion){
        upgrade(db, oldVersion, newVersion, c);
    }

    /**
     * Método llamado cuando se realiza una actualización de la versión de la base de datos
     * @param db            La base de datos
     * @param oldVersion    Id de la versión anterior
     * @param newVersion    Id de la nueva versión
     * @param c             Contexto de la aplicación
     */
    private void upgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion, Context c) {

        if(newVersion > oldVersion) {

            // Tablas de los modelos de formularios
            db.execSQL("DROP TABLE " + DATABASE_TABLE_PROJECT);
            db.execSQL("DROP TABLE " + DATABASE_TABLE_OBJECTS);
            db.execSQL("DROP TABLE " + DATABASE_TABLE_OBJECT_PROPERTIES);
            db.execSQL("DROP TABLE " + DATABASE_TABLE_USER_OBJECT);
            db.execSQL("DROP TABLE " + DATABASE_CREATE_USER_OBJECT_PROPERTIE);
            db.execSQL("DROP TABLE " + DATABASE_CREATE_LINES);
            db.execSQL("DROP TABLE " + DATABASE_CREATE_POINTS);

            Toast toast = Toast.makeText(c, R.string.dbActualized, Toast.LENGTH_SHORT);
            toast.show();
            // Se crea la base de datos
            onCreate(db);
        }else{
            Toast toast = Toast.makeText(c, R.string.dbNotActualized, Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    /**
     * Método que se encarga de insertar elementos en la Base de Datos.
     * Este método define los objetos que estarán disponibles junto con sus propiedades.
     * @param db    La base de datos
     */
    private void populateDataBase(SQLiteDatabase db){

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_OBJECT_NAME, "Apoyo");
        db.insert(DATABASE_TABLE_OBJECTS, null, newValues);
        newValues = new ContentValues();
        newValues.put(KEY_OBJECT_NAME, "CT");
        db.insert(DATABASE_TABLE_OBJECTS, null, newValues);
        newValues = new ContentValues();
        newValues.put(KEY_OBJECT_NAME, "Subestación");
        db.insert(DATABASE_TABLE_OBJECTS, null, newValues);

        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "1");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Número de Apoyo");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 1);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "1");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Nombre del Apoyo");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "1");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Número de Lineas");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 1);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "1");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Altura");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 1);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "1");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Fecha de alta");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 2);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);

        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "2");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Tensión de entrada");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "2");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Tensión de salida");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);

        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Nombre");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues = new ContentValues();
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Descripción");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Municipio");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Parroquia");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Provincia");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Zona");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Tipo");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Clase");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 0);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Fecha de Instalación");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 2);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Potencia de Transformación (KW)");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 1);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Potencia Contratada (KW)");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 1);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);
        newValues.put(KEY_ID_OBJECT, "3");
        newValues.put(KEY_OBJECT_PROPERTIE_NAME, "Potencia Activa Consumida (KW)");
        newValues.put(KEY_OBJECT_PROPERTIE_TYPE, 1);
        db.insert(DATABASE_TABLE_OBJECT_PROPERTIES, null, newValues);


        // Prueba de cambio de coordenadas

        uk.me.jstott.jcoord.LatLng ll4 = new uk.me.jstott.jcoord.LatLng(-60.1167, -111.7833);
        System.out.println("Latitude/Longitude: " + ll4.toString());
        UTMRef utm2 = ll4.toUTMRef();
        System.out.println("Converted to UTM Ref: " + utm2.toString()+ "@" + utm2.getEasting() + "@" + utm2.getNorthing());

    }

    /**
     * Método que se encarga de guardar un proyecto
     * @param name El nombre del proyecto
     * @param desc La descripción del proyecto
     * @param context Contexto
     * @return true si se guarda correctamente, false en caso contrario
     */
    public static boolean saveProject(String name, String desc, Context context){
        long idProject = 0;
        SQLiteDatabase db = null;
        try {

            DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();
            final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            String date = dateFormatter.format(Calendar.getInstance().getTime());

            ContentValues project = new ContentValues();
            project.put(KEY_PROJECT_NAME, name);
            project.put(KEY_PROJECT_DESCRIPTION, desc);
            project.put(KEY_CREATE_DATE, date);

            idProject = db.insert(DATABASE_TABLE_PROJECT, null, project);

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
        }

        if (idProject == -1 || idProject == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Método empleado para recuperar los proyectos que existen en la base de datos.
     * @param context El contecto de la aplicación.
     * @return Vector con los proyectos existentes.
     */
    public static Vector<Project> getExistingProjects(Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<Project> projects = new Vector<>();

        // Se recuperan los proyectos
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_PROJECT, null);
            if (c != null) {
                while (c.moveToNext()) {
                    Project p = new Project();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_ID_PROJECT:
                                p.setProjectId(c.getLong(i));
                                break;
                            case KEY_CREATE_DATE:
                                p.setCreateDate(c.getString(i));
                                break;
                            case KEY_PROJECT_NAME:
                                p.setProjectName(c.getString(i));
                                break;
                            case KEY_PROJECT_DESCRIPTION:
                                p.setProjectDesc(c.getString(i));
                                break;
                        }
                    }
                    projects.add(p);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return projects;
    }

    /**
     * Método empleado para recuperar los proyectos que existen en la base de datos.
     * @param context El contecto de la aplicación.
     * @return Vector con los proyectos existentes.
     */
    public static Vector<ObjectType> getObjectTypes(Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<ObjectType> objects = new Vector<>();

        // Se recuperan los tipos de objeto
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_OBJECTS, null);
            if (c != null) {
                while (c.moveToNext()) {
                    ObjectType o = new ObjectType();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_ID_OBJECT:
                                o.setObjectId(c.getLong(i));
                                break;
                            case KEY_OBJECT_NAME:
                                o.setObjectName(c.getString(i));
                                break;
                        }
                    }
                    objects.add(o);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return objects;
    }

    /**
     * Método empleado para recuperar las propiedades de los objetos que existen en la base de datos.
     * @param context El contecto de la aplicación.
     * @return Vector con los proyectos existentes.
     */
    public static Vector<ObjectPropertie> getObjectProperties(Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<ObjectPropertie> objects = new Vector<>();

        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_OBJECT_PROPERTIES, null);
            if (c != null) {
                while (c.moveToNext()) {
                    ObjectPropertie op = new ObjectPropertie();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_ID_OBJECT_PROPERTIE:
                                op.setIdObjectPropertie(c.getLong(i));
                                break;
                            case KEY_ID_OBJECT:
                                op.setIdObject(c.getLong(i));
                                break;
                            case KEY_OBJECT_PROPERTIE_NAME:
                                op.setPropertieName(c.getString(i));
                                break;
                            case KEY_OBJECT_PROPERTIE_TYPE:
                                op.setPropertieType(c.getInt(i));
                        }
                    }
                    objects.add(op);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return objects;
    }

    /**
     * Método que se encarga de guardar un objecto de usuario
     * @param u el objeto a almacenar
     * @param context el contecto
     * @return true si se inserta correctamente, false en caso contrario
     */
    public static boolean saveUserObject(UserObject u, ObjectPropertiesData opd, Context context){
        long idUo = 0;
        SQLiteDatabase db = null;
        try {
            DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();
            final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            String date = dateFormatter.format(Calendar.getInstance().getTime());

            ContentValues uo = new ContentValues();
            uo.put(KEY_CREATE_DATE, date);
            uo.put(KEY_ID_PROJECT, u.getIdProject());
            uo.put(KEY_LAT, String.valueOf(u.getLat()));
            uo.put(KEY_LON, String.valueOf(u.getLon()));
            uo.put(KEY_TYPE, u.getType());

            if(u.getDescription() != null)
                uo.put(KEY_DESC, u.getDescription());

            idUo = db.insert(DATABASE_TABLE_USER_OBJECT, null, uo);
            u.setIdUserObject(idUo);
            opd.setIdObject(idUo);

            // Se guardan las propiedades del objeto
            saveObjectProperties(opd, context);

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
        }
        if (idUo == -1 || idUo == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Método que actualiza la posición de un Objeto de usuario
     * @param u  el objeto a actualizar
     * @param context el contexto
     * @return true si se realiza la actualización, false en caso contrario.
     */
    public static boolean updateUserObjectPosition(UserObject u, Context context){
        long idUo = 0;
        SQLiteDatabase db = null;
        try {
            DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();

            String where = KEY_ID_USER_OBJECT + " = " + u.getIdUserObject();

            ContentValues uo = new ContentValues();
            uo.put(KEY_LAT, String.valueOf(u.getLat()));
            uo.put(KEY_LON, String.valueOf(u.getLon()));

            idUo = db.update(DATABASE_TABLE_USER_OBJECT, uo, where, null);

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
        }
        if (idUo == -1 || idUo == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Método que recupera los objetos de un proyecto
     * @param idProject el id del proyecto
     * @param context el contexto
     * @return los objetos del proyecto
     */
    public static Vector<UserObject> getUserObjects(long idProject, Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<UserObject> objects = new Vector<>();

        // Se recuperan los proyectos
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_USER_OBJECT + " WHERE " + KEY_ID_PROJECT + " = ?" , new String[]{String.valueOf(idProject)});
            if (c != null) {
                while (c.moveToNext()) {
                    UserObject o = new UserObject();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_ID_USER_OBJECT:
                                o.setIdUserObject(c.getLong(i));
                                break;
                            case KEY_CREATE_DATE:
                                o.setCreateDate(c.getString(i));
                                break;
                            case KEY_ID_PROJECT:
                                o.setIdProject(c.getLong(i));
                                break;
                            case KEY_LAT:
                                o.setLat(c.getDouble(i));
                                break;
                            case KEY_LON:
                                o.setLon(c.getDouble(i));
                                break;
                            case KEY_TYPE:
                                o.setType(c.getInt(i));
                                break;
                            case KEY_DESC:
                                o.setDescription(c.getString(i));
                                break;
                        }
                    }
                    objects.add(o);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return objects;
    }

    /**
     * Método que se encarga de almacenar las propiedades introducidas por el usuario
     * @param opd El objeto con toda la información
     * @param c el contexto
     */
    private static void saveObjectProperties(ObjectPropertiesData opd, Context c){

        // Se recuperan las propiedades y los valores.
        Vector<ObjectPropertie> p = opd.getP();
        Vector<String> values = opd.getValues();

        SQLiteDatabase db = null;
        try {
            DbHelper dbHelper = new DbHelper(c, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();

            // Se inserta cada uno de los valores
            for(int i = 0; i < p.size(); i++) {
                long idInsert;

                ContentValues uo = new ContentValues();
                uo.put(KEY_ID_USER_OBJECT, opd.getIdObject());
                uo.put(KEY_ID_OBJECT_PROPERTIE, p.get(i).getIdObjectPropertie());
                uo.put(KEY_VALUE, values.get(i));

                idInsert = db.insert(DATABASE_TABLE_USER_OBJECT_PROPERTIES, null, uo);
                if (idInsert == -1 || idInsert == 0) {
                    throw new Exception(c.getString(R.string.e_object_properties));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
        }
    }

    /**
     * Método que devuelve las propiedades de un objeto
     * @param idObject el id del objeto
     * @param context el contecto
     * @return un vector con los valores
     */
    public static Vector<String> loadObjectProperties(long idObject, Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<String> values = new Vector<>();

        // Se recuperan los proyectos
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_USER_OBJECT_PROPERTIES + " WHERE " +
                    KEY_ID_USER_OBJECT + " = ? ORDER BY " + KEY_ID_USER_OBJECT_PROPERTIE, new String[]{String.valueOf(idObject)});

            if (c != null) {
                while (c.moveToNext()) {
                    String value = "";
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_VALUE:
                                value = c.getString(i);
                                break;
                        }
                    }
                    values.add(value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return values;
    }

    /**
     * Método que se encarga de actualizar las propiedades de un objeto modificado por el usuario
     * @param opd Las propiedades a actualizar
     * @param context El contecto
     * @return true si se actualizan correctamente, false en caso contrario
     */
    public static boolean updateObjectProperties(ObjectPropertiesData opd, Context context){
        long idProp;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();

            long idObject = opd.getIdObject();
            Vector<Long> ids = new Vector<>();

            // Primero recuperamos los ids de las propiedades
            c = db.rawQuery("SELECT " + KEY_ID_USER_OBJECT_PROPERTIE + " FROM " + DATABASE_TABLE_USER_OBJECT_PROPERTIES + " WHERE " +
                    KEY_ID_USER_OBJECT + " = ? ORDER BY " + KEY_ID_USER_OBJECT_PROPERTIE, new String[]{String.valueOf(idObject)});

            if (c != null) {
                while (c.moveToNext()) {
                    ids.add(c.getLong(0));
                }
            }

            // Actualizamos los valores
            Vector<String> props = opd.getValues();

            if(props.size() == ids.size()) {
                for (int i = 0; i < props.size(); i++) {

                    String where = KEY_ID_USER_OBJECT_PROPERTIE + " = " + ids.get(i);

                    ContentValues uo = new ContentValues();
                    uo.put(KEY_VALUE, props.get(i));

                    idProp = db.update(DATABASE_TABLE_USER_OBJECT_PROPERTIES, uo, where, null);
                    if (idProp == -1 || idProp == 0) {
                        return false;
                    }
                }
            }else{
                Toast toast = Toast.makeText(context, R.string.dbNotUpdatedProperties, Toast.LENGTH_SHORT);
                toast.show();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
            if(c != null)
                c.close();

        }
        return true;
    }

    /**
     * Método que se encarga de almacenar una linea
     * @param l La linea a almacenar.
     * @param c El contexto.
     * @return true si se guarda correctamente, false en caso contrario.
     */
    public static boolean saveLine(Line l, Context c){

        SQLiteDatabase db = null;
        try {

            DbHelper dbHelper = new DbHelper(c, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();

            // Primero se inserta la nueva linea
            ContentValues cV = new ContentValues();

            long idLine = l.getIdLine();
            if(idLine == 0) {

                cV.put(KEY_ID_PROJECT, l.getIdProject());
                cV.put(KEY_ID_LINE_NAME, l.getName());
                cV.put(KEY_ID_LINE_LENGTH, l.getLength());
                cV.put(KEY_ID_LINE_DESC, l.getDesc());

                idLine = db.insert(DATABASE_TABLE_LINES, null, cV);
                l.setIdLine(idLine);
            }

            if(idLine != -1 && idLine != 0){
                // Se inserta cada uno de los puntos que forman la linea
                for(int i = 0; i < l.getPoints().size(); i++){
                    UserObject uo = l.getPoints().get(i);
                    cV = new ContentValues();
                    cV.put(KEY_ID_LINE, idLine);
                    cV.put(KEY_ID_USER_OBJECT, uo.getIdUserObject());
                    long idP = db.insert(DATABASE_TABLE_POINTS, null, cV);
                    if (idP == -1 || idP == 0) {
                        return false;
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
        }
        return true;
    }

    /**
     * Método que valida si alguno de los puntos de una linea está asociado ya a otra linea a la hora
     * de guardar. En caso afirmativo, se devuelven los datos generales de esa linea.
     * @param l La linea de la que se desean validar los puntos
     * @param context El contexto
     * @return Una linea en caso de pertenecer algún punto a esa linea. NULL en caso contrario.
     */
    @Nullable
    public static Line validateLineExistence(Line l, Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        String sql = "";

        for(int i = 0; i < l.getPoints().size(); i++){
            sql = sql + l.getPoints().get(i).getIdUserObject() + ", ";
        }
        sql = sql.substring(0, sql.length()-2);
        sql = " IN (" + sql + ")";

        try{
            c = db.rawQuery("SELECT " + KEY_ID_LINE + " FROM " + DATABASE_TABLE_POINTS + " WHERE " +
                    KEY_ID_USER_OBJECT + sql , null);
            if(c.moveToNext()){
                return(getLineData(c.getLong(0), context));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return null;
    }

    /**
     * Método que devuelve la información general de una linea.
     * @param id El id de la linea
     * @param context El contexto
     * @return La la linea si existe, null en caso contrario
     */
    @Nullable
    private static Line getLineData(Long id, Context context){
        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        Line l = new Line();

        try{
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_LINES + " WHERE " +
                    KEY_ID_LINE + " = ?" , new String[]{String.valueOf(id)});

            while (c.moveToNext()) {

                // Se recuperan los datos de la linea.
                for (int i = 0; i < c.getColumnCount(); i++) {
                    switch (c.getColumnName(i)) {
                        case KEY_ID_LINE:
                            l.setIdLine(c.getLong(i));
                            break;
                        case KEY_ID_PROJECT:
                            l.setIdProject(c.getLong(i));
                            break;
                        case KEY_ID_LINE_NAME:
                            l.setName(c.getString(i));
                            break;
                        case KEY_ID_LINE_LENGTH:
                            l.setLength(c.getFloat(i));
                            break;
                        case KEY_ID_LINE_DESC:
                            l.setDesc(c.getString(i));
                            break;
                    }
                }
            }
            // Se recuperan los puntos de la linea.
            getLinePoints(l, context);
            return l;
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return null;
    }

    /**
     * Método que recupera las lineas de un proyecto
     * @param idProject el id del proyecto
     * @param context el contexto
     * @return las lineas del proyecto
     */
    public static Vector<Line> getProjectLines(long idProject, Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<Line> lines = new Vector<>();

        // Se recuperan las lineas
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_LINES + " WHERE " + KEY_ID_PROJECT + " = ?" , new String[]{String.valueOf(idProject)});
            if (c != null) {
                while (c.moveToNext()) {
                    Line l = new Line();

                    // Se recuperan los datos de la linea.
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_ID_LINE:
                                l.setIdLine(c.getLong(i));
                                break;
                            case KEY_ID_PROJECT:
                                l.setIdProject(c.getLong(i));
                                break;
                            case KEY_ID_LINE_NAME:
                                l.setName(c.getString(i));
                                break;
                            case KEY_ID_LINE_LENGTH:
                                l.setLength(c.getFloat(i));
                                break;
                            case KEY_ID_LINE_DESC:
                                l.setDesc(c.getString(i));
                                break;
                        }
                    }
                    // Se recuperan los puntos de la linea actual.
                    getLinePoints(l, context);
                    lines.add(l);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return lines;
    }

    /**
     * Método que recupera los objetos de usuario que forman los puntos de una linea.
     * @param l La linea de la que se quieren recuperar los puntos.
     * @param context El contexto.
     */
    private static void getLinePoints(Line l, Context context){
        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Vector<Long> idUos = new Vector<>() ;

        // Se recuperan los puntos
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT " + KEY_ID_USER_OBJECT + " FROM " + DATABASE_TABLE_POINTS
                    + " WHERE " + KEY_ID_LINE + " = ?" , new String[]{String.valueOf(l.getIdLine())});

            if (c != null) {
                while (c.moveToNext()) {
                    // Se recuperan los ids de los objeto de usuario
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        idUos.add(c.getLong(i));
                    }
                }
            }
            String queryIn = "";
            for(int i = 0; i < idUos.size(); i++){
                queryIn = queryIn.concat(idUos.get(i) + ",");
            }
            queryIn = queryIn.substring(0, queryIn.length()-1);

            c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_USER_OBJECT
                    + " WHERE " + KEY_ID_USER_OBJECT + " in (" + queryIn + ")" , null);

            Vector<UserObject> objects = new Vector<>();
            if (c != null) {
                while (c.moveToNext()) {
                    UserObject o = new UserObject();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        switch (c.getColumnName(i)) {
                            case KEY_ID_USER_OBJECT:
                                o.setIdUserObject(c.getLong(i));
                                break;
                            case KEY_CREATE_DATE:
                                o.setCreateDate(c.getString(i));
                                break;
                            case KEY_ID_PROJECT:
                                o.setIdProject(c.getLong(i));
                                break;
                            case KEY_LAT:
                                o.setLat(c.getDouble(i));
                                break;
                            case KEY_LON:
                                o.setLon(c.getDouble(i));
                                break;
                            case KEY_TYPE:
                                o.setType(c.getInt(i));
                                break;
                            case KEY_DESC:
                                o.setDescription(c.getString(i));
                                break;
                        }
                    }
                    objects.add(o);
                }
            }
            l.setPoints(objects);

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
    }

    /**
     * Método que actualiza las propiedades de una linea.
     * @param l  la linea a actualizar
     * @param context el contexto
     * @return true si se realiza la actualización, false en caso contrario.
     */
    public static boolean updateLine(Line l, Context context){
        long idLine = 0;
        SQLiteDatabase db = null;
        try {
            DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
            db = dbHelper.getWritableDatabase();

            String where = KEY_ID_LINE + " = " + l.getIdLine();

            ContentValues cV = new ContentValues();
            cV.put(KEY_ID_LINE_NAME, l.getName());
            cV.put(KEY_ID_LINE_DESC, l.getDesc());
            cV.put(KEY_ID_LINE_LENGTH, l.getLength());

            idLine = db.update(DATABASE_TABLE_LINES, cV, where, null);

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null)
                db.close();
        }
        if (idLine == -1 || idLine == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Método que actualiza los puntos que pasan por unas coordenadas determinadas
     * @param uoId Id del objeto de usuario.
     * @param context Contexto.
     */
    public static void updateLinePoints(long uoId, Context context ){
        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedLines = 0;

        Cursor c = null;
        Cursor c2 = null;
        try {
            db.beginTransaction();
            // Se recuperan los puntos que deben ser modificados

            // 1. Se recuperan las lineas para las que hay que recalcular la longitud.
            c = db.rawQuery("SELECT DISTINCT(" + KEY_ID_LINE + ") FROM " + DATABASE_TABLE_POINTS
                    + " WHERE " + KEY_ID_USER_OBJECT + " = (?)", new String[]{String.valueOf(uoId)});

            if (c != null) {
                while (c.moveToNext()) {
                    Vector<Location> positions = new Vector<>();
                    // 2. Para cada linea se recuperan sus las coordenadas de sus puntos
                    c2 = db.rawQuery("SELECT " + KEY_LAT + ", " + KEY_LON + " FROM " + DATABASE_TABLE_USER_OBJECT
                            + " WHERE " + KEY_ID_USER_OBJECT + " IN (SELECT " + KEY_ID_USER_OBJECT + " FROM " + DATABASE_TABLE_POINTS
                            + " WHERE " + KEY_ID_LINE + " = ?)", new String[]{String.valueOf(c.getLong(0))});
                    if(c2 != null){
                        while (c2.moveToNext()){
                            Location l = new Location("");
                            // 3. Se guardan los puntos que forman la linea
                            for (int i = 0; i < c2.getColumnCount(); i++) {
                                switch (c2.getColumnName(i)) {
                                    case KEY_LAT:
                                        l.setLatitude(c2.getDouble(i));
                                        break;
                                    case KEY_LON:
                                        l.setLongitude(c2.getDouble(i));
                                        break;
                                }
                            }
                            positions.add(l);
                        }
                    }
                    // 4. Se calcula la nueva longitud de la linea
                    LatLng actualMarker = new LatLng(positions.get(0).getLatitude(), positions.get(0).getLongitude());
                    float length = 0;
                    for(int i = 1; i < positions.size(); i++){
                        Location l1 = new Location("");
                        Location l2 = new Location("");
                        l1.setLatitude(actualMarker.latitude);
                        l1.setLongitude(actualMarker.longitude);
                        l2.setLatitude(positions.get(i).getLatitude());
                        l2.setLongitude(positions.get(i).getLongitude());
                        length += l1.distanceTo(l2);
                        actualMarker = new LatLng(positions.get(i).getLatitude(), positions.get(i).getLongitude());
                    }

                    // 5. Se actualiza la longitud de la linea
                    String where  = KEY_ID_LINE + " = " + c.getLong(0);
                    ContentValues cV = new ContentValues();
                    cV.put(KEY_ID_LINE_LENGTH, length);
                    updatedLines += db.update(DATABASE_TABLE_LINES, cV, where, null);
                }
            }

            assert c != null;
            if(updatedLines != c.getCount()){
                Toast toast = Toast.makeText(context, R.string.dbErrorUpdatingPoints, Toast.LENGTH_SHORT);
                toast.show();
            }else{
                db.setTransactionSuccessful();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(db != null) {
                db.endTransaction();
                db.close();
            }
            if(c != null)
                c.close();
            if(c2 != null)
                c2.close();
        }
    }

    /**
     * Método que recupera las lineas de un proyecto
     * @param idUo el identificador del objecto de usuario.
     * @param context el contexto
     * @return Número de lineas que pasan por el punto indicado.
     */
    public static int getPointsForUserObject(long idUo, Context context){

        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int pointsNumber = 0;
        // Se recuperan las lineas
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE_POINTS + " WHERE " + KEY_ID_USER_OBJECT + " = ?" , new String[]{String.valueOf(idUo)});
            if (c != null) {
                while (c.moveToNext()) {
                    pointsNumber = c.getInt(0);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(c != null)
                c.close();
            if(db != null)
                db.close();
        }
        return pointsNumber;
    }

    /**
     * Método encargado de eliminar un objeto y sus propiedades.
     * @param idUo El id del objeto a eliminar.
     * @param context El contexto.
     * @return True si se elimina correctamente, false en caso contrario.
     */
    public static int deleteObject(long idUo, Context context){
        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if(db.delete(DATABASE_TABLE_USER_OBJECT_PROPERTIES, KEY_ID_USER_OBJECT + "=" + idUo, null) > 0){
            return db.delete(DATABASE_TABLE_USER_OBJECT, KEY_ID_USER_OBJECT + "=" + idUo, null);
        }
        return 0;
    }

    /**
     * Método encargado de eliminar una linea y sus puntos.
     * @param idLine El id de la linea a eliminar.
     * @param context El contexto.
     * @return True si se elimina correctamente, false en caso contrario.
     */
    public static int deleteLine(long idLine, Context context){
        DbHelper dbHelper = new DbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if(db.delete(DATABASE_TABLE_POINTS, KEY_ID_LINE + "=" + idLine, null) > 0){
            return db.delete(DATABASE_TABLE_LINES, KEY_ID_LINE + "=" + idLine, null);
        }
        return 0;
    }

}
