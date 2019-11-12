package com.example.practica;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.spatialite.database.SQLiteDatabase;
import org.spatialite.database.SQLiteOpenHelper;

import java.util.ArrayList;

public class ConexionSpatiaLiteHelper extends SQLiteOpenHelper {

    //final String CREAR_TABLA = "CREATE TABLE ";
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "geo_db2.sqlite";
    private static final String TABLE_NAME = "poligonos";
    private static final String KEY_ID = "geometry_column";
    private static final String FLAG_EXPORT = "export";
    private static final String KEY_ID_TYPE = "POLYGON";
    private static final String FLAG_EXPORT_TYPE = "INT";
    //private static final String KEY_ID = "geometry_column";
    //private static final String KEY_ID_TYPE = "POLYGON";

    //public ConexionSpatiaLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    public ConexionSpatiaLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE ="CREATE TABLE "+TABLE_NAME+"("+ KEY_ID+" "+KEY_ID_TYPE+","+FLAG_EXPORT+" "+FLAG_EXPORT_TYPE +" )";
        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }
}
