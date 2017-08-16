package com.applus.georeference.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.applus.georeference.R;
import com.applus.georeference.entities.Line;
import com.applus.georeference.entities.ObjectProperties;
import com.applus.georeference.entities.ObjectPropertiesData;
import com.applus.georeference.entities.ObjectTypes;
import com.applus.georeference.entities.UserObject;
import com.applus.georeference.helpers.DbHelper;
import com.applus.georeference.helpers.SyncHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.Vector;

/**
 * /**
 * Entidad Clase encargada de mostrar el mapa y los elementos definidos por los usuarios
 * @version 1
 * @author Enrique Vila
 * @since 02/09/2016
 * @since 15/09/2016 EVM - Añadida la opción de guardar las lineas cuando se termina de introducir
 *                   sus propiedades (en la actividad LineActivity).
 * @since 20/09/2016 EVM - Añadidas funcionalidades al mapa en un menú de opciones: eliminar, medir.
 * @since 02/11/2016 EVM - Se comienza a desarrollar el proceso de subida del proyecto al servidor.
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int GET_OBJECT_TYPE = 0;
    private static final int SHOW_OBJECT_FOR_UPDATE = 1;
    private static final int GET_LINE_PROPS = 2;
    private static final int SHOW_LINE_PROPS_FOR_UPDATE = 3;

    private boolean exit = false;
    private long idProject = 0;

    private GoogleMap mMap;
    private HashMap<String, Marker> markers;
    private HashMap<String, UserObject> userObjects;
    private Vector<Line> lines;
    private HashMap<String,Line> polylines;
    private ObjectTypes objectTypes;
    private ObjectProperties objectPropertiesNames;
    private ObjectPropertiesData opd;
    private Vector<Marker> lineMarkers;
    private Vector<Marker> measureLineMarkers;

    // Diálogos para indicar cargas de la aplicación.
    private ProgressDialog progressMovingMarker;
    private ProgressDialog progressMeasureMarker;
    private ProgressDialog progressSync;

    // Identificador de la polilinea que se modifica.
    private String polId;

    // Preferencias de usuario: tipo de mapa.
    private int mapType;

    // Preferencias de usuario: opciones de localización.
    private boolean locationEnabled;
    private boolean gpsEnabled;

    // Variables para la localización.
    private LocationManager locationManager;
    private LocationListener locationListener;

    // Variable que indica si se está dibujando una linea
    private boolean drawingLine = false;

    // Variable que indica si se están eliminando elementos
    private boolean deletingElement = false;

    // Variable que indica si se está realizando una medición
    private boolean measuring = false;

    // Variable que indica si se están mostrando las opciones
    private boolean showOptions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        final ImageButton drawLine = (ImageButton) findViewById(R.id.imageButtonDrawLine);
        final ImageButton deleteElement = (ImageButton) findViewById(R.id.imageButtonDelete);
        final ImageButton options = (ImageButton) findViewById(R.id.imageButtonOptions);
        final ImageButton measure = (ImageButton) findViewById(R.id.imageButtonMeasure);
        final ImageButton sync = (ImageButton) findViewById(R.id.imageButtonSync);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getPreferences();
        mapFragment.getMapAsync(this);
        markers = new HashMap<>();
        lines = new Vector<>();
        lineMarkers = new Vector<>();
        measureLineMarkers = new Vector<>();
        userObjects = new HashMap<>();
        polylines = new HashMap<>();

        // Se hacen invisibles los botones
        drawLine.setVisibility(View.INVISIBLE);
        drawLine.setEnabled(false);
        drawLine.setClickable(false);
        deleteElement.setVisibility(View.INVISIBLE);
        deleteElement.setEnabled(false);
        deleteElement.setClickable(false);
        measure.setVisibility(View.INVISIBLE);
        measure.setEnabled(false);
        measure.setClickable(false);
        sync.setVisibility(View.INVISIBLE);
        sync.setEnabled(false);
        sync.setClickable(false);

        progressMovingMarker = new ProgressDialog(this);
        progressMovingMarker.setIndeterminate(true);
        progressMovingMarker.setTitle(R.string.loading);
        progressMovingMarker.setMessage(getResources().getString(R.string.moving_marker));
        progressMovingMarker.setCancelable(false);

        progressMeasureMarker = new ProgressDialog(this);
        progressMeasureMarker.setIndeterminate(true);
        progressMeasureMarker.setTitle(R.string.loading);
        progressMeasureMarker.setCancelable(false);

        progressSync = new ProgressDialog(this);
        progressSync.setIndeterminate(true);
        progressSync.setTitle(R.string.syncing);
        progressSync.setCancelable(false);

        // Listener para el menú de opciones. Este menú se encarga de mostrar / ocultar las opciones
        // que se encuentran disponibles en el mapa.
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions = !showOptions;

                if(showOptions) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        options.setBackground(getResources().getDrawable(R.drawable.map_button_clicked_shape, null));
                    }
                    drawLine.setVisibility(View.VISIBLE);
                    drawLine.setEnabled(true);
                    drawLine.setClickable(true);
                    deleteElement.setVisibility(View.VISIBLE);
                    deleteElement.setEnabled(true);
                    deleteElement.setClickable(true);
                    measure.setVisibility(View.VISIBLE);
                    measure.setEnabled(true);
                    measure.setClickable(true);
                    sync.setVisibility(View.VISIBLE);
                    sync.setEnabled(true);
                    sync.setClickable(true);
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        options.setBackground(getResources().getDrawable(R.drawable.map_button_shape, null));
                    }
                    drawLine.setVisibility(View.INVISIBLE);
                    drawLine.setEnabled(false);
                    drawLine.setClickable(false);
                    deleteElement.setVisibility(View.INVISIBLE);
                    deleteElement.setEnabled(false);
                    deleteElement.setClickable(false);
                    measure.setVisibility(View.INVISIBLE);
                    measure.setEnabled(false);
                    measure.setClickable(false);
                    sync.setVisibility(View.INVISIBLE);
                    sync.setEnabled(false);
                    sync.setClickable(false);
                }
            }
        });

        // Listener del botón que activa/desactiva la creación de lineas.
        drawLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!deletingElement & !measuring) {
                    drawingLine = !drawingLine;
                    if (drawingLine) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            drawLine.setBackground(getResources().getDrawable(R.drawable.map_button_clicked_shape, null));
                        }
                        Toast.makeText(MapActivity.this, R.string.draw_line, Toast.LENGTH_SHORT).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            drawLine.setBackground(getResources().getDrawable(R.drawable.map_button_shape, null));
                        }
                        // Se obtienen las propiedas de la linea invocando una nueva actividad
                        if (lineMarkers.size() > 1) {
                            Line l = new Line();
                            Vector<UserObject> lps = new Vector<>();
                            for (int i = 0; i < lineMarkers.size(); i++) {
                                lps.add(userObjects.get(lineMarkers.get(i).getId()));
                            }
                            l.setPoints(lps);
                            l.setLength(calculateLineLength());
                            Intent intent = new Intent(getApplicationContext(), LineActivity.class);
                            intent.putExtra("LINE", l);
                            startActivityForResult(intent, GET_LINE_PROPS);
                        } else {
                            Toast.makeText(MapActivity.this, R.string.line_cancelled, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // Listener del botón que activa/desactiva la eliminación de elementos.
        deleteElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!drawingLine &! measuring) {
                    deletingElement = !deletingElement;
                    if (deletingElement) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            deleteElement.setBackground(getResources().getDrawable(R.drawable.map_button_delete_shape, null));
                        }
                        Toast.makeText(MapActivity.this, R.string.delete_item, Toast.LENGTH_SHORT).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            deleteElement.setBackground(getResources().getDrawable(R.drawable.map_button_shape, null));
                        }
                    }
                }
            }
        });

        // Listener del botón que activa/desactiva la medición de distancias.
        measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!deletingElement &! drawingLine) {
                    measuring = !measuring;
                    if (measuring) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            measure.setBackground(getResources().getDrawable(R.drawable.map_button_clicked_shape, null));
                        }
                        Toast.makeText(MapActivity.this, R.string.measure, Toast.LENGTH_SHORT).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            measure.setBackground(getResources().getDrawable(R.drawable.map_button_shape, null));
                        }
                        measureLineMarkers = new Vector<>();
                        progressMeasureMarker.show();
                        mMap.clear();
                        loadData();
                        progressMeasureMarker.dismiss();
                    }
                }
            }
        });

        // Listener del botón de sincronización con el servidor (subida del proyecto al servidor)
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!deletingElement &! drawingLine &! measuring) {
                    showSyncDialog();
                }
            }
        });

    }

    // Método que es llamado cuando el mapa está listo para ser usado y lo renderiza.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(getMapType(mapType));
        loadData();

        // Se comprueba si es necesario utilizar la localización del usuario.
        if (locationEnabled) {
            // Variables necesarias para la localización.
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 14));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 20, locationListener);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, locationListener);
            }
        } else {
            // Se añade un marcador en Applus Sada y se mueve la cámara.
            LatLng applus = new LatLng(43.3177046, -8.2961921);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(applus, 14));
        }

        // Listener que recoge el evento de una pulsación larga en el mapa (Crear marcador)
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(!deletingElement &! measuring &! drawingLine) {
                    Intent intent = new Intent(getApplicationContext(), SelectObjectActivity.class);
                    intent.putExtra("LAT", latLng.latitude);
                    intent.putExtra("LON", latLng.longitude);
                    intent.putExtra("OBJECT_TYPES", objectTypes);
                    intent.putExtra("OBJECT_PROPERTIES", objectPropertiesNames);
                    startActivityForResult(intent, GET_OBJECT_TYPE);
                }
            }
        });

        // Listener que recibe el evento de hacer click en un marcador. Se utiliza para dibujar
        // lineas, siempre que la acción esté seleccionada.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Si está activa la herramienta de eliminar, se elimina el marcador, si el usuario lo confirma.
                if(deletingElement){
                    showDeleteMarkerDialog(userObjects.get(marker.getId()).getIdUserObject());
                }
                if(measuring){
                    measure(marker);
                }
                if (drawingLine) {
                    lineMarkers.add(marker);
                    //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    Toast.makeText(MapActivity.this, R.string.added_point, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        // Listener para las lineas dibujadas en el mapa.
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Si está activa la herramienta de eliminar, se elimina el marcador, si el usuario lo confirma.
                if(deletingElement){
                    showDeletePolylineDialog(polylines.get(polyline.getId()).getIdLine());
                }else {
                    polId = polyline.getId();
                    Line l = polylines.get(polyline.getId());
                    Intent intent = new Intent(getApplicationContext(), LineActivity.class);
                    intent.putExtra("LINE", l);
                    intent.putExtra("LOADING", true);
                    startActivityForResult(intent, SHOW_LINE_PROPS_FOR_UPDATE);
                }
            }
        });

        // Listener que recoge el evento de arrastrar un marcador. (Modificar marcador).
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                progressMovingMarker.show();
                // Se actualiza la posición del marcador
                updateMarker(marker);
            }
        });

        // Listener que recoge el evento de pinchar en los tooltips del mapa. (Abrir información de un objeto)
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(!deletingElement &! measuring &! drawingLine) {
                    // Se muestra la información del elemento seleccionado
                    showObjectData(marker.getId());
                }
            }
        });

    }

    /**
     * Se controla el botón volver para mostrar una confirmación antes de abandonar la actividad.
     */
    @Override
    public void onBackPressed() {
        if (exit) {
            if(locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                // Se eliminan las actualizaciones de la posición del usuario.
                locationManager.removeUpdates(locationListener);
            }
            super.onBackPressed();
        }else {
            showExitDialog();
        }
    }

    /**
     * Método que controla el flujo de la aplicación a la hora crear un nuevo marcador.
     * @param requestCode Código de petición
     * @param resultCode Resultado de la petición
     * @param data Datos
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(data.getExtras() != null) {
                Bundle extras = data.getExtras();
                switch (requestCode) {
                    // Se ha creado un nuevo objeto
                    case GET_OBJECT_TYPE:
                        double lat = 0, lon = 0;
                        int color = 0;

                        if (extras != null) {
                            if (extras.containsKey("LAT")) {
                                lat = (double) extras.get("LAT");
                            }
                            if (extras.containsKey("LON")) {
                                lon = (double) extras.get("LON");
                            }
                            if (extras.containsKey("SELECTED")) {
                                color = (int) extras.get("SELECTED");
                            }
                            if (extras.containsKey("DATA")) {
                                opd = (ObjectPropertiesData) extras.get("DATA");
                            }
                        }
                        // Se crea el marcador
                        createMarker(new LatLng(lat, lon), color, null, false);
                        break;

                    // Se ha actualizado un objeto.
                    case SHOW_OBJECT_FOR_UPDATE:
                        if (extras.containsKey("DATA")) {
                            opd = (ObjectPropertiesData) extras.get("DATA");
                        }
                        if (opd != null)
                            DbHelper.updateObjectProperties(opd, getApplicationContext());
                        break;

                    // Se ha creado una linea
                    case GET_LINE_PROPS:
                        if (extras.containsKey("LINE")) {
                            // Se dibuja la linea
                            Polyline p = drawLines();
                            saveLine((Line) extras.get("LINE"), p);
                            // Se crea un nuevo vector para la siguiente linea
                            lineMarkers = new Vector<>();
                        }
                        break;

                    // Se ha actualizado una linea
                    case SHOW_LINE_PROPS_FOR_UPDATE:
                        if (extras.containsKey("LINE")) {
                            Line l = (Line) extras.get("LINE");
                            if (l != null) {
                                // Se actualiza la polilea que se está mostrando en el mapa
                                Line lAux = new Line();
                                lAux.setName(l.getName());
                                lAux.setDesc(l.getDesc());
                                lAux.setLength(l.getLength());
                                polylines.remove(polId);
                                polylines.put(polId, lAux);
                                // Se actualiza la base de datos.
                                DbHelper.updateLine(l, getApplicationContext());
                            }
                        }
                        break;

                    // Error
                    default:
                        System.out.println("ERROR");
                        break;

                }
            }
        }else if (resultCode == RESULT_CANCELED){
            if(requestCode == GET_LINE_PROPS ) {
                // Se crea un nuevo vector para la siguiente linea.
                lineMarkers = new Vector<>();
            }
        }
    }

    /**
     * Método que se encuentra de almacenar un marcador (UserObject).
     * @param pos la posición del objeto
     * @param type el color del marcador
     */
    private String createMarker(LatLng pos, int type,String desc, boolean loading){

        UserObject uo = new UserObject();
        uo.setIdProject(idProject);
        uo.setLat(pos.latitude);
        uo.setLon(pos.longitude);
        uo.setType(type);
        String markerTitle = objectTypes.getObjectTypes().get(type).getObjectName().toUpperCase();
        Marker m = null;
        // Se añaden los marcadores en función del tipo de elemento.
        // TODO Cuando se añada un nuevo tipo a la BBDD, se deberá modificar este método. Es posible añadir iconos a cada tipo de elemento.
        switch (type){
            case 0:
                m = mMap.addMarker((new MarkerOptions().position(pos).title(markerTitle).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker)).anchor(0.5f, 0.5f).snippet(desc).draggable(true)));
                markers.put(m.getId(), m);
                break;
            case 1:
                m = mMap.addMarker((new MarkerOptions().position(pos).title(markerTitle).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)).anchor(0.5f, 0.5f).snippet(desc).draggable(true)));
                markers.put(m.getId(), m);
                break;
            case 2:
                m = mMap.addMarker((new MarkerOptions().position(pos).title(markerTitle).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker)).anchor(0.5f, 0.5f).snippet(desc).draggable(true)));
                markers.put(m.getId(), m);
                break;
            default:
                System.out.println("ERROR GRAVE EN LA SELECCIÓN");
        }
        if(!loading) {
            if (m != null) {
                uo.setLat(m.getPosition().latitude);
                uo.setLon(m.getPosition().longitude);
                // Se guarda el objeto en base de datos
                DbHelper.saveUserObject(uo, opd, getApplicationContext());
                userObjects.put(m.getId(), uo);
            }
        }
        if (m != null) {
            return m.getId();
        }else{
            return null;
        }
    }

    /**
     * Método encargado de validar si el usuario desea abandonar el proyecto actual.
     */
    private void showExitDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(R.string.dialog_alert_map).setMessage(getResources().getString(R.string.dialog_text_map))
                .setPositiveButton(R.string.dialog_continue_map, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exit = true;
                        onBackPressed();
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

    /**
     * Método encargado de validar si el usuario desea eliminar el objeto seleccionado.
     * @param uoId Identificador del objeto a eliminar.
     */
    private void showDeleteMarkerDialog(final long uoId){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(R.string.dialog_alert_map).setMessage(getResources().getString(R.string.dialog_delete_marker_map))
                .setPositiveButton(R.string.dialog_delete_confirmation_map, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       deleteMarker(uoId);
                       mMap.clear();
                       loadData();
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

    /**
     * Método encargado de validar si el usuario desea eliminar el objeto seleccionado.
     * @param lineId  Id de la linea a eliminar
     */
    private void showDeletePolylineDialog(final long lineId){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(R.string.dialog_alert_map).setMessage(getResources().getString(R.string.dialog_delete_polyline_map))
                .setPositiveButton(R.string.dialog_delete_confirmation_map, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePolyline(lineId);
                        mMap.clear();
                        loadData();
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

    /**
     * Método encargado de validar si el usuario desea sincronizar el proyecto
     */
    private void showSyncDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle(R.string.dialog_alert_map).setMessage(getResources().getString(R.string.dialog_sync_project))
                .setPositiveButton(R.string.dialog_sync_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressSync.show();
                        boolean syncResult = SyncHelper.sendProject(getApplication(), idProject);
                        progressSync.dismiss();
                        if(syncResult){
                            Toast.makeText(MapActivity.this, R.string.sync_ok, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MapActivity.this, R.string.sync_ko, Toast.LENGTH_SHORT).show();
                        }
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

    /**
     * Método encargado de recuperar los datos de un proyecto
     */
    private void loadData(){
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey("ID_PROJECT")) {
                idProject = (long) parameters.get("ID_PROJECT");
            }
            if (parameters.containsKey("OBJECT_TYPES")) {
                objectTypes = (ObjectTypes) parameters.get("OBJECT_TYPES");
            }
            if (parameters.containsKey("OBJECT_PROPERTIES")) {
                objectPropertiesNames = (ObjectProperties) parameters.get("OBJECT_PROPERTIES");
            }
        }
        if(idProject != 0){
            Vector<UserObject> uos = DbHelper.getUserObjects(idProject, getApplicationContext());
            if(uos != null){
                // Se recargan los marcadores ya existentes y se guardan los objetos del usuario.
                for(int i = 0; i < uos.size(); i++){
                    LatLng pos = new LatLng(uos.get(i).getLat(), uos.get(i).getLon());
                    String mId = createMarker(pos, uos.get(i).getType(), uos.get(i).getDescription(), true);
                    if(mId != null)
                        userObjects.put(mId, uos.get(i));
                }
            }
            Vector<Line> linesToLoad = DbHelper.getProjectLines(idProject, getApplicationContext());
            for(int i = 0; i < linesToLoad.size(); i++){
                Line l = linesToLoad.get(i);
                lines.add(l);
                PolylineOptions opts = new PolylineOptions();
                for(int j = 0; j < l.getPoints().size(); j++) {
                    opts.add(l.getPoints().get(j).getPosition());
                }
                // TODO Distintos colores de linea
                opts.color(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                opts.width(9);
                opts.clickable(true);
                Polyline p = mMap.addPolyline(opts);
                polylines.put(p.getId(), l);
            }
        }
    }

    /**
     * Método que se encarga de actualizar un marcador y su UserObject asociado
     * @param marker El marcador a actualizar
     */
    private void updateMarker(Marker marker){

        LatLng l = marker.getPosition();
        UserObject uo = userObjects.get(marker.getId());
        markers.remove(marker.getId());
        markers.put(marker.getId(), marker);
        uo.setLat(l.latitude);
        uo.setLon(l.longitude);
        userObjects.remove(marker.getId());
        userObjects.put(marker.getId(), uo);

        DbHelper.updateUserObjectPosition(uo, getApplicationContext());

        // Se actualizan las posibles lineas que pasen por el marcador que ha sido desplazado.
        updatePolylinePoints(uo.getIdUserObject());
    }

    /**
     * Método llama a la actividad que muestra las propiedades de un objeto.
     * @param mId identificador del objeto
     */
    private void showObjectData(String mId){
        UserObject u = userObjects.get(mId);
        Intent intent = new Intent(getApplicationContext(), ObjectActivity.class);
        intent.putExtra("OBJECT_TYPES", objectTypes);
        intent.putExtra("OBJECT_PROPERTIES", objectPropertiesNames);
        intent.putExtra("SELECTED", u.getType());
        intent.putExtra("OBJECT_ID", u.getIdUserObject());
        startActivityForResult(intent, SHOW_OBJECT_FOR_UPDATE);
    }

    /**
     * Método que recupera las preferencias de usuario.
     */
    private void getPreferences(){
        SharedPreferences prefs = getSharedPreferences("georeference", Context.MODE_PRIVATE);
        if (prefs != null) {
            if(prefs.contains("location")){
                locationEnabled = prefs.getBoolean("location", false);
            }
            if(prefs.contains("gps")){
                gpsEnabled = prefs.getBoolean("gps", false);
            }
            if(prefs.contains("map_type")){
                mapType = prefs.getInt("map_type", 0);

            }
        }
    }

    /**
     * Método que devuelve el tipo de mapa a utilizar
     * @param type el tipo de mapa seleccionado por el usuario
     * @return el tipo de mapa a emplear por el visor
     */
    private int getMapType(int type){
        int googleType = 0;
        switch (type){
            case 0:
                googleType = GoogleMap.MAP_TYPE_NORMAL;
                break;
            case 1:
                googleType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case 2:
                googleType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case 3:
                googleType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
        }
        return googleType;
    }

    /**
     * Método encargado de dibujar una linea definida por varios puntos
     */
    private Polyline drawLines(){
        if(lineMarkers != null){
            if(lineMarkers.size() > 1){
                PolylineOptions opts = new PolylineOptions();
                for(int i = 0; i < lineMarkers.size(); i++){
                    opts.add(lineMarkers.get(i).getPosition());
                }
                // TODO Distintos colores de linea
                opts.color(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                opts.width(9);
                opts.clickable(true);
                return mMap.addPolyline(opts);
            }else{
                Toast.makeText(MapActivity.this, R.string.select_more_points, Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return null;
    }

    /**
     * Método que calcula la longitud de una linea (en metros).
     * @return Los metros que mide la linea.
     */
    private float calculateLineLength(){
        float length = 0;
        if(lineMarkers != null && lineMarkers.size() > 1){
            LatLng actualMarker = lineMarkers.get(0).getPosition();
            for(int i = 1; i < lineMarkers.size(); i++){
                Location l1 = new Location("");
                Location l2 = new Location("");
                l1.setLatitude(actualMarker.latitude);
                l1.setLongitude(actualMarker.longitude);
                l2.setLatitude(lineMarkers.get(i).getPosition().latitude);
                l2.setLongitude(lineMarkers.get(i).getPosition().longitude);
                length += l1.distanceTo(l2);
                actualMarker = lineMarkers.get(i).getPosition();
            }
        }
        return length;
    }

    /**
     * Método que calcula la longitud de una linea (en metros).
     * @return Los metros que mide la linea.
     */
    private float calculateLineLengthForMeasuring(){
        float length = 0;
        if(measureLineMarkers != null && measureLineMarkers.size() > 1){
            LatLng actualMarker = measureLineMarkers.get(0).getPosition();
            for(int i = 1; i < measureLineMarkers.size(); i++){
                Location l1 = new Location("");
                Location l2 = new Location("");
                l1.setLatitude(actualMarker.latitude);
                l1.setLongitude(actualMarker.longitude);
                l2.setLatitude(measureLineMarkers.get(i).getPosition().latitude);
                l2.setLongitude(measureLineMarkers.get(i).getPosition().longitude);
                length += l1.distanceTo(l2);
                actualMarker = measureLineMarkers.get(i).getPosition();
            }
        }
        return length;
    }

    /**
     * Método encargado de almacenar una linea en base de datos y la añade a las lineas del mapa.
     * @param l La linea a almacenar.
     */
    private void saveLine(Line l, Polyline p){
        l.setIdProject(idProject);
        // Se persiste en base de datos
        DbHelper.saveLine(l, getApplicationContext());
        // Se añade la linea a las lineas actuales del mapa.
        lines.add(l);
        polylines.put(p.getId(), l);
    }

    /**
     * Método que actualiza las lineas cuando se desplaza un marcador
     * @param uoId Identificador
     */
    private void updatePolylinePoints(long uoId){
        // Se actualiza la base de datos
        DbHelper.updateLinePoints(uoId, getApplicationContext());
        mMap.clear();
        loadData();
        progressMovingMarker.dismiss();
    }

    /**
     * Método que elimina un marcador, medianta la eliminación del userObject asociado
     * @param uoId El identificador del objeto de usuario.
     * @return true si se elimina, false en caso contrario.
     */
    private boolean deleteMarker(long uoId){
        if(DbHelper.getPointsForUserObject(uoId, getApplicationContext()) > 0){
            Toast.makeText(MapActivity.this, R.string.lines_at_selected_point, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(DbHelper.deleteObject(uoId, getApplicationContext()) > 0){
            return true;
        }

        Toast.makeText(MapActivity.this, R.string.error_deleting_marker, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Método que elimina un marcador, medianta la eliminación del userObject asociado
     * @param lineId El identificador del objeto de usuario.
     * @return true si se elimina, false en caso contrario.
     */
    private boolean deletePolyline(long lineId){
        if(DbHelper.deleteLine(lineId, getApplicationContext()) > 0){
            return true;
        }
        Toast.makeText(MapActivity.this, R.string.error_deleting_line, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Método encargado de gestionar las mediciones.
     * @param m Marcador a añadir a la linea de medición.
     */
    private void measure(Marker m){
        if(measureLineMarkers.size() == 0){
            measureLineMarkers.add(m);
        }else{
            measureLineMarkers.add(m);
            PolylineOptions opts = new PolylineOptions();
            for(int i = 0; i < measureLineMarkers.size(); i++){
                opts.add(measureLineMarkers.get(i).getPosition());
            }
            opts.color(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_dark));
            mMap.addPolyline(opts);

            float length = calculateLineLengthForMeasuring();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(String.valueOf(length), String.valueOf(length)));
            Toast.makeText(MapActivity.this, getResources().getString(R.string.measured_line_length) +
                    " " + length + " m.\n" + getResources().getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
        }
    }
}
