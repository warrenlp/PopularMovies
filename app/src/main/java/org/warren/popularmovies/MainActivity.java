package org.warren.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.FragmentOnItemClickListener,
        DetailsFragment.FragmentSetAsFavoriteListener {
    private static final String ID = "id";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";
    private static final String POSTER_PATH = "poster_path";
    private static final String POPULARITY = "popularity";
    private static final String VOTE_AVERAGE = "vote_average";
    public static final String SHOW_OVER_MENU = "show_over_menu";
    public static final String THIS_APP = "popular_movies";
    public static final String FAVORITE_MOVIES = "favorite_movies";

    private ResultReceiver mReceiver;
    private Menu mMenu;
    private boolean mMenuGroupVisible;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_detail_container) != null) {
            mTwoPane = true;
        }

        if (savedInstanceState == null) {
            MainActivityFragment maf = new MainActivityFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, maf);
            fragmentTransaction.commit();
            mMenuGroupVisible = true;

            mReceiver = new ResultReceiver(new Handler()) {

                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    String jsonResult = null;
                    List<Movie> jsonResults = new ArrayList<>();
                    if (resultCode == 200) {
                        jsonResult = resultData.getString(FetchMoviesService.BUNDLE_KEY_REQUEST_RESULT);
                        JSONArray resultsArray = null;
                        try {
                            JSONObject rootObject = new JSONObject(jsonResult);
                            resultsArray = rootObject.getJSONArray("results");
                            for (int i = 0; i < resultsArray.length(); ++i) {
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
        }
        else {
            mMenuGroupVisible = savedInstanceState.getBoolean(SHOW_OVER_MENU);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
                        if (backStackEntryCount < 1) {
                            mMenuGroupVisible = true;
                            showOverMenu();
                        }
                    }
                });

        doFetchMoviesGet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        showOverMenu();
        return true;
    }

    public void showOverMenu() {
        if (mMenu == null) {
            return;
        }
        mMenu.setGroupEnabled(R.id.main_menu_group, mMenuGroupVisible);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SHOW_OVER_MENU, mMenuGroupVisible);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.sort_by_popularity) {
            MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            maf.sortByPopularity();
            return true;
        } else if (id == R.id.sort_by_highest_rated) {
            MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            maf.sortByHighestRated();
            return true;
        } else if (id == R.id.view_favorites) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String favoriteMovies = sharedPreferences.getString(FAVORITE_MOVIES, null);
            if (favoriteMovies == null) {
                Toast.makeText(this, "You have not selected any favorites yet!", Toast.LENGTH_SHORT). show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(favoriteMovies);
                    if (jsonArray.length() > 0) {
                        MainActivityFragment maf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        maf.viewFavorites(jsonArray);
                    } else {
                        Toast.makeText(this, "You have not selected any favorites yet!", Toast.LENGTH_SHORT). show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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
        if (mTwoPane) {
            fragmentTransaction.replace(R.id.fragment_detail_container, detailsFragment);
        } else {
            fragmentTransaction.replace(R.id.fragment_container, detailsFragment);
            fragmentTransaction.addToBackStack(null);
            mMenuGroupVisible = false;
            showOverMenu();
        }
        fragmentTransaction.commit();
    }

    // onClick method from DetailsFragment
    public void setMovieAsFavorite(View view) {
        DetailsFragment df = null;
        if (mTwoPane) {
            df = (DetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_detail_container);
        } else {
            df = (DetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }
        df.setCurrentMovieAsFavorite();
    }

    @Override
    public void onSetFavorite(Movie movie) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String favoriteMovies = sharedPreferences.getString(FAVORITE_MOVIES, null);
        int movieID = movie.getId();
        boolean updateSet = false;
        JSONArray jsonArray = null;
        if (favoriteMovies == null) {
            jsonArray = new JSONArray();
            jsonArray.put(movieID);
            updateSet = true;
        } else {
            try {
                jsonArray = new JSONArray(favoriteMovies);
                if (movie.isFavorite()) {
                    updateSet = true;
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        int currentMovieID = (int) jsonArray.get(i);
                        if (currentMovieID == movieID) {
                            updateSet = false;
                            break;
                        }
                    }
                    if (updateSet) {
                        jsonArray.put(movieID);
                    }
                } else {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        int currentMovieID = (int) jsonArray.get(i);
                        if (currentMovieID == movieID) {
                            updateSet = true;
                            jsonArray.remove(i);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (updateSet) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(FAVORITE_MOVIES, jsonArray.toString());
            editor.apply();
        }
    }

    public void markFavoriteMovies(List<Movie> movies) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String favoriteMovies = sharedPreferences.getString(FAVORITE_MOVIES, null);

        if (favoriteMovies != null) {
            try {
                JSONArray jsonArray = new JSONArray(favoriteMovies);
                for (Movie movie : movies) {
                    int currentMovieID = movie.getId();
                    for (int i=0; i<jsonArray.length(); ++i) {
                        int jsonMovieId = (int) jsonArray.get(i);
                        if (currentMovieID == jsonMovieId) {
                            movie.setFavorite(true);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
