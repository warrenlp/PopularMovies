package org.warren.popularmovies;

/**
 * Created by warre_000 on 7/26/2015.
 */
public class Movie {

    private int mId;
    private String mOriginalTitle;
    private String mPosterPath;

    public Movie(int id, String originalTitle, String posterPath) {
        mId = id;
        mOriginalTitle = originalTitle;
        mPosterPath = posterPath;
    }

    public int getId() {
        return mId;
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public String getPosterPath() {
        return mPosterPath;
    }
}
