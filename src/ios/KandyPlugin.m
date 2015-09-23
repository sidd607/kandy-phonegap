//
//  KandyPlugin.m
//  KandyPlugin
//
//  Created by Srinivasan Baskaran on 2/6/15.
//
//

#import "KandyPlugin.h"
#import "CallViewController.h"
#import "KandyUtil.h"
#import <MediaPlayer/MediaPlayer.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import "OpenChatAttachment.h"

@interface KandyPlugin() <KandyCallServiceNotificationDelegate, KandyChatServiceNotificationDelegate, KandyGroupServiceNotificationDelegate,KandyContactsServiceNotificationDelegate, KandyAccessNotificationDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIActionSheetDelegate, MPMediaPickerControllerDelegate, ActiveCallOptionDelegate>

/**
 * Kandy response listeners *
 */
@property (nonatomic) NSString *callbackID;
@property (nonatomic) UIView *viewRemoteVideo;
@property (nonatomic) UIView *viewLocalVideo;

/**
 * Kandy Plugin configuration properties *
 */

@property (nonatomic) NSString *startWithVideo;
@property (nonatomic) NSString *downloadPath;
@property (assign) int mediaSizePicker;
@property (nonatomic) NSString *downloadPolicy;
@property (assign) int downloadThumbnailSize;
@property (nonatomic) NSString *kandyHostUrl;

/**
 * Kandy incoming and out going delegate to set
 */
@property (nonatomic) UIImagePickerController *pickerView;

//UIStoryboard or UIfile
@property (nonatomic) UIStoryboard * kandyStoryboard;

/**
 * The Incoming options action sheet for Kandy *
 */
@property (nonatomic) NSString *incomingcall;
@property (nonatomic) UIActionSheet * incomingCallOPtions;


/**
 * The {@link CallbackContext} for Kandy listeners *
*/
@property (nonatomic) NSString * kandyConnectServiceNotificationCallback;
@property (nonatomic) NSString * kandyCallServiceNotificationCallback;
@property (nonatomic) NSString * kandyCallServiceNotificationPluginCallback;
@property (nonatomic) NSString * kandyAddressBookServiceNotificationCallback;
@property (nonatomic) NSString * kandyChatServiceNotificationCallback;
@property (nonatomic) NSString * kandyGroupServiceNotificationCallback;

@property (nonatomic) NSString * kandyChatServiceNotificationPluginCallback;

// Kandy listeners for call, chat, presence
@property (nonatomic) NSString * incomingCallListener;
@property (nonatomic) NSString * videoStateChangedListener;
@property (nonatomic) NSString * audioStateChangedListener;
@property (nonatomic) NSString * callStateChangedListener;
@property (nonatomic) NSString * GSMCallIncomingListener;
@property (nonatomic) NSString * GSMCallConnectedListener;
@property (nonatomic) NSString * GSMCallDisconnectedListener;

@property (nonatomic) NSString * chatReceivedListener;
@property (nonatomic) NSString * chatDeliveredListener;
@property (nonatomic) NSString * chatMediaDownloadProgressListener;
@property (nonatomic) NSString * chatMediaDownloadFailedListener;
@property (nonatomic) NSString * chatMediaDownloadSuccededListener;

@property (nonatomic) NSString * deviceAddressBookChangedListener;

// Whether or not the call start with sharing video enabled
@property (assign) BOOL startVideoCall;

// The call dialog (native)
@property (assign) BOOL hasNativeCallView;
@property (assign) BOOL hasNativeAcknowledgement;
@property (assign) BOOL showNativeCallPage;
@property (assign) BOOL renewSession;

//Configuration
@property (nonatomic) NSString *downloadMediaPath;
@property (assign) double mediaMaxSize;
@property (nonatomic) NSString *autoDownloadMediaConnectionType;
@property (nonatomic) NSString *autoDownloadThumbnailSize;

@property (nonatomic) NSArray *connectionState;
@property (nonatomic) NSArray *callState;
@property (nonatomic) NSMutableDictionary *activeCalls;

// Login username
@property (nonatomic) NSString *username;
@property (nonatomic) NSData* deviceToken;
//Kandy Group
@property (nonatomic) KandyGroup *kandyGroup;

//Location callback properties
@property (assign) successResponse success;
@property (assign) failureResponse failure;

@property (nonatomic) CallViewController *activeCallViewController;
@end


@implementation KandyPlugin

#pragma mark - pluginInitialize

- (void)pluginInitialize {
    [self InitializeObjects];
}

- (void) InitializeObjects {
    
    // Set deafult for configuration variables
    self.startVideoCall = YES;
    self.hasNativeAcknowledgement = YES;
    self.showNativeCallPage = YES;
    self.renewSession = NO;
    
    self.connectionState = @[@"DISCONNECTING", @"DISCONNECTED", @"CONNECTING", @"CONNECTED"];
    self.callState = @[@"INITIAL", @"RINGING", @"DIALING", @"TALKING", @"TERMINATED", @"ON_DOUBLE_HOLD", @"REMOTELY_HELD", @"ON_HOLD"];
    
    // Initialize Kandy SDK
    if ([KandyUtil getDomainAPIKey] && [KandyUtil getDomainSecrect]) {
        [Kandy initializeSDKWithDomainKey:[KandyUtil getDomainAPIKey] domainSecret:[KandyUtil getDomainSecrect]];
    }
    [[Kandy sharedInstance].globalSettings setKandyServiceHost:[KandyUtil getHostURL]];
}

#pragma mark - Initialize

- (NSMutableDictionary *) activeCalls {
    if (_activeCalls == nil) {
        _activeCalls = [[NSMutableDictionary alloc] init];
    }
    return _activeCalls;
}

- (UIStoryboard *) kandyStoryboard {
    return [UIStoryboard storyboardWithName:@"KandyPlugin" bundle:nil];
}
/**
 * Register listeners to receive events from Kandy background service.
 */
- (void) registerNotifications {
    //Connect service
    [[Kandy sharedInstance].access registerNotifications:self];
    [[Kandy sharedInstance].services.call registerNotifications:self];
    [[Kandy sharedInstance].services.chat registerNotifications:self];
    [[Kandy sharedInstance].services.group registerNotifications:self];
    [[Kandy sharedInstance].services.contacts registerNotifications:self];
}

/**
 * Unregister listeners out of Kandy background service.
 */
- (void) unRegisterNotifications {
    [[Kandy sharedInstance].access unregisterNotifications:self];
    [[Kandy sharedInstance].services.call unregisterNotifications:self];
    [[Kandy sharedInstance].services.chat unregisterNotifications:self];
    [[Kandy sharedInstance].services.group registerNotifications:self];
    [[Kandy sharedInstance].services.contacts unregisterNotifications:self];
}

#pragma mark - Method routing 
- (void) invokeKandyServiceByIndex:(KandyPluginServices) index withPluginCommand:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        self.callbackID = command.callbackId;
        NSDictionary *serviceconfig = [[KandyUtil sharedInstance]kandyServices][@(index)];
        SEL kandyselector = NSSelectorFromString(serviceconfig[METHOD]);
        int paramcount = [serviceconfig[PARAMS] intValue];
        NSArray *exparams = [serviceconfig[EXTRAPARAM] array];
        NSArray *params = command.arguments;
        if (![KandyUtil validateInputParam:params withRequiredInputs:paramcount]) {
            [self handleRequiredInputError];
            return;
        }

        NSMethodSignature *sig = nil;
        sig = [[self class] instanceMethodSignatureForSelector:kandyselector];
        NSInvocation *myInvocation = [NSInvocation invocationWithMethodSignature:sig];
        [myInvocation setSelector:kandyselector];
        [myInvocation setTarget:self];
        
        int i = 2;
        for (NSString * __unsafe_unretained param in params) {
            [myInvocation setArgument:&param atIndex:i];
            i++;
        }
        for (NSString * __unsafe_unretained param in exparams) {
            [myInvocation setArgument:&param atIndex:i];
            i++;
        }
        [myInvocation invoke];
    }];
}

#pragma mark - Public Plugin Methods

-(void) configurations:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSArray *config = command.arguments;
        NSLog(@"configurations Variables %@", config);
        if (config && [config count] > 0) {
            NSDictionary *configvariables = config[0];
            self.hasNativeCallView = [configvariables[@"hasNativeCallView"] boolValue];
            self.hasNativeAcknowledgement = [configvariables[@"acknowledgeOnMsgRecieved"] boolValue];
            self.showNativeCallPage = [configvariables[@"showNativeCallPage"] boolValue];
            self.renewSession = [configvariables[@"renewExpiredSession"] boolValue];
            //Kandy Settings
            self.downloadMediaPath = configvariables[@"downloadMediaPath"];
            self.mediaMaxSize = [configvariables[@"mediaMaxSize"] doubleValue];
            self.autoDownloadMediaConnectionType = configvariables[@"autoDownloadMediaConnectionType"];
            self.autoDownloadThumbnailSize = configvariables[@"autoDownloadThumbnailSize"];
            [self applyKandySettings];
        }
    }];
}
- (void) makeToast:(CDVInvokedUrlCommand *)command {
    NSString *message = (command.arguments[0] ? command.arguments[0] : @"");
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:message
                                                   delegate:self
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}
- (void) setKey:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:APIKEY withPluginCommand:command];
}
- (void) setHostUrl:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SETHOST withPluginCommand:command];
}
- (void) getHostUrl:(CDVInvokedUrlCommand *)command {
    [self notifySuccessResponse:[[Kandy sharedInstance].globalSettings kandyServiceHost] withCallbackID:command.callbackId];
}
- (void) getReport:(CDVInvokedUrlCommand *)command {
    [self notifySuccessResponse:[[Kandy sharedInstance].globalSettings getReport] withCallbackID:command.callbackId];
}
- (void) connectServiceNotificationCallback:(CDVInvokedUrlCommand *)command {
    self.kandyConnectServiceNotificationCallback = command.callbackId;
}
- (void) callServiceNotificationCallback:(CDVInvokedUrlCommand *)command {
    self.kandyCallServiceNotificationCallback = command.callbackId;
}
- (void) callServiceNotificationPluginCallback:(CDVInvokedUrlCommand *)command {
    self.kandyCallServiceNotificationPluginCallback = command.callbackId;
}
- (void) addressBookServiceNotificationCallback:(CDVInvokedUrlCommand *)command {
    self.kandyAddressBookServiceNotificationCallback = command.callbackId;
}
- (void) chatServiceNotificationCallback:(CDVInvokedUrlCommand *)command {
    self.kandyChatServiceNotificationCallback = command.callbackId;
}
- (void) groupServiceNotificationCallback:(CDVInvokedUrlCommand *)command {
    self.kandyGroupServiceNotificationCallback = command.callbackId;
}
- (void) chatServiceNotificationPluginCallback:(CDVInvokedUrlCommand *)command {
    self.kandyChatServiceNotificationPluginCallback = command.callbackId;
}

