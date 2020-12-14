package com.pruthvi.gobraille.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pruthvi.gobraille.Constants;
import com.pruthvi.gobraille.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.CheckedOutputStream;

public class DatabaseUtil extends SQLiteOpenHelper {

    private static final String DB_NAME = "braille";
    private static final int DB_VERSION = 1;
    private static DatabaseUtil instance;

    private static final String TABLE_GRADE_ONE = "grade_one";
    private static final String TABLE_GRADE_TWO = "grade_two";

    private static class Column {
        static final String bits = "bits";
        static final String english = "english";
    }

    private static final String CREATE_TABLE_GRADE_ONE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GRADE_ONE + " ("
            + Column.bits + " TEXT,"
            + Column.english + " TEXT"
            + " )";

    private static final String CREATE_TABLE_GRADE_TWO = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GRADE_TWO + " ("
            + Column.bits + " TEXT,"
            + Column.english + " TEXT"
            + " )";

    private GradeOne gradeOne;
    private Context context;

    private DatabaseUtil(@Nullable Context context) {
        super(context, DB_NAME, (null), DB_VERSION);
        gradeOne = new GradeOne();
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GRADE_ONE);
        db.execSQL(CREATE_TABLE_GRADE_TWO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADE_ONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADE_TWO);
        onCreate(db);
    }



    public static DatabaseUtil getInstance(Context context){
        if (instance == null) instance = new DatabaseUtil(context);
        return instance;
    }



    private static class GradeOne{

        private void insert(String bits, String character){
            SQLiteDatabase sqLiteDatabase = instance.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.bits, bits);
            contentValues.put(Column.english, character);
            sqLiteDatabase.insert(TABLE_GRADE_ONE, (null), contentValues);
            sqLiteDatabase.close();
        }

        private void init(){

            try {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(instance.context.getResources().openRawResource(R.raw.braille_grade_one)));
                String line = "";

                while ((line = bufferedReader.readLine()) != null){
                    final String[] arr = line.split("\\,");
                    if (line.contains(",,"))insert(arr[2], (",")); else insert(arr[1], arr[0]);
                }
                bufferedReader.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public String toEnglish(ArrayList<String> bitList){

            String result = "";
            SQLiteDatabase sqLiteDatabase = instance.getWritableDatabase();

            for (String bit : bitList){

               final String query = "SELECT " + Column.english + " from " + TABLE_GRADE_ONE + " WHERE " + Column.bits + " = " + "'" + bit + "'";
               final Cursor cursor = sqLiteDatabase.rawQuery(query, (null));
               while (cursor.moveToNext()){
                   result += cursor.getString((0));
                   break;
               }
               cursor.close();
            }

            sqLiteDatabase.close();
            return result;
        }
    }


    public String gradeOneText(ArrayList<String> list){
        return gradeOne.toEnglish(list);
    }


    public void initDatabase(){
        gradeOne.init();
    }

}
