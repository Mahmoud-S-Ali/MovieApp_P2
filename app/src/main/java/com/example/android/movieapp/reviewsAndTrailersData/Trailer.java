package com.example.android.movieapp.reviewsAndTrailersData;

/**
 * Created by Toty on 9/28/2015.
 */
public class Trailer {
    private String mName;
    private String mKey;
    private String mLink;

    public Trailer(String name, String key, String link) {
        mName = name;
        mKey = key;
        mLink = link;
    }

    public String getName() {
        return mName;
    }
    public String getKey() {
        return mKey;
    }
    public String getLink() {
        return mLink;
    }
}
