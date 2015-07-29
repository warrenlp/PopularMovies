package org.warren.popularmovies;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String MOVIE_LIST = "movie_list";

    private ImageAdapter mImageAdapter;
    private GridView mGridView;
    private FragmentOnItemClickListener mFragmentOnItemClickListener;
    private FragmentOnItemClickListener mDummyFragmentOnItemClickListener = new FragmentOnItemClickListener() {

        @Override
        public void onItemClick(Bundle bundle) {
            // Do nothing default
        }
    };

    public MainActivityFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof FragmentOnItemClickListener)) {
            throw new IllegalStateException("activity must be of typ FragmentOnItemClickListener");
        }
        mFragmentOnItemClickListener = (FragmentOnItemClickListener) activity;
        mImageAdapter = new ImageAdapter(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && mImageAdapter != null) {
            List<Movie> movies = savedInstanceState.getParcelableArrayList(MOVIE_LIST);
            if (movies != null) {
                mImageAdapter.updateMovieResults(movies);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentOnItemClickListener = mDummyFragmentOnItemClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.main_grid_view);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST, mImageAdapter.getMovieArrayList());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Movie clickedMovie = (Movie) mImageAdapter.getItem(i);

        Bundle movieBundle = new Bundle();
        movieBundle.putParcelable(Movie.CLICKED_MOVIE, clickedMovie);
        mFragmentOnItemClickListener.onItemClick(movieBundle);

        StringBuilder sb = new StringBuilder();
        sb.append("You clicked the poster for \"");
        String originalTitle = clickedMovie.getOriginalTitle();
        sb.append(originalTitle);
        sb.append("\"");
        Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_SHORT).show();
    }

    public void onMovieResultsReady(List<Movie> jsonObjects) {
        mImageAdapter.updateMovieResults(jsonObjects);
    }

    public void sortByPopularity() {
        mImageAdapter.sortByPopularity();
        Toast.makeText(getActivity(), "Sorted By: Popularity", Toast.LENGTH_SHORT).show();
    }

    public void sortByHighestRated() {
        mImageAdapter.sortByHighestRated();
        Toast.makeText(getActivity(), "Sorted By: Highest Rated", Toast.LENGTH_SHORT).show();
    }

    public interface FragmentOnItemClickListener {
        void onItemClick(Bundle bundle);
    }
}
