
"use strict";

var exec = cordova.require('cordova/exec');

/**
 * Kandy PhoneGap Plugin interface.
 * See [README](https://github.com/Kandy-IO/kandy-phonegap/blob/master/doc/index.md) for more details.
 *
 * @author kodeplusdev
 * @version 1.3.2
 */
var Kandy = {

    //***** CONSTANT *****//

    ELEMENT_TAG: "kandy",

    Widget: {
        PROVISIONING: "provisioning",
        ACCESS: "access",
        CALL: "call",
        CHAT: "chat",
        GROUP: "group",
        PRESENCE: "presence"
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

    RecordType: {
        CONTACT: "CONTACT",
        GROUP: "GROUP"
    },

    CameraInfo: {
        FACING_FRONT: "FACING_FRONT",
        FACING_BACK: "FACING_BACK",
        UNKNOWN: "UNKNOWN"
    },

    PickerResult: {
        CONTACT_PICKER_RESULT: 1001,
        IMAGE_PICKER_RESULT: 1002,
        VIDEO_PICKER_RESULT: 1003,
        AUDIO_PICKER_RESULT: 1004,
        FILE_PICKER_RESULT: 1005
    },

    presenceType: {
        PRESENCETYPE_AWAY: "Away",
        PRESENCETYPE_IDLE: "Idle",
        PRESENCETYPE_OTHER: "Other",
        PRESENCETYPE_UNKOWN: "Unknown",
        PRESENCETYPE_OUTOFLUNCH: "Out To Lunch",
        PRESENCETYPE_BUSY: "Busy",
        PRESENCETYPE_ONVACATION: "On Vacation",
        PRESENCETYPE_BERIGHTBACK: "Be Right Back",
        PRESENCETYPE_ONTHEPHONE: "On the Phone",
        PRESENCETYPE_ACTIVE: "Active",
        PRESENCETYPE_INACTIVE: "Inactive",
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

    // Presence listeners
    onPresenceArrived: function (args) {
    },


    //*** LOGIC ***//

    _messageContainers: [],

    videoView: {
        top: 400,
        left: 86,
        width: 596,
        height: 596
    },

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

        if (config.apiKey != undefined && config.apiSecret != undefined)
            this.setKey(config.apiKey, config.apiSecret);

        if (config.hostUrl != undefined)
            this.setHostUrl(config.hostUrl);

        if (config.videoView != undefined)
            Kandy.videoView = config.videoView;


        var callback = function (args) {
            console.log(args);
        };

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
                Kandy._addMessagetoContainers(args.data);
                break;
            case "onChatDelivered":
                // TODO: not complete yet
                Kandy._makeToast("Message delivered");
                break;
            case "onChatMediaAutoDownloadProgress":
                if (args.data.process == 0) {
                    Kandy._makeToast("Downloading a attachment...");
                }
                break;
            case "onChatMediaAutoDownloadSucceded":
                Kandy._addMessagetoContainers(args.data);
                Kandy._makeToast(args.data.message.message.content_name + " saved at " + args.data.uri);
                break;
            default :
        }
    },

    /**
     * Default call service notification callback.
     *
     * @param args The callback parameter.
     * @private
     */
    _callServiceNotificationPluginCallback: function (args) {
        switch (args.action) {
            case "onIncomingCall":
                Kandy._incomingCallWidget(args.data.callee.uri);
                break;
            case "onCallStateChanged":
            {
                var state = args.data.state;
                if (state == Kandy.CallState.TERMINATED) {
                    var modal = document.getElementById(args.data.callee.uri + '-talking-modal');
                    if (modal != null) modal.remove();

                    modal = document.getElementById(args.data.callee.uri + '-incoming-modal');
                    if (modal != null) modal.remove();
                }
                break;
            }
            default:
                break;
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
        exec(this._notificationCallback, null, "KandyPlugin", "presenceServiceNotificationCallback", []);

        exec(this._chatServiceNotificationPluginCallback, null, "KandyPlugin", "chatServiceNotificationPluginCallback", []);
        exec(this._callServiceNotificationPluginCallback, null, "KandyPlugin", "callServiceNotificationPluginCallback", []);
    },

    /**
     * Load plugin stylesheets and javascripts.
     * @private
     */
    _loadPluginResources: function () {
        var css = '.materialize-red.lighten-5{background-color:#fdeaeb !important}.materialize-red-text.text-lighten-5{color:#fdeaeb !important}.materialize-red.lighten-4{background-color:#f8c1c3 !important}.materialize-red-text.text-lighten-4{color:#f8c1c3 !important}.materialize-red.lighten-3{background-color:#f3989b !important}.materialize-red-text.text-lighten-3{color:#f3989b !important}.materialize-red.lighten-2{background-color:#ee6e73 !important}.materialize-red-text.text-lighten-2{color:#ee6e73 !important}.materialize-red.lighten-1{background-color:#ea454b !important}.materialize-red-text.text-lighten-1{color:#ea454b !important}.materialize-red{background-color:#e51c23 !important}.materialize-red-text{color:#e51c23 !important}.materialize-red.darken-1{background-color:#d0181e !important}.materialize-red-text.text-darken-1{color:#d0181e !important}.materialize-red.darken-2{background-color:#b9151b !important}.materialize-red-text.text-darken-2{color:#b9151b !important}.materialize-red.darken-3{background-color:#a21318 !important}.materialize-red-text.text-darken-3{color:#a21318 !important}.materialize-red.darken-4{background-color:#8b1014 !important}.materialize-red-text.text-darken-4{color:#8b1014 !important}.red.lighten-5{background-color:#FFEBEE !important}.red-text.text-lighten-5{color:#FFEBEE !important}.red.lighten-4{background-color:#FFCDD2 !important}.red-text.text-lighten-4{color:#FFCDD2 !important}.red.lighten-3{background-color:#EF9A9A !important}.red-text.text-lighten-3{color:#EF9A9A !important}.red.lighten-2{background-color:#E57373 !important}.red-text.text-lighten-2{color:#E57373 !important}.red.lighten-1{background-color:#EF5350 !important}.red-text.text-lighten-1{color:#EF5350 !important}.red{background-color:#F44336 !important}.red-text{color:#F44336 !important}.red.darken-1{background-color:#E53935 !important}.red-text.text-darken-1{color:#E53935 !important}.red.darken-2{background-color:#D32F2F !important}.red-text.text-darken-2{color:#D32F2F !important}.red.darken-3{background-color:#C62828 !important}.red-text.text-darken-3{color:#C62828 !important}.red.darken-4{background-color:#B71C1C !important}.red-text.text-darken-4{color:#B71C1C !important}.red.accent-1{background-color:#FF8A80 !important}.red-text.text-accent-1{color:#FF8A80 !important}.red.accent-2{background-color:#FF5252 !important}.red-text.text-accent-2{color:#FF5252 !important}.red.accent-3{background-color:#FF1744 !important}.red-text.text-accent-3{color:#FF1744 !important}.red.accent-4{background-color:#D50000 !important}.red-text.text-accent-4{color:#D50000 !important}.pink.lighten-5{background-color:#fce4ec !important}.pink-text.text-lighten-5{color:#fce4ec !important}.pink.lighten-4{background-color:#f8bbd0 !important}.pink-text.text-lighten-4{color:#f8bbd0 !important}.pink.lighten-3{background-color:#f48fb1 !important}.pink-text.text-lighten-3{color:#f48fb1 !important}.pink.lighten-2{background-color:#f06292 !important}.pink-text.text-lighten-2{color:#f06292 !important}.pink.lighten-1{background-color:#ec407a !important}.pink-text.text-lighten-1{color:#ec407a !important}.pink{background-color:#e91e63 !important}.pink-text{color:#e91e63 !important}.pink.darken-1{background-color:#d81b60 !important}.pink-text.text-darken-1{color:#d81b60 !important}.pink.darken-2{background-color:#c2185b !important}.pink-text.text-darken-2{color:#c2185b !important}.pink.darken-3{background-color:#ad1457 !important}.pink-text.text-darken-3{color:#ad1457 !important}.pink.darken-4{background-color:#880e4f !important}.pink-text.text-darken-4{color:#880e4f !important}.pink.accent-1{background-color:#ff80ab !important}.pink-text.text-accent-1{color:#ff80ab !important}.pink.accent-2{background-color:#ff4081 !important}.pink-text.text-accent-2{color:#ff4081 !important}.pink.accent-3{background-color:#f50057 !important}.pink-text.text-accent-3{color:#f50057 !important}.pink.accent-4{background-color:#c51162 !important}.pink-text.text-accent-4{color:#c51162 !important}.purple.lighten-5{background-color:#f3e5f5 !important}.purple-text.text-lighten-5{color:#f3e5f5 !important}.purple.lighten-4{background-color:#e1bee7 !important}.purple-text.text-lighten-4{color:#e1bee7 !important}.purple.lighten-3{background-color:#ce93d8 !important}.purple-text.text-lighten-3{color:#ce93d8 !important}.purple.lighten-2{background-color:#ba68c8 !important}.purple-text.text-lighten-2{color:#ba68c8 !important}.purple.lighten-1{background-color:#ab47bc !important}.purple-text.text-lighten-1{color:#ab47bc !important}.purple{background-color:#9c27b0 !important}.purple-text{color:#9c27b0 !important}.purple.darken-1{background-color:#8e24aa !important}.purple-text.text-darken-1{color:#8e24aa !important}.purple.darken-2{background-color:#7b1fa2 !important}.purple-text.text-darken-2{color:#7b1fa2 !important}.purple.darken-3{background-color:#6a1b9a !important}.purple-text.text-darken-3{color:#6a1b9a !important}.purple.darken-4{background-color:#4a148c !important}.purple-text.text-darken-4{color:#4a148c !important}.purple.accent-1{background-color:#ea80fc !important}.purple-text.text-accent-1{color:#ea80fc !important}.purple.accent-2{background-color:#e040fb !important}.purple-text.text-accent-2{color:#e040fb !important}.purple.accent-3{background-color:#d500f9 !important}.purple-text.text-accent-3{color:#d500f9 !important}.purple.accent-4{background-color:#aa00ff !important}.purple-text.text-accent-4{color:#aa00ff !important}.deep-purple.lighten-5{background-color:#ede7f6 !important}.deep-purple-text.text-lighten-5{color:#ede7f6 !important}.deep-purple.lighten-4{background-color:#d1c4e9 !important}.deep-purple-text.text-lighten-4{color:#d1c4e9 !important}.deep-purple.lighten-3{background-color:#b39ddb !important}.deep-purple-text.text-lighten-3{color:#b39ddb !important}.deep-purple.lighten-2{background-color:#9575cd !important}.deep-purple-text.text-lighten-2{color:#9575cd !important}.deep-purple.lighten-1{background-color:#7e57c2 !important}.deep-purple-text.text-lighten-1{color:#7e57c2 !important}.deep-purple{background-color:#673ab7 !important}.deep-purple-text{color:#673ab7 !important}.deep-purple.darken-1{background-color:#5e35b1 !important}.deep-purple-text.text-darken-1{color:#5e35b1 !important}.deep-purple.darken-2{background-color:#512da8 !important}.deep-purple-text.text-darken-2{color:#512da8 !important}.deep-purple.darken-3{background-color:#4527a0 !important}.deep-purple-text.text-darken-3{color:#4527a0 !important}.deep-purple.darken-4{background-color:#311b92 !important}.deep-purple-text.text-darken-4{color:#311b92 !important}.deep-purple.accent-1{background-color:#b388ff !important}.deep-purple-text.text-accent-1{color:#b388ff !important}.deep-purple.accent-2{background-color:#7c4dff !important}.deep-purple-text.text-accent-2{color:#7c4dff !important}.deep-purple.accent-3{background-color:#651fff !important}.deep-purple-text.text-accent-3{color:#651fff !important}.deep-purple.accent-4{background-color:#6200ea !important}.deep-purple-text.text-accent-4{color:#6200ea !important}.indigo.lighten-5{background-color:#e8eaf6 !important}.indigo-text.text-lighten-5{color:#e8eaf6 !important}.indigo.lighten-4{background-color:#c5cae9 !important}.indigo-text.text-lighten-4{color:#c5cae9 !important}.indigo.lighten-3{background-color:#9fa8da !important}.indigo-text.text-lighten-3{color:#9fa8da !important}.indigo.lighten-2{background-color:#7986cb !important}.indigo-text.text-lighten-2{color:#7986cb !important}.indigo.lighten-1{background-color:#5c6bc0 !important}.indigo-text.text-lighten-1{color:#5c6bc0 !important}.indigo{background-color:#3f51b5 !important}.indigo-text{color:#3f51b5 !important}.indigo.darken-1{background-color:#3949ab !important}.indigo-text.text-darken-1{color:#3949ab !important}.indigo.darken-2{background-color:#303f9f !important}.indigo-text.text-darken-2{color:#303f9f !important}.indigo.darken-3{background-color:#283593 !important}.indigo-text.text-darken-3{color:#283593 !important}.indigo.darken-4{background-color:#1a237e !important}.indigo-text.text-darken-4{color:#1a237e !important}.indigo.accent-1{background-color:#8c9eff !important}.indigo-text.text-accent-1{color:#8c9eff !important}.indigo.accent-2{background-color:#536dfe !important}.indigo-text.text-accent-2{color:#536dfe !important}.indigo.accent-3{background-color:#3d5afe !important}.indigo-text.text-accent-3{color:#3d5afe !important}.indigo.accent-4{background-color:#304ffe !important}.indigo-text.text-accent-4{color:#304ffe !important}.blue.lighten-5{background-color:#E3F2FD !important}.blue-text.text-lighten-5{color:#E3F2FD !important}.blue.lighten-4{background-color:#BBDEFB !important}.blue-text.text-lighten-4{color:#BBDEFB !important}.blue.lighten-3{background-color:#90CAF9 !important}.blue-text.text-lighten-3{color:#90CAF9 !important}.blue.lighten-2{background-color:#64B5F6 !important}.blue-text.text-lighten-2{color:#64B5F6 !important}.blue.lighten-1{background-color:#42A5F5 !important}.blue-text.text-lighten-1{color:#42A5F5 !important}.blue{background-color:#2196F3 !important}.blue-text{color:#2196F3 !important}.blue.darken-1{background-color:#1E88E5 !important}.blue-text.text-darken-1{color:#1E88E5 !important}.blue.darken-2{background-color:#1976D2 !important}.blue-text.text-darken-2{color:#1976D2 !important}.blue.darken-3{background-color:#1565C0 !important}.blue-text.text-darken-3{color:#1565C0 !important}.blue.darken-4{background-color:#0D47A1 !important}.blue-text.text-darken-4{color:#0D47A1 !important}.blue.accent-1{background-color:#82B1FF !important}.blue-text.text-accent-1{color:#82B1FF !important}.blue.accent-2{background-color:#448AFF !important}.blue-text.text-accent-2{color:#448AFF !important}.blue.accent-3{background-color:#2979FF !important}.blue-text.text-accent-3{color:#2979FF !important}.blue.accent-4{background-color:#2962FF !important}.blue-text.text-accent-4{color:#2962FF !important}.light-blue.lighten-5{background-color:#e1f5fe !important}.light-blue-text.text-lighten-5{color:#e1f5fe !important}.light-blue.lighten-4{background-color:#b3e5fc !important}.light-blue-text.text-lighten-4{color:#b3e5fc !important}.light-blue.lighten-3{background-color:#81d4fa !important}.light-blue-text.text-lighten-3{color:#81d4fa !important}.light-blue.lighten-2{background-color:#4fc3f7 !important}.light-blue-text.text-lighten-2{color:#4fc3f7 !important}.light-blue.lighten-1{background-color:#29b6f6 !important}.light-blue-text.text-lighten-1{color:#29b6f6 !important}.light-blue{background-color:#03a9f4 !important}.light-blue-text{color:#03a9f4 !important}.light-blue.darken-1{background-color:#039be5 !important}.light-blue-text.text-darken-1{color:#039be5 !important}.light-blue.darken-2{background-color:#0288d1 !important}.light-blue-text.text-darken-2{color:#0288d1 !important}.light-blue.darken-3{background-color:#0277bd !important}.light-blue-text.text-darken-3{color:#0277bd !important}.light-blue.darken-4{background-color:#01579b !important}.light-blue-text.text-darken-4{color:#01579b !important}.light-blue.accent-1{background-color:#80d8ff !important}.light-blue-text.text-accent-1{color:#80d8ff !important}.light-blue.accent-2{background-color:#40c4ff !important}.light-blue-text.text-accent-2{color:#40c4ff !important}.light-blue.accent-3{background-color:#00b0ff !important}.light-blue-text.text-accent-3{color:#00b0ff !important}.light-blue.accent-4{background-color:#0091ea !important}.light-blue-text.text-accent-4{color:#0091ea !important}.cyan.lighten-5{background-color:#e0f7fa !important}.cyan-text.text-lighten-5{color:#e0f7fa !important}.cyan.lighten-4{background-color:#b2ebf2 !important}.cyan-text.text-lighten-4{color:#b2ebf2 !important}.cyan.lighten-3{background-color:#80deea !important}.cyan-text.text-lighten-3{color:#80deea !important}.cyan.lighten-2{background-color:#4dd0e1 !important}.cyan-text.text-lighten-2{color:#4dd0e1 !important}.cyan.lighten-1{background-color:#26c6da !important}.cyan-text.text-lighten-1{color:#26c6da !important}.cyan{background-color:#00bcd4 !important}.cyan-text{color:#00bcd4 !important}.cyan.darken-1{background-color:#00acc1 !important}.cyan-text.text-darken-1{color:#00acc1 !important}.cyan.darken-2{background-color:#0097a7 !important}.cyan-text.text-darken-2{color:#0097a7 !important}.cyan.darken-3{background-color:#00838f !important}.cyan-text.text-darken-3{color:#00838f !important}.cyan.darken-4{background-color:#006064 !important}.cyan-text.text-darken-4{color:#006064 !important}.cyan.accent-1{background-color:#84ffff !important}.cyan-text.text-accent-1{color:#84ffff !important}.cyan.accent-2{background-color:#18ffff !important}.cyan-text.text-accent-2{color:#18ffff !important}.cyan.accent-3{background-color:#00e5ff !important}.cyan-text.text-accent-3{color:#00e5ff !important}.cyan.accent-4{background-color:#00b8d4 !important}.cyan-text.text-accent-4{color:#00b8d4 !important}.teal.lighten-5{background-color:#e0f2f1 !important}.teal-text.text-lighten-5{color:#e0f2f1 !important}.teal.lighten-4{background-color:#b2dfdb !important}.teal-text.text-lighten-4{color:#b2dfdb !important}.teal.lighten-3{background-color:#80cbc4 !important}.teal-text.text-lighten-3{color:#80cbc4 !important}.teal.lighten-2{background-color:#4db6ac !important}.teal-text.text-lighten-2{color:#4db6ac !important}.teal.lighten-1{background-color:#26a69a !important}.teal-text.text-lighten-1{color:#26a69a !important}.teal{background-color:#009688 !important}.teal-text{color:#009688 !important}.teal.darken-1{background-color:#00897b !important}.teal-text.text-darken-1{color:#00897b !important}.teal.darken-2{background-color:#00796b !important}.teal-text.text-darken-2{color:#00796b !important}.teal.darken-3{background-color:#00695c !important}.teal-text.text-darken-3{color:#00695c !important}.teal.darken-4{background-color:#004d40 !important}.teal-text.text-darken-4{color:#004d40 !important}.teal.accent-1{background-color:#a7ffeb !important}.teal-text.text-accent-1{color:#a7ffeb !important}.teal.accent-2{background-color:#64ffda !important}.teal-text.text-accent-2{color:#64ffda !important}.teal.accent-3{background-color:#1de9b6 !important}.teal-text.text-accent-3{color:#1de9b6 !important}.teal.accent-4{background-color:#00bfa5 !important}.teal-text.text-accent-4{color:#00bfa5 !important}.green.lighten-5{background-color:#E8F5E9 !important}.green-text.text-lighten-5{color:#E8F5E9 !important}.green.lighten-4{background-color:#C8E6C9 !important}.green-text.text-lighten-4{color:#C8E6C9 !important}.green.lighten-3{background-color:#A5D6A7 !important}.green-text.text-lighten-3{color:#A5D6A7 !important}.green.lighten-2{background-color:#81C784 !important}.green-text.text-lighten-2{color:#81C784 !important}.green.lighten-1{background-color:#66BB6A !important}.green-text.text-lighten-1{color:#66BB6A !important}.green{background-color:#4CAF50 !important}.green-text{color:#4CAF50 !important}.green.darken-1{background-color:#43A047 !important}.green-text.text-darken-1{color:#43A047 !important}.green.darken-2{background-color:#388E3C !important}.green-text.text-darken-2{color:#388E3C !important}.green.darken-3{background-color:#2E7D32 !important}.green-text.text-darken-3{color:#2E7D32 !important}.green.darken-4{background-color:#1B5E20 !important}.green-text.text-darken-4{color:#1B5E20 !important}.green.accent-1{background-color:#B9F6CA !important}.green-text.text-accent-1{color:#B9F6CA !important}.green.accent-2{background-color:#69F0AE !important}.green-text.text-accent-2{color:#69F0AE !important}.green.accent-3{background-color:#00E676 !important}.green-text.text-accent-3{color:#00E676 !important}.green.accent-4{background-color:#00C853 !important}.green-text.text-accent-4{color:#00C853 !important}.light-green.lighten-5{background-color:#f1f8e9 !important}.light-green-text.text-lighten-5{color:#f1f8e9 !important}.light-green.lighten-4{background-color:#dcedc8 !important}.light-green-text.text-lighten-4{color:#dcedc8 !important}.light-green.lighten-3{background-color:#c5e1a5 !important}.light-green-text.text-lighten-3{color:#c5e1a5 !important}.light-green.lighten-2{background-color:#aed581 !important}.light-green-text.text-lighten-2{color:#aed581 !important}.light-green.lighten-1{background-color:#9ccc65 !important}.light-green-text.text-lighten-1{color:#9ccc65 !important}.light-green{background-color:#8bc34a !important}.light-green-text{color:#8bc34a !important}.light-green.darken-1{background-color:#7cb342 !important}.light-green-text.text-darken-1{color:#7cb342 !important}.light-green.darken-2{background-color:#689f38 !important}.light-green-text.text-darken-2{color:#689f38 !important}.light-green.darken-3{background-color:#558b2f !important}.light-green-text.text-darken-3{color:#558b2f !important}.light-green.darken-4{background-color:#33691e !important}.light-green-text.text-darken-4{color:#33691e !important}.light-green.accent-1{background-color:#ccff90 !important}.light-green-text.text-accent-1{color:#ccff90 !important}.light-green.accent-2{background-color:#b2ff59 !important}.light-green-text.text-accent-2{color:#b2ff59 !important}.light-green.accent-3{background-color:#76ff03 !important}.light-green-text.text-accent-3{color:#76ff03 !important}.light-green.accent-4{background-color:#64dd17 !important}.light-green-text.text-accent-4{color:#64dd17 !important}.lime.lighten-5{background-color:#f9fbe7 !important}.lime-text.text-lighten-5{color:#f9fbe7 !important}.lime.lighten-4{background-color:#f0f4c3 !important}.lime-text.text-lighten-4{color:#f0f4c3 !important}.lime.lighten-3{background-color:#e6ee9c !important}.lime-text.text-lighten-3{color:#e6ee9c !important}.lime.lighten-2{background-color:#dce775 !important}.lime-text.text-lighten-2{color:#dce775 !important}.lime.lighten-1{background-color:#d4e157 !important}.lime-text.text-lighten-1{color:#d4e157 !important}.lime{background-color:#cddc39 !important}.lime-text{color:#cddc39 !important}.lime.darken-1{background-color:#c0ca33 !important}.lime-text.text-darken-1{color:#c0ca33 !important}.lime.darken-2{background-color:#afb42b !important}.lime-text.text-darken-2{color:#afb42b !important}.lime.darken-3{background-color:#9e9d24 !important}.lime-text.text-darken-3{color:#9e9d24 !important}.lime.darken-4{background-color:#827717 !important}.lime-text.text-darken-4{color:#827717 !important}.lime.accent-1{background-color:#f4ff81 !important}.lime-text.text-accent-1{color:#f4ff81 !important}.lime.accent-2{background-color:#eeff41 !important}.lime-text.text-accent-2{color:#eeff41 !important}.lime.accent-3{background-color:#c6ff00 !important}.lime-text.text-accent-3{color:#c6ff00 !important}.lime.accent-4{background-color:#aeea00 !important}.lime-text.text-accent-4{color:#aeea00 !important}.yellow.lighten-5{background-color:#fffde7 !important}.yellow-text.text-lighten-5{color:#fffde7 !important}.yellow.lighten-4{background-color:#fff9c4 !important}.yellow-text.text-lighten-4{color:#fff9c4 !important}.yellow.lighten-3{background-color:#fff59d !important}.yellow-text.text-lighten-3{color:#fff59d !important}.yellow.lighten-2{background-color:#fff176 !important}.yellow-text.text-lighten-2{color:#fff176 !important}.yellow.lighten-1{background-color:#ffee58 !important}.yellow-text.text-lighten-1{color:#ffee58 !important}.yellow{background-color:#ffeb3b !important}.yellow-text{color:#ffeb3b !important}.yellow.darken-1{background-color:#fdd835 !important}.yellow-text.text-darken-1{color:#fdd835 !important}.yellow.darken-2{background-color:#fbc02d !important}.yellow-text.text-darken-2{color:#fbc02d !important}.yellow.darken-3{background-color:#f9a825 !important}.yellow-text.text-darken-3{color:#f9a825 !important}.yellow.darken-4{background-color:#f57f17 !important}.yellow-text.text-darken-4{color:#f57f17 !important}.yellow.accent-1{background-color:#ffff8d !important}.yellow-text.text-accent-1{color:#ffff8d !important}.yellow.accent-2{background-color:#ffff00 !important}.yellow-text.text-accent-2{color:#ffff00 !important}.yellow.accent-3{background-color:#ffea00 !important}.yellow-text.text-accent-3{color:#ffea00 !important}.yellow.accent-4{background-color:#ffd600 !important}.yellow-text.text-accent-4{color:#ffd600 !important}.amber.lighten-5{background-color:#fff8e1 !important}.amber-text.text-lighten-5{color:#fff8e1 !important}.amber.lighten-4{background-color:#ffecb3 !important}.amber-text.text-lighten-4{color:#ffecb3 !important}.amber.lighten-3{background-color:#ffe082 !important}.amber-text.text-lighten-3{color:#ffe082 !important}.amber.lighten-2{background-color:#ffd54f !important}.amber-text.text-lighten-2{color:#ffd54f !important}.amber.lighten-1{background-color:#ffca28 !important}.amber-text.text-lighten-1{color:#ffca28 !important}.amber{background-color:#ffc107 !important}.amber-text{color:#ffc107 !important}.amber.darken-1{background-color:#ffb300 !important}.amber-text.text-darken-1{color:#ffb300 !important}.amber.darken-2{background-color:#ffa000 !important}.amber-text.text-darken-2{color:#ffa000 !important}.amber.darken-3{background-color:#ff8f00 !important}.amber-text.text-darken-3{color:#ff8f00 !important}.amber.darken-4{background-color:#ff6f00 !important}.amber-text.text-darken-4{color:#ff6f00 !important}.amber.accent-1{background-color:#ffe57f !important}.amber-text.text-accent-1{color:#ffe57f !important}.amber.accent-2{background-color:#ffd740 !important}.amber-text.text-accent-2{color:#ffd740 !important}.amber.accent-3{background-color:#ffc400 !important}.amber-text.text-accent-3{color:#ffc400 !important}.amber.accent-4{background-color:#ffab00 !important}.amber-text.text-accent-4{color:#ffab00 !important}.orange.lighten-5{background-color:#fff3e0 !important}.orange-text.text-lighten-5{color:#fff3e0 !important}.orange.lighten-4{background-color:#ffe0b2 !important}.orange-text.text-lighten-4{color:#ffe0b2 !important}.orange.lighten-3{background-color:#ffcc80 !important}.orange-text.text-lighten-3{color:#ffcc80 !important}.orange.lighten-2{background-color:#ffb74d !important}.orange-text.text-lighten-2{color:#ffb74d !important}.orange.lighten-1{background-color:#ffa726 !important}.orange-text.text-lighten-1{color:#ffa726 !important}.orange{background-color:#ff9800 !important}.orange-text{color:#ff9800 !important}.orange.darken-1{background-color:#fb8c00 !important}.orange-text.text-darken-1{color:#fb8c00 !important}.orange.darken-2{background-color:#f57c00 !important}.orange-text.text-darken-2{color:#f57c00 !important}.orange.darken-3{background-color:#ef6c00 !important}.orange-text.text-darken-3{color:#ef6c00 !important}.orange.darken-4{background-color:#e65100 !important}.orange-text.text-darken-4{color:#e65100 !important}.orange.accent-1{background-color:#ffd180 !important}.orange-text.text-accent-1{color:#ffd180 !important}.orange.accent-2{background-color:#ffab40 !important}.orange-text.text-accent-2{color:#ffab40 !important}.orange.accent-3{background-color:#ff9100 !important}.orange-text.text-accent-3{color:#ff9100 !important}.orange.accent-4{background-color:#ff6d00 !important}.orange-text.text-accent-4{color:#ff6d00 !important}.deep-orange.lighten-5{background-color:#fbe9e7 !important}.deep-orange-text.text-lighten-5{color:#fbe9e7 !important}.deep-orange.lighten-4{background-color:#ffccbc !important}.deep-orange-text.text-lighten-4{color:#ffccbc !important}.deep-orange.lighten-3{background-color:#ffab91 !important}.deep-orange-text.text-lighten-3{color:#ffab91 !important}.deep-orange.lighten-2{background-color:#ff8a65 !important}.deep-orange-text.text-lighten-2{color:#ff8a65 !important}.deep-orange.lighten-1{background-color:#ff7043 !important}.deep-orange-text.text-lighten-1{color:#ff7043 !important}.deep-orange{background-color:#ff5722 !important}.deep-orange-text{color:#ff5722 !important}.deep-orange.darken-1{background-color:#f4511e !important}.deep-orange-text.text-darken-1{color:#f4511e !important}.deep-orange.darken-2{background-color:#e64a19 !important}.deep-orange-text.text-darken-2{color:#e64a19 !important}.deep-orange.darken-3{background-color:#d84315 !important}.deep-orange-text.text-darken-3{color:#d84315 !important}.deep-orange.darken-4{background-color:#bf360c !important}.deep-orange-text.text-darken-4{color:#bf360c !important}.deep-orange.accent-1{background-color:#ff9e80 !important}.deep-orange-text.text-accent-1{color:#ff9e80 !important}.deep-orange.accent-2{background-color:#ff6e40 !important}.deep-orange-text.text-accent-2{color:#ff6e40 !important}.deep-orange.accent-3{background-color:#ff3d00 !important}.deep-orange-text.text-accent-3{color:#ff3d00 !important}.deep-orange.accent-4{background-color:#dd2c00 !important}.deep-orange-text.text-accent-4{color:#dd2c00 !important}.brown.lighten-5{background-color:#efebe9 !important}.brown-text.text-lighten-5{color:#efebe9 !important}.brown.lighten-4{background-color:#d7ccc8 !important}.brown-text.text-lighten-4{color:#d7ccc8 !important}.brown.lighten-3{background-color:#bcaaa4 !important}.brown-text.text-lighten-3{color:#bcaaa4 !important}.brown.lighten-2{background-color:#a1887f !important}.brown-text.text-lighten-2{color:#a1887f !important}.brown.lighten-1{background-color:#8d6e63 !important}.brown-text.text-lighten-1{color:#8d6e63 !important}.brown{background-color:#795548 !important}.brown-text{color:#795548 !important}.brown.darken-1{background-color:#6d4c41 !important}.brown-text.text-darken-1{color:#6d4c41 !important}.brown.darken-2{background-color:#5d4037 !important}.brown-text.text-darken-2{color:#5d4037 !important}.brown.darken-3{background-color:#4e342e !important}.brown-text.text-darken-3{color:#4e342e !important}.brown.darken-4{background-color:#3e2723 !important}.brown-text.text-darken-4{color:#3e2723 !important}.blue-grey.lighten-5{background-color:#eceff1 !important}.blue-grey-text.text-lighten-5{color:#eceff1 !important}.blue-grey.lighten-4{background-color:#cfd8dc !important}.blue-grey-text.text-lighten-4{color:#cfd8dc !important}.blue-grey.lighten-3{background-color:#b0bec5 !important}.blue-grey-text.text-lighten-3{color:#b0bec5 !important}.blue-grey.lighten-2{background-color:#90a4ae !important}.blue-grey-text.text-lighten-2{color:#90a4ae !important}.blue-grey.lighten-1{background-color:#78909c !important}.blue-grey-text.text-lighten-1{color:#78909c !important}.blue-grey{background-color:#607d8b !important}.blue-grey-text{color:#607d8b !important}.blue-grey.darken-1{background-color:#546e7a !important}.blue-grey-text.text-darken-1{color:#546e7a !important}.blue-grey.darken-2{background-color:#455a64 !important}.blue-grey-text.text-darken-2{color:#455a64 !important}.blue-grey.darken-3{background-color:#37474f !important}.blue-grey-text.text-darken-3{color:#37474f !important}.blue-grey.darken-4{background-color:#263238 !important}.blue-grey-text.text-darken-4{color:#263238 !important}.grey.lighten-5{background-color:#fafafa !important}.grey-text.text-lighten-5{color:#fafafa !important}.grey.lighten-4{background-color:#f5f5f5 !important}.grey-text.text-lighten-4{color:#f5f5f5 !important}.grey.lighten-3{background-color:#eeeeee !important}.grey-text.text-lighten-3{color:#eeeeee !important}.grey.lighten-2{background-color:#e0e0e0 !important}.grey-text.text-lighten-2{color:#e0e0e0 !important}.grey.lighten-1{background-color:#bdbdbd !important}.grey-text.text-lighten-1{color:#bdbdbd !important}.grey{background-color:#9e9e9e !important}.grey-text{color:#9e9e9e !important}.grey.darken-1{background-color:#757575 !important}.grey-text.text-darken-1{color:#757575 !important}.grey.darken-2{background-color:#616161 !important}.grey-text.text-darken-2{color:#616161 !important}.grey.darken-3{background-color:#424242 !important}.grey-text.text-darken-3{color:#424242 !important}.grey.darken-4{background-color:#212121 !important}.grey-text.text-darken-4{color:#212121 !important}.shades.black{background-color:#000000 !important}.shades-text.text-black{color:#000000 !important}.shades.white{background-color:#FFFFFF !important}.shades-text.text-white{color:#FFFFFF !important}.shades.transparent{background-color:transparent !important}.shades-text.text-transparent{color:transparent !important}.black{background-color:#000000 !important}.black-text{color:#000000 !important}.white{background-color:#FFFFFF !important}.white-text{color:#FFFFFF !important}.transparent{background-color:transparent !important}.transparent-text{color:transparent !important}kandy html{box-sizing:border-box}kandy *,kandy *:before,kandy *:after{box-sizing:inherit}kandy ul{list-style-type:none}kandy a{color:#039be5;text-decoration:none;-webkit-tap-highlight-color:transparent}kandy .valign-wrapper{display:-webkit-box;display:-moz-box;display:-ms-flexbox;display:-webkit-flex;display:flex;-webkit-flex-align:center;-ms-flex-align:center;-webkit-align-items:center;align-items:center}kandy .valign-wrapper .valign{display:block}kandy ul{padding:0}kandy ul li{list-style-type:none}kandy .clearfix{clear:both}kandy .z-depth-1,kandy .btn,kandy .btn-large,kandy .btn-floating{-webkit-box-shadow:0 2px 5px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12);-moz-box-shadow:0 2px 5px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12);box-shadow:0 2px 5px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12)}kandy .z-depth-1-half,kandy .btn:hover,kandy .btn-large:hover,kandy .btn-floating:hover{-webkit-box-shadow:0 5px 11px 0 rgba(0,0,0,0.18),0 4px 15px 0 rgba(0,0,0,0.15);-moz-box-shadow:0 5px 11px 0 rgba(0,0,0,0.18),0 4px 15px 0 rgba(0,0,0,0.15);box-shadow:0 5px 11px 0 rgba(0,0,0,0.18),0 4px 15px 0 rgba(0,0,0,0.15)}kandy .z-depth-2{-webkit-box-shadow:0 8px 17px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19);-moz-box-shadow:0 8px 17px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19);box-shadow:0 8px 17px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19)}kandy .z-depth-3{-webkit-box-shadow:0 12px 15px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19);-moz-box-shadow:0 12px 15px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19);box-shadow:0 12px 15px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19)}kandy .z-depth-4,kandy .modal{-webkit-box-shadow:0 16px 28px 0 rgba(0,0,0,0.22),0 25px 55px 0 rgba(0,0,0,0.21);-moz-box-shadow:0 16px 28px 0 rgba(0,0,0,0.22),0 25px 55px 0 rgba(0,0,0,0.21);box-shadow:0 16px 28px 0 rgba(0,0,0,0.22),0 25px 55px 0 rgba(0,0,0,0.21)}kandy .z-depth-5{-webkit-box-shadow:0 27px 24px 0 rgba(0,0,0,0.2),0 40px 77px 0 rgba(0,0,0,0.22);-moz-box-shadow:0 27px 24px 0 rgba(0,0,0,0.2),0 40px 77px 0 rgba(0,0,0,0.22);box-shadow:0 27px 24px 0 rgba(0,0,0,0.2),0 40px 77px 0 rgba(0,0,0,0.22)}kandy .divider{height:1px;overflow:hidden;background-color:#e0e0e0}kandy blockquote{margin:20px 0;padding-left:1.5rem;border-left:5px solid #EF9A9A}kandy i{line-height:inherit}kandy i.left{float:left;margin-right:15px}kandy i.right{float:right;margin-left:15px}kandy i.tiny{font-size:1rem}kandy i.small{font-size:2rem}kandy i.medium{font-size:4rem}kandy i.large{font-size:6rem}kandy img.responsive-img,kandy video.responsive-video{max-width:100%;height:auto}kandy .pagination li{font-size:1.2rem;float:left;width:30px;height:30px;margin:0 10px;border-radius:2px;text-align:center}kandy .pagination li a{color:#444}kandy .pagination li.active a{color:#fff}kandy .pagination li.active{background-color:#ee6e73}kandy .pagination li.disabled a{color:#999}kandy .pagination li i{font-size:2rem;line-height:1.8rem}kandy .parallax-container{position:relative;overflow:hidden;height:500px}kandy .parallax{position:absolute;top:0;left:0;right:0;bottom:0;z-index:-1}kandy .parallax img{display:none;position:absolute;left:50%;bottom:0;min-width:100%;min-height:100%;-webkit-transform:translate3d(0, 0, 0);transform:translate3d(0, 0, 0);transform:translateX(-50%)}kandy .pin-top,kandy .pin-bottom{position:relative}kandy .pinned{position:fixed !important}kandy ul.staggered-list li{opacity:0}kandy .fade-in{opacity:0;transform-origin:0 50%}@media only screen and (max-width: 600px){kandy .hide-on-small-only,kandy .hide-on-small-and-down{display:none !important}}@media only screen and (max-width: 992px){kandy .hide-on-med-and-down{display:none !important}}@media only screen and (min-width: 601px){kandy .hide-on-med-and-up{display:none !important}}@media only screen and (min-width: 600px) and (max-width: 992px){kandy .hide-on-med-only{display:none !important}}@media only screen and (min-width: 993px){kandy .hide-on-large-only{display:none !important}}@media only screen and (min-width: 993px){kandy .show-on-large{display:initial !important}}@media only screen and (min-width: 600px) and (max-width: 992px){kandy .show-on-medium{display:initial !important}}@media only screen and (max-width: 600px){kandy .show-on-small{display:initial !important}}@media only screen and (min-width: 601px){kandy .show-on-medium-and-up{display:initial !important}}@media only screen and (max-width: 992px){kandy .show-on-medium-and-down{display:initial !important}}@media only screen and (max-width: 600px){kandy .center-on-small-only{text-align:center}}kandy table,kandy th,kandy td{border:none}kandy table{width:100%;display:table}kandy table.bordered tr{border-bottom:1px solid #d0d0d0}kandy table.striped tbody tr:nth-child(odd){background-color:#f2f2f2}kandy table.hoverable tbody tr{-webkit-transition:background-color 0.25s ease;-moz-transition:background-color 0.25s ease;-o-transition:background-color 0.25s ease;-ms-transition:background-color 0.25s ease;transition:background-color 0.25s ease}kandy table.hoverable tbody tr:hover{background-color:#f2f2f2}kandy table.centered thead tr th,kandy table.centered tbody tr td{text-align:center}kandy thead{border-bottom:1px solid #d0d0d0}kandy td,kandy th{padding:15px 5px;display:table-cell;text-align:left;vertical-align:middle;border-radius:2px}@media only screen and (max-width: 992px){kandy table.responsive-table{width:100%;border-collapse:collapse;border-spacing:0;display:block;position:relative}kandy table.responsive-table th,kandy table.responsive-table td{margin:0;vertical-align:top}kandy table.responsive-table th{text-align:left}kandy table.responsive-table thead{display:block;float:left}kandy table.responsive-table thead tr{display:block;padding:0 10px 0 0}kandy table.responsive-table thead tr th::before{content:"00a0"}kandy table.responsive-table tbody{display:block;width:auto;position:relative;overflow-x:auto;white-space:nowrap}kandy table.responsive-table tbody tr{display:inline-block;vertical-align:top}kandy table.responsive-table th{display:block;text-align:right}kandy table.responsive-table td{display:block;min-height:1.25em;text-align:left}kandy table.responsive-table tr{padding:0 10px}kandy table.responsive-table thead{border:0;border-right:1px solid #d0d0d0}kandy table.responsive-table.bordered th{border-bottom:0;border-left:0}kandy table.responsive-table.bordered td{border-left:0;border-right:0;border-bottom:0}kandy table.responsive-table.bordered tr{border:0}kandy table.responsive-table.bordered tbody tr{border-right:1px solid #d0d0d0}}kandy .collection{margin:0.5rem 0 1rem 0;border:1px solid #e0e0e0;border-radius:2px;overflow:hidden;position:relative}kandy .collection .collection-item{background-color:#fff;line-height:1.5rem;padding:10px 20px;margin:0;border-bottom:1px solid #e0e0e0}kandy .collection .collection-item.avatar{height:84px;padding-left:72px;position:relative}kandy .collection .collection-item.avatar .circle{position:absolute;width:42px;height:42px;overflow:hidden;left:15px;display:inline-block;vertical-align:middle}kandy .collection .collection-item.avatar i.circle{font-size:18px;line-height:42px;color:#fff;background-color:#999;text-align:center}kandy .collection .collection-item.avatar .title{font-size:16px}kandy .collection .collection-item.avatar p{margin:0}kandy .collection .collection-item.avatar .secondary-content{position:absolute;top:16px;right:16px}kandy .collection .collection-item:last-child{border-bottom:none}kandy .collection .collection-item.active{background-color:#26a69a;color:#eafaf9}kandy .collection a.collection-item{display:block;-webkit-transition:0.25s;-moz-transition:0.25s;-o-transition:0.25s;-ms-transition:0.25s;transition:0.25s;color:#26a69a}kandy .collection a.collection-item:not(.active):hover{background-color:#ddd}kandy .collection.with-header .collection-header{background-color:#fff;border-bottom:1px solid #e0e0e0;padding:10px 20px}kandy .collection.with-header .collection-item{padding-left:30px}kandy .secondary-content{float:right;color:#26a69a}kandy span.badge{min-width:3rem;padding:0 6px;text-align:center;font-size:1rem;line-height:inherit;color:#757575;position:absolute;right:15px;-webkit-box-sizing:border-box;-moz-box-sizing:border-box;box-sizing:border-box}kandy span.badge.new{font-weight:300;font-size:0.8rem;color:#fff;background-color:#26a69a;border-radius:2px}kandy span.badge.new:after{content:" new"}kandy .video-container{position:relative;padding-bottom:56.25%;padding-top:30px;height:0;overflow:hidden}kandy .video-container.no-controls{padding-top:0}kandy .video-container iframe,kandy .video-container object,kandy .video-container embed{position:absolute;top:0;left:0;width:100%;height:100%}kandy .progress{position:relative;height:4px;display:block;width:100%;background-color:#acece6;border-radius:2px;margin:0.5rem 0 1rem 0;overflow:hidden}kandy .progress .determinate{position:absolute;background-color:inherit;top:0;bottom:0;background-color:#26a69a;-webkit-transition:width 0.3s linear;-moz-transition:width 0.3s linear;-o-transition:width 0.3s linear;-ms-transition:width 0.3s linear;transition:width 0.3s linear}kandy .progress .indeterminate{background-color:#26a69a}kandy .progress .indeterminate:before{content:"""";position:absolute;background-color:inherit;top:0;left:0;bottom:0;will-change:left, right;-webkit-animation:indeterminate 2.1s cubic-bezier(0.65, 0.815, 0.735, 0.395) infinite;-moz-animation:indeterminate 2.1s cubic-bezier(0.65, 0.815, 0.735, 0.395) infinite;-ms-animation:indeterminate 2.1s cubic-bezier(0.65, 0.815, 0.735, 0.395) infinite;-o-animation:indeterminate 2.1s cubic-bezier(0.65, 0.815, 0.735, 0.395) infinite;animation:indeterminate 2.1s cubic-bezier(0.65, 0.815, 0.735, 0.395) infinite}kandy .progress .indeterminate:after{content:"""";position:absolute;background-color:inherit;top:0;left:0;bottom:0;will-change:left, right;-webkit-animation:indeterminate-short 2.1s cubic-bezier(0.165, 0.84, 0.44, 1) infinite;-moz-animation:indeterminate-short 2.1s cubic-bezier(0.165, 0.84, 0.44, 1) infinite;-ms-animation:indeterminate-short 2.1s cubic-bezier(0.165, 0.84, 0.44, 1) infinite;-o-animation:indeterminate-short 2.1s cubic-bezier(0.165, 0.84, 0.44, 1) infinite;animation:indeterminate-short 2.1s cubic-bezier(0.165, 0.84, 0.44, 1) infinite;-webkit-animation-delay:1.15s;-moz-animation-delay:1.15s;-ms-animation-delay:1.15s;-o-animation-delay:1.15s;animation-delay:1.15s}@-webkit-keyframes indeterminate{0%{left:-35%;right:100%}60%{left:100%;right:-90%}100%{left:100%;right:-90%}}@-moz-keyframes indeterminate{0%{left:-35%;right:100%}60%{left:100%;right:-90%}100%{left:100%;right:-90%}}@keyframes indeterminate{0%{left:-35%;right:100%}60%{left:100%;right:-90%}100%{left:100%;right:-90%}}@-webkit-keyframes indeterminate-short{0%{left:-200%;right:100%}60%{left:107%;right:-8%}100%{left:107%;right:-8%}}@-moz-keyframes indeterminate-short{0%{left:-200%;right:100%}60%{left:107%;right:-8%}100%{left:107%;right:-8%}}@keyframes indeterminate-short{0%{left:-200%;right:100%}60%{left:107%;right:-8%}100%{left:107%;right:-8%}}kandy .hide{display:none !important}kandy .left-align{text-align:left}kandy .right-align{text-align:right}kandy .center,kandy .center-align{text-align:center}kandy .left{float:left !important}kandy .right{float:right !important}kandy .no-select,kandy input[type=range],kandy input[type=range]+.thumb{-webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}kandy .circle{border-radius:50%}kandy .center-block{display:block;margin-left:auto;margin-right:auto}kandy .truncate{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}kandy .no-padding{padding:0 !important}kandy .btn,kandy .btn-large,kandy .btn-flat{border:none;border-radius:2px;display:inline-block;height:36px;line-height:36px;outline:0;padding:0 2rem;text-transform:uppercase;vertical-align:middle;-webkit-tap-highlight-color:transparent}kandy .btn.disabled,kandy .disabled.btn-large,kandy .btn-floating.disabled,kandy .btn-large.disabled,kandy .btn:disabled,kandy .btn-large:disabled,kandy .btn-large:disabled,kandy .btn-floating:disabled{background-color:#DFDFDF;box-shadow:none;color:#9F9F9F;cursor:default}kandy .btn.disabled *,kandy .disabled.btn-large *,kandy .btn-floating.disabled *,kandy .btn-large.disabled *,kandy .btn:disabled *,kandy .btn-large:disabled *,kandy .btn-large:disabled *,kandy .btn-floating:disabled *{pointer-events:none}kandy .btn.disabled:hover,kandy .disabled.btn-large:hover,kandy .btn-floating.disabled:hover,kandy .btn-large.disabled:hover,kandy .btn:disabled:hover,kandy .btn-large:disabled:hover,kandy .btn-large:disabled:hover,kandy .btn-floating:disabled:hover{background-color:#DFDFDF;color:#9F9F9F}kandy .btn i,kandy .btn-large i,kandy .btn-floating i,kandy .btn-large i,kandy .btn-flat i{font-size:1.3rem;line-height:inherit}kandy .btn,kandy .btn-large{text-decoration:none;color:#FFF;background-color:#26a69a;text-align:center;letter-spacing:.5px;-webkit-transition:0.2s ease-out;-moz-transition:0.2s ease-out;-o-transition:0.2s ease-out;-ms-transition:0.2s ease-out;transition:0.2s ease-out;cursor:pointer}kandy .btn:hover,kandy .btn-large:hover{background-color:#2bbbad}kandy .btn-floating{display:inline-block;color:#FFF;position:relative;overflow:hidden;z-index:1;width:37px;height:37px;line-height:37px;padding:0;background-color:#26a69a;border-radius:50%;transition:.3s;cursor:pointer;vertical-align:middle}kandy .btn-floating i{width:inherit;display:inline-block;text-align:center;color:#FFF;font-size:1.6rem;line-height:37px}kandy .btn-floating:before{border-radius:0}kandy .btn-floating.btn-large{width:55.5px;height:55.5px}kandy .btn-floating.btn-large i{line-height:55.5px}kandy button.btn-floating{border:none}kandy .fixed-action-btn{position:fixed;right:23px;bottom:23px;padding-top:15px;margin-bottom:0;z-index:998}kandy .fixed-action-btn ul{left:0;right:0;text-align:center;position:absolute;bottom:64px}kandy .fixed-action-btn ul li{margin-bottom:15px}kandy .fixed-action-btn ul a.btn-floating{opacity:0}kandy .btn-flat{box-shadow:none;background-color:transparent;color:#343434;cursor:pointer}kandy .btn-flat.disabled{color:#b3b3b3;cursor:default}kandy .btn-large{height:54px;line-height:56px}kandy .btn-large i{font-size:1.6rem}kandy select:focus{outline:1px solid #c9f3ef}kandy button:focus{outline:none;background-color:#2ab7a9}kandy label{font-size:0.8rem;color:#9e9e9e}kandy ::-webkit-input-placeholder{color:#d1d1d1}kandy :-moz-placeholder{color:#d1d1d1}kandy ::-moz-placeholder{color:#d1d1d1}kandy :-ms-input-placeholder{color:#d1d1d1}kandy input[type=text],kandy input[type=password],kandy input[type=email],kandy input[type=url],kandy input[type=time],kandy input[type=date],kandy input[type=datetime-local],kandy input[type=tel],kandy input[type=number],kandy input[type=search],kandy textarea.materialize-textarea{background-color:transparent;border:none;border-bottom:1px solid #9e9e9e;border-radius:0;outline:none;height:3rem;width:100%;font-size:1rem;margin:0 0 15px 0;padding:0;box-shadow:none;-webkit-box-sizing:content-box;-moz-box-sizing:content-box;box-sizing:content-box;transition:all .3s}kandy input[type=text]:disabled,kandy input[type=text][readonly="readonly"],kandy input[type=password]:disabled,kandy input[type=password][readonly="readonly"],kandy input[type=email]:disabled,kandy input[type=email][readonly="readonly"],kandy input[type=url]:disabled,kandy input[type=url][readonly="readonly"],kandy input[type=time]:disabled,kandy input[type=time][readonly="readonly"],kandy input[type=date]:disabled,kandy input[type=date][readonly="readonly"],kandy input[type=datetime-local]:disabled,kandy input[type=datetime-local][readonly="readonly"],kandy input[type=tel]:disabled,kandy input[type=tel][readonly="readonly"],kandy input[type=number]:disabled,kandy input[type=number][readonly="readonly"],kandy input[type=search]:disabled,kandy input[type=search][readonly="readonly"],kandy textarea.materialize-textarea:disabled,kandy textarea.materialize-textarea[readonly="readonly"]{color:rgba(0,0,0,0.26);border-bottom:1px dotted rgba(0,0,0,0.26)}kandy input[type=text]:disabled+label,kandy input[type=text][readonly="readonly"]+label,kandy input[type=password]:disabled+label,kandy input[type=password][readonly="readonly"]+label,kandy input[type=email]:disabled+label,kandy input[type=email][readonly="readonly"]+label,kandy input[type=url]:disabled+label,kandy input[type=url][readonly="readonly"]+label,kandy input[type=time]:disabled+label,kandy input[type=time][readonly="readonly"]+label,kandy input[type=date]:disabled+label,kandy input[type=date][readonly="readonly"]+label,kandy input[type=datetime-local]:disabled+label,kandy input[type=datetime-local][readonly="readonly"]+label,kandy input[type=tel]:disabled+label,kandy input[type=tel][readonly="readonly"]+label,kandy input[type=number]:disabled+label,kandy input[type=number][readonly="readonly"]+label,kandy input[type=search]:disabled+label,kandy input[type=search][readonly="readonly"]+label,kandy textarea.materialize-textarea:disabled+label,kandy textarea.materialize-textarea[readonly="readonly"]+label{color:rgba(0,0,0,0.26)}kandy input[type=text]:focus:not([readonly]),kandy input[type=password]:focus:not([readonly]),kandy input[type=email]:focus:not([readonly]),kandy input[type=url]:focus:not([readonly]),kandy input[type=time]:focus:not([readonly]),kandy input[type=date]:focus:not([readonly]),kandy input[type=datetime-local]:focus:not([readonly]),kandy input[type=tel]:focus:not([readonly]),kandy input[type=number]:focus:not([readonly]),kandy input[type=search]:focus:not([readonly]),kandy textarea.materialize-textarea:focus:not([readonly]){border-bottom:1px solid #26a69a;box-shadow:0 1px 0 0 #26a69a}kandy input[type=text]:focus:not([readonly])+label,kandy input[type=password]:focus:not([readonly])+label,kandy input[type=email]:focus:not([readonly])+label,kandy input[type=url]:focus:not([readonly])+label,kandy input[type=time]:focus:not([readonly])+label,kandy input[type=date]:focus:not([readonly])+label,kandy input[type=datetime-local]:focus:not([readonly])+label,kandy input[type=tel]:focus:not([readonly])+label,kandy input[type=number]:focus:not([readonly])+label,kandy input[type=search]:focus:not([readonly])+label,kandy textarea.materialize-textarea:focus:not([readonly])+label{color:#26a69a}kandy input[type=text].valid,kandy input[type=text]:focus.valid,kandy input[type=password].valid,kandy input[type=password]:focus.valid,kandy input[type=email].valid,kandy input[type=email]:focus.valid,kandy input[type=url].valid,kandy input[type=url]:focus.valid,kandy input[type=time].valid,kandy input[type=time]:focus.valid,kandy input[type=date].valid,kandy input[type=date]:focus.valid,kandy input[type=datetime-local].valid,kandy input[type=datetime-local]:focus.valid,kandy input[type=tel].valid,kandy input[type=tel]:focus.valid,kandy input[type=number].valid,kandy input[type=number]:focus.valid,kandy input[type=search].valid,kandy input[type=search]:focus.valid,kandy textarea.materialize-textarea.valid,kandy textarea.materialize-textarea:focus.valid{border-bottom:1px solid #4CAF50;box-shadow:0 1px 0 0 #4CAF50}kandy input[type=text].invalid,kandy input[type=text]:focus.invalid,kandy input[type=password].invalid,kandy input[type=password]:focus.invalid,kandy input[type=email].invalid,kandy input[type=email]:focus.invalid,kandy input[type=url].invalid,kandy input[type=url]:focus.invalid,kandy input[type=time].invalid,kandy input[type=time]:focus.invalid,kandy input[type=date].invalid,kandy input[type=date]:focus.invalid,kandy input[type=datetime-local].invalid,kandy input[type=datetime-local]:focus.invalid,kandy input[type=tel].invalid,kandy input[type=tel]:focus.invalid,kandy input[type=number].invalid,kandy input[type=number]:focus.invalid,kandy input[type=search].invalid,kandy input[type=search]:focus.invalid,kandy textarea.materialize-textarea.invalid,kandy textarea.materialize-textarea:focus.invalid{border-bottom:1px solid #F44336;box-shadow:0 1px 0 0 #F44336}kandy .input-field{position:relative;margin-top:1rem}kandy .input-field label{color:#9e9e9e;position:absolute;top:0.8rem;left:0.75rem;font-size:1rem;cursor:text;-webkit-transition:0.2s ease-out;-moz-transition:0.2s ease-out;-o-transition:0.2s ease-out;-ms-transition:0.2s ease-out;transition:0.2s ease-out}kandy .input-field label.active{font-size:0.8rem;-webkit-transform:translateY(-140%);-moz-transform:translateY(-140%);-ms-transform:translateY(-140%);-o-transform:translateY(-140%);transform:translateY(-140%)}kandy .input-field .prefix{position:absolute;width:3rem;font-size:2rem;-webkit-transition:color 0.2s;-moz-transition:color 0.2s;-o-transition:color 0.2s;-ms-transition:color 0.2s;transition:color 0.2s}kandy .input-field .prefix.active{color:#26a69a}kandy .input-field .prefix ~ input,kandy .input-field .prefix ~ textarea{margin-left:3rem;width:92%;width:calc(100% - 3rem)}kandy .input-field .prefix ~ textarea{padding-top:.8rem}kandy .input-field .prefix ~ label{margin-left:3rem}@media only screen and (max-width: 992px){kandy .input-field .prefix ~ input{width:86%;width:calc(100% - 3rem)}}@media only screen and (max-width: 600px){kandy .input-field .prefix ~ input{width:80%;width:calc(100% - 3rem)}}kandy .input-field input[type=search]{display:block;line-height:inherit;padding-left:4rem;width:calc(100% - 4rem)}kandy .input-field input[type=search]:focus{background-color:#FFF;border:0;box-shadow:none;color:#444}kandy .input-field input[type=search]:focus+label i,kandy .input-field input[type=search]:focus ~ .mdi-navigation-close{color:#444}kandy .input-field input[type=search]+label{left:1rem}kandy .input-field input[type=search] ~ .mdi-navigation-close{position:absolute;top:0;right:1rem;color:transparent;cursor:pointer;font-size:2rem;transition:.3s color}kandy textarea{width:100%;height:3rem;background-color:transparent}kandy textarea.materialize-textarea{overflow-y:hidden;padding:1.6rem 0;resize:none;min-height:3rem}kandy .hiddendiv{display:none;white-space:pre-wrap;word-wrap:break-word;overflow-wrap:break-word;padding-top:1.2rem}kandy [type="radio"]:not(:checked),kandy [type="radio"]:checked{position:absolute;left:-9999px;visibility:hidden}kandy [type="radio"]:not(:checked)+label,kandy [type="radio"]:checked+label{position:relative;padding-left:35px;cursor:pointer;display:inline-block;height:25px;line-height:25px;font-size:1rem;-webkit-transition:0.28s ease;-moz-transition:0.28s ease;-o-transition:0.28s ease;-ms-transition:0.28s ease;transition:0.28s ease;-webkit-user-select:none;-moz-user-select:none;-khtml-user-select:none;-ms-user-select:none}kandy [type="radio"]+label:before,kandy [type="radio"]+label:after{content:"""";position:absolute;left:0;top:0;margin:4px;width:16px;height:16px;z-index:0;-webkit-transition:0.28s ease;-moz-transition:0.28s ease;-o-transition:0.28s ease;-ms-transition:0.28s ease;transition:0.28s ease}kandy [type="radio"]:not(:checked)+label:before{border-radius:50%;border:2px solid #5a5a5a}kandy [type="radio"]:not(:checked)+label:after{border-radius:50%;border:2px solid #5a5a5a;z-index:-1;-webkit-transform:scale(0);-moz-transform:scale(0);-ms-transform:scale(0);-o-transform:scale(0);transform:scale(0)}kandy [type="radio"]:checked+label:before{border-radius:50%;border:2px solid transparent}kandy [type="radio"]:checked+label:after{border-radius:50%;border:2px solid #26a69a;background-color:#26a69a;z-index:0;-webkit-transform:scale(1.02);-moz-transform:scale(1.02);-ms-transform:scale(1.02);-o-transform:scale(1.02);transform:scale(1.02)}kandy [type="radio"].with-gap:checked+label:before{border-radius:50%;border:2px solid #26a69a}kandy [type="radio"].with-gap:checked+label:after{border-radius:50%;border:2px solid #26a69a;background-color:#26a69a;z-index:0;-webkit-transform:scale(0.5);-moz-transform:scale(0.5);-ms-transform:scale(0.5);-o-transform:scale(0.5);transform:scale(0.5)}kandy [type="radio"]:disabled:not(:checked)+label:before,kandy [type="radio"]:disabled:checked+label:before{background-color:transparent;border-color:rgba(0,0,0,0.26)}kandy [type="radio"]:disabled+label{color:rgba(0,0,0,0.26)}kandy [type="radio"]:disabled:not(:checked)+label:hover:before{border-color:rgba(0,0,0,0.26)}kandy form p{margin-bottom:10px;text-align:left}kandy form p:last-child{margin-bottom:0}kandy [type="checkbox"]:not(:checked),kandy [type="checkbox"]:checked{position:absolute;left:-9999px}kandy [type="checkbox"]+label{position:relative;padding-left:35px;cursor:pointer;display:inline-block;height:25px;line-height:25px;font-size:1rem;-webkit-user-select:none;-moz-user-select:none;-khtml-user-select:none;-ms-user-select:none}kandy [type="checkbox"]+label:before{content:"""";position:absolute;top:0;left:0;width:18px;height:18px;z-index:0;border:2px solid #5a5a5a;border-radius:1px;margin-top:2px;-webkit-transition:0.2s;-moz-transition:0.2s;-o-transition:0.2s;-ms-transition:0.2s;transition:0.2s}kandy [type="checkbox"]:not(:checked):disabled+label:before{border:none;background-color:rgba(0,0,0,0.26)}kandy [type="checkbox"]:checked+label:before{top:-4px;left:-3px;width:12px;height:22px;border-top:2px solid transparent;border-left:2px solid transparent;border-right:2px solid #26a69a;border-bottom:2px solid #26a69a;-webkit-transform:rotate(40deg);-moz-transform:rotate(40deg);-ms-transform:rotate(40deg);-o-transform:rotate(40deg);transform:rotate(40deg);-webkit-backface-visibility:hidden;-webkit-transform-origin:100% 100%;-moz-transform-origin:100% 100%;-ms-transform-origin:100% 100%;-o-transform-origin:100% 100%;transform-origin:100% 100%}kandy [type="checkbox"]:checked:disabled+label:before{border-right:2px solid rgba(0,0,0,0.26);border-bottom:2px solid rgba(0,0,0,0.26)}kandy [type="checkbox"]:indeterminate+label:before{left:-10px;top:-11px;width:10px;height:22px;border-top:none;border-left:none;border-right:2px solid #26a69a;border-bottom:none;-webkit-transform:rotate(90deg);-moz-transform:rotate(90deg);-ms-transform:rotate(90deg);-o-transform:rotate(90deg);transform:rotate(90deg);-webkit-backface-visibility:hidden;-webkit-transform-origin:100% 100%;-moz-transform-origin:100% 100%;-ms-transform-origin:100% 100%;-o-transform-origin:100% 100%;transform-origin:100% 100%}kandy [type="checkbox"]:indeterminate:disabled+label:before{border-right:2px solid rgba(0,0,0,0.26);background-color:transparent}kandy [type="checkbox"].filled-in+label:after{border-radius:2px}kandy [type="checkbox"].filled-in+label:before,kandy [type="checkbox"].filled-in+label:after{content:"""";left:0;position:absolute;transition:border .25s, background-color .25s, width .20s .1s, height .20s .1s, top .20s .1s, left .20s .1s;z-index:1}kandy [type="checkbox"].filled-in:not(:checked)+label:before{width:0;height:0;border:3px solid transparent;left:6px;top:10px;-webkit-transform:rotateZ(37deg);transform:rotateZ(37deg);-webkit-transform-origin:20% 40%;transform-origin:100% 100%}kandy [type="checkbox"].filled-in:not(:checked)+label:after{height:20px;width:20px;background-color:transparent;border:2px solid #5a5a5a;top:0px;z-index:0}kandy [type="checkbox"].filled-in:checked+label:before{top:0;left:1px;width:8px;height:13px;border-top:2px solid transparent;border-left:2px solid transparent;border-right:2px solid #fff;border-bottom:2px solid #fff;-webkit-transform:rotateZ(37deg);transform:rotateZ(37deg);-webkit-transform-origin:100% 100%;transform-origin:100% 100%}kandy [type="checkbox"].filled-in:checked+label:after{top:0px;width:20px;height:20px;border:2px solid #26a69a;background-color:#26a69a;z-index:0}kandy [type="checkbox"].filled-in:disabled:not(:checked)+label:before{background-color:transparent;border:2px solid transparent}kandy [type="checkbox"].filled-in:disabled:not(:checked)+label:after{border-color:transparent;background-color:#BDBDBD}kandy [type="checkbox"].filled-in:disabled:checked+label:before{background-color:transparent}kandy [type="checkbox"].filled-in:disabled:checked+label:after{background-color:#BDBDBD;border-color:#BDBDBD}kandy .switch,kandy .switch *{-webkit-user-select:none;-moz-user-select:none;-khtml-user-select:none;-ms-user-select:none}kandy .switch label{cursor:pointer}kandy .switch label input[type=checkbox]{opacity:0;width:0;height:0}kandy .switch label input[type=checkbox]:checked+.lever{background-color:#84c7c1}kandy .switch label input[type=checkbox]:checked+.lever:after{background-color:#26a69a}kandy .switch label .lever{content:"";display:inline-block;position:relative;width:40px;height:15px;background-color:#818181;border-radius:15px;margin-right:10px;transition:background 0.3s ease;vertical-align:middle;margin:0 16px}kandy .switch label .lever:after{content:"";position:absolute;display:inline-block;width:21px;height:21px;background-color:#F1F1F1;border-radius:21px;box-shadow:0 1px 3px 1px rgba(0,0,0,0.4);left:-5px;top:-3px;transition:left 0.3s ease, background 0.3s ease, box-shadow 0.1s ease}kandy input[type=checkbox]:checked:not(:disabled) ~ .lever:active:after{box-shadow:0 1px 3px 1px rgba(0,0,0,0.4),0 0 0 15px rgba(38,166,154,0.1)}kandy input[type=checkbox]:not(:disabled) ~ .lever:active:after{box-shadow:0 1px 3px 1px rgba(0,0,0,0.4),0 0 0 15px rgba(0,0,0,0.08)}kandy .switch label input[type=checkbox]:checked+.lever:after{left:24px}kandy .switch input[type=checkbox][disabled]+.lever{cursor:default}kandy .switch label input[type=checkbox][disabled]+.lever:after,kandy .switch label input[type=checkbox][disabled]:checked+.lever:after{background-color:#BDBDBD}kandy .select-label{position:absolute}kandy .select-wrapper{position:relative}kandy .select-wrapper input.select-dropdown{position:relative;cursor:pointer;background-color:transparent;border:none;border-bottom:1px solid #9e9e9e;outline:none;height:3rem;line-height:3rem;width:100%;font-size:1rem;margin:0 0 15px 0;padding:0;display:block}kandy .select-wrapper .mdi-navigation-arrow-drop-down{color:initial;position:absolute;right:0;top:0;font-size:23px}kandy .select-wrapper .mdi-navigation-arrow-drop-down.disabled{color:rgba(0,0,0,0.26)}kandy .select-wrapper+label{position:absolute;top:-14px;font-size:0.8rem}kandy select{display:none}kandy select.browser-default{display:block}kandy select:disabled{color:rgba(0,0,0,0.3)}kandy .select-wrapper input.select-dropdown:disabled{color:rgba(0,0,0,0.3);cursor:default;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;border-bottom:1px solid rgba(0,0,0,0.3)}kandy .select-wrapper i{color:rgba(0,0,0,0.3)}kandy .select-dropdown li.disabled{color:rgba(0,0,0,0.3);background-color:transparent}kandy .file-field{position:relative}kandy .file-field input.file-path{margin-left:100px;width:calc(100% - 100px)}kandy .file-field .btn,kandy .file-field .btn-large{position:absolute;top:0;left:0;height:3rem;line-height:3rem}kandy .file-field span{cursor:pointer}kandy .file-field input[type=file]{position:absolute;top:0;right:0;left:0;bottom:0;width:100%;margin:0;padding:0;font-size:20px;cursor:pointer;opacity:0;filter:alpha(opacity=0)}kandy .range-field{position:relative}kandy input[type=range],kandy input[type=range]+.thumb{cursor:pointer}kandy input[type=range]{position:relative;background-color:transparent;border:none;outline:none;width:100%;margin:15px 0px;padding:0}kandy input[type=range]+.thumb{position:absolute;border:none;height:0;width:0;border-radius:50%;background-color:#26a69a;top:10px;margin-left:-6px;-webkit-transform-origin:50% 50%;-moz-transform-origin:50% 50%;-ms-transform-origin:50% 50%;-o-transform-origin:50% 50%;transform-origin:50% 50%;-webkit-transform:rotate(-45deg);-moz-transform:rotate(-45deg);-ms-transform:rotate(-45deg);-o-transform:rotate(-45deg);transform:rotate(-45deg)}kandy input[type=range]+.thumb .value{display:block;width:30px;text-align:center;color:#26a69a;font-size:0;-webkit-transform:rotate(45deg);-moz-transform:rotate(45deg);-ms-transform:rotate(45deg);-o-transform:rotate(45deg);transform:rotate(45deg)}kandy input[type=range]+.thumb.active{border-radius:50% 50% 50% 0}kandy input[type=range]+.thumb.active .value{color:#fff;margin-left:-1px;margin-top:8px;font-size:10px}kandy input[type=range]:focus{outline:none}kandy input[type=range]{-webkit-appearance:none}kandy input[type=range]::-webkit-slider-runnable-track{height:3px;background:#c2c0c2;border:none}kandy input[type=range]::-webkit-slider-thumb{-webkit-appearance:none;border:none;height:14px;width:14px;border-radius:50%;background-color:#26a69a;transform-origin:50% 50%;margin:-5px 0 0 0;-webkit-transition:0.3s;-moz-transition:0.3s;-o-transition:0.3s;-ms-transition:0.3s;transition:0.3s}kandy input[type=range]:focus::-webkit-slider-runnable-track{background:#ccc}kandy input[type=range]{border:1px solid white}kandy input[type=range]::-moz-range-track{height:3px;background:#ddd;border:none}kandy input[type=range]::-moz-range-thumb{border:none;height:14px;width:14px;border-radius:50%;background:#26a69a;margin-top:-5px}kandy input[type=range]:-moz-focusring{outline:1px solid white;outline-offset:-1px}kandy input[type=range]:focus::-moz-range-track{background:#ccc}kandy input[type=range]::-ms-track{height:3px;background:transparent;border-color:transparent;border-width:6px 0;color:transparent}kandy input[type=range]::-ms-fill-lower{background:#777}kandy input[type=range]::-ms-fill-upper{background:#ddd}kandy input[type=range]::-ms-thumb{border:none;height:14px;width:14px;border-radius:50%;background:#26a69a}kandy input[type=range]:focus::-ms-fill-lower{background:#888}kandy input[type=range]:focus::-ms-fill-upper{background:#ccc}kandy select{background-color:rgba(255,255,255,0.9);width:100%;padding:5px;border:1px solid #f2f2f2;border-radius:2px;height:3rem}kandy .container{padding:0 1.5rem;margin:0 auto;max-width:1280px;width:90%}@media only screen and (min-width: 601px){kandy .container{width:85%}}@media only screen and (min-width: 993px){kandy .container{width:70%}}kandy .container .row{margin-left:-0.75rem;margin-right:-0.75rem}kandy .section{padding-top:1rem;padding-bottom:1rem}kandy .section.no-pad{padding:0}kandy .section.no-pad-bot{padding-bottom:0}kandy .section.no-pad-top{padding-top:0}kandy .row{margin-left:auto;margin-right:auto;margin-bottom:20px}kandy .row:after{content:"";display:table;clear:both}kandy .row .col{float:left;-webkit-box-sizing:border-box;-moz-box-sizing:border-box;box-sizing:border-box;padding:0 0.75rem}kandy .row .col.s1{width:8.33333%;margin-left:0}kandy .row .col.s2{width:16.66667%;margin-left:0}kandy .row .col.s3{width:25%;margin-left:0}kandy .row .col.s4{width:33.33333%;margin-left:0}kandy .row .col.s5{width:41.66667%;margin-left:0}kandy .row .col.s6{width:50%;margin-left:0}kandy .row .col.s7{width:58.33333%;margin-left:0}kandy .row .col.s8{width:66.66667%;margin-left:0}kandy .row .col.s9{width:75%;margin-left:0}kandy .row .col.s10{width:83.33333%;margin-left:0}kandy .row .col.s11{width:91.66667%;margin-left:0}kandy .row .col.s12{width:100%;margin-left:0}kandy .row .col.offset-s1{margin-left:8.33333%}kandy .row .col.offset-s2{margin-left:16.66667%}kandy .row .col.offset-s3{margin-left:25%}kandy .row .col.offset-s4{margin-left:33.33333%}kandy .row .col.offset-s5{margin-left:41.66667%}kandy .row .col.offset-s6{margin-left:50%}kandy .row .col.offset-s7{margin-left:58.33333%}kandy .row .col.offset-s8{margin-left:66.66667%}kandy .row .col.offset-s9{margin-left:75%}kandy .row .col.offset-s10{margin-left:83.33333%}kandy .row .col.offset-s11{margin-left:91.66667%}kandy .row .col.offset-s12{margin-left:100%}@media only screen and (min-width: 601px){kandy .row .col.m1{width:8.33333%;margin-left:0}kandy .row .col.m2{width:16.66667%;margin-left:0}kandy .row .col.m3{width:25%;margin-left:0}kandy .row .col.m4{width:33.33333%;margin-left:0}kandy .row .col.m5{width:41.66667%;margin-left:0}kandy .row .col.m6{width:50%;margin-left:0}kandy .row .col.m7{width:58.33333%;margin-left:0}kandy .row .col.m8{width:66.66667%;margin-left:0}kandy .row .col.m9{width:75%;margin-left:0}kandy .row .col.m10{width:83.33333%;margin-left:0}kandy .row .col.m11{width:91.66667%;margin-left:0}kandy .row .col.m12{width:100%;margin-left:0}kandy .row .col.offset-m1{margin-left:8.33333%}kandy .row .col.offset-m2{margin-left:16.66667%}kandy .row .col.offset-m3{margin-left:25%}kandy .row .col.offset-m4{margin-left:33.33333%}kandy .row .col.offset-m5{margin-left:41.66667%}kandy .row .col.offset-m6{margin-left:50%}kandy .row .col.offset-m7{margin-left:58.33333%}kandy .row .col.offset-m8{margin-left:66.66667%}kandy .row .col.offset-m9{margin-left:75%}kandy .row .col.offset-m10{margin-left:83.33333%}kandy .row .col.offset-m11{margin-left:91.66667%}kandy .row .col.offset-m12{margin-left:100%}}@media only screen and (min-width: 993px){kandy .row .col.l1{width:8.33333%;margin-left:0}kandy .row .col.l2{width:16.66667%;margin-left:0}kandy .row .col.l3{width:25%;margin-left:0}kandy .row .col.l4{width:33.33333%;margin-left:0}kandy .row .col.l5{width:41.66667%;margin-left:0}kandy .row .col.l6{width:50%;margin-left:0}kandy .row .col.l7{width:58.33333%;margin-left:0}kandy .row .col.l8{width:66.66667%;margin-left:0}kandy .row .col.l9{width:75%;margin-left:0}kandy .row .col.l10{width:83.33333%;margin-left:0}kandy .row .col.l11{width:91.66667%;margin-left:0}kandy .row .col.l12{width:100%;margin-left:0}kandy .row .col.offset-l1{margin-left:8.33333%}kandy .row .col.offset-l2{margin-left:16.66667%}kandy .row .col.offset-l3{margin-left:25%}kandy .row .col.offset-l4{margin-left:33.33333%}kandy .row .col.offset-l5{margin-left:41.66667%}kandy .row .col.offset-l6{margin-left:50%}kandy .row .col.offset-l7{margin-left:58.33333%}kandy .row .col.offset-l8{margin-left:66.66667%}kandy .row .col.offset-l9{margin-left:75%}kandy .row .col.offset-l10{margin-left:83.33333%}kandy .row .col.offset-l11{margin-left:91.66667%}kandy .row .col.offset-l12{margin-left:100%}}kandy .modal{display:none;position:fixed;left:0;right:0;background-color:#fafafa;padding:0;max-height:70%;width:55%;margin:auto;overflow-y:auto;z-index:1000;border-radius:2px;-webkit-transform:translate(0);-moz-transform:translate(0);-ms-transform:translate(0);-o-transform:translate(0);transform:translate(0);will-change:top, opacity}@media only screen and (max-width: 992px){kandy .modal{width:80%}}kandy .modal h1,kandy .modal h2,kandy .modal h3,kandy .modal h4{margin-top:0}kandy .modal .modal-content{padding:24px}kandy .modal .modal-footer{border-radius:0 0 2px 2px;background-color:#fafafa;padding:4px 6px;height:56px;width:100%}kandy .modal .modal-footer .btn,kandy .modal .modal-footer .btn-large,kandy .modal .modal-footer .btn-flat{float:right;margin:6px 0}kandy #lean-overlay{position:fixed;z-index:999;top:0;left:0;bottom:0;right:0;height:115%;width:100%;background:#000;display:none;will-change:opacity}kandy .modal.modal-fixed-footer{padding:0;height:70%}kandy .modal.modal-fixed-footer .modal-content{position:fixed;max-height:100%;padding-bottom:64px;width:100%;overflow-y:auto}kandy .modal.modal-fixed-footer .modal-footer{border-top:1px solid rgba(0,0,0,0.1);position:fixed;bottom:0}kandy .modal.bottom-sheet{top:auto;bottom:-100%;margin:0;width:100%;max-height:45%;border-radius:0;will-change:bottom, opacity}kandy .btn,kandy .btn-large{margin:1px}kandy .btn-block{width:75%}kandy .btn-small{padding:5px 10px;font-size:12px;line-height:1.5;border-radius:3px}kandy a.btn-small{height:26px}kandy .btn-xsmall{padding:1px 5px;font-size:9px;line-height:1.5;border-radius:3px}kandy a.btn-xsmall{height:14px}kandy .btn-group{display:inline-block}kandy .btn-group>.btn,kandy .btn-group>.btn-large{float:left}kandy .btn-group>.btn-append{float:left;max-width:20px;margin-left:-3px}kandy .btn-append .btn-large{padding:0}kandy .modal{width:90%;max-height:100%;display:block;opacity:1;top:5%}kandy .modal-overlay{position:fixed;z-index:999;top:0;left:0;bottom:0;right:0;height:115%;width:100%;background:#000;display:block;opacity:0.5}',
            head = document.head || document.getElementsByTagName('head')[0],
            style = document.createElement('style');

        style.type = 'text/css';
        if (style.styleSheet){
            style.styleSheet.cssText = css;
        } else {
            style.appendChild(document.createTextNode(css));
        }

        head.appendChild(style);
    },

    /**
     * Render Kandy widgets.
     *
     * @private
     */
    _renderKandyWidgets: function () {

        var widgets = document.getElementsByTagName(Kandy.ELEMENT_TAG);

        if (widgets.length > 0)
            this._loadPluginResources();

        for (var i = 0; i < widgets.length; ++i) {
            var name = widgets[i].getAttribute("widget");

            if (name != undefined)
                name = name.toLowerCase();

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
                case this.Widget.GROUP:
                    this._renderKandyGroupWidget(widgets[i]);
                    break;
                case this.Widget.PRESENCE:
                    this._renderKandyPresenceWidget(widgets[i]);
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

        element.innerHTML += '<div class="container center">'
            + '<div class="row">'
            + '<input type="tel" id="' + id + '-phone-number" placeholder="Enter number phone..."/>'
            + '<input type="text" id="' + id + '-region-code" maxlength="2" placeholder="Country code" value="' + code + '"/>'
            + '<button class="btn" id="' + id + '-btn-request">Request</button>'
            + '</div>'
            + '<div class="row">'
            + '<input type="text" id="' + id + '-otp-code" placeholder="Enter OTP code..."/>'
            + '<button class="btn blue" id="' + id + '-btn-validate">Validate</button>'
            + '</div>'
            + '<div class="row">'
            + '<button class="btn red" id="' + id + '-btn-deactivate">Deactivate</button>'
            + '</div>'
            + '</div>';

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

        var loginForm = '<div class="container center">'
            + '<input type="text" id="' + id + '-username" placeholder="Username"/>'
            + '<input type="password" id="' + id + '-password" placeholder="Password"/>'
            + '<button class="btn" id="' + id + '-btn-login">Login</button>'
            + '</div>';

        var logoutForm = function (user) {
            return '<div class="container center">'
                + '<h5>' + user + '</h5>'
                + '<button class="btn red" id="' + id + '-btn-logout">Logout</button>'
                + '</div>';
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

        Kandy.access.getConnectionState(function (state) {
            if (state != Kandy.ConnectionState.CONNECTED) {
                element.innerHTML = loginForm;
                addLoginAction();
            } else {
                Kandy.getSession(function (data) {
                    element.innerHTML = logoutForm(data.user.id);
                    addLogoutAction();
                })
            }
        })
    },

    _incomingCallWidget: function (calleeId) {
        var modal = document.createElement(Kandy.ELEMENT_TAG);
        modal.id = calleeId + '-incoming-modal';
        modal.innerHTML = '<div class="modal">'
            + '<div class="modal-content center">'
            + '<h5>' + calleeId + ' calling...</h5>'
            + '<div class="row">'
            + '<button class="btn" id="' + calleeId + '-btn-call-accept">Accept</button>'
            + '<button class="btn red" id="' + calleeId + '-btn-call-reject">Reject</button>'
            + '<button class="btn orange" id="' + calleeId + '-btn-call-ignore">Ignore</button>'
            + '</div>'
            + '</div>'
            + '</div>'
            + '<div id="' + calleeId + '-overlay" class="modal-overlay"></div>'

        var body = document.getElementsByTagName("body")[0];
        body.appendChild(modal);

        document.getElementById(calleeId + '-btn-call-accept').onclick = function () {
            Kandy.call.accept(function () {
                Kandy._talkingCallWidget(calleeId);
            }, function (e) {
                Kandy._defaultErrorAction(e);
            }, calleeId, true);

            modal.remove();
        }

        document.getElementById(calleeId + '-btn-call-reject').onclick = function () {
            Kandy.call.reject(null, function (e) {
                Kandy._defaultErrorAction(e);
            }, calleeId);

            modal.remove();
        }

        document.getElementById(calleeId + '-btn-call-ignore').onclick = function () {
            Kandy.call.ignore(null, function (e) {
                Kandy._defaultErrorAction(e);
            }, calleeId);

            modal.remove();
        }
    },

    _talkingCallWidget: function (calleeId, video) {

        if (video == undefined || video == true)
            video = 'checked';
        else video = '';

        var modal = document.createElement(Kandy.ELEMENT_TAG);
        modal.id = calleeId + '-talking-modal';
        modal.innerHTML = '<div class="modal">'
            + '<div class="modal-content center">'
            + '<div class="row">'
            + '<h6><b>' + calleeId + '</b></h6>'
            + '</div>'
            + '<div class="row">'
            + '<div class="switch">'
            + '<label>Hold'
            + '<input id="' + calleeId + '-btn-call-hold" type="checkbox"/>'
            + '<span class="lever"></span>'
            + '</label>'
            + '<label>Mute'
            + '<input id="' + calleeId + '-btn-call-mute" type="checkbox"/>'
            + '<span class="lever"></span>'
            + '</label>'
            + '<label>Video'
            + '<input id="' + calleeId + '-btn-call-video" type="checkbox" ' + video + '/>'
            + '<span class="lever"></span>'
            + '</label>'
            + '</div>'
            + '</div>'
            + '<div class="row">'
            + '<div class="switch">'
            + '<label>Camera'
            + '<input id="' + calleeId + '-btn-call-camera" type="checkbox" checked/>'
            + '<span class="lever"></span>'
            + '</label>'
            + '<label>Speaker'
            + '<input id="' + calleeId + '-btn-call-speaker" type="checkbox"/>'
            + '<span class="lever"></span>'
            + '</label>'
            + '</div>'
            + '</div>'
            + '<div class="row">'
            + '<button class="btn red btn-large btn-block" id="' + calleeId + '-btn-call-hangup">Hangup</button>'
            + '</div>'
            + '<div class="row">'
            + '<img id="' + calleeId + '-video-view-placeholder" src="" width="298" height="298" />'
            + '</div>'
            + '</div>'
            + '</div>'
            + '<div id="' + calleeId + '-overlay" class="modal-overlay"></div>'

        var body = document.getElementsByTagName("body")[0];
        body.appendChild(modal);

        if (Kandy.videoView != undefined) {
            Kandy.call.showRemoteVideo(null, function (e) {
                Kandy._defaultErrorAction(e);
            }, calleeId, Kandy.videoView.left, Kandy.videoView.top, Kandy.videoView.width, Kandy.videoView.height);

            var delta = {
                width: Kandy.videoView.width * 0.3,
                height: Kandy.videoView.height * 0.3
            };

            Kandy.call.showLocalVideo(null, function (e) {
                Kandy._defaultErrorAction(e);
            }, calleeId, Kandy.videoView.left + Kandy.videoView.width - delta.width, Kandy.videoView.top + Kandy.videoView.height - delta.height, delta.width, delta.height);
        }

        document.getElementById(calleeId + '-btn-call-camera').onchange = function () {
            var checked = document.getElementById(calleeId + '-btn-call-camera').checked;
            if (checked) {
                Kandy.call.switchFrontCamera(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-camera').checked = false;
                }, calleeId);
            } else {
                Kandy.call.switchBackCamera(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-camera').checked = true;
                }, calleeId);
            }
        };

        document.getElementById(calleeId + '-btn-call-speaker').onchange = function () {
            var checked = document.getElementById(calleeId + '-btn-call-speaker').checked;
            if (checked) {
                Kandy.call.switchSpeakerOn();
            } else {
                Kandy.call.switchSpeakerOff();
            }
        };

        document.getElementById(calleeId + '-btn-call-hold').onchange = function () {
            var checked = document.getElementById(calleeId + '-btn-call-hold').checked;
            if (checked) {
                Kandy.call.hold(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-hold').checked = false;
                }, calleeId);
            } else {
                Kandy.call.unhold(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-hold').checked = true;
                }, calleeId);
            }
        };

        document.getElementById(calleeId + '-btn-call-mute').onchange = function () {
            var checked = document.getElementById(calleeId + '-btn-call-mute').checked;
            if (checked) {
                Kandy.call.mute(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-mute').checked = false;
                }, calleeId);
            } else {
                Kandy.call.unmute(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-mute').checked = true;
                }, calleeId);
            }
        };

        document.getElementById(calleeId + '-btn-call-video').onchange = function () {
            var checked = document.getElementById(calleeId + '-btn-call-video').checked;
            if (checked) {
                Kandy.call.enableVideo(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-video').checked = false;
                }, calleeId);
            } else {
                Kandy.call.disableVideo(null, function (e) {
                    Kandy._defaultErrorAction(e);
                    document.getElementById(calleeId + '-btn-call-video').checked = true;
                }, calleeId);
            }
        };

        document.getElementById(calleeId + '-btn-call-hangup').onclick = function () {
            Kandy.call.hangup(null, function (e) {
                Kandy._defaultErrorAction(e);
            }, calleeId);

            modal.remove();
        }
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

        if (type == undefined) type = 'VOIP';
        else type = type.toUpperCase();


        var callPanel = '<div id="' + id + '-dial-panel">';

        if (type == 'PSTN') {
            if (callee != undefined && callee != "" && !this._validateEmail(callee)) {
                callPanel += '<input type="hidden" id="' + id +
                    '-callee" value="' + callee + '"/>';
            } else {
                callPanel += '<input type="text" id="' + id + '-callee" placeholder="Enter number phone..."/>';
            }

            callPanel += '<button id="' + id + '-btn-call" class="btn">' + label + '</button>'
                + '</div>';
        } else {

            if (callee != undefined && callee != "" && this._validateEmail(callee)) {
                callPanel += '<input type="hidden" id="' + id + '-callee" value="' + callee + '"/>';
            } else {
                callPanel += '<input type="text" id="' + id + '-callee" placeholder="userID@domain.com"/>';
            }

            if (startWithVideo != undefined && startWithVideo != "") {
                var checked = (startWithVideo == 1 || startWithVideo == "true") ? "checked" : "";
                callPanel += '<input type="hidden" id="' + id + '-start-with-video"' + checked + '/>';
            } else
                callPanel += '<p><input type="checkbox" id="' + id + '-start-with-video"/><label for="' + id + '-start-with-video">Start with video</label></p>';

            callPanel += '<button id="' + id + '-btn-call" class="btn">' + label + '</button>'
                + '</div>';
        }

        element.innerHTML = '<div class="container center">' + callPanel + '</div>';

        if (type == 'PSTN') {
            document.getElementById(id + '-btn-call').onclick = function (event) {
                var username = document.getElementById(id + '-callee').value;

                Kandy.call.createPSTNCall(function (s) {
                        Kandy._talkingCallWidget(s.callee.uri, false);
                        Kandy._callSuccessFunction(element, "call", s, Kandy._defaultSuccessAction);
                    }, function (e) {
                        Kandy._callErrorFunction(element, "call", e, Kandy._defaultErrorAction);
                    }, username
                );
            }
        } else {
            document.getElementById(id + '-btn-call').onclick = function (event) {
                var username = document.getElementById(id + '-callee').value,
                    startWithVideo = document.getElementById(id + '-start-with-video').checked == true ? 1 : 0;

                Kandy.call.createVoipCall(function (s) {
                        Kandy._talkingCallWidget(s.callee.uri, startWithVideo)
                        Kandy._callSuccessFunction(element, "call", s, Kandy._defaultSuccessAction);
                    }, function (e) {
                        Kandy._callErrorFunction(element, "call", e, Kandy._defaultErrorAction);
                    }, username, startWithVideo
                );
            }
        }
    },

    /**
     * Render message item.
     *
     * @param data The message to render.
     * @returns {string} The message item.
     * @private
     */
    _renderMessageItem: function (data) {
        var msg = data.message;

        if ($("#" + msg.UUID).length) return "";

        var extras = "";
        switch (msg.contentType) {
            case "text":
                break;
            case "location":
                extras = "lat: " + msg.message.location_latitude
                    + " lng: " + msg.message.location_longitude
                    + " acc: " + msg.message.media_accuracy
                    + " zoom: " + msg.message.media_map_zoom;
                extras = '<div id="' + msg.UUID + '-extras">' + extras + '</div>'
                break;
            default: // audio, video, contact, file
                if (data.uri == undefined) return "";
                extras = "<div id=\"" + msg.UUID + "-extras\" onclick=\"js:Kandy.chat.openAttachment(null, null,\'" + data.uri + "\',\'" + msg.message.mimeType + "\')\">" + msg.message.content_name + "</div>";
                break;
        }

        var item = '<li class="collection-item" onClick="js:Kandy._markMessageAsReceived(\'' + msg.UUID + '\')">'
            + '<h5><b>' + msg.sender + '</b></h5>'
            + '<div class="row"><small>' + new Date(msg.timestamp).toUTCString() + '</small></div>'
            + '<div class="row" id="' + msg.UUID + '">'
            + '<div id="' + msg.UUID + '-text"><strong>' + msg.message.text + '</strong></div>'
            + extras
            + '</div>'
            + '</li>';

        return item;
    },

    /**
     * Add the message to the message containers.
     *
     * @param data The message to add.
     * @private
     */
    _addMessagetoContainers: function (data) {

        var item = data.message == undefined ? data.item : Kandy._renderMessageItem(data);
        var type = data.message == undefined ? data.type : data.message.messageType;

        if (item == "") return;

        for (var i = 0; i < this._messageContainers.length; ++i) {
            var container = document.getElementById(this._messageContainers[i]);
            if (container != undefined) {
                if (type == container.getAttribute("type"))
                    container.innerHTML = item + container.innerHTML;
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
        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        var type = element.getAttribute("type");
        var recipientValue = element.getAttribute("send-to");

        if (type == undefined) type = "CHAT";
        else type = type.toUpperCase();

        this._messageContainers.push(id + '-messages');

        if (type == "SMS") {

            if (recipientValue != undefined && recipientValue != "" && !this._validateEmail(recipientValue)) {
                recipientValue = 'value="' + recipientValue + '" disabled';
            } else {
                recipientValue = "";
            }

            element.innerHTML = '<div class="container">'
                + '<div class="center">'
                + '<input type="text" placeholder="Enter number phone..." id="' + id + '-recipient" ' + recipientValue + '/>'
                + '<div class="row">'
                + '<div class="col s10">'
                + '<input type="text" placeholder="Message..." id="' + id + '-message"/>'
                + '</div>'
                + '<div class="col s2">'
                + '<button class="btn btn-small" id="' + id + '-btn-send">Send</button>'
                + '</div>'
                + '</div>'
                + '<div class="row">'
                + '<button class="btn orange" id="' + id + '-btn-pull">Pull</button>'
                + '</div>'
                + '</div>'
                + '<h5><b>Messages</b></h5>'
                + '<ul id="' + id + '-messages" type="' + type + '" class="collection">'
                + '</ul>'
                + '</div>';

            document.getElementById(id + '-btn-send').onclick = function (event) {
                var recipient = document.getElementById(id + '-recipient').value,
                    message = document.getElementById(id + '-message').value;

                Kandy.chat.sendSMS(function (s) {
                    Kandy._callSuccessFunction(element, "send", s, function () {
                        var item = '<li class="collection-item"><h5><b>You: </b></h5><p>' + message + '</p><p class="right"><small>' + new Date().toUTCString() + '</small></p></li>';
                        Kandy._addMessagetoContainers({item: item, type: type});
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

            element.innerHTML = '<div class="container">'
                + '<div class="center">'
                + '<input type="text" placeholder="recipientID@domain" id="' + id + '-recipient" ' + recipientValue + '/>'
                + '<div class="row">'
                + '<div class="col s10">'
                + '<input type="text" placeholder="Message..." id="' + id + '-message"/>'
                + '</div>'
                + '<div class="col s2">'
                + '<button class="btn btn-small" id="' + id + '-btn-send">Send</button>'
                + '</div>'
                + '</div>'
                + '<div class="row">'
                + '<button class="btn" id="' + id + '-btn-attach">Attactment</button>'
                + '<button class="btn orange" id="' + id + '-btn-pull">Pull</button>'
                + '</div>'
                + '</div>'
                + '<h5><b>Messages</b></h5>'
                + '<ul id="' + id + '-messages" type="' + type + '" class="collection">'
                + '</ul>'
                + '</div>';

            document.getElementById(id + '-btn-send').onclick = function (event) {
                var recipient = document.getElementById(id + '-recipient').value,
                    message = document.getElementById(id + '-message').value;

                Kandy.chat.sendChat(function (s) {
                    Kandy._callSuccessFunction(element, "send", s, function () {
                        var item = '<li class="collection-item"><h5><b>You: </b></h5><p>' + message + '</p><p class="right"><small>' + new Date().toUTCString() + '</small></p></li>';
                        Kandy._addMessagetoContainers({item: item, type: type});
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

    _renderKandyGroupWidget: function (element) {
        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        element.innerHTML = '<div class="container">'
            + '<div id="' + id + '-groups">'
            + '<div class="center">'
            + '<div class="row">'
            + '<button id="' + id + '-btn-refresh-groups" class="btn">Refresh</button>'
            + '<button id="' + id + '-btn-create-group" class="btn cyan">New</button>'
            + '</div>'
            + '</div>'
            + '<h5><b>Groups list</b></h5>'
            + '<ul id="' + id + '-groups-list" class="collection">'
            + '</ul>'
            + '</div>'
            + '</div>';

        document.getElementById(id + '-btn-refresh-groups').onclick = function () {
            Kandy._refreshGroups(id + '-groups-list');
        };

        document.getElementById(id + '-btn-create-group').onclick = function () {
            var name = prompt("Enter group name");
            if (name != null) {
                Kandy.group.createGroup(function () {
                    Kandy._refreshGroups(id + '-groups-list');
                    alert("Created successfully!");
                }, function (e) {
                    Kandy._defaultErrorAction(e);
                }, name);
            }
        };

        Kandy._refreshGroups(id + '-groups-list');
    },

    _refreshGroups: function (id) {
        Kandy.group.getMyGroups(function (groups) {

            var list = document.getElementById(id);
            list.innerHTML = "";

            for (var i = 0; i < groups.length; ++i) {
                var group = groups[i];

                var item = document.createElement('li');
                item.setAttribute('class', 'collection-item');

                item.innerHTML = '<select class="browser-default" id="' + id + '-group-' + group.id.uri + '" uri="' + group.id.uri + '">'
                    + '<option disabled selected>' + group.name + '</option>'
                    + '<option>Chat room</option>'
                    + '<option>View detail</option>'
                    + '<option value="' + group.isGroupMuted + '">' + (group.isGroupMuted ? 'Unmute' : 'Mute') + '</option>'
                    + '<option>Leave</option>'
                    + (group.selfParticipant.isAdmin ? '<option>Rename</option><option>Delete</option>' : '')
                    + '</select>';

                list.appendChild(item);

                var selector = document.getElementById(id + '-group-' + group.id.uri);
                selector.onchange = function (e) {
                    var selector = e.target;
                    var uri = selector.getAttribute('uri');
                    var idx = selector.selectedIndex;
                    selector.selectedIndex = 0;
                    switch (idx) {
                        case 0: // disabled
                            break;
                        case 1: // Chat room
                            // TODO
                            break;
                        case 2: // View detail
                            Kandy._detailGroupWidget(uri);
                            break;
                        case 3: // Mute/Unmute
                            var state = selector.options[idx].value;
                            if (state == 'true') {
                                Kandy.group.unmuteGroup(function () {
                                    selector.options[idx].value = 'false';
                                    selector.options[idx].text = 'Mute';
                                    Kandy._makeToast('Unmuted');
                                }, function (e) {
                                    Kandy._defaultErrorAction(e);
                                }, uri);
                            } else {
                                Kandy.group.muteGroup(function () {
                                    selector.options[idx].value = 'true';
                                    selector.options[idx].text = 'Unmute';
                                    Kandy._makeToast('Muted');
                                }, function (e) {
                                    Kandy._defaultErrorAction(e);
                                }, uri);
                            }
                            break;
                        case 4:
                        { // Leave
                            var ok = confirm("Are you sure?");
                            if (ok == true) {
                                Kandy.group.leaveGroup(function () {
                                    Kandy._refreshGroups(id + '-groups-list');
                                    alert("Leaved successfully!");
                                }, function (e) {
                                    Kandy._defaultErrorAction(e);
                                }, uri);
                            }
                            break;
                        }
                        case 5: // Rename
                            var name = prompt("Enter new group name");
                            if (name != null) {
                                Kandy.group.updateGroupName(function (group) {
                                    selector.options[0].text = group.name;
                                    alert("Renamed successfully!");
                                }, function (e) {
                                    Kandy._defaultErrorAction(e);
                                }, uri, name);
                            }
                            break;
                        case 6:
                        {// Delete
                            var ok = confirm("Are you sure?");
                            if (ok == true) {
                                Kandy.group.destroyGroup(function () {
                                    Kandy._refreshGroups(id + '-groups-list');
                                    alert("Deleted successfully!");
                                }, function (e) {
                                    Kandy._defaultErrorAction(e);
                                }, uri);
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
        }, function (e) {
            Kandy._defaultErrorAction(e);
        });
    },

    _detailGroupWidget: function (groupId) {
        Kandy.group.getGroupById(function (group) {
            var modal = document.createElement('kandy');
            modal.id = groupId + '-modal';
            modal.innerHTML = '<div class = "modal">'
                + '<div class="modal-content">'
                + '<div class="center">'
                + '<h5><b>' + group.name + '</b></h5>'
                + '<img id="' + group.id.uri + '-group-image" src="" alt="group-thumbnail" width="100%" height="100%">'
                + '<div class="row">'
                + '<div class="btn-group">'
                + '<button id="' + group.id.uri + '-btn-chat" class="btn btn-large">Chat room</button>'
                + '<div class="btn-append">'
                + '<select class="browser-default btn btn-large white-text" id="' + group.id.uri + '-options">'
                + '<option value="" disabled selected>Choose your action</option>'
                + '<option value="' + group.isGroupMuted + '">' + (group.isGroupMuted ? 'Unmute' : 'Mute') + '</option>'
                + '<option>Change image</option>'
                + '<option>Remove image</option>'
                + '<option>Add participant</option>'
                + '</select>'
                + '</div>'
                + '</div>'
                + '</div>'
                + '</div>'
                + '<div class="row">'
                + '<div class="col s8">'
                + '<h5><b>Participants:</b></h5>'
                + '</div>'
                + '<div class="col s4">'
                + '<button id="' + group.id.uri + '-btn-refresh-participants" class="btn btn-small">Refresh</button>'
                + '</div>'
                + '</div>'
                + '<ul id="' + groupId + '-participants-list" class="collection">'
                + '</ul>'
                + '</div>'
                + '</div>'
                + '<div class="modal-overlay"></div>';

            var body = document.getElementsByTagName('body')[0];
            body.appendChild(modal);

            document.getElementById(group.id.uri + '-btn-refresh-participants').onclick = function () {
                Kandy.group.getGroupById(function (updatedGroup) {
                    Kandy._refreshPariticipants(updatedGroup);
                }, function (e) {
                    Kandy._defaultErrorAction(e);
                }, group.id.uri);
            };

            Kandy._refreshPariticipants(group);

            Kandy.group.downloadGroupImageThumbnail(function (uri) {
                document.getElementById(group.id.uri + '-group-image').setAttribute('src', uri);
            }, function (e) {
                Kandy._defaultErrorAction(e);
            }, group.id.uri, Kandy.ThumbnailSize.LARGE);

            modal.onclick = function (e) {
                if (e.target == modal.getElementsByClassName('modal-overlay')[0])
                    modal.remove();
            }

            var selector = document.getElementById(groupId + '-options');
            selector.onchange = function () {
                var idx = selector.selectedIndex;
                selector.selectedIndex = 0;

                switch (idx) {
                    case 0: // disabled
                        break;
                    case 1: // Mute/Unmute
                        var state = selector.options[idx].value;
                        if (state == 'true') {
                            Kandy.group.unmuteGroup(function () {
                                selector.options[idx].value = 'false';
                                selector.options[idx].text = 'Mute';
                                Kandy._makeToast('Unmuted');
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri);
                        } else {
                            Kandy.group.muteGroup(function () {
                                selector.options[idx].value = 'true';
                                selector.options[idx].text = 'Unmute';
                                Kandy._makeToast('Muted');
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri);
                        }
                        break;
                    case 2: // Change group image
                        Kandy.chat.pickImage(function (uri) {
                            Kandy.group.updateGroupImage(function () {
                                document.getElementById(group.id.uri + '-group-image').setAttribute('src', uri);
                                Kandy._makeToast('Updated successfully');
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri, uri);
                        }, function (e) {
                            Kandy._defaultErrorAction(e);
                        });
                        break;
                    case 3: // Delete group image
                        var ok = confirm("Are you sure?");
                        if (ok == true) {
                            Kandy.group.removeGroupImage(function () {
                                document.getElementById(group.id.uri + '-group-image').setAttribute('src', '');
                                Kandy._makeToast('Removed successfully');
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri);
                        }
                        break;
                    case 4: // Add participants
                        var name = prompt("Enter participant name");
                        if (name != null) {
                            Kandy.group.addParticipants(function (updatedGroup) {
                                Kandy._refreshPariticipants(updatedGroup);
                                alert("Added successfully!");
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri, [name]);
                        }
                        break;
                    default:
                        break;
                }
            }
        }, function (e) {
            Kandy._defaultErrorAction(e);
        }, groupId);
    },

    _refreshPariticipants: function (group) {
        var list = document.getElementById(group.id.uri + '-participants-list');
        list.innerHTML = '';
        var participants = group.participants;
        for (var i = 0; i < participants.length; ++i) {
            var participant = participants[i];
            var item = document.createElement('li');
            item.setAttribute('class', 'collection-item');
            item.innerHTML = '<select class="browser-default" id="' + group.id.uri + '-participant-' + participant.uri + '-options" uri="' + participant.uri + '">'
                + '<option disabled selected>' + participant.username + (participant.isAdmin ? ' (admin)' : '') + '</option>'
                + '<option value="' + participant.isMuted + '">' + (participant.isMuted ? 'Unmute' : 'Mute') + '</option>'
                + '<option>Remove</option>'
                + '</select>';
            list.appendChild(item);

            var selector = document.getElementById(group.id.uri + '-participant-' + participant.uri + '-options');
            selector.onchange = function (e) {
                var selector = e.target;
                var uri = selector.getAttribute('uri');
                var idx = selector.selectedIndex;
                selector.selectedIndex = 0;
                switch (idx) {
                    case 0: // disabled
                        break;
                    case 1: // Mute/Unmute
                        var state = selector.options[idx].value;
                        if (state == 'true') {
                            Kandy.group.unmuteParticipants(function () {
                                selector.options[idx].value = 'false';
                                selector.options[idx].text = 'Mute';
                                Kandy._makeToast('Unmuted');
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri, [uri]);
                        } else {
                            Kandy.group.muteParticipants(function () {
                                selector.options[idx].value = 'true';
                                selector.options[idx].text = 'Unmute';
                                Kandy._makeToast('Muted');
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri, [uri]);
                        }
                        break;
                    case 2: // Delete
                        var ok = confirm("Are you sure?");
                        if (ok == true) {
                            Kandy.group.removeParticipants(function (updatedGroup) {
                                Kandy._refreshPariticipants(updatedGroup);
                                alert("Deleted successfully!");
                            }, function (e) {
                                Kandy._defaultErrorAction(e);
                            }, group.id.uri, [uri]);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    },

    _renderKandyPresenceWidget: function (element) {

        if (element == undefined) return;

        var id = this._getIdOrGenerateNextId(element);

        element.innerHTML = '<div class="container">'
            + '<div id="' + id + '-presence">'
            + '<p>Wath for: <span id="'+id+'-usersWatched">none</span></p>'
            + '<p>Onlines: <span id="'+id+'-usersOnline">none</span></p>'
            + '<p>Offlines: <span id="'+id+'-usersOffline">none</span></p>'
            + '<p>Presence Status: <span id="'+id+'-userStatus">none</span></p>'
            + '<div class="center">'
            + '<div class="row">'
            + '<input type="text" id="'+id+'-usersIdWatched" placeholder="recipientID@domain.com"/>'
            + '<button id="' +id+ '-btn-last-seen" class="btn btn-large btn-block">Get Last Seen</button>'
            + '<button id="' +id+ '-btn-start-watch" class="btn btn-large btn-block">Start Watch</button>'
            + '<button id="' +id+ '-btn-stop-watch" class="btn btn-large btn-block">Stop Watch</button>'
            + '<button id="' +id+ '-btn-update-status" class="btn btn-large btn-block">Update self state</button>'
            + '</div>'
            + '</div>'
            + '<h5><b>Presence Status</b></h5>'
            + '<ul id="' + id + '-presence-status-list" class="collection">'
            + '</ul>'
            + '</div>'
            + '</div>';

          document.getElementById(id + '-btn-last-seen').onclick = function () {

            var recipients = document.getElementById(id+'-usersIdWatched').value;

            $("#usersOnline").html("");
            $("#usersOffline").html("");

            Kandy.presence.lastSeen(function (s) {
                document.getElementById(id+'-usersIdWatched').innerHTML(recipients);

                var presences = [], absences = [];

                for (var i = 0; i < s.presences.length; ++i)
                    presences += '[' + s.presences[i].user + ']'

                for (var i = 0; i < s.absences.length; ++i)
                    absences += '[' + s.absences[i] + ']'

                document.getElementById(id+'-usersOnline').innerHTML(presences);
                document.getElementById(id+'usersOffline').innerHTML(absences);
            }, function (e) {
                alert(e);
            }, recipients.split(','));
          };

        document.getElementById(id + '-btn-start-watch').onclick = function () {

          var recipients = document.getElementById(id+'-usersIdWatched').value;

          Kandy.presence.startWatch(function (s) {
              document.getElementById(id+'-usersIdWatched').innerHTML(recipients);
          }, function (e) {
              alert(e);
          }, recipients.split(','));

        };

        document.getElementById(id + '-btn-stop-watch').onclick = function () {
          var recipients = document.getElementById(id+'-usersIdWatched').value;
          Kandy.presence.stopWatch(function (s) {
          }, function (e) {
              alert(e);
          }, recipients.split(','));
        };

        document.getElementById(id+ '-btn-update-status').onclick = function () {
          Kandy._refreshPresenceStatus(id);
        };
    },

    _refreshPresenceStatus: function (id) {
        var recipients = document.getElementById(id+'-usersIdWatched').value;
        var list = document.getElementById(id + '-presence-status-list');
        list.innerHTML = '';

        var item = document.createElement('li');
        item.setAttribute('class', 'collection-item');
        var presenceTypes = '<select class="browser-default" id="presence-status-list">';
        for (var key in Kandy.presenceType) {
          console.log(key, Kandy.presenceType[key]);
          presenceTypes = presenceTypes + '<option value="'+key+'">' + Kandy.presenceType[key] + '</option>';
        }
        presenceTypes = presenceTypes + '</select>';
        item.innerHTML = presenceTypes;
        list.appendChild(item);

        var selector = document.getElementById('presence-status-list');
        selector.onchange = function (e) {
          var selector = e.target;
          var idx = selector.selectedIndex;
            Kandy.presence.updateUserStatus(function (s) {
            }, function (e) {
                alert(e);
            }, recipients.split(','));
        }
    },


    //*** CONFIGURATIONS ***//

    /**
     * Setup API key.
     *
     * @param api The api key.
     * @param secret The api secret key.
     */
    setKey: function (api, secret) {
        exec(null, null, "KandyPlugin", "setKey", [api, secret]);
    },

    /**
     * Set host address.
     *
     * @param url
     */
    setHostUrl: function (url) {
        if (!/^(f|ht)tps?:\/\//i.test(url.toLowerCase())) {
            url = "http://" + url;
        }
        exec(null, null, "KandyPlugin", "setHostUrl", [url]);
    },

    /**
     * Get current host address.
     *
     * @param callback The callback function.
     */
    getHostUrl: function (callback) {
        exec(callback, null, "KandyPlugin", "getHostUrl", []);
    },

    /**
     * Get current plugin configurations report.
     *
     * @param callback The callback function.
     */
    getReport: function (callback) {
        exec(callback, null, "KandyPlugin", "getReport", []);
    },

    /**
     * Get current session.
     *
     * @param success The success callback function.
     */
    getSession: function (success) {
        exec(success, null, "KandyPlugin", "getSession", []);
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
            exec(success, error, "KandyPlugin", "request", [phoneNumber, countryCode]);
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
            exec(success, error, "KandyPlugin", "validate", [phoneNumber, otp, countryCode]);
        },

        /**
         * Signing off the registered account (phone number) from a Kandy.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        deactivate: function (success, error) {
            exec(success, error, "KandyPlugin", "deactivate", []);
        },

        /**
         * Get the details of user.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param phoneNumber The user phone number.
         */
        getUserDetails: function (success, error, phoneNumber) {
            exec(success, error, "KandyPlugin", "getUserDetails", [phoneNumber]);
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
         * Register/login the user on the server by access token.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param token The access token.
         */
        loginByToken: function (success, error, token) {
            exec(success, error, "KandyPlugin", "loginByToken", [token]);
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
            startWithVideo = startWithVideo ? 1 : 0;
            exec(success, error, "KandyPlugin", "createVoipCall", [user, startWithVideo]);
        },

        /**
         * Create a PSTN call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param phoneNumber The user phone number
         */
        createPSTNCall: function (success, error, phoneNumber) {
            exec(success, error, "KandyPlugin", "createPSTNCall", [phoneNumber]);
        },

        /**
         * Create a SIP Trunk call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param phoneNumber The user phone number
         */
        createSIPTrunkCall: function (success, error, phoneNumber) {
            exec(success, error, "KandyPlugin", "createSIPTrunkCall", [phoneNumber]);
        },

        /**
         * Show Local Video in given Dimension.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         * @param left The co-ordinate of X position.
         * @param top The co-ordinate of Y position.
         * @param width The width of of Video that needs to show.
         * @param height The height of of Video that needs to show.
         */
        showLocalVideo: function (success, error, id, left, top, width, height) {
            exec(success, error, "KandyPlugin", "showLocalVideo", [id, left, top, width, height]);
        },

        /**
         * Show Remote Video in given Dimension.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         * @param left The co-ordinate of X position.
         * @param top The co-ordinate of Y position.
         * @param width The width of of Video that needs to show.
         * @param height The height of of Video that needs to show.
         */
        showRemoteVideo: function (success, error, id, left, top, width, height) {
            exec(success, error, "KandyPlugin", "showRemoteVideo", [id, left, top, width, height]);
        },

        /**
         * Hide Local Video.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        hideLocalVideo: function (success, error, id) {
            exec(success, error, "KandyPlugin", "hideLocalVideo", [id]);
        },

        /**
         * Hide Local Video.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        hideRemoteVideo: function (success, error, id) {
            exec(success, error, "KandyPlugin", "hideRemoteVideo", [id]);
        },

        /**
         * Hangup current call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        hangup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "hangup", [id]);
        },

        /**
         * Mute current call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        mute: function (success, error, id) {
            exec(success, error, "KandyPlugin", "mute", [id]);
        },

        /**
         * Unmute current call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        unmute: function (success, error, id) {
            exec(success, error, "KandyPlugin", "unmute", [id]);
        },

        /**
         * Hold current call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        hold: function (success, error, id) {
            exec(success, error, "KandyPlugin", "hold", [id]);
        },

        /**
         * Unhold current call
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        unhold: function (success, error, id) {
            exec(success, error, "KandyPlugin", "unhold", [id]);
        },

        /**
         * Enable sharing video.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        enableVideo: function (success, error, id) {
            exec(success, error, "KandyPlugin", "enableVideo", [id]);
        },

        /**
         * Disable sharing video.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        disableVideo: function (success, error, id) {
            exec(success, error, "KandyPlugin", "disableVideo", [id]);
        },

        /**
         * Switch to front-camera
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        switchFrontCamera: function (success, error, id) {
            exec(success, error, "KandyPlugin", "switchFrontCamera", [id]);
        },

        /**
         * Switch to back-camera
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        switchBackCamera: function (success, error, id) {
            exec(success, error, "KandyPlugin", "switchBackCamera", [id]);
        },

        /**
         * Switch speaker on.
         */
        switchSpeakerOn: function () {
            exec(null, null, "KandyPlugin", "switchSpeakerOn", [])
        },

        /**
         * Switch speaker off.
         */
        switchSpeakerOff: function () {
            exec(null, null, "KandyPlugin", "switchSpeakerOff", [])
        },

        /**
         * Accept current coming call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         * @param videoEnabled Enable video call or not.
         */
        accept: function (success, error, id, videoEnabled) {
            if (videoEnabled == undefined) videoEnabled = false;
            videoEnabled = videoEnabled ? 1 : 0;
            exec(success, error, "KandyPlugin", "accept", [id, videoEnabled]);
        },

        /**
         * Reject current coming call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        reject: function (success, error, id) {
            exec(success, error, "KandyPlugin", "reject", [id]);
        },

        /**
         * Ignore current coming call.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The callee uri.
         */
        ignore: function (success, error, id) {
            exec(success, error, "KandyPlugin", "ignore", [id]);
        },

        /**
         * Is in call.
         *
         * @param callback The callback function
         */
        isInCall: function (callback) {
            exec(callback, null, "KandyPlugin", "isInCall", []);
        },

        /**
         * Is in GSM call.
         *
         * @param callback The callback function
         */
        isInGSMCall: function (callback) {
            exec(callback, null, "KandyPlugin", "isInGSMCall", []);
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
         * @param type The {@link RecordType} to use.
         */
        sendChat: function (success, error, recipient, message, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendChat", [recipient, message, type]);
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
            exec(success, error, "KandyPlugin", "sendSMS", [recipient, message]);
        },

        /**
         * Pick a audio file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         *
         */
        pickAudio: function (success, error) {
            exec(success, error, "KandyPlugin", "pickAudio", []);
        },

        /**
         * Send a audio file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         * @param type The {@link RecordType} to use.
         */
        sendAudio: function (success, error, recipient, caption, uri, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendAudio", [recipient, caption, uri, type]);
        },

        /**
         * Pick a video file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickVideo: function (success, error) {
            exec(success, error, "KandyPlugin", "pickVideo", []);
        },

        /**
         * Send a video file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         * @param type The {@link RecordType} to use.
         */
        sendVideo: function (success, error, recipient, caption, uri, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendVideo", [recipient, caption, uri, type]);
        },

        /**
         * Pick a image file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickImage: function (success, error) {
            exec(success, error, "KandyPlugin", "pickImage", []);
        },

        /**
         * Send a image file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         * @param type The {@link RecordType} to use.
         */
        sendImage: function (success, error, recipient, caption, uri, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendImage", [recipient, caption, uri, type]);
        },

        /**
         * Pick a file file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickFile: function (success, error) {
            exec(success, error, "KandyPlugin", "pickFile", []);
        },

        /**
         * Send a file to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         * @param type The {@link RecordType} to use.
         */
        sendFile: function (success, error, recipient, caption, uri, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendFile", [recipient, caption, uri, type]);
        },

        /**
         * Pick a contact file by android default picker.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pickContact: function (success, error) {
            exec(success, error, "KandyPlugin", "pickContact", []);
        },

        /**
         * Send a contact to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param uri The URI of the file.
         * @param type The {@link RecordType} to use.
         */
        sendContact: function (success, error, recipient, caption, uri, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendContact", [recipient, caption, uri, type]);
        },

        /**
         * Send current location info to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param type The {@link RecordType} to use.
         */
        sendCurrentLocation: function (success, error, recipient, caption, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendCurrentLocation", [recipient, caption, type]);
        },

        /**
         * Send a location info to the recipient.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The destination of the message.
         * @param caption The caption of the file.
         * @param location The location to send.
         * @param type The {@link RecordType} to use.
         */
        sendLocation: function (success, error, recipient, caption, location, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;
            exec(success, error, "KandyPlugin", "sendLocation", [recipient, caption, location, type]);
        },

        /**
         * Open a chooser dialog and send the attachment.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param recipient The recipient user.
         * @param caption The message (caption) of the attachment.
         * @param type The {@link RecordType} to use.
         */
        sendAttachment: function (success, error, recipient, caption, type) {
            if (type == undefined) type = Kandy.RecordType.CONTACT;

            var _success = function (args) {
                switch (args.code) {
                    case Kandy.PickerResult.AUDIO_PICKER_RESULT:
                        Kandy.chat.sendAudio(success, error, recipient, caption, args.uri, type);
                        break;
                    case Kandy.PickerResult.VIDEO_PICKER_RESULT:
                        Kandy.chat.sendVideo(success, error, recipient, caption, args.uri, type);
                        break;
                    case Kandy.PickerResult.IMAGE_PICKER_RESULT:
                        Kandy.chat.sendImage(success, error, recipient, caption, args.uri, type);
                        break;
                    case Kandy.PickerResult.CONTACT_PICKER_RESULT:
                        Kandy.chat.sendContact(success, error, recipient, caption, args.uri, type);
                        break;
                    case Kandy.PickerResult.FILE_PICKER_RESULT:
                        Kandy.chat.sendFile(success, error, recipient, caption, args.uri, type);
                        break;
                    default:
                        break;
                }
            }

            exec(_success, error, "KandyPlugin", "sendAttachment", [recipient, caption, type]);
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
            exec(success, error, "KandyPlugin", "openAttachment", [uri, mimeType]);
        },

        /**
         * Cancel the current media transfer of the message.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         */
        cancelMediaTransfer: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "cancelMediaTransfer", [uuid]);
        },

        /**
         * Download the media file of the message.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         */
        downloadMedia: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "downloadMedia", [uuid]);
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
            exec(success, error, "KandyPlugin", "downloadMediaThumbnail", [uuid, thumbnailSize]);
        },

        /**
         * Send ack to sever for UUID of received/handled message(s).
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the message.
         */
        markAsReceived: function (success, error, uuid) {
            exec(success, error, "KandyPlugin", "markAsReceived", [uuid]);
        },

        /**
         * Pull pending events from Kandy service.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        pullEvents: function (success, error) {
            exec(success, error, "KandyPlugin", "pullEvents", []);
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
            exec(success, error, "KandyPlugin", "createGroup", [name]);
        },

        /**
         * Get group list of user.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getMyGroups: function (success, error) {
            exec(success, error, "KandyPlugin", "getMyGroups", []);
        },

        /**
         * Get group detail by group id.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        getGroupById: function (success, error, id) {
            exec(success, error, "KandyPlugin", "getGroupById", [id]);
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
            exec(success, error, "KandyPlugin", "updateGroupName", [id, newName]);
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
            exec(success, error, "KandyPlugin", "updateGroupImage", [id, uri]);
        },

        /**
         * Remove group image.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        removeGroupImage: function (success, error, id) {
            exec(success, error, "KandyPlugin", "removeGroupImage", [id]);
        },

        /**
         * Get the group image.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        downloadGroupImage: function (success, error, id) {
            exec(success, error, "KandyPlugin", "downloadGroupImage", [id]);
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
            exec(success, error, "KandyPlugin", "downloadGroupImageThumbnail", [id, thumbnailSize]);
        },

        /**
         * Mute the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        muteGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "muteGroup", [id]);
        },

        /**
         * Unmute the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        unmuteGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "unmuteGroup", [id]);
        },

        /**
         * Destroy the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        destroyGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "destroyGroup", [id]);
        },

        /**
         * Leave the group.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param id The id of the group.
         */
        leaveGroup: function (success, error, id) {
            exec(success, error, "KandyPlugin", "leaveGroup", [id]);
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
            exec(success, error, "KandyPlugin", "removeParticipants", [id, participants]);
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
            exec(success, error, "KandyPlugin", "muteParticipants", [id, participants]);
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
            exec(success, error, "KandyPlugin", "unmuteParticipants", [id, participants]);
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
            exec(success, error, "KandyPlugin", "addParticipants", [id, participants]);
        }

    },

    //*** PRESENCE SERVICE ***//
    presence: {

        /**
         * Retrieves the Presence data for the userlist parameter, and returns it in the response callback as success objects.
         * Also returns requested error for which no presence data was found.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param userList The id list of users.
         */
        lastSeen: function (success, error, userList) {
            exec(success, error, "KandyPlugin", "presence", userList);
        },

        /**
         * This method starts watching contacts, which are given in the list.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param userList The id list of users.
         */
        startWatch: function (success, error, userList) {
            exec(success, error, "KandyPlugin", "startWatch", userList);
        },

        /**
         * Stop watching all KandyRecords presence.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param userList The id list of users.
         */
        stopWatch: function (success, error, userList) {
            exec(success, error, "KandyPlugin", "stopWatch", userList);
        },

        /**
         * Updates the user's presence state.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param userList The id list of users.
         */
        updateUserStatus: function (success, error, userList) {
            exec(success, error, "KandyPlugin", "updateStatus", userList);
        },
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
            exec(success, error, "KandyPlugin", "getCountryInfo", []);
        },

        /**
         * Get current location.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getCurrentLocation: function (success, error) {
            exec(success, error, "KandyPlugin", "getCurrentLocation", []);
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
            exec(success, error, "KandyPlugin", "enable", []);
        },

        /**
         * Disable the push service.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        disable: function (success, error) {
            exec(success, error, "KandyPlugin", "disable", []);
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
            exec(success, error, "KandyPlugin", "getDeviceContacts", [filters]);
        },

        /**
         * Get the contacts list from the host domain.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getDomainContacts: function (success, error) {
            exec(success, error, "KandyPlugin", "getDomainContacts", []);
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
            exec(success, error, "KandyPlugin", "getFilteredDomainDirectoryContacts", [filter, searchString]);
        },

        /**
         * Get personal address book.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getPersonalAddressBook: function (success, error) {
            exec(success, error, "KandyPlugin", "getPersonalAddressBook", []);
        },

        /**
         * Add a contact to personal address book.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param contact The {@link Json} contact details.
         */
        addContactToPersonalAddressBook: function (success, error, contact) {
            exec(success, error, "KandyPlugin", "addContactToPersonalAddressBook", [contact]);
        },

        /**
         * Remove contact from personal address book.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param userId The contact.
         */
        removePersonalAddressBookContact: function (success, error, userId) {
            exec(success, error, "KandyPlugin", "removePersonalAddressBookContact", [userId]);
        }
    },

    //*** DEVICE PROFILE SERVICE ***//
    profile: {

        /**
         * Update device profile.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param deviceDisplayName
         * @param deviceName
         * @param deviceFamily
         */
        updateDeviceProfile: function (success, error, deviceDisplayName, deviceName, deviceFamily) {
            exec(success, error, "KandyPlugin", "updateDeviceProfile", [deviceDisplayName, deviceName, deviceFamily]);
        },

        /**
         * Get device profiles.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getUserDeviceProfiles: function (success, error) {
            exec(success, error, "KandyPlugin", "getUserDeviceProfiles", []);
        }
    },

    //*** BILLING SERVICE ***//
    billing: {

        /**
         * Get user credit.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         */
        getUserCredit: function (success, error) {
            exec(success, error, "KandyPlugin", "getUserCredit", []);
        }
    },

    //*** CLOUD STORAGE SERVICE ***//
    cloudStorage: {

        /**
         * Upload a file to cloud storage.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uri The file uri
         */
        uploadMedia: function (success, error, uri) {
            exec(success, error, "KandyPlugin", "uploadMedia", [uri]);
        },

        /**
         * Download a file from cloud storage.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the file.
         * @param filename The name of the file.
         */
        downloadMedia: function (success, error, uuid, filename) {
            exec(success, error, "KandyPlugin", "downloadMediaFromCloudStorage", [uuid, filename]);
        },

        /**
         * Download the thumbnail of the file from cloud storage.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the file.
         * @param filename The name of the file.
         * @param thumbnailSize The {@link ThumbnailSize} of the media.
         */
        downloadMediaThumbnail: function (success, error, uuid, filename, thumbnailSize) {
            exec(success, error, "KandyPlugin", "downloadMediaThumbnailFromCloudStorage", [uuid, filename, thumbnailSize]);
        },

        /**
         * Cancel a download process.
         *
         * @param success The success callback function.
         * @param error The error callback function.
         * @param uuid The UUID of the file.
         * @param filename The name of the file.
         */
        cancelMediaTransfer: function (success, error, uuid, filename) {
            exec(success, error, "KandyPlugin", "cancelMediaTransferFromCloudStorage", [uuid, filename]);
        },

        /**
         * Get local files list
         *
         * @param callback The callback function
         */
        getLocalFiles: function (callback) {
            exec(callback, null, "KandyPlugin", "getLocalFiles", []);
        }
    }
};

module.exports = Kandy;
