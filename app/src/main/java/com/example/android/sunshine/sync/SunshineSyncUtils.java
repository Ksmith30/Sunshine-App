package com.example.android.sunshine.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.android.sunshine.data.WeatherContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class SunshineSyncUtils {

    private static final int HOURS_TO_SYNC = 4;
    private static final int SECONDS_TO_SYNC = (int) TimeUnit.HOURS.toSeconds(HOURS_TO_SYNC);
    private static final int FLEXTIME_SECONDS = SECONDS_TO_SYNC / 3;

    private static boolean sInitialized;

    private static final String SYNC_JOB = "sync-job";

     private static void scheduleWeatherSync(final Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job weatherSyncJob = dispatcher.newJobBuilder()
                .setService(SunshineFirebaseJobService.class)
                .setTag(SYNC_JOB)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow(SECONDS_TO_SYNC, SECONDS_TO_SYNC + FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.schedule(weatherSyncJob);
    }

    @SuppressLint("StaticFieldLeak")
    synchronized public static void initialize(final Context context) {

        if (sInitialized) return;
        sInitialized = true;

        scheduleWeatherSync(context);

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


        }
    }

    public static void startImmediateSync(final Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
