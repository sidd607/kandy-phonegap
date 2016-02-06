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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyProvsionResponseListener;
import com.genband.kandy.api.provisioning.KandyValidationMethoud;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.addressbook.*;
import com.genband.kandy.api.services.billing.IKandyBillingPackage;
import com.genband.kandy.api.services.billing.IKandyCredit;
import com.genband.kandy.api.services.billing.KandyUserCreditResponseListener;
import com.genband.kandy.api.services.calls.*;
import com.genband.kandy.api.services.chats.*;
import com.genband.kandy.api.services.common.*;
import com.genband.kandy.api.services.events.*;
import com.genband.kandy.api.services.groups.*;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceResponseListener;
import com.genband.kandy.api.services.profile.IKandyDeviceProfile;
import com.genband.kandy.api.services.profile.KandyDeviceProfileParams;
import com.genband.kandy.api.services.profile.KandyDeviceProfileResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.google.android.gcm.GCMRegistrar;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * Kandy Plugin interface for Cordova (PhoneGap).
 *
 * @author kodeplusdev
 * @version 1.3.4
 */
public class KandyPlugin extends CordovaPlugin {

    private static final String LCAT = KandyPlugin.class.getSimpleName();

    private CordovaWebView webView;
    private Activity activity;
    private SharedPreferences prefs;

    private HashMap<String, IKandyCall> calls = new HashMap<String, IKandyCall>();
    private HashMap<String, KandyVideoView> localVideoViews = new HashMap<String, KandyVideoView>();
    private HashMap<String, KandyVideoView> remoteVideoViews = new HashMap<String, KandyVideoView>();

    private KandyIncallDialog _incallDialog;
    private AlertDialog _incomingCallDialog;

    /**
     * The {@link CallbackContext} for Kandy listeners
     */
    private CallbackContext kandyConnectServiceNotificationCallback;
    private CallbackContext kandyCallServiceNotificationCallback;
    private CallbackContext kandyAddressBookServiceNotificationCallback;
    private CallbackContext kandyChatServiceNotificationCallback;
    private CallbackContext kandyGroupServiceNotificationCallback;

    private CallbackContext kandyChatServiceNotificationPluginCallback;
    private CallbackContext kandyCallServiceNotificationPluginCallback;

    /**
     * The {@link CallbackContext} for current action
     */
    private CallbackContext callbackContext;

    private String downloadMediaPath;
    private int mediaMaxSize;
    private String autoDownloadMediaConnectionType;
    private String autoDownloadThumbnailSize;
    private Boolean useDownloadCustomPath;

    private boolean startWithVideoEnabled = true;
    private boolean useNativeCallDialog = true;
    private boolean acknowledgeOnMsgReceived = true;

    /**
     * Sound effect
     */
    private MediaPlayer ringin;
    private MediaPlayer ringout;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.webView = webView;
        activity = cordova.getActivity();
        KandyUtils.initialize(activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        ringin = MediaPlayer.create(activity, KandyUtils.getResource("ringin", "raw"));
        ringin.setLooping(true);

        ringout = MediaPlayer.create(activity, KandyUtils.getResource("ringout", "raw"));
        ringout.setLooping(true);

        // Initialize Kandy SDK
        Kandy.initialize(activity, // TODO: user can change Kandy API keys
                prefs.getString(KandyConstant.API_KEY_PREFS_KEY, prefs.getString(KandyConstant.API_KEY_PREFS_KEY, KandyUtils.getString("kandy_api_key"))),
                prefs.getString(KandyConstant.API_SECRET_PREFS_KEY, prefs.getString(KandyConstant.API_SECRET_PREFS_KEY, KandyUtils.getString("kandy_api_secret"))));

        IKandyGlobalSettings settings = Kandy.getGlobalSettings();
        settings.setKandyHostURL(prefs.getString(KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));

        useDownloadCustomPath = prefs.getBoolean(KandyConstant.PREF_KEY_CUSTOM_PATH, false);
        downloadMediaPath = prefs.getString(KandyConstant.PREF_KEY_PATH, null);
        mediaMaxSize = prefs.getInt(KandyConstant.PREF_KEY_MAX_SIZE, -1);
        autoDownloadMediaConnectionType = prefs.getString(KandyConstant.PREF_KEY_POLICY, null);
        autoDownloadThumbnailSize = prefs.getString(KandyConstant.PREF_KEY_THUMB_SIZE, KandyThumbnailSize.MEDIUM.name());

        prepareLocalStorage();
    }

