package com.digimarc.disdemo.spinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.digimarc.dis.views.DISSpinnerView;
import com.digimarc.disdemo.R;

@SuppressLint("DrawAllocation")
public class DemoSpinnerView extends DISSpinnerView
{

    public DemoSpinnerView( Context context )
    {
        super( context );

        setSingleImage( R.drawable.spinner, 20, 18 );
//        Bitmap logo = BitmapFactory.decodeResource( context.getResources(), R.drawable.progress_spinner_logo );
//        SetOverlayImage( logo );
    }

    public DemoSpinnerView( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        setSingleImage( R.drawable.spinner, 20, 18 );
//        Bitmap logo = BitmapFactory.decodeResource( context.getResources(), R.drawable.progress_spinner_logo );
//        SetOverlayImage( logo );
    }

    public DemoSpinnerView( Context context, AttributeSet attrs, int defStyle )
    {
        super(context, attrs, defStyle);

        setSingleImage( R.drawable.spinner, 20, 18 );
//        Bitmap logo = BitmapFactory.decodeResource( context.getResources(), R.drawable.progress_spinner_logo );
//        SetOverlayImage( logo );
    }

    @Override
    public void onDraw( Canvas canvas )
	{
	    super.onDraw( canvas );
	}
}
