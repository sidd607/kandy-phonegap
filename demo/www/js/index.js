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

        Kandy.initialize(); // Use default configurations
    }
};

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