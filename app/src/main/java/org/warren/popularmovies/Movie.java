package org.warren.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by warre_000 on 7/26/2015.
 */
public class Movie implements Parcelable {

    public static final String CLICKED_MOVIE = "clicked_movie";
    private int mId;
    private String mOriginalTitle;
    private String mOverview;
    private Date mReleaseDate;
    private String mPosterPath;
    private Double mPopularity;
    private Double mVoteAverage;
    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");

    public Movie(int id, String originalTitle, String overview, Date releaseDate, String posterPath, Double popularity, Double voteAverage) {
        mId = id;
        mOriginalTitle = originalTitle;
        mOverview = overview;
        mReleaseDate = releaseDate;
        mPosterPath = posterPath;
        mPopularity = popularity;
        mVoteAverage = voteAverage;
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

    public Double getPopularity() { return mPopularity; }

    public String getOverview() {
        return mOverview;
    }

    public Date getReleaseDate() {
        return mReleaseDate;
    }

    public String getReleaseDateString() {
        return mSimpleDateFormat.format(mReleaseDate);
    }

    public Double getVoteAverage() {
        return mVoteAverage;
    }

    public static final Parcelable.Creator<Movie> CREATOR =
            new Parcelable.Creator<Movie>() {

                @Override
                public Movie createFromParcel(Parcel source) {
                    return new Movie(source);
                }

                @Override
                public Movie[] newArray(int size) {
                    return new Movie[size];
                }
            };

    public Movie(Parcel in) {
        mId = in.readInt();
        mOriginalTitle = in.readString();
        mOverview = in.readString();
        mReleaseDate = (Date) in.readSerializable();
        mPosterPath = in.readString();
        mVoteAverage = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mOriginalTitle);
        dest.writeString(mOverview);
        dest.writeSerializable(mReleaseDate);
        dest.writeString(mPosterPath);
        dest.writeDouble(mVoteAverage);
    }
}
