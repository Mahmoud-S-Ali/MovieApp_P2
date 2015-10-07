package com.example.android.movieapp.movieData;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by Toty on 8/17/2015.
 */
public class MyMovie implements Parcelable {
    private HashMap<String, String> mMovieDetails;

    public MyMovie(HashMap<String, String> data) {
        mMovieDetails = data;
    }

    public HashMap<String, String> getMovieDetails() {
        return mMovieDetails;
    }

    protected MyMovie(Parcel in) {
        mMovieDetails = (HashMap) in.readValue(HashMap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mMovieDetails);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyMovie> CREATOR = new Parcelable.Creator<MyMovie>() {
        @Override
        public MyMovie createFromParcel(Parcel in) {
            return new MyMovie(in);
        }

        @Override
        public MyMovie[] newArray(int size) {
            return new MyMovie[size];
        }
    };
}