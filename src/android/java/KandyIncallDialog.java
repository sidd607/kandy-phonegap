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
 * @version 1.3.4
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