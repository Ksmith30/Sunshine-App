package com.example.android.sunshine.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.android.sunshine.data.WeatherContract;

public class SunshineSyncUtils {

    private static boolean sInitialized;

    @SuppressLint("StaticFieldLeak")
    synchronized public static void initialize(final Context context) {

        if (sInitialized) return;

        if (!sInitialized) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {

                    Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                    String[] projection = {WeatherContract.WeatherEntry._ID};
                    String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                    Cursor cursor = context.getContentResolver().query(
                            forecastQueryUri,
                            projection,
                            selectionStatement,
                            null,
                            null);

                    if (null == cursor || cursor.getCount() == 0) {
                        startImmediateSync(context);
                    }

                    cursor.close();
                    return null;
                }
            }.execute();

            sInitialized = true;
        }
    }

    public static void startImmediateSync(final Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
