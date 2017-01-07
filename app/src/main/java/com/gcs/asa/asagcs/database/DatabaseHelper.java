package com.gcs.asa.asagcs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Alive on 12/25/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "emotion.db";
    public static final String WAYPOINTS_TABLE_NAME = "waypoint_table";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sqlCommand = " CREATE TABLE " + WAYPOINTS_TABLE_NAME
                + "(" +
                "ID INTEGER PRIMARY KEY     AUTOINCREMENT   NOT NULL," +
                "WAYPOINT_ID INT PRIMARY" +
                "TITLE TEXT," +
                "LATITUDE TEXT," +
                "LONGITUDE TEXT," +
                "ALTITUDE TEXT" +
                ")";
        sqLiteDatabase.execSQL(sqlCommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS"+ WAYPOINTS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean createNewWaypoint(int waypointId , String title, String latitude, String longitude, String altitude){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("WAYPOINT_ID",  String.valueOf(waypointId));
        contentValues.put("TITLE",title);
        contentValues.put("LATITUDE", latitude);
        contentValues.put("LONGITUDE", longitude);
        contentValues.put("ALTITUDE", altitude);
        long result = sqLiteDatabase.insert(WAYPOINTS_TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        }
        else{
            return true;
        }
    }

    public void clearWaypoints(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.clear();
    }

    public boolean updateWaypoint(int waypointId , String title, String latitude, String longitude, String altitude){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TITLE",title);
        contentValues.put("LATITUDE", latitude);
        contentValues.put("LONGITUDE", longitude);
        contentValues.put("ALTITUDE", altitude);

        long result = sqLiteDatabase.update(WAYPOINTS_TABLE_NAME, contentValues, "WAYPOINT_ID = ?"
                , new String[]{String.valueOf(waypointId)});
        if( result < 1 )
            return false;
        else
            return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + WAYPOINTS_TABLE_NAME, null);
    }

    public void createWaypoints(int number, String defaultLatitude, String defaultLongitude, String defaultAltitude){
        Cursor cursor = getAllData();
        // if we have to have bigger database
        if(cursor.getCount()<number){
            for (int i = cursor.getCount(); i < number; i++) {
                if(createNewWaypoint(number, String.valueOf(number),defaultLatitude,defaultLongitude,defaultAltitude)==false){
                    Log.d("DataBaseHelper","Way Points Init: Something was wrong on creating bigger database;(");
                }
            }
        }
        // if we have'nt any record in our database
        if(cursor.getCount()==0) {
            for(int i=0; i<number;i++){
                if(createNewWaypoint(number, String.valueOf(number),defaultLatitude,defaultLongitude,defaultAltitude)==false){
                    Log.d("DataBaseHelper","Way Points Init: Something was wrong on creating;(");
                }
            }
        }
    }
}
