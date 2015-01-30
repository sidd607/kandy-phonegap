/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package com.kandy.phonegap;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyView;

import org.apache.cordova.CallbackContext;

/**
 * The native (video) call dialog.
 */
public class KandyVideoCallDialog extends Dialog {
    private static final String TAG = "KandyPlugin/KandyVideoCallDialog";

    // The interactive buttons.
    private Button uiHangupButton;
    private ToggleButton uiHoldTButton;
    private ToggleButton uiMuteTButton;
    private ToggleButton uiVideoTButton;

    // The video views
    private KandyView uiRemoteVideoView;
    private KandyView uiLocalVideoView;

    // Determines the state of the call
    private boolean mIsHold = false;
    private boolean mIsMute = false;
    private boolean mIsVideo = true;

    // The current call
    private IKandyCall _currentCall;

    // The KandyPlugin listener
    private KandyVideoCallDialogListener _kandyVideoCallDialogListener;

    // The callBackContext
    private CallbackContext _callbackContext;

    private View.OnClickListener onHangupButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            doHangup(_currentCall);
        }
    };

    private CompoundButton.OnCheckedChangeListener onHoldTButtonClicked = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switchHold(_currentCall, isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener onMuteTButtonClicked = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switchMute(_currentCall, isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener onVideoTButtonClicked = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switchVideo(_currentCall, isChecked);
        }
    };

    /**
     * {@inheritDoc}
     *
     * @param context The {@link android.content.Context} to use.
     */
    public KandyVideoCallDialog(Context context) {
        super(context);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.kandy_video_call_dialog);
        initViews();

    }

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
    }

    private void initViews() {
        uiHangupButton = (Button) findViewById(R.id.kandy_calls_hangup_button);
        uiHangupButton.setOnClickListener(onHangupButtonClicked);

        uiHoldTButton = (ToggleButton) findViewById(R.id.kandy_calls_hold_tbutton);
        uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);
        uiHoldTButton.setChecked(mIsHold);

        uiMuteTButton = (ToggleButton) findViewById(R.id.kandy_calls_mute_tbutton);
        uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);
        uiMuteTButton.setChecked(mIsMute);

        uiVideoTButton = (ToggleButton) findViewById(R.id.kandy_calls_video_tbutton);
        uiVideoTButton.setChecked(mIsVideo);
        uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);

        uiRemoteVideoView = (KandyView) findViewById(R.id.kandy_calls_video_view);
        uiLocalVideoView = (KandyView) findViewById(R.id.kandy_calls_local_video_view);
    }

    private void doHangup(IKandyCall pCall) {

        if (pCall == null) {
            _callbackContext.error(getContext().getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        _kandyVideoCallDialogListener.hangup();
        this.dismiss();
    }

    private void switchMute(IKandyCall pCall, boolean isMute) {

        if (pCall == null) {
            uiMuteTButton.setChecked(false);
            _callbackContext.error(getContext().getString(R.string.kandy_calls_invalid_mute_call_text_msg));
            return;
        }

        mIsMute = isMute;

        _kandyVideoCallDialogListener.doMute(isMute);
    }

    private void switchHold(IKandyCall pCall, boolean isHold) {

        if (pCall == null) {
            uiHoldTButton.setChecked(false);
            _callbackContext.error(getContext().getString(R.string.kandy_calls_invalid_hold_text_msg));
            return;
        }

        mIsHold = isHold;

        _kandyVideoCallDialogListener.doHold(isHold);
    }

    private void switchVideo(IKandyCall pCall, boolean isVideoOn) {
        if (pCall == null) {
            uiVideoTButton.setChecked(false);
            _callbackContext.error(getContext().getString(R.string.kandy_calls_invalid_video_call_text_msg));
            return;
        }

        _kandyVideoCallDialogListener.doVideo(isVideoOn);
    }

    public void setKandyCall(IKandyCall kandyCall) {
        _currentCall = kandyCall;

        _currentCall.setLocalVideoView(uiLocalVideoView);
        _currentCall.setRemoteVideoView(uiRemoteVideoView);
    }

    public void setKandyVideoCallListener(KandyVideoCallDialogListener listener) {
        _kandyVideoCallDialogListener = listener;
    }

    public void setKandyCallbackContext(CallbackContext callbackContext) {
        _callbackContext = callbackContext;
    }

    public interface KandyVideoCallDialogListener {
        void hangup();

        void doMute(boolean enable);

        void doHold(boolean enable);

        void doVideo(boolean enable);
    }
}