    private void prepareLocalStorage() {
        //File localStorageDirectory = KandyUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE);
        //KandyUtils.clearDirectory(localStorageDirectory);
        //KandyUtils.copyAssets(activity, localStorageDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        Kandy.getAccess().registerNotificationListener(kandyConnectServiceNotificationListener);
        registerNotificationListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        Kandy.getAccess().unregisterNotificationListener(kandyConnectServiceNotificationListener);
        unregisterNotificationListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext ctx) throws JSONException {

        /** Save current {@link callbackContext} to use**/
        callbackContext = ctx;

        if (action.equals("connectServiceNotificationCallback")) {
            kandyConnectServiceNotificationCallback = ctx;

        } else if (action.equals("callServiceNotificationCallback")) {
            kandyCallServiceNotificationCallback = ctx;

        } else if (action.equals("addressBookServiceNotificationCallback")) {
            kandyAddressBookServiceNotificationCallback = ctx;

        } else if (action.equals("chatServiceNotificationCallback")) {
            kandyChatServiceNotificationCallback = ctx;

        } else if (action.equals("groupServiceNotificationCallback")) {
            kandyGroupServiceNotificationCallback = ctx;

            //***** PLUGIN LISTENERS *****//
        } else if (action.equals("chatServiceNotificationPluginCallback")) {
            kandyChatServiceNotificationPluginCallback = ctx;

        } else if (action.equals("callServiceNotificationPluginCallback")) {
            kandyCallServiceNotificationPluginCallback = ctx;

            //***** PLUGIN CONFIGURATIONS *****//
        } else if (action.equals("configurations")) {
            JSONObject config = args.getJSONObject(0);

            useNativeCallDialog = KandyUtils.getBoolValueFromJson(config, "showNativeCallPage", useNativeCallDialog);
            acknowledgeOnMsgReceived = KandyUtils.getBoolValueFromJson(config, "acknowledgeOnMsgReceived", acknowledgeOnMsgReceived);
            startWithVideoEnabled = KandyUtils.getBoolValueFromJson(config, "startWithVideoEnabled", startWithVideoEnabled);

            useDownloadCustomPath = KandyUtils.getBoolValueFromJson(config, "useDownloadCustomPath", useDownloadCustomPath);
            downloadMediaPath = KandyUtils.getStringValueFromJson(config, "downloadMediaPath", downloadMediaPath);
            mediaMaxSize = KandyUtils.getIntValueFromJson(config, "mediaMaxSize", mediaMaxSize);
            autoDownloadMediaConnectionType = KandyUtils.getStringValueFromJson(config, "autoDownloadMediaConnectionType", autoDownloadMediaConnectionType);
            autoDownloadThumbnailSize = KandyUtils.getStringValueFromJson(config, "autoDownloadThumbnailSize", autoDownloadThumbnailSize);

            applyKandySettings();

        } else if (action.equals("makeToast")) {
            final String message = args.getString(0);
            makeToast(message);

            //***** CONFIGURATIONS *****//
        } else if (action.equals("setKey")) {
            String apiKey = args.getString(0);
            String apiSecret = args.getString(1);

            setKey(apiKey, apiSecret);

        } else if (action.equals("setHostUrl")) {
            String url = args.getString(0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(KandyConstant.KANDY_HOST_PREFS_KEY, url).apply();
            IKandyGlobalSettings settings = Kandy.getGlobalSettings();
            settings.setKandyHostURL(prefs.getString(KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));

        } else if (action.equals("getHostUrl")) {
            callbackContext.success(Kandy.getGlobalSettings().getKandyHostURL());

        } else if (action.equals("getReport")) {
            callbackContext.success(Kandy.getGlobalSettings().getReport());

        } else if (action.equals("getSession")) {
            getSession();

            //***** PROVISIONING SERVICE *****//
        } else if (action.equals("request")) {
            String userId = args.getString(0);
            String twoLetterISOCountryCode = args.getString(1);
            String callerPhonePrefix = args.getString(2);
            String validationMethod = args.getString(3);

            if (callerPhonePrefix == "" || callerPhonePrefix == "null")
                callerPhonePrefix = null;

            KandyValidationMethoud kandyValidationMethoud = KandyValidationMethoud.SMS;
            if (validationMethod != null && validationMethod == "CALL")
                kandyValidationMethoud = KandyValidationMethoud.CALL;

            Kandy.getProvisioning().requestCode(kandyValidationMethoud, userId, twoLetterISOCountryCode, callerPhonePrefix, kandyResponseListener);

        } else if (action.equals("validate")) {
            String userId = args.getString(0);
            String otp = args.getString(1);
            String twoLetterISOCountryCode = args.getString(2);

            Kandy.getProvisioning().validateAndProvision(userId, otp, twoLetterISOCountryCode, kandyValidationResponseListener);

        } else if (action.equals("deactivate")) {
            Kandy.getProvisioning().deactivate(kandyResponseListener);

        } else if (action.equals("getUserDetails")) {
            String userId = args.getString(0);
            Kandy.getProvisioning().getUserDetails(userId, kandyProvsionResponseListener);

        } else if (action.equals("login")) {
            String username = args.getString(0);
            String password = args.getString(1);
            login(username, password);

        } else if (action.equals("loginByToken")) {
            String token = args.getString(0);
            loginByToken(token);

        } else if (action.equals("logout")) {
            logout();

        } else if (action.equals("getConnectionState")) {
            String state = Kandy.getAccess().getConnectionState().toString();
            callbackContext.success(state);

            //***** CALL SERVICE *****//
        } else if (action.equals("showLocalVideo")) {
            String id = args.getString(0);
            int left = args.getInt(1);
            int top = args.getInt(2);
            int width = args.getInt(3);
            int height = args.getInt(4);
            showLocalVideo(id, left, top, width, height);

        } else if (action.equals("showRemoteVideo")) {
            String id = args.getString(0);
            int left = args.getInt(1);
            int top = args.getInt(2);
            int width = args.getInt(3);
            int height = args.getInt(4);
            showRemoteVideo(id, left, top, width, height);

        } else if (action.equals("hideLocalVideo")) {
            String id = args.getString(0);
            hideLocalVideo(id);

        } else if (action.equals("hideRemoteVideo")) {
            String id = args.getString(0);
            hideRemoteVideo(id);

        } else if (action.equals("createVoipCall")) {
            String username = args.getString(0);
            boolean videoEnabled = args.getInt(1) == 1;
            createVoipCall(username, videoEnabled);

        } else if (action.equals("createPSTNCall")) {
            String number = args.getString(0);
            createPSTNCall(number);

        } else if (action.equals("createSIPTrunkCall")) {
            String number = args.getString(0);
            createSIPTrunkCall(number);

        } else if (action.equals("hangup")) {
            String id = args.getString(0);
            doHangup(id);

        } else if (action.equals("mute")) {
            String id = args.getString(0);
            switchMuteState(id, true);

        } else if (action.equals("unmute")) {
            String id = args.getString(0);
            switchMuteState(id, false);

        } else if (action.equals("hold")) {
            String id = args.getString(0);
            switchHoldState(id, true);

        } else if (action.equals("unhold")) {
            String id = args.getString(0);
            switchHoldState(id, false);

        } else if (action.equals("enableVideo")) {
            String id = args.getString(0);
            switchVideoCallState(id, true);

        } else if (action.equals("disableVideo")) {
            String id = args.getString(0);
            switchVideoCallState(id, false);

        } else if (action.equals("switchFrontCamera")) {
            String id = args.getString(0);
            switchCamera(id, KandyCameraInfo.FACING_FRONT);

        } else if (action.equals("switchBackCamera")) {
            String id = args.getString(0);
            switchCamera(id, KandyCameraInfo.FACING_BACK);

        } else if (action.equals("switchSpeakerOn")) {
            AudioManager mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setSpeakerphoneOn(true);

        } else if (action.equals("switchSpeakerOff")) {
            AudioManager mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setSpeakerphoneOn(false);

        } else if (action.equals("transferCall")) {
            String id = args.getString(0);
            String destination = args.getString(1);
            transferCall(id, destination);

        } else if (action.equals("accept")) {
            String id = args.getString(0);
            boolean videoEnabled = args.getInt(1) == 1;
            accept(id, videoEnabled);

        } else if (action.equals("reject")) {
            String id = args.getString(0);
            reject(id);

        } else if (action.equals("ignore")) {
            String id = args.getString(0);
            ignore(id);

        } else if (action.equals("isInCall")) {
            int result = Kandy.getServices().getCallService().isInCall() ? 1 : 0;
            callbackContext.success(result);

        } else if (action.equals("isInGSMCall")) {
            int result = Kandy.getServices().getCallService().isInGSMCall() ? 1 : 0;
            callbackContext.success(result);

            //***** CHAT SERVICE *****//
        } else if (action.equals("sendSMS")) {
            String destination = args.getString(0);
            String text = args.getString(1);

            sendSMS(destination, text);

        } else if (action.equals("sendChat")) {
            String destination = args.getString(0);
            String text = args.getString(1);
            String type = args.getString(2);

            sendChat(destination, text, type);

        } else if (action.equals("pickAudio")) {
            pickAudio();

        } else if (action.equals("sendAudio")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            String uri = args.getString(2);
            String type = args.getString(3);

            sendAudio(destination, caption, uri, type);

        } else if (action.equals("pickContact")) {
            pickContact();

        } else if (action.equals("sendContact")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            String uri = args.getString(2);
            String type = args.getString(3);

            sendContact(destination, caption, uri, type);

        } else if (action.equals("pickVideo")) {
            pickVideo();

        } else if (action.equals("sendVideo")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            String uri = args.getString(2);
            String type = args.getString(3);

            sendVideo(destination, caption, uri, type);

        } else if (action.equals("sendCurrentLocation")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            String type = args.getString(2);

            sendCurrentLocation(destination, caption, type);

        } else if (action.equals("sendLocation")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            JSONObject location = args.getJSONObject(2);
            String type = args.getString(3);

            sendLocation(destination, caption, KandyUtils.getLocationFromJson(location), type);

        } else if (action.equals("pickImage")) {
            pickImage();

        } else if (action.equals("sendImage")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            String uri = args.getString(2);
            String type = args.getString(3);

            sendImage(destination, caption, uri, type);

        } else if (action.equals("pickFile")) {
            pickFile();

        } else if (action.equals("sendFile")) {
            String destination = args.getString(0);
            String caption = args.getString(1);
            String uri = args.getString(2);
            String type = args.getString(3);

            sendFile(destination, caption, uri, type);

        } else if (action.equals("sendAttachment")) {
            String recipient = args.getString(0);
            String caption = args.getString(1);
            String type = args.getString(2);

            openChooserDialog(recipient, caption, type);

        } else if (action.equals("openAttachment")) {
            String uri = args.getString(0);
            String mimeType = args.getString(1);

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(uri), mimeType);
            activity.startActivity(i);

        } else if (action.equals("cancelMediaTransfer")) {
            String uuid = args.getString(0);
            cancelMediaTransfer(uuid);

        } else if (action.equals("downloadMedia")) {
            String uuid = args.getString(0);
            downloadMedia(uuid);

        } else if (action.equals("downloadMediaThumbnail")) {
            String uuid = args.getString(0);
            String size = args.getString(1);
            KandyThumbnailSize thumbnailSize;
            if (size == null || size == "null")
                thumbnailSize = KandyThumbnailSize.MEDIUM;
            else
                thumbnailSize = KandyThumbnailSize.valueOf(size);
            downloadMediaThumbnail(uuid, thumbnailSize);

        } else if (action.equals("markAsReceived")) {
            String uuid = args.getString(0);
            markAsReceived(uuid);

        } else if (action.equals("pullEvents")) {
            Kandy.getServices().getChatService().pullEvents(kandyResponseListener);

            //***** EVENTS SERVICE *****//
        } else if (action.equals("pullPendingEvents")) {
            Kandy.getServices().getEventsService().pullPendingEvents(kandyResponseListener);

        } else if (action.equals("pullHistoryEvents")) {
            try {
                KandyRecord recipient = new KandyRecord(args.getString(0));
                int numberOfEventsToPull = args.getInt(1);
                long timestamp = args.getLong(2);
                boolean moveBackword = args.getBoolean(3);

                Kandy.getServices().getEventsService().pullHistoryEvents(recipient, numberOfEventsToPull, timestamp, moveBackword, new KandyPullHistoryEventsListner() {
                    @Override
                    public void onResponseSucceded(boolean endOfEvents) {
                        callbackContext.success(endOfEvents ? 1 : 0);
                    }

                    @Override
                    public void onRequestFailed(int code, String error) {
                        callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
                    }
                });
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
            }

        } else if (action.equals("getAllConversations")) {
            Kandy.getServices().getEventsService().getAllConversations(new KandyAllConversationsListener() {
                @Override
                public void onResponseSucceded(IKandySumOfConversation sumOfConversation, ArrayList<IKandyConversation> conversations) {
                    callbackContext.success(KandyUtils.getJsonObjectFromKandyConversations(sumOfConversation, conversations));
                }

                @Override
                public void onRequestFailed(int code, String error) {
                    callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
                }
            });

        } else if (action.equals("pullAllConversationsWithMessages")) {
            int numberOfEventsToPull = args.getInt(0);
            long timestamp = args.getLong(1);
            boolean moveBackword = args.getBoolean(2);

            Kandy.getServices().getEventsService().pullAllConversationsWithMessages(numberOfEventsToPull, timestamp, moveBackword, new KandyAllConverstionWithMessagesListener() {
                @Override
                public void onResponseSucceded(boolean endOfEvents, IKandySumOfConversation sumOfConversation, ArrayList<IKandyConversation> conversations) {
                    JSONObject result = KandyUtils.getJsonObjectFromKandyConversations(sumOfConversation, conversations);
                    try {
                        result.put("endOfEvents", endOfEvents);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(result);
                }

                @Override
                public void onRequestFailed(int code, String error) {
                    callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
                }
            });

            //***** GROUP SERVICE *****//
        } else if (action.equals("createGroup")) {
            String groupName = args.getString(0);
            createGroup(groupName);

        } else if (action.equals("getMyGroups")) {
            Kandy.getServices().getGroupService().getMyGroups(kandyGroupsResponseListener);

        } else if (action.equals("getGroupById")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().getGroupById(new KandyRecord(groupId), kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("updateGroupName")) {
            String groupId = args.getString(0);
            String newName = args.getString(1);
            try {
                Kandy.getServices().getGroupService().updateGroupName(new KandyRecord(groupId), newName, kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("updateGroupImage")) {
            String groupId = args.getString(0);
            String uri = args.getString(1);
            try {
                Kandy.getServices().getGroupService().updateGroupImage(new KandyRecord(groupId), Uri.parse(uri), kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("removeGroupImage")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().removeGroupImage(new KandyRecord(groupId), kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("downloadGroupImage")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().downloadGroupImage(new KandyRecord(groupId), kandyResponseProgressListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("downloadGroupImageThumbnail")) {
            String groupId = args.getString(0);
            String size = args.getString(1);
            KandyThumbnailSize thumbnailSize;
            if (size == null || size == "null")
                thumbnailSize = KandyThumbnailSize.MEDIUM;
            else
                thumbnailSize = KandyThumbnailSize.valueOf(size);
            try {
                Kandy.getServices().getGroupService().downloadGroupImageThumbnail(new KandyRecord(groupId), thumbnailSize, kandyResponseProgressListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("muteGroup")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().muteGroup(new KandyRecord(groupId), kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("unmuteGroup")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().unmuteGroup(new KandyRecord(groupId), kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("destroyGroup")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().destroyGroup(new KandyRecord(groupId), kandyResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("leaveGroup")) {
            String groupId = args.getString(0);
            try {
                Kandy.getServices().getGroupService().leaveGroup(new KandyRecord(groupId), kandyResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("removeParticipants")) {
            String groupId = args.getString(0);
            JSONArray participants = args.getJSONArray(1);
            List<KandyRecord> records = new ArrayList<KandyRecord>();
            for (int i = 0; i < participants.length(); ++i) {
                try {
                    records.add(new KandyRecord(participants.getString(i)));
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            try {
                Kandy.getServices().getGroupService().removeParticipants(new KandyRecord(groupId), records, kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("muteParticipants")) {
            String groupId = args.getString(0);
            JSONArray participants = args.getJSONArray(1);
            List<KandyRecord> records = new ArrayList<KandyRecord>();
            for (int i = 0; i < participants.length(); ++i) {
                try {
                    records.add(new KandyRecord(participants.getString(i)));
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            try {
                Kandy.getServices().getGroupService().muteParticipants(new KandyRecord(groupId), records, kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("unmuteParticipants")) {
            String groupId = args.getString(0);
            JSONArray participants = args.getJSONArray(1);
            List<KandyRecord> records = new ArrayList<KandyRecord>();
            for (int i = 0; i < participants.length(); ++i) {
                try {
                    records.add(new KandyRecord(participants.getString(i)));
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            try {
                Kandy.getServices().getGroupService().unmuteParticipants(new KandyRecord(groupId), records, kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

        } else if (action.equals("addParticipants")) {
            String groupId = args.getString(0);
            JSONArray participants = args.getJSONArray(1);
            List<KandyRecord> records = new ArrayList<KandyRecord>();
            for (int i = 0; i < participants.length(); ++i) {
                try {
                    records.add(new KandyRecord(participants.getString(i)));
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            try {
                Kandy.getServices().getGroupService().addParticipants(new KandyRecord(groupId), records, kandyGroupResponseListener);
            } catch (KandyIllegalArgumentException e) {
                callbackContext.error(e.getMessage());
                e.printStackTrace();
            }

            //***** PRESENCE SERVICE *****//
        } else if (action.equals("presence")) {
            retrievePresence(args);

        } else if (action.equals("startWatch") || action.equals("stopWatch") || action.equals("updateStatus")) {
            callbackContext.error("This action was removed from android api.");
            //***** LOCATION SERVICE *****//
        } else if (action.equals("getCountryInfo")) {
            Kandy.getServices().getLocationService().getCountryInfo(kandyCountryInfoResponseListener);

        } else if (action.equals("getCurrentLocation")) {
            try {
                Kandy.getServices().getLocationService().getCurrentLocation(kandyCurrentLocationListener);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                callbackContext.error("Invalid method");
            }

            //***** PUSH SERVICE *****//
        } else if (action.equals("enable")) {
            enablePushNotification();

        } else if (action.equals("disable")) {
            disablePushNotification();

            //***** ADDRESS BOOK SERVICE *****//
        } else if (action.equals("getDeviceContacts")) {
            List<KandyDeviceContactsFilter> filters = new ArrayList<KandyDeviceContactsFilter>();
            JSONArray array = null;
            try {
                array = args.getJSONArray(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (array != null) {
                for (int i = 0; i < array.length(); ++i) {
                    String name = array.getString(i);
                    if (name != null && name != "")
                        filters.add(KandyDeviceContactsFilter.valueOf(name));
                }
            }
            if (filters.size() == 0)
                filters.add(KandyDeviceContactsFilter.ALL);
            Kandy.getServices().getAddressBookService().getDeviceContacts(filters.toArray(new KandyDeviceContactsFilter[filters.size()]), kandyContactsListener);

        } else if (action.equals("getDomainContacts")) {
            Kandy.getServices().getAddressBookService().getDomainDirectoryContacts(kandyContactsListener);

        } else if (action.equals("getFilteredDomainDirectoryContacts")) {
            String filterName = args.getString(0);
            String searchString = args.getString(1);
            KandyDomainContactFilter filter;
            if (filterName != null && filterName != "")
                filter = KandyDomainContactFilter.valueOf(filterName);
            else filter = KandyDomainContactFilter.ALL;
            Kandy.getServices().getAddressBookService().getFilteredDomainDirectoryContacts(filter, false, searchString, kandyContactsListener);

        } else if (action.equals("getPersonalAddressBook")) {
            Kandy.getServices().getAddressBookService().getPersonalAddressBook(kandyContactsListener);

        } else if (action.equals("addContactToPersonalAddressBook")) {
            JSONObject contact = args.getJSONObject(0);
            KandyContactParams contactParams = new KandyContactParams();
            contactParams.initFromJson(contact);
            Kandy.getServices().getAddressBookService().addContactToPersonalAddressBook(contactParams, kandyContactListener);

        } else if (action.equals("removePersonalAddressBookContact")) {
            String userId = args.getString(0);
            Kandy.getServices().getAddressBookService().removePersonalAddressBookContact(userId, kandyResponseListener);

            //***** BILLING SERVICE *****//
        } else if (action.equals("getUserCredit")) {
            Kandy.getServices().getBillingService().getUserCredit(kandyUserCreditResponseListener);

            //***** DEVICE PROFILE SERVICE *****//
        } else if (action.equals("getUserDeviceProfiles")) {
            Kandy.getServices().getProfileService().getUserDeviceProfiles(kandyDeviceProfileResponseListener);
        } else if (action.equals("updateDeviceProfile")) {
            String deviceDisplayName = args.getString(0);
            String deviceName = args.getString(1);
            String deviceFamily = args.getString(2);
            KandyDeviceProfileParams profileParams = new KandyDeviceProfileParams(deviceDisplayName);
            profileParams.setDeviceName(deviceName);
            profileParams.setDeviceFamily(deviceFamily);
            Kandy.getServices().getProfileService().updateDeviceProfile(profileParams, kandyResponseListener);

        } else if (action.equals("uploadMedia")) {
            final String uri = args.getString(0);
            try {
                final IKandyFileItem item = KandyMessageBuilder.createFile("", Uri.parse(uri));
                Kandy.getServices().geCloudStorageService().uploadMedia(item, new KandyUploadProgressListener() {

                    @Override
                    public void onProgressUpdate(IKandyTransferProgress progress) {
                        Log.d(LCAT, "KandyUploadProgressListener->onProgressUpdate() was invoked: " + progress.getState().toString() + " " + progress.getProgress());

                        JSONObject result = new JSONObject();

                        try {
                            result.put("process", progress.getProgress());
                            result.put("state", progress.getState().toString());
                            result.put("byteTransfer", progress.getByteTransfer());
                            result.put("byteExpected", progress.getByteExpected());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        KandyUtils.sendPluginResultAndKeepCallback(callbackContext, result);
                    }

                    @Override
                    public void onRequestSucceded() {
                        Log.d(LCAT, "KandyUploadProgressListener->onRequestSucceded() was invoked.");
                        File f = new File(uri);
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("name", f.getName());
                            obj.put("uuid", item.getServerUUID().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callbackContext.success(obj);
                    }

                    @Override
                    public void onRequestFailed(int code, String error) {
                        Log.d(LCAT, "KandyUploadProgressListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
                        callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
                    }
                });
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }

        } else if (action.equals("downloadMediaFromCloudStorage")) {
            String uuid = args.getString(0);
            String fileName = args.getString(1);
            final long timeStamp = Calendar.getInstance().getTimeInMillis();
            Uri fileUri = Uri.parse(KandyUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE).getAbsolutePath()
                    + "//" + timeStamp + "_" + fileName);
            try {
                IKandyFileItem item = KandyMessageBuilder.createFile("", fileUri);
                item.setServerUUID(UUID.fromString(uuid));
                Kandy.getServices().geCloudStorageService().downloadMedia(item, kandyResponseProgressListener);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }

        } else if (action.equals("downloadMediaThumbnailFromCloudStorage")) {
            String uuid = args.getString(0);
            String fileName = args.getString(1);
            KandyThumbnailSize thumbnailSize = KandyThumbnailSize.MEDIUM;
            try {
                thumbnailSize = KandyThumbnailSize.valueOf(args.getString(2));
            } catch (Exception e) {
            }
            final long timeStamp = Calendar.getInstance().getTimeInMillis();
            Uri fileUri = Uri.parse(KandyUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE).getAbsolutePath()
                    + "//" + timeStamp + fileName);
            try {
                IKandyFileItem item = KandyMessageBuilder.createFile("", fileUri);
                item.setServerUUID(UUID.fromString(uuid));
                Kandy.getServices().geCloudStorageService().downloadMediaThumbnail(item, thumbnailSize, kandyResponseProgressListener);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }

        } else if (action.equals("cancelMediaTransferFromCloudStorage")) {
            String uuid = args.getString(0);
            String fileName = args.getString(1);
            final long timeStamp = Calendar.getInstance().getTimeInMillis();
            Uri fileUri = Uri.parse(KandyUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE).getAbsolutePath()
                    + "//" + timeStamp + fileName);
            try {
                IKandyFileItem item = KandyMessageBuilder.createFile("", fileUri);
                item.setServerUUID(UUID.fromString(uuid));
                Kandy.getServices().geCloudStorageService().cancelMediaTransfer(item, kandyResponseCancelListener);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }

        } else if (action.equals("getLocalFiles")) {
            File mPath = KandyUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE);
            String[] files = mPath.list();
            JSONArray list = new JSONArray();
            for (String f : files) {
                JSONObject obj = new JSONObject();
                obj.put("name", f);
                obj.put("uri", mPath.getAbsolutePath() + "//" + f);
                list.put(obj);
            }
            callbackContext.success(list);
        } else {
            return super.execute(action, args, ctx); // return false
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case KandyConstant.CONTACT_PICKER_RESULT:
                    case KandyConstant.IMAGE_PICKER_RESULT:
                    case KandyConstant.VIDEO_PICKER_RESULT:
                    case KandyConstant.AUDIO_PICKER_RESULT:
                    case KandyConstant.FILE_PICKER_RESULT:
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("code", requestCode);
                            obj.put("uri", data.getData().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callbackContext.success(obj);
                }
                break;
            case Activity.RESULT_CANCELED:
                callbackContext.error("Operation canceled");
                break;
            default:
                break;
        }
    }

    private void makeToast(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Applies the {@link KandyChatSettings} with user defined settings or default if not set by developer.
     */
    private void applyKandySettings() {
        KandyChatSettings settings = Kandy.getServices().getChatService().getSettings();
        SharedPreferences.Editor edit = prefs.edit();

        if (autoDownloadMediaConnectionType != null) {
            ConnectionType downloadPolicy = ConnectionType.valueOf(autoDownloadMediaConnectionType);
            settings.setAutoDownloadMediaConnectionType(downloadPolicy);
            edit.putString(KandyConstant.PREF_KEY_POLICY, autoDownloadMediaConnectionType);
        }

        if (mediaMaxSize != -1) {
            try {
                settings.setMediaMaxSize(mediaMaxSize);
                edit.putInt(KandyConstant.PREF_KEY_MAX_SIZE, mediaMaxSize);
            } catch (KandyIllegalArgumentException e) {
                Log.d(LCAT, "applyKandyChatSettings: " + e.getMessage());
            }
        }

        if (downloadMediaPath != null) {
            File downloadPath = new File(downloadMediaPath);
            settings.setDownloadMediaPath(downloadPath);
            edit.putString(KandyConstant.PREF_KEY_PATH, downloadMediaPath);
        }

        if (useDownloadCustomPath != null) {
            settings.setDownloadMediaPath(new IKandyChatDownloadPathBuilder() {

                @Override
                public File uploadAbsolutePath() {

                    File dir = new File(Environment.getExternalStorageDirectory(), "custom");
                    dir.mkdirs();

                    return dir;
                }

                @Override
                public File downloadAbsolutPath(KandyRecord sender, KandyRecord recipient, IKandyFileItem fileItem,
                                                boolean isThumbnail) {

                    File dir = new File(Environment.getExternalStorageDirectory(), sender.getUserName());
                    dir.mkdirs();

                    File file = new File(dir, fileItem.getDisplayName());

                    return file;
                }
            });
            edit.putBoolean(KandyConstant.PREF_KEY_CUSTOM_PATH, useDownloadCustomPath);
        }

        if (autoDownloadThumbnailSize != null) { //otherwise will be used default setting from SDK
            KandyThumbnailSize thumbnailSize = KandyThumbnailSize.valueOf(autoDownloadThumbnailSize);
            settings.setAutoDownloadThumbnailSize(thumbnailSize);
            edit.putString(KandyConstant.PREF_KEY_THUMB_SIZE, autoDownloadThumbnailSize);
        }
    }

    /**
     * Register listeners to receive events from Kandy background service.
     */
    private void registerNotificationListener() {
        Log.d(LCAT, "registerNotificationListener() was invoked");
        Kandy.getServices().getCallService().registerNotificationListener(kandyCallServiceNotificationListener);
        Kandy.getServices().getChatService().registerNotificationListener(kandyChatServiceNotificationListener);
        Kandy.getServices().getAddressBookService().registerNotificationListener(kandyAddressBookServiceNotificationListener);
        Kandy.getServices().getGroupService().registerNotificationListener(kandyGroupServiceNotificationListener);
    }

    /**
     * Unregister listeners out of Kandy background service.
     */
    private void unregisterNotificationListener() {
        Log.d(LCAT, "unregisterNotificationListener() was invoked");
        Kandy.getServices().getCallService().unregisterNotificationListener(kandyCallServiceNotificationListener);
        Kandy.getServices().getChatService().unregisterNotificationListener(kandyChatServiceNotificationListener);
        Kandy.getServices().getAddressBookService().unregisterNotificationListener(kandyAddressBookServiceNotificationListener);
        Kandy.getServices().getGroupService().unregisterNotificationListener(kandyGroupServiceNotificationListener);
    }

    private void setKey(String apiKey, String apiSecret) {
        SharedPreferences.Editor edit = prefs.edit();

        edit.putString(KandyConstant.API_KEY_PREFS_KEY, apiKey).apply();
        edit.putString(KandyConstant.API_SECRET_PREFS_KEY, apiSecret).apply();

        Kandy.getGlobalSettings().setKandyDomainSecret(apiSecret);
        Kandy.initialize(activity, apiKey, apiSecret);
    }

    /**
     * Register/login the user on the server with credentials received from admin.
     *
     * @param username The username to use.
     * @param password The password to use.
     */
    private void login(String username, String password) {
        KandyRecord kandyUser;

        try {
            kandyUser = new KandyRecord(username);

        } catch (KandyIllegalArgumentException ex) {
            ex.printStackTrace();
            callbackContext.error(KandyUtils.getString("kandy_login_empty_username_text"));
            return;
        }

        if (password == null || password.isEmpty()) {
            callbackContext.error(KandyUtils.getString("kandy_login_empty_password_text"));
            return;
        }

        Kandy.getAccess().login(kandyUser, password, new KandyLoginResponseListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onLoginSucceeded() {
                Log.d(LCAT, "Kandy.login->onLoginSucceeded() was invoked");
                registerNotificationListener();
                callbackContext.success();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onRequestFailed(int code, String error) {
                Log.d(LCAT, "Kandy.login->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
                callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
            }
        });
    }

    /**
     * Register/login the user on the server by access token.
     *
     * @param token The access token.
     */
    private void loginByToken(String token) {
        if (token == null || token.isEmpty()) {
            callbackContext.error("Invalid access token.");
            return;
        }
        Kandy.getAccess().login(token, new KandyLoginResponseListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onLoginSucceeded() {
                Log.d(LCAT, "Kandy.login->onLoginSucceeded() was invoked");
                registerNotificationListener();
                callbackContext.success();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onRequestFailed(int code, String error) {
                Log.d(LCAT, "Kandy.login->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
                callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
            }
        });
    }

    /**
     * This method unregisters user from the Kandy server.
     */
    private void logout() {
        Kandy.getAccess().logout(new KandyLogoutResponseListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onLogoutSucceeded() {
                Log.d(LCAT, "Kandy.logout->onLoginSucceeded() was invoked");
                unregisterNotificationListener();
                callbackContext.success();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onRequestFailed(int code, String error) {
                Log.d(LCAT, "Kandy.logout->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
                callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
            }
        });
    }

    /**
     * Load previous session.
     *
     * @throws JSONException
     */
    private void getSession() throws JSONException {
        JSONObject obj = new JSONObject();

        JSONObject domain = new JSONObject();
        domain.put("apiKey", Kandy.getSession().getKandyDomain().getApiKey());
        domain.put("apiSecret", Kandy.getSession().getKandyDomain().getApiSecret());
        domain.put("name", Kandy.getSession().getKandyDomain().getName());

        JSONObject user = new JSONObject();
        IKandyUser kuser = Kandy.getSession().getKandyUser();
        user.put("countryCode", kuser.getCountryCode());
        user.put("email", kuser.getEmail());
        user.put("firstName", kuser.getFirstName());
        user.put("lastName", kuser.getLastName());
        user.put("kandyDeviceId", kuser.getKandyDeviceId());
        user.put("nativeDeviceId", kuser.getNativeDeviceId());
        user.put("phoneNumber", kuser.getPhoneNumber());
        user.put("password", kuser.getPassword());
        user.put("pushGCMRegistrationId", kuser.getPushGCMRegistrationId());
        user.put("name", kuser.getUser());
        user.put("id", kuser.getUserId());
        user.put("virtualPhoneNumber", kuser.getVirtualPhoneNumber());

        obj.put("domain", domain);
        obj.put("user", user);

        callbackContext.success(obj);
    }

    public void answerIncomingCall(final IKandyIncomingCall call) {
        if (useNativeCallDialog) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    createIncomingCallPopup(call);
                }
            });
        } else {
            calls.put(call.getCallee().getUri(), call);
        }
    }

    /**
     * Create a native dialog alert on UI thread.
     *
     * @param call incoming call instance
     */
    private void createIncomingCallPopup(final IKandyIncomingCall call) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setPositiveButton(KandyUtils.getString("kandy_calls_answer_button_label"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                doAccept(call);
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(KandyUtils.getString("kandy_calls_ignore_incoming_call_button_label"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ignoreIncomingCall(call);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(KandyUtils.getString("kandy_calls_reject_incoming_call_button_label"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectIncomingCall(call);
                dialog.dismiss();
            }
        });

        builder.setMessage(KandyUtils.getString("kandy_calls_incoming_call_popup_message_label") + call.getCallee().getUri());

        _incomingCallDialog = builder.create();
        _incomingCallDialog.show();
    }

    /**
     * Ignoring the incoming call -  the caller wont know about ignore, call will continue on his side
     *
     * @param call incoming call instance
     */
    public void ignoreIncomingCall(IKandyIncomingCall call) {
        if (call == null) {
            makeToast(KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        call.ignore(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(LCAT, "mCurrentIncomingCall.ignore succeed");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(LCAT, "mCurrentIncomingCall.ignore failed");
            }
        });
    }

    /**
     * Reject the incoming call
     *
     * @param call incoming call instance
     */
    public void rejectIncomingCall(IKandyIncomingCall call) {
        if (call == null) {
            makeToast(KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        call.reject(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(LCAT, "mCurrentIncomingCall.reject succeeded");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(LCAT, "mCurrentIncomingCall.reject. Error: " + err + "\nResponse code: " + responseCode);
            }
        });
    }

    /**
     * Accept incoming call
     *
     * @param call incoming call instance
     */
    public void doAccept(IKandyIncomingCall call) {
        if (call.canReceiveVideo()) {
            call.accept(startWithVideoEnabled, new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    showIncallDialog(call);
                    Log.i(LCAT, "mCurrentIncomingCall.accept succeed");
                }

                @Override
                public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                    Log.i(LCAT, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
                }
            });
        } else {
            call.accept(false, new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    showIncallDialog(call);
                    Log.i(LCAT, "mCurrentIncomingCall.accept succeed");
                }

                @Override
                public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                    Log.i(LCAT, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
                }
            });
        }
    }

    /**
     * Create a native call dialog on UI thread.
     *
     * @param call The current call.
     */

    private void showIncallDialog(final IKandyCall call) {
        showIncallDialog(call, false);
    }

    private void showIncallDialog(final IKandyCall call, final boolean isOutgoingCall) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _incallDialog = new KandyIncallDialog(activity, call);

                if (isOutgoingCall) {
                    _incallDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            ((IKandyOutgoingCall) call).establish(kandyCallResponseListener);
                        }
                    });
                }

                _incallDialog.show();
            }
        });
    }


    /**
     * Create a voip call.
     *
     * @param username The username of the callee.
     */
    private void createVoipCall(String username, boolean videoEnabled) {
        KandyRecord callee;
        try {
            callee = new KandyRecord(username);
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            callbackContext.error(KandyUtils.getString("kandy_calls_invalid_phone_text_msg"));
            return;
        }

        KandyOutgingVoipCallOptions callOptions = videoEnabled ? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO : KandyOutgingVoipCallOptions.START_CALL_WITHOUT_VIDEO;
        IKandyCall call = Kandy.getServices().getCallService().createVoipCall(null, callee, callOptions);
        setKandyVideoViewsAndEstablishCall(call);
    }

    private void setKandyVideoViewsAndEstablishCall(IKandyCall call) {
        if (useNativeCallDialog) {
            showIncallDialog(call, true);
        } else {
            KandyVideoView localVideo = new KandyVideoView(activity);
            KandyVideoView remoteVideo = new KandyVideoView(activity);

            localVideo.setLocalVideoView(call);
            remoteVideo.setRemoteVideoView(call);

            calls.put(call.getCallee().getUri(), call);
            localVideoViews.put(call.getCallee().getUri(), localVideo);
            remoteVideoViews.put(call.getCallee().getUri(), remoteVideo);

            ((IKandyOutgoingCall) call).establish(kandyCallResponseListener);
        }
    }

    /**
     * Create a PSTN call.
     *
     * @param number The number phone of the callee.
     */
    private void createPSTNCall(String number) {
        number = number.replace("+", "");
        number = number.replace("-", "");

        IKandyCall call = Kandy.getServices().getCallService().createPSTNCall(null, number, null);
        setKandyVideoViewsAndEstablishCall(call);
    }

    /**
     * Create a SIP Trunk call.
     *
     * @param number The number phone of the callee.
     */
    private void createSIPTrunkCall(String number) {
        KandyRecord callee = null;

        try {
            number = KandyRecord.normalize(number);
            callee = new KandyRecord(number);
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            callbackContext.error(KandyUtils.getString("kandy_calls_invalid_phone_text_msg"));

        }

        IKandyCall call = Kandy.getServices().getCallService().createSIPTrunkCall(null, callee);
        setKandyVideoViewsAndEstablishCall(call);
    }

    /**
     * Check call exists.
     *
     * @param id The callee uri.
     * @return
     */
    private boolean checkActiveCall(String id) {
        if (!calls.containsKey(id)) {
            if (!useNativeCallDialog)
                callbackContext.error(KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
            return false;
        }
        return true;
    }

    /**
     * Remove call and video views out of lists.
     *
     * @param id The callee uri.
     */
    private void removeCall(String id) {
        if (ringin.isPlaying()) {
            ringin.pause();
            ringin.seekTo(0);
        }
        if (ringout.isPlaying()) {
            ringout.pause();
            ringout.seekTo(0);
        }
        if (calls.containsKey(id))
            calls.remove(id);
        if (localVideoViews.containsKey(id)) {
            localVideoViews.get(id).dismiss();
            localVideoViews.remove(id);
        }
        if (remoteVideoViews.containsKey(id)) {
            remoteVideoViews.get(id).dismiss();
            remoteVideoViews.remove(id);
        }
    }

    /**
     * Show local call video view.
     *
     * @param id     The user uri.
     * @param id     The callee uri.
     * @param left   The co-ordinate of X position.
     * @param top    The co-ordinate of Y position.
     * @param width  The width of of Video that needs to show.
     * @param height The height of of Video that needs to show.
     */
    private void showLocalVideo(String id, int left, int top, int width, int height) {
        if (!checkActiveCall(id)) return;

        KandyVideoView localVideo = localVideoViews.get(id);
        localVideo.layout(left, top, width, height);
        localVideo.show();

        callbackContext.success();
    }

    /**
     * Show remote call video view.
     *
     * @param id     The user uri.
     * @param id     The callee uri.
     * @param left   The co-ordinate of X position.
     * @param top    The co-ordinate of Y position.
     * @param width  The width of of Video that needs to show.
     * @param height The height of of Video that needs to show.
     */
    private void showRemoteVideo(String id, int left, int top, int width, int height) {
        if (!checkActiveCall(id)) return;

        KandyVideoView remoteVideo = remoteVideoViews.get(id);
        remoteVideo.layout(left, top, width, height);
        remoteVideo.show();

        callbackContext.success();
    }

    /**
     * Hide local video view.
     *
     * @param id The callee uri.
     */
    private void hideLocalVideo(String id) {
        if (!checkActiveCall(id)) return;

        KandyVideoView localVideo = localVideoViews.get(id);
        localVideo.hide();
        callbackContext.success();
    }

    /**
     * Hide remote video view.
     *
     * @param id The callee uri.
     */
    private void hideRemoteVideo(String id) {
        if (!checkActiveCall(id)) return;

        KandyVideoView remoteVideo = remoteVideoViews.get(id);
        remoteVideo.hide();
        callbackContext.success();
    }

    /**
     * Accept a incoming call.
     *
     * @param videoEnabled Video enabled or not (use NativeView).
     */
    private void accept(String id, boolean videoEnabled) {
        if (!checkActiveCall(id)) return;

        IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
        KandyVideoView localVideo = new KandyVideoView(activity);
        KandyVideoView remoteVideo = new KandyVideoView(activity);

        localVideo.setLocalVideoView(call);
        remoteVideo.setRemoteVideoView(call);

        localVideoViews.put(call.getCallee().getUri(), localVideo);
        remoteVideoViews.put(call.getCallee().getUri(), remoteVideo);

        call.accept(videoEnabled, kandyCallResponseListener);
    }

    /**
     * Reject current coming call.
     */
    private void reject(String id) {
        if (!checkActiveCall(id)) return;
        IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
        call.reject(kandyCallResponseListener);
    }

    /**
     * Ignore current coming call.
     */
    private void ignore(String id) {
        if (!checkActiveCall(id)) return;
        IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
        call.ignore(kandyCallResponseListener);
    }

    /**
     * Hangup current call.
     */
    private void doHangup(String id) {
        if (!checkActiveCall(id)) return;

        IKandyCall call = calls.get(id);
        call.hangup(kandyCallResponseListener);
        removeCall(id);
    }

    /**
     * Mute/Unmute current call.
     *
     * @param mute The mute state.
     */
    private void switchMuteState(String id, boolean mute) {
        if (!checkActiveCall(id)) return;

        IKandyCall call = calls.get(id);

        if (mute) {
            call.mute(kandyCallResponseListener);
        } else {
            call.unmute(kandyCallResponseListener);
        }
    }

    /**
     * Hold/Unhole current call.
     *
     * @param hold The hold state.
     */
    private void switchHoldState(String id, boolean hold) {
        if (!checkActiveCall(id)) return;

        IKandyCall call = calls.get(id);

        if (hold) {
            call.hold(kandyCallResponseListener);
        } else {
            call.unhold(kandyCallResponseListener);
        }
    }

    /**
     * Whether or not The sharing video is enabled.
     *
     * @param video The state video sharing.
     */
    private void switchVideoCallState(String id, boolean video) {
        if (!checkActiveCall(id)) return;

        IKandyCall call = calls.get(id);

        if (video) {
            call.startVideoSharing(kandyCallResponseListener);
        } else {
            call.stopVideoSharing(kandyCallResponseListener);
        }
    }

    /**
     * Switch between front and back camera.
     *
     * @param id         The callee uri.
     * @param cameraInfo The {@Link KandyCameraInfo}
     */
    private void switchCamera(String id, KandyCameraInfo cameraInfo) {
        if (!checkActiveCall(id)) return;
        IKandyCall call = calls.get(id);
        call.switchCamera(cameraInfo);
    }

    private void transferCall(String id, String destination) {
        if (!checkActiveCall(id)) return;
        IKandyCall call = calls.get(id);
        call.transfer(destination, kandyCallResponseListener);
    }

    /**
     * Send text SMS message.
     *
     * @param destination The recipient user.
     * @param text        The message to send.
     */
    private void sendSMS(String destination, String text) {
        if (text == null || text.equals("")) {
            callbackContext.error(KandyUtils.getString("kandy_chat_message_empty_message"));
            return;
        }

        final KandySMSMessage message;

        try {
            message = new KandySMSMessage(destination, "Kandy SMS", text);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(KandyUtils.getString("kandy_chat_message_invalid_phone"));
            Log.e(LCAT, "sendSMS: " + " " + e.getLocalizedMessage(), e);
            return;
        }

        Kandy.getServices().getChatService().sendSMS(message, kandyResponseListener);
    }

    /**
     * Send a chat message with {@link IKandyMediaItem}.
     *
     * @param destination The recipient user.
     * @param data        The media item.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendChatMessage(String destination, IKandyMediaItem data, String type) {
        KandyRecord recipient;
        try {
            KandyRecordType recordType;
            try {
                recordType = KandyRecordType.valueOf(type);
            } catch (Exception ex) {
                recordType = KandyRecordType.CONTACT;
            }

            recipient = new KandyRecord(destination, recordType);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(KandyUtils.getString("kandy_chat_message_invalid_phone"));
            Log.e(LCAT, "sendChatMessage: " + " " + e.getLocalizedMessage(), e);
            return;
        }

        KandyChatMessage message = new KandyChatMessage(recipient, data);
        Kandy.getServices().getChatService().sendChat(message, kandyUploadProgressListener);
    }

    /**
     * Send text chat message.
     *
     * @param destination The recipient user.
     * @param text        The message to send.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendChat(String destination, String text, String type) {
        IKandyTextItem kandyText = KandyMessageBuilder.createText(text);
        sendChatMessage(destination, kandyText, type);
    }

    /**
     * Pick audio by android default audio picker
     */
    private void pickAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        cordova.startActivityForResult(this, intent, KandyConstant.AUDIO_PICKER_RESULT);
    }

    /**
     * Send a audio message.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the audio file.
     * @param uri         The uri of the audio file.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendAudio(String destination, String caption, String uri, String type) {
        IKandyAudioItem kandyAudio = null;
        try {
            kandyAudio = KandyMessageBuilder.createAudio(caption, Uri.parse(uri));
        } catch (KandyIllegalArgumentException e) {
            Log.d(LCAT, "sendAudio: " + e.getLocalizedMessage(), e);
        }

        sendChatMessage(destination, kandyAudio, type);
    }

    /**
     * Pick contact by android default contact picker
     */
    public void pickContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        cordova.startActivityForResult(this, contactPickerIntent, KandyConstant.CONTACT_PICKER_RESULT);
    }

    /**
     * Send a contact message.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the contact.
     * @param uri         The uri of the contact.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendContact(String destination, String caption, String uri, String type) {
        IKandyContactItem kandyContact = null;
        try {
            kandyContact = KandyMessageBuilder.createContact(caption, Uri.parse(uri));
        } catch (KandyIllegalArgumentException e) {
            Log.d(LCAT, "sendContact: " + e.getLocalizedMessage(), e);
        }

        sendChatMessage(destination, kandyContact, type);
    }

    /**
     * Pick video by android default video picker
     */
    private void pickVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        cordova.startActivityForResult(this, intent, KandyConstant.VIDEO_PICKER_RESULT);
    }

    /**
     * Send a video message.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the video file.
     * @param uri         The uri of the video file.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendVideo(String destination, String caption, String uri, String type) {
        IKandyVideoItem kandyVideo = null;
        try {
            kandyVideo = KandyMessageBuilder.createVideo(caption, Uri.parse(uri));
        } catch (KandyIllegalArgumentException e) {
            Log.d(LCAT, "sendVideo: " + e.getLocalizedMessage(), e);
        }

        sendChatMessage(destination, kandyVideo, type);
    }

    /**
     * Send current location.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the current location.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendCurrentLocation(final String destination, final String caption, final String type) {
        try {
            Kandy.getServices().getLocationService().getCurrentLocation(new KandyCurrentLocationListener() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onCurrentLocationReceived(Location location) {
                    Log.d(LCAT, "sendCurrentLocation->KandyCurrentLocationListener->onCurrentLocationReceived() was invoked: " + location.toString());
                    sendLocation(destination, caption, location, type);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onCurrentLocationFailed(int code, String error) {
                    Log.d(LCAT, "sendCurrentLocation->KandyCurrentLocationListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
                    callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
                }
            });
        } catch (KandyIllegalArgumentException e) {
            Log.e(LCAT, "sendCurrentLocation(); " + e.getLocalizedMessage(), e);
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * addParticipants
     * Send a location message.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the location.
     * @param location    The location to send.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendLocation(String destination, String caption, Location location, String type) {
        IKandyLocationItem kandyLocation = KandyMessageBuilder.createLocation(caption, location);
        sendChatMessage(destination, kandyLocation, type);
    }

    /**
     * Pick image by android default gallery picker
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        cordova.startActivityForResult(this, intent, KandyConstant.IMAGE_PICKER_RESULT);
    }

    /**
     * Send a image message.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the image.
     * @param uri         The uri of the image.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendImage(String destination, String caption, String uri, String type) {
        IKandyImageItem kandyImage = null;
        try {
            kandyImage = KandyMessageBuilder.createImage(caption, Uri.parse(uri));
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
        }
        sendChatMessage(destination, kandyImage, type);
    }

    /**
     * Pick a file by android default picker
     */
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            cordova.startActivityForResult(this, Intent.createChooser(intent, "Select a File to Upload"), KandyConstant.FILE_PICKER_RESULT);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show(); }
            callbackContext.error("Please install a File Manager");
        }
    }

    /**
     * Send a file message.
     *
     * @param destination The recipient user.
     * @param caption     The caption of the file.
     * @param uri         The uri of the file.
     * @param type        The {@link KandyRecordType} to use.
     */
    private void sendFile(String destination, String caption, String uri, String type) {
        IKandyFileItem kandyFile = null;
        try {
            kandyFile = KandyMessageBuilder.createFile(caption, Uri.parse(uri));
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
        }
        sendChatMessage(destination, kandyFile, type);
    }

    private void openChooserDialog(final String recipient, final String caption, final String type) {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(KandyUtils.getLayout("kandy_chooser_dialog"));
        dialog.setTitle("Attachment Chooser");

        dialog.findViewById(KandyUtils.getId("kandy_chat_img_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
                dialog.dismiss();
            }
        });
        dialog.findViewById(KandyUtils.getId("kandy_chat_audio_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAudio();
                dialog.dismiss();
            }
        });
        dialog.findViewById(KandyUtils.getId("kandy_chat_video_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVideo();
                dialog.dismiss();
            }
        });
        dialog.findViewById(KandyUtils.getId("kandy_chat_contact_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact();
                dialog.dismiss();
            }
        });
        dialog.findViewById(KandyUtils.getId("kandy_chat_file_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
                dialog.dismiss();
            }
        });
        dialog.findViewById(KandyUtils.getId("kandy_chat_location_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCurrentLocation(recipient, caption, type);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Get {@link KandyChatMessage} from {@link UUID}.
     *
     * @param uuid The {@link UUID}.
     * @return The {@link KandyChatMessage}.
     * @throws KandyIllegalArgumentException
     */
    private KandyChatMessage getMessageFromUUID(String uuid) throws KandyIllegalArgumentException {
        KandyChatMessage message = new KandyChatMessage(new KandyRecord("dummy@dummy.com"), "dummy"); // FIXME: dummy?
        message.setUUID(UUID.fromString(uuid));
        return message;
    }

    /**
     * Cancel current media transfer process.
     *
     * @param uuid The UUID of the message.
     */
    private void cancelMediaTransfer(String uuid) {
        try {
            KandyChatMessage message = getMessageFromUUID(uuid);
            Kandy.getServices().getChatService().cancelMediaTransfer(message, kandyResponseCancelListener);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Download a media message.
     *
     * @param uuid The UUID of the message.
     */
    private void downloadMedia(String uuid) {
        try {
            KandyChatMessage message = getMessageFromUUID(uuid);
            Kandy.getServices().getChatService().downloadMedia(message, kandyResponseProgressListener);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Download the thumbnail of the media message.
     *
     * @param uuid The UUID of the message.
     */
    private void downloadMediaThumbnail(String uuid, KandyThumbnailSize thumbnailSize) {
        try {
            KandyChatMessage message = getMessageFromUUID(uuid);
            Kandy.getServices().getChatService().downloadMediaThumbnail(message, thumbnailSize, kandyResponseProgressListener);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mark message as received.
     *
     * @param uuid The UUID of the message.
     */
    private void markAsReceived(String uuid) {
        try {
            getMessageFromUUID(uuid).markAsReceived(kandyResponseListener);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new group.
     *
     * @param groupName The new group name.
     */
    private void createGroup(String groupName) {
        KandyGroupParams params = new KandyGroupParams();
        params.setGroupName(groupName);

        Kandy.getServices().getGroupService().createGroup(params, kandyGroupResponseListener);
    }

    /**
     * Try to get the state presence of users.
     *
     * @param args The {@link JSONArray} users array.
     * @throws JSONException
     */
    private void retrievePresence(JSONArray args) throws JSONException {
        ArrayList<KandyRecord> list = new ArrayList<KandyRecord>();

        try {
            for (int i = 0; i < args.length(); ++i)
                list.add(new KandyRecord(args.getString(i)));
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }

        if (list.size() == 0) return;

        Kandy.getServices().getPresenceService().retrievePresence(list, kandyPresenceResponseListener);
    }

    /**
     * Enable Push Notification service.
     */
    private void enablePushNotification() {
        GCMRegistrar.checkDevice(activity);
        GCMRegistrar.checkManifest(activity);
        String registrationId = GCMRegistrar.getRegistrationId(activity);

        if (TextUtils.isEmpty(registrationId)) {
            GCMRegistrar.register(activity, KandyConstant.GCM_PROJECT_ID);
        }

        Kandy.getServices().getPushService().enablePushNotification(registrationId, kandyResponseListener);
    }

    /**
     * Disable Push Notification service.
     */
    private void disablePushNotification() {
        Kandy.getServices().getPushService().disablePushNotification(kandyResponseListener);
    }

    /**
     * Try to get contact details.
     *
     * @param contact The {@link IKandyContact} to use.
     * @return The {@link JSONObject} contact details.
     * @throws JSONException
     */
    private JSONObject getContactDetails(IKandyContact contact) throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("displayName", contact.getDisplayName());
        if (contact.getServerIdentifier() != null)
            obj.put("serverIdentifier", contact.getServerIdentifier().getUri());
        obj.put("emails", getEmailsFromContact(contact));
        obj.put("phones", getPhonesFromContact(contact));

        return obj;
    }

    /**
     * Try to get the phones list from the contact.
     *
     * @param contact The {@link IKandyContact} to use.
     * @return The {@link JSONArray} phones list.
     * @throws JSONException
     */
    private JSONArray getPhonesFromContact(IKandyContact contact) throws JSONException {
        JSONArray phones = new JSONArray();

        if (contact.getNumbers() != null) {
            for (KandyPhoneContactRecord phone : contact.getNumbers()) {
                JSONObject p = new JSONObject();
                p.put("number", phone.getNumber());
                p.put("type", phone.getType().name());
                phones.put(p);
            }
        }

        return phones;
    }

    /**
     * Try to get the emails list from the contact.
     *
     * @param contact The {@link IKandyContact} to use.
     * @return The {@link JSONArray} emails list.
     * @throws JSONException
     */
    private JSONArray getEmailsFromContact(IKandyContact contact) throws JSONException {
        JSONArray emails = new JSONArray();

        if (contact.getEmails() != null) {
            for (KandyEmailContactRecord email : contact.getEmails()) {
                JSONObject e = new JSONObject();
                e.put("address", email.getAddress());
                e.put("type", email.getType().name());
                emails.put(e);
            }
        }

        return emails;
    }

    //***** LISTENERS *****//

    private KandyCallResponseListener kandyCallResponseListener = new KandyCallResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceeded(IKandyCall call) {
            Log.d(LCAT, "KandyCallResponseListener->onRequestSucceeded() was invoked: " + call.getCallId());

            JSONObject result = KandyUtils.getJsonObjectFromKandyCall(call);
            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(IKandyCall call, int code, String error) {
            Log.d(LCAT, "KandyCallResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyResponseListener kandyResponseListener = new KandyResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded() {
            Log.d(LCAT, "KandyResponseListener->onRequestSucceeded() was invoked.");
            callbackContext.success();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyResponseCancelListener kandyResponseCancelListener = new KandyResponseCancelListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCancelSucceded() {
            Log.d(LCAT, "KandyResponseCancelListener->onCancelSucceded() was invoked.");
            callbackContext.success();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyResponseCancelListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));

        }
    };

    private KandyResponseProgressListener kandyResponseProgressListener = new KandyResponseProgressListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded(Uri uri) {
            Log.d(LCAT, "KandyResponseProgressListener->onRequestSucceded() was invoked: " + uri);
            callbackContext.success(uri.toString());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onProgressUpdate(IKandyTransferProgress progress) {
            Log.d(LCAT, "KandyResponseProgressListener->onRequestSucceded() was invoked: " + progress.getState().toString() + " " + progress.getProgress());

            JSONObject result = new JSONObject();

            try {
                result.put("process", progress.getProgress());
                result.put("state", progress.getState().toString());
                result.put("byteTransfer", progress.getByteTransfer());
                result.put("byteExpected", progress.getByteExpected());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(callbackContext, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyResponseProgressListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyGroupResponseListener kandyGroupResponseListener = new KandyGroupResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded(KandyGroup kandyGroup) {
            Log.d(LCAT, "KandyGroupResponseListener->onRequestSucceeded() was invoked: " + kandyGroup.getGroupId().getUri());
            callbackContext.success(KandyUtils.getJsonObjectFromKandyGroup(kandyGroup));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyGroupResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyGroupsResponseListener kandyGroupsResponseListener = new KandyGroupsResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded(List<KandyGroup> groups) {
            Log.d(LCAT, "KandyGroupsResponseListener->onRequestSucceeded() was invoked: " + groups.size());

            JSONArray result = new JSONArray();

            for (KandyGroup group : groups)
                result.put(KandyUtils.getJsonObjectFromKandyGroup(group));

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyGroupsResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyValidationResponseListener kandyValidationResponseListener = new KandyValidationResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSuccess(IKandyValidationResponse response) {
            Log.d(LCAT, "KandyValidationResponseListener->onRequestSucceeded() was invoked: " + response.toString());

            JSONObject result = new JSONObject();
            try {
                result.put("id", response.getUserId());
                result.put("domain", response.getDomainName());
                result.put("username", response.getUser());
                result.put("password", response.getUserPassword()); // FIXME: security?
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyValidationResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyProvsionResponseListener kandyProvsionResponseListener = new KandyProvsionResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSuccess(IKandyUser user) {
            Log.d(LCAT, "kandyProvsionResponseListener->onRequestSucceeded() was invoked: " + user.getUser());

            JSONObject result = new JSONObject();
            try {
                result.put("countryCode", user.getCountryCode());
                result.put("email", user.getEmail());
                result.put("firstName", user.getFirstName());
                result.put("lastName", user.getLastName());
                result.put("kandyDeviceId", user.getKandyDeviceId());
                result.put("nativeDeviceId", user.getNativeDeviceId());
                result.put("phoneNumber", user.getPhoneNumber());
                result.put("password", user.getPassword());
                result.put("pushGCMRegistrationId", user.getPushGCMRegistrationId());
                result.put("name", user.getUser());
                result.put("id", user.getUserId());
                result.put("virtualPhoneNumber", user.getVirtualPhoneNumber());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "kandyProvsionResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyUploadProgressListener kandyUploadProgressListener = new KandyUploadProgressListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onProgressUpdate(IKandyTransferProgress progress) {
            Log.d(LCAT, "KandyUploadProgressListener->onProgressUpdate() was invoked: " + progress.getState().toString() + " " + progress.getProgress());

            JSONObject result = new JSONObject();

            try {
                result.put("process", progress.getProgress());
                result.put("state", progress.getState().toString());
                result.put("byteTransfer", progress.getByteTransfer());
                result.put("byteExpected", progress.getByteExpected());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(callbackContext, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded() {
            Log.d(LCAT, "KandyUploadProgressListener->onRequestSucceded() was invoked.");
            callbackContext.success();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyUploadProgressListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyCountryInfoResponseListener kandyCountryInfoResponseListener = new KandyCountryInfoResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSuccess(IKandyAreaCode response) {
            Log.d(LCAT, "KandyCountryInfoResponseListener->onRequestSucceeded() was invoked: " + response.getCountryCode());

            JSONObject result = new JSONObject();

            try {
                result.put("code", response.getCountryCode());
                result.put("long", response.getCountryNameLong());
                result.put("short", response.getCountryNameShort());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyCountryInfoResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyCurrentLocationListener kandyCurrentLocationListener = new KandyCurrentLocationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCurrentLocationReceived(Location location) {
            Log.d(LCAT, "KandyCurrentLocationListener->onRequestSucceeded() was invoked: " + location.toString());

            JSONObject result = new JSONObject();

            try {
                result.put("provider", location.getProvider());
                result.put("time", location.getTime());
                result.put("latitude", location.getLatitude());
                result.put("longitude", location.getLongitude());
                result.put("hasAltitude", location.hasAltitude());
                result.put("altitude", location.getAltitude());
                result.put("hasSpeed", location.hasSpeed());
                result.put("speed", location.getSpeed());
                result.put("hasBearing", location.hasBearing());
                result.put("bearing", location.getBearing());
                result.put("hasAccuracy", location.hasAccuracy());
                result.put("accuracy", location.getAccuracy());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCurrentLocationFailed(int code, String error) {
            Log.d(LCAT, "KandyCurrentLocationListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyPresenceResponseListener kandyPresenceResponseListener = new KandyPresenceResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceed(ArrayList<IKandyPresence> presences, ArrayList<KandyRecord> absences) {
            Log.d(LCAT, "KandyPresenceResponseListener->onRequestSucceeded() was invoked: presences: " + presences.size() + " and absences: " + absences.size());

            JSONObject result = new JSONObject();

            try {
                JSONArray presencesList = new JSONArray();
                for (IKandyPresence online : presences) {
                    JSONObject obj = new JSONObject();
                    obj.put("user", online.getUser().getUri());
                    obj.put("lastSeen", online.getLastSeenDate().toString());
                    presencesList.put(obj);
                }
                result.put("presences", presencesList);

                JSONArray absencesList = new JSONArray();
                for (KandyRecord offline : absences) {
                    absencesList.put(offline.getUri());
                }
                result.put("absences", absencesList);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyPresenceResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyContactsListener kandyContactsListener = new KandyContactsListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded(List<IKandyContact> contacts) {
            Log.d(LCAT, "KandyDeviceContactsListener->onRequestSucceeded() was invoked: " + contacts.size());

            JSONArray result = new JSONArray();

            try {
                for (IKandyContact contact : contacts) {
                    result.put(getContactDetails(contact));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyDeviceContactsListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyContactListener kandyContactListener = new KandyContactListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded(IKandyContact contact) {
            Log.d(LCAT, "KandyDeviceContactListener->onRequestSucceeded() was invoked: " + contact.getId());

            JSONObject result = new JSONObject();
            try {
                result = getContactDetails(contact);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyDeviceContactListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyConnectServiceNotificationListener kandyConnectServiceNotificationListener = new KandyConnectServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onConnectionStateChanged(KandyConnectionState state) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onConnectionStateChanged() was invoked: " + state.toString());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onConnectionStateChanged");
                result.put("data", state.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketConnected() {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketConnected() was invoked.");

            JSONObject result = new JSONObject();
            try {
                result.put("action", "onSocketConnected");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketConnecting() {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketConnecting() was invoked.");

            JSONObject result = new JSONObject();
            try {
                result.put("action", "onSocketConnecting");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketDisconnected() {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketDisconnected() was invoked.");

            JSONObject result = new JSONObject();
            try {
                result.put("action", "onSocketDisconnected");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketFailedWithError(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketFailedWithError() was invoked: " + error);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onSocketFailedWithError");
                result.put("data", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onInvalidUser(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onInvalidUser() was invoked: " + error);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onInvalidUser");
                result.put("data", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSessionExpired(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSessionExpired() was invoked: " + error);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onSessionExpired");
                result.put("data", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSDKNotSupported(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSDKNotSupported() was invoked: " + error);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onSDKNotSupported");
                result.put("data", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCertificateError(String error) {
            JSONObject result = new JSONObject();

            try {
                result.put("action", "onCertificateError");
                result.put("data", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServerConfigurationReceived(JSONObject config) {
            JSONObject result = new JSONObject();

            try {
                result.put("action", "onServerConfigurationReceived");
                result.put("data", config);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, result);
        }
    };

    private KandyCallServiceNotificationListener kandyCallServiceNotificationListener = new KandyCallServiceNotificationListener() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onIncomingCall(IKandyIncomingCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onIncomingCall() was invoked: " + call.getCallId());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onIncomingCall");
                JSONObject data = KandyUtils.getJsonObjectFromKandyCall(call);
                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ringin.start();

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);

            answerIncomingCall(call);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onMissedCall(KandyMissedCallMessage call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onMissedCall() was invoked: " + call.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onMissedCall");

                JSONObject data = new JSONObject();

                data.put("uri", call.getSource().getUri());
                data.put("uuid", call.getUUID());
                data.put("timestamp", call.getTimestamp());
                data.put("via", call.getVia());
                data.put("eventType", call.getEventType().name());

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);
            removeCall(call.getSource().getUri());
        }

        @Override
        public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage event) {
            Log.d(LCAT, "onWaitingVoiceMailCall: event: " + event);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCallStateChanged(KandyCallState state, IKandyCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onCallStateChanged() was invoked: " + call.getCallId() + "and state: " + state.toString());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onCallStateChanged");
                JSONObject data = KandyUtils.getJsonObjectFromKandyCall(call);
                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);

            switch (state) {
                case TERMINATED: {
                    if (_incallDialog != null && _incallDialog.isShowing())
                        _incallDialog.dismiss();
                    if (_incomingCallDialog != null && _incomingCallDialog.isShowing())
                        _incomingCallDialog.dismiss();
                    _incallDialog = null;
                    _incomingCallDialog = null;
                    removeCall(call.getCallee().getUri());
                    break;
                }
                // TODO: something here
                case INITIAL:
                case DIALING: {
                    //ringin.start();
                    break;
                }
                case RINGING: {
                    ringout.start();
                    break;
                }
                case ON_DOUBLE_HOLD:
                case ON_HOLD:
                case REMOTELY_HELD:
                case TALKING:
                default: {
                    if (ringin.isPlaying()) {
                        ringin.pause();
                        ringin.seekTo(0);
                    }
                    if (ringout.isPlaying()) {
                        ringout.pause();
                        ringout.seekTo(0);
                    }
                    break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onVideoStateChanged(IKandyCall call, boolean receiving, boolean sending) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onVideoStateChanged() was invoked: "
                    + call.getCallId() + " and receiving state: " + receiving + " and sending state: " + sending);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onVideoStateChanged");
                JSONObject data = KandyUtils.getJsonObjectFromKandyCall(call);
                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);

            if (_incallDialog != null && _incallDialog.isShowing()) {
                _incallDialog.switchVideoView(sending, receiving);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGSMCallIncoming(IKandyCall call, String incomingNumber) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onGSMCallIncoming() was invoked: " + call.getCallId());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onGSMCallIncoming");
                JSONObject data = KandyUtils.getJsonObjectFromKandyCall(call);
                data.put("incomingNumber", incomingNumber);
                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGSMCallConnected(IKandyCall call, String incomingNumber) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onGSMCallConnected() was invoked: " + call.getCallId());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onGSMCallConnected");
                JSONObject data = KandyUtils.getJsonObjectFromKandyCall(call);
                data.put("incomingNumber", incomingNumber);
                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);

            if (useNativeCallDialog) {
                if (_incallDialog != null)
                    _incallDialog.switchHold(true);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGSMCallDisconnected(IKandyCall call, String incomingNumber) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onGSMCallDisconnected() was invoked: " + call.getCallId());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onGSMCallDisconnected");
                JSONObject data = KandyUtils.getJsonObjectFromKandyCall(call);
                data.put("incomingNumber", incomingNumber);
                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationPluginCallback, result);

            if (useNativeCallDialog) {
                if (_incallDialog != null)
                    _incallDialog.switchHold(false);
            }
        }
    };

    private KandyChatServiceNotificationListener kandyChatServiceNotificationListener = new KandyChatServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatReceived(IKandyMessage message, KandyRecordType type) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatReceived() was invoked: " + message.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onChatReceived");

                JSONObject data = new JSONObject();

                data.put("type", type.toString());
                data.put("message", message.toJson().getJSONObject("message"));

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, result);

            if (acknowledgeOnMsgReceived)
                markAsReceived(message.getUUID().toString());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatDelivered(KandyDeliveryAck ack) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatDelivered() was invoked: " + ack.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onChatDelivered");
                result.put("data", ack.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatMediaAutoDownloadProgress(IKandyMessage message, IKandyTransferProgress process) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadProgress() was invoked: " + process);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onChatMediaAutoDownloadProgress");

                JSONObject data = new JSONObject();

                data.put("process", process.getProgress());
                data.put("state", process.getState());
                data.put("byteTransfer", process.getByteTransfer());
                data.put("byteExpected", process.getByteExpected());
                data.put("message", message.toJson().getJSONObject("message"));

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatMediaAutoDownloadFailed(IKandyMessage message, int code, String error) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadFailed() was invoked.");

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onChatMediaAutoDownloadFailed");

                JSONObject data = new JSONObject();

                data.put("error", error);
                data.put("code", code);
                data.put("message", message.toJson().getJSONObject("message"));

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri uri) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadSucceded() was invoked: " + uri);

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onChatMediaAutoDownloadSucceded");

                JSONObject data = new JSONObject();

                data.put("uri", uri);
                data.put("message", message.toJson().getJSONObject("message"));

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, result);
            KandyUtils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, result);
        }
    };

    private KandyAddressBookServiceNotificationListener kandyAddressBookServiceNotificationListener
            = new KandyAddressBookServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDeviceAddressBookChanged() {
            Log.d(LCAT, "KandyAddressBookServiceNotificationListener->onDeviceAddressBookChanged() was invoked.");

            JSONObject result = new JSONObject();
            try {
                result.put("action", "onDeviceAddressBookChanged");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyAddressBookServiceNotificationCallback, result);
        }
    };

    private KandyGroupServiceNotificationListener kandyGroupServiceNotificationListener = new KandyGroupServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGroupDestroyed(IKandyGroupDestroyed group) {
            Log.d(LCAT, "KandyGroupServiceNotificationListener->onGroupDestroyed() was invoked: " + group.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onGroupDestroyed");

                JSONObject data = new JSONObject();
                if (group.getGroupId() != null)
                    data.put("id", group.getGroupId().getUri());
                data.put("uuid", group.getUUID());
                data.put("timestamp", group.getTimestamp());
                if (group.getEraser() != null)
                    data.put("eraser", group.getEraser().getUri());

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyGroupServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGroupUpdated(IKandyGroupUpdated group) {
            Log.d(LCAT, "KandyGroupServiceNotificationListener->onGroupUpdated() was invoked: " + group.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onGroupUpdated");

                JSONObject data = new JSONObject();

                if (group.getGroupId() != null)
                    data.put("id", group.getGroupId().getUri());
                data.put("uuid", group.getUUID());
                data.put("timestamp", group.getTimestamp());
                if (group.getGroupParams() != null) {
                    JSONObject obj = new JSONObject();
                    KandyGroupParams groupParams = group.getGroupParams();

                    obj.put("name", groupParams.getGroupName());
                    obj.put("image", groupParams.getImageUri());

                    data.put("groupParams", obj);
                }

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyGroupServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onParticipantJoined(IKandyGroupParticipantJoined participantJoined) {
            Log.d(LCAT, "KandyGroupServiceNotificationListener->onParticipantJoined() was invoked: " + participantJoined.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onParticipantJoined");

                JSONObject data = new JSONObject();

                data.put("uuid", participantJoined.getUUID());
                data.put("groupId", KandyUtils.getJsonObjectFromKandyRecord(participantJoined.getGroupId()));
                data.put("inviter", KandyUtils.getJsonObjectFromKandyRecord(participantJoined.getInviter()));
                data.put("timestamp", participantJoined.getTimestamp());

                if (participantJoined.getInvitees() != null) {
                    List<KandyRecord> invitees = participantJoined.getInvitees();
                    JSONArray is = new JSONArray();

                    for (KandyRecord record : invitees) {
                        is.put(KandyUtils.getJsonObjectFromKandyRecord(record));
                    }

                    data.put("invitees", is);
                }

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyGroupServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onParticipantKicked(IKandyGroupParticipantKicked participantKicked) {
            Log.d(LCAT, "KandyGroupServiceNotificationListener->onParticipantKicked() was invoked: " + participantKicked.getUUID());

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onParticipantKicked");

                JSONObject data = new JSONObject();

                data.put("uuid", participantKicked.getUUID());
                data.put("groupId", KandyUtils.getJsonObjectFromKandyRecord(participantKicked.getGroupId()));
                data.put("booter", KandyUtils.getJsonObjectFromKandyRecord(participantKicked.getBooter()));
                data.put("timestamp", participantKicked.getTimestamp());

                if (participantKicked.getBooted() != null) {
                    List<KandyRecord> booted = participantKicked.getBooted();
                    JSONArray is = new JSONArray();

                    for (KandyRecord record : booted) {
                        is.put(KandyUtils.getJsonObjectFromKandyRecord(record));
                    }

                    data.put("booted", is);
                }

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyGroupServiceNotificationCallback, result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onParticipantLeft(IKandyGroupParticipantLeft participantLeft) {
            Log.d(LCAT, "KandyGroupServiceNotificationListener->onParticipantLeft() was invoked.");

            JSONObject result = new JSONObject();

            try {
                result.put("action", "onParticipantLeft");

                JSONObject data = new JSONObject();

                data.put("uuid", participantLeft.getUUID());
                data.put("groupId", KandyUtils.getJsonObjectFromKandyRecord(participantLeft.getGroupId()));
                data.put("leaver", KandyUtils.getJsonObjectFromKandyRecord(participantLeft.getLeaver()));
                data.put("timestamp", participantLeft.getTimestamp());

                result.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            KandyUtils.sendPluginResultAndKeepCallback(kandyGroupServiceNotificationCallback, result);
        }
    };

    private KandyUserCreditResponseListener kandyUserCreditResponseListener = new KandyUserCreditResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSuccess(IKandyCredit credit) {
            JSONObject result = new JSONObject();

            try {
                result.put("credit", credit.getCredit());
                result.put("currency", credit.getCurrency());
                result.put("dids", credit.getDids());

                JSONArray billingPackages = new JSONArray();

                if (credit.getPackages().size() > 0) {
                    ArrayList<IKandyBillingPackage> billingPackageArrayList = credit.getPackages();
                    for (IKandyBillingPackage billingPackage : billingPackageArrayList)
                        billingPackages.put(KandyUtils.getJsonObjectFromKandyPackagesCredit(billingPackage));
                }
                result.put("packages", billingPackages);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyUserCreditResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyDeviceProfileResponseListener kandyDeviceProfileResponseListener = new KandyDeviceProfileResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSuccess(ArrayList<IKandyDeviceProfile> profiles) {
            JSONArray results = new JSONArray();

            for (IKandyDeviceProfile profile : profiles) {
                JSONObject p = new JSONObject();
                try {
                    p.put("deviceDisplayName", profile.getDeviceDisplayName());
                    p.put("kandyDeviceId", profile.getKandyDeviceId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                results.put(p);
            }

            callbackContext.success(results);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyDeviceProfileResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(KandyUtils.getString("kandy_error_message"), code, error));
        }
    };
}
