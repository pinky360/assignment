package com.hit.demo.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hit.demo.Model.Movie;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movieDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PLAYLIST_LAYOUT = "TABLE_PLAYLIST_LAYOUT";
    private static final String moviePoster = "moviePoster";
    private static final String movieTitle = "movieTitle";
    private static final String moviePopularity = "moviePopularity";
    private static final String movieRating = "movieRating";
    private static final String id = "movieId";
    private static final String PID = "PID";

    private Context context;
    private SQLiteDatabase db;
    private ContentValues values, values1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PlAYLIST_TABLE = " CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLIST_LAYOUT +
                " (" + id + " INTEGER PRIMARY KEY, "
                + moviePoster + " VARCHAR, "
                + movieTitle + " VARCHAR, "
                + moviePopularity + " VARCHAR, "
                + movieRating + " VARCHAR)";
        db.execSQL(CREATE_PlAYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_LAYOUT);
            onCreate(db);
        }
    }

    public long addMovieList(Movie movie) {
        db = this.getWritableDatabase();
        values1 = new ContentValues();
        values1.put(id, movie.getId());
        values1.put(moviePoster, movie.getPoster());
        values1.put(movieTitle, movie.getTitle());
        values1.put(moviePopularity, movie.getPopularity());
        values1.put(movieRating, movie.getRating());

        long l = db.insert(TABLE_PLAYLIST_LAYOUT, null, values1);
        db.close();
        return l;
    }

    public List<Movie> getMovieData() {
        List<Movie> listBundle = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT  * FROM TABLE_PLAYLIST_LAYOUT", null);
        Movie map;
        while (c.moveToNext()) {
            map = new Movie(c.getString(c.getColumnIndex(id))
                    , c.getString(c.getColumnIndex(moviePoster))
                    , c.getString(c.getColumnIndex(movieTitle))
                    , c.getString(c.getColumnIndex(moviePopularity))
                    , c.getString(c.getColumnIndex(movieRating)));
            listBundle.add(map);
        }
        db.close();
        return listBundle;
    }
}
