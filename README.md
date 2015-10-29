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
- IOS

## Installation
Check out PhoneGap CLI [docs](http://docs.phonegap.com/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface)
before starting out.

    cordova plugin add "path/to/kandy-phonegap/directory"
or

    cordova plugin add https://github.com/kodeplusdev/kandyphonegap.git
## Getting Started
This plugin defines a global `Kandy` object, which provide an easy way to interact with KANDY.
Although the object is in the global scope, it is not available until after the `deviceready` event. After the `deviceready` event, you must initialize to  config and register listeners to use Kandy plugin.

```js
    function onDeviceReady(){
        ...
        Kandy.initialize({
            apiKey: "api",
            secretKey: "secret",
            hostUrl: "https://api.kandy.io"
        });
    }
```
After you initialize the `KandyPlugin`, you can use `Kandy` with following syntax:
```js
    Kandy.access.login(function(s){ // successCallback function
        // your code here
    }, function(e){ // errorCallback function
        // your code here
    }, username, password);
```
or as a widget:
```html
    <kandy widget="call" call-success="callSuccess"></kandy>
```
See [API Reference](#api-reference) for more details.

**Note: To use Kandy plugin, you have to setup `apiKey` and `secretKey` via `initialize` or `setKey` function**

## How to use example codes
**Create the App**

Go to the directory where you maintain your source code, and run a command such as the following:
```shell
    phonegap create hello com.example.hello HelloWorld
```
Then, copy example source codes from [`demo`](/demo) directory of this plugin to your app directory

**Add android platform**

Run a command such as the following:
```shell
    phonegap platform add android
```
**Add iOS platform**

Run a command such as the following:
```shell
    phonegap platform add ios
```
**Add plugin**

Go to your app directory and run a command such as the following:
```shell
    phonegap plugin add directory/to/kandy-phonegap/plugin
```
**Build the App Android**

Run a command such as the following:
```shell
    phonegap build android
```
**Build the App iOS**

Run a command such as the following:
```shell
    phonegap build iOS
```
## Widgets
The plugin provides several widgets that you can easily use Kandy.
### Provisioning widget
The `widget` attribute is `provisioning`. Some callback actions you can use: `request`, `validate`, `deactivate`

Example:
```html
    <kandy widget="provisioning" request-success="onRequestSuccess" request-error="onRequestError"></kandy>
```
### Access widget
The `widget` attribute is `access`. Some callback actions you can use: `login`, `logout`

Example:
```html
    <kandy widget="access" login-success="onLoginSuccess"></kandy>
```
### Call widget
The `widget` attribute is `call`. The callback actions you can use: `call`.
Default call widget type is `voip`. If you want to use `PSTN`, you can add `type="PSTN"`. Other widget-specific attributes: `type`, `call-to`, `label`, and `start-with-video`.

Example:
```html
    <kandy widget="call"></kandy>
```
```html
    <kandy widget="call" type="PSTN" call-to="0123456789" label="Call Us"></kandy>
```
### Chat widget
The `widget` attribute is `chat`. Some callback actions you can use: `send`, `pull`.
Default chat wisget type is `chat`. If you want to use `SMS`, you can add `type="SMS"`. Other widget-specific attributes: `type` and `send-to`.

Example
```html
    <kandy widget="chat" send-success="onSendSuccess"></kandy>
```
```html
    <kandy widget="chat" type="sms" send-to="0123456789"></kandy>
```
## API Reference
### Configurations
**initialize**(*config*)

Initialize the `KandyPlugin` with default configuration values. The `config` parameter is an object with the following properties:
- `apiKey` (string) - The api key.
- `secretKey` (string) - The secret key.
- `hostUrl` (string) - The Kandy host URL.
- `startWithVideo` (boolean) - Create call with video enabled default.
- `downloadMediaPath` (string) - Where to save downloaded media.
- `mediaMaxSize` (int) - The max size of the media.
- `autoDownloadMediaConnectionType` (string)  - The connectionType to download media.
- `autoDownloadThumbnailSize` (string) - The thumbnailSize.
- `useNativeCallView` (boolean) - Use native call dialog default.

**setKey**(*apiKey*, *secretKey*)

Setup Kandy key.
- `apiKey` (string) - The api key.
- `secretKey` (string) - The secret key.

**setHostUrl**(*url*)

Setup Kandy host url.
- `url` (string) - The Kandy host url.

**onConnectionStateChanged**(*state*)

Called when the connection state is changed.
- `state` (string) - The The KandyConnectionState value.

**onSocketConnected**()

Called when the socket is connected.

**onSocketConnecting**()

Called when the socket is connecting.

**onSocketDisconnected**()

Called when the socket is disconnected.

**onSocketFailedWithError**(*error*)

Called when the the socket connection is failed.
- `error` (string) - The error string value.
 
**onInvalidUser**(*error*)

Called when the user is invalid.
- `error` (string) - The error string value.

**onSessionExpired**(*error*)

Called when the session is expired.
- `error` (string) - The error string value.

**onSDKNotSupported**(*error*)

Called when the current SDK is not supported.
- `error` (string) - The error string value.

**onIncomingCall**(*args*)

Called when a call is coming. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.

**onMissedCall**(*args*)

Called when there is a missed call. The `args` paremeter is an object with the following properties:
- `uuid` (string) - The UUID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.
- `timestamp` (long) - When the call was missed. 

**onVideoStateChanged**(*args*)

Called when the video of the call change states. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.
- `receiving` (boolean) - Determines whether or it is receiving video.
- `sending` (boolean) - Determines whether or not it is sending video.

**onAudioStateChanged**(*args*)

Called when the audio of the call change states. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.
- `state` (boolean) - Determines whether or not the audio is muted.
 
**onCallStateChanged**(*args*)

Called when the call change states. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.
- `state` (string) - The name of current call state (holding, terminated, ...).

**onGSMCallIncoming**(*args*)

Called when a GSM call is in coming. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.

**onGSMCallConnected**(*args*)

Called when a GSM call is in connected. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.

**onGSMCallDisconnected**(*args*)

Called when a GSM call is in disconnected. The `args` parameter is an object with the following properties:
- `id` (string) - The ID of the call.
- `uri` (string) - The uri of caller (Ex. *callee@domain.com*).
- `via` (string) - Where the call make from.

**onChatReceived**(*args*)

Called when there is a comming message. The `args` parameter is an object with the following properties:
- `type` (string) - The type of the message.
- `message` (object) The object message with `chat` type, sent to the user by another user. Here's an example:
```json
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
    },
```
**onChatDelivered**(*ack*)

Called when a message the user sent was received by the recipient's application. The `ack` parameter is an object message with `chatRemoteAck` type. Here's an example of a message:
```json
    {
        "UUID": "4D9D8213-AD6A-4F6D-B0D2-AE02BB95C06B",
        "timestamp": 1412201946514
    }
```
**onChatMediaAutoDownloadProgress**(*args*)

Called when a chat media is in downloading state. The `args` parameter is an object with the following properties:
- `message` (object) - The message object.
- `process` (integer) - The current process value.
- `state` (string) - The current process state.
- `byteTransfer` (long) - The bye transfer value.
- `byteExpected` (long) - The byte expected value.

**onChatMediaAutoDownloadFailed**(*args*)

Called when a chat media download failed. The `args` parameter is an object with the following properties:
- `message` (object) - The message object.
- `error` (string) - The error string.
- `code` (int) - The error code.

**onChatMediaAutoDownloadSucceded**(*args*)

Called when a chat media download successful. The `args` parameter is an object with the following properties:
- `message` (object) - The message obect.
- `uri` (string) - The uri of the media file.

**onDeviceAddressBookChanged**()

In order to receive local address book related events.

**onGroupDestroyed**(*args*)

Called when there is a group destroyed. The `args` parameter is an object with the following properties:
- `id` (string) - The uri of the group.
- `uuid` (string) - The UUID of the group.
- `timestamp` (long) - The timestamp when the group was destroyed.
- `eraser` (string) - The uri of the eraser user.

**onGroupUpdated**(*args*)

Called when there is a group updated. The `args` parameter is an object with the following properties:
- `id` (string) - The uri of the group.
- `uuid` (string) - The UUID of the group.
- `timestamp` (long) - The timestamp when the group was destroyed.
- `groupParams` (object) - The params of the group (`name`, `image`).

**onParticipantJoined**(*args*)

Called when there is a participant joined. The `args` parameter is an object with the following properties:
- `uuid` (string) - The UUID of the group.
- `groupId` (object) - The group object (`uri`, `type`, `domain`, `username`).
- `inviter` (object) - The inviter (`uri`, `type`, `domain`, `username`).
- `timestamp` (long) - The timestamp when the participant joined.
- `invitees` (array) - The invitees array (`uri`, `type`, `domain`, `username`);

**onParticipantKicked**(*args*)

Called when there is a participant kicked. The `args` parameter is an object with the following properties:
- `uuid` (string) - The UUID of the group.
- `groupId` (object) - The group object (`uri`, `type`, `domain`, `username`).
- `booter` (object) - The booter (`uri`, `type`, `domain`, `username`).
- `timestamp` (long) - The timestamp when the participant joined.
- `booted` (array) - The invitees array (`uri`, `type`, `domain`, `username`);

**onParticipantLeft**(*args*)

Called when there is a participant left. The `args` parameter is an object with the following properties:
- `uuid` (string) - The UUID of the group.
- `groupId` (object) - The group object (`uri`, `type`, `domain`, `username`).
- `leaver` (object) - The leaver (`uri`, `type`, `domain`, `username`).
- `timestamp` (long) - The timestamp when the participant joined.

### Provisioning (namespace `provisioning`)
**requestCode**(*successCallback*, *errorCallback*, *numberPhone*, *twoLetterCountryCode*)

Request code for verification and registration process.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `numberPhone` (string) - The user's phone number.
- `twoLetterCountryCode` (string) - The two letter the country code. To retrieve the two letter country code, you can use location service `location.getCountryInfo()`.

**validate**(*successCallback*, *errorCallback*, *numberPhone*, *otpCode*, *twoLetterCountryCode*)

Validation of the signed up phone number send received code to the server.
- `successCallback` (function) - Called when the request was successful. Parameters: `response` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `numberPhone` (string) - The user's phone number.
- `twoLetterCountryCode` (string) - The two letter the country code. To retrieve the two letter country code, you can use location service `location.getCountryInfo()`.
- `otpCode` (string) - The OTP code received.

**deactivate**(*successCallback*, *errorCallback*)

Signing off the registered account(phone number) from a Kandy.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

### Access (namespace `access`)
**login**(*successCallback*, *errorCallback*, *username*, *password*)

Register/login the user on the server with credentials received from admin.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `username` (string) - The user's username.
- `password` (string) - The user's password

**logout**(*successCallback*, *errorCallback*)

This method unregisters user from the Kandy server.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**getConnectionState**(*successCallback*)

Get the current connect state
- `successCallback` (function) - Called when the request was successful. Parameters: `state` (string).

**getSession**(*successCallback*, *errorCallback*)

Get the current session.
- `successCallback` (function) - Called when the request was successful. Parameter is an object with the properties: `domain` (object), `user` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

### Call (namespace `call`)
**createVoipCall**(*successCallback*, *errorCallback*, *user*, *startWithVideo*)

Create a voip call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `user` (string) - The callee username.
- `startWithVideo` (boolean) - Create a call with video enabled.

**createPSTNCall**(*successCallback*, *errorCallback*, *phoneNumber*)

Create a PSTN call
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `phonNumber` (string) - The callee number.

**createSIPTrunkCall**(*successCallback*, *errorCallback*, *phoneNumber*)

Create a SIP Trunk call
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `phonNumber` (string) - The callee number.

**showLocalVideo**(*successCallback*, *errorCallback*, *id*, *left*, *top*, *width*, *height*)

Show Local Video in given Dimension.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The callee uri.
- `left` The co-ordinate of X position.
- `top` The co-ordinate of Y position.
- `width` The width of of Video that needs to show.
- `height` The height of of Video that needs to show.

**showRemoteVideo**(*successCallback*, *errorCallback*, *id*, *left*, *top*, *width*, *height*)

Show Remote Video in given Dimension.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The callee uri.
- `left` The co-ordinate of X position.
- `top` The co-ordinate of Y position.
- `width` The width of of Video that needs to show.
- `height` The height of of Video that needs to show.

**hideLocalVideo**(*successCallback*, *errorCallback*, *id*)

Hide Local Video.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The callee uri.

**hideRemoteVideo**(*successCallback*, *errorCallback*, *id*)

Hide Remote Video.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The callee uri.

**hangup**(*successCallback*, *errorCallback*, *id*)

Hangup current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The callee uri.

**mute**(*successCallback*, *errorCallback*, *id*)

Mute current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**unmute**(*successCallback*, *errorCallback*, *id*)

Unmute current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**hold**(*successCallback*, *errorCallback*, *id*)

Hold currnet call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**unhold**(*successCallback*, *errorCallback*, *id*)

Unhold current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**enableVideo**(*successCallback*, *errorCallback*, *id*)

Enable sharing video for current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**disableVideo**(*successCallback*, *errorCallback*, *id*)

Disable sharing video for current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**switchFrontCamera**(*successCallback*, *errorCallback*, *id*)

Switch to front-camera.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**switchBackCamera**(*successCallback*, *errorCallback*, *id*)

Switch to back-camera.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**switchSpeakerOn**()

Switch speaker on.

**switchSpeakerOff**()

Switch speaker Off.

**accept**(*successCallback*, *errorCallback*, *id*, *videoEnabled*)

Accept current coming call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.
- `videoEnabled` - Enable video call or not

**reject**(*successCallback*, *errorCallback*, *id*)

Reject current coming call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**ignore**(*successCallback*, *errorCallback*, *id*)

Ignore current coming call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (String) - The callee uri.

**isInCall**(*callback*)

Is in call.
- `callback` (function) - Called when the request was successful. The function has boolean parameter.

**isInGSMCall**(*callback*)

Is in GSM call.
- `callback` (function) - Called when the request was successful. The function has boolean parameter.

### Chat (namespace `chat`)
**sendChat**(*successCallback*, *errorCallback*, *recipient*, *message* [, *recodeType*])

Send the message to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `message` (string) - The message to send.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**sendSMS**(*successCallback*, *errorCallback*, *recipient*, *message*)

Send the SMS to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `message` (string) - The message to send.

**pickAudio**(*successCallback*, *errorCallback*)

Pick a audio file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendAudio**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri* [, *recodeType*])

Send a audio file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the audio file.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**pickVideo**(*successCallback*, *errorCallback*)

Pick a video file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendVideo**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri* [, *recodeType*])

