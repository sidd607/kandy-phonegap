package com.kandy.phonegap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyView;

/**
 * Display call video view
 *
 * @author kodeplusdev
 * @version 1.3.3
 */
public class KandyVideoView extends Dialog {

    private KandyView kandyView;
    private RelativeLayout relativeLayout;

    private WindowManager.LayoutParams wmlp;
    private ViewGroup.LayoutParams params;
    DisplayMetrics metrics;

    public KandyVideoView(Context context) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        setContentView(KandyUtils.getLayout("kandy_video_view"));

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.TOP | Gravity.LEFT);

        metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        setCanceledOnTouchOutside(false);

        kandyView = (KandyView) findViewById(KandyUtils.getId("kandy_video_view"));
        relativeLayout = (RelativeLayout) findViewById(KandyUtils.getId("kandy_video_view_container"));

        wmlp = getWindow().getAttributes();

        params = relativeLayout.getLayoutParams();
    }

    public void layout(int left, int top, int width, int height) {
        Log.i("KandyVideoView", "left: " + left + ", top: " + top + ", width: " + width + ", height: " + height);


        float logicalDensity = metrics.density;

        wmlp.x = (int) Math.ceil(left * logicalDensity);
        wmlp.y = (int) Math.ceil(top * logicalDensity);

        params.width = (int) Math.ceil(width * logicalDensity);
        params.height = (int) Math.ceil(height * logicalDensity);

        getWindow().setAttributes(wmlp);
        relativeLayout.setLayoutParams(params);
    }

    public void setLocalVideoView(final IKandyCall call) {
        call.setLocalVideoView(kandyView); // to establish a call
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // re-set video view on dialog showing
                call.setLocalVideoView(kandyView);
            }
        });
    }

    public void setRemoteVideoView(final IKandyCall call) {
        call.setRemoteVideoView(kandyView); // to establish a call
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // re-set video view on dialog showing
                call.setRemoteVideoView(kandyView);
            }
        });
    }
}