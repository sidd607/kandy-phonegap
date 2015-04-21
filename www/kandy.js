
"use strict";

var exec = require('cordova/exec');

/**
 * Kandy PhoneGap Plugin interface.
 *
 * See [README](https://github.com/Kandy-IO/kandy-phonegap/blob/master/doc/index.md) for more details.
 */
var Kandy = {

    //***** CONSTANT *****//

    Widget: {
        PROVISIONING: "provisioning",
        ACCESS: "access",
        CALL: "call",
        CHAT: "chat"
    },

    DeviceContactsFilter: {
        ALL: "ALL",
        IS_FAVORITE: "IS_FAVORITE",
        HAS_PHONE_NUMBER: "HAS_PHONE_NUMBER",
        HAS_EMAIL_ADDRESS: "HAS_EMAIL_ADDRESS"
    },

    DomainContactFilter: {
        ALL: "ALL",
        FIRST_AND_LAST_NAME: "FIRST_AND_LAST_NAME",
        USER_ID: "USER_ID",
        PHONE: "PHONE"
    },

    ThumbnailSize: {
        LARGE: "LARGE",
        MEDIUM: "MEDIUM",
        SMALL: "SMALL"
    },

    ConnectionState: {
        UNKNOWN: "UNKNOWN",
        DISCONNECTED: "DISCONNECTED",
        CONNECTED: "CONNECTED",
        DISCONNECTING: "DISCONNECTING",
        CONNECTING: "CONNECTING",
        FAILED: "FAILED"
    },

    CallState: {
        INITIAL: "INITIAL",
        RINGING: "RINGING",
        DIALING: "DIALING",
        TALKING: "TALKING",
        TERMINATED: "TERMINATED",
        ON_DOUBLE_HOLD: "ON_DOUBLE_HOLD",
        REMOTELY_HELD: "REMOTELY_HELD",
        ON_HOLD: "ON_HOLD"
    },

    ConnectionType: {
        NONE: "NONE",
        MOBILE: "MOBILE",
        WIFI: "WIFI",
        ALL: "ALL"
    },

    PickerResult: {
        CONTACT_PICKER_RESULT: 1001,
        IMAGE_PICKER_RESULT: 1002,
        VIDEO_PICKER_RESULT: 1003,
        AUDIO_PICKER_RESULT: 1004,
        FILE_PICKER_RESULT: 1005
    },

    //*** LISTENERS ***//

    // Access listeners
    onConnectionStateChanged: function (args) {
    },
    onSocketConnected: function () {
    },
    onSocketConnecting: function () {
    },
    onSocketDisconnected: function () {
    },
    onSocketFailedWithError: function (args) {
    },
    onInvalidUser: function (args) {
    },
    onSessionExpired: function (args) {
    },
    onSDKNotSupported: function (args) {
    },

    // Call listeners
    onIncomingCall: function (args) {
    },
    onMissedCall: function (args) {
    },
    onCallStateChanged: function (args) {
    },
    onVideoStateChanged: function (args) {
    },
    onAudioStateChanged: function (args) {
    },
    onGSMCallIncoming: function (args) {
    },
    onGSMCallConnected: function (args) {
    },
    onGSMCallDisconnected: function (args) {
    },

    // Chat listeners
    onChatReceived: function (args) {
    },
    onChatDelivered: function (args) {
    },
    onChatMediaAutoDownloadProgress: function (args) {
    },
    onChatMediaAutoDownloadSucceded: function (args) {
    },
    onChatMediaAutoDownloadFailed: function (args) {
    },

    // Group listeners
    onGroupDestroyed: function (args) {
    },
    onGroupUpdated: function (args) {
    },
    onParticipantJoined: function (args) {
    },
    onParticipantKicked: function (args) {
    },
    onParticipantLeft: function (args) {
    },

    // Addressbook listeners
    onDeviceAddressBookChanged: function () {
    },

    //*** LOGIC ***//

    /**
     * Initialize Kandy SDK.
     *
     * @param config The Kandy plugin configurations.
     */
    initialize: function (config) {
        this._setupKandyPluginWithConfig(config);
        this._registerNotificationListeners();
        this._renderKandyWidgets();
    },

    /**
     * Setup kandy plugin
     *
     * @param config The configurations
     * @private
     */
    _setupKandyPluginWithConfig: function (config) {
        if (config == undefined) return;

        var callback = function (args) {
            console.log(args);
        }

        exec(callback, callback, "KandyPlugin", "configurations", [config]);
    },

    /**
     * Default chat service notification callback.
     *
     * @param args The callback parameter.
     * @private
     */
    _chatServiceNotificationPluginCallback: function (args) {
        switch (args.action) {
            case "onChatReceived":
                Kandy._onChatReceived(args.data);
                break;
            case "onChatDelivered":
                // TODO: not complete yet
                break;
            case "onChatMediaAutoDownloadProgress":
                if (args.data.process == 0) {
                    Kandy._makeToast("Downloading a attachment...");
                }
                break;
            case "onChatMediaAutoDownloadSucceded":
                Kandy._onChatReceived(args.data);
                Kandy._makeToast(args.data.message.message.content_name + " saved at " + args.data.uri);
                break;
            default :
        }
    },

    /**
     * Execute callback function by name `args["action"]`
     *
     * @param args The args of the callback function.
     * @private
     */
    _notificationCallback: function (args) {
        return Kandy._executeFunctionByName(args.action, Kandy, args.data);
    },

    /**
     * Register notification listeners.
     *
     * @private
     */
    _registerNotificationListeners: function () {
        exec(this._notificationCallback, null, "KandyPlugin", "connectServiceNotificationCallback", []);
        exec(this._notificationCallback, null, "KandyPlugin", "callServiceNotificationCallback", []);
        exec(this._notificationCallback, null, "KandyPlugin", "addressBookServiceNotificationCallback", []);
        exec(this._notificationCallback, null, "KandyPlugin", "chatServiceNotificationCallback", []);
        exec(this._notificationCallback, null, "KandyPlugin", "groupServiceNotificationCallback", []);

        exec(this._chatServiceNotificationPluginCallback, null, "KandyPlugin", "chatServiceNotificationPluginCallback", []);
    },

    /**
     * Load plugin stylesheets and javascripts.
     * @private
     */
    _loadPluginResources: function () {
        this._loadStylesheets(["kandy.css"]);
        this._loadJavascript([]);
    },

    /**
     * Get the file path of the plugin.
     *
     * @param filename The file name.
     * @param type The type of the file.
     * @returns {string}
     * @private
     */
    _link: function (filename, type) {
        return "plugins/com.kandy.phonegap/www/" + type + "/" + filename;
    },

    /**
     * Load plugin stylesheets.
     *
     * @param files The file list.
     * @private
     */
    _loadStylesheets: function (files) {
        for (var i = 0; i < files.length; ++i) {
            var link = document.createElement("link");
            link.setAttribute("rel", "stylesheet");
            link.setAttribute("type", "text/css");
            link.setAttribute("href", this._link(files[i], "css"));
            if (typeof link != "undefined")
                document.getElementsByTagName("head")[0].appendChild(link);
        }
    },

    /**
     * Load plugin javascript.
     *
     * @param files The file list.
     * @private
     */
    _loadJavascript: function (files) {
        for (var i = 0; i < files.length; ++i) {
            var link = document.createElement("script");
            link.setAttribute("type", "text/javascript");
            link.setIdAttribute("src", this._link(files[i], "js"));
            if (typeof link != "undefined")
                document.getElementsByTagName("head")[0].appendChild(link);
        }
    },

    /**
     * Render Kandy widgets.
     *
     * @private
     */
    _renderKandyWidgets: function () {

        this._loadPluginResources();

        var widgets = document.getElementsByTagName("kandy");
        for (var i = 0; i < widgets.length; ++i) {
            var name = widgets[i].getAttribute("widget");
            switch (name) {
                case this.Widget.PROVISIONING:
                    this._renderKandyProvisioningWidget(widgets[i]);
                    break;
                case this.Widget.ACCESS:
                    this._renderKandyAccessWidget(widgets[i]);
                    break;
                case this.Widget.CALL:
                    this._renderKandyCallWidget(widgets[i]);
                    break;
                case this.Widget.CHAT:
                    this._renderKandyChatWidget(widgets[i]);
                    break;
                default:
                    break;
            }
        }
    },

    /**
     * Default success action callback.
     *
     * @param success The success parameter.
     * @private
     */
    _defaultSuccessAction: function (success) {
        console.log(success);
    },

    /**
     * Default error action callback.
     *
     * @param error The error parameter.
     * @private
     */
    _defaultErrorAction: function (error) {
        console.log(error), alert(error); // default action
    },

    /**
     * Create a Toast message.
     * @param message The message to show.
     */
    _makeToast: function (message) {
        exec(null, null, "KandyPlugin", "makeToast", [message]);
    },

    /**
     * Find or Validate an Email Address
     *
     * @param email The email address
     * @returns {boolean}
     * @private
     */
    _validateEmail: function (email) {
        var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
        return re.test(email);
    },

    /**
     * Execute a function by name in a context scope.
     *
     * @param functionName The function name to execute.
     * @param context The context scope.
     * @private
     */
    _executeFunctionByName: function (functionName, context /*, args */) {
        var args = [].slice.call(arguments).splice(2);
        var namespaces = functionName.split(".");
        var fn = namespaces.pop();
        for (var i = 0; i < namespaces.length; i++) {
            context = context[namespaces[i]];
        }
        return context[fn].apply(this, args);
    },

    /**
     * Check and execute a function by name.
     *
     * @param fn The function name.
     * @param args The args of the function.
     * @param callback The callback function if fn do not exist.
     * @private
     */
    _checkAndCallFunction: function (fn, args, callback) {
        if (fn != undefined && fn != "" && fn != '') {
            return this._executeFunctionByName(fn, window, args);
        } else if (callback != undefined) {
            return callback(args);
        }
    },

    /**
     * Get function from element and trigger it.
     *
     * @param element The element widget.
     * @param action The action name.
     * @param val The value of the parameter which is passed to function.
     * @param callback The default action would be used if the function was not found.
     * @private
     */
    _callFunctionByAction: function (element, action, val, callback) {
        var fn = element.getAttribute(action);

        this._checkAndCallFunction(fn, val, callback);
    },

    /**
     * Call success action from element.
     *
     * @param element The element widget.
     * @param val The value of the parameter which is passed to function.
     * @param callback The default action would be used if the function was not found.
     * @private
     */
    _callSuccessFunction: function (element, fn, val, callback) {
        this._callFunctionByAction(element, fn + "-success", val, callback);
    },

    /**
     * Call error function from element.
     *
     * @param element The element widget.
     * @param val The value of the parameter which is passed to function.
     * @param callback The default action would be used if the function was not found.
     * @private
     */
    _callErrorFunction: function (element, fn, val, callback) {
        this._callFunctionByAction(element, fn + "-error", val, callback);
    },

    /**
     * Get next id of the element.
     *
     * @param element The element of the widget.
     * @private
     */
    _getIdOrGenerateNextId: function (element) {
        var id = element.getAttribute("id");
        if (id == undefined || id == "") {
            var idx = -1;
            var prefix = "kandy";
            var name = element.getAttribute("widget");

            do {
                ++idx;
                id = prefix + "-" + name + "-" + idx;
            } while (document.getElementById(id) != undefined);
        }

        element.setAttribute("id", id);

        return id;
    },

    /**
     * Render provisioning widget.
     *
     * @param element The element of the provisioning widget.
     * @private
     */
    _renderKandyProvisioningWidget: function (element) {
        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        var code = element.getAttribute("country-code");

        element.innerHTML += '<input type="tel" id="' + id + '-phone-number" placeholder="Enter your number" />'
        + '<input type="text" id="' + id + '-region-code" placeholder="2-letters country code" maxlength="2" value="' + code + '"/>'
        + '<button id = "' + id + '-btn-request">Request code</button>'
        + '<input type="text" id="' + id + '-otp-code" placeholder="Enter the OTP code" />'
        + '<button id="' + id + '-btn-validate">Validate</button>'
        + '<button id="' + id + '-btn-deactivate">Deactivate</button>';

        document.getElementById(id + '-btn-request').onclick = function (event) {
            var number = document.getElementById(id + '-phone-number').value,
                code = document.getElementById(id + '-region-code').value;

            Kandy.provisioning.requestCode(function (s) {
                Kandy._callSuccessFunction(element, "request", s, function (s) {
                    alert("Your request has been sent successfully.");
                });
            }, function (e) {
                Kandy._callErrorFunction(element, "request", e, Kandy._defaultErrorAction);
            }, number, code);
        }

        document.getElementById(id + '-btn-validate').onclick = function (event) {
            var number = document.getElementById(id + '-phone-number').value,
                code = document.getElementById(id + '-region-code').value,
                otp = document.getElementById(id + '-otp-code').value;

            Kandy.provisioning.validate(function (s) {
                Kandy._callSuccessFunction(element, "validate", s, function (account) {
                    alert("ACCOUNT INFORMATION\nId: " + account.id + "\nDomain: " + account.domain + "\nUsername: " + account.username + "\nPassword: " + account.password);
                });
            }, function (e) {
                Kandy._callErrorFunction(element, "validate", e, Kandy._defaultErrorAction);
            }, number, otp, code);
        }

        document.getElementById(id + '-btn-deactivate').onclick = function (event) {
            Kandy.provisioning.deactivate(function (s) {
                Kandy._callSuccessFunction(element, "deactivate", s, function (s) {
                    alert("Your account has been deactivated.");
                });
            }, function (e) {
                Kandy._callErrorFunction(element, "deactivate", e, Kandy._defaultErrorAction);
            })
        }
    },

    /**
     * Render access widget.
     *
     * @param element The element of the access widget.
     * @private
     */
    _renderKandyAccessWidget: function (element) {
        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        var loginForm = '<input type="text" id="' + id + '-username" placeholder="userID@domain.com"/>'
            + '<input type="password" id="' + id + '-password" placeholder="Password"/>'
            + '<button id="' + id + '-btn-login">Login</button>';
        var logoutForm = function (user) {
            return '<button id="' + id + '-btn-logout">' + user + '</button>';
        }

        var addLogoutAction = function () {
            document.getElementById(id + '-btn-logout').onclick = function (event) {
                Kandy.access.logout(function (s) {
                    element.innerHTML = loginForm;
                    addLoginAction();
                    Kandy._callSuccessFunction(element, "logout", s, Kandy._defaultSuccessAction);
                }, function (e) {
                    Kandy._callErrorFunction(element, "logout", e, Kandy._defaultErrorAction);
                })
            }
        }

        var addLoginAction = function () {
            document.getElementById(id + '-btn-login').onclick = function (event) {
                var username = document.getElementById(id + '-username').value,
                    password = document.getElementById(id + '-password').value;

                Kandy.access.login(function (s) {
                        element.innerHTML = logoutForm(username);
                        addLogoutAction();
                        Kandy._callSuccessFunction(element, "login", s, Kandy._defaultSuccessAction);
                    }, function (e) {
                        Kandy._callErrorFunction(element, "login", e, Kandy._defaultErrorAction);
                    }, username, password
                )
            }
        }

        element.innerHTML = loginForm;
        addLoginAction();
    },

    /**
     * Render call widget.
     *
     * @param element The element of the call widget.
     * @private
     */
    _renderKandyCallWidget: function (element) {
        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        var type = element.getAttribute("type");
        var callee = element.getAttribute("call-to");
        var label = element.getAttribute("label");
        var startWithVideo = element.getAttribute("start-with-video");

        if (label == undefined || label == "") label = "Call";

        if (type.toLowerCase() == 'pstn') {
            if (callee != undefined && callee != "" && !this._validateEmail(callee)) {
                element.innerHTML = '<input type="hidden" id="' + id + '-callee" value=' + callee + '/>';
            } else {
                element.innerHTML = '<input type="text" id="' + id + '-callee" placeholder="Number phone"/>';
            }

            element.innerHTML += '<button id="' + id + '-btn-call">' + label + '</button>';

            document.getElementById(id + '-btn-call').onclick = function (event) {
                var username = document.getElementById(id + '-callee').value;

                Kandy.call.createPSTNCall(function (s) {
                        Kandy._callSuccessFunction(element, "call", s, Kandy._defaultSuccessAction);
                    }, function (e) {
                        Kandy._callErrorFunction(element, "call", e, Kandy._defaultErrorAction);
                    }, username
                );
            }
        } else {

            if (callee != undefined && callee != "" && this._validateEmail(callee)) {
                element.innerHTML = '<input type="hidden" id="' + id + '-callee" value=' + callee + '/>';
            } else {
                element.innerHTML = '<input type="text" id="' + id + '-callee" placeholder="userID@domain.com"/>';
            }

            if (startWithVideo != undefined && startWithVideo != "") {
                var checked = (startWithVideo == 1 || startWithVideo == "true") ? "checked" : "";
                element.innerHTML += '<input type="hidden" id="' + id + '-start-with-video"' + checked + '/>';
            } else
                element.innerHTML += '<label><input type="checkbox" id="' + id + '-start-with-video"/>Start with video</label>';

            element.innerHTML += '<button id="' + id + '-btn-call">' + label + '</button>';

            document.getElementById(id + '-btn-call').onclick = function (event) {
                var username = document.getElementById(id + '-callee').value,
                    startWithVideo = document.getElementById(id + '-start-with-video').checked == true ? 1 : 0;

                Kandy.call.createVoipCall(function (s) {
                        Kandy._callSuccessFunction(element, "call", s, Kandy._defaultSuccessAction);
                    }, function (e) {
                        Kandy._callErrorFunction(element, "call", e, Kandy._defaultErrorAction);
                    }, username, startWithVideo
                );
            }
        }
    },

    /**
     * Render chat widget.
     *
     * @param element The element of the chat widget.
     * @private
     */
    _renderKandyChatWidget: function (element) {
        this._onChatReceived = function (message) {
            var msg = message.message;

            if ($("#" + msg.UUID).length) return;

            var extras = "";
            switch (msg.contentType) {
                case "text":
                    break;
                case "location":
                    extras = "lat: " + msg.message.location_latitude
                    + " lng: " + msg.message.location_longitude
                    + " acc: " + msg.message.media_accuracy
                    + " zoom: " + msg.message.media_map_zoom;
                    break;
                default: // audio, video, contact, file
                    if (message.uri == undefined) return;
                    extras = "<u onclick=\"js:Kandy.chat.openAttachment(null, null,\'" + message.uri + "\',\'" + msg.message.mimeType + "\')\">" + msg.message.content_name + "</u>";
                    break;
            }

            var item = '<li onClick="js:Kandy._markMessageAsReceived(\'' + msg.UUID + '\')">' +
                '<h3>' + msg.sender + '</h3>' +
                '<p id="' + msg.UUID + '">' +
                '<div id="' + msg.UUID + "-text" + '"><strong>' + msg.message.text + '</strong></div>' +
                '<div id="' + msg.UUID + "-extras" + '">' + extras + '</div>' +
                '</p>' +
                '<p><small>' + new Date(msg.timestamp).toUTCString() + '</small></p>' +
                '</li>';

            messages.innerHTML = item + messages.innerHTML;
        }

        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        var type = element.getAttribute("type");
        var recipientValue = element.getAttribute("send-to");

        element.innerHTML = '<button id="' + id + '-btn-pull">Pull pending events</button>'
        + '<div id="' + id + '-messages"></div>';

        var messages = document.getElementById(id + '-messages');

        if (type.toLowerCase() == "sms") {

            if (recipientValue != undefined && recipientValue != "" && !this._validateEmail(recipientValue)) {
                recipientValue = 'value="' + recipientValue + '" disabled';
            } else {
                recipientValue = "";
            }

            element.innerHTML = '<input type="text" id="' + id + '-recipient" placeholder="The number phone" ' + recipientValue + '/>'
            + '<input type="text" id="' + id + '-message" placeholder="Message"/>'
            + '<button id="' + id + '-btn-send">Send</button>'
            + element.innerHTML;

            messages = document.getElementById(id + '-messages');

            document.getElementById(id + '-btn-send').onclick = function (event) {
                var recipient = document.getElementById(id + '-recipient').value,
                    message = document.getElementById(id + '-message').value;

                Kandy.chat.sendSMS(function (s) {
                    Kandy._callSuccessFunction(element, "send", s, function () {
                        Kandy._makeToast("Message has been sent");
                    });
                }, function (e) {
                    Kandy._callErrorFunction(element, "send", e, Kandy._defaultErrorAction);
                }, recipient, message)
            }
        } else {
            if (recipientValue != undefined && recipientValue != "" && this._validateEmail(recipientValue)) {
                recipientValue = 'value="' + recipientValue + '" disabled';
            } else {
                recipientValue = "";
            }

            element.innerHTML = '<input type="text" id="' + id + '-recipient" placeholder="recipientID@domain" ' + recipientValue + '/>'
            + '<input type="text" id="' + id + '-message" placeholder="Message"/>'
            + '<button id="' + id + '-btn-send">Send</button>'
            + '<button id="' + id + '-btn-attach">Attachment</button>'
            + element.innerHTML;

            messages = document.getElementById(id + '-messages');

            document.getElementById(id + '-btn-send').onclick = function (event) {
                var recipient = document.getElementById(id + '-recipient').value,
                    message = document.getElementById(id + '-message').value;

                Kandy.chat.sendChat(function (s) {
                    var item = '<li><h3>You: </h3><p>' + message + '</p><p></p></li>';
                    messages.innerHTML = item + messages.innerHTML;
                    Kandy._callSuccessFunction(element, "send", s, function () {
                        Kandy._makeToast("Message has been sent");
                    });
                }, function (e) {
                    Kandy._callErrorFunction(element, "send", e, Kandy._defaultErrorAction);
                }, recipient, message)

            }

            document.getElementById(id + '-btn-attach').onclick = function (event) {
                var recipient = document.getElementById(id + '-recipient').value,
                    caption = document.getElementById(id + '-message').value;

                Kandy.chat.sendAttachment(function (s) {
                    Kandy._callSuccessFunction(element, "attach", s, function () {
                        Kandy._makeToast("Attachment has been sent");
                    });
                }, function (e) {
                    Kandy._callErrorFunction(element, "attach", e, Kandy._defaultErrorAction);
                }, recipient, caption)
            }
        }

        document.getElementById(id + '-btn-pull').onclick = function (event) {
            Kandy.chat.pullEvents(function (s) {
                Kandy._callSuccessFunction(element, "pull", s, Kandy._defaultSuccessAction);
            }, function (e) {
                Kandy._callErrorFunction(element, "pull", e, Kandy._defaultErrorAction);
            });
        }
    },

    /**
     * Notify message read.
     *
     * @param uuid The UUID of the message.
     * @private
     */
    _markMessageAsReceived: function (uuid) {
        Kandy.chat.markAsReceived(function () {
            var e = document.getElementById(uuid + "-text");
            e.innerHTML = e.textContent;
        }, function (e) {
            Kandy._defaultErrorAction(e);
        }, uuid);
    },

    //*** PROVISIONING SERVICE ***//
    provisioning: {

        /**
         * Request code for verification and registration process.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param phoneNumber The user phone number.
         * @param countryCode The two letter ISO country code.
         */
        requestCode: function (success, error, phoneNumber, countryCode) {
            exec(success, error, "KandyPlugin", "provisioning:request", [phoneNumber, countryCode]);
        },

        /**
         * Validation of the signed up phone number send received code to the server.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param phoneNumber The user phone number.
         * @param otp The OTP code.
         * @param countryCode The two letter ISO country code.
         */
        validate: function (success, error, phoneNumber, otp, countryCode) {
            exec(success, error, "KandyPlugin", "provisioning:validate", [phoneNumber, otp, countryCode]);
        },

        /**
         * Signing off the registered account (phone number) from a Kandy.
         *
         * @param success The success callback function.
         * @param error The error callback function.
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
         * @param success The success callback function.
         * @param error The error callback function.
         * @param username The username to login
         * @param password The password to login
         */
        login: function (success, error, username, password) {
            exec(success, error, "KandyPlugin", "login", [username, password]);
        },

        /**
         * This method unregisters user from the Kandy server.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        logout: function (success, error) {
            exec(success, error, "KandyPlugin", "logout", []);
        },

        /**
         * Get the current state.
         *
         * @param success The success callback function.
         */
        getConnectionState: function (success) {
            exec(success, null, "KandyPlugin", "getConnectionState", []);
        },

        /**
         * Get current session.
         *
         * @param success The success callback function.
         */
        getSession: function (success) {
            exec(success, null, "KandyPlugin", "getSession", []);
        }

    },

    //*** CALL SERVICE ***//
    call: {

        /**
         * Create a voip call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param user The id of callee.
         * @param startWithVideo Start the call with video call enabled.
         */
        createVoipCall: function (success, error, user, startWithVideo) {
            exec(success, error, "KandyPlugin", "call:createVoipCall", [user, startWithVideo]);
        },

        /**
         * Create a PSTN call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param phoneNumber The user phone number
         */
        createPSTNCall: function (success, error, phoneNumber) {
            exec(success, error, "KandyPlugin", "call:createPSTNCall", [phoneNumber]);
        },

        /**
         * Hangup current call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        hangup: function (success, error) {
            exec(success, error, "KandyPlugin", "call:hangup", []);
        },

        /**
         * Mute current call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        mute: function (success, error) {
            exec(success, error, "KandyPlugin", "call:mute", []);
        },

        /**
         * Unmute current call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        unmute: function (success, error) {
            exec(success, error, "KandyPlugin", "call:unmute", []);
        },

        /**
         * Hold current call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        hold: function (success, error) {
            exec(success, error, "KandyPlugin", "call:hold", []);
        },

        /**
         * Unhold current call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        unhold: function (success, error) {
            exec(success, error, "KandyPlugin", "call:unhold", []);
        },

        /**
         * Enable sharing video.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        enableVideo: function (success, error) {
            exec(success, error, "KandyPlugin", "call:enableVideo", []);
        },

        /**
         * Disable sharing video.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        disableVideo: function (success, error) {
            exec(success, error, "KandyPlugin", "call:disableVideo", []);
        },

        /**
         * Accept current coming call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        accept: function (success, error) {
            exec(success, error, "KandyPlugin", "call:accept", []);
        },

        /**
         * Reject current coming call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        reject: function (success, error) {
            exec(success, error, "KandyPlugin", "call:reject", []);
        },

        /**
         * Ignore current coming call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        ignore: function (success, error) {
            exec(success, error, "KandyPlugin", "call:ignore", []);
        }
    },

    //*** CHAT SERVICE ***/
    chat: {

        /**
         * Send the message to recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message/
         * @param message The message to send.
         */
        sendChat: function (success, error, recipient, message) {
            exec(success, error, "KandyPlugin", "chat:sendChat", [recipient, message]);
        },

        /**
         * Send the SMS to recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param message The message to send.
         */
        sendSMS: function (success, error, recipient, message) {
            exec(success, error, "KandyPlugin", "chat:sendSMS", [recipient, message]);
        },

        /**
         * Pick a audio file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         *
         */
        pickAudio: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pickAudio", []);
        },

        /**
         * Send a audio file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         */
        sendAudio: function (success, error, recipient, caption, uri) {
            exec(success, error, "KandyPlugin", "chat:sendAudio", [recipient, caption, uri]);
        },

        /**
         * Pick a video file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickVideo: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pickVideo", []);
        },

        /**
         * Send a video file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         */
        sendVideo: function (success, error, recipient, caption, uri) {
            exec(success, error, "KandyPlugin", "chat:sendVideo", [recipient, caption, uri]);
        },

        /**
         * Pick a image file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickImage: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pickImage", []);
        },

        /**
         * Send a image file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         */
        sendImage: function (success, error, recipient, caption, uri) {
            exec(success, error, "KandyPlugin", "chat:sendImage", [recipient, caption, uri]);
        },

        /**
         * Pick a file file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickFile: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pickFile", []);
        },

        /**
         * Send a file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         */
        sendFile: function (success, error, recipient, caption, uri) {
            exec(success, error, "KandyPlugin", "chat:sendFile", [recipient, caption, uri]);
        },

        /**
         * Pick a contact file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickContact: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pickContact", []);
        },

        /**
         * Send a contact to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         */
        sendContact: function (success, error, recipient, caption, uri) {
            exec(success, error, "KandyPlugin", "chat:sendContact", [recipient, caption, uri]);
        },

        /**
         * Send current location info to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         */
        sendCurrentLocation: function (success, error, recipient, caption) {
            exec(success, error, "KandyPlugin", "chat:sendCurrentLocation", [recipient, caption]);
        },

        /**
         * Send a location info to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param location The location to send.
         */
        sendLocation: function (success, error, recipient, caption, location) {
            exec(success, error, "KandyPlugin", "chat:sendLocation", [recipient, caption, location]);
        },

        /**
         * Open a chooser dialog and send the attachment.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The recipient user.
         * @param caption The message (caption) of the attachment.
         */
        sendAttachment: function (success, error, recipient, caption) {
            var _success = function (args) {
                switch (args.code) {
                    case Kandy.PickerResult.AUDIO_PICKER_RESULT:
                        Kandy.chat.sendAudio(success, error, recipient, caption, args.uri);
                        break;
                    case Kandy.PickerResult.VIDEO_PICKER_RESULT:
                        Kandy.chat.sendVideo(success, error, recipient, caption, args.uri);
                        break;
                    case Kandy.PickerResult.IMAGE_PICKER_RESULT:
                        Kandy.chat.sendImage(success, error, recipient, caption, args.uri);
                        break;
                    case Kandy.PickerResult.CONTACT_PICKER_RESULT:
                        Kandy.chat.sendContact(success, error, recipient, caption, args.uri);
                        break;
                    case Kandy.PickerResult.FILE_PICKER_RESULT:
                        Kandy.chat.sendFile(success, error, recipient, caption, args.uri);
                        break;
                    default:
                        break;
                }
            }

            exec(_success, error, "KandyPlugin", "chat:sendAttachment", [recipient, caption]);
        },

        /**
         * Open a attachment of the file.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uri The uri of the attachment.
         * @param mimeType The mimeType of the attachment.
         */
        openAttachment: function (success, error, uri, mimeType) {
            exec(success, error, "KandyPlugin", "chat:openAttachment", [uri, mimeType]);
        },

        /**
         * Cancel the current media transfer of the message.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         */
        cancelMediaTransfer: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "chat:cancelMediaTransfer", [uuid]);
        },

        /**
         * Download the media file of the message.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         */
        downloadMedia: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "chat:downloadMedia", [uuid]);
        },

        /**
         * Get a thumbnail of the media message.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         * @param thumbnailSize The {@link ThumbnailSize} of image.
         */
        downloadMediaThumbnail: function (success, error, uuid, thumbnailSize) {
            exec(success, error, "KandyPlugin", "chat:downloadMediaThumbnail", [uuid, thumbnailSize]);
        },

        /**
         * Send ack to sever for UUID of received/handled message(s).
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         */
        markAsReceived: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "chat:markAsReceived", [uuid]);
        },

        /**
         * Pull pending events from Kandy service.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pullEvents: function (success, error) {
            exec(success, error, "KandyPlugin", "chat:pullEvents", []);
        }
    },

    // *** GROUP SERVICE ***//
    group: {

        /**
         * Create a new group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param name The name of the group to create.
         */
        createGroup: function (success, error, name) {
            exec(success, error, "KandyPlugin", "group:createGroup", [name]);
        },

        /**
         * Get group list of user.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getMyGroups: function (success, error) {
            exec(success, error, "KandyPlugin", "group:getMyGroups", []);
        },

        /**
         * Get group detail by group id.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        getGroupById: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:getGroupById", [id]);
        },

        /**
         * Update group name.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param newName The new name of the group.
         */
        updateGroupName: function (success, error, id, newName) {
            exec(success, error, "KandyPlugin", "group:updateGroupName", [id, newName]);
        },

        /**
         * Update group image.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param uri The uri of the image.
         */
        updateGroupImage: function (success, error, id, uri) {
            exec(success, error, "KandyPlugin", "group:updateGroupImage", [id, uri]);
        },

        /**
         * Remove group image.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        removeGroupImage: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:removeGroupImage", [id]);
        },

        /**
         * Get the group image.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        downloadGroupImage: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:downloadGroupImage", [id]);
        },

        /**
         * Get thumbnail group image.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param thumbnailSize The {@link ThumbnailSize} of image.
         */
        downloadGroupImageThumbnail: function (success, error, id, thumbnailSize) {
            exec(success, error, "KandyPlugin", "group:downloadGroupImageThumbnail", [id, thumbnailSize]);
        },

        /**
         * Mute the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        muteGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:muteGroup", [id]);
        },

        /**
         * Unmute the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        unmuteGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:unmuteGroup", [id]);
        },

        /**
         * Destroy the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        destroyGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:destroyGroup", [id]);
        },

        /**
         * Leave the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        leaveGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "group:leaveGroup", [id]);
        },

        /**
         * Remove participants of the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param participants The uri list of the participants.
         */
        removeParticipants: function (success, error, id, participants) {
            exec(success, error, "KandyPlugin", "group:removeParticipants", [id, participants]);
        },

        /**
         * Mute participants of the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param participants The uri list of the participants.
         */
        muteParticipants: function (success, error, id, participants) {
            exec(success, error, "KandyPlugin", "group:muteParticipants", [id, participants]);
        },

        /**
         * Unmute participants of the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param participants The uri list of the participants.
         */
        unmuteParticipants: function (success, error, id, participants) {
            exec(success, error, "KandyPlugin", "group:unmuteParticipants", [id, participants]);
        },

        /**
         * Add participants to the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         * @param participants The uri list of the participants.
         */
        addParticipants: function (success, error, id, participants) {
            exec(success, error, "KandyPlugin", "group:addParticipants", [id, participants]);
        }

    },

    //*** PRESENCE SERVICE ***//
    presence: {

        /**
         * Register listener for presence's callbacks/notifications.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param userList The id list of users.
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
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getCountryInfo: function (success, error) {
            exec(success, error, "KandyPlugin", "location:getCountryInfo", []);
        },

        /**
         * Get current location.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getCurrentLocation: function (success, error) {
            exec(success, error, "KandyPlugin", "location:getCurrentLocation", []);
        }
    },

    //*** PUSH SERVICE ***//
    push: {

        /**
         * Enable the push service.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        enable: function (success, error) {
            exec(success, error, "KandyPlugin", "push:enable", []);
        },

        /**
         * Disable the push service.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        disable: function (success, error) {
            exec(success, error, "KandyPlugin", "push:disable", []);
        }
    },

    //*** ADDRESS BOOK SERVICE ***//
    addressBook: {

        /**
         * Get the contacts list from user device.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param filters The {@link DeviceContactsFilter} array to use.
         */
        getDeviceContacts: function (success, error, filters) {
            exec(success, error, "KandyPlugin", "addressBook:getDeviceContacts", [filters]);
        },

        /**
         * Get the contacts list from the host domain.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getDomainContacts: function (success, error) {
            exec(success, error, "KandyPlugin", "addressBook:getDomainContacts", []);
        },

        /**
         * Get the filterd contacts list from the host domain.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param filter The {@link DomainContactFilter} to search.
         * @param searchString The search string.
         */
        getFilteredDomainDirectoryContacts: function (success, error, filter, searchString) {
            exec(success, error, "KandyPlugin", "addressBook:getFilteredDomainDirectoryContacts", [filter, searchString]);
        }
    }
};

module.exports = Kandy;

