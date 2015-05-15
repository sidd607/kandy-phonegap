var app = {
    // Application Constructor
    initialize: function () {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function () {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function () {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function (id) {

        // Initialize Kandy plugin
        Kandy.initialize({
            apiKey: "DAK81e603cf961e4c0295aa8e665828913d",
            apiSecret: "DAS89933b8fc32949dc84eeebfabadfddee"
        });

        Kandy.onChatReceived = function (args) {
            console.log(args);
            refreshUI();
        }

        var pages = ["call", "chat", "group", "presence", "location", "push", "address-book"];
        for (var i = 0; i < pages.length; ++i)
            $(document).on("pagebeforeshow", "#" + pages[i], function () {
                Kandy.access.getConnectionState(function (state) {
                    if (state != Kandy.ConnectionState.CONNECTED) {
                        $.mobile.changePage("#access");
                    }
                })
            });
    }
};


function refreshUI() {
    $(".ui-mobile").trigger('create');
}


function pushEnable() {
    Kandy.push.enable(function () {
        $("#pushState").html("enabled");
    }, function (e) {
        alert(e);
    });
}


function pushDisable() {
    Kandy.push.disable(function () {
        $("#pushState").html("disabled");
    }, function (e) {
        alert(e);
    });
}

function startWatch() {
    var recipients = $("#usersIdWatched").val();

    $("#usersOnline").html("");
    $("#usersOffline").html("");

    Kandy.presence.startWatch(function (s) {
        $("#usersWatched").html(recipients);

        var presences = [], absences = [];

        for (var i = 0; i < s.presences.length; ++i)
            presences += '[' + s.presences[i].user + ']'

        for (var i = 0; i < s.absences.length; ++i)
            absences += '[' + s.absences[i] + ']'

        $("#usersOnline").html(presences);
        $("#usersOffline").html(absences);
    }, function (e) {
        alert(e);
    }, recipients.split(','));
}

function getCountryInfo() {
    Kandy.location.getCountryInfo(function (s) {
        $("#countryCode").html(s.code);
        $("#countryNameLong").html(s.long);
        $("#countryNameShort").html(s.short);
    }, function (e) {
        alert(e);
    });
}

function getCurrentLocation() {
    $("#currentLocationInfo").html("");

    Kandy.location.getCurrentLocation(function (s) {
        $("#currentLocationInfo").html("<br /> Current location info:<br />" + JSON.stringify(s));
    }, function (e) {
        alert(e);
    });
}

function getDeviceContacts() {
    $("#addressBooks").html("");

    Kandy.addressBook.getDeviceContacts(function (s) {
        $("#addressBooks").html(JSON.stringify(s));
    }, function (e) {
        alert(e);
    }, [Kandy.DeviceContactsFilter.HAS_EMAIL_ADDRESS]);
}

function getDomainContacts() {
    $("#addressBooks").html("");

    Kandy.addressBook.getDomainContacts(function (s) {
        $("#addressBooks").html(JSON.stringify(s));
    }, function (e) {
        alert(e);
    });
}

function getGroups() {
    Kandy.group.getMyGroups(function (s) {
        if (s.length == 0) {
            $("#kandy-groups").html("<b>0 group found.</b>")
        } else {
            $("#kandy-groups").html("");
            for (var i = 0; i < s.length; ++i) {
                var group = s[i];
                $("#kandy-groups").append('<li><a onclick="viewGroup(\'' + group.id.uri + '\')">' + group.name + '</a></li>');
            }
        }
    }, function (e) {
        alert(e);
    })
}

function createGroup() {
    var name = prompt("Enter group name");
    if (name != null) {
        Kandy.group.createGroup(function (group) {
            $("#kandy-groups").append('<li><a onclick="viewGroup(\'' + group.id.uri + '\')">' + group.name + '</a></li>');
            alert("Created successfully!");
        }, function (e) {
            alert(e);
        }, name);
    }
}

function viewGroup(id) {
    Kandy.group.getGroupById(function (group) {
        $("#group-id").val(group.id.uri)
        $("#group-detail h1").html(group.name);
        $("#group-participants").html("");
        var participants = group.participants;
        for (var i = 0; i < participants.length; ++i) {
            var item = participants[i].uri;
            if (participants[i].isAdmin)
                item = "<b>" + item + "</b>";
            $("#group-participants").append("<li>" + item + "</li>");
        }

        Kandy.group.downloadGroupImageThumbnail(function (uri) {
            $("#group-img").attr("src", uri).show();
        }, function () {
        }, id, Kandy.ThumbnailSize.LARGE);

        var mute = $("#group-mute-state");
        if (group.isGroupMuted) mute.html("Unmute");
        else mute.html("Mute");

        $.mobile.changePage("#group-detail");
    }, function (e) {
        alert(e);
    }, id);
}

function chatRoom() {
    $("#group-chat-room h1").html($("#group-detail h1").html());
    $("#kandy-group-chat-room-recipient").val($("#group-id").val());
    $.mobile.changePage("#group-chat-room");
}

function leaveGroup() {
    var id = $("#group-id").val();
    var ok = confirm("Are you sure?");
    if (ok == true) {
        Kandy.group.leaveGroup(function () {
            getGroups();
            $.mobile.changePage("#group");
            alert("Leaved successfully!");
        }, function (e) {
            alert(e)
        }, id);
    }
}

function renameGroup() {
    var id = $("#group-id").val();
    var name = prompt("Enter new group name");
    if (name != null) {
        Kandy.group.updateGroupName(function (group) {
            $("#group-detail h1").html(group.name);
            getGroups();
            alert("Renamed successfully!");
        }, function (e) {
            alert(e);
        }, id, name);
    }
}

function deleteGroup() {
    var id = $("#group-id").val();
    var ok = confirm("Are you sure?");
    if (ok == true) {
        Kandy.group.destroyGroup(function () {
            getGroups();
            $.mobile.changePage("#group");
            alert("Deleted successfully!");
        }, function (e) {
            alert(e)
        }, id);
    }
}

function muteGroup() {
    var id = $("#group-id").val();
    var mute = $("#group-mute-state");
    if (mute.html() == "Mute") {
        Kandy.group.muteGroup(function (group) {
            mute.html("Unmute");
        }, function (e) {
            alert(e);
        }, id);
    } else {
        Kandy.group.unmuteGroup(function (group) {
            mute.html("Mute");
        }, function (e) {
            alert(e);
        }, id);
    }
}

function changeGroupImage() {
    var id = $("#group-id").val();
    Kandy.chat.pickImage(function (uri) {
        Kandy.group.updateGroupImage(function (group) {
            $("#group-img").attr("src", uri).show();
            alert("Changed successfully!");
        }, function (e) {
            alert(e);
        }, id, uri);
    }, function (e) {
        alert(e);
    })
}

function removeGroupImage() {
    var id = $("#group-id").val();
    Kandy.group.removeGroupImage(function (group) {
        $("#group-img").hide();
        alert("Removed successfully!");
    }, function (e) {
        alert(e);
    }, id);
}

function addParticipant() {
    var id = $("#group-id").val();
    var name = prompt("Enter participant name");
    if (name != null) {
        Kandy.group.addParticipants(function (group) {
            $("#group-participants").html("");
            var participants = group.participants;
            for (var i = 0; i < participants.length; ++i) {
                var item = participants[i].uri;
                if (participants[i].isAdmin)
                    item = "<b>" + item + "</b>";
                $("#group-participants").append("<li>" + item + "</li>");
            }
            alert("Added successfully!");
        }, function (e) {
            alert(e);
        }, id, [name]);
    }
}

function removeParticipant() {
    var id = $("#group-id").val();
    var name = prompt("Enter participant name");
    if (name != null) {
        Kandy.group.removeParticipants(function (group) {
            $("#group-participants").html("");
            var participants = group.participants;
            for (var i = 0; i < participants.length; ++i) {
                var item = participants[i].uri;
                if (participants[i].isAdmin)
                    item = "<b>" + item + "</b>";
                $("#group-participants").append("<li>" + item + "</li>");
            }
            alert("Removed successfully!");
        }, function (e) {
            alert(e);
        }, id, [name]);
    }
}

function muteParticipant() {
    var id = $("#group-id").val();
    var name = prompt("Enter participant name");
    if (name != null) {
        Kandy.group.muteParticipants(function () {
            alert("Muted successfully!");
        }, function (e) {
            alert(e);
        }, id, [name]);
    }
}

function unmuteParticipant() {
    var id = $("#group-id").val();
    var name = prompt("Enter participant name");
    if (name != null) {
        Kandy.group.unmuteParticipants(function () {
            alert("Unmuted successfully!");
        }, function (e) {
            alert(e);
        }, id, [name]);
    }
}