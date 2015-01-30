/**
*    Licensed to the Apache Software Foundation (ASF) under one
*    or more contributor license agreements.  See the NOTICE file
*    distributed with this work for additional information
*    regarding copyright ownership.  The ASF licenses this file
*    to you under the Apache License, Version 2.0 (the
*    "License"); you may not use this file except in compliance
*    with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing,
*    software distributed under the License is distributed on an
*    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*    KIND, either express or implied.  See the License for the
*    specific language governing permissions and limitations
*    under the License.
*/

package com.kandy.phonegap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyAddressBookServiceNotificationListener;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsListener;
import com.genband.kandy.api.services.addressbook.KandyEmailContactRecord;
import com.genband.kandy.api.services.addressbook.KandyPhoneContactRecord;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.IKandyOutgoingCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
import com.genband.kandy.api.services.chats.KandyMessage;
import com.genband.kandy.api.services.common.IKandySession;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceServiceNotificationListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kandy Plugin interface for Cordova (PhoneGap).
 *
 * @author Kodeplusdev
 * @version 0.0.1
 */
public class KandyPlugin extends CordovaPlugin {
    private static final String TAG = "KandyPlugin";

    private CordovaWebView _webView;
    private Activity _activity;

    // CallbackContext for current execute
    private CallbackContext _callbackContext;

    // Kandy listeners for call, chat, presence
    private CallbackContext _onIncomingCallListener;
    private CallbackContext _onVideoStateChangedListener;
    private CallbackContext _onAudioStateChangedListener;
    private CallbackContext _onCallStateChangedListener;
    private CallbackContext _onGSMCallIncomingListener;
    private CallbackContext _onGSMCallConnectedListener;
    private CallbackContext _onGSMCallDisconnectedListener;

    private CallbackContext _onChatReceivedListener;
    private CallbackContext _onChatDeliveredListener;
    private CallbackContext _onChatMediaDownloadProgressListener;
    private CallbackContext _onChatMediaDownloadFailedListener;
    private CallbackContext _onChatMediaDownloadSuccededListener;

    private CallbackContext _onPresenceChangedListener;

    private CallbackContext _onDeviceAddressBookChangedListener;

    // The current call
    private IKandyCall _currentCall;

    // Whether or not the call start with sharing video enabled
    private boolean _isCreateVideoCall = true;

    // Whether or not using call dialog (native)
    private boolean _isUsingNativeDialog = true;

    // The call dialog (native)
    private KandyVideoCallDialog _videoCallDialog;

    // The incoming call dialog (native)
    private AlertDialog _incomingCallDialog;

    /**
     * The listeners for callback
     */