Send a video file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the video file.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**pickImage**(*successCallback*, *errorCallback*)

Pick a image file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendImage**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri* [, *recodeType*])

Send a image file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the image file.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**pickContact**(*successCallback*, *errorCallback*)

Pick a contact.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendContact**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri* [, *recodeType*])

Send a contact file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the contact file.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**pickFile**(*successCallback*, *errorCallback*)

Pick a file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendFile**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri* [, *recodeType*])

Send a file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the file.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**sendCurrentLocation**(*successCallback*, *errorCallback*, *recipient*, *caption* [, *recodeType*])

Send the current location info to the recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the current location.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**sendLocation**(*successCallback*, *errorCallback*, *recipient*, *caption*, *location* [, *recodeType*])

Send the current location info to the recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the current location.
- `location` (object) - The location info to send.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**sendAttachment**(*successCallback*, *errorCallback*, *recipient*, *caption* [, *recodeType*])

Open a chooser dialog and send the attachment to the recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the current location.
- `recordType` (string) - The type of recipient. Default `CONTACT`

**openAttachment**(*successCallback*, *errorCallback*, *uri*, *mimeType*)

Open the attachment by URI and mimeType.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `uri` (string) - The uri of the file.
- `mimeType` (string) - The mimeType of the file.

