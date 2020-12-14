package com.james.gobraille.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.james.gobraille.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DatabaseUtil extends SQLiteOpenHelper {

    private static final String DB_NAME = "braille";
    private static final int DB_VERSION = 1;
    private static DatabaseUtil instance;

    private static final String TABLE_GRADE = "grade_one";

    private static class Column {
        static final String bits = "bits";
        static final String english = "english";
    }

    private static final String CREATE_TABLE_GRADE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GRADE + " ("
            + Column.bits + " TEXT,"
            + Column.english + " TEXT"
            + " )";

    private GradeConversion gradeConversion;
    private Context context;

    private DatabaseUtil(@Nullable Context context) {
        super(context, DB_NAME, (null), DB_VERSION);
        gradeConversion = new GradeConversion();
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GRADE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADE);
        onCreate(db);
    }



    public static DatabaseUtil getInstance(Context context){
        if (instance == null) instance = new DatabaseUtil(context);
        return instance;
    }

    private static class GradeConversion{

        private void insert(String bits, String character){
            SQLiteDatabase sqLiteDatabase = instance.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.bits, character);
            contentValues.put(Column.english, bits);
            sqLiteDatabase.insert(TABLE_GRADE, (null), contentValues);
            sqLiteDatabase.close();
        }

        private void init(){

            try {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(instance.context.getResources().openRawResource(R.raw.braille_english)));
                String line = "";

                while ((line = bufferedReader.readLine()) != null){
                    final String[] arr = line.split("\\:");
                    if (line.contains(",,"))insert(arr[2], (",")); else insert(arr[1], arr[0]);
                }
                bufferedReader.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public String toEnglish(String bitList){

            String result = "";
            SQLiteDatabase sqLiteDatabase = instance.getWritableDatabase();
            final String query = "SELECT " + Column.english + " from " + TABLE_GRADE + " WHERE " + Column.bits + " = " + "'" + bitList + "'";
            final Cursor cursor = sqLiteDatabase.rawQuery(query, (null));
            while (cursor.moveToNext())
                   result = cursor.getString((0));
            cursor.close();
            sqLiteDatabase.close();
            return result;
        }
    }


    public String gradeText(String list){
        return gradeConversion.toEnglish(list);
    }


    public void initDatabase(){
        gradeConversion.init();
    }

}
