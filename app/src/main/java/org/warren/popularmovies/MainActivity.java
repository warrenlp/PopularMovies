package org.warren.popularmovies;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity  {
    private static final String POSTER_PATH = "poster_path";
    FetchMoviesService mService;
    private ResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReceiver = new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                int httpStatus = resultCode;
                String jsonResult = null;
                List<String> jsonResults = new ArrayList<>();
                if (httpStatus == 200) {
                    jsonResult = resultData.getString(FetchMoviesService.BUNDLE_KEY_REQUEST_RESULT);
                    JSONArray resultsArray = null;
                    try {
                        resultsArray = new JSONArray(new JSONObject(jsonResult).getJSONArray("results"));
                        for (int i=0; i<resultsArray.length(); ++i ) {
                            JSONObject tempJSONObj = resultsArray.getJSONObject(i);
                            String posterPath = tempJSONObj.getString(POSTER_PATH);
                            if (posterPath != null) {
                                jsonResults.add(posterPath);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    maf.onMovieResultsReady(jsonResults);
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        doFetchMoviesGet();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doFetchMoviesGet() {
        Intent intent = new Intent(this, FetchMoviesService.class);
        String apiKey = getResources().getString(R.string.movie_db_api_key);
        intent.putExtra(FetchMoviesService.API_STRING_EXTRA, apiKey);
        intent.putExtra(FetchMoviesService.RESULT_RECEIVER, mReceiver);
    }
}
