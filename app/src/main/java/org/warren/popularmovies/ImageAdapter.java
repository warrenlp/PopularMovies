package org.warren.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Warren on 7/24/2015.
 */
public class ImageAdapter extends BaseAdapter {

    public static final String BASE_URL = "http://image.tmdb.org/t/p/w185";

    private Context mContext;
    private List<Movie> mAllMovies;
    private List<Movie> mShowMovies;
    private List<Movie> mFavoriteMovies;
    private boolean mShowFavorites;
    private JSONArray mFavoriteMoviesJSONArray;

    public ImageAdapter(Context c) {
        mContext = c;
        mAllMovies = new ArrayList<>();
        mShowMovies = new ArrayList<>();
        mFavoriteMovies = new ArrayList<>();
        mShowFavorites = false;
    }

    @Override
    public int getCount() {
        return mShowMovies.size();
    }

    @Override
    public Object getItem(int i) {
        return mShowMovies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public ArrayList<Movie> getMovieArrayList() {
        return (ArrayList<Movie>) mAllMovies;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView squaredImageView;
        if (view == null) {
            squaredImageView = new ImageView(mContext);
            squaredImageView.setLayoutParams(new GridView.LayoutParams(300, 300));
            squaredImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            squaredImageView = (ImageView) view;
        }
        String url = BASE_URL + ((Movie)getItem(i)).getPosterPath();
        Picasso.with(mContext).load(url).into(squaredImageView);
        return squaredImageView;
    }

    public void updateMovieResults(List<Movie> movies) {
        mAllMovies = movies;
        mShowMovies = mAllMovies;
        notifyDataSetChanged();
    }

    public void sortByPopularity() {
        mShowMovies = mAllMovies;
        Collections.sort(mShowMovies, new SortByPopularityComparator());
        notifyDataSetChanged();
    }

    public void sortByHighestRated() {
        mShowMovies = mAllMovies;
        Collections.sort(mShowMovies, new SortByHighestRatedComparator());
        notifyDataSetChanged();
    }

    public void setShowFavorites(boolean showFavorites) {
        this.mShowFavorites = showFavorites;
    }

    public boolean getShowFavorites() {
        return mShowFavorites;
    }

    public void updateFavoriteMovies(JSONArray favoriteMovies) {
        mFavoriteMoviesJSONArray = favoriteMovies;
        parseFavoriteMoviesJSONArray();
        mShowMovies = mFavoriteMovies;
        notifyDataSetChanged();
    }

    private void parseFavoriteMoviesJSONArray() {
        mFavoriteMovies.clear();
        for (int i=0; i<mFavoriteMoviesJSONArray.length(); ++i) {
            for (Movie movie : mAllMovies) {
                int favoriteMovieID = 0;
                try {
                    favoriteMovieID = (int) mFavoriteMoviesJSONArray.get(i);
                    if (movie.getId() == favoriteMovieID) {
                        mFavoriteMovies.add(movie);
                        break;
                    }
                } catch (JSONException e) {
                    throw new IllegalStateException(e.toString());
                }
            }
        }
    }

    public String getJSONFavoritesString() {
        if (mFavoriteMoviesJSONArray == null) {
            return null;
        } else {
            return mFavoriteMoviesJSONArray.toString();
        }
    }

    private static class SortByPopularityComparator implements Comparator<Movie> {

        @Override
        public int compare(Movie lhs, Movie rhs) {
            return -1 * lhs.getPopularity().compareTo(rhs.getPopularity());
        }
    }

    private static class SortByHighestRatedComparator implements Comparator<Movie> {

        @Override
        public int compare(Movie lhs, Movie rhs) {
            return -1 * lhs.getVoteAverage().compareTo(rhs.getVoteAverage());
        }
    }
}
