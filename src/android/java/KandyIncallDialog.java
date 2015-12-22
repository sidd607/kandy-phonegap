package com.kandy.phonegap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
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
    private KandyIncallDialogListener listener;

    public KandyIncallDialog(Activity activity, IKandyCall call) {
        super(activity, android.R.style.Theme_Light_NoTitleBar);

        this.activity = activity;
        this.call = call;

        setContentView(KandyUtils.getLayout("kandy_incall_dialog"));
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        uiUnknownAvatar = (ImageView) findViewById(KandyUtils.getId("kandy_calls_unknown_avatar"));
        videoCallLayout = findViewById(KandyUtils.getId("kandy_calls_video_layout"));

        ((TextView)findViewById(KandyUtils.getId("kandy_calls_title"))).setText(call.getCallee().getUri());

        // TODO: support multi-call
        localView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_local_video_view"));
        localView.setZOrderOnTop(true);
        remoteView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_video_view"));
        remoteView.setZOrderMediaOverlay(true);

        holdTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_hold_tbutton"));
        holdTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton) v).isChecked();
                switchHold(isChecked);
            }
        });

        muteTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_mute_tbutton"));
        muteTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton) v).isChecked();
                switchMute(isChecked);
            }
        });

        videoTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_video_tbutton"));
        videoTbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton) v).isChecked();
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

    public void setKandyIncallDialogListener(KandyIncallDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void show() {
        call.setLocalVideoView(localView);
        call.setRemoteVideoView(remoteView);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switchVideoView(call.isCallStartedWithVideo());
            }
        });
        super.show();
    }

    public void switchVideoView(final boolean isVideoView) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (isVideoView) {
                    videoTbutton.setChecked(true);
                    videoCallLayout.setVisibility(View.VISIBLE);
                    localView.setVisibility(View.VISIBLE);
                    call.setRemoteVideoView(remoteView);
                    uiUnknownAvatar.setVisibility(View.GONE);
                } else {
                    videoTbutton.setChecked(false);
                    localView.setVisibility(View.GONE);
                    videoCallLayout.setVisibility(View.GONE);
                    uiUnknownAvatar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void switchHold(boolean isChecked) {
        if (call == null)
            return;
        if (isChecked)
            call.hold(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    // UIUtils.showToastWithMessage(activity, error);
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            holdTbutton.setChecked(false);
                        }
                    });
                }
            });
        else
            call.unhold(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    // UIUtils.showToastWithMessage(activity, error);
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            holdTbutton.setChecked(true);
                        }
                    });
                }
            });
    }

    public void switchMute(boolean isChecked) {
        if (call == null)
            return;
        Log.i("KandyInCallDialog", "switchMute() was invoked: " + String.valueOf(isChecked));
        if (isChecked)
            call.mute(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    // UIUtils.showToastWithMessage(activity, error);
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            muteTbutton.setChecked(false);
                        }
                    });
                }
            });
        else
            call.unmute(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    // UIUtils.showToastWithMessage(activity, error);
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            muteTbutton.setChecked(true);
                        }
                    });
                }
            });
    }

    public void switchVideo(boolean isChecked) {
        if (call == null)
            return;
        if (isChecked)
            call.startVideoSharing(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    switchVideoView(true);
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    // UIUtils.showToastWithMessage(activity, error);
                    switchVideoView(false);
                }
            });
        else
            call.stopVideoSharing(new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    switchVideoView(false);
                }

                @Override
                public void onRequestFailed(IKandyCall call, int code, String error) {
                    // UIUtils.showToastWithMessage(activity, error);
                    switchVideoView(true);
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
        if (listener != null)
            listener.onHangup();
        this.dismiss();
//        call.hangup(new KandyCallResponseListener() {
//
//            @Override
//            public void onRequestSucceeded(IKandyCall callee) {
//                Log.i("KandyIncallDialog", "onRequestSucceeded()");
//                dismiss();
//            }
//
//            @Override
//            public void onRequestFailed(IKandyCall callee, int code, String error) {
//                Log.i("KandyIncallDialog", "onRequestFailed()");
//            }
//        });
    }

    public interface KandyIncallDialogListener {
        void onHangup();
    }
}