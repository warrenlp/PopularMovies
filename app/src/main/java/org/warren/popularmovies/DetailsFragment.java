package org.warren.popularmovies;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by warre_000 on 7/26/2015.
 * Movie details layout contains title, release date, movie poster, vote average, and plot synopsis.
 */
public class DetailsFragment extends Fragment {

    private Movie mMovie;
    private Button mFavoriteButton;
    private ViewSwitcher mTrailersViewSwitcher;
    private ViewSwitcher mReviewsViewSwitcher;
    private FragmentSetAsFavoriteListener mFragmentSetAsFavoriteListener;
    private FragmentSetAsFavoriteListener mDummyFragmentSetAsFavoriteListener = new FragmentSetAsFavoriteListener() {
        @Override
        public void onSetFavorite(Movie movie) {
            // Do nothing default
        }
    };

    public DetailsFragment() {
        // Do nothing
    }

    public Movie getMovie() {
        return mMovie;
    }

    public ViewSwitcher getTrailersViewSwitcher() {
        return mTrailersViewSwitcher;
    }

    public ViewSwitcher getReviewsViewSwitcher() {
        return mReviewsViewSwitcher;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof FragmentSetAsFavoriteListener)) {
            throw new IllegalStateException("Activity must be of type FragmentSetAsFavoriteListener");
        }
        mFragmentSetAsFavoriteListener = (FragmentSetAsFavoriteListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(Movie.CLICKED_MOVIE)) {
            mMovie = getArguments().getParcelable(Movie.CLICKED_MOVIE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Movie.CLICKED_MOVIE, mMovie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentSetAsFavoriteListener = mDummyFragmentSetAsFavoriteListener;
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
            ((TextView)rootView.findViewById(R.id.original_title)).setText(mMovie.getOriginalTitle());
            ((TextView)rootView.findViewById(R.id.release_date)).setText(mMovie.getReleaseDateString());
            ((TextView)rootView.findViewById(R.id.vote_average)).setText(Double.toString(mMovie.getVoteAverage()));
            ((TextView)rootView.findViewById(R.id.overview)).setText(mMovie.getOverview());
            mTrailersViewSwitcher = ((ViewSwitcher)rootView.findViewById(R.id.trailersViewSwitcher));
            mReviewsViewSwitcher = ((ViewSwitcher)rootView.findViewById(R.id.reviewsViewSwitcher));
            mFavoriteButton = (Button) rootView.findViewById(R.id.mark_as_fav_btn);
            if (mMovie.isFavorite()) {
                String unmarkText = getResources().getString(R.string.unmark_as_favorite);
                mFavoriteButton.setText(unmarkText);
            }
        }

        String api_key = getResources().getString(R.string.movie_db_api_key);
        new RetrieveTrailersAsyncTask(this, api_key).execute(mMovie.getId());
        new RetrieveReviewsAsyncTask(this, api_key).execute(mMovie.getId());

        return rootView;
    }

    public void setCurrentMovieAsFavorite() {
        StringBuilder sb = new StringBuilder();
        sb.append(mMovie.getOriginalTitle());
        sb.append(" is ");
        if (mMovie.isFavorite()) {
            sb.append("not ");
            String markText = getResources().getString(R.string.mark_as_favorite);
            mFavoriteButton.setText(markText);
            mMovie.setFavorite(false);
        } else {
            String unmarkText = getResources().getString(R.string.unmark_as_favorite);
            mFavoriteButton.setText(unmarkText);
            mMovie.setFavorite(true);
        }
        sb.append("a favorite.");
        Toast.makeText(getActivity(), sb.toString(), Toast.LENGTH_SHORT).show();
        mFragmentSetAsFavoriteListener.onSetFavorite(mMovie);
    }

    public interface FragmentSetAsFavoriteListener {
        void onSetFavorite(Movie movie);
    }
}
