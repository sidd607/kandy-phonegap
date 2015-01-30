var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {

        // Setup Kandy PhoneGap plugin
        Kandy.setup({
            listeners: {
                onChatReceived: function (msg) {
                    messages[msg.message] = msg;
                    handleMessageReceived(msg.message);
                },
                onChatDelivered: function(msg) {
                },
                onPresenceChanged: function(presence) {
                    $("#userWatched").html(presence.user);
                    $("#userState").html(presence.state);
                }
            }
        });

        // Load previous session
        Kandy.access.getSession(function(s){
            $("userInfo").html(s.user + '@' + s.domain);
            $("#loginForm").hide();
            $("#userInfo").show();
        }, function(e){});
    }
};

var sender;
var messages = {};
var countryCode = "VN";

function requestCode() {
    var phoneNumber = $("#phoneNumber").val();
    Kandy.provisioning.requestCode(function(){
        alert("Signed up successfully");
        $("#userSigned").html(phoneNumber);
    }, function(e){
        alert(e);
    }, phoneNumber, countryCode);
}

function validate(){
    var otpCode = $("#otpCode").val(),
        phoneNumber = $("#phoneNumber").val();

    Kandy.provisioning.validate(function(){
        alert("Validation successed");
    }, function(e){
        alert(e);
    }, phoneNumber, otpCode, countryCode);
}

function deactivate(){
    Kandy.provisioning.deactivate(function(){
        alert("Sigoff successed");
        $("#userSigned").hmtl("");
    }, function(e){
        alert(e);
    });
}

function login(){
    var username = $("#username").val(),
        password = $("#password").val();

    sender = username;

    Kandy.access.login(function(s){
        $("#loginForm").hide();
        $("#userInfo").html(username);
        $("#userInfo").show();
    }, function(e){
        alert(e);
    }, username, password)
}

function logout(){
    Kandy.access.logout(function(s){
        $("#userInfo").hide();
        $("#loginForm").show();
    }, function(e){
        alert(e);
    })
}

function voiceCall(){
    var username = $("#username").val(),
        startWithVideo = $("#startWithVideo").is(':checked');

    Kandy.call.makeVoiceCall(function(s){

    }, function(e){
        alert(e);
    }, username);
}

function makeCallDlg(){
    var username = $("#callee").val(),
        startWithVideo = $("#startWithVideo").is(':checked');

    Kandy.call.makeCallDialog(function(s){

    }, function(e){
        alert(e);
    }, [{
        phoneNumber: username,
        startWithVideo: startWithVideo,
    }]);   
}

function hangup(){
    Kandy.call.hangup(function(){
        alert("Call ended.");
    }, function(e){
        alert(e);
    });
}

function mute(){
    if($("#btnMute").text() == "Mute"){
        Kandy.call.mute(function(){
            alert("Call muted.");
        }, function(e){
            alert(e);
        });
        $("#btnMute").text("unMute");
    } else {
        Kandy.call.unMute(function(){
            alert("Call unmuted.");
        }, function(e){
            alert(e);
        });
        $("#btnMute").text("unMute");
    }
}

function hold(){
    if($("#btnHold").text() == "Hold"){
        Kandy.call.hold(function(){
            alert("Call holded.");
        }, function(e){
            alert(e);
        });
        $("#btnHold").text("unHold");
    } else {
        Kandy.call.unHold(function(){
            alert("Call unholded.");
        }, function(e){
            alert(e);
        });
        $("#btnHold").text("Hold");
    }
}

function video(){
    if($("#btnVideo").text() == "Video"){
        Kandy.call.enableVideo(function(){
            alert("Video call enabled.");
        }, function(e){
            alert(e);
        });
        $("#btnVideo").text("noVideo");
    } else {
        Kandy.call.disableVideo(function(){
            alert("Video call disabled.");
        }, function(e){
            alert(e);
        });
        $("#btnVideo").text("Video");
    }
}

function sendMessage() {
    var recipient = $("#recipient").val(),
        message = $("#message").val();
    Kandy.chat.send(function(s){
        var item = '<li class="ui-li-static ui-body-inherit"><h3>' + sender + '</h3><p>' + message + '</p><p class="ui-li-aside"></p></li>';
        $("#chatArea").prepend(item);
    }, function(e){
        alert(e);
    }, recipient, message);
}

function handleMessageReceived(msg){
    var item = '<li class="ui-li-static ui-body-inherit" onClick="markAsReceived(\'' + msg.UUID + '\')"><h3>' + msg.sender + '</h3><p id="' + msg.UUID +'"><strong>' + msg.message.text + '</strong></p><p class="ui-li-aside">' + msg.timestamp + '</p></li>';
    $("#chatArea").prepend(item);
}

function pullEvents(){
    Kandy.chat.pullEvents();
}

function markAsReceived(uuid){
    Kandy.chat.markAsReceived(function(){
        var message = $("#" + uuid).text();
        $("#" + uuid).html(message);
    }, function(e){
        alert(e);
    }, uuid);
}

function pushEnable(){
    Kandy.push.enable(function(){
        $("#pushState").html("enabled");
    }, function(e){
        alert(e);
    });
}

function pushDisable(){
    Kandy.push.disable(function(){
        $("#pushState").html("disabled");
    }, function(e){
        alert(e);
    });
}

function startWatch() {
    var recipient = $("#userIdWatched").val();
    $("#userWatched").html(recipient);
    Kandy.presence.startWatch(function(){   
    }, function(e){
        alert(e);
    }, [recipient]);
}

function getCountryInfo() {
    Kandy.location.getCountryInfo(function(s){
        $("#countryCode").html(s.code);
        $("#countryNameLong").html(s.nameLong);
        $("#countryNameShort").html(s.nameShort);
    }, function(e){
        alert(e);
    });
}