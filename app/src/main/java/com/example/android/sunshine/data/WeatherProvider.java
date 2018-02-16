package com.example.android.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.sunshine.utilities.SunshineDateUtils;

public class WeatherProvider extends ContentProvider {

    private static final int CODE_WEATHER = 10;
    private static final int CODE_WEATHER_WITH_DATE = 11;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    WeatherDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        uriMatcher.addURI(WeatherContract.AUTHORITY, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sURIMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case CODE_WEATHER:
                cursor = queryWeather(db, projection, selection, selectionArgs, sortOrder);
                break;
            case CODE_WEATHER_WITH_DATE:
                cursor = queryWeatherWithDate(db, uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public Cursor queryWeather(SQLiteDatabase db, @Nullable String[] projection, @Nullable String selection,
                              @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        cursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        return cursor;
    }

    public Cursor queryWeatherWithDate(SQLiteDatabase db, @NonNull Uri uri, @Nullable String[] projection,
                                       @Nullable String sortOrder) {
        Cursor cursor;
        String normalizedUtcDateString = uri.getLastPathSegment();
        String[] selectionArguments = new String[]{normalizedUtcDateString};
        cursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,
                projection,
                WeatherContract.WeatherEntry.COLUMN_DATE + " = ?",
                selectionArguments,
                null,
                null,
                sortOrder);
        return cursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int rowsInserted = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sURIMatcher.match(uri)) {

            case CODE_WEATHER:
                db.beginTransaction();
                rowsInserted = bulkInsertWeather(values, db, rowsInserted, uri);
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    public int bulkInsertWeather(ContentValues[] values, SQLiteDatabase db, int rowsInserted, Uri uri) {
        try {
            for (ContentValues cv : values) {
                checkWeather(cv);
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, cv);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    public void checkWeather(ContentValues contentValues) {
        long weatherDate =
                contentValues.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
        if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
            throw new IllegalArgumentException("Date must be normalized to insert!");
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String whereClause, @Nullable String[] whereArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        int rowsDeleted = 0;

        if (null == whereClause) whereClause = "1";

        switch (match) {
            case CODE_WEATHER:
                rowsDeleted = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return  rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
