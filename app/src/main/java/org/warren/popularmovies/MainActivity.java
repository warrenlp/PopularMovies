package org.warren.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.FragmentOnItemClickListener  {
    private static final String ID = "id";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";
    private static final String POSTER_PATH = "poster_path";
    private static final String POPULARITY = "popularity";
    private static final String VOTE_AVERAGE = "vote_average";

    private ResultReceiver mReceiver;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivityFragment maf = new MainActivityFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, maf);
        fragmentTransaction.commit();

        mReceiver = new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                int httpStatus = resultCode;
                String jsonResult = null;
                List<Movie> jsonResults = new ArrayList<>();
                if (httpStatus == 200) {
                    jsonResult = resultData.getString(FetchMoviesService.BUNDLE_KEY_REQUEST_RESULT);
                    JSONArray resultsArray = null;
                    try {
                        JSONObject rootObject = new JSONObject(jsonResult);
                        resultsArray = rootObject.getJSONArray("results");
                        for (int i=0; i<resultsArray.length(); ++i ) {
                            JSONObject tempJSONObj = resultsArray.getJSONObject(i);
                            int id = tempJSONObj.getInt(ID);
                            String originalTitle = tempJSONObj.getString(ORIGINAL_TITLE);
                            String overView = tempJSONObj.getString(OVERVIEW);
                            String releaseDateString = tempJSONObj.getString(RELEASE_DATE);
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            Date releaseDate = null;
                            try {
                                releaseDate = df.parse(releaseDateString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return;
                            }
                            String posterPath = tempJSONObj.getString(POSTER_PATH);
                            String popularityString = tempJSONObj.getString(POPULARITY);
                            Double popularity = Double.parseDouble(popularityString);
                            String voteAvgString = tempJSONObj.getString(VOTE_AVERAGE);
                            Double voteAvg = Double.parseDouble(voteAvgString);
                            Movie movie = new Movie(id, originalTitle, overView, releaseDate, posterPath, popularity, voteAvg);
                            jsonResults.add(movie);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    maf.onMovieResultsReady(jsonResults);
                }
            }
        };

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
                        if (backStackEntryCount < 1) {
                            showOverMenu(true);
                        }
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    public void showOverMenu(boolean showMenu) {
        if (mMenu == null) {
            return;
        }
        mMenu.setGroupEnabled(R.id.main_menu_group, showMenu);
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
        if (id == R.id.sort_by_popularity) {
            MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            maf.sortByPopularity();
            return true;
        } else if (id == R.id.sort_by_highest_rated) {
            MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            maf.sortByHighestRated();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doFetchMoviesGet() {
        Intent intent = new Intent(this, FetchMoviesService.class);
        String apiKey = getResources().getString(R.string.movie_db_api_key);
        intent.putExtra(FetchMoviesService.API_STRING_EXTRA, apiKey);
        intent.putExtra(FetchMoviesService.RESULT_RECEIVER, mReceiver);
        startService(intent);
    }

    @Override
    public void onItemClick(Bundle bundle) {
        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, detailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        showOverMenu(false);
    }
}
