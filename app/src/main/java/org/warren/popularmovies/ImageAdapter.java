package org.warren.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Warren on 7/24/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private static final String BASE_URL = "http://image.tmdb.org/t/p/w185";

    private Context mContext;
    private List<String> mMovieURLs;

    public ImageAdapter(Context c) {
        mContext = c;
        mMovieURLs = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mMovieURLs.size();
    }

    @Override
    public Object getItem(int i) {
        return mMovieURLs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView squaredImageView;
        if (view == null) {
            squaredImageView = new ImageView(mContext);
            squaredImageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            squaredImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            squaredImageView.setPadding(8, 8, 8, 8);
            String url = BASE_URL + getItem(i);
            Picasso.with(mContext).load(url).into(squaredImageView);
        } else {
            squaredImageView = (ImageView) view;
        }

        return squaredImageView;
    }

    public void updateMovieResults(List<String> movieURLs) {
        mMovieURLs = movieURLs;
        notifyDataSetChanged();
    }
}
