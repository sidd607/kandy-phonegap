/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

 "use strict";

var exec = require('cordova/exec');

/**
* Kandy PhoneGap Plugin interface
*
* See [README](https://github.com/kodeplusdev/kandyphonegap/blob/master/README.md) for more details.
*/
var Kandy = {

    /**
    * Setup KandyPlugin based on config parameters.
    */
    setup: function (config) {
        var l = config.listeners;

        exec(null, null, "KandyPlugin", "config/startWithVideo", [config.startWithVideo]);
        exec(null, null, "KandyPlugin", "config/useNativeDialog", [config.useNativeDialog]);

        // Call service
        exec(l.onIncomingCall, null, "KandyPlugin", "config/onIncomingCall", []);
        exec(l.onVideoStateChanged, null, "KandyPlugin", "config/onVideoStateChanged", []);
        exec(l.onAudioStateChanged, null, "KandyPlugin", "config/onAudioStateChanged", []);
        exec(l.onCallStateChanged, null, "KandyPlugin", "config/onCallStateChanged", []);
        exec(l.onGSMCallIncoming, null, "KandyPlugin", "config/onGSMCallIncoming", []);
        exec(l.onGSMCallConnected, null, "KandyPlugin", "config/onGSMCallConnected", []);
        exec(l.onGSMCallDisconnected, null, "KandyPlugin", "config/onGSMCallDisconnected", []);

        // Chat service
        exec(l.onChatReceived, null, "KandyPlugin", "config/onChatReceived", []);
        exec(l.onChatDelivered, null, "KandyPlugin", "config/onChatDelivered", []);
        exec(l.onChatMediaDownloadProgress, null, "KandyPlugin", "config/onChatMediaDownloadProgress", []);
        exec(l.onChatMediaDownloadFailed, null, "KandyPlugin", "config/onChatMediaDownloadFailed", []);
        exec(l.onChatMediaDownloadSucceded, null, "KandyPlugin", "config/onChatMediaDownloadSucceded", []);

        // Presence service
        exec(l.onPresenceChanged, null, "KandyPlugin", "config/onPresenceChanged", []);

        // Address book service
        exec(l.onDeviceAddressBookChanged, null, "KandyPlugin", "config/onDeviceAddressBookChanged", []);
    },

    /**
    * Provisioning Service
    */
    provisioning: {

        /**
        * Request code for verification and registration process.
        */
        requestCode: function (s, f, userId, countryCode) {
            exec(s, f, "KandyPlugin", "provisioning/request", [userId, countryCode]);
        },

        /**
        * Validation of the signed up phone number send received code to the server.
        */
        validate: function (s, f, userId, otp, countryCode) {
            exec(s, f, "KandyPlugin", "provisioning/validate", [userId, otp, countryCode]);
        },

        /**
        * Signing off the registered account (phone number) from a Kandy.
        */
        deactivate: function (s, f) {
            exec(s, f, "KandyPlugin", "provisioning/deactivate", []);
        }
    },

    /**
    * Access Service
    */
    access: {

        /**
        * Register/login the user on the server with credentials received from admin.
        */
        login: function (s, f, username, password) {
            exec(s, f, "KandyPlugin", "login", [username, password]);
        },

        /**
        * This method unregisters user from the Kandy server.
        */
        logout: function (s, f) {
            exec(s, f, "KandyPlugin", "logout", []);
        },

        /**
        * Get the current session.
        */
        getSession: function (s, f) {
            exec(s, f, "KandyPlugin", "session", []);
        }

    },

    /**
    * Call Service
    */
    call: {

        /**
        * Create a voice call.
        */
        makeVoiceCall: function (s, f, phoneNumber) {
            exec(s, f, "KandyPlugin", "call/create", [phoneNumber]);
        },

        /**
        * Hangup current call.
        */
        hangup: function (s, f) {
            exec(s, f, "KandyPlugin", "call/hangup", []);
        },

        /**
        * Mute current call.
        */
        mute: function (s, f) {
            exec(s, f, "KandyPlugin", "call/mute", []);
        },

        /**
        * Unmute current call.
        */
        unMute: function (s, f) {
            exec(s, f, "KandyPlugin", "call/unMute", []);
        },

        /**
        * Hold current call
        */
        hold: function (s, f) {
            exec(s, f, "KandyPlugin", "call/hold", []);
        },

        /**
        * Unhold current call
        */
        unHold: function (s, f) {
            exec(s, f, "KandyPlugin", "call/unHold", []);
        },

        /**
        * Enable sharing video.
        */
        enableVideo: function (s, f) {
            exec(s, f, "KandyPlugin", "call/enableVideo", []);
        },

        /**
        * Diable sharing video.
        */
        disableVideo: function (s, f) {
            exec(s, f, "KandyPlugin", "call/disableVideo", []);
        },

        /**
        * Accept current coming call.
        */
        accept: function (s, f) {
            exec(s, f, "KandyPlugin", "call/accept", []);
        },

        /**
        * Reject currnet coming call.
        */
        reject: function (s, f) {
            exec(s, f, "KandyPlugin", "call/reject", []);
        },

        /**
        * Ignore current coming call.
        */
        ignore: function (s, f) {
            exec(s, f, "KandyPlugin", "call/ignore", []);
        },

        /**
        * Create a call dialog (native) with video supported.
        */
        makeCallDialog: function (s, f, config) {
            exec(s, f, "KandyPlugin", "call/dialog", config);
        }
    },

    /**
    * Chat Service
    */
    chat: {

        /**
        * Send the message to recipient.
        */
        send: function (s, f, recipient, message) {
            exec(s, f, "KandyPlugin", "chat/send", [recipient, message]);
        },

        /**
        * Send ack to sever for UUID of received/handled message(s).
        */
        markAsReceived: function (s, f, uuid) {
            exec(s, f, "KandyPlugin", "chat/markAsReceived", [uuid]);
        },

        /**
        * Pull pending events from Kandy service.
        */
        pullEvents: function () {
            exec(null, null, "KandyPlugin", "chat/pullEvents", []);
        }
    },

    /**
    * Presence Service
    */
    presence: {

        /**
        * Register listener for presence's callbacks/notifications.
        */
        startWatch: function (s, f, userList) {
            exec(s, f, "KandyPlugin", "presence", userList);
        }
    },

    /**
    * Location Service
    */
    location: {

        /**
        * Get the country info.
        */
        getCountryInfo: function (s, f) {
            exec(s, f, "KandyPlugin", "location", []);
        }
    },

    /**
    * Push Service
    */
    push: {

        /**
        * Enable the push service.
        */
        enable: function (s, f) {
            exec(s, f, "KandyPlugin", "push/enable", []);
        },

        /**
        * Disable the push service.
        */
        disable: function (s, f) {
            exec(s, f, "KandyPlugin", "push/disable", []);
        }
    },

    /**
    * Address Book Service
    */
    addressBook: {

        /**
        * Get local contacts from device
        */
        getDeviceContacts: function (s, f, filters){
            exec(s, f, "KandyPlugin", "getDeviceContacts", filters);
        }
    },
};

module.exports = Kandy;