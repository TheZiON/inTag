package com.digimarc.disdemo;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.digimarc.dis.DISStatus;
import com.digimarc.dis.DMSDetectorView;
import com.digimarc.dis.interfaces.DISListener;
import com.digimarc.dis.interfaces.DISNotify;
import com.digimarc.disdemo.spinner.DemoSpinnerView;
import com.digimarc.disutils.DISDebugLog;
import com.digimarc.dms.DMSPayload;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class DetectorActivity extends Activity
{	
//    private static final float RETICLE_VERTICAL_SPACING = 0.1f;

    private static final int HANDLE_UI_UPDATE = 100;
    private static final int HANDLE_CYCLE_FADEIN = 101;
    private static final int HANDLE_CYCLE_FADEOUT = 102;
    private static final int HANDLE_FLASH_ICON_OFF = 103;

    private static final long PROMPT_DELAY = 6 * 1000;
    private static final long PROMPT_FADE = 250;

    private static final float ANIMATION_START = 0.6f;
    private static final float ANIMATION_END = 1.0f;
    private static final int ANIMATION_DURATION = 2500;

    private DMSDetectorView mDrop;

    private static Context mContext = null;

    private DemoSpinnerView mSpinner;
    private TextView mCameraText = null;
    boolean mTorchState = false;
    boolean mFirstPass = true;
    private MenuItem mFlash;

    private ImageView mVizCircle;
    private ObjectAnimator mVizAnim;
    private FrameLayout mVizLayout;

    private static DISErrorMsgDialog mErrorMsgDialog = null;

    private LookUIHandler mUIHandler = new LookUIHandler( this );

    private static final List<Integer> mPromptsFlash = Arrays.asList(
            Integer.valueOf( R.string.prompt_distance ),
            Integer.valueOf( R.string.prompt_light ),
            Integer.valueOf( R.string.prompt_focus ) );
    private static final List<Integer> mPromptsNoFlash = Arrays.asList(
            Integer.valueOf(R.string.prompt_distance ),
            Integer.valueOf( R.string.prompt_focus ) );
    //R.string.prompt_distance

    private List<Integer> mTextPrompts = mPromptsFlash;
    private int mCurrentPrompt = 0;

    DISListener mListener = new DISListener()
    {

        @Override
        public void OnMediaIdentified( MediaType type, String payload, String title, String subtitle, String content )
        {
            DISItemData data = new DISItemData();

            DISDebugLog.Write( "OnMediaIdentified" );

            data.mPayloadId = payload;
            data.mTitle = title;
            data.mSubtitle = subtitle;
            data.mPayOff = content;

            if ( type != MediaType.AUDIO )
                launchContent( data );
        }

        @Override
        public boolean OnDigimarcDetected(DMSPayload payload) 
        {
        	if (payload.isBarCode()) {
        		DISDebugLog.Write("Detected Barcode");
        	} else if (payload.isImage()) {
        		DISDebugLog.Write("Detected Digimarc");
        	} else if (payload.isQRCode()) {
        		DISDebugLog.Write("Detected QR Code");
        	} else if (payload.isAudio()) {
        		DISDebugLog.Write("Detected Digimarc Audio");
        	}
        	
        	return true; // always resolve
        }
        
        @Override
        public void onError( int errorCode )
        {
            if ( errorCode == DISStatus.DISResolveFailure )
                ShowError( getResources().getString( R.string.error_title_network ), getResources().getString( R.string.error_text_network ) );
            else
                ShowError( "DISListener Error", DISStatus.getStatusDescription( errorCode ) );
        }

        @Override
        public void onWarning( int warningCode )
        {
            ShowWarning( "DISListener Warning", DISStatus.getStatusDescription( warningCode ) );
        }
    };

    DISNotify mCameraListener = new DISNotify()
    {
        @Override
        public void onCameraAvailable()
        {
            if ( mDrop.isTorchAvailable() )
                mTextPrompts = mPromptsFlash;
            else
            {
                mTextPrompts = mPromptsNoFlash;

                // The app menu may not have been loaded yet, so rather than try setting the icon here
                // we're going to send a message to the handler and let it be dealt with on the main
                // thread. If the menu still isn't available when the handler gets the message it will
                // repost the message to itself and loop until the menu is available & can be dealt with.
                Message msg = mUIHandler.obtainMessage( HANDLE_FLASH_ICON_OFF );
                msg.sendToTarget();
            }
        }
    };

    static class LookUIHandler extends Handler
    {
        private final WeakReference<DetectorActivity> mState;

        LookUIHandler( DetectorActivity state )
        {
            super(Looper.getMainLooper());
            mState = new WeakReference<DetectorActivity>(state);
        }

        @Override
        public void handleMessage( Message msg )
        {
            if (msg != null)
            {
                DetectorActivity state = mState.get();
                if (state != null)
                {
                    switch (msg.what)
                    {
                        case HANDLE_UI_UPDATE:
                            break;

                        case HANDLE_CYCLE_FADEOUT:
                            ObjectAnimator out = ObjectAnimator.ofFloat( state.mCameraText, "alpha", 1.0f, 0.0f );
                            AnimatorSet outSet = new AnimatorSet();

                            outSet.play( out );
                            outSet.addListener(new AnimatorListener()
                            {
                                @Override
                                public void onAnimationStart( Animator animation )
                                {
                                }

                                @Override
                                public void onAnimationRepeat( Animator animation )
                                {
                                }

                                @Override
                                public void onAnimationEnd( Animator animation )
                                {
                                    Message msg = obtainMessage( HANDLE_CYCLE_FADEIN );
                                    msg.sendToTarget();
                                }

                                @Override
                                public void onAnimationCancel( Animator animation )
                                {
                                }
                            });
                            outSet.setDuration(PROMPT_FADE);
                            outSet.setInterpolator(new LinearInterpolator());
                            outSet.start();
                            break;

                        case HANDLE_CYCLE_FADEIN:
                            state.mCurrentPrompt++;
                            if ( state.mCurrentPrompt >= state.mTextPrompts.size() )
                                state.mCurrentPrompt = 0;

                            state.mCameraText.setText( state.mTextPrompts.get( state.mCurrentPrompt ) );

                            ObjectAnimator in = ObjectAnimator.ofFloat( state.mCameraText, "alpha", 0.0f, 1.0f );
                            AnimatorSet inSet = new AnimatorSet();

                            inSet.play( in );
                            inSet.addListener(new AnimatorListener()
                            {
                                @Override
                                public void onAnimationStart( Animator animation )
                                {
                                }

                                @Override
                                public void onAnimationRepeat( Animator animation )
                                {
                                }

                                @Override
                                public void onAnimationEnd( Animator animation )
                                {
                                    Message msg = obtainMessage( HANDLE_CYCLE_FADEOUT );
                                    sendMessageDelayed( msg, PROMPT_DELAY );
                                }

                                @Override
                                public void onAnimationCancel( Animator animation )
                                {
                                }
                            });
                            inSet.setDuration(PROMPT_FADE);
                            inSet.setInterpolator(new LinearInterpolator());
                            inSet.start();
                            break;

                        case HANDLE_FLASH_ICON_OFF:
                            if ( state.mFlash != null )
                                state.mFlash.setVisible( false );
                            else
                            {
                                Message tryagain = obtainMessage( HANDLE_FLASH_ICON_OFF );
                                tryagain.sendToTarget();
                            }
                            break;

                        default:
                            super.dispatchMessage(msg);
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.dis_activity_main );

        mContext = this;

        mSpinner = new DemoSpinnerView( this );
        
        mCameraText = (TextView) findViewById( R.id.listenview_camera_text );

  //      mVisualizer = (DISVisualizationView) findViewById( R.id.disVisualizationView );
        mVizCircle = (ImageView) findViewById( R.id.viz_circle );
        mVizLayout = (FrameLayout) findViewById( R.id.vizualizerLayout );

        mVizAnim = ObjectAnimator.ofPropertyValuesHolder( mVizCircle,
                PropertyValuesHolder.ofFloat( "scaleX", ANIMATION_START, ANIMATION_END ),
                PropertyValuesHolder.ofFloat( "scaleY", ANIMATION_START, ANIMATION_END ) );

        mVizAnim.setRepeatCount( ValueAnimator.INFINITE );
        mVizAnim.setRepeatMode( ValueAnimator.RESTART );
        mVizAnim.setDuration( ANIMATION_DURATION );

        mDrop = (DMSDetectorView) findViewById( R.id.component );
        
        mDrop.setImageOnly(false); // Set to true if you don't want to detect audio 

        mDrop.initialize( this, DISCredentials.getUser(), DISCredentials.getKey(),
                getResources().getString( R.string.DMSReadersConfig ), mListener);
    //    mDrop.setVisualizerListener( mVisualizer );
        mDrop.setSpinner( mSpinner, null );
        mDrop.setNotifyListener( mCameraListener );

        mDrop.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
//                Intent inHome = new Intent(Intent.ACTION_VIEW);
//                inHome.setData(Uri.parse("market://details?id=com.vurb.vurbapp"));
//                startActivity(inHome);


                Intent inCardViewo=new Intent(DetectorActivity.this, com.digimarc.disdemo.inCard.class);
                startActivity(inCardViewo);

                return true;
            }
        });
        mDrop.setTapFocusState( false ); //+
            }


        // By default tapping on the camera view will cause an immediate focus operation
        // to be performed.  Change this to false to disable.



    @Override
    protected void onPause()
    {
        super.onPause();

        // If the torch is currently on we need to turn it off before leaving the app. At the same time we'll
        // update the icon display so that if the user comes back into this app session the on-screen icon will
        // match the actual torch state.
        if ( mTorchState )
            //setTorchState( false );

        mDrop.stopDetection();

        mUIHandler.removeMessages( HANDLE_CYCLE_FADEOUT );
        mUIHandler.removeMessages( HANDLE_CYCLE_FADEIN );
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mVizAnim.start();

        mDrop.startDetection();

        mCameraText.setAlpha( 1.0f );
        if ( mCurrentPrompt >= mTextPrompts.size() )
            mCurrentPrompt = 0;
        mCameraText.setText( mTextPrompts.get( mCurrentPrompt ) );
        Message msg = mUIHandler.obtainMessage( HANDLE_CYCLE_FADEOUT );
        mUIHandler.sendMessageDelayed( msg, PROMPT_DELAY );
    }

    @Override
    protected void onDestroy()
    {
       mDrop.uninitialize();

        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        mDrop.uninitialize();
        super.onBackPressed();
    }

    //@Override
    //public boolean onCreateOptionsMenu( Menu menu )
    //{
        // Inflate the menu; this adds items to the action bar if it is present.
       //+ getMenuInflater().inflate( R.menu.dis_main, menu );

        //+mFlash = menu.findItem( R.id.toggle_flash );

        //return true;
   // }

    //@Override
   // public boolean onOptionsItemSelected( MenuItem item )
   // {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       // int id = item.getItemId();
       // if ( id == R.id.toggle_flash )
       // {
         //   setTorchState( !mTorchState );
        //}

      //  return true; //super.onOptionsItemSelected( item );
   // }

    private void launchContent( DISItemData data )
    {
        mSpinner.Stop();

        // handle payoff
        try {
            Intent i = new Intent(
                Intent.ACTION_VIEW);
            i.setData(Uri.parse(data.mPayOff));
            mContext.startActivity(i);
        }
        catch (Exception e) {
            ShowError("Payoff Error", "Unable to launch payoff content.");
        }
    }

//    private void setTorchState( boolean enable )
//    {
//        mTorchState = enable;
//
//        mDrop.setTorch( mTorchState );
//        mFlash.setIcon( mTorchState ? R.drawable.dis_flash_off : R.drawable.dis_flash_on );
//        mFlash.setTitle( mTorchState ? R.string.flash_off : R.string.flash_on );
//    }
     
    public static void ShowError( String caption, String msg) {
        ShowMsg( caption, msg, true, false);
    }

    public static void ShowWarning(String caption, String msg) {
        ShowMsg( caption, msg, true, false);
    }

    public static void ShowInfo( String caption, String msg) {
        ShowMsg( caption, msg, false, false);
    }

    public static void ShowMsg( String title, String msg, boolean display, boolean cancelable) {
        try {
            if ((title != null) && (title.isEmpty() == false) && (msg != null) && (msg.isEmpty() == false)) {
                DISDebugLog.Write( title, msg);
                if (display == true) {
                    if((mErrorMsgDialog == null) || (mErrorMsgDialog.Dismissed() == true)) {
                        mErrorMsgDialog = new DISErrorMsgDialog(mContext, title, msg, cancelable);
                    }
                }
            }
        }
        catch (Exception e) {
            DISDebugLog.Write("DMSDemo.ShowMsg", e);
        }
    }
}
