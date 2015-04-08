package com.kandy.phonegap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.addressbook.*;
import com.genband.kandy.api.services.calls.*;
import com.genband.kandy.api.services.chats.*;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.groups.*;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.google.android.gcm.GCMRegistrar;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kandy Plugin interface for Cordova (PhoneGap).
 *
 * @author kodeplusdev
 * @version 1.0.0
 */
public class KandyPlugin extends CordovaPlugin {

    private static final String LCAT = KandyPlugin.class.getSimpleName();

    private CordovaWebView webView;
    private Activity activity;
    private SharedPreferences prefs;
    private KandyUtils utils;

    private IKandyCall currentCall;
    private KandyCallDialog callDialog;
    private AlertDialog incomingCallDialog;

    private boolean startWithVideo = false;

    /** The {@link CallbackContext} for Kandy listeners **/
    private CallbackContext kandyConnectServiceNotificationCallback;
    private CallbackContext kandyCallServiceNotificationCallback;
    private CallbackContext kandyAddressBookServiceNotificationCallback;
    private CallbackContext kandyChatServiceNotificationCallback;
    private CallbackContext kandyGroupServiceNotificationCallback;

    private CallbackContext kandyChatServiceNotificationPluginCallback;

    /** The {@link CallbackContext} for current action **/
    private CallbackContext callbackContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.webView = webView;
        activity = cordova.getActivity();
        utils = KandyUtils.getInstance(activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        // Initialize Kandy SDK
        Kandy.initialize(activity, // TODO: user can change Kandy API keys
                prefs.getString(KandyConstant.API_KEY_PREFS_KEY, utils.getString("kandy_api_key")),
                prefs.getString(KandyConstant.API_SECRET_PREFS_KEY, utils.getString("kandy_api_secret")));

        IKandyGlobalSettings settings = Kandy.getGlobalSettings();
        settings.setKandyHostURL(prefs.getString(KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));

        applyKandyChatSettings();
    }

