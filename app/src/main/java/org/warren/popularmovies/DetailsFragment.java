package org.warren.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by warre_000 on 7/26/2015.
 * Movie details layout contains title, release date, movie poster, vote average, and plot synopsis.
 */
public class DetailsFragment extends Fragment {

    private Movie mMovie;

    public DetailsFragment() {
        // Do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(Movie.CLICKED_MOVIE)) {
            mMovie = getArguments().getParcelable(Movie.CLICKED_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.details_fragment, container, false);

        if (mMovie != null) {
            Context context = getActivity();
            String url = ImageAdapter.BASE_URL + mMovie.getPosterPath();
            final ImageView localImageView = (ImageView) rootView.findViewById(R.id.poster);
            Picasso.with(context)
                    .load(url)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            Bitmap bMapScaled = Bitmap.createScaledBitmap(bitmap, 400, 700, true);
                            localImageView.setImageBitmap(bMapScaled);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
            // TODO: Adjust image size to be approx. 2x as large.
            ((TextView)rootView.findViewById(R.id.original_title)).setText(mMovie.getOriginalTitle());
            ((TextView)rootView.findViewById(R.id.release_date)).setText(mMovie.getReleaseDateString());
            ((TextView)rootView.findViewById(R.id.vote_average)).setText(Double.toString(mMovie.getVoteAverage()));
            ((TextView)rootView.findViewById(R.id.overview)).setText(mMovie.getOverview());
        }

        return rootView;
    }
}
