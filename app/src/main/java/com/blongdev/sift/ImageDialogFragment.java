package com.blongdev.sift;

import android.app.ActionBar;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.blongdev.sift.R;
import com.squareup.picasso.Picasso;

/**
 * Created by Brian on 3/11/2016.
 */
public class ImageDialogFragment extends DialogFragment {

    Matrix mMatrix;
    ImageView mImageView;

    float mInitialScaleFactor = 1;

    ScaleGestureDetector mScaleGestureDetector;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.image_dialog_fragment, container, false);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);



        mImageView = (ImageView) rootView.findViewById(R.id.dialog_fragment_image);
        Bundle args = getArguments();
        String imageUrl = args.getString(getString(R.string.image_url));
        Picasso.with(getContext()).load(imageUrl).into(mImageView);

        mMatrix = mImageView.getMatrix();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        return rootView;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //previously scaled, needs adjustment. not working well yet
            float scaleFactor = detector.getScaleFactor() * mInitialScaleFactor;

            mMatrix.setScale(scaleFactor,scaleFactor);
            mImageView.setImageMatrix(mMatrix);
            return true;
        }
    }

    @Override
    public void onResume()
    {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int imageWidth = mImageView.getDrawable().getIntrinsicWidth();
        int imageHeight = mImageView.getDrawable().getIntrinsicHeight();

        if (imageHeight > imageWidth && imageHeight > 0) {
            mInitialScaleFactor = Float.valueOf(screenHeight)/Float.valueOf(imageHeight);
            if (imageWidth * mInitialScaleFactor > screenWidth) {

            }
        } else if(imageWidth > 0){
            mInitialScaleFactor =  Float.valueOf(screenWidth)/Float.valueOf(imageWidth);
        }

        mMatrix.setScale(mInitialScaleFactor, mInitialScaleFactor);
        mImageView.setImageMatrix(mMatrix);

        int newWidth = (int)(imageWidth * mInitialScaleFactor);
        int newHeight = (int)(imageHeight * mInitialScaleFactor);

        getDialog().getWindow().setLayout(newWidth, newHeight);

        super.onResume();
    }
}
