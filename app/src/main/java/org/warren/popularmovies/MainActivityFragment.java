package org.warren.popularmovies;

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

    private ImageAdapter mImageAdapter;
    private GridView mGridView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageAdapter = new ImageAdapter(getActivity());
        mGridView = (GridView) getActivity().findViewById(R.id.main_grid_view);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        StringBuilder sb = new StringBuilder();
        sb.append("You clicked the poster for \"");
        String originalTitle = ((Movie) mImageAdapter.getItem(i)).getOriginalTitle();
        sb.append(originalTitle);
        sb.append("\"");
        Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_SHORT).show();
    }

    public void onMovieResultsReady(List<Movie> jsonObjects) {
        mImageAdapter.updateMovieResults(jsonObjects);
    }
}