// Provisioning
- (void) request:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:REQUEST withPluginCommand:command];
}
- (void) validate:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:VALIDATE withPluginCommand:command];
}
- (void) deactivate:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        self.callbackID = command.callbackId;
        [[Kandy sharedInstance].provisioning deactivateWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }];
}

//TODO:
- (void) getUserDetails:(CDVInvokedUrlCommand *)command {
    NSArray *params = command.arguments;
    [self.commandDelegate runInBackground:^{
        __block NSString *userId = params[0];
        [[[Kandy sharedInstance] provisioning] getUserDetails:userId responseCallback:^(NSError *error, KandyUserInfo *userInfo) {
            if (error) {
                [self didHandleResponse:error];
            } else {
                //TODO:
//                NSDictionary *result = @ {
//                };
            }
        }];
    }];
}

// Access Service
- (void) login:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:LOGIN withPluginCommand:command];
}

- (void) loginByToken:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:TOKENLOGIN withPluginCommand:command];
}
/**
 * This method unregisters user from the Kandy server.
 */
- (void) logout:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        [[Kandy sharedInstance].access logoutWithResponseCallback:^(NSError *error) {
            if (error) {
                [self notifyFailureResponse:kandy_error_message withCallbackID:command.callbackId];
            } else {
                [self notifySuccessResponse:kandy_login_logout_success withCallbackID:command.callbackId];
                [self unRegisterNotifications];
            }
        }];
    }];
}
- (void) getConnectionState:(CDVInvokedUrlCommand *)command {
    [self notifySuccessResponse:[self.connectionState objectAtIndex:[Kandy sharedInstance].access.connectionState] withCallbackID:command.callbackId];
}
/**
 * Session Service
 * Load previous session.
 *
 */
- (void) getSession:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSDictionary *domain = @{
                                  @"apiKey": [Kandy sharedInstance].sessionManagement.session.kandyDomain.key,
                                  @"apiSecret": [Kandy sharedInstance].sessionManagement.session.kandyDomain.secret,
                                  @"name": [Kandy sharedInstance].sessionManagement.session.kandyDomain.name,
                                };

        NSDictionary *user = nil;
        if ([Kandy sharedInstance].sessionManagement.session.currentUser) {
            user = @{
                     @"id": [Kandy sharedInstance].sessionManagement.session.currentUser.userId,
                     @"name": [Kandy sharedInstance].sessionManagement.session.currentUser.record.userName,
                     //@"deviceId": [Kandy sharedInstance].sessionManagement.session.currentUser.record.userName,
                     @"password": [Kandy sharedInstance].sessionManagement.session.currentUser.password,
                     };
        }
        
        NSDictionary *result = @{
                                 @"domain": domain,
                                 @"user": (user ? user : @"")
                               };
        [self notifySuccessResponse:result withCallbackID:command.callbackId];
    }];
}

//** Call Service **/

/**
 * Create a voip call.
 *
 * @param username The username of the callee.
 */
- (void) createVoipCall:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:VOIP withPluginCommand:command];
}
- (void) showLocalVideo:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SHOWLVIDEO withPluginCommand:command];
}
/**
 * Remote local call video view.
 */
- (void) hideLocalVideo:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        [self.viewLocalVideo setHidden:YES];
        [self.viewLocalVideo removeFromSuperview];
    }];
}
- (void) showRemoteVideo:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SHOWRVIDEO withPluginCommand:command];
}
/**
 * Remote Remote call video view.
 */
- (void) hideRemoteVideo:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        [self.viewRemoteVideo setHidden:YES];
        [self.viewRemoteVideo removeFromSuperview];
    }];
}
/**
 * Create a PSTN call.
 *
 * @param number The number phone of the callee.
 */
- (void) createPSTNCall:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:PSTN withPluginCommand:command];
}
- (void) hangup:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:HANGUP withPluginCommand:command];
}
- (void) mute:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:MUTE withPluginCommand:command];
}
- (void) UnMute:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UNMUTE withPluginCommand:command];
}
- (void) hold:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:HOLD withPluginCommand:command];
}
- (void) unHold:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UNHOLD withPluginCommand:command];
}
- (void) enableVideo:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:EVIDEO withPluginCommand:command];
}
- (void) disableVideo:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DVIDEO withPluginCommand:command];
}
- (void) switchFrontCamera:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SWITCHCAMERA withPluginCommand:command];
}
- (void) switchBackCamera:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SWITCHCAMERA withPluginCommand:command];
}
- (void) switchSpeakerOn:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SPEAKERONOFF withPluginCommand:command];
}
- (void) switchSpeakerOff:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SPEAKERONOFF withPluginCommand:command];
}
- (void) accept:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:ACCEPT withPluginCommand:command];
}
- (void) reject:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:REJECT withPluginCommand:command];
}
- (void) ignore:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:IGNORE withPluginCommand:command];
}
- (void) isInCall:(CDVInvokedUrlCommand *)command {
    int result = [[Kandy sharedInstance].services.call isInCall];
    [self notifySuccessResponse:@(result) withCallbackID:command.callbackId];
}
- (void) isInGSMCall:(CDVInvokedUrlCommand *)command {
    int result = [[Kandy sharedInstance].services.call isInGSMCall];
    [self notifySuccessResponse:@(result) withCallbackID:command.callbackId];
}
// Additional method to set Name and Image for active call
- (void) setActiveCallUserProfile:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:USERPROFILE withPluginCommand:command];
}

// Chat Service
- (void) sendChat:(CDVInvokedUrlCommand *)command{
    [self invokeKandyServiceByIndex:CHAT withPluginCommand:command];
}
- (void) sendSMS:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SMS withPluginCommand:command];
}
- (void) openAttachment:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:OPENATTACHMENT withPluginCommand:command];
}
- (void) sendAttachment:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SENDATTACHMENT withPluginCommand:command];
}
- (void) pickAudio:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self pickAudio];
}
- (void) sendAudio:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SAUDIO withPluginCommand:command];
}
- (void) pickVideo:(CDVInvokedUrlCommand *)command; {
    self.callbackID = command.callbackId;
    [self pickVideo];
}
- (void) sendVideo:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SVIDEO withPluginCommand:command];
}
- (void) pickImage:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self pickImage];
}
- (void) sendImage:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SIMAGE withPluginCommand:command];
}
- (void) pickContact:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self pickContact];
}
- (void) sendContact:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SCONTACT withPluginCommand:command];
}
- (void) sendCurrentLocation:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SCURRENTLOC withPluginCommand:command];
}
- (void) sendLocation:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:SLOC withPluginCommand:command];
}
- (void) markAsReceived:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:ACKNOWLEDGE withPluginCommand:command];
}
- (void) pullEvents:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [[Kandy sharedInstance].services.chat pullEventsWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }];
}
- (void) downloadMedia:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DOWNLOADMEDIA withPluginCommand:command];
}
- (void) downloadMediaThumbnail:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DOWNLOADMEDIATHUMB withPluginCommand:command];
}
- (void) cancelMediaTransfer:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:CANCELMEDIA withPluginCommand:command];
}

//Group Service
- (void) createGroup:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:CREATEGROUP withPluginCommand:command];
}
- (void) getMyGroups:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self getMyGroups];
}
- (void) getGroupById:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:GROUPBYID withPluginCommand:command];
}
- (void) updateGroupName:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UPGROUPNAME withPluginCommand:command];
}
- (void) updateGroupImage:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UPGROUPIMG withPluginCommand:command];
}
- (void) removeGroupImage:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:RMGROUPIMG withPluginCommand:command];
}
- (void) downloadGroupImage:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DOWNGROUPIMG withPluginCommand:command];
}
- (void) downloadGroupImageThumbnail:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DOWNGROUPTHUMB withPluginCommand:command];
}
- (void) muteGroup:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:MUTEGROUP withPluginCommand:command];
}
- (void) unmuteGroup:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UNMUTEGROUP withPluginCommand:command];
}
- (void) destroyGroup:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DELGROUP withPluginCommand:command];
}
- (void) leaveGroup:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:LEAVEGROUP withPluginCommand:command];
}
- (void) removeParticipants:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:RMPARTICIPANTS withPluginCommand:command];
}
- (void) muteParticipants:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:MUTEPARTICIPANTS withPluginCommand:command];
}
- (void) unmuteParticipants:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UNMUTEPARTICIPANTS withPluginCommand:command];
}
- (void) addParticipants:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:ADDPARTICIPANTS withPluginCommand:command];
}

// Presence service
- (void) presence:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:PRESENCE withPluginCommand:command];
}

//Location service
- (void) getCountryInfo:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self getLocationinfo];
}

- (void) getCurrentLocation:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self getCurrentLocation];
}

// Push service
- (void) enable:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [self enableKandyPushNotification];
    }];

}
- (void) disable:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [self disableKandyPushNotification];
    }];
}

//AddressBook
- (void) getDeviceContacts:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [self getUsersFromDeviceContacts];
    }];
}
- (void) getDomainContacts:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [self getDomainContacts];
    }];
}
- (void) getFilteredDomainDirectoryContacts:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:FILTER withPluginCommand:command];
}

- (void) getPersonalAddressBook:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self.commandDelegate runInBackground:^{
        [self getPersonalAddressBook];
    }];
}
//TODO:
- (void) addContactToPersonalAddressBook:(CDVInvokedUrlCommand *)command {
    //Waiting for Native SDK support
    NSLog(@"Support will be added soon...");
}
//TODO:
- (void) removePersonalAddressBookContact:(CDVInvokedUrlCommand *)command {
    //Waiting for Native SDK support
    NSLog(@"Support will be added soon...");
}