    /**
     * Applies the {@link KandyChatSettings} with user defined settings or default if not set by developer.
     */
    private void applyKandyChatSettings() {
        // TODO: not complete yet
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        registerNotificationListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        unregisterNotificationListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext ctx) throws JSONException {

        /** Save current {@link callbackContext} to use**/
        callbackContext = ctx;

        switch (action){
            //***** REGISTRATION LISTENERS *****//
            case "connectServiceNotificationCallback":
                kandyConnectServiceNotificationCallback = ctx;
                break;
            case "callServiceNotificationCallback":
                kandyCallServiceNotificationCallback = ctx;
                break;
            case "addressBookServiceNotificationCallback":
                kandyAddressBookServiceNotificationCallback = ctx;
                break;
            case "chatServiceNotificationCallback":
                kandyChatServiceNotificationCallback = ctx;
                break;
            case "groupServiceNotificationCallback":
                kandyGroupServiceNotificationCallback = ctx;
                break;
            //***** PLUGIN LISTENERS *****//
            case "chatServiceNotificationPluginCallback":
                kandyChatServiceNotificationPluginCallback = ctx;
                break;
            //***** PROVISIONING SERVICE *****//
            case "provisioning:request": {
                String userId = args.getString(0);
                String twoLetterISOCountryCode = args.getString(1);

                Kandy.getProvisioning().requestCode(userId, twoLetterISOCountryCode, kandyResponseListener);
                break;
            }
            case "provisioning:validate": {
                String userId = args.getString(0);
                String otp = args.getString(1);
                String twoLetterISOCountryCode = args.getString(2);

                Kandy.getProvisioning().validate(userId, otp, twoLetterISOCountryCode, kandyValidationResponseListener);
                break;
            }
            case "provisioning:deactivate":
                Kandy.getProvisioning().deactivate(kandyResponseListener);
                break;
            //***** ACCESS *****//
            case "login": {
                String username = args.getString(0);
                String password = args.getString(1);

                login(username, password);
                break;
            }
            case "logout":
                logout();
                break;
            case "getConnectionState": {
                String state = Kandy.getAccess().getConnectionState().toString();
                callbackContext.success(state);
                break;
            }
            case "getSession":
                getSession();
                break;
            //***** CALL SERVICE *****//
            case "call:createVoipCall": {
                String username = args.getString(0);
                startWithVideo = args.getInt(1) == 1;
                createVoipCall(username);
                break;
            }
            case "call:createPSTNCall": {
                String number = args.getString(0);
                createPSTNCall(number);
                break;
            }
            case "call:hangup":
                doHangup();
                break;
            case "call:mute":
                switchMuteState(true);
                break;
            case "call:unmute":
                switchMuteState(false);
                break;
            case "call:hold":
                switchHoldState(true);
                break;
            case "call:unhold":
                switchHoldState(false);
                break;
            case "call:enableVideo":
                switchVideoCallState(true);
                break;
            case "call:disableVideo":
                switchVideoCallState(false);
                break;
            case "call:accept":
                doAccept();
                break;
            case "call:reject":
                doReject();
                break;
            case "call:ignore":
                doIgnore();
                break;
            //***** CHAT SERVICE *****//
            case "chat:sendSMS": {
                String destination = args.getString(0);
                String text = args.getString(1);

                sendSMS(destination, text);
                break;
            }
            case "chat:sendChat": {
                String destination = args.getString(0);
                String text = args.getString(1);

                sendChat(destination, text);
                break;
            }
            case "chat:markAsReceived": {
                String uuid = args.getString(0);
                markAsReceived(uuid);
                break;
            }
            case "chat:pullEvents":
                Kandy.getServices().getChatService().pullEvents(kandyResponseListener);
                break;
            //***** GROUP SERVICE *****//
            //***** PRESENCE SERVICE *****//
            case "presence":
                retrievePresence(args);
                break;
            //***** LOCATION SERVICE *****//
            case "location:getCountryInfo":
                Kandy.getServices().getLocationService().getCountryInfo(kandyCountryInfoResponseListener);
                break;
            case "location:getCurrentLocation":
                try {
                    Kandy.getServices().getLocationService().getCurrentLocation(kandyCurrentLocationListener);
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                    callbackContext.error("Invalid method");
                }
                break;
            //***** PUSH SERVICE *****//
            case "push:enable":
                enablePushNotification();
                break;
            case "push:disable":
                disablePushNotification();
                break;
            //***** ADDRESS BOOK SERVICE *****//
            case "getDeviceContacts":
                //TODO: user can set filters
                KandyDeviceContactsFilter[] filters = new KandyDeviceContactsFilter[]{ KandyDeviceContactsFilter.ALL };
                Kandy.getServices().getAddressBookService().getDeviceContacts(filters, kandyDeviceContactsListener);
                break;
            case "getDomainContacts":
                Kandy.getServices().getAddressBookService().getDomainDirectoryContacts(kandyDeviceContactsListener);
                break;
            default:
                return super.execute(action, args, ctx); // return false
        }

        return true;
    }

    /**
     * Register listeners to receive events from Kandy background service.
     */
    private void registerNotificationListener(){
        Log.d(LCAT, "registerNotificationListener() was invoked");
        Kandy.getAccess().registerNotificationListener(kandyConnectServiceNotificationListener);
        Kandy.getServices().getCallService().registerNotificationListener(kandyCallServiceNotificationListener);
        Kandy.getServices().getChatService().registerNotificationListener(kandyChatServiceNotificationListener);
        Kandy.getServices().getAddressBookService().registerNotificationListener(kandyAddressBookServiceNotificationListener);
        Kandy.getServices().getGroupService().registerNotificationListener(kandyGroupServiceNotificationListener);
    }

    /**
     * Unregister listeners out of Kandy background service.
     */
    private void unregisterNotificationListener(){
        Log.d(LCAT, "unregisterNotificationListener() was invoked");
        Kandy.getAccess().unregisterNotificationListener(kandyConnectServiceNotificationListener);
        Kandy.getServices().getCallService().unregisterNotificationListener(kandyCallServiceNotificationListener);
        Kandy.getServices().getChatService().unregisterNotificationListener(kandyChatServiceNotificationListener);
        Kandy.getServices().getAddressBookService().unregisterNotificationListener(kandyAddressBookServiceNotificationListener);
        Kandy.getServices().getGroupService().unregisterNotificationListener(kandyGroupServiceNotificationListener);
    }

