package com.example.android.movieapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Toty on 8/22/2015.
 */

/*Source: http://stackoverflow.com/questions/15261088
          /gridview-with-two-columns-and-auto-resized-images*/
public class Thumbnail extends ImageView{
    public Thumbnail(Context context) {
        super(context);
    }

    public Thumbnail(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Thumbnail(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() * 3 / 2); //Snap to width
    }
}