**cancelMediaTransfer**(*successCallback*, *errorCallback*, *uuid*)

Cancel the current media transfer of the attachment.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `uuid` (string) - The UUID of the message.

**downloadMedia**(*successCallback*, *errorCallback*, *uuid*)

Download the media file of the message.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `uuid` (string) - The UUID of the message.

**downloadMediaThumbnail**(*successCallback*, *errorCallback*, *uuid*, *thumbnailSize*)

Download the media thumbnail file of the attachment.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `uuid` (string) - The UUID of the message.
- `thumbnailSize` (string) - The KandyThumbnailSize value.

**markAsReceived**(*successCallback*, *errorCallback*, *uuid*)

Send ack to sever for UUID of received/handled message.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `uuid` (string) - The uuid of the message to mark.

**pullEvents**(*successCallback*, *errorCallback*)

Pull pending events from Kandy service.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

### Group (namespace `group`)
**createGroup**(*successCallback*, *errorCallback*, *name*)

Create a new group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `name` (string) - The group name to create.
 
**getMyGroups**(*successCallback*, *errorCallback*)

Get the group list of the user.
- `successCallback` (function) - Called when the request was successful. Parameters: `groupList` (object array).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**getGroupById**(*successCallback*, *errorCallback*, *id*)

Get the group detail by group id.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**updateGroupName**(*successCallback*, *errorCallback*, *id*, *newName*)

