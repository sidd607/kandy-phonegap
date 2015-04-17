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
- IOS (coming soon)

## Installation
Check out PhoneGap CLI [docs](http://docs.phonegap.com/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface)
before starting out.

    cordova plugin add "path/to/plugin/directory" --variable API_KEY=<api-key> --variable API_SECRET=<api-secret>
or

    cordova plugin add https://github.com/kodeplusdev/kandyphonegap.git --variable API_KEY=<api-key> --variable API_SECRET=<api-secret>
## Getting Started
This plugin defines a global `Kandy` object, which provide an easy way to interact with KANDY.
Although the object is in the global scope, it is not available until after the `deviceready` event. After the `deviceready` event, you must initialize to  config and register listeners to use Kandy plugin.

```js
    function onDeviceReady(){
        ...
        Kandy.initialize();
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
**Add plugin**

Go to your app directory and run a command such as the following:
```shell
    phonegap plugin add directory/to/this/plugin --variable API_KEY=<your_api_key> --variable API_SECRET=<your_api_secret>
```
**Build the App**

Run a command such as the following:
```shell
    phonegap build android
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
Default call widget type is `voip`. If you want to use `PSTN`, you can add `type="PSTN"`.

Example:
```html
    <kandy widget="call" type="PSTN"></kandy>
```
### Chat widget
The `widget` attribute is `chat`. Some callback actions you can use: `send`, `pull`.
Default chat wisget type is `chat`. If you want to use `SMS`, you can add `type="SMS"`.

Example
```html
    <kandy widget="chat" send-success="onSendSuccess"></kandy>
```
## API Reference
### Configurations
**initialize**()

Initialize the `KandyPlugin` with default configuration values.

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

**hangup**(*successCallback*, *errorCallback*)

Hangup current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**mute**(*successCallback*, *errorCallback*)

Mute current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**unmute**(*successCallback*, *errorCallback*)

Unmute current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**hold**(*successCallback*, *errorCallback*)

Hold currnet call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**unhold**(*successCallback*, *errorCallback*)

Unhold current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**enableVideo**(*successCallback*, *errorCallback*)

Enable sharing video for current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**disableVideo**(*successCallback*, *errorCallback*)

Disable sharing video for current call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**accept**(*successCallback*, *errorCallback*)

Accept current coming call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**reject**(*successCallback*, *errorCallback*)

Reject current coming call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**ignore**(*successCallback*, *errorCallback*)

Ignore current coming call.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

### Chat (namespace `chat`)
**sendChat**(*successCallback*, *errorCallback*, *recipient*, *message*)

Send the message to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `message` (string) - The message to send.

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

**sendAudio**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri*)

Send a audio file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the audio file.

**pickVideo**(*successCallback*, *errorCallback*)

Pick a video file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendVideo**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri*)

Send a video file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the video file.

**pickImage**(*successCallback*, *errorCallback*)

Pick a image file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendImage**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri*)

Send a image file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the image file.

**pickContact**(*successCallback*, *errorCallback*)

Pick a contact.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendContact**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri*)

Send a contact file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the contact file.

**pickFile**(*successCallback*, *errorCallback*)

Pick a file.
- `successCallback` (function) - Called when the request was successful. Parameters: `uri` (string).
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

**sendFile**(*successCallback*, *errorCallback*, *recipient*, *caption*, *uri*)

Send a file to recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the file.
- `uri` (string) - The uri of the file.

**sendCurrentLocation**(*successCallback*, *errorCallback*, *recipient*, *caption*)

Send the current location info to the recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the current location.

**sendLocation**(*successCallback*, *errorCallback*, *recipient*, *caption*, *location*)

Send the current location info to the recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the current location.
- `location` (object) - The location info to send.

**sendAttachment**(*successCallback*, *errorCallback*, *recipient*, *caption*)

Open a chooser dialog and send the attachment to the recipient.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).
- `recipient` (string) - The recipient to receive the message.
- `caption` (string) - The caption of the current location.

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
**enable**(*successCallback*, *errorCallback*)

Enable the push service.
- `successCallback` (function) - Called when the request was successful. The function has no parameter.
- `errorCallback` (function) - Called when the request was failed. Parameters: `error` (string).

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
