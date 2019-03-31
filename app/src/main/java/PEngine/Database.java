package PEngine;

//IMAGES:
//ID    title   category    author


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Vector;

public class Database {
    public static final String TABLE_NAME_IMAGES = "Images";
    public static final String TABLE_NAME_SANDBOX = "Sandbox";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME_IMAGES + " (" +
                    "ID"        +  " INTEGER UNIQUE," +
                    "title"     +  " TEXT," +
                    "category"  +  " TEXT," +
                    "author"    +  " TEXT)";
    //private static final String SQL_DELETE_ENTRIES =
    //        "DROP TABLE IF EXISTS " + TABLE_NAME_IMAGES;
    private static final String SQL_CREATE_ENTRIES_SANDBOX =
            "CREATE TABLE " + TABLE_NAME_SANDBOX + " (" +
                    "ID"        +  " INTEGER PRIMARY KEY," +
                    "title"     +  " TEXT," +
                    "category"  +  " TEXT," +
                    "author"    +  " TEXT)";
    //private static final String SQL_DELETE_ENTRIES_SANDBOX =
    //        "DROP TABLE IF EXISTS " + TABLE_NAME_SANDBOX;

    DatabaseHelper dbHelper;
    public Database(Context context)
    {
        dbHelper = new DatabaseHelper(context);
    }
    ///===== PUT =====
    public long put(String tableName, DatabaseEntry newEntry){
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(tableName, null, newEntry.getContentValues());
        if(newEntry.id != -1){
            return newEntry.id;
        }else{
            return newRowId;
        }
    }
    ///===== GET =====
    public Vector<DatabaseEntry> getAuthor(String tableName, String author){
        String selection = "author" + " = ?";
        String[] selectionArgs = { author };
        return get(tableName, selection, selectionArgs);
    }
    public Vector<DatabaseEntry> getCategory(String tableName, String category){
        String selection = "category" + " = ?";
        String[] selectionArgs = { category };
        return get(tableName, selection, selectionArgs);
    }
    public Vector<DatabaseEntry> get(String tableName, long id){
        // Filter results WHERE "title" = 'My Title'
        String selection = "ID" + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        return get(tableName, selection, selectionArgs);
    }
    public Vector<DatabaseEntry> get(String tableName, String selection, String[] selectionArgs){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = DatabaseEntry.getProjection();

        // How you want the results sorted in the resulting Cursor
        //String sortOrder =
        //        FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                tableName,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null/*sortOrder*/               // The sort order
        );

        Vector<DatabaseEntry> ret = new Vector<>();
        while(cursor.moveToNext()) {
            DatabaseEntry entry = new DatabaseEntry(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            /*Log.wtf("Database","ID=" + cursor.getInt(0)+ " Title=" + cursor.getString(1) + " category=" + cursor.getString(2) + " author=" + cursor.getString(3));
            for(int i = 0; i < 3; ++i)
                entry[i] = cursor.getString(i+1);*/
            ret.add(entry);
        }
        cursor.close();

        return ret;
    }

    public String[] getAllCategories(String tableName){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = DatabaseEntry.getProjection();

        // How you want the results sorted in the resulting Cursor
        //String sortOrder =
        //        FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";
        String sortOrder = "category";

        Cursor cursor = db.query(
                tableName,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                "category",                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        String[] ret = new String[cursor.getCount()];
        int retI = 0;
        while(cursor.moveToNext()) {
            ret[retI++] = cursor.getString(cursor.getColumnIndexOrThrow("category"));
        }
        cursor.close();

        return ret;
    }
    public long[] getAllIDs(String tableName){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = new String[]{ "ID" };

        String sortOrder = "ID DESC";

        Cursor cursor = db.query(
                tableName,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        long[] ret = new long[cursor.getCount()];
        int retI = 0;
        while(cursor.moveToNext()) {
            ret[retI++] = cursor.getLong(cursor.getColumnIndexOrThrow("ID"));
        }
        cursor.close();

        return ret;
    }
    //==========
    public boolean deleteID(String tableName, long ID){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.delete(tableName, "ID" + "=" + String.valueOf(ID), null) > 0;
    }

    /*public class Entry{
        int id;
        String title, category, author;
        public Entry(int id, String title, String category, String author){
            this.id = id;
            this.title = title;
            this.category = category;
            this.author = author;
        }


    }*/

    private class DatabaseHelper extends SQLiteOpenHelper{
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "TileArtist.db";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_ENTRIES_SANDBOX);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*// This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);*/
        }
        /*public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }*/

    }
}