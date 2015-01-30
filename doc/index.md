<!---
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# com.kandy.phonegap
[**Kandy**](http://www.kandy.io/) is a full-service cloud platform that enables real-time communications for business applications. **KandyPhonegap** is a [Cordova](http://cordova.apache.org/) plugin that makes it easy to use Kandy API with Cordova/PhoneGap.

**Kandy** homepage: [kandy.io](http://www.kandy.io/)
## Supported Platforms
- Android
- IOS (comming soon)

## Installation
Check out PhoneGap CLI [docs](http://docs.phonegap.com/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface)
before starting out.

    cordova plugin add org.apache.cordova.device --variable API_KEY=<api-key> --variable API_SECRET=<api-secret>
or

    cordova plugin add https://github.com/kodeplusdev/kandyphonegap.git --variable API_KEY=<api-key> --variable API_SECRET=<api-secret>
## Getting Started
This plugin defines a global `Kandy` object, which provide an easy way to interact with KANDY.
Although the object is in the global scope, it is not available until after the `deviceready` event.
    
```js
    document.addEventListener("deviceready", onDeviceReady, false);
    function onDeviceReady() {
        console.log(Kandy);
    }
```
After the `deviceready` event, you should config and register listeners to receive events from KANDY background service
```js
    function onDeviceReady(){
        ...
        Kandy.setup({
            startWithVideo: true,
            listeners: {
                onIncomingCall: function(callee){
                    // YOUR CODE GOES HERE
                },
                onChatReceived: function(message){
                    // YOUR CODE GOES HERE
                },
                onChatDelivered: function(message){
                    // YOUR CODE GOES HERE
                },
                ...
                onPresenceChanged: function(presence){
                    // YOUR CODE GOES HERE
                },
                ...
            },
        });
    }
```
After you setup `KandyPlugin`, you can use `Kandy` with following syntax:
```js
    Kandy.access.login(function(){
        // successCallback...
    }, function(e){
        // errorCallback
    }, username, password);
```
See [API Reference](#api-reference) for more details.
## API Reference
### Configurations
**setup**(*config*)

Setup `KandyPlugin` based on `config` parameters. The `config` parameter is an object with the following properties:
- `startWithVideo` (boolean) - Starts a call to/coming with video enabled.
- `useNativeDialog` (boolean) - Use native dialog for making a (video) call, alerting coming call.
- `listeners` (object) - Registers listeners to receive events from KandyPlugin background service. There are a number of events for which you may register:  `onIncomingCall`, `onVideoStateChanged`, `onAudioStateChanged`, `onCallStateChanged`, `onGSMCallIncoming`, `onGSMCallConnected`, `onGSMCallDisconnected`, `onChatReceived`, `onChatDelivered`, `onChatMediaDownloadProgress`, `onChatMediaDownloadFailed`, `onChatMediaDownloadSucceded`, `onDeviceAddressBookChanged` and `onPresenceChanged`.

**onIncomingCall**(*callee*)

Called when a call is coming. The `callee` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `callee` (string) - The uri of caller (*callee@domain.com*).
- `via` (string) - Where the call make from.

**onVideoStateChanged**(*state*)

Called when the video of the call change states. The `state` parameter is an object with the following properties:
- `isReceivingVideo` (boolean) - Determines whether or it is receiving video.
- `isSendingVideo` (boolean) - Determines whether or not it is sending video.

**onAudioStateChanged**(*state*)

Called when the audio of the call change states. The `state` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `isMute` (boolean) - Determines whether or not the audio is muted.
 
**onCallStateChanged**(*state*)

Called when the call change states. The `state` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `state` (string) - The name of current call state (holding, terminated, ...).

**onGSMCallIncoming**(*callee*)

Called when a GSM call is in coming.
- `id` (string) - The ID of the call.
- `callee` (string) - The uri of caller (*callee@domain.com*).
- `via` (string) - Where the call make from.

**onGSMCallConnected**(*callee*)
Called when a GSM call is in connected.
- `id` (string) - The ID of the call.
- `callee` (string) - The uri of caller (*callee@domain.com*).
- `via` (string) - Where the call make from.

**onGSMCallDisconnected**(*callee*)
Called when a GSM call is in disconnected.
- `id` (string) - The ID of the call.
- `callee` (string) - The uri of caller (*callee@domain.com*).
- `via` (string) - Where the call make from.

**onChatReceived**(*message*)

Called when there is a comming message. The `message` parameter is an object message with `chat` type, sent to the user by another user. Here's an example:
```json
    {
        "message": [
        {
            "UUID":"46242C4A-D47A-4098-8DE1-19A5482D1F0F",
            "messageType":"chat",
            "timestamp":1419979765811,
            "sender": "user2@joecool.com",
            "contentType":"text",
            "message":{
                "mimeType":"text/plain",
                "text":"hello world"
            }
        }
        ]
    }
```
**onChatDelivered**(*message*)

Called when a message the user sent was received by the recipient's application. The `message` parameter is an object message with `chatRemoteAck` type. Here's an example of a message:
```json
    {
        "message": [
            {
                "UUID": "4D9D8213-AD6A-4F6D-B0D2-AE02BB95C06B",
                "timestamp": 1412201946514
            }
        ]
    }
```
**onChatMediaDownloadProgress**(*message*, *progress*)

Called when a chat media is in download process.
- `message` (object)
- `process` (integer)

**onChatMediaDownloadFailed**(*message*, *error*)

Called when a chat media download failed.
- `message` (object)
- `error` (string)

**onChatMediaDownloadSucceded**(*message*, *uri*)

Called when a chat media download successed.
- `message` (object)
- `uri` (string)

**onPresenceChanged**(*presence*)

Called when the users change the presence. The `presence` parameter is an object with the following properties:
- `user` (string) - The contact name.
- `state` (string) - the contact state.

**onDeviceAddressBookChanged**()

In order to receive local address book related events.

### Provisioning (namespace `provisioning`)
**requestCode**(*sucessCallback*, *errorCallback*, *userId*, *twoLetterCountryCode*)

Request code for verification and registration process.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `userId` (string) - The user's phone number.
- `twoLetterCountryCode` (string) - The two letter the country code. To retrieve the two letter country code, you can use location service `location.getCountryInfo()`.

**validate**(*successCallback*, *errorCallback*, *userId*, *otpCode*, *twoLetterCountryCode*)

Validation of the signed up phone number send received code to the server.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `userId` (string) - The user's phone number.
- `twoLetterCountryCode` (string) - The two letter the country code. To retrieve the two letter country code, you can use location service `location.getCountryInfo()`.
- `otpCode` (string) - The OTP code received.

**deactivate**(*successCallback*, *errorCallback*)

Signing off the registered account(phone number) from a Kandy.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

### Access (namespace `access`)
**login**(*successCallback*, *errorCallback*, *username*, *password*)

Register/login the user on the server with credentials received from admin.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `username` (string) - The user's username.
- `password` (string) - The user's password

**logout**(*successCallback*, *errorCallback*)

This method unregisters user from the Kandy server.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**getSession**(*successCallback*, *errorCallback*)

Get the current session.
- `sucessCallback` (function) - Called when the request was successed. Parameter is an object with the properties: `userId`, `user`, `domain` and `deviceId`.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

### Call (namespace `call`)
**makeCallDialog**(*successCallback*, *errorCallback*, *config*)

Create a call dialog (use native dialog).
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `config` (object) - Initialize the (video) call dialog. The `config` parameters is an object with the properties: `phoneNumber`, `startWithVideo` and `buttons`. `buttons` is an object array with the properties: `type` and `text`.

**makeVoiceCall**(*successCallback*, *errorCallback*, *phoneNumber*)

Create a voice call
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `phonNumber` (string) - The callee number.

**hangup**(*successCallback*, *errorCallback*)

Hangup current call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**mute**(*successCallback*, *errorCallback*)

Mute current call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**unMute**(*successCallback*, *errorCallback*)

Unmute current call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**hold**(*successCallback*, *errorCallback*)

Hold currnet call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**unHold**(*successCallback*, *errorCallback*)

Unhold current call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**enableVideo**(*successCallback*, *errorCallback*)

Enable sharing video for current call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**disableVideo**(*successCallback*, *errorCallback*)

Disable sharing video for current call.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

### Chat (namespace `chat`)
**send**(*successCallback*, *errorCallback*, *recipient*, *message*)

Send the message to recipient.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `recipient` (string) - The recipient to receive the message.
- `message` (string) - The message to send.

**markAsReceived**(*successCallback*, *errorCallback*, *UUID*)

Send ack to sever for UUID of received/handled message(s).
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `UUID` (string/string array) - The uuid(s) of the message(s) to mark.

**pullEvents**()

Pull pending events from Kandy service.

### Presence (namespace `presence`)
**startWatch**(*successCallback*, *errorCallback*, *userList*)

Register listener for presence's callbacks/notifications
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `userList` (string array) - The list users needed to watched.

### Location (namespace `location`)
**getCountryInfo**(*successCallback*, *errorCallback*)

Get the country info.
- `sucessCallback` (function) - Called when the request was successed. The parameters is an object with the properties: `code`, `nameLong` and `nameShort`.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

### Push (namespace `push`)
**enable**(*successCallback*, *errorCallback*)

Enable the push service.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

**disable**(*successCallback*, *errorCallback*)

Disable the push service.
- `sucessCallback` (function) - Called when the request was successed. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).

### Address book (namespace `addressBook`)
**getDeviceContacts**(*successCallback*, *errorCallback*, *filters*)

Receive your local device contacts with simple Kandy API.
- `sucessCallback` (function) - Called when the request was successed. The parameters is an object with the properties: `size`, `contacts` (object array). The `contacts` is an array object with the properties: `displayName`, `emails` (object array: `address`, `type`), `phones` (object array: `number`, `type`).
- `errorCallback` (function) - Called when the request was failed. Parameters: `errorMessage` (string).
- `filters` (string array) - The name of filters (ALL, HAS_EMAIL_ADDRESS, HAS_PHONE_NUMBER, IS_FAVORITE).

## Troubleshooting

## License