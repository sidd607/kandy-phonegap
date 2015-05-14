package com.kandy.phonegap;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import org.apache.cordova.CallbackContext;

/**
 * The native (video) call {@link android.app.AlertDialog}.
 *
 * @author kodeplusdev
 * @version 1.0.0
 */
public class KandyCallDialog extends Dialog {
    private static final String LCAT = KandyCallDialog.class.getSimpleName();

    public enum KandyCallDialogType {VOIP_CALL, VOICE_CALL, PSTN_CALL}

    private ImageView uiHangupButton;
    private ToggleButton uiHoldTButton;
    private ToggleButton uiMuteTButton;
    private ToggleButton uiVideoTButton;
    private ToggleButton uiSwitchCameraTButton;

    private TextView uiTitle;
    private ImageView uiImageView;

    private KandyView uiRemoteVideoView;
    private KandyView uiLocalVideoView;

    private boolean mHoldState = false;
    private boolean mMuteState = false;
    private boolean mVideoSharingState = true;

    private IKandyCall _currentCall;

    private KandyCallDialogListener _kandyCallDialogListener;

    private CallbackContext _callbackContext;

    private KandyUtils utils = KandyUtils.getInstance(null);

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

    private CompoundButton.OnCheckedChangeListener onSwitchCameraTButtonClicked = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switchCamera(_currentCall, isChecked);
        }
    };

    /**
     * {@inheritDoc}
     *
     * @param context The {@link android.content.Context} to use.
     */
    public KandyCallDialog(Context context) {
        super(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(utils.getLayout("kandy_call_dialog"));
        initializeViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
    }

    /**
     * Initialize the dialog.
     */
    private void initializeViews() {

        uiHangupButton = (ImageView) findViewById(utils.getId("kandy_calls_hangup_button"));
        uiHangupButton.setOnClickListener(onHangupButtonClicked);

        uiHoldTButton = (ToggleButton) findViewById(utils.getId("kandy_calls_hold_tbutton"));
        uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);
        uiHoldTButton.setChecked(mHoldState);

        uiMuteTButton = (ToggleButton) findViewById(utils.getId("kandy_calls_mute_tbutton"));
        uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);
        uiMuteTButton.setChecked(mMuteState);

        uiVideoTButton = (ToggleButton) findViewById(utils.getId("kandy_calls_video_tbutton"));
        uiVideoTButton.setChecked(mVideoSharingState);
        uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);

        uiSwitchCameraTButton = (ToggleButton) findViewById(utils.getId("kandy_calls_switch_camera_tbutton"));
        uiSwitchCameraTButton.setChecked(mVideoSharingState);
        uiSwitchCameraTButton.setOnCheckedChangeListener(onSwitchCameraTButtonClicked);

        uiTitle = (TextView) findViewById(utils.getId("kandy_calls_title"));
        uiImageView = (ImageView) findViewById(utils.getId("kandy_calls_user_image"));

        uiRemoteVideoView = (KandyView) findViewById(utils.getId("kandy_calls_video_view"));
        uiLocalVideoView = (KandyView) findViewById(utils.getId("kandy_calls_local_video_view"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(CharSequence title) {
        if (uiTitle != null)
            uiTitle.setText(title);
    }

    /**
     * Set avatar image for voice call.
     *
     * @param uri The uri of the avatar image.
     */
    public void setCallImage(Uri uri) {
        if (uiImageView != null)
            uiImageView.setImageURI(uri);
    }

    /**
     * Set call dialog type.
     *
     * @param type The type of the call dialog.
     */
    public void setCallDialogType(KandyCallDialogType type) {
        if (type != KandyCallDialogType.VOIP_CALL) {
            uiImageView.setVisibility(View.VISIBLE);
            uiLocalVideoView.setVisibility(View.GONE);
            uiRemoteVideoView.setVisibility(View.GONE);
        }
    }

    /**
     * Hangup the current call.
     *
     * @param call The current call.
     */
    public void doHangup(IKandyCall call) {
        if (call == null) {
            _callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        if (_kandyCallDialogListener != null)
            _kandyCallDialogListener.hangup();

        this.dismiss();
    }

    /**
     * Switch mute state of the current call.
     *
     * @param call  The current call.
     * @param state The mute state of current call.
     */
    public void switchMuteState(IKandyCall call, boolean state) {
        if (call == null) {
            uiMuteTButton.setChecked(false);
            _callbackContext.error(utils.getString("kandy_calls_invalid_mute_call_text_msg"));
            return;
        }

        mMuteState = state;

        if (_kandyCallDialogListener != null)
            _kandyCallDialogListener.switchMuteState(state);
    }

    /**
     * Switch hold state of the current call.
     *
     * @param call  The current call.
     * @param state The hold state of current call.
     */
    public void switchHoldState(IKandyCall call, boolean state) {
        if (call == null) {
            uiHoldTButton.setChecked(false);
            _callbackContext.error(utils.getString("kandy_calls_invalid_hold_text_msg"));
            return;
        }


        mHoldState = state;

        if (_kandyCallDialogListener != null)
            _kandyCallDialogListener.switchHoldState(state);
    }

    /**
     * Switch video sharing state of the current call.
     *
     * @param call  The current call.
     * @param state The video sharing state of current video.
     */
    public void switchVideoSharing(IKandyCall call, boolean state) {
        if (call == null) {
            uiVideoTButton.setChecked(false);
            _callbackContext.error(utils.getString("kandy_calls_invalid_video_call_text_msg"));
            return;
        }

        mVideoSharingState = state;

        if (_kandyCallDialogListener != null)
            _kandyCallDialogListener.switchVideoSharingState(state);
    }

    /**
     * Switch camera sharing mode.
     *
     * @param currentCall The current call.
     * @param isChecked   The camera mode.
     */
    public void switchCamera(IKandyCall currentCall, boolean isChecked) {
        currentCall.getCameraForVideo();
        KandyCameraInfo cameraInfo = isChecked ? KandyCameraInfo.FACING_FRONT : KandyCameraInfo.FACING_BACK;
        currentCall.switchCamera(cameraInfo);
    }

    /**
     * Set the current call and the video views for this dialog
     *
     * @param call The current call.
     * @param startWithVideo Start call with video enabled.
     */
    public void setKandyCall(IKandyCall call, boolean startWithVideo) {
        _currentCall = call;
        _currentCall.setLocalVideoView(uiLocalVideoView);
        _currentCall.setRemoteVideoView(uiRemoteVideoView);

        uiVideoTButton.setChecked(startWithVideo);
    }

    /**
     * Register the call listener from Kandy plugin.
     *
     * @param listener The callback listener.
     */
    public void setKandyVideoCallListener(KandyCallDialogListener listener) {
        _kandyCallDialogListener = listener;
    }

    /**
     * Register the callback from Kandy plugin.
     *
     * @param callbackContext The current {@link CallbackContext} to use.
     */
    public void setKandyCallbackContext(CallbackContext callbackContext) {
        _callbackContext = callbackContext;
    }

    /**
     * The listener interface of the video call dialog
     */
    public interface KandyCallDialogListener {

        /**
         * Hangup the current call.
         */
        void hangup();

        /**
         * Switch mute state of the current call.
         *
         * @param state The mute state.
         */
        void switchMuteState(boolean state);

        /**
         * Switch hold state of current call.
         *
         * @param state The hold state.
         */
        void switchHoldState(boolean state);

        /**
         * Switch video sharing state of current call.
         *
         * @param state The video sharing state.
         */
        void switchVideoSharingState(boolean state);
    }
}
