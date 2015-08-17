package org.warren.popularmovies;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Warren on 7/24/2015.
 */
public class FetchMoviesService extends IntentService {

    private static final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
    public static final String API_STRING_EXTRA = "api_key";
    public static final String BUNDLE_KEY_REQUEST_RESULT = "request_result";
    public static final String RESULT_RECEIVER = "result_receiver";

    public FetchMoviesService() {
        super("FetchMoviesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String apiKey = (String) intent.getSerializableExtra(API_STRING_EXTRA);
        ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER);
        Uri builtUri = null;
        if (apiKey != null && receiver != null) {
            builtUri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter("sort_by", "popularity.desc")
                    .appendQueryParameter(API_STRING_EXTRA, apiKey)
                    .build();
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        if (builtUri != null) {
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder sb = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    sb.append(line + "\n");
                }

                if (sb.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                moviesJsonStr = sb.toString();
                Bundle resultBundle = new Bundle();
                resultBundle.putString(BUNDLE_KEY_REQUEST_RESULT, moviesJsonStr);
                receiver.send(urlConnection.getResponseCode(), resultBundle);
            } catch (IOException e) {
                Log.e("FetchMoviesService", "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in attempting
                // to parse it.
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("FetchMoviesService", "Error closing stream", e);
                    }
                }
            }
        }
    }
}
