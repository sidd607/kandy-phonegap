/*******************************************************************************
 * Copyright 2015 © GENBAND US LLC, All Rights Reserved
 * <p/>
 * This software embodies materials and concepts which are
 * proprietary to GENBAND and/or its licensors and is made
 * available to you for use solely in association with GENBAND
 * products or services which must be obtained under a separate
 * agreement between you and GENBAND or an authorized GENBAND
 * distributor or reseller.
 * <p/>
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * AND/OR ITS LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * THE WARRANTY AND LIMITATION OF LIABILITY CONTAINED IN THIS
 * AGREEMENT ARE FUNDAMENTAL PARTS OF THE BASIS OF GENBAND’S BARGAIN
 * HEREUNDER, AND YOU ACKNOWLEDGE THAT GENBAND WOULD NOT BE ABLE TO
 * PROVIDE THE PRODUCT TO YOU ABSENT SUCH LIMITATIONS.  IN THOSE
 * STATES AND JURISDICTIONS THAT DO NOT ALLOW CERTAIN LIMITATIONS OF
 * LIABILITY, GENBAND’S LIABILITY SHALL BE LIMITED TO THE GREATEST
 * EXTENT PERMITTED UNDER APPLICABLE LAW.
 * <p/>
 * Restricted Rights legend:
 * Use, duplication, or disclosure by the U.S. Government is
 * subject to restrictions set forth in subdivision (c)(1) of
 * FAR 52.227-19 or in subdivision (c)(1)(ii) of DFAR 252.227-7013.
 *******************************************************************************/
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
 * @version 1.3.4
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