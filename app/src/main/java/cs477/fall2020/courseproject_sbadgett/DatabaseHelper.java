package cs477.fall2020.courseproject_sbadgett;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "CourseProject2DB";
    public static final String TABLE_NAME = "Sessions";
    public static final String TABLE_NAME2 = "LastN";
    public static final String Col_2 = "AverageN";
    public static final String Col_1 = "NumRounds";
    public static final String Col = "N";
    public static final String Col2 = "DecThresh";
    public static final String Col3 = "IncThresh";

    //Build string used to create the database table to store session data
    private static final String CREATE_CMD1 = "create table " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + Col_1 + " INTEGER NOT NULL, " +
            Col_2 + " REAL NOT NULL)";

    //Build string used to create table to store settings
    private static final String CREATE_CMD2 = "create table " + TABLE_NAME2 + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + Col + " INTEGER NOT NULL, " + Col2 + " INTEGER NOT NULL, " + Col3 + " INTEGER NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context,DB_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creates the tables
        db.execSQL(CREATE_CMD1);
        db.execSQL(CREATE_CMD2);

        //Initialize starting N of 2
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col, 2);
        contentValues.put(Col2, 50);
        contentValues.put(Col3, 80);
        db.insert(TABLE_NAME2,null, contentValues);

        //Add starting data to test progress functionality
        contentValues = new ContentValues();
        contentValues.put(Col_1, 8);
        contentValues.put(Col_2, 2);
        db.insert(TABLE_NAME,null, contentValues);
        contentValues = new ContentValues();
        contentValues.put(Col_1, 10);
        contentValues.put(Col_2, 3);
        db.insert(TABLE_NAME,null, contentValues);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        onCreate(db);
    }
}