    /**
     * Register/login the user on the server with credentials received from admin.
     *
     * @param username The username to use.
     * @param password The password to use.
     */
    private void login(String username, String password){
        KandyRecord kandyUser ;

        try {
            kandyUser = new KandyRecord(username);

        } catch (KandyIllegalArgumentException ex) {
            ex.printStackTrace();
            callbackContext.error(utils.getString("kandy_login_empty_username_text"));
            return;
        }

        if(password == null || password.isEmpty()) {
            callbackContext.error(utils.getString("kandy_login_empty_password_text"));
            return ;
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
                callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
            }
        });
    }

    /**
     * This method unregisters user from the Kandy server.
     */
    private void logout(){
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
                callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
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
        user.put("id", Kandy.getSession().getKandyUser().getUserId());
        user.put("name", Kandy.getSession().getKandyUser().getUser());
        user.put("deviceId", Kandy.getSession().getKandyUser().getDeviceId());
        //user.put("password", Kandy.getSession().getKandyUser().getPassword()); // FIXME: security?

        obj.put("domain", domain);
        obj.put("user", user);

        callbackContext.success(obj);
    }

    /**
     * Create a voip call.
     *
     * @param username The username of the callee.
     */
    private void createVoipCall(String username){
        KandyRecord callee;
        try {
            callee = new KandyRecord(username);
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            callbackContext.error(utils.getString("kandy_calls_invalid_phone_text_msg"));
            return;
        }
        currentCall = Kandy.getServices().getCallService().createVoipCall(callee, startWithVideo);
        createCallDialogForCurrentCall();
    }

    /**
     * Create a PSTN call.
     *
     * @param number The number phone of the callee.
     */
    private void createPSTNCall(String number){
        currentCall = Kandy.getServices().getCallService().createPSTNCall(number);
        createCallDialogForCurrentCall();
    }

    /**
     * Create a call {@link AlertDialog}
     */
    private void createCallDialogForCurrentCall(){
        if (currentCall == null){
            callbackContext.error("Invalid number");
            return;
        }

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (callDialog == null) {
                    callDialog = new KandyCallDialog(activity);
                }

                // Setup dialog
                String callee = currentCall.getCallee().getUserName() + currentCall.getCallee().getDomain();
                callDialog.setTitle(callee);
                callDialog.setKandyCall(currentCall);

                switchVideoCallState(startWithVideo);

                callDialog.setKandyCallbackContext(callbackContext);

                callDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        ((IKandyOutgoingCall) currentCall).establish(kandyCallResponseListener);
                    }
                });

                callDialog.setKandyVideoCallListener(new KandyCallDialog.KandyVideoCallDialogListener() {

                    @Override
                    public void hangup() {
                        KandyPlugin.this.doHangup();
                    }

                    @Override
                    public void switchMuteState(boolean state) {
                        KandyPlugin.this.switchMuteState(state);
                    }

                    @Override
                    public void switchHoldState(boolean state) {
                        KandyPlugin.this.switchHoldState(state);
                    }

                    @Override
                    public void switchVideoSharingState(boolean state) {
                        KandyPlugin.this.switchVideoCallState(state);
                    }
                });

                callDialog.show();
            }
        });
    }

    /**
     * Answer a coming call (use native {@link AlertDialog}).
     *
     * @param call The current {@link IKandyIncomingCall} to use.
     */
    private void answerIncomingCall(final IKandyIncomingCall call) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                createIncomingCallPopup(call);
            }
        });
    }

    /**
     * Create a native dialog alert on UI thread.
     *
     * @param call The current {@link IKandyIncomingCall} to use.
     */
    private void createIncomingCallPopup(IKandyIncomingCall call) {
        currentCall = call;

        /*if(isFinishing()) {
        //FIXME remove this after fix twice callback call
              Log.i(LCAT, "createIncomingCallPopup is finishing()");
              return;
        }*/

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setPositiveButton(utils.getString("kandy_calls_answer_button_label"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                createCallDialogForCurrentCall();
                doAccept();
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(utils.getString("kandy_calls_ignore_incoming_call_button_label"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ignoreIncomingCall((IKandyIncomingCall) currentCall);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(utils.getString("kandy_calls_reject_incoming_call_button_label"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectIncomingCall((IKandyIncomingCall) currentCall);
                dialog.dismiss();
            }
        });

        builder.setMessage(utils.getString("kandy_calls_incoming_call_popup_message_label") + currentCall.getCallee().getUri());

        incomingCallDialog = builder.create();

        incomingCallDialog.show();
    }

    /**
     * Reject current coming call.
     *
     * @param call The current {@link IKandyIncomingCall} to use.
     */
    private void rejectIncomingCall(IKandyIncomingCall call) {
        if (call == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        call.reject(kandyCallResponseListener);
    }

    /**
     * Ignore current coming call.
     *
     * @param call The current {@link IKandyIncomingCall} to use.
     */
    private void ignoreIncomingCall(IKandyIncomingCall call) {
        if (call == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        call.ignore(kandyCallResponseListener);
    }

    /**
     * Hangup current call.
     */
    private void doHangup() {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }
        currentCall.hangup(kandyCallResponseListener);
    }

    /**
     * Mute/Unmute current call.
     *
     * @param mute The mute state.
     */
    private void switchMuteState(boolean mute) {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        if (!mute && currentCall.isMute()) {
            currentCall.mute(kandyCallResponseListener);
        } else if (mute && !currentCall.isMute()) {
            currentCall.unmute(kandyCallResponseListener);
        }
    }

    /**
     * Hold/Unhole current call.
     *
     * @param hold The hold state.
     */
    private void switchHoldState(boolean hold) {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        if (!hold && currentCall.isOnHold()) {
            currentCall.hold(kandyCallResponseListener);
        } else if (hold && !currentCall.isOnHold()) {
            currentCall.unhold(kandyCallResponseListener);
        }
    }

    /**
     * Whether or not The sharing video is enabled.
     *
     * @param video The state video sharing.
     */
    private void switchVideoCallState(boolean video) {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        if (!video && currentCall.isSendingVideo()) {
            currentCall.stopVideoSharing(kandyCallResponseListener);
        } else if (video && !currentCall.isSendingVideo()) {
            currentCall.startVideoSharing(kandyCallResponseListener);
        }
    }

    /**
     * Accept a coming call.
     */
    private void doAccept() {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        ((IKandyIncomingCall) currentCall).accept(startWithVideo, kandyCallResponseListener);
    }

    /**
     * Reject a coming call.
     */
    private void doReject() {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        ((IKandyIncomingCall) currentCall).reject(kandyCallResponseListener);
    }

    /**
     * Ignore a coming call.
     */
    private void doIgnore() {
        if (currentCall == null) {
            callbackContext.error(utils.getString("kandy_calls_invalid_hangup_text_msg"));
            return;
        }

        ((IKandyIncomingCall) currentCall).ignore(kandyCallResponseListener);
    }

    /**
     * Send text chat message.
     *
     * @param destination The recipient user.
     * @param text The message to send.
     */
    private void sendChat(String destination, String text){
        // Set the recipient
        KandyRecord recipient = null;
        try {
            recipient = new KandyRecord(destination);
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
        }

        if(recipient == null) {
            callbackContext.error(utils.getString("kandy_chat_phone_number_verification_text"));
            return;
        }

        // creating message to be sent
        final KandyChatMessage message = new KandyChatMessage(recipient, text);
        // Sending message
        Kandy.getServices().getChatService().sendChat(message, kandyResponseListener);
    }

    /**
     * Send text SMS message.
     *
     * @param destination The recipient user.
     * @param text The message to send.
     */
    private void sendSMS(String destination, String text) {
        if(text == null || text.equals("")) {
            callbackContext.error(utils.getString("kandy_chat_message_invalid_message"));
            return;
        }

        final KandySMSMessage message;

        try {
            message = new KandySMSMessage(destination, "Kandy SMS", text);
        } catch (KandyIllegalArgumentException e) {
            callbackContext.error(utils.getString("kandy_chat_message_invalid_phone"));
            Log.e(LCAT, "sendSMS: " + " " + e.getLocalizedMessage(), e);
            return;
        }

        // Sending message
        Kandy.getServices().getChatService().sendSMS(message, kandyResponseListener);
    }

    /**
     * Mark message as received.
     *
     * @param uuid The UUID of the message.
     */
    private void markAsReceived(String uuid){
        try {
            KandyChatMessage message = new KandyChatMessage(new KandyRecord("dummy@dummy.com"), "dummy"); // FIXME: dummy?
            message.setUUID(UUID.fromString(uuid));
            message.markAsReceived(kandyResponseListener);
        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Try to get the state presence of users.
     *
     * @param args The {@link JSONArray} users array.
     * @throws JSONException
     */
    private void retrievePresence(JSONArray args) throws JSONException {
        ArrayList<KandyRecord> list = new ArrayList<>();

        for (int i = 0; i < args.length(); ++i){
            try {
                list.add(new KandyRecord(args.getString(i)));
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        Kandy.getServices().getPresenceService().retrievePresence(list, kandyPresenceResponseListener);
    }

    /**
     * Enable Push Notification service.
     */
    private void enablePushNotification(){
        GCMRegistrar.checkDevice(activity);
        GCMRegistrar.checkManifest(activity);
        String registrationId = GCMRegistrar.getRegistrationId(activity);

        if (TextUtils.isEmpty(registrationId))
        {
            GCMRegistrar.register(activity, KandyConstant.GCM_PROJECT_ID);
        }

        Kandy.getServices().getPushService().enablePushNotification(registrationId, kandyResponseListener);
    }

    /**
     * Disable Push Notification service.
     */
    private void disablePushNotification(){
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

        if (contact.getNumbers() != null){
            for (KandyPhoneContactRecord phone: contact.getNumbers()){
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

        if (contact.getEmails() != null){
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

            JSONObject result = new JSONObject();

            try {
                result.put("id", call.getCallId());
                result.put("uri", call.getCallee().getUri());
                result.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callbackContext.success();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(IKandyCall call, int code, String error) {
            Log.d(LCAT, "KandyCallResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
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
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyValidationResponseListener kandyValidationResponseListener = new KandyValidationResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSuccess(IKandyValidationResponse iKandyValidationResponse) {
            Log.d(LCAT, "KandyValidationResponseListener->onRequestSucceeded() was invoked.");
            callbackContext.success();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestFailed(int code, String error) {
            Log.d(LCAT, "KandyValidationResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - " + error);
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
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
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
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
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyPresenceResponseListener kandyPresenceResponseListener = new KandyPresenceResponseListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceed(ArrayList<IKandyPresence> onlines, ArrayList<KandyRecord> offlines) {
            Log.d(LCAT, "KandyPresenceResponseListener->onRequestSucceeded() was invoked: onlines: " + onlines.size() + " and offlines: " + offlines.size());

            JSONObject result = new JSONObject();

            try {
                JSONArray onlinesList = new JSONArray();
                for (IKandyPresence online : onlines){
                    JSONObject obj = new JSONObject();
                    obj.put("user", online.getUser().getUri());
                    obj.put("lastSeenDate", online.getLastSeenDate().toString());
                    onlinesList.put(obj);
                }
                result.put("onlines", onlinesList);

                JSONArray offlinesList = new JSONArray();
                for (KandyRecord offline : offlines){
                    offlinesList.put(offline.getUri());
                }
                result.put("offlines", offlinesList);

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
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyDeviceContactsListener kandyDeviceContactsListener = new KandyDeviceContactsListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onRequestSucceded(List<IKandyContact> list) {
            Log.d(LCAT, "KandyDeviceContactsListener->onRequestSucceeded() was invoked: " + list.size());

            JSONArray result = new JSONArray();

            try {
                for (IKandyContact contact : list) {
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
            callbackContext.error(String.format(utils.getString("kandy_error_message"), code, error));
        }
    };

    private KandyConnectServiceNotificationListener kandyConnectServiceNotificationListener = new KandyConnectServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onConnectionStateChanged(KandyConnectionState state) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onConnectionStateChanged() was invoked: " + state.toString());

            JSONObject obj = new JSONObject();

            try {
                obj.put("method", "onConnectionStateChanged");
                obj.put("state", state.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketConnected() {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketConnected() was invoked.");

            JSONObject obj = new JSONObject();
            try {
                obj.put("action", "onSocketConnected");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketConnecting() {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketConnecting() was invoked.");

            JSONObject obj = new JSONObject();
            try {
                obj.put("action", "onSocketConnecting");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketDisconnected() {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketDisconnected() was invoked.");

            JSONObject obj = new JSONObject();
            try {
                obj.put("action", "onSocketDisconnected");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSocketFailedWithError(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSocketFailedWithError() was invoked: " + error);

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onSocketFailedWithError");
                obj.put("error", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onInvalidUser(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onInvalidUser() was invoked: " + error);

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onInvalidUser");
                obj.put("error", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSessionExpired(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSessionExpired() was invoked: " + error);

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onSessionExpired");
                obj.put("error", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSDKNotSupported(String error) {
            Log.d(LCAT, "KandyConnectServiceNotificationListener->onSDKNotSupported() was invoked: " + error);

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onSDKNotSupported");
                obj.put("error", error);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyConnectServiceNotificationCallback, obj);
        }
    };

    private KandyCallServiceNotificationListener kandyCallServiceNotificationListener = new KandyCallServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onIncomingCall(IKandyIncomingCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onIncomingCall() was invoked: " + call.getCallId());

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onIncomingCall");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);

            answerIncomingCall(call);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onMissedCall(KandyMissedCallMessage call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onMissedCall() was invoked: " + call.toString());

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onMissedCall");
                obj.put("uri", call.getSource().getUri());
                obj.put("uuid", call.getUUID());
                obj.put("timestamp", call.getTimestamp());
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCallStateChanged(KandyCallState state, IKandyCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onCallStateChanged() was invoked: " + call.getCallId() + "and state: " + state.toString());

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onCallStateChanged");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("state", state.toString());
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);

            if (incomingCallDialog != null && incomingCallDialog.isShowing()) {
                incomingCallDialog.dismiss();
                incomingCallDialog = null;
            }

            if (state == KandyCallState.TERMINATED) {
                currentCall = null;
                callDialog.dismiss();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onVideoStateChanged(IKandyCall call, boolean receiving, boolean sending) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onVideoStateChanged() was invoked: "
                    + call.getCallId() + " and receiving state: " + receiving + " and sending state: " + sending);

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onVideoStateChanged");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("receiving", receiving);
                obj.put("sending", sending);
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAudioStateChanged(IKandyCall call, boolean state) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onAudioStateChanged() was invoked: " + call.getCallId() + " and audio state: " + state);

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onAudioStateChanged");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("state", state);
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGSMCallIncoming(IKandyCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onGSMCallIncoming() was invoked: " + call.getCallId());

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onGSMCallIncoming");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGSMCallConnected(IKandyCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onGSMCallConnected() was invoked: " + call.getCallId());

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onGSMCallConnected");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);

            switchHoldState(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGSMCallDisconnected(IKandyCall call) {
            Log.d(LCAT, "KandyCallServiceNotificationListener->onGSMCallDisconnected() was invoked: " + call.getCallId());

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onGSMCallDisconnected");
                obj.put("uri", call.getCallee().getUri());
                obj.put("id", call.getCallId());
                obj.put("via", call.getVia());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyCallServiceNotificationCallback, obj);

            switchHoldState(false);
        }
    };

    private KandyChatServiceNotificationListener kandyChatServiceNotificationListener = new KandyChatServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatReceived(IKandyMessage message, KandyRecordType type) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatReceived() was invoked.");

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onChatReceived");
                obj.put("type", type.toString());
                obj.put("data", message.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, obj);
            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatDelivered(KandyDeliveryAck ack) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatDelivered() was invoked.");

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onChatDelivered");
                obj.put("data", ack.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, obj);
            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatMediaAutoDownloadProgress(IKandyMessage message, IKandyTransferProgress process) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadProgress() was invoked.");

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onChatMediaAutoDownloadProgress");
                obj.put("process", process.getProgress());
                obj.put("state", process.getState());
                obj.put("byteTransfer", process.getByteTransfer());
                obj.put("byteExpected", process.getByteExpected());
                obj.put("data", message.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, obj);
            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatMediaAutoDownloadFailed(IKandyMessage message, int code, String error) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadFailed() was invoked.");

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onChatMediaAutoDownloadFailed");
                obj.put("error", error);
                obj.put("code", code);
                obj.put("data", message.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, obj);
            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri uri) {
            Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadSucceded() was invoked.");

            JSONObject obj = new JSONObject();

            try {
                obj.put("action", "onChatMediaAutoDownloadSucceded");
                obj.put("uri", uri);
                obj.put("data", message.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationCallback, obj);
            utils.sendPluginResultAndKeepCallback(kandyChatServiceNotificationPluginCallback, obj);
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
            utils.sendPluginResultAndKeepCallback(kandyAddressBookServiceNotificationCallback);
        }
    };

    private KandyGroupServiceNotificationListener kandyGroupServiceNotificationListener = new KandyGroupServiceNotificationListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGroupDestroyed(IKandyGroupDestroyed group) {
            // TODO: not complete yet
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGroupUpdated(IKandyGroupUpdated group) {
            // TODO: not complete yet
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onParticipantJoined(IKandyGroupParticipantJoined participantJoined) {
            // TODO: not complete yet
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onParticipantKicked(IKandyGroupParticipantKicked participantKicked) {
            // TODO: not complete yet
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onParticipantLeft(IKandyGroupParticipantLeft participantLeft) {
            // TODO: not complete yet
        }
    };
}
