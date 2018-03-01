package com.example.android.sunshine.sync;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class SunshineFirebaseJobService extends JobService {

    AsyncTask<Void, Void, Void> mFetchWeatherTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters job) {

        final boolean needsRescheduling = false;

        mFetchWeatherTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
               SunshineSyncTask.syncWeather(getApplicationContext());
               return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, needsRescheduling);
            }
        };

        mFetchWeatherTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        boolean mayInterruptIfRunning = true;

        if (mFetchWeatherTask != null) {
            mFetchWeatherTask.cancel(mayInterruptIfRunning);
        }
        return true;
    }
}