//Device Profile Service
- (void) updateDeviceProfile:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UPDATEDEVICEPROFILE withPluginCommand:command];
}
- (void) getUserDeviceProfiles:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self getUserDeviceProfiles];
}

//Billing service
- (void) getUserCredit:(CDVInvokedUrlCommand *)command {
    self.callbackID = command.callbackId;
    [self getUserCredit];
}

//Cloud Storage
- (void) uploadMedia:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:UPLOADMEDIA withPluginCommand:command];
}
- (void) downloadMediaFromCloudStorage:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:DOWNLOADCLOUD withPluginCommand:command];
}
- (void) downloadMediaThumbnailFromCloudStorage:(CDVInvokedUrlCommand *)command {
     //TODO:
    [self notifySuccessResponse:@"No sdk Support" withCallbackID:command.callbackId];
}
- (void) cancelMediaTransferFromCloudStorage:(CDVInvokedUrlCommand *)command {
    [self invokeKandyServiceByIndex:CANCELCLOUDMEDIA withPluginCommand:command];
}

#pragma mark - Private Plugin Methods

/*
 *  Configurations
 */

- (void) setKandyDomainAPIKey:(NSString *)key andSecret:(NSString *)secret {
    [KandyUtil saveAPIKey:key secret:secret];
    [Kandy initializeSDKWithDomainKey:key domainSecret:secret];
}

- (void) setkandyHostURL:(NSString *)url {
    [KandyUtil saveHostURL:url];
    [[Kandy sharedInstance].globalSettings setKandyServiceHost:url];
}

/*
 *  Provisioning
 */
- (void) requestCodeWithPhone:(NSString *)phoneno andISOCountryCode:(NSString *)isocode {
    KandyAreaCode * kandyAreaCode = [[KandyAreaCode alloc] initWithISOCode:isocode andCountryName:@"" andPhonePrefix:@""];
    [[Kandy sharedInstance].provisioning requestCode:kandyAreaCode phoneNumber:phoneno codeRetrivalMethod:EKandyValidationMethod_sms responseCallback:^(NSError *error, NSString *destinationToValidate) {
        [self didHandleResponse:error];
    }];
}

- (void) validate:(NSString *)phoneno otp:(NSString *)otp ISOCountryCode:(NSString *)isocode  {
    KandyAreaCode * kandyAreaCode = [[KandyAreaCode alloc] initWithISOCode:isocode andCountryName:@"" andPhonePrefix:@""];
    [[Kandy sharedInstance].provisioning validateAndProvision:otp destination:phoneno areaCode:kandyAreaCode responseCallback:^(NSError *error, KandyUserInfo *userInfo) {
        if (error) {
            [self didHandleResponse:error];
        } else {
            NSDictionary *jsonObj = @{
                                      @"id": userInfo.userId,
                                      @"domain": userInfo.record.domain,
                                      @"username": userInfo.record.userName,
                                      @"password": userInfo.password,
                                      };
            
            [self notifySuccessResponse:jsonObj];
        }
        
    }];
 }

/*
 *  Access
 */

/**
 * Register/login the user on the server with credentials received from admin.
 *
 * @param username The username to use.
 * @param password The password to use.
 */
-(void)connectWithUserName:(NSString *)usrname andPassword:(NSString *)pwd {
    
    if (usrname && [usrname isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_login_empty_username_text];
        return;
    }
    if (pwd && [pwd isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_login_empty_password_text];
        return;
    }
    
    KandyUserInfo * kandyUserInfo = [[KandyUserInfo alloc] initWithUserId:usrname password:pwd];
    [[Kandy sharedInstance].access login:kandyUserInfo responseCallback:^(NSError *error) {
        if (error) {
            [self notifyFailureResponse:kandy_error_message];
        } else {
            self.username = usrname;
            [self registerNotifications];
            [self notifySuccessResponse:kandy_login_login_success];
        }
    }];
}

- (void) loginWithToken:(NSString *)token {
    if (!token) {
        [self notifyFailureResponse:@"Invalid access token."];
        return;
    }
    [[Kandy sharedInstance].access loginWithAccessToken:token responseCallback:^(NSError *error) {
        if (error) {
            [self notifyFailureResponse:kandy_error_message];
        } else {
            [self registerNotifications];
            [self notifySuccessResponse:kandy_login_login_success];
        }
    }];
}

/*
 *  Location
 */

- (void) getLocationinfo {
    [self.commandDelegate runInBackground:^{
        [[Kandy sharedInstance].services.location getCountryInfoWithResponseCallback:^(NSError *error, KandyAreaCode *areaCode) {
            if (error) {
                [self notifyFailureResponse:kandy_error_message];
            } else {
                NSDictionary *jsonObj = @{
                                         @"long": areaCode.countryName,
                                         @"code": areaCode.isoCode,
                                         @"short": areaCode.phonePrefix,
                                         };
                
                [self notifySuccessResponse:jsonObj];
            }
        }];
    }];
}

- (void) getCurrentLocation {
    [self.commandDelegate runInBackground:^{
        [[KandyUtil sharedInstance] getCurrentLocationUsingBlcok:^(CLLocation *location) {
            NSDictionary *result = @ {
                                        @"time":location.timestamp,
                                        @"latitude": @(location.coordinate.latitude),
                                        @"longitude" : @(location.coordinate.longitude),
                                        @"altitude": @(location.altitude),
                                        @"speed": @(location.speed),
                                    };
            [self notifySuccessResponse:result];
            
            
        } andFailure:^(NSError *error) {
            [self notifyFailureResponse:error.description];
        }];
    }];
   
}

/*
 *  Call
 */

/**
 * Show local call video view.
 *
 * @param frame     Set localvideo position using Frame.
 */
- (void) setLocalVideoView:(NSString *)callid left:(NSString *)x top:(NSString *)y width:(NSString *)width height:(NSString *)height   {
    CGRect frame = CGRectMake([x floatValue], [y floatValue], [width floatValue], [height floatValue]);
    // Local Video
    if (![self checkActiveCall:callid]) return;
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    [self.viewLocalVideo setHidden:NO];
    [self.viewLocalVideo setFrame:frame];
    activecall.localVideoView = self.viewLocalVideo;
}

/**
 * Show local call video view.
 *
 * @param frame     Set remote video position using Frame.
 */

- (void) setRemoteVideoView:(NSString *)callid left:(NSString *)x top:(NSString *)y width:(NSString *)width height:(NSString *)height {
    CGRect frame = CGRectMake([x floatValue], [y floatValue], [width floatValue], [height floatValue]);
    // Remote Video
    if (![self checkActiveCall:callid]) return;
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    [self.viewRemoteVideo setHidden:NO];
    [self.viewRemoteVideo setFrame:frame];
    activecall.remoteVideoView = self.viewRemoteVideo;
}

-(void)establishVoipCallTo:(NSString *)caller andWithStartVideo:(NSString *)videoenabled {
    EKandyOutgingVoIPCallOptions outgoingCallOption = [videoenabled intValue] == 1;
    if (caller && [caller isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_calls_invalid_phone_text_msg];
        return;
    }
    KandyRecord * kandyRecord = [[KandyRecord alloc] initWithURI:caller type:EKandyRecordType_contact];
    KandyRecord * initiator = nil;
    
    if (self.username.length > 0) {
        initiator = [[KandyRecord alloc] initWithURI:self.username type:EKandyRecordType_contact];
    }
    id <KandyOutgoingCallProtocol> kandyOutgoingCall = [[Kandy sharedInstance].services.call createVoipCall:initiator callee:kandyRecord options:outgoingCallOption];
    [self WillHandleOutgoingCall:kandyOutgoingCall];
}

/**
 * Create a PSTN call.
 *
 * @param number The number phone of the callee.
 */

-(void)establishPSTNCall:(NSString *)caller {
    if (caller && [caller isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_calls_invalid_phone_text_msg];
        return;
    }
    NSString *destination = [caller stringByReplacingOccurrencesOfString:@"+" withString:@""];
    destination = [destination stringByReplacingOccurrencesOfString:@"-" withString:@""];
    id <KandyOutgoingCallProtocol> kandyOutgoingCall = [[Kandy sharedInstance].services.call createPSTNCall:destination destination:self.username options:EKandyOutgingPSTNCallOptions_blockedCallerID];
    [self WillHandleOutgoingCall:kandyOutgoingCall];
}

/**
 * Check call exists.
 *
 * @param id The callee uri.
 * @return
 */
- (BOOL) checkActiveCall:(NSString *)callid {
    if (!self.activeCalls[callid]) {
        return false;
    }
    return true;
}

/**
 * Hangup current call.
 */
-(void)hangupCall:(NSString *)callid {
    
    if (![self checkActiveCall:callid]) return;
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    [activecall hangupWithResponseCallback:^(NSError *error) {
        [self didHandleResponse:error];
        if (!error) {
            [self.activeCalls removeObjectForKey:callid];
        }
        
    }];
}

/**
 * Mute/Unmute current call.
 *
 * @param mute The state of current audio call.
 */

