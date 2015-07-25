package org.warren.popularmovies;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Warren on 7/24/2015.
 */
public class FetchMoviesService extends IntentService {

    private static final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
    public static final String API_STRING_EXTRA = "api_key";
    public static final String BUNDLE_KEY_REQUEST_RESULT = "request_result";
    public static final String RESULT_RECEIVER = "result_receiver";

    private final IBinder mBinder = new FetchMoviesBinder();


    public FetchMoviesService() {
        super("FetchMoviesService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        String apiKey = (String) intent.getSerializableExtra(API_STRING_EXTRA);
        ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER);
        if (apiKey != null && receiver != null) {
            Uri builtUri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter("sort_by", "popularity.desc")
                    .appendQueryParameter(API_STRING_EXTRA, apiKey)
                    .build();
        }
    }

    public class FetchMoviesBinder extends Binder {
        FetchMoviesService handleMovieResults() {
            return FetchMoviesService.this;
        }
    }

    public interface FetchMoviesServiceHandler {
        void handleResults(List<JSONObject> jsonObjects);
    }

}