    private KandyCallServiceNotificationListener _kandyCallServiceNotificationListener = new KandyCallServiceNotificationListener() {

        /**
         * {@inheritDoc}
         *
         * @param call
         */
        @Override
        public void onIncomingCall(IKandyIncomingCall call) {
            Log.i(TAG, "onIncomingCall: " + call.getCallId());

            if (_isUsingNativeDialog) {
                answerCall(call);
            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("id", call.getCallId());
                    result.put("callee", call.getCallee().getUri());
                    result.put("via", call.getVia());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onIncomingCallListener, result);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param state
         * @param call
         */
        @Override
        public void onCallStateChanged(KandyCallState state, IKandyCall call) {
            Log.i(TAG, "onCallStatusChanged: " + state.name());

            if (_isUsingNativeDialog) {
                if (_incomingCallDialog != null && _incomingCallDialog.isShowing()) {
                    _incomingCallDialog.dismiss();
                    _incomingCallDialog = null;
                }

                if (state == KandyCallState.TERMINATED) {
                    _currentCall = null;
                    _videoCallDialog.dismiss();
                }
            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("id", call.getCallId());
                    result.put("state", state.name());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onCallStateChangedListener, result);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param iKandyCall
         * @param isReceivingVideo
         * @param isSendingVideo
         */
        @Override
        public void onVideoStateChanged(IKandyCall iKandyCall, boolean isReceivingVideo, boolean isSendingVideo) {
            Log.i(TAG, "onVideoStateChanged: Receiving: " + isReceivingVideo + " Sending: " + isSendingVideo);

            if (_isUsingNativeDialog) {

            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("isReceivingVideo", isReceivingVideo);
                    result.put("isSendingVideo", isSendingVideo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onVideoStateChangedListener, result);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param call
         * @param onMute
         */
        @Override
        public void onAudioStateChanged(IKandyCall call, boolean onMute) {
            Log.i(TAG, "onAudioStateChanged to mute? - " + onMute);

            if (_isUsingNativeDialog) {

            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("id", call.getCallId());
                    result.put("isMute", onMute);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onAudioStateChangedListener, result);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param call
         */
        @Override
        public void onGSMCallIncoming(IKandyCall call) {
            Log.i(TAG, "onGSMCallIncoming: " + call.getCallee().getUri());

            if (_isUsingNativeDialog) {
                // TODO: I'm coming...
            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("id", call.getCallId());
                    result.put("callee", call.getCallee().getUri());
                    result.put("via", call.getVia());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onGSMCallIncomingListener, result);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param call
         */
        @Override
        public void onGSMCallConnected(IKandyCall call) {
            Log.i(TAG, "onGSMCallConnected: " + call.getCallee().getUri());

            if (_isUsingNativeDialog) {
                // TODO: I'm coming...
            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("id", call.getCallId());
                    result.put("callee", call.getCallee().getUri());
                    result.put("via", call.getVia());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onGSMCallConnectedListener, result);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param call
         */
        @Override
        public void onGSMCallDisconnected(IKandyCall call) {
            Log.i(TAG, "onGSMCallDisconnected: " + call.getCallee().getUri());

            if (_isUsingNativeDialog) {
                // TODO: I'm coming...
            } else {
                JSONObject result = new JSONObject();

                try {
                    result.put("id", call.getCallId());
                    result.put("callee", call.getCallee().getUri());
                    result.put("via", call.getVia());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyListener(_onGSMCallDisconnectedListener, result);
            }
        }
    };

    private KandyChatServiceNotificationListener _kandyChatServiceNotificationListener = new KandyChatServiceNotificationListener() {

        /**
         * {@inheritDoc}
         *
         * @param message
         */
        @Override
        public void onChatReceived(IKandyMessage message) {
            Log.i(TAG, "onChatReceived: " + message.getUUID());

            notifyListener(_onChatReceivedListener, message.toJson());
        }

        /**
         * {@inheritDoc}
         *
         * @param message
         */
        @Override
        public void onChatDelivered(IKandyMessage message) {
            Log.i(TAG, "onChatDelivered: " + message.getUUID());

            notifyListener(_onChatDeliveredListener, message.toJson());
        }

        /**
         * {@inheritDoc}
         *
         * @param message
         * @param process
         */
        @Override
        public void onChatMediaDownloadProgress(IKandyMessage message, int process) {
            Log.i(TAG, "onChatMediaDownloadProgress(): " + message.getUUID() + " with process: " + process);

            JSONObject result = message.toJson();

            try {
                result.put("process", process);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            notifyListener(_onChatMediaDownloadProgressListener, result);
        }

        /**
         * {@inheritDoc}
         *
         * @param message
         * @param err
         */
        @Override
        public void onChatMediaDownloadFailed(IKandyMessage message, String err) {
            Log.i(TAG, "onChatMediaDownloadFailed(): " + message.getUUID() + ". Error: " + err);

            JSONObject result = message.toJson();
            try {
                result.put("error", err);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            notifyListener(_onChatMediaDownloadFailedListener, result);
        }

        /**
         * {@inheritDoc}
         *
         * @param message
         * @param uri
         */
        @Override
        public void onChatMediaDownloadSucceded(IKandyMessage message, Uri uri) {
            Log.i(TAG, "onChatMediaDownloadSucceded(): " + message.getUUID() + ". Uri: " + uri);

            JSONObject result = message.toJson();
            try {
                result.put("uri", uri);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            notifyListener(_onChatMediaDownloadSuccededListener, result);
        }
    };

    private KandyAddressBookServiceNotificationListener _kandyAddressBookServiceNotificationListener = new KandyAddressBookServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDeviceAddressBookChanged() {
            Log.i(TAG, "onDeviceAddressBookChanged(): true");

            notifyListener(_onDeviceAddressBookChangedListener, new JSONObject());
        }
    };

    private KandyCallResponseListener _kandyCallResponseListener = new KandyCallResponseListener() {

        /**
         * {@inheritDoc}
         *
         * @param iKandyCall
         */
        @Override
        public void onRequestSucceeded(IKandyCall iKandyCall) {
            Log.i(TAG, "onRequestSucceeded: " + iKandyCall.toString());

            _callbackContext.success();
        }

        /**
         * {@inheritDoc}
         *
         * @param iKandyCall
         * @param responseCode
         * @param err
         */
        @Override
        public void onRequestFailed(IKandyCall iKandyCall, int responseCode, String err) {
            Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);

            _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
        }
    };

    private KandyResponseListener _kandyResponseListener = new KandyResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded() {
            Log.i(TAG, "onRequestSucceeded: true");

            _callbackContext.success();
        }

        /**
         * {@inheritDoc}
         *
         * @param responseCode
         * @param err
         */
        @Override
        public void onRequestFailed(int responseCode, String err) {
            Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);

            _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
        }
    };

    private KandyValidationResponseListener _kandyValidationResponseListener = new KandyValidationResponseListener() {

        /**
         * {@inheritDoc}
         *
         * @param iKandyValidationResponse
         */
        @Override
        public void onRequestSuccess(IKandyValidationResponse iKandyValidationResponse) {
            Log.i(TAG, "onRequestSucceeded: " + iKandyValidationResponse.toString());

            _callbackContext.success();
        }

        /**
         * {@inheritDoc}
         *
         * @param responseCode
         * @param err
         */
        @Override
        public void onRequestFailed(int responseCode, String err) {
            Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);

            _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
        }
    };

    private KandyCountryInfoResponseListener _kandyCountryInfoResponseListener = new KandyCountryInfoResponseListener() {

        /**
         * {@inheritDoc}
         *
         * @param response
         */
        @Override
        public void onRequestSuccess(IKandyAreaCode response) {
            Log.i(TAG, "onRequestSucceeded: " + response.toString());

            JSONObject result = new JSONObject();

            try {
                result.put("code", response.getCountryCode());
                result.put("nameLong", response.getCountryNameLong());
                result.put("nameShort", response.getCountryNameShort());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            _callbackContext.success(result);
        }

        /**
         * {@inheritDoc}
         *
         * @param responseCode
         * @param err
         */
        @Override
        public void onRequestFailed(int responseCode, String err) {
            Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);

            _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
        }
    };

    private KandyPresenceServiceNotificationListener _kandyPresenceServiceNotificationListener = new KandyPresenceServiceNotificationListener() {

        /**
         * {@inheritDoc}
         *
         * @param presence
         */
        @Override
        public void onPresenceStateChanged(IKandyPresence presence) {
            Log.i(TAG, "Kandy.getPresenceService().registerNotificationListener: onPresenceStateChanged to " + presence.getState().name());

            JSONObject result = new JSONObject();

            try {
                result.put("user", presence.getContactName());
                result.put("state", presence.getState());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            notifyListener(_onPresenceChangedListener, result);
        }
    };

    /**
     * {@inheritDoc}
     *
     * @param cordova
     * @param webView
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        _activity = cordova.getActivity();
        _webView = webView;

        // Initialize Kandy service
        Kandy.initialize(_activity, _activity.getString(R.string.kandy_api_key),
                _activity.getString(R.string.kandy_api_secret));
    }

    /**
     * {@inheritDoc}
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        // Save current callBack context
        _callbackContext = callbackContext;

        switch (action) {
            /**
             * Setup/Initialize
             */
            case "config/startWithVideo": {
                _isCreateVideoCall = args.getBoolean(0);
                break;
            }
            case "config/useNativeDialog": {
                _isUsingNativeDialog = args.getBoolean(0);
                break;
            }
            case "config/onIncomingCall": {
                _onIncomingCallListener = callbackContext;
                break;
            }
            case "config/onVideoStateChanged": {
                _onVideoStateChangedListener = callbackContext;
                break;
            }
            case "config/onAudioStateChanged": {
                _onAudioStateChangedListener = callbackContext;
                break;
            }
            case "config/onCallStateChanged": {
                _onCallStateChangedListener = callbackContext;
                break;
            }
            case "config/onGSMCallIncoming": {
                _onGSMCallIncomingListener = callbackContext;
            }
            case "config/onGSMCallConnected": {
                _onGSMCallConnectedListener = callbackContext;
            }
            case "config/onGSMCallDisconnected": {
                _onGSMCallDisconnectedListener = callbackContext;
            }
            case "config/onChatReceived": {
                _onChatReceivedListener = callbackContext;
                break;
            }
            case "config/onChatDelivered": {
                _onChatDeliveredListener = callbackContext;
                break;
            }
            case "config/onChatMediaDownloadProgress": {
                _onChatMediaDownloadProgressListener = callbackContext;
                break;
            }
            case "config/onChatMediaDownloadFailed": {
                _onChatMediaDownloadFailedListener = callbackContext;
                break;
            }
            case "config/onChatMediaDownloadSucceded": {
                _onChatMediaDownloadSuccededListener = callbackContext;
                break;
            }
            case "config/onPresenceChanged": {
                _onPresenceChangedListener = callbackContext;
                break;
            }
            case "config/onDeviceAddressBookChanged": {
                _onDeviceAddressBookChangedListener = callbackContext;
                break;
            }
            /**
             * Provisioning service
             */
            case "provisioning/request": {
                String userId = args.getString(0);
                String twoLetterISOCountryCode = args.getString(1);

                Kandy.getProvisioning().requestCode(userId, twoLetterISOCountryCode, _kandyResponseListener);
                break;
            }
            case "provisioning/validate": {
                String userId = args.getString(0);
                String otp = args.getString(1);
                String twoLetterISOCountryCode = args.getString(2);

                Kandy.getProvisioning().validate(userId, otp, twoLetterISOCountryCode, _kandyValidationResponseListener);
                break;
            }
            case "provisioning/deactivate": {
                Kandy.getProvisioning().deactivate(_kandyResponseListener);
                break;
            }
            /**
             * Access service
             */
            case "login": {
                String username = args.getString(0);
                String password = args.getString(1);

                login(username, password);
                break;
            }
            case "logout": {
                logout();
                break;
            }
            case "session": {
                loadSession();
                break;
            }
            /**
             * Call service
             */
            case "call/create": {
                String phoneNumber = args.getString(0);

                startVoiceCall(phoneNumber);
                break;
            }
            case "call/hangup": {
                doHangup();
                break;
            }
            case "call/mute": {
                doMute(true);
                break;
            }
            case "call/unMute": {
                doMute(false);
                break;
            }
            case "call/hold": {
                doHold(true);
                break;
            }
            case "call/unHold": {
                doHold(false);
                break;
            }
            case "call/enableVideo": {
                doVideoCall(true);
                break;
            }
            case "call/disableVideo": {
                doVideoCall(false);
                break;
            }
            case "call/accept": {
                doAccept();
                break;
            }
            case "call/reject": {
                doReject();
                break;
            }
            case "call/ignore": {
                doIgnore();
                break;
            }
            case "call/dialog": {
                JSONObject params = args.getJSONObject(0);

                startVideoCallDialog(params);
                break;
            }
            /**
             * Chat service
             */
            case "chat/send": {
                String user = args.getString(0);
                String message = args.getString(1);

                sendMessage(user, message);
                break;
            }
            case "chat/markAsReceived": {
                Object obj = args.get(0);

                if (obj instanceof JSONArray) {
                    ArrayList<String> uuids = new ArrayList<>();

                    for (int i = 0; i < ((JSONArray) obj).length(); ++i) {
                        uuids.add(((JSONArray) obj).getString(i));
                    }

                    markAsReceived(uuids);
                } else {
                    markAsReceived((String) obj);
                }
                break;
            }
            case "chat/pullEvents": {
                Kandy.getServices().getChatService().pullEvents();
                break;
            }
            /**
             * Presence service
             */
            case "presence": {
                ArrayList<String> list = new ArrayList<String>();

                for (int i = 0; i < args.length(); ++i) {
                    list.add(args.getString(i));
                }

                Kandy.getServices().getPresenceService().startWatch(list, _kandyResponseListener);
                break;
            }
            /**
             * Location service
             */
            case "location": {
                Kandy.getServices().getLocationService().getCountryInfo(_kandyCountryInfoResponseListener);
                break;
            }
            /**
             * Push service
             */
            case "push/enable": {
                Kandy.getServices().getPushService().enablePushNotification(_kandyResponseListener);
                break;
            }
            case "push/disable": {
                Kandy.getServices().getPushService().disablePushNotification(_kandyResponseListener);
                break;
            }
            /**
             * Address book service
             */
            case "addressBook": {
                ArrayList<KandyDeviceContactsFilter> filters = new ArrayList<>();

                for (int i = 0; i < args.length(); ++i){
                    filters.add(KandyDeviceContactsFilter.valueOf(args.getString(i)));
                }

                getDeviceContacts((KandyDeviceContactsFilter[]) filters.toArray());
            }
            default:
                return false;
        }
        return true;
    }

    /**
     * Send result back into JavaScript and keep callBack.
     *
     * @param ctx The callback context used when calling back into JavaScript.
     * @param obj The result.
     */
    private void notifyListener(CallbackContext ctx, JSONObject obj) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
        result.setKeepCallback(true);
        ctx.sendPluginResult(result);
    }

    /**
     * Register/login the user on the server with credentials received from admin.
     *
     * @param username The user's username.
     * @param password The user's password.
     */
    private void login(final String username, final String password) {
        KandyRecord kandyUser;

        try {
            kandyUser = new KandyRecord(username);

        } catch (IllegalArgumentException ex) {
            _callbackContext.error(_activity.getString(R.string.kandy_login_empty_username_text));
            return;
        }

        if (password == null || password.isEmpty()) {
            _callbackContext.error(_activity.getString(R.string.kandy_login_empty_password_text));
            return;
        }

        Kandy.getAccess().login(kandyUser, password, new KandyLoginResponseListener() {

            /**
             * {@inheritDoc}
             *
             * @param responseCode
             * @param err
             */
            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.i(TAG, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: " + responseCode);

                _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onLoginSucceeded() {
                Log.i(TAG, "Kandy.getAccess().login:onLoginSucceeded");

                _callbackContext.success(_activity.getString(R.string.kandy_login_login_success));

                registerKandyListeners();
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
             *
             * @param responseCode
             * @param err
             */
            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.i(TAG, "Kandy.getAccess().logout:onRequestFailed error: " + err + ". Response code: " + responseCode);

                _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onLogoutSucceeded() {
                _callbackContext.success(_activity.getString(R.string.kandy_login_logout_success));

                unregisterKandyListeners();
            }
        });
    }

    /**
     * Register listeners to receive events from Kandy background service.
     */
    private void registerKandyListeners() {
        Kandy.getServices().getCallService().registerNotificationListener(_kandyCallServiceNotificationListener);
        Kandy.getServices().getChatService().registerNotificationListener(_kandyChatServiceNotificationListener);
        Kandy.getServices().getPresenceService().registerNotificationListener(_kandyPresenceServiceNotificationListener);
        Kandy.getServices().getAddressBookService().registerNotificationListener(_kandyAddressBookServiceNotificationListener);
    }

    /**
     * Unregister listeners out of Kandy background service.
     */
    private void unregisterKandyListeners() {
        Kandy.getServices().getCallService().unregisterNotificationListener(_kandyCallServiceNotificationListener);
        Kandy.getServices().getChatService().unregisterNotificationListener(_kandyChatServiceNotificationListener);
        Kandy.getServices().getPresenceService().unregisterNotificationListener(_kandyPresenceServiceNotificationListener);
        Kandy.getServices().getAddressBookService().unregisterNotificationListener(_kandyAddressBookServiceNotificationListener);
    }

    /**
     * Load previous session.
     *
     * @throws JSONException
     */
    private void loadSession() throws JSONException {
        IKandySession session = Kandy.getSession();

        if (session.getKandyUser().getUser() != null) {
            JSONObject p = new JSONObject();

            p.put("userId", session.getKandyUser().getUserId());
            p.put("user", session.getKandyUser().getUser());
            p.put("domain", session.getKandyDomain().getName());
            p.put("deviceId", session.getKandyUser().getDeviceId());

            _callbackContext.success(p);
        } else {
            _callbackContext.error("Session not found.");
        }
    }

    /**
     * Answer a coming call (use native dialog alert).
     *
     * @param pCall The coming call.
     */
    private void answerCall(final IKandyIncomingCall pCall) {
        _activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                createIncomingCallPopup(pCall);
            }
        });
    }

    /**
     * Create a native dialog alert on UI thread.
     *
     * @param pInCall
     */
    private void createIncomingCallPopup(IKandyIncomingCall pInCall) {
        _currentCall = pInCall;

            /*if(isFinishing()) {
            //FIXME remove this after fix twice callback call
                  Log.i(TAG, "createIncomingCallPopup is finishing()");
                  return;
            }*/

        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        builder.setPositiveButton(_activity.getString(R.string.kandy_calls_answer_button_label), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                createVideoCallDialog(_currentCall);
                doAccept();
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(_activity.getString(R.string.kandy_calls_ignore_incoming_call_button_label), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ignoreIncomingCall((IKandyIncomingCall) _currentCall);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(_activity.getString(R.string.kandy_calls_reject_incoming_call_button_label), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectIncomingCall((IKandyIncomingCall) _currentCall);
                dialog.dismiss();
            }
        });

        builder.setMessage(_activity.getString(R.string.kandy_calls_incoming_call_popup_message_label) + _currentCall.getCallee().getUri());

        _incomingCallDialog = builder.create();
        _incomingCallDialog.show();
    }

    /**
     * Reject current coming call.
     *
     * @param pCall The coming call.
     */
    private void rejectIncomingCall(IKandyIncomingCall pCall) {
        if (pCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        pCall.reject(_kandyCallResponseListener);
    }

    /**
     * Ignore current coming call.
     *
     * @param pCall The coming call.
     */
    private void ignoreIncomingCall(IKandyIncomingCall pCall) {
        if (pCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        pCall.ignore(_kandyCallResponseListener);
    }

    /**
     * Create a native call dialog on UI thread.
     *
     * @param call The current call.
     */
    private void createVideoCallDialog(final IKandyCall call) {
        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_videoCallDialog == null) {
                    _videoCallDialog = new KandyVideoCallDialog(_activity);
                }

                // Set title
                String callee = call.getCallee().getUserName() + call.getCallee().getDomain();
                _videoCallDialog.setTitle(callee);

                // Set current call
                _videoCallDialog.setKandyCall(_currentCall);
                doVideoCall(_isCreateVideoCall);

                // Set callBack
                _videoCallDialog.setKandyCallbackContext(_callbackContext);

                _videoCallDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        ((IKandyOutgoingCall) _currentCall).establish(_kandyCallResponseListener);
                    }
                });

                _videoCallDialog.setKandyVideoCallListener(new KandyVideoCallDialog.KandyVideoCallDialogListener() {
                    @Override
                    public void hangup() {
                        KandyPlugin.this.doHangup();
                    }

                    @Override
                    public void doMute(boolean enable) {
                        KandyPlugin.this.doMute(enable);
                    }

                    @Override
                    public void doHold(boolean enable) {
                        KandyPlugin.this.doHold(enable);
                    }

                    @Override
                    public void doVideo(boolean enable) {
                        KandyPlugin.this.doVideoCall(enable);
                    }
                });
                _videoCallDialog.show();
            }
        });
    }

    /**
     * Create a native call dialog.
     *
     * @param params The call parameters.
     * @throws JSONException
     */
    private void startVideoCallDialog(JSONObject params) throws JSONException {
        final String phoneNumber = params.getString("phoneNumber");
        boolean startWithVideo = params.getBoolean("startWithVideo");

        KandyRecord callee;
        try {
            callee = new KandyRecord(phoneNumber);
        } catch (IllegalArgumentException ex) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_phone_text_msg));
            return;
        }

        _currentCall = Kandy.getServices().getCallService().createVoipCall(callee, startWithVideo);

        createVideoCallDialog(_currentCall);
    }

    /**
     * Start a voice call.
     *
     * @param phoneNumber The callee's number.
     */
    private void startVoiceCall(String phoneNumber) {

        KandyRecord callee;
        try {
            callee = new KandyRecord(phoneNumber);
        } catch (IllegalArgumentException ex) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_phone_text_msg));
            return;
        }

        _currentCall = Kandy.getServices().getCallService().createVoipCall(callee, false/* voice call */);
        // FIXME: setVideoView
        KandyView dummyVideoView = new KandyView(_activity, null);
        _currentCall.setLocalVideoView(dummyVideoView);
        _currentCall.setRemoteVideoView(dummyVideoView);

        ((IKandyOutgoingCall) _currentCall).establish(_kandyCallResponseListener);
    }

    /**
     * Hangup current call.
     */
    private void doHangup() {

        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }
        _currentCall.hangup(_kandyCallResponseListener);
    }

    /**
     * Mute/Unmute current call.
     *
     * @param mute The state of current audio call.
     */
    private void doMute(boolean mute) {
        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        if (!mute && _currentCall.isMute()) {
            _currentCall.mute(_kandyCallResponseListener);
        } else if (mute && !_currentCall.isMute()) {
            _currentCall.unmute(_kandyCallResponseListener);
        }
    }

    /**
     * Hold/unhold current call.
     *
     * @param hold The state of current call.
     */
    private void doHold(boolean hold) {
        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        if (!hold && _currentCall.isOnHold()) {
            _currentCall.hold(_kandyCallResponseListener);
        } else if (hold && !_currentCall.isOnHold()) {
            _currentCall.unhold(_kandyCallResponseListener);
        }
    }

    /**
     * Accept a coming call.
     */
    private void doAccept() {

        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        ((IKandyIncomingCall) _currentCall).accept(_isCreateVideoCall, _kandyCallResponseListener);
    }

    /**
     * Reject a coming call.
     */
    private void doReject() {

        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        ((IKandyIncomingCall) _currentCall).reject(_kandyCallResponseListener);
    }

    /**
     * Ignore a coming call.
     */
    private void doIgnore() {

        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        ((IKandyIncomingCall) _currentCall).ignore(_kandyCallResponseListener);
    }

    /**
     * Whether or not The sharing video is enabled.
     *
     * @param video The state of current video call.
     */
    private void doVideoCall(boolean video) {
        if (_currentCall == null) {
            _callbackContext.error(_activity.getString(R.string.kandy_calls_invalid_hangup_text_msg));
            return;
        }

        if (!video && _currentCall.isSendingVideo()) {
            _currentCall.stopVideoSharing(_kandyCallResponseListener);
        } else if (video && !_currentCall.isSendingVideo()) {
            _currentCall.startVideoSharing(_kandyCallResponseListener);
        }
    }

    /**
     * Send a message to the recipient.
     *
     * @param user The recipient.
     * @param text The message text
     */
    private void sendMessage(String user, String text) {
        //Set the recipient
        KandyRecord recipient;

        try {
            recipient = new KandyRecord(user);
        } catch (IllegalArgumentException ex) {
            _callbackContext.error(_activity.getString(R.string.kandy_chat_phone_number_verification_text));
            return;
        }

        //creating message to be sent
        final KandyMessage message = new KandyMessage(recipient, text);
        //Sending message
        Kandy.getServices().getChatService().send(message, _kandyResponseListener);
    }

    /**
     * Mark message as read.
     *
     * @param uuid The uuid of the message.
     */
    private void markAsReceived(String uuid) {
        // FIXME
        KandyMessage event = new KandyMessage(new KandyRecord("dummy@domain.com"), "dummy");
        event.setUUID(UUID.fromString(uuid));
        Kandy.getServices().getChatService().markAsReceived(event, _kandyResponseListener);
    }

    /**
     * Mark messages as read
     *
     * @param uuids The uuid list of the messages.
     */
    private void markAsReceived(ArrayList<String> uuids) {
        for (String uuid : uuids) {
            markAsReceived(uuid);
        }
    }

    /**
     * Get local contacts from device
     *
     * @param filters The filters
     */
    private void getDeviceContacts(KandyDeviceContactsFilter[] filters) {
        Kandy.getServices().getAddressBookService().getDeviceContacts(filters, new KandyDeviceContactsListener() {

            /**
             * {@inheritDoc}
             *
             * @param contacts
             */
            @Override
            public void onRequestSucceded(List<IKandyContact> contacts) {
                Log.i(TAG, "onRequestSucceded(): " + contacts.size() + " contact(s)");

                JSONObject result = new JSONObject();

                try {
                    result.put("size", contacts.size());

                    JSONArray contactsArray = new JSONArray();
                    for (IKandyContact contact : contacts) {
                        JSONObject c = new JSONObject();

                        // Get display name
                        c.put("displayName", contact.getDisplayName());

                        // Get emails
                        JSONArray emails = new JSONArray();
                        for (KandyEmailContactRecord email : contact.getEmails()) {
                            JSONObject e = new JSONObject();
                            e.put("address", email.getAddress());
                            e.put("type", email.getType().name());
                            emails.put(e);
                        }
                        c.put("emails", emails);

                        // Get number phones
                        JSONArray phones = new JSONArray();
                        for (KandyPhoneContactRecord phone: contact.getNumbers()){
                            JSONObject p = new JSONObject();
                            p.put("number", phone.getNumber());
                            p.put("type", phone.getType().name());
                            phones.put(p);
                        }
                        c.put("phones", phones);

                        contactsArray.put(c);

                    }

                    result.put("contacts", contactsArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                _callbackContext.success(result);
            }

            /**
             * {@inheritDoc}
             *
             * @param responseCode
             * @param err
             */
            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);

                _callbackContext.error(String.format(_activity.getString(R.string.kandy_error_message), responseCode, err));
            }
        });
    }
}