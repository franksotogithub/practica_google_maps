package com.example.practica;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPolygon;

import org.json.JSONObject;
import org.spatialite.database.SQLiteDatabase;

public class MapsActivity extends FragmentActivity  implements OnMapReadyCallback ,
        GoogleMap.OnMapClickListener, LocationListener {

    private Button btnSatellite;
    private Button btnNormal;
    private Button btnAgregar;

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE  = 1 ;


    private Polyline line;
    private Polygon poligon;
    private ArrayList<LatLng> listPoints = new ArrayList<LatLng>() ;
    private ArrayList<LatLng> newListPoints = new ArrayList<LatLng>();
    private LocationManager mLocationManager;
    private Location location;
    //private SQLiteDatabase db ;
    private SQLiteDatabase db ;
    //private ConexionSqlLiteHelper conn;
    private ConexionSpatiaLiteHelper conn;

    private boolean checkAgregar=false;

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnSatellite = (Button) findViewById(R.id.btnSatellite);
        btnNormal = (Button) findViewById(R.id.btnNormal);
        btnAgregar = (Button) findViewById(R.id.btnAgregar);

        btnSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

        btnNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        /*View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };*/

        //conn = new ConexionSqlLiteHelper(this);
        conn = new ConexionSpatiaLiteHelper(this);
        db = conn.getWritableDatabase();



        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_UPDATE_MIN_TIME,LOCATION_UPDATE_MIN_DISTANCE,this);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng peru = new LatLng(-9, -74);
        mMap.addMarker(new MarkerOptions().position(peru).title("Marcador en Peru"));



        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);

            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location!=null){
                LatLng gps=new LatLng(location.getLatitude(),location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps,12));
            }
            else{


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(peru,5));

            }






        }
        else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            }

            else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            }


        }


        //mMap.setMyLocationEnabled(true);
        line = mMap.addPolyline(new PolylineOptions());
        poligon = mMap.addPolygon(new PolygonOptions().add(new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0)).fillColor(Color.BLUE).strokeWidth(10));


        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(this);

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAgregar = !checkAgregar;

                if(checkAgregar){
                    listPoints = new ArrayList<>();
                    btnAgregar.setText("GUARDAR");
                }

                else{
                    agregarPoligono(newListPoints);
                    btnAgregar.setText("AGREGAR");
                }

            }
        });


    }



    public String formatGeom(ArrayList<LatLng> poligono){

        String format ="";

        for (int i = 0; i <poligono.size() ; i++) {
            if (i >0){
                format = format +"," + poligono.get(i).latitude+ " "+poligono.get(i).longitude;

            }
            else{
                format = poligono.get(i).latitude+ " "+poligono.get(i).longitude;
            }

        }

        return format;

    }

    public  void agregarPoligono(ArrayList<LatLng> poligono){
        //AGREGAR poligono
        String query = "INSERT INTO poligonos(geometry_column) VALUES ( GeomFromText('POLYGON(("+formatGeom(poligono)+"))',4326));" ;
        Log.d("query",query);
        db.execSQL(query);

        String queryJson = "SELECT AsGeoJSON(geometry_column) geom from poligonos;" ;
        //db.execSQL(queryJson);
        //GeoJsonLineString geoJsonPoligono = new GeoJsonLineString(poligono);
        //ArrayList<ArrayList<LatLng>> final = {poligono} ;
        /*ArrayList<ArrayList<LatLng>> poligonos = new ArrayList<>();
        poligonos.add(poligono);

        GeoJsonPolygon geoJsonPoligono = new GeoJsonPolygon(poligonos);
        Log.d("query",geoJsonPoligono.getCoordinates().toString());
        Log.d("query",query);
        Log.d("query",query);
        geoJsonPoligono.getCoordinates();


*/


        Cursor res = db.rawQuery( queryJson, null );
        res.moveToFirst();
        String  finalres = "";

        while(res.isAfterLast() == false) {

            /*Log.d("geom",res.getString(res.getColumnIndex("geom")));

            */

            String campoGeom=res.getString(res.getColumnIndex("geom"));

            String jsonFinal = "";

            try {
                JSONObject  geom = new JSONObject(campoGeom);
                String rings=geom.get("coordinates").toString();
                jsonFinal = "{\"rings\":"+ rings+", \"spatialReference\" : {\"wkid\" : 4326}}";
                JSONObject obj = new JSONObject(jsonFinal);
                Log.d("My App", obj.toString());

            } catch (Throwable tx) {
                Log.e("My App", "Could not parse malformed JSON: \"" + jsonFinal + "\"");
            }
            res.moveToNext();

        }


        //String json = "{\"phonetype\":3,\"cat\":4}";


        //return array_list;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,
                                            int[] grantResults) {


        if (requestCode == LOCATION_REQUEST_CODE) {
            // ¿Permisos asignados?
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);


            } else {

                Toast.makeText(this, "Error de permisos", Toast.LENGTH_LONG).show();
            }

        }
    }




    /*private  void getCurrentLocation(){
        boolean isGPSEnable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Location location = null;
        if(isGPSEnable){



        }

    }*/


    @Override
    public void onMapClick(LatLng latLng) {

        if(checkAgregar){
            listPoints.add(latLng);
            newListPoints = new ArrayList<LatLng>(listPoints);
            newListPoints.add(listPoints.get(0));
            poligon.setPoints(newListPoints);
        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