- (void)muteCall:(NSString *)callid mute:(NSString *)mute {
    if (![self checkActiveCall:callid]) return;

    BOOL flag = [mute boolValue];
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    if (flag) {
        [activecall muteWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }
    else {
        [activecall unmuteWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }
}

/**
 * Hold/unhold current call.
 *
 * @param hold The state of current call.
 */
- (void)holdCall:(NSString *)callid hold:(NSString *)hold {
    if (![self checkActiveCall:callid]) return;

    BOOL flag = [hold boolValue];
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    if (flag) {
        [activecall holdWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }
    else {
        [activecall unHoldWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }
}

/**
 * Whether or not The sharing video is enabled.
 *
 * @param video The state of current video call.
 */

- (void)enableVideoCall:(NSString *)callid video:(NSString *)videoOn{
    if (![self checkActiveCall:callid]) return;
    
    BOOL flag = [videoOn boolValue];
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    if (flag) {
        [activecall startVideoSharingWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }
    else {
        [activecall stopVideoSharingWithResponseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }
}

/**
 * Switch between front and back camera.
 *
 * @param id         The callee uri.
 * @param cameraInfo The {@Link KandyCameraInfo}
 */

- (void) switchCamera:(NSString *)callid {
    if (![self checkActiveCall:callid]) return;
    
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    [activecall switchCameraWithResponseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}
- (void) speakerOnOff:(NSString *)callid {
    if (![self checkActiveCall:callid]) return;
    
    id<KandyCallProtocol> activecall = (id<KandyCallProtocol>) self.activeCalls[callid];
    if (activecall.audioRoute != EKandyCallAudioRoute_speaker) {
        [activecall changeAudioRoute:EKandyCallAudioRoute_speaker withResponseCallback:^(NSError *error){
        [self didHandleResponse:error];
    }];
    } else {
        [activecall changeAudioRoute:EKandyCallAudioRoute_receiver withResponseCallback:^(NSError *error){
            [self didHandleResponse:error];
        }];
    }
}

/**
 * Accept a coming call.
 */
- (void) acceptCall:(NSString *)callid video:(NSString *)video {
    if (![self checkActiveCall:callid]) return;
    BOOL flag = [video boolValue];
    id<KandyIncomingCallProtocol> activecall = (id<KandyIncomingCallProtocol>) self.activeCalls[callid];
    [activecall accept:flag withResponseBlock:^(NSError *error) {
        if (error) {
            [self notifyFailureResponse:error.description];
        } else {
            if (self.showNativeCallPage) {
                [self loadNativeVideoPage:activecall];
            }
            [self notifySuccessResponse:nil];
        }
    }];
}

/**
 * Reject a coming call.
 */
-(void) rejectCall:(NSString *)callid {
    if (![self checkActiveCall:callid]) return;

    id<KandyIncomingCallProtocol> activecall = (id<KandyIncomingCallProtocol>) self.activeCalls[callid];
    [activecall rejectWithResponseBlock:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}

/**
 * Ignore a coming call.
 */
-(void) ignoreCall:(NSString *)callid {
    if (![self checkActiveCall:callid]) return;
    
    id<KandyIncomingCallProtocol> activecall = (id<KandyIncomingCallProtocol>) self.activeCalls[callid];
    [activecall ignoreWithResponseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}

// Set Active user call name and image
- (void) setActivaCallUserName:(NSString *)name image:(NSString *)imgurl {
    [self.activeCallViewController setUserName:name andUserImageURL:imgurl];
}

//*** CHAT SERVICE ***/

/**
 * Send a message to the recipient.
 *
 * @param user The recipient.
 * @param text The message text
 */

-(void)sendMessageTo:(NSString *)recipient message:(NSString *)textMessage type:(NSString *)type {
    
    if (textMessage && [textMessage isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_error_message];
    }
    
    if (recipient && [recipient isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_error_message];
    }
    EKandyRecordType chatType = [type intValue];
    KandyRecord * kandyRecord = [[KandyRecord alloc] initWithURI:recipient type:chatType];
    KandyChatMessage *chatMessage = [[KandyChatMessage alloc] initWithText:textMessage recipient:kandyRecord];
    [self sendChatMessage:chatMessage];
}

/**
 * Send a message to the recipient.
 *
 * @param KandyChatMessage The recipient.
 */
- (void) sendChatMessage:(KandyChatMessage *)chatMessage {
    [[Kandy sharedInstance].services.chat sendChat:chatMessage
      progressCallback:^(KandyTransferProgress *transferProgress) {
          NSLog(@"Uploading message. Recipient - %@, UUID - %@, upload percentage - %ld", chatMessage.recipient.uri, chatMessage.uuid, (long)transferProgress.transferProgressPercentage);
      }
      responseCallback:^(NSError *error) {
          [self didHandleResponse:error];
      }];
}

/**
 * Send text SMS message.
 *
 * @param recipient The recipient user.
 * @param text        The message to send.
 */
-(void)sendSMS:(NSString *)recipient message:(NSString *)textMessage {
    
    if (textMessage && [textMessage isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_error_message];
    }
    
    if (recipient && [recipient isEqual:[NSNull null]]) {
        [self notifyFailureResponse:kandy_error_message];
    }
    
    KandySMSMessage *smsMessage = [[KandySMSMessage alloc] initWithText:textMessage recipient:recipient displayName:recipient];
    [[Kandy sharedInstance].services.chat sendSMS:smsMessage responseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}

- (void) openAttachmentWithURI:(NSString *)uri mimeType:(NSString *)type {
    
    OpenChatAttachment * attachmentVC = nil;
    
    switch ([type intValue]) {
        case EKandyFileType_image:
        {
            attachmentVC = [self.kandyStoryboard instantiateViewControllerWithIdentifier:@"imageview"];
            attachmentVC.image = [UIImage imageWithContentsOfFile:uri];
            break;
        }
        case EKandyFileType_video:{
            attachmentVC = [self.kandyStoryboard instantiateViewControllerWithIdentifier:@"emptyview"];
            attachmentVC.urlMovie = [NSURL fileURLWithPath:uri isDirectory:NO];
            break;
        }
        case EKandyFileType_audio:{
            attachmentVC = [self.kandyStoryboard instantiateViewControllerWithIdentifier:@"emptyview"];
            attachmentVC.urlAudio = [NSURL fileURLWithPath:uri isDirectory:NO];
            break;
        }
        case EKandyFileType_location:{
            attachmentVC = [self.kandyStoryboard instantiateViewControllerWithIdentifier:@"locationview"];
            //attachmentVC.location = mediaItem.location;
            break;
        }
        case EKandyFileType_contact:{
            attachmentVC = [self.kandyStoryboard instantiateViewControllerWithIdentifier:@"emptyview"];
            attachmentVC.vcfFilePath = uri;
            break;
        }
        default:
            break;
    }
    if (attachmentVC) {
        UINavigationController *navigationController =
        [[UINavigationController alloc] initWithRootViewController:attachmentVC];
        [KandyUtil presentModalViewContriller:navigationController];
    }
}

/**
 * Pick audio by iOS default audio picker
 */

- (void) pickAudio {
    MPMediaPickerController *mediaPicker = [[MPMediaPickerController alloc] initWithMediaTypes:MPMediaTypeMusic];
    mediaPicker.delegate = self;
    mediaPicker.allowsPickingMultipleItems = NO;
    [KandyUtil presentModalViewContriller:mediaPicker];
}

/**
 * Send a audio message.
 */
- (void) sendAudio:(NSString *)destination caption:(NSString *)message uri:(NSString *)uri type:(NSString *)type {
    id<KandyMediaItemProtocol> mediaItem = [KandyMessageBuilder createAudioItem:uri text:message];
    KandyRecord * record = [KandyUtil getRecipientKandyRecord:destination];
    [self sendMediaItem:mediaItem withRecord:record];
}

/**
 * Pick video by iOS default video picker
 */
- (void) pickVideo {
    self.pickerView = [[UIImagePickerController alloc] init];
    self.pickerView.delegate = self;
    self.pickerView.allowsEditing = YES;
    self.pickerView.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    self.pickerView.mediaTypes = [[NSArray alloc] initWithObjects:(NSString *)kUTTypeMovie, nil];
    [KandyUtil presentModalViewContriller:self.pickerView];
}

/**
 * Send a video message.
 */
- (void) sendVideo:(NSString *)destination caption:(NSString *)message uri:(NSString *)uri type:(NSString *)type {
    id<KandyMediaItemProtocol> mediaItem = [KandyMessageBuilder createVideoItem:uri text:message];
    KandyRecord * record = [KandyUtil getRecipientKandyRecord:destination];
    [self sendMediaItem:mediaItem withRecord:record];
}

/**
 * Pick image by android default gallery picker
 */
- (void) pickImage {
    self.pickerView = [[UIImagePickerController alloc] init];
    self.pickerView.delegate = self;
    self.pickerView.allowsEditing = YES;
    self.pickerView.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
    self.pickerView.mediaTypes = [[NSArray alloc] initWithObjects:(NSString *)kUTTypeImage, nil];
    [KandyUtil presentModalViewContriller:self.pickerView];
}

/**
 * Send a image message.
 *
 */
- (void) sendImage:(NSString *)destination caption:(NSString *)message uri:(NSString *)uri type:(NSString *)type {
    id<KandyMediaItemProtocol> mediaItem = [KandyMessageBuilder createImageItem:uri text:message];
    KandyRecord * record = [KandyUtil getRecipientKandyRecord:destination];
    [self sendMediaItem:mediaItem withRecord:record];
}
- (void) pickContact {
    NSString *vcardPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"vcard.vcf"];
    [[Kandy sharedInstance].services.contacts getDeviceContactsWithResponseCallback:^(NSError *error, NSArray *kandyContacts) {
        if(kandyContacts.count > 0)
        {
            id<KandyContactProtocol> contact = [kandyContacts objectAtIndex:0];
            [[Kandy sharedInstance].services.contacts createVCardDataByContact:contact completionBlock:^(NSError *error, NSData *vCardData) {
                if(!error)
                {
                    [vCardData writeToFile:vcardPath atomically:YES];
                    NSDictionary * jsonObj = [NSDictionary dictionaryWithObjectsAndKeys:
                                              @(CONTACT_PICKER_RESULT),@"code", vcardPath, @"uri",nil];
                    [self notifySuccessResponse:jsonObj];

                } else {
                    NSDictionary * jsonObj = [NSDictionary dictionaryWithObjectsAndKeys:
                                              @(CONTACT_PICKER_RESULT),@"code", vcardPath, @"uri",nil];
                    [self notifySuccessResponse:jsonObj];
                }
            }];
        }
    }];
}

- (void) sendContact:(NSString *)destination caption:(NSString *)message uri:(NSString *)uri type:(NSString *)type {
    id<KandyMediaItemProtocol> contactMediaItem = [KandyMessageBuilder createContactItem:uri text:message];
    KandyRecord * record = [KandyUtil getRecipientKandyRecord:destination];
    [self sendMediaItem:contactMediaItem withRecord:record];
}
/**
 * Send current location.
 *
*/
- (void) sendCurrentLocation:(NSString *)destination caption:(NSString *)message type:(NSString *)type {
    [[KandyUtil sharedInstance] getCurrentLocationUsingBlcok:^(CLLocation *location) {
        id<KandyMediaItemProtocol> mediaItem = [KandyMessageBuilder createLocationItem:location text:message];
        KandyRecord * record = [KandyUtil getRecipientKandyRecord:destination];
        [self sendMediaItem:mediaItem withRecord:record];
    } andFailure:^(NSError *error) {
        NSLog(@"Fail to send current location");
    }];
}

/**
 * addParticipants
 * Send a location message.
 *
*/
- (void) sendLocation:(NSString *)destination caption:(NSString *)message location:(id)location type:(NSString *)type {
    CLLocation * locationObj = [[CLLocation alloc]initWithLatitude:[location[@"latitude"] floatValue] longitude:[location[@"longitude"] floatValue]];
    id<KandyMediaItemProtocol> mediaItem = [KandyMessageBuilder createLocationItem:locationObj text:message];
    KandyRecord * record = [KandyUtil getRecipientKandyRecord:destination];
    [self sendMediaItem:mediaItem withRecord:record];
}

- (void) downloadMediaFromChat:(NSString *)uuid {
    KandyChatMessage *kandyMessage = [KandyUtil KandyMessageFromUUID:uuid];
    [[Kandy sharedInstance].services.chat downloadMedia:kandyMessage progressCallback:^(KandyTransferProgress *transferProgress) {
            [self notifySuccessResponse:[KandyUtil dictionaryWithTransferProgress:transferProgress]];
    } responseCallback:^(NSError *error, NSString *fileAbsolutePath) {
        if (error) {
            [self notifyFailureResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:fileAbsolutePath];
        }
    }];
}
- (void) downloadMediaThumbnailFromChat:(NSString *)uuid size:(NSString *)size {
    EKandyThumbnailSize thumbsize;
    if ([size isEqual:[NSNull null]] && [size isEqualToString:@"null"]) {
        thumbsize = EKandyThumbnailSize_medium;
    }else {
        thumbsize = [size intValue];
    }
    
    [[Kandy sharedInstance].services.chat downloadMediaThumbnail:[KandyUtil KandyMessageFromUUID:uuid] thumbnailSize:thumbsize progressCallback:^(KandyTransferProgress *transferProgress) {
        [self notifySuccessResponse:[KandyUtil dictionaryWithTransferProgress:transferProgress]];
    } responseCallback:^(NSError *error, NSString *fileAbsolutePath) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:fileAbsolutePath];
        }
    }];
}
- (void) cancelMedia:(NSString *)uuid {
    [[Kandy sharedInstance].services.chat cancel:[KandyUtil KandyMessageFromUUID:uuid] responseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}

// *** GROUP SERVICE **//

/**
 * Create a new group.
 *
 * @param groupName The new group name.
 */

- (void) createGroupName:(NSString *)name {
    KandyGroupParams * kandyGroupParams = [[KandyGroupParams alloc] init];
    kandyGroupParams.groupName = name;
    kandyGroupParams.groupAbsoluteImagePath = [[NSBundle mainBundle] pathForResource:@"group" ofType:@"jpeg"];
    [[Kandy sharedInstance].services.group createGroup:kandyGroupParams progressCallback:^(KandyTransferProgress *transferProgress) {
        NSLog(@"Group Image upload progress : %ld", (long)transferProgress.transferProgressPercentage);
    } responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            //Success
            self.kandyGroup = group;
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) getMyGroups {
    [self.commandDelegate runInBackground:^{
        [[Kandy sharedInstance].services.group getMyGroupsWithResponseCallback:^(NSError *error, NSArray *groups) {
            if (error) {
                [self notifyFailureResponse:error.localizedDescription];
            } else {
                NSMutableArray *groupList = [[NSMutableArray alloc] init];
                for (KandyGroup *group in groups) {
                    [groupList addObject:[KandyUtil dictionaryWithKandyGroup:group]];
                }
                [self notifySuccessResponse:groupList];
            }
        }];
    }];
}
- (void) groupDetailsById:(NSString *)groupid {

    [[Kandy sharedInstance].services.group getGroupDetails:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) updateGroupbyID:(NSString *)groupid name:(NSString *)newgroupname {
    [[Kandy sharedInstance].services.group updateGroupName:newgroupname groupId:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) updateGroupbyID:(NSString *)groupid imagePath:(NSString *)imagepath {
    [[Kandy sharedInstance].services.group updateGroupImage:imagepath groupId:[KandyUtil recordWithGroupID:groupid] progressCallback:^(KandyTransferProgress *transferProgress) {
        NSLog(@"Update group image progress : %ld", (long)transferProgress.transferProgressPercentage);
    } responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) removeGroupImageByID:(NSString *)groupid {
    [[Kandy sharedInstance].services.group removeGroupImage:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) downloadGroupImageByGroupID:(NSString *)groupid {
    [[Kandy sharedInstance].services.group downloadGroupImage:[[KandyGroup alloc] initWithGroupID:[KandyUtil recordWithGroupID:groupid]] progressCallback:^(KandyTransferProgress *transferProgress) {
        [self notifySuccessResponse:[KandyUtil dictionaryWithTransferProgress:transferProgress]];
    } responseCallback:^(NSError *error, NSString *fileAbsolutePath) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:fileAbsolutePath];
        }
    }];
}
- (void) downloadGroupImageThumbnailByGroupID:(NSString *)groupid size:(NSString *)size {
    
    EKandyThumbnailSize thumbsize;
    if ([size isEqual:[NSNull null]] && [size isEqualToString:@"null"]) {
        thumbsize = EKandyThumbnailSize_medium;
    }else {
        thumbsize = [size intValue];
    }
        
    [[Kandy sharedInstance].services.group downloadGroupThumbnail:self.kandyGroup thumbnailSize:thumbsize progressCallback:^(KandyTransferProgress *transferProgress) {
        [self notifySuccessResponse:[KandyUtil dictionaryWithTransferProgress:transferProgress]];
    } responseCallback:^(NSError *error, NSString *fileAbsolutePath) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:fileAbsolutePath];
        }
    }];
}
- (void) muteGroupByID:(NSString *)groupid {
    [[Kandy sharedInstance].services.group muteGroup:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) unmuteGroupByID:(NSString *)groupid {
    
    [[Kandy sharedInstance].services.group unMuteGroup:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) destroyGroupByID:(NSString *)groupid {
    [[Kandy sharedInstance].services.group destroyGroup:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:nil];
        }
    }];
}
- (void) leaveGroupByID:(NSString *)groupid {
    [[Kandy sharedInstance].services.group leaveGroup:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:nil];
        }
    }];
}
- (void) removeParticipantsByID:(NSString *)groupid participants:(id)participants {
    NSMutableArray * arrParticipants = [[NSMutableArray alloc] init];
    for (NSString *participantid in participants) {
        [arrParticipants addObject:[KandyUtil recordWithGroupID:participantid]];
    }
    
    [[Kandy sharedInstance].services.group removeGroupParticipants:arrParticipants groupId:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) muteParticipantsByID:(NSString *)groupid participants:(id)participants{
    NSMutableArray * arrParticipants = [[NSMutableArray alloc] init];
    for (NSString *participantid in participants) {
        [arrParticipants addObject:[KandyUtil recordWithGroupID:participantid]];
    }
    
    [[Kandy sharedInstance].services.group muteGroupParticipants:arrParticipants groupId:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) unmuteParticipantsByID:(NSString *)groupid participants:(id)participants{
    NSMutableArray * arrParticipants = [[NSMutableArray alloc] init];
    for (NSString *participantid in participants) {
        [arrParticipants addObject:[KandyUtil recordWithGroupID:participantid]];
    }

    [[Kandy sharedInstance].services.group unMuteGroupParticipants:arrParticipants groupId:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}
- (void) addParticipantsByID:(NSString *)groupid participants:(id)participants{
    NSMutableArray * arrParticipants = [[NSMutableArray alloc] init];
    for (NSString *participantid in participants) {
        [arrParticipants addObject:[KandyUtil recordWithGroupID:participantid]];
    }
    [[Kandy sharedInstance].services.group addGroupParticipants:arrParticipants groupId:[KandyUtil recordWithGroupID:groupid] responseCallback:^(NSError *error, KandyGroup *group) {
        if (error) {
            [self notifySuccessResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[KandyUtil dictionaryWithKandyGroup:group]];
        }
    }];
}

/**
 * Mark message as read.
 *
 * @param arrKandyEvents The uuid of the message.
 */

-(void)ackEvents:(id)uuids {
    NSArray *params = nil;
    if (![uuids isEqual:[NSNull null]] && [uuids isKindOfClass:[NSArray class]]) {
        params = uuids;
    } else {
        params = [NSArray arrayWithObject:uuids];
    }
    [[Kandy sharedInstance].services.chat markAsReceived:params responseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}

/*
 * Presence service
 */

- (void) getPresenceInfoByUser:(NSString *)user
{
    if (user && [user isEqualToString:@""]) {
        [self notifyFailureResponse:kandy_error_message];
        return;
    }

    KandyRecord* kandyRecord = [[KandyRecord alloc]initWithURI:user];
    [[Kandy sharedInstance].services.presence getPresenceForRecords:[NSArray arrayWithObject:kandyRecord] responseCallback:^(NSError *error, NSArray *presenceObjects, NSArray * missingPresenceKandyRecords) {
        if (error) {
            [self notifyFailureResponse:error.description];
        } else {
            NSMutableArray *presenceList = [[NSMutableArray alloc] init];
            for (id <KandyPresenceProtocol> presence in presenceObjects) {
                double epochTime = [@(floor([presence.lastSeen timeIntervalSince1970])) longLongValue];
                NSDictionary *presenceObj = @ {
                    @"user" : presence.kandyRecord.uri,
                    @"lastSeen" : [NSNumber numberWithDouble:epochTime],
                };
                [presenceList addObject:presenceObj];
            }
            NSMutableArray *absenceList = [[NSMutableArray alloc] init];
            for (KandyRecord *record in missingPresenceKandyRecords) {
                [absenceList addObject:record.uri];
            }
            NSDictionary *result = @{
                                     @"presences" : presenceList,
                                     @"absences" : absenceList
                                     };
            
            [self notifySuccessResponse:result];
        }
    }];
}

// ** CONTACT SERVICE **/

/**
 * Try to get the phones list from the contact.
 *
 */
-(void)getUsersFromDeviceContacts {
    [[Kandy sharedInstance].services.contacts getDeviceContactsWithResponseCallback:^(NSError *error, NSArray *kandyContacts) {
        if (error) {
            [self notifyFailureResponse:error.localizedDescription];
        } else {
            [self notifySuccessResponse:[[KandyUtil sharedInstance] enumerateContactDetails:kandyContacts] withCallbackID:self.callbackID];
        }
    }];
}

/**
 * Try to get contact details.
 *
 */

- (void) getDomainContacts {
    [[Kandy sharedInstance].services.contacts getDomainDirectoryContactsWithResponseCallback:^(NSError *error, NSArray *kandyContacts) {
        if (error) {
            [self didHandleResponse:error];
        } else {
            [self notifySuccessResponse:[[KandyUtil sharedInstance] enumerateContactDetails:kandyContacts] withCallbackID:self.callbackID];
        }
    }];
}

- (void) getFilteredDomainDirectoryContacts:(NSString *)fields searchString:(NSString*)text {
    EKandyDomainContactFilter filter = [fields intValue];
    [[Kandy sharedInstance].services.contacts getFilteredDomainDirectoryContactsWithTextSearch:text filterType:filter caseSensitive:NO responseCallback:^(NSError *error, NSArray *kandyContacts) {
        if (error) {
            [self didHandleResponse:error];
        } else {
            [self notifySuccessResponse:[[KandyUtil sharedInstance] enumerateContactDetails:kandyContacts] withCallbackID:self.callbackID];
        }
    }];
}

- (void) getPersonalAddressBook {
    [[Kandy sharedInstance].services.contacts getPersonalAddressBookWithResponseCallback:^(NSError *error, NSArray *kandyContacts) {
        if (error) {
            [self didHandleResponse:error];
        } else {
            [self notifySuccessResponse:[[KandyUtil sharedInstance] enumerateContactDetails:kandyContacts] withCallbackID:self.callbackID];
        }
    }];
}
- (void) updateDeviceProfile:(NSString *)dispname deviceName:(NSString *)devicename deviceFamily:(NSString *)familyname {
    KandyDeviceProfileParams * deviceParams = [[KandyDeviceProfileParams alloc] init];
    deviceParams.deviceDisplayName = dispname;
    [[[[Kandy sharedInstance] services] profile] updateDeviceProfile:deviceParams responseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}
- (void) getUserDeviceProfiles {
    [self.commandDelegate runInBackground:^{
        [[[[Kandy sharedInstance] services] profile] getUserDeviceProfilesWithResponseCallback:^(NSError *error, NSArray *userDeviceProfiles) {
            if (error) {
                [self didHandleResponse:error];
            } else {
                NSMutableArray * deviceParams = [[NSMutableArray alloc] initWithCapacity:[userDeviceProfiles count]];
                for (KandyDeviceProfileParams *profile in userDeviceProfiles) {
                    [deviceParams addObject:@{@"deviceDisplayName":profile.deviceDisplayName}];
                }
                [self notifySuccessResponse:deviceParams];
            }
        }];
    }];
}

- (void) getUserCredit {
    [self.commandDelegate runInBackground:^{
        [[[[Kandy sharedInstance] services] billing] getUserCreditWithResponseCallback:^(NSError *error, KandyCredit *kandyCredit) {
            if (error) {
                [self didHandleResponse:error];
            } else {
                NSMutableArray *billingPackages = [[NSMutableArray alloc] initWithCapacity:[kandyCredit.packages count]];
                for (KandyBillingPackage *billing in kandyCredit.packages) {
                    [billingPackages addObject:@{
                                                 @"currency" : billing.currency,
                                                 @"balance" : @(billing.balance),
                                                 @"exiparyDate" : billing.exiparyDate,
                                                 @"packageId" : billing.Id,
                                                 @"remainingTime" : @(billing.remainingTime),
                                                 @"startDate" : billing.startDate,
                                                 @"packageName" : billing.name
                                                 }];
                }
                NSDictionary *result = @{
                                         @"credit": @(kandyCredit.credit),
                                         @"currency":kandyCredit.currency,
                                         @"dids": kandyCredit.DIDs,
                                         @"properties" : billingPackages
                                         };
                [self notifySuccessResponse:result];
            }
        }];
    }];
}

// Cloud Service

- (void) uploadMediaURI:(NSString *)uri {
    [self.commandDelegate runInBackground:^{
        id<KandyFileItemProtocol> transferingFileItem = [KandyMessageBuilder createFileItem:uri text:nil];
        // Upload the file
        [[Kandy sharedInstance].services.cloudStorage uploadMedia:transferingFileItem progressCallback:^(KandyTransferProgress *transferProgress) {
            [self notifySuccessResponse:[KandyUtil dictionaryWithTransferProgress:transferProgress]];
        } responseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }];
}

- (void) downloadCloudMedia:(NSString *)uuid fileName:(NSString *)filename {
    [self.commandDelegate runInBackground:^{
        // Save downloaded files to /Documents with name "document-1"..."document-N"
        NSString * downloadFileName = [[KandyUtil documentsDirectory] stringByAppendingPathComponent:filename];
        NSInteger counter = 1;
        BOOL isDirectory = NO;
        
        // Check files already exist with this name
        while ([[NSFileManager defaultManager] fileExistsAtPath:downloadFileName isDirectory:&isDirectory]) {
            downloadFileName = [NSString stringWithFormat:@"%@/%@_%ld", [KandyUtil documentsDirectory], filename, (long)counter++];
        }
        id<KandyFileItemProtocol> transferingFileItem = [KandyMessageBuilder createFileItem:downloadFileName text:nil];
        
        // Download the file
        [[Kandy sharedInstance].services.cloudStorage downloadMedia:transferingFileItem fileName:[downloadFileName lastPathComponent] downloadPath:[KandyUtil documentsDirectory] progressCallback:^(KandyTransferProgress *transferProgress) {
            [self notifySuccessResponse:[KandyUtil dictionaryWithTransferProgress:transferProgress]];
        } responseCallback:^(NSError *error, NSString *filePath) {
            [self didHandleResponse:error];
        }];
    }];
}

- (void) cancelCloudMediaTransfer:(NSString *)uuid fileName:(NSString *)filename {
    [self.commandDelegate runInBackground:^{
        NSString * cancelFileName = [[KandyUtil documentsDirectory] stringByAppendingPathComponent:filename];
        id<KandyFileItemProtocol> transferingFileItem = [KandyMessageBuilder createFileItem:cancelFileName text:nil];
        [[Kandy sharedInstance].services.cloudStorage cancelMediaTransfer:transferingFileItem responseCallback:^(NSError *error) {
            [self didHandleResponse:error];
        }];
    }];
}

// AddressBook
- (void) addContact:(KandyContactParams *)contact {
    [[Kandy sharedInstance].services.contacts addPersonalConatct:contact responseCallback:^(NSError *error, id<KandyContactProtocol> kandyContact) {
        //Handle response
    }];
}

- (void) removeContactByUerID:(NSString *)contactid {
    [[Kandy sharedInstance].services.contacts deletePersonalContact:contactid responseCallback:^(NSError *error) {
        //Handle response
    }];
}

/** Push Notification **/

/**
 * Enable Push Notification service.
 */

-(void)enableKandyPushNotification
{
    NSData* deviceToken = [[NSUserDefaults standardUserDefaults]objectForKey:@"deviceToken"];
    NSString* bundleId = [[NSBundle mainBundle]bundleIdentifier];
    BOOL isSandbox = NO;
    #ifdef DEBUG
        isSandbox = YES;
    #endif
    
    [[Kandy sharedInstance].services.push enableRemoteNotificationsWithToken:deviceToken bundleId:bundleId isSandbox:isSandbox responseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}
/**
 * Disable Push Notification service.
 */
- (void) disableKandyPushNotification {
    NSString* bundleId = [[NSBundle mainBundle]bundleIdentifier];
    [[Kandy sharedInstance].services.push disableRemoteNotificationsWithBundleId:bundleId responseCallback:^(NSError *error) {
        [self didHandleResponse:error];
    }];
}

#pragma mark - Helper methods

- (void) didHandleResponse:(NSError *)error {
    if (error) {
        [self notifyFailureResponse:[NSString stringWithFormat:kandy_error_message,error.code,error.description]];
    } else {
        [self notifySuccessResponse:nil];
    }
}
- (void) notifySuccessResponse:(id)response {
    [self notifySuccessResponse:response withCallbackID:self.callbackID];
}

- (void) notifySuccessResponse:(id)response withCallbackID:(NSString *)callbackId {
    // Create an instance of CDVPluginResult, with an OK status code.
    CDVPluginResult *pluginResult;
    if ([response isKindOfClass:[NSDictionary class]]) {
        pluginResult = [ CDVPluginResult
                        resultWithStatus    : CDVCommandStatus_OK
                        messageAsDictionary : response
                        ];
    }else if ([response isKindOfClass:[NSArray class]]) {
            pluginResult = [ CDVPluginResult
                            resultWithStatus: CDVCommandStatus_OK
                            messageAsArray: response
                            ];
    } else {
        pluginResult = [ CDVPluginResult
                        resultWithStatus: CDVCommandStatus_OK
                        messageAsString:response
                        ];
    }
    // Execute sendPluginResult on this plugin's commandDelegate, passing in the ...
    // ... instance of CDVPluginResult
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) notifyFailureResponse:(NSString *)errString {
    [self notifyFailureResponse:errString withCallbackID:self.callbackID];
}

- (void) notifyFailureResponse:(NSString *)errString withCallbackID:(NSString *)callbackId {
    // Create an instance of CDVPluginResult, with an OK status code.
    // Set the return message as the String object (errString)...
    CDVPluginResult *pluginResult = [ CDVPluginResult
                                     resultWithStatus    : CDVCommandStatus_ERROR
                                     messageAsString:errString
                                     ];
    
    // Execute sendPluginResult on this plugin's commandDelegate, passing in the ...
    // ... instance of CDVPluginResult
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) handleRequiredInputError {
    [self notifyFailureResponse:@"Missing required input parameter" withCallbackID:self.callbackID];
}
- (void) answerIncomingCall {
    self.incomingCallOPtions = [[UIActionSheet alloc] initWithTitle:@"Incoming Call" delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:@"Accept With Video", @"Accept Without Video", @"Reject", @"Ignore", nil];
    self.incomingCallOPtions.tag = 200;
    [self.incomingCallOPtions showInView:[UIApplication sharedApplication].keyWindow];
}

- (void) WillHandleOutgoingCall:(id <KandyOutgoingCallProtocol>)call {
    [[KandyUtil sharedInstance] ringOut];
    [self.activeCalls setObject:call forKey:call.callee.uri];
    [call establishWithResponseBlock:^(NSError *error) {
        if (error) {
            [[KandyUtil sharedInstance] stopRingOut];
            [self didHandleResponse:error];
        } else if(!self.showNativeCallPage) {
            [self loadNativeVideoView];
        } else {
            [self loadNativeVideoPage:call];
        }
        [self notifySuccessResponse:@"Call Init"];
    }];
}

- (void) loadNativeVideoView {
    // Local Video
    self.viewLocalVideo = [[UIView alloc] initWithFrame:CGRectZero];
    self.viewLocalVideo.backgroundColor = [UIColor blackColor];
    [self.webView.superview addSubview:self.viewLocalVideo];
    
    // Remote Video
    self.viewRemoteVideo = [[UIView alloc] initWithFrame:CGRectZero];
    self.viewRemoteVideo.backgroundColor = [UIColor blackColor];
    [self.webView.superview addSubview:self.viewRemoteVideo];
}

- (void) loadNativeVideoPage:(id<KandyCallProtocol>) kandyCall {
    self.activeCallViewController = [self.kandyStoryboard instantiateViewControllerWithIdentifier:@"callview"];
    self.activeCallViewController.kandyCall = kandyCall;
    self.activeCallViewController.delegate = self;
    UINavigationController *navigationController =
    [[UINavigationController alloc] initWithRootViewController:self.activeCallViewController];
    [KandyUtil presentModalViewContriller:navigationController];
}

-(void) sendMediaItem:(id<KandyMediaItemProtocol>)mediaItem withRecord:(KandyRecord *)kandyRecord
{
    if(mediaItem)
    {
        KandyChatMessage *chatMessage = [[KandyChatMessage alloc] initWithMediaItem:mediaItem recipient:kandyRecord];
        [self sendChatMessage:chatMessage];
    }
}

- (void) showAttachementOptions:(NSString *)destination caption:(NSString *)message type:(NSString *)type {
    [[KandyUtil sharedInstance] showAttachmentOptionsUsingBlock:^(NSInteger index) {
        switch (index) {
            case image:
            {
                [self pickImage];
            }
                break;
            case video:
            {
                [self pickVideo];
            }
                break;
            case audio:
            {
                [self pickAudio];
            }
                break;
            case location:
            {
                [self sendCurrentLocation:destination caption:message type:type];
            }
                break;
            case contact:
            {
                [self pickContact];
            }
                break;
            default:
                break;
        }
    }];
}

- (void) applyKandySettings {
    if (self.autoDownloadMediaConnectionType) {
        [[Kandy sharedInstance].services.chat.settings setAutoDownload_media_connectionType:[self.autoDownloadMediaConnectionType intValue]];
    }
    if (self.mediaMaxSize > -1) {
        [[Kandy sharedInstance].services.chat.settings setAutoDownload_media_maxSizeKB:self.mediaMaxSize];
    }
    if (self.autoDownloadThumbnailSize) {
        [[Kandy sharedInstance].services.chat.settings setAutoDownload_thumbnailSize:[self.autoDownloadThumbnailSize  intValue]];
    }
}

#pragma mark - Delegate

/**
 * The listeners for callback
 */

#pragma mark - KandyAccessNotificationDelegate

-(void) registrationStatusChanged:(EKandyRegistrationState)registrationState
{
    //TODO:
}

-(void) connectionStatusChanged:(EKandyConnectionState)connectionStatus {
    NSDictionary *jsonObj = @{
                             @"action": @"onConnectionStateChanged",
                             @"data": [self.connectionState objectAtIndex:connectionStatus],
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyConnectServiceNotificationCallback];
}

-(void) gotInvalidUser:(NSError*)error{
    NSDictionary *jsonObj = @{
                             @"action": @"onInvalidUser",
                             @"data": error,
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyConnectServiceNotificationCallback];
}
// Handled by appDelegate
-(void) sessionExpired:(NSError*)error{
    NSDictionary *jsonObj = @{
                             @"action": @"onSessionExpired",
                             @"data": error,
                             };
    
    if (self.renewSession) {
        [[Kandy sharedInstance].access renewExpiredSession:^(NSError *error) {
        }];
    }
    
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyConnectServiceNotificationCallback];
}
// Handled by appDelegate
-(void) SDKNotSupported:(NSError*)error{
    NSDictionary *jsonObj = @{
                             @"action": @"onSDKNotSupported",
                             @"data": error,
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyConnectServiceNotificationCallback];
}

#pragma mark - KandyCallServiceNotificationDelegate

/**
 *
 * @param call
 */
-(void) gotIncomingCall:(id<KandyIncomingCallProtocol>)call{
    NSLog(@"gotIncomingCall...");
    [[KandyUtil sharedInstance] ringIn];
    self.incomingcall = call.callee.uri;
    [self.activeCalls setObject:call forKey:call.callee.uri];
    NSDictionary *jsonObj = @{
                             @"action": @"onIncomingCall",
                             @"data": @{
                                        @"id": call.callId,
                                        @"callee":call.callee.uri
                                        }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
    
    
    if ([[UIApplication sharedApplication] applicationState] == UIApplicationStateBackground) {

        NSLog(@"Background Incoming call...");
        NSCalendar *calendar = [NSCalendar autoupdatingCurrentCalendar];
        NSDateComponents *dateComps = [[NSDateComponents alloc] init];
        NSDate *itemDate = [calendar dateFromComponents:dateComps];
        
        UILocalNotification *localNotif = [[UILocalNotification alloc] init];
        if (localNotif == nil)
            return;
        localNotif.fireDate = itemDate;
        localNotif.timeZone = [NSTimeZone defaultTimeZone];
        
        localNotif.alertBody = [NSString stringWithFormat:NSLocalizedString(@"%@ Calling...", nil),
                                call.callee.uri];
        localNotif.alertAction = NSLocalizedString(@"View", nil);
        localNotif.alertTitle = NSLocalizedString(@"Incoming call", nil);
        
        localNotif.soundName = UILocalNotificationDefaultSoundName;
        //localNotif.applicationIconBadgeNumber = 1;
        
        NSDictionary *infoDict = [NSDictionary dictionaryWithObject:call.callee.uri forKey:@"Callee"];
        localNotif.userInfo = infoDict;
        
        [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
    }
    
    [self answerIncomingCall];
}

/**
 *
 * @param state
 * @param call
 */
-(void) stateChanged:(EKandyCallState)callState forCall:(id<KandyCallProtocol>)call{
    
    switch (callState) {
        case EKandyCallState_talking:
        case EKandyCallState_terminated: {
            [self.activeCalls removeObjectForKey:call.callId];
        }
        case EKandyCallState_notificationWaiting:
        case EKandyCallState_unknown: {
            [[KandyUtil sharedInstance] stopRingOut];
            break;
        }
        default:
            break;
    }

    NSDictionary *jsonObj = @{
                             @"action": @"onCallStateChanged",
                             @"data": @{
                                     @"state": [self.callState objectAtIndex:callState],
                                     @"id": (call.callId ? call.callId : @"0"),
                                     @"callee": (call.callee.uri ? call.callee.uri : @"")
                                     }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
    
}

/**
 *
 * @param iKandyCall
 * @param isReceivingVideo
 * @param isSendingVideo
 */
-(void) videoStateChangedForCall:(id<KandyCallProtocol>)call{
    NSDictionary *jsonObj = @{
                             @"action": @"onVideoStateChanged",
                             @"data": @{
                                     @"id": call.callId,
                                     @"callee": call.callee.uri,
                                     @"isReceivingVideo": @(call.isReceivingVideo),
                                     @"isSendingVideo": @(call.isSendingVideo)
                                     }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
}

/**
 *
 * @param call
 * @param onMute
 */
-(void) audioRouteChanged:(EKandyCallAudioRoute)audioRoute forCall:(id<KandyCallProtocol>)call{
    NSDictionary *jsonObj = @{
                             @"action": @"onAudioStateChanged",
                             @"data": @{
                                         @"id": call.callId,
                                         @"callee": call.callee.uri,
                                         @"isMute": @(call.isMute)
                                        }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
}
-(void) gotMissedCall:(KandyMissedCall*)missedCall {
    [self actionSheet:self.incomingCallOPtions clickedButtonAtIndex:3];
    NSDictionary *jsonObj = @{
                             @"action": @"onMissedCall",
                             @"data": @{
                                     @"id": missedCall.caller.userName,
                                     @"callee": missedCall.caller.uri
                                     }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
}
-(void) participantsChanged:(NSArray*)participants forCall:(id<KandyCallProtocol>)call{
}
-(void) videoCallImageOrientationChanged:(EKandyVideoCallImageOrientation)newImageOrientation forCall:(id<KandyCallProtocol>)call{
}
-(void) availableAudioOutputChanged:(NSArray*)updatedAvailableAudioOutputs {
}
/**
 *
 * @param call
 */
-(void) GSMCallIncoming {
    NSDictionary *jsonObj = @{
                             @"action": @"onGSMCallIncoming"
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
}

-(void) GSMCallDialing{
}
-(void) GSMCallConnected {
    NSDictionary *jsonObj = @{
                             @"action": @"onGSMCallConnected"
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
}
-(void) GSMCallDisconnected {
    NSDictionary *jsonObj = @{
                             @"action": @"onGSMCallDisconnected"
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyCallServiceNotificationCallback];
}

-(void)gotPendingVoiceMailMessage:(KandyPendingVoiceMail *)pendingVoiceMail
{
    //handle this as you wish
}

#pragma mark - KandyChatServiceNotificationDelegate


-(void)onMessageReceived:(id<KandyMessageProtocol>)kandyMessage recipientType:(EKandyRecordType)recipientType {
    double epochTime = [@(floor([kandyMessage.timestamp timeIntervalSince1970])) longLongValue];
    EKandyFileType  mediaType = [[kandyMessage mediaItem]mediaType];
    NSDictionary *jsonObj = @{
                             @"action": @"onChatReceived",
                             @"data": @{
                                     @"uuid": ([kandyMessage uuid] ? [kandyMessage uuid] : @""),
                                     @"message":@{
                                             @"sender": ([[kandyMessage sender]uri] ? [[kandyMessage sender]uri] : @""),
                                             @"UUID": ([kandyMessage uuid] ? [kandyMessage uuid] : @""),
                                             @"contentType":kandyFileTypes[(mediaType ? mediaType : 0)],
                                             @"message":@{@"text":([[kandyMessage mediaItem]text] ? [[kandyMessage mediaItem]text] : @"")},
                                             @"timestamp": [NSNumber numberWithDouble:epochTime],
                                             @"messageType": kandyMessageType[([kandyMessage type]? [kandyMessage type] : 0)]
                                                  },
                                     @"type": @(recipientType)
                                     }
                                };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationCallback];
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationPluginCallback];
    
    if (self.hasNativeAcknowledgement) {
        [kandyMessage markAsReceivedWithResponseCallback:^(NSError *error) {
        }];
    }
}
-(void)onMessageDelivered:(KandyDeliveryAck *)ackData {

    double epochTime = [@(floor([ackData.timestamp timeIntervalSince1970])) longLongValue];

    NSDictionary *jsonObj = @{
                             @"action": @"onChatDelivered",
                             @"data" : @{
                                            @"uuid": ackData.uuid,
                                            @"timestamp": [NSNumber numberWithDouble:epochTime],
                                        }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationCallback];
//    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationPluginCallback];
}

-(void) onAutoDownloadProgress:(KandyTransferProgress*)transferProgress kandyMessage:(id<KandyMessageProtocol>)kandyMessage {
    NSDictionary *jsonObj = @{
                             @"action": @"onChatMediaAutoDownloadProgress",
                             @"data": @{
                                     @"uuid": kandyMessage.uuid,
                                     @"timestamp": kandyMessage.timestamp,
                                     @"process": @(transferProgress.transferProgressPercentage),
                                     @"state": @(transferProgress.transferState),
                                     @"byteTransfer": @(transferProgress.transferredSize),
                                     @"byteExpected": @(transferProgress.expectedSize)
                                     }
                             };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationCallback];
//    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationPluginCallback];
}

-(void) onAutoDownloadFinished:(NSError*)error fileAbsolutePath:(NSString*)path kandyMessage:(id<KandyMessageProtocol>)kandyMessage {
    NSDictionary *jsonObj;
    if(error)
    {
        jsonObj = @{
                   @"action": @"onChatMediaAutoDownloadFailed",
                   @"data": @{
                           @"error": error.description,
                           @"code": @(error.code)
                           }
                   };
    } else {
        jsonObj = @{
                   @"action": @"onChatMediaAutoDownloadSucceded",
                   @"data": @{ @"uri": kandyMessage.recipient.uri }
                   };
    }
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationCallback];
//    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyChatServiceNotificationPluginCallback];
}

#pragma mark - KandyGroupServiceNotificationDelegate

-(void)onGroupDestroyed:(KandyGroupDestroyed*)groupDestroyedEvent{
    
    double epochTime = [@(floor([groupDestroyedEvent.timestamp timeIntervalSince1970])) longLongValue];
    
    NSDictionary *jsonObj = @{
                              @"action": @"onGroupDestroyed",
                              @"data" : @{
                                      @"id": groupDestroyedEvent.groupId,
                                      @"uuid": groupDestroyedEvent.uuid,
                                      @"eraser": groupDestroyedEvent.eraser.uri,
                                      @"timestamp": [NSNumber numberWithDouble:epochTime],
                                      }
                              };

    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyGroupServiceNotificationCallback];
}

-(void)onGroupUpdated:(KandyGroupUpdated*)groupUpdatedEvent{
    double epochTime = [@(floor([groupUpdatedEvent.timestamp timeIntervalSince1970])) longLongValue];
    NSDictionary *jsonObj = @{
                              @"action": @"onGroupUpdated",
                              @"data" : @{
                                      @"id": (groupUpdatedEvent.groupId.uri ? groupUpdatedEvent.groupId.uri : @""),
                                      @"uuid": groupUpdatedEvent.uuid,
                                      @"groupParams": @{
                                                @"name": (groupUpdatedEvent.groupName ? groupUpdatedEvent.groupName : @""),
                                                @"image": (groupUpdatedEvent.groupImage.fileAbsolutePath ? groupUpdatedEvent.groupImage.fileAbsolutePath : @""),
                                              },
                                      @"timestamp": [NSNumber numberWithDouble:epochTime],
                                      }
                              };
    
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyGroupServiceNotificationCallback];
}

-(void)onParticipantJoined:(KandyGroupParticipantJoined*)participantJoined{
    
    double epochTime = [@(floor([participantJoined.timestamp timeIntervalSince1970])) longLongValue];
    NSMutableArray *invitees = [[NSMutableArray alloc] init];
    if (participantJoined.invitees) {
        for (KandyRecord* invitee in participantJoined.invitees) {
            [invitees addObject:[KandyUtil dictionaryWithKandyRecord:invitee]];
        }
    }
    
    NSDictionary *jsonObj = @{
                              @"action": @"onParticipantJoined",
                              @"data" : @{
                                      @"groupId": [KandyUtil dictionaryWithKandyRecord:participantJoined.groupId],
                                      @"uuid": participantJoined.uuid,
                                      @"inviter": [KandyUtil dictionaryWithKandyRecord:participantJoined.inviter],
                                      @"timestamp": [NSNumber numberWithDouble:epochTime],
                                      @"invitees": ([invitees count] > 0 ? invitees : @"")
                                      }
                              };
    
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyGroupServiceNotificationCallback];
}

-(void)onParticipantKicked:(KandyGroupParticipantKicked*)participantKicked{
    double epochTime = [@(floor([participantKicked.timestamp timeIntervalSince1970])) longLongValue];
    NSMutableArray *booters = [[NSMutableArray alloc] init];
    if (participantKicked.bootedParticipants) {
        for (KandyRecord* booter in participantKicked.bootedParticipants) {
            [booters addObject:[KandyUtil dictionaryWithKandyRecord:booter]];
        }
    }
    NSDictionary *jsonObj = @{
                              @"action": @"onParticipantKicked",
                              @"data" : @{
                                      @"groupId": [KandyUtil dictionaryWithKandyRecord:participantKicked.groupId],
                                      @"uuid": participantKicked.uuid,
                                      @"booter": [KandyUtil dictionaryWithKandyRecord:participantKicked.booter],
                                      @"timestamp": [NSNumber numberWithDouble:epochTime],
                                      @"booted": ([booters count] > 0 ? booters : @"")
                                      }
                              };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyGroupServiceNotificationCallback];
}

-(void)onParticipantLeft:(KandyGroupParticipantLeft*)participantLeft{
    double epochTime = [@(floor([participantLeft.timestamp timeIntervalSince1970])) longLongValue];
    
    NSDictionary *jsonObj = @{
                              @"action": @"onParticipantLeft",
                              @"data" : @{
                                      @"groupId": [KandyUtil dictionaryWithKandyRecord:participantLeft.groupId],
                                      @"uuid": participantLeft.uuid,
                                      @"leaver": [KandyUtil dictionaryWithKandyRecord:participantLeft.leaver],
                                      @"timestamp": [NSNumber numberWithDouble:epochTime],
                                      }
                              };
    [self notifySuccessResponse:jsonObj withCallbackID:self.kandyGroupServiceNotificationCallback];
}

#pragma mark - KandyContactsServiceNotificationDelegate

-(void)onDeviceContactsChanged {
    [self notifySuccessResponse:nil withCallbackID:self.kandyAddressBookServiceNotificationCallback];
}

#pragma mark - UIActionSheetDelegate
- (void)actionSheet:(UIActionSheet *)popup clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (buttonIndex) {
        case 0:
            [self acceptCall:self.incomingcall video:@"YES"];
            break;
        case 1:
            [self acceptCall:self.incomingcall video:@"NO"];
            break;
        case 2:
            [self rejectCall:self.incomingcall];
            break;
        case 3:
            [self ignoreCall:self.incomingcall];
            break;
        default:
            break;
    }
    [[KandyUtil sharedInstance] stopRingIn];
    [popup dismissWithClickedButtonIndex:buttonIndex animated:YES];
}

#pragma mark - UIImagePickerControllerDelegate

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    NSString *path;
    NSDictionary * jsonObj;

    if ([picker sourceType] == UIImagePickerControllerSourceTypeSavedPhotosAlbum) {
        UIImage *chosenImage = [info objectForKey:UIImagePickerControllerEditedImage];
        path = [KandyUtil saveImage:chosenImage];
        jsonObj = @{
                    @"code": @(IMAGE_PICKER_RESULT),
                    @"uri": path
                    };
    }
    
    if ([picker sourceType] == UIImagePickerControllerSourceTypePhotoLibrary) {
        NSURL *videoUrl=(NSURL*)[info objectForKey:UIImagePickerControllerMediaURL];
        path = [videoUrl path];
        jsonObj = @{
                    @"code": @(VIDEO_PICKER_RESULT),
                    @"uri": path
                    };
    }

    [self notifySuccessResponse:jsonObj];
    [picker dismissViewControllerAnimated:YES completion:NULL];
}

#pragma mark - MPMediaPickerDelegate

- (void)mediaPicker: (MPMediaPickerController *)mediaPicker didPickMediaItems:(MPMediaItemCollection *)mediaItemCollection
{
    MPMediaItem *item = [[mediaItemCollection items] objectAtIndex:0];
    NSURL *url = [item valueForProperty:MPMediaItemPropertyAssetURL];

    NSDictionary * jsonObj = @{
                               @"code": @(AUDIO_PICKER_RESULT),
                               @"uri": url
                               };

    [self notifySuccessResponse:jsonObj];
}

- (void)mediaPickerDidCancel:(MPMediaPickerController *)mediaPicker {
    [mediaPicker dismissViewControllerAnimated:YES completion:nil];
}
@end