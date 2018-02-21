package com.example.android.sunshine;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import org.w3c.dom.Text;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecast;
    private String[] mWeatherData = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    private static final int COLUMN_DATE = 1;
    private static final int COLUMN_WEATHER = 2;
    private static final int COLUMN_HIGH = 3;
    private static final int COLUMN_LOW = 4;
    private static final int COLUMN_HUMIDITY = 5;
    private static final int COLUMN_WIND = 6;
    private static final int COLUMN_PRESSURE = 7;
    private static final int COLUMN_DEGREES = 8;

    private static final int LOADER_ID = 84;

    private Uri mUri;

    private TextView mDate;
    private TextView mDescription;
    private TextView mHigh;
    private TextView mLow;
    private TextView mHumidity;
    private TextView mWind;
    private TextView mPressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDate = (TextView) findViewById(R.id.selected_day_date);
        mDescription = (TextView) findViewById(R.id.selected_day_weather_description);
        mHigh = (TextView) findViewById(R.id.selected_day_high_temp);
        mLow = (TextView) findViewById(R.id.selected_day_low_temp);
        mHumidity = (TextView) findViewById(R.id.selected_day_humidity);
        mWind = (TextView) findViewById(R.id.selected_day_wind);
        mPressure = (TextView) findViewById(R.id.selected_day_pressure);

        Intent intentThatStartedThisActivity = getIntent();
        mUri = intentThatStartedThisActivity.getData();
        if (mUri == null) {
            throw new NullPointerException("There is no Uri in the intent!");
        }

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecast + FORECAST_SHARE_HASHTAG)
                .getIntent();
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case LOADER_ID:

                return new CursorLoader(this,
                        mUri,
                        mWeatherData,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented" + LOADER_ID);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        } if (!cursorHasValidData) {
            return;
        }

        long localDateMidnightGmt = data.getLong(COLUMN_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt,
                true);
        mDate.setText(dateText);

        int weatherID = data.getInt(COLUMN_WEATHER);
        String weatherDescription = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherID);
        mDescription.setText(weatherDescription);

        double highTemp = data.getDouble(COLUMN_HIGH);
        String highString= SunshineWeatherUtils.formatTemperature(this, highTemp);
        mHigh.setText(highString);

        double lowTemp = data.getDouble(COLUMN_LOW);
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowTemp);
        mLow.setText(lowString);

        float humidity = data.getFloat(COLUMN_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        mHumidity.setText(humidityString);

        float windSpeed = data.getFloat(COLUMN_WIND);
        float windDirection = data.getFloat(COLUMN_DEGREES);
        String wind = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        mWind.setText(wind);

        float pressure = data.getFloat(COLUMN_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        mPressure.setText(pressureString);

        mForecast = String.format("%s - %s - %s/%s",
                dateText, weatherDescription, highString, lowString);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}