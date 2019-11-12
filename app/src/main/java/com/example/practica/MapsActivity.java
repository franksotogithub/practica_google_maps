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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.Hex;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPolygon;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spatialite.database.SQLiteDatabase;

public class MapsActivity extends FragmentActivity  implements OnMapReadyCallback ,
        GoogleMap.OnMapClickListener, LocationListener {

    private Button btnSatellite;
    private Button btnNormal;
    private Button btnAgregar;
    private Button btnExportar;

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
    private RequestQueue  mQueue;

    private final static int COLOR_FILL_POLYGON = 0x7F00FF00;
    private final static int COLOR_FILL_POLYGON_GREEN = 0x5500ff00;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnSatellite = (Button) findViewById(R.id.btnSatellite);
        btnNormal = (Button) findViewById(R.id.btnNormal);
        btnAgregar = (Button) findViewById(R.id.btnAgregar);
        btnExportar = (Button) findViewById(R.id.btnExportar);

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
        //line = mMap.addPolyline(new PolylineOptions());
        poligon = mMap.addPolygon(new PolygonOptions().add(new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0)).fillColor(COLOR_FILL_POLYGON).strokeWidth(8));


        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(this);

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAgregar = !checkAgregar;

                if(checkAgregar){
                    listPoints = new ArrayList<>();
                    //poligon.setPoints(listPoints);
                    btnAgregar.setText("GUARDAR");

                }

                else{
                    agregarPoligono(newListPoints);
                    btnAgregar.setText("AGREGAR");
                }

            }
        });

        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    exportarDatos();


            }
        });


    }



    public String formatGeom(ArrayList<LatLng> poligono){

        String format ="";

        for (int i = 0; i <poligono.size() ; i++) {
            if (i >0){
                format = format +"," + poligono.get(i).longitude+ " "+poligono.get(i).latitude;

            }
            else{
                format = poligono.get(i).longitude+ " "+poligono.get(i).latitude;
            }

        }

        return format;

    }

    public  void agregarPoligono(ArrayList<LatLng> poligono){
        Polygon poligonAdd = mMap.addPolygon(new PolygonOptions().add(new LatLng(0, 0), new LatLng(0, 0), new LatLng(0, 0)).fillColor(COLOR_FILL_POLYGON_GREEN).strokeWidth(8));
        poligonAdd.setPoints(poligono);
        String query = "INSERT INTO poligonos(geometry_column,export) VALUES ( GeomFromText('POLYGON(("+formatGeom(poligono)+"))',4326),0);" ;
        Log.d("query",query);
        db.execSQL(query);

    }



    public void exportarDatos(){

        String queryJson = "SELECT AsGeoJSON(geometry_column) geom from poligonos where export=0;" ;
        Cursor res = db.rawQuery( queryJson, null );
        res.moveToFirst();

        while(res.isAfterLast() == false) {

            String campoGeom=res.getString(res.getColumnIndex("geom"));

            String stringJsonFinal = "";

            try {
                JSONObject  geom = new JSONObject(campoGeom);
                String rings=geom.get("coordinates").toString();
                stringJsonFinal = "{\"geometry\":{\"rings\":"+ rings+", \"spatialReference\" : {\"wkid\" : 4326}}}";
                JSONArray arrayGeom = new JSONArray();
                //arrayGeom.put();
                JSONObject obj = new JSONObject(stringJsonFinal);
                arrayGeom.put(obj);
                Log.d("My App", arrayGeom.toString());
                insertarServicio(arrayGeom);


            } catch (Throwable tx) {
                Log.e("My App", "Could not parse malformed JSON: \"" + stringJsonFinal + "\"");
            }



            res.moveToNext();

        }

        String queryJsonUpdate = "UPDATE poligonos SET export =1  where export=0;" ;
        db.execSQL(queryJsonUpdate);
    }


    public void insertarServicio( final JSONArray arrayGeom)

    {
        String url = "http://arcgis4.inei.gob.pe:6080/arcgis/rest/services/DESARROLLO/servicio_prueba_captura/FeatureServer/0/addFeatures";


        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("SUCCESS", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR", error.toString());
            }
        }){

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=utf-8";
            }


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("features", arrayGeom.toString());
                params.put("f", "json");
                return params;
            }

            /*@Override
            public byte[] getBody() throws AuthFailureError{
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }*/

            /*@Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }*/




        };






/*
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, arrayGeom,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("SUCCESS", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERROR", error.toString());
            }
        }
        );*/

       /* JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("employees");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject employee = jsonArray.getJSONObject(i);

                                String firstName = employee.getString("firstname");
                                int age = employee.getInt("age");
                                String mail = employee.getString("mail");

                                mTextViewResult.append(firstName + ", " + String.valueOf(age) + ", " + mail + "\n\n");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });*/

                mQueue.add(request);

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
