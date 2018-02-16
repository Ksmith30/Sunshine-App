package com.example.android.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
