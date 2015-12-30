package com.kandy.phonegap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.KandyCameraInfo;

/**
 * Native Kandy incall dialog.
 *
 * @author kodeplusdev
 * @version 1.3.3
 */
public class KandyIncallDialog extends Dialog {

    private ImageView uiUnknownAvatar;
    private View videoCallLayout;
    private KandyView localView, remoteView;

    private ToggleButton holdTbutton, muteTbutton, videoTbutton, cameraTbutton, speakerTbutton;
    private ImageButton hangupButton;

    private IKandyCall call;
    private Activity activity;

    public KandyIncallDialog(Activity activity, IKandyCall call) {
        super(activity, android.R.style.Theme_Light_NoTitleBar);

        this.activity = activity;
        this.call = call;

        setContentView(KandyUtils.getLayout("kandy_incall_dialog"));
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        uiUnknownAvatar = (ImageView) findViewById(KandyUtils.getId("kandy_calls_unknown_avatar"));
        videoCallLayout = findViewById(KandyUtils.getId("kandy_calls_video_layout"));

        ((TextView) findViewById(KandyUtils.getId("kandy_calls_title"))).setText(call.getCallee().getUri());

        // TODO: support multi-call
        localView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_local_video_view"));
        localView.setZOrderOnTop(true);
        remoteView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_video_view"));
        remoteView.setZOrderMediaOverlay(true);

        holdTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_hold_tbutton"));
        holdTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ToggleButton tb = (ToggleButton) v;
                boolean isChecked = tb.isChecked();
                tb.setChecked(!isChecked);
                tb.setEnabled(false);
                switchHold(isChecked);
            }
        });

        muteTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_mute_tbutton"));
        muteTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ToggleButton tb = (ToggleButton) v;
                boolean isChecked = tb.isChecked();
                tb.setChecked(!isChecked);
                tb.setEnabled(false);
                switchMute(isChecked);
            }
        });

        videoTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_video_tbutton"));
        videoTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ToggleButton tb = (ToggleButton) v;
                boolean isChecked = tb.isChecked();
                tb.setChecked(!isChecked);
                tb.setEnabled(false);
                switchVideo(isChecked);
            }
        });

        speakerTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_switch_speaker_tbutton"));
        speakerTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton) v).isChecked();
                switchSpeaker(isChecked);
            }
        });

        cameraTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_switch_camera_tbutton"));
        cameraTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton) v).isChecked();
                switchCamera(isChecked);
            }
        });

        hangupButton = (ImageButton) findViewById(KandyUtils.getId("kandy_calls_hangup_button"));
        hangupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hangup();
            }
        });
    }

    @Override
    public void show() {
        call.setLocalVideoView(localView);
        call.setRemoteVideoView(remoteView);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switchVideoView(call.isSendingVideo(), call.isReceivingVideo());
            }
        });
        super.show();
    }

    public void switchVideoView(final boolean isSendingVideo, final boolean isReceivingVideo) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                videoTbutton.setChecked(isSendingVideo);

                if (isReceivingVideo) {
                    videoCallLayout.setVisibility(View.VISIBLE);
                    uiUnknownAvatar.setVisibility(View.GONE);
                    call.setRemoteVideoView(remoteView);
                } else {
                    videoCallLayout.setVisibility(View.GONE);
                    uiUnknownAvatar.setVisibility(View.VISIBLE);
                }

                if (isSendingVideo) {
                    localView.setVisibility(View.VISIBLE);
                } else {
                    localView.setVisibility(View.GONE);
                }
            }
        });
    }

    public void switchHold(final boolean isChecked) {
        if (call == null)
            return;
        if (isChecked)
            call.hold(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    holdTbutton.setChecked(isChecked);
                    holdTbutton.setEnabled(true);
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    holdTbutton.setEnabled(true);
                }
            });
        else
            call.unhold(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    holdTbutton.setChecked(isChecked);
                    holdTbutton.setEnabled(true);
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    holdTbutton.setEnabled(true);
                }
            });
    }

    public void switchMute(final boolean isChecked) {
        if (call == null)
            return;
        Log.i("KandyInCallDialog", "switchMute() was invoked: " + String.valueOf(isChecked));
        if (isChecked)
            call.mute(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    muteTbutton.setChecked(isChecked);
                    muteTbutton.setEnabled(true);
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    muteTbutton.setEnabled(true);
                }
            });
        else
            call.unmute(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    muteTbutton.setChecked(isChecked);
                    muteTbutton.setEnabled(true);
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    muteTbutton.setEnabled(true);
                }
            });
    }

    public void switchVideo(final boolean isChecked) {
        if (call == null)
            return;
        if (isChecked)
            call.startVideoSharing(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    videoTbutton.setChecked(isChecked);
                    videoTbutton.setEnabled(true);
                    switchVideoView(call.isSendingVideo(), call.isReceivingVideo());
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    videoTbutton.setEnabled(true);
                }
            });
        else
            call.stopVideoSharing(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    videoTbutton.setChecked(isChecked);
                    videoTbutton.setEnabled(true);
                    switchVideoView(call.isSendingVideo(), call.isReceivingVideo());
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    videoTbutton.setEnabled(true);
                }
            });
    }

    public void switchCamera(boolean isChecked) {
        if (call == null)
            return;
        KandyCameraInfo cameraInfo = isChecked ? KandyCameraInfo.FACING_FRONT : KandyCameraInfo.FACING_BACK;
        call.switchCamera(cameraInfo);
    }

    public void switchSpeaker(boolean isChecked) {
        if (call == null)
            return;
        AudioManager mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setSpeakerphoneOn(isChecked);
    }

    public void hangup() {
        call.hangup(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall callee) {
            }

            @Override
            public void onRequestFailed(IKandyCall callee, final int code, final String error) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, String.format(KandyUtils.getString("kandy_error_message"), code, error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dismiss();
    }
}