Update the group name.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `newName` (string) - The new name of the group.

**updateGroupImage**(*successCallback*, *errorCallback*, *uri*)

Update the group image.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `uri` (string) - The uri of the image.

**removeGroupImage**(*successCallback*, *errorCallback*, *id*)

Remove the group image.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**downloadGroupImage**(*successCallback*, *errorCallback*, *id*)

Download the group image.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**downloadGroupImageThumbnail**(*successCallback*, *errorCallback*, *id*, *thumbnailSize*)

Download the group image thumbnail.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `thumbnailSize` (string) - The KandyThumbnailSize value.

**muteGroup**(*successCallback*, *errorCallback*, *id*)

Mute the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**unmuteGroup**(*successCallback*, *errorCallback*, *id*)

Unmute the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**destroyGroup**(*successCallback*, *errorCallback*, *id*)

Destroy the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**leaveGroup**(*successCallback*, *errorCallback*, *id*)

Leave the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.

**removeParticipants**(*successCallback*, *errorCallback*, *id*, *participants*)

Remove participants of the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `participants` (array) - The uri list of the participants.

**muteParticipants**(*successCallback*, *errorCallback*, *id*, *participants*)

Mute participants of the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `participants` (array) - The uri list of the participants.

**unmuteParticipants**(*successCallback*, *errorCallback*, *id*, *participants*)

Unmute participants of the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `participants` (array) - The uri list of the participants.

**addParticipants**(*successCallback*, *errorCallback*, *id*, *participants*)

Add participants to the group.
- `successCallback` (function) - Called when the request was successful. Parameters: `group` (object).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `id` (string) - The id of the group.
- `participants` (array) - The uri list of the participants.

### Presence (namespace `presence`)
**startWatch**(*successCallback*, *errorCallback*, *userList*)

Register listener for presence's callbacks/notifications
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `userList` (string array) - The list users needed to watched.

### Location (namespace `location`)
**getCountryInfo**(*successCallback*, *errorCallback*)

Get the country info.
- `successCallback` (function) - Called when the request was successful. The parameters is an object with the properties: `code`, `nameLong` and `nameShort`.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**getCurrentLocation**(*successCallback*, *errorCallback*)

Get the current location info.
- `successCallback` (function) - Called when the request was successful. Parameters: `location`.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

### Push (namespace `push`)

**Precondition for iOS**

Please add below code into application: didFinishLaunchingWithOptions:

```ios
ifdef __IPHONE_8_0
UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:(UIRemoteNotificationTypeBadge
                                                                                     |UIRemoteNotificationTypeSound
                                                                                     |UIRemoteNotificationTypeAlert) categories:nil];
[[UIApplication sharedApplication] registerUserNotificationSettings:settings];

else

UIRemoteNotificationType myTypes = UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeSound;
[[UIApplication sharedApplication] registerForRemoteNotificationTypes:myTypes];
endif
[application registerForRemoteNotifications];

```

**enable**(*successCallback*, *errorCallback*)

Enable the push service.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- 
**disable**(*successCallback*, *errorCallback*)

Disable the push service.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

### Address book (namespace `addressBook`)
**getDeviceContacts**(*successCallback*, *errorCallback*, *filters*)

Get the local contacts of the device.
- `successCallback` (function) - Called when the request was successful. Parameters: `contacts` (object)
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `filter` (string) - The DeviceContactsFilter value.

**getDomainContacts**(*successCallback*, *errorCallback*)

Get the contacts of the domain.
- `successCallback` (function) - Called when the request was successful. Parameters: `contacts` (object)
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**getFilteredDomainDirectoryContacts**(*successCallback*, *errorCallback*, *filter*)

Get the contacts of the domain by filters.
- `successCallback` (function) - Called when the request was successful. Parameters: `contacts` (object)
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `filter` (string) - The DomainContactFilter value.

## Troubleshooting

## License
