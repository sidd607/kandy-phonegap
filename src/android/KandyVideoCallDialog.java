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
 *
 * @author Kodeplusdev
 * @version 0.0.2
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
    private boolean mHoldState = false;
    private boolean mMuteState = false;
    private boolean mVideoSharingState = true;

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
            switchHoldState(_currentCall, isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener onMuteTButtonClicked = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switchMuteState(_currentCall, isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener onVideoTButtonClicked = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switchVideoSharing(_currentCall, isChecked);
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
        setContentView(KandyUtils.getLayout(context, "kandy_video_call_dialog"));
        initializeViews();
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

    /**
     * Initialize the dialog
     */
    private void initializeViews() {
        uiHangupButton = (Button) findViewById(KandyUtils.getId(getContext(), "kandy_calls_hangup_button"));
        uiHangupButton.setOnClickListener(onHangupButtonClicked);

        uiHoldTButton = (ToggleButton) findViewById(KandyUtils.getId(getContext(), "kandy_calls_hold_tbutton"));
        uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);
        uiHoldTButton.setChecked(mHoldState);

        uiMuteTButton = (ToggleButton) findViewById(KandyUtils.getId(getContext(), "kandy_calls_mute_tbutton"));
        uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);
        uiMuteTButton.setChecked(mMuteState);

        uiVideoTButton = (ToggleButton) findViewById(KandyUtils.getId(getContext(), "kandy_calls_video_tbutton"));
        uiVideoTButton.setChecked(mVideoSharingState);
        uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);

        uiRemoteVideoView = (KandyView) findViewById(KandyUtils.getId(getContext(), "kandy_calls_video_view"));
        uiLocalVideoView = (KandyView) findViewById(KandyUtils.getId(getContext(), "kandy_calls_local_video_view"));
    }

    /**
     * Hangup the current call
     *
     * @param pCall
     */
    private void doHangup(IKandyCall pCall) {

        if (pCall == null) {
            _callbackContext.error(KandyUtils.getString(getContext(), "kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        _kandyVideoCallDialogListener.hangup();
        this.dismiss();
    }

    /**
     * Switch mute state of the current call
     *
     * @param pCall
     * @param isMute
     */
    private void switchMuteState(IKandyCall pCall, boolean isMute) {

        if (pCall == null) {
            uiMuteTButton.setChecked(false);
            _callbackContext.error(KandyUtils.getString(getContext(), "kandy_calls_invalid_mute_call_text_msg"));
            return;
        }

        mMuteState = isMute;

        _kandyVideoCallDialogListener.switchMuteState(isMute);
    }

    /**
     * Switch hold state of the current call
     *
     * @param pCall
     * @param isHold
     */
    private void switchHoldState(IKandyCall pCall, boolean isHold) {

        if (pCall == null) {
            uiHoldTButton.setChecked(false);
            _callbackContext.error(KandyUtils.getString(getContext(), "kandy_calls_invalid_hold_text_msg"));
            return;
        }

        mHoldState = isHold;

        _kandyVideoCallDialogListener.switchHoldState(isHold);
    }

    /**
     * Switch video sharing state of the current call
     *
     * @param pCall
     * @param isVideoOn
     */
    private void switchVideoSharing(IKandyCall pCall, boolean isVideoOn) {
        if (pCall == null) {
            uiVideoTButton.setChecked(false);
            _callbackContext.error(KandyUtils.getString(getContext(), "kandy_calls_invalid_video_call_text_msg"));
            return;
        }

        _kandyVideoCallDialogListener.switchVideoSharingState(isVideoOn);
    }

    /**
     * Set the current call and the video views for this dialog
     *
     * @param kandyCall
     */
    public void setKandyCall(IKandyCall kandyCall) {
        _currentCall = kandyCall;

        _currentCall.setLocalVideoView(uiLocalVideoView);
        _currentCall.setRemoteVideoView(uiRemoteVideoView);
    }

    /**
     * Register the call listener from Kandy plugin
     *
     * @param listener
     */
    public void setKandyVideoCallListener(KandyVideoCallDialogListener listener) {
        _kandyVideoCallDialogListener = listener;
    }

    /**
     * Register the callback from Kandy plugin
     *
     * @param callbackContext
     */
    public void setKandyCallbackContext(CallbackContext callbackContext) {
        _callbackContext = callbackContext;
    }

    /**
     * The listener interface of the video call dialog
     */
    public interface KandyVideoCallDialogListener {

        void hangup();

        void switchMuteState(boolean state);

        void switchHoldState(boolean state);

        void switchVideoSharingState(boolean state);
    }
}
