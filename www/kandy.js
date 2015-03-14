/*
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
     * Initialize Kandy
     *
     * @param config The configurations
     */
    initialize : function(config){
        this.WIDGETS_DEFAULT = { // Default widgets
            PROVISIONING: "kandy-provisioning-widget",
            ACCESS: "kandy-access-widget",
            CALL: "kandy-call-widget",
            CHAT: "kandy-chat-widget"
        };

        this.defaultErrorAction = function(e){
            console.log(e);
            alert(e); // default
        }

        this.defaultSuccessAction = function(s){
            // nothing
        }

        if (config == undefined){
            config = {}; // avoid undefined object
        }

        this.registerNotificationListeners(config.listeners);
        this.renderKandyWidgets(config.widgets);
    },

    /**
     * Register notification listeners
     *
     * @param listeners The notification listeners
     */
    registerNotificationListeners : function(listeners){
        if(listeners == undefined){
            listeners = {}
        }

        // Register call listeners
        exec(listeners.onIncomingCall, null, "KandyPlugin", "config:onIncomingCall", []);
        exec(listeners.onVideoStateChanged, null, "KandyPlugin", "config:onVideoStateChanged", []);
        exec(listeners.onAudioStateChanged, null, "KandyPlugin", "config:onAudioStateChanged", []);
        exec(listeners.onCallStateChanged, null, "KandyPlugin", "config:onCallStateChanged", []);
        exec(listeners.onGSMCallIncoming, null, "KandyPlugin", "config:onGSMCallIncoming", []);
        exec(listeners.onGSMCallConnected, null, "KandyPlugin", "config:onGSMCallConnected", []);
        exec(listeners.onGSMCallDisconnected, null, "KandyPlugin", "config:onGSMCallDisconnected", []);

        // Register chat listeners
        exec(function(message){
            if (listeners.onChatReceived != undefined){
                listeners.onChatReceived(message);
            }
            if (Kandy.onChatReceived != undefined){ // for widget
                Kandy.onChatReceived(message);
            }
        }, null, "KandyPlugin", "config:onChatReceived", []);
        exec(function(message){
            if (listeners.onChatDelivered != undefined){
                listeners.onChatDelivered(message);
            }
            if (Kandy.onChatDelivered != undefined){ // for widget
                Kandy.onChatDelivered(message);
            }
        }, null, "KandyPlugin", "config:onChatDelivered", []);
        exec(listeners.onChatMediaDownloadProgress, null, "KandyPlugin", "config:onChatMediaDownloadProgress", []);
        exec(listeners.onChatMediaDownloadFailed, null, "KandyPlugin", "config:onChatMediaDownloadFailed", []);
        exec(listeners.onChatMediaDownloadSucceded, null, "KandyPlugin", "config:onChatMediaDownloadSucceded", []);

        // Register presence listeners
        exec(listeners.onPresenceChanged, null, "KandyPlugin", "config:onPresenceChanged", []);

        // Register address book listeners
        exec(listeners.onDeviceAddressBookChanged, null, "KandyPlugin", "config:onDeviceAddressBookChanged", []);
    },

    /**
     * Render Kandy widgets
     *
     * @param widgets The ids of widgets
     */
    renderKandyWidgets : function(widgets){
        if (widgets == undefined){
            widgets = this.WIDGETS_DEFAULT; // avoid undefined
        }

        this.renderProvisioningWidget(widgets.provisioning);
        this.renderAccessWidget(widgets.access);
        this.renderCallWidget(widgets.call);
        this.renderChatWidget(widgets.chat);
    },

    /**
     * Check id for null, undefined or empty
     *
     * @param id
     * @param def
     * @returns {true|false}
     */
    checkAndVerifyNotUndefinedOrEmpty: function(id, def){
        return (id == undefined || id == "" || id == '') ? def : id;
    },

    /**
     * Get function from element and call function
     *
     * @param element
     * @param action
     * @param val
     * @param callback
     */
    callFunctionByAction: function(element, action, val, callback){
        var fn = element.getAttribute("action-" + action);

        if (fn != undefined && fn != "" && fn != ''){
            fn = window[fn];
            if (typeof fn === "function"){
                fn(val);
            }
        } else if(callback != undefined){
            callback(val);
        }
    },

    /**
     * Call success action from element
     *
     * @param element
     * @param val
     * @param callback
     */
    callSuccessFunction: function (element, fn, val, callback) {
        this.callFunctionByAction(element, fn + "-success", val, callback);
    },

    /**
     * Call error function from element
     *
     * @param element
     * @param val
     * @param callback
     */
    callErrorFunction: function(element, fn, val, callback){
        this.callFunctionByAction(element, fn + "-error", val, callback);
    },

    /**
     * Render provisioning widget
     *
     * @param id
     */
    renderProvisioningWidget: function(id){
        id = this.checkAndVerifyNotUndefinedOrEmpty(id, this.WIDGETS_DEFAULT.PROVISIONING);

        var provisioning = document.getElementById(id);

        if (provisioning != undefined){
            var request = '<input type="tel" id="' + id + '-phone-number" placeholder="Enter your number" />'
                + '<input type="text" id="' + id + '-region-code" placeholder="code" maxlength=2/>'
                + '<button id = "' + id + '-btn-request">Request code</button>';
            var validate = '<input type="text" id="' + id + '-otp-code" placeholder="Enter the OTP code" />'
                + '<button id="' + id + '-btn-validate">Validate</button>';
            var deactivate = '<p>Signed as: <span id="' + id + '-user-provisioning">none</span></p>'
                + '<button id="' + id + '-btn-deactivate">Deactivate</button>';

            provisioning.innerHTML = request + validate + deactivate;

            document.getElementById(id + '-btn-request').onclick = function(event){
                var number = document.getElementById(id + '-phone-number').value,
                    code = document.getElementById(id + '-region-code').value;

                Kandy.provisioning.requestCode(function(s){
                    Kandy.callSuccessFunction(request, "request", s);
                }, function(e){
                    Kandy.callErrorFunction(request, "request", e, Kandy.defaultErrorAction);
                }, number, code);
            }

            document.getElementById(id + '-btn-validate').onclick = function (event) {
                var number = document.getElementById(id + '-phone-number').value,
                    code = document.getElementById(id + '-region-code').value,
                    otp = document.getElementById(id + '-otp-code').value;

                Kandy.provisioning.validate(function(s){
                    Kandy.callSuccessFunction(validate, "validate", s);
                    document.getElementById(id + '-user-provisioning').innerText = number;
                }, function(e){
                    Kandy.callErrorFunction(validate, "validate", e, Kandy.defaultErrorAction);
                }, number, otp, code);
            }

            document.getElementById(id + '-btn-deactivate').onclick = function(event){
                Kandy.provisioning.deactivate(function(s){
                    Kandy.callSuccessFunction(deactivate, "deactivate", s);
                }, function(e){
                    Kandy.callErrorFunction(deactivate, "deactivate", e, Kandy.defaultErrorAction);
                })
            }
        }
    },

    /**
     * Render access widget
     *
     * @param id
     */
    renderAccessWidget: function(id){
        id = this.checkAndVerifyNotUndefinedOrEmpty(id, this.WIDGETS_DEFAULT.ACCESS);

        var access = document.getElementById(id);

        if (access != undefined){
            var loginForm = '<input type="text" id="' + id + '-username" placeholder="userID@domain.com"/>'
                + '<input type="password" id="' + id + '-password" placeholder="Password"/>'
                + '<button id="' + id + '-btn-login">Login</button>';
            var logoutForm = function(user){
                return '<button id="' + id + '-btn-logout">' + user + '</button>';
            }

            access.innerHTML = loginForm;

            var addLogoutAction = function(){
                document.getElementById(id + '-btn-logout').onclick = function(event){
                    Kandy.access.logout(function(s){
                        Kandy.callSuccessFunction(access, "logout", s);
                        access.innerHTML = loginForm;
                    }, function(e){
                        Kandy.callErrorFunction(access, "logout", e, Kandy.defaultErrorAction);
                    })
                }
            }

            document.getElementById(id + '-btn-login').onclick = function(event){
                var username = document.getElementById(id + '-username').value,
                    password = document.getElementById(id + '-password').value;

                Kandy.access.login(function(s){
                        Kandy.callSuccessFunction(access, "login", s);
                        access.innerHTML = logoutForm(username);
                        addLogoutAction();
                    }, function(e){
                        Kandy.callErrorFunction(access, "login", e, Kandy.defaultErrorAction);
                    }, username, password
                )
            }
        }
    },

    /**
     * Render call widget
     * @param id
     */
    renderCallWidget: function(id) {
        id = this.checkAndVerifyNotUndefinedOrEmpty(id, this.WIDGETS_DEFAULT.CALL);

        var call = document.getElementById(id);

        if(call != undefined){
            call.innerHTML = '<input type="text" id="' + id + '-callee" placeholder="userID@domain.com"/>'
            + '<input type="checkbox" id="' + id + '-start-with-video"/>Start with video</label>'
            + '<button id="' + id + '-btn-call">Call</button>';

            document.getElementById(id + '-btn-call').onclick = function (event) {
                var username = document.getElementById(id + '-callee').value,
                    startWithVideo = document.getElementById(id + '-start-with-video').checked;

                Kandy.call.makeCallDialog(function(s){
                        Kandy.callSuccessFunction(call, "call", s);
                    }, function(e){
                        Kandy.callErrorFunction(call, "call", e, Kandy.defaultErrorAction);
                    }, [{phoneNumber: username, startWithVideo: startWithVideo}]
                );
            }
        }
    },

    /**
     * Render chat widget
     *
     * @param id
     */
    renderChatWidget: function(id){
        // Register message received
        this.onChatReceived = function(message){
            var msg = message.message;
            var item =  '<li onClick="js:Kandy.markMessageAsReceived(\'' + msg.UUID + '\')"><h3>' + msg.sender + '</h3><p id="' + msg.UUID +'"><strong>' + msg.message.text + '</strong></p><p>' + msg.timestamp + '</p></li>';
            messages.innerHTML += item + messages.innerHTML;
        }

        id = this.checkAndVerifyNotUndefinedOrEmpty(id, this.WIDGETS_DEFAULT.CHAT);

        var chat = document.getElementById(id);

        if (chat != undefined){
            chat.innerHTML = '<input type="text" id="' + id + '-recipient" placeholder="recipientID@domain.com"/>'
            + '<input type="text" id="' + id + '-message" placeholder="Message"/>'
            + '<button id="' + id + '-btn-send">Send</button>'
            + '<button id="' + id + '-btn-pull">Pull pending events</button>'
            + '<div id="' + id + '-messages"></div>';

            var messages = document.getElementById(id + '-messages');


            document.getElementById(id + '-btn-send').onclick = function(event){
                var recipient = document.getElementById(id + '-recipient').value,
                    message = document.getElementById(id + '-message').value;

                Kandy.chat.send(function(s){
                    Kandy.callSuccessFunction(chat, "send", s);
                    var item = '<li><h3>You: </h3><p>' + message + '</p><p></p></li>';
                    messages.innerHTML = item + messages.innerHTML;
                }, function (e) {
                    Kandy.callErrorFunction(chat, "send", e, Kandy.defaultErrorAction);
                }, recipient, message)

            }

            document.getElementById(id + '-btn-pull').onclick = function(event){
                Kandy.chat.pullEvents(function(s){
                    Kandy.callSuccessFunction(chat, "pull", s);
                }, function(e){
                    Kandy.callErrorFunction(chat, "pull", e, Kandy.defaultErrorAction);
                });
            }
        }
    },

    /**
     * Notify message read
     *
     * @param uuid
     */
    markMessageAsReceived: function(uuid){
        Kandy.chat.markAsReceived(function(){
            // Mark as read
            var message = $("#" + uuid).text();
            $("#" + uuid).html(message);
        }, function(e){
            Kandy.defaultErrorAction(e);
        }, uuid);
    },

    //*** PROVISIONING SERVICE ***//
    provisioning: {

        /**
         * Request code for verification and registration process.
         *
         * @param success
         * @param error
         * @param userId
         * @param countryCode
         */
        requestCode: function (success, error, userId, countryCode) {
            exec(success, error, "KandyPlugin", "provisioning:request", [userId, countryCode]);
        },

        /**
         * Validation of the signed up phone number send received code to the server.
         *
         * @param success
         * @param error
         * @param userId
         * @param otp
         * @param countryCode
         */
        validate: function (success, error, userId, otp, countryCode) {
            exec(success, error, "KandyPlugin", "provisioning:validate", [userId, otp, countryCode]);
        },

        /**
         * Signing off the registered account (phone number) from a Kandy.
         *
         * @param success
         * @param error
         */
        deactivate: function (success, error) {
            exec(success, error, "KandyPlugin", "provisioning:deactivate", []);
        }
    },

    //*** ACCESS SERVICE ***//
    access: {

        /**
         * Register/login the user on the server with credentials received from admin.
         *
         * @param success
         * @param error
         * @param username
         * @param password
         */
        login: function (success, error, username, password) {
            exec(success, error, "KandyPlugin", "login", [username, password]);
        },

        /**
         * This method unregisters user from the Kandy server.
         *
         * @param success
         * @param error
         */
        logout: function (success, error) {
            exec(success, error, "KandyPlugin", "logout", []);
        },

        /**
         * Get the current session.
         *
         * @param success
         * @param error
         */
        getSession: function (success, error) {
            exec(success, error, "KandyPlugin", "session", []);
        }

    },

    //*** CALL SERVICE ***//
    call: {

        /**
         * Create a voice call.
         *
         * @param success
         * @param error
         * @param phoneNumber
         */
        makeVoiceCall: function (success, error, phoneNumber) {
            exec(success, error, "KandyPlugin", "call:create", [phoneNumber]);
        },

        /**
         * Hangup current call.
         *
         * @param success
         * @param error
         */
        hangup: function (success, error) {
            exec(success, error, "KandyPlugin", "call:hangup", []);
        },

        /**
         * Mute current call.
         *
         * @param success
         * @param error
         */
        mute: function (success, error) {
            exec(success, error, "KandyPlugin", "call:mute", []);
        },

        /**
         * Unmute current call.
         *
         * @param success
         * @param error
         */
        unmute: function (success, error) {
            exec(success, error, "KandyPlugin", "call:unmute", []);
        },

        /**
         * Hold current call
         *
         * @param success
         * @param error
         */
        hold: function (success, error) {
            exec(success, error, "KandyPlugin", "call:hold", []);
        },

        /**
         * Unhold current call
         *
         * @param success
         * @param error
         */
        unhold: function (success, error) {
            exec(success, error, "KandyPlugin", "call:unhold", []);
        },

        /**
         * Enable sharing video.
         *
         * @param success
         * @param error
         */
        enableVideo: function (success, error) {
            exec(success, error, "KandyPlugin", "call:enableVideo", []);
        },

        /**
         * Disable sharing video.
         *
         * @param success
         * @param error
         */
        disableVideo: function (success, error) {
            exec(success, error, "KandyPlugin", "call:disableVideo", []);
        },

        /**
         * Accept current coming call.
         *
         * @param success
         * @param error
         */
        accept: function (success, error) {
            exec(success, error, "KandyPlugin", "call:accept", []);
        },

        /**
         * Reject current coming call.
         *
         * @param success
         * @param error
         */
        reject: function (success, error) {
            exec(success, error, "KandyPlugin", "call:reject", []);
        },

        /**
         * Ignore current coming call.
         *
         * @param success
         * @param error
         */
        ignore: function (success, error) {
            exec(success, error, "KandyPlugin", "call:ignore", []);
        },

        /**
         * Create a call dialog (native) with video supported.
         *
         * @param success
         * @param error
         * @param config
         */
        makeCallDialog: function (success, error, config) {
            exec(success, error, "KandyPlugin", "call:dialog", config);
        }
    },

    //*** CHAT SERVICE ***/
    chat: {

        /**
         * Send the message to recipient.
         *
         * @param success
         * @param error
         * @param recipient
         * @param message
         */
        send: function (success, error, recipient, message) {
            exec(success, error, "KandyPlugin", "chat:send", [recipient, message]);
        },

        /**
         * Send ack to sever for UUID of received/handled message(s).
         *
         * @param success
         * @param error
         * @param uuid
         */
        markAsReceived: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "chat:markAsReceived", [uuid]);
        },

        /**
         * Pull pending events from Kandy service.
         *
         * @param success
         * @param error
         */
        pullEvents: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pullEvents", []);
        }
    },

    //*** PRESENCE SERVICE ***//
    presence: {

        /**
         * Register listener for presence's callbacks/notifications.
         *
         * @param success
         * @param error
         * @param userList
         */
        startWatch: function (success, error, userList) {
            exec(success, error, "KandyPlugin", "presence", userList);
        }
    },

    //*** LOCATION SERVICE ***//
    location: {

        /**
         * Get the country info.
         *
         * @param success
         * @param error
         */
        getCountryInfo: function (success, error) {
            exec(success, error, "KandyPlugin", "location", []);
        }
    },

    //*** PUSH SERVICE ***//
    push: {

        /**
         * Enable the push service.
         *
         * @param success
         * @param error
         */
        enable: function (success, error) {
            exec(success, error, "KandyPlugin", "push:enable", []);
        },

        /**
         * Disable the push service.
         *
         * @param success
         * @param error
         */
        disable: function (success, error) {
            exec(success, error, "KandyPlugin", "push:disable", []);
        }
    },

    //*** ADDRESS BOOK SERVICE ***//
    addressBook: {

        /**
         * Get local contacts from device
         *
         * @param success
         * @param error
         * @param filters
         */
        getDeviceContacts: function(success, error, filters){
            exec(success, error, "KandyPlugin", "getDeviceContacts", filters);
        }
    }
};

module.exports = Kandy;