//
//  KandyUtil.m
//  Kandy
//
//  Created by Srinivasan Baskaran on 5/11/15.
//
//

#import "KandyUtil.h"

NSString *kandy_error_message = @"Response code: %d - %@";
 NSString *kandy_login_login_success = @"Login succeed";
 NSString *kandy_login_logout_success = @"Logout succeed";
 NSString *kandy_login_empty_username_text = @"Invalid username (userID@domain.com)";
 NSString *kandy_login_empty_password_text = @"Enter password";

 NSString *kandy_calls_local_video_label = @"Local video";
 NSString *kandy_calls_checkbox_label = @"Start with video";
 NSString *kandy_calls_state_video_label = @"Video state";
 NSString *kandy_calls_state_audio_label = @"Audio state";
 NSString *kandy_calls_state_calls_label = @"Calls state";
 NSString *kandy_calls_hold_label = @"hold";
 NSString *kandy_calls_unhold_label = @"unhold";
 NSString *kandy_calls_mute_label = @"mute";
 NSString *kandy_calls_unmute_label = @"unmute";
 NSString *kandy_calls_video_label = @"video";
 NSString *kandy_calls_novideo_label = @"no video";
 NSString *kandy_calls_call_button_label = @"Call";
 NSString *kandy_calls_hangup_button_label = @"Hangup";
 NSString *kandy_calls_receiving_video_state = @"Receiving video:";
 NSString *kandy_calls_sending_video_state = @"Sending video:";
 NSString *kandy_calls_audio_state = @"Audio isMute:";
 NSString *kandy_calls_phone_number_hint = @"userID@domain.com";
 NSString *kandy_calls_invalid_phone_text_msg = @"Invalid recipient (recipientID@domain.com)";
 NSString *kandy_calls_invalid_domain_text_msg = @"Wrong domain";
 NSString *kandy_calls_invalid_hangup_text_msg = @"No active calls";
 NSString *kandy_calls_invalid_hold_text_msg = @"Can not hold - No Active Call";
 NSString *kandy_calls_invalid_mute_call_text_msg = @"Can not mute - No active calls";
 NSString *kandy_calls_invalid_video_call_text_msg = @"Can not enable/disable video - No active calls";
 NSString *kandy_calls_attention_title_text = @"!!! ATTENTION !!!";
 NSString *kandy_calls_full_user_id_message_text = @"Enter full destination user id (userID@domain.com)";
 NSString *kandy_calls_answer_button_label = @"Answer";
 NSString *kandy_calls_ignore_incoming_call_button_label = @"Ignore";
 NSString *kandy_calls_reject_incoming_call_button_label = @"Reject";
 NSString *kandy_calls_incoming_call_popup_message_label = @"Incoming call from:";
 NSString *kandy_calls_remote_video_label = @"Remote video";
 NSString *kandy_chat_phone_number_verification_text = @"Invalid recipient\'s number (recipientID@domain.com)";


NSString * const kandyFileTypes[] = { @"unknown", @"text", @"image", @"video", @"audio", @"location", @"contact", @"file", @"custom" };

NSString * const kandyMessageType[] = {@"UNKNOWN", @"CHAT", @"SMS"};

//Kandy Login details
 NSString *kandy_api_key = @"_kandy_api_key";
 NSString *kandy_api_secret = @"_kandy_secret";
 NSString *kandy_host_url = @"_kandy_host_url";

NSString * METHOD = @"method";
NSString * PARAMS = @"params";
NSString * EXTRAPARAM = @"extraparam";

@interface KandyUtil() <CLLocationManagerDelegate, UIActionSheetDelegate>
@property (nonatomic) AVAudioPlayer *ringin;
@property (nonatomic) AVAudioPlayer *ringout;
//Location callback properties
@property (nonatomic, copy) successResponse success;
@property (nonatomic, copy) failureResponse failure;
@property (nonatomic, copy) successAttachmentOPtions successOptions;
@end;


static KandyUtil *obj;
@implementation KandyUtil


+ (KandyUtil *) sharedInstance {
    static dispatch_once_t once;
    static id sharedInstance;
    dispatch_once(&once, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (id) init {
    
    if (self = [super init]) {
        
    self.kandyServices = @{
                        @(HANGUP) : @{
                               METHOD: @"hangupCall:",
                               PARAMS: @"1"
                               },
                        @(MUTE) : @{
                               METHOD: @"muteCall:mute:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"1"]
                               },
                        @(UNMUTE) : @{
                               METHOD: @"muteCall:mute:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"0"]
                               },
                       @(HOLD) : @{
                               METHOD: @"holdCall:hold:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"1"]
                               },
                       @(UNHOLD) : @{
                               METHOD: @"holdCall:hold:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"0"]
                               },
                       @(EVIDEO) : @{
                               METHOD: @"enableVideoCall:video:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"1"]
                               },
                       @(DVIDEO) : @{
                               METHOD: @"enableVideoCall:video:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"0"]
                               },
                       @(SWITCHCAMERA) : @{
                               METHOD: @"switchCamera:",
                               PARAMS: @"0",
                               },
                       @(SPEAKERONOFF) : @{
                               METHOD: @"speakerOnOff:",
                               PARAMS: @"0",
                               },
                        @(TRANSFERCALL) : @{
                            METHOD: @"transferCall:destination:",
                            PARAMS: @"2",
                            },
                       @(ACCEPT) : @{
                               METHOD: @"acceptCall:video:",
                               PARAMS: @"1",
                               EXTRAPARAM: @[@"1"]
                               },
                       @(REJECT) : @{
                               METHOD:@"rejectCall:",
                               PARAMS:@"1",
                               },
                       @(IGNORE) : @{
                               METHOD: @"ignoreCall:",
                               PARAMS: @"1",
                               },
                        @(VOIP) : @{
                            METHOD: @"establishVoipCallTo:andWithStartVideo:",
                            PARAMS: @"2",
                            },
                        @(PSTN) : @{
                            METHOD: @"establishPSTNCall:",
                            PARAMS: @"1",
                            },
                        @(LOGIN) : @{
                            METHOD: @"connectWithUserName:andPassword:",
                            PARAMS: @"2",
                        },
                    @(TOKENLOGIN) : @{
                            METHOD: @"loginWithToken:",
                            PARAMS: @"1",
                            },
                    @(LOGOUT) : @{
                            METHOD: @"connectWithUserName:andPassword:",
                            PARAMS: @"2",
                            },
                    @(APIKEY) : @{
                            METHOD: @"setKandyDomainAPIKey:andSecret:",
                            PARAMS: @"2"
                            },
                    @(SETHOST) : @{
                            METHOD: @"setkandyHostURL:",
                            PARAMS: @"1",
                            },
                    @(REQUEST) : @{
                            METHOD: @"requestCodeWithPhone:andISOCountryCode:",
                            PARAMS: @"2",
                            },
                    @(VALIDATE) : @{
                            METHOD: @"validate:otp:ISOCountryCode:",
                            PARAMS: @"3",
                            },
                    @(SHOWLVIDEO) : @{
                            METHOD: @"setLocalVideoView:left:top:width:height:",
                            PARAMS: @"5",
                            },
                    @(SHOWRVIDEO) : @{
                            METHOD: @"setRemoteVideoView:left:top:width:height:",
                            PARAMS: @"5",
                            },
                    @(USERPROFILE) : @{
                            METHOD: @"setActivaCallUserName:image:",
                            PARAMS: @"2",
                            },
                    @(CHAT) : @{
                            METHOD: @"sendMessageTo:message:type:",
                            PARAMS: @"3",
                            },
                    @(OPENATTACHMENT) : @{
                            METHOD: @"openAttachmentWithURI:mimeType:",
                            PARAMS: @"2",
                            },
                    @(ACKNOWLEDGE) : @{
                            METHOD: @"ackEvents:",
                            PARAMS: @"1",
                            },
                    @(PULLHISTORY) : @{
                            METHOD: @"pullHistoryEvents:maxEventPull:timeStamp:moveForward:",
                            PARAMS: @"4",
                            },
                    @(PULLALLMESSAAGE) : @{
                            METHOD: @"pullAllConversationsWithMessages:timestamp:moveForward:",
                            PARAMS: @"3",
                            },
                    @(DOWNLOADMEDIA) : @{
                            METHOD: @"downloadMediaFromChat:",
                            PARAMS: @"1",
                            },
                    @(DOWNLOADMEDIATHUMB) : @{
                            METHOD: @"downloadMediaThumbnailFromChat:size:",
                            PARAMS: @"2",
                            },
                    @(CANCELMEDIA) : @{
                            METHOD: @"cancelMedia:",
                            PARAMS: @"1",
                            },
                    @(SCONTACT) : @{
                            METHOD: @"sendContact:caption:uri:type:",
                            PARAMS: @"4",
                            },
                    @(SCURRENTLOC) : @{
                            METHOD: @"sendCurrentLocation:caption:type:",
                            PARAMS: @"3",
                            },
                    @(SLOC) : @{
                            METHOD: @"sendLocation:caption:location:type:",
                            PARAMS: @"4",
                            },
                    @(SAUDIO) : @{
                            METHOD: @"sendAudio:caption:uri:type:",
                            PARAMS: @"4",
                            },
                    @(SVIDEO) : @{
                            METHOD: @"sendVideo:caption:uri:type:",
                            PARAMS: @"4",
                            },
                    @(SIMAGE) : @{
                            METHOD: @"sendImage:caption:uri:type:",
                            PARAMS: @"4",
                            },
                    @(SENDATTACHMENT) : @{
                            METHOD: @"showAttachementOptions:caption:type:",
                            PARAMS: @"3",
                            },
                    @(CREATEGROUP) : @{
                            METHOD: @"createGroupName:",
                            PARAMS: @"1",
                            },
                    @(GROUPBYID) : @{
                            METHOD: @"groupDetailsById:",
                            PARAMS: @"1",
                            },
                    @(UPGROUPNAME) : @{
                            METHOD: @"updateGroupbyID:name:",
                            PARAMS: @"2",
                            },
                    @(UPGROUPIMG) : @{
                            METHOD: @"updateGroupbyID:imagePath:",
                            PARAMS: @"2",
                            },
                    @(RMGROUPIMG) : @{
                            METHOD: @"removeGroupImageByID:",
                            PARAMS: @"1",
                            },
                    @(DOWNGROUPIMG) : @{
                            METHOD: @"downloadGroupImageByGroupID:",
                            PARAMS: @"1",
                            },
                    @(DOWNGROUPTHUMB) : @{
                            METHOD: @"downloadGroupImageThumbnailByGroupID:size:",
                            PARAMS: @"2",
                            },
                    @(MUTEGROUP) : @{
                            METHOD: @"muteGroupByID:",
                            PARAMS: @"1",
                            },
                    @(UNMUTEGROUP) : @{
                            METHOD: @"unmuteGroupByID:",
                            PARAMS: @"1",
                            },
                    @(DELGROUP) : @{
                            METHOD: @"destroyGroupByID:",
                            PARAMS: @"1",
                            },
                    @(LEAVEGROUP) : @{
                            METHOD: @"leaveGroupByID:",
                            PARAMS: @"1",
                            },
                    @(RMPARTICIPANTS) : @{
                            METHOD: @"removeParticipantsByID:participants",
                            PARAMS: @"2",
                            },
                    @(MUTEPARTICIPANTS) : @{
                            METHOD: @"muteParticipantsByID:participants:",
                            PARAMS: @"2",
                            },
                    @(UNMUTEPARTICIPANTS) : @{
                            METHOD: @"unmuteParticipantsByID:participants:",
                            PARAMS: @"2",
                            },
                    @(ADDPARTICIPANTS) : @{
                            METHOD: @"addParticipantsByID:participants:",
                            PARAMS: @"2",
                            },
                    @(LASTSEEN) : @{
                            METHOD: @"getPresenceInfoByUser:",
                            PARAMS: @"1",
                            },
                    @(STARTPRESENCE) : @{
                            METHOD: @"startWatchUserPresence:",
                            PARAMS: @"1",
                            },
                    @(STOPPRESENCE) : @{
                            METHOD: @"stopWatchUserPresence:",
                            PARAMS: @"1",
                            },
                    @(UPDATEPRESENCE) : @{
                            METHOD: @"updatePresenceStatus:",
                            PARAMS: @"1",
                            },
                   @(FILTER) : @{
                            METHOD: @"getFilteredDomainDirectoryContacts:searchString:",
                            PARAMS: @"2",
                            },
                    @(UPDATEDEVICEPROFILE) : @{
                            METHOD: @"updateDeviceProfile:deviceName:deviceFamily:",
                            PARAMS: @"3",
                            },
                    @(UPLOADMEDIA) : @{
                            METHOD: @"uploadMediaURI:",
                            PARAMS: @"1",
                            },
                    @(DOWNLOADCLOUD) : @{
                            METHOD: @"downloadCloudMedia:fileName:",
                            PARAMS: @"2",
                            },
                    @(CANCELCLOUDMEDIA) : @{
                            METHOD: @"cancelCloudMediaTransfer:fileName:",
                            PARAMS: @"2",
                            },
     
                        };
        
        
        self.presenceStatus = @{
                                    @(EKandyPresenceType_away) : @"Away",
                                    @(EKandyPresenceType_outToLunch) : @"Out To Lunch",
                                    @(EKandyPresenceType_onVacation) : @"On Vacation",
//                                    @(EKandyPresenceType_away) : @"Be Right Back",
                                    @(EKandyPresenceType_onThePhone) : @"On The Phone",
//                                    @(EKandyPresenceType_away) : @"Active",
                                    @(EKandyPresenceType_busy) : @"Busy",
//                                    @(EKandyPresenceType_onVacation) : @"Inactive",
//                                    @(EKandyPresenceType_away) : @"Idle",
                                    @(EKandyPresenceType_unknown) : @"Unknown"
                                };
        

    }
    return self;
}
- (void) ringIn {

    NSString *soundFilePath = [NSString stringWithFormat:@"%@/ringin.mp3",
                               [[NSBundle mainBundle] resourcePath]];
    NSURL *soundFileURL = [NSURL fileURLWithPath:soundFilePath];
    self.ringin = [[AVAudioPlayer alloc]initWithContentsOfURL:soundFileURL error:nil];
    [self.ringin setNumberOfLoops:100];
    NSError *error;
    
//    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback withOptions:AVAudioSessionCategoryOptionMixWithOthers error:nil];
    [[AVAudioSession sharedInstance] setActive:YES error:nil];
    [self.ringin setVolume:1.0];
    [[AVAudioSession sharedInstance] overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&error];
    [self.ringin play];
}

- (void) stopRingIn {
    if (self.ringin) {
        [self.ringin stop];
    }
}

- (void) ringOut {
    NSString *soundFilePath = [NSString stringWithFormat:@"%@/ringout.mp3",
                               [[NSBundle mainBundle] resourcePath]];
    NSURL *soundFileURL = [NSURL fileURLWithPath:soundFilePath];
    
    self.ringout = [[AVAudioPlayer alloc] initWithContentsOfURL:soundFileURL
                                                                   error:nil];
    self.ringout.numberOfLoops = -1; //Infinite
    
    [self.ringout play];
}

- (void) stopRingOut {
    if (self.ringout) {
        [self.ringout stop];
    }
}

+ (KandyRecord *) getRecipientKandyRecord:(NSString *)recipient {
    KandyRecord *kandyRecord = [[KandyRecord alloc] initWithURI:recipient];
    return kandyRecord;
}
+ (NSString *) saveImage:(UIImage *)image {
    NSData *imageData = UIImageJPEGRepresentation(image, 0.5f);
    [imageData writeToFile:[[KandyUtil sharedInstance] getSavePath:@"imageItem.jpeg"]  atomically:YES];
    return [[KandyUtil sharedInstance] getSavePath:@"imageItem.jpeg"];
}
+ (void) saveAudioVideo:(NSData *)data {
    [data writeToFile:[[KandyUtil sharedInstance] getSavePath:@"videoItem.MOV"]  atomically:YES];
}
- (NSString *) getSavePath:(NSString *)filename {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *fullPathToFile = [documentsDirectory stringByAppendingPathComponent:filename];
    return fullPathToFile;
}

+ (void) saveAPIKey:(NSString *)key secret:(NSString *)secret {
    [[NSUserDefaults standardUserDefaults] setObject:key forKey:kandy_api_key];
    [[NSUserDefaults standardUserDefaults] setObject:secret forKey:kandy_api_secret];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

+ (NSString *) getDomainAPIKey {
    return [[NSUserDefaults standardUserDefaults] objectForKey:kandy_api_key];
}

+ (NSString *) getDomainSecrect {
    return [[NSUserDefaults standardUserDefaults] objectForKey:kandy_api_secret];
}

+ (void) saveHostURL:(NSString *)url {
    [[NSUserDefaults standardUserDefaults] setObject:url forKey:kandy_host_url];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

+ (NSString *) getHostURL {
    return [[NSUserDefaults standardUserDefaults] objectForKey:kandy_host_url];
}

+ (NSDictionary *) dictionaryWithKandyGroup:(KandyGroup *)group {
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"yyyy-MM-dd hh:mm aaa"];
    NSString *dateString = [dateFormat stringFromDate:group.creationDate];
    NSDictionary *jsonObj = @{
                                 @"id": [KandyUtil dictionaryWithKandyRecord:group.groupId],
                                 @"name": group.name,
                                 @"creationDate": dateString,
                                 @"maxParticipantsNumber": @(group.maxParticipants),
                                 @"isGroupMuted": @(group.isMuted),
                                 @"selfParticipant": [KandyUtil parseParticipants:group.participants],
                                 @"participants": [KandyUtil parseParticipants:group.participants]
                             };

    return jsonObj;
}
+ (NSDictionary *) dictionaryWithKandyRecord:(KandyRecord *)record {
    NSDictionary *jsonObj = @{
                                @"uri" : record.uri,
                                @"type" : @(record.type),
                                @"domain": record.domain,
                                @"username": record.userName
                             };
    return jsonObj;
}

+ (KandyRecord *) recordWithGroupID:(NSString *)groupid {
    return [[KandyRecord alloc] initWithURI:groupid];
}

+ (NSArray *)parseParticipants:(NSArray *)participants {
    
    NSMutableArray * participantList = [[NSMutableArray alloc] init];
    for (KandyGroupParticipant *participant in participants) {
        NSMutableDictionary *jsonObj = [NSMutableDictionary dictionaryWithDictionary:[KandyUtil dictionaryWithKandyRecord:participant.kandyRecord]];
        [jsonObj setObject:@(participant.isAdmin) forKey:@"isAdmin" ];
        [jsonObj setObject:@(participant.isMuted) forKey:@"isMuted" ];
        [participantList addObject:jsonObj];
    }
    return participantList;
}

/**
 * Get {@link KandyChatMessage} from {@link UUID}.
 *
 * @param uuid The {@link UUID}.
 * @return The {@link KandyChatMessage}.
 */

+ (KandyChatMessage *) KandyMessageFromUUID:(NSString *)uuid {
    KandyRecord *record = [[KandyRecord alloc] initWithUsername:@"dummy@dummy.com" withDomain:@"dummy"];
    KandyChatMessage *message = [[KandyChatMessage alloc] initWithText:@"" recipient:record];
    [message updateUUID:uuid];
    return message;
}

+ (NSDictionary *) dictionaryWithTransferProgress:(KandyTransferProgress *)progress {
    NSDictionary *jsonObj = @{
                                 @"process": @(progress.transferProgressPercentage),
                                 @"state": @(progress.transferState),
                                 @"byteTransfer": @(progress.transferredSize),
                                 @"byteExpected": @(progress.expectedSize)
                             };
    return jsonObj;
}

+ (NSDictionary *) dictionaryFromKandyCall:(id<KandyCallProtocol>)kandyCall {
    NSDictionary *jsonObj = @{
                              @"callId": kandyCall.callId ,
                              @"callee": [KandyUtil dictionaryWithKandyRecord:kandyCall.remoteRecord] ,
                              @"via": @(kandyCall.audioRoute),
                              @"type": @(kandyCall.callType),
                              //@"cameraForVideo": @(),
                              @"isCallStartedWithVideo": @(kandyCall.isCallStartedWithVideo),
                              @"isIncomingCall": @(kandyCall.isIncomingCall),
                              @"isMute": @(kandyCall.isMute),
                              @"isOnHold": @(kandyCall.isOnHold),
                              @"isOtherParticipantOnHold": @(kandyCall.isOtherParticipantOnHold),
                              @"isReceivingVideo": @(kandyCall.isReceivingVideo),
                              @"isSendingVideo": @(kandyCall.isSendingVideo)
                              };
    return jsonObj;
}

+ (void) presentModalViewContriller:(id)viewcontroller {
    UIWindow *window = [[[UIApplication sharedApplication] delegate] window];
    [window.rootViewController presentViewController:viewcontroller animated:YES completion:nil];
}

+ (BOOL) validateInputParam:(NSArray *)params withRequiredInputs:(int)input {
    if (params && [params count] < input) {
        return NO;
    }
    return YES;
}

+ (NSString *)documentsDirectory
{
    NSString * documentsDirectory;
    documentsDirectory = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    return documentsDirectory;
}

#pragma mark - CLLocationManagerDelegate

- (void) getCurrentLocationUsingBlcok:(successResponse)success andFailure:(failureResponse)failure {
    self.success = success;
    self.failure = failure;
    
    CLLocationManager *locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    locationManager.pausesLocationUpdatesAutomatically = NO;
    [locationManager startUpdatingLocation];
};

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"Localtion Manager Failed Error :  %@", error.description);
    self.failure(error);
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    NSLog(@"didUpdateToLocation: %@", newLocation);
    CLLocation *location = [[CLLocation alloc] initWithLatitude:newLocation.coordinate.latitude longitude:newLocation.coordinate.longitude];
    self.success(location);
    [manager stopUpdatingLocation];
    manager.delegate = nil;
}

- (void) showAttachmentOptionsUsingBlock:(successAttachmentOPtions)success {
    self.successOptions = success;
    UIActionSheet *popup = [[UIActionSheet alloc] initWithTitle:@"Send attachment:" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:
                            @"Send image",
                            @"Send video",
                            @"Send audio",
                            @"Send Current location",
                            @"Send contact",
                            nil];
    dispatch_async(dispatch_get_main_queue(), ^{
        [popup showInView:[UIApplication sharedApplication].keyWindow];
    });
}

#pragma UIActionSheet Delegate

- (void)actionSheet:(UIActionSheet *)popup clickedButtonAtIndex:(NSInteger)buttonIndex {
    self.successOptions(buttonIndex);
    [popup dismissWithClickedButtonIndex:buttonIndex animated:YES];
}

- (NSArray *) enumerateContactDetails:(NSArray *)kandyContacts {
    NSMutableArray *contacts = [[NSMutableArray alloc] init];
    for (id <KandyContactProtocol> kandyContact in kandyContacts) {
        NSMutableDictionary *deviceContacts = [[NSMutableDictionary alloc] init];
        [deviceContacts setValue:kandyContact.displayName forKey:@"displayName"];
        NSString *uri = kandyContact.serverIdentifier.uri;
        if (uri) {
            [deviceContacts setValue:uri forKey:@"serverIdentifier"];
        }
        for (id <KandyEmailContactRecordProtocol> kandyEmailContactRecord in kandyContact.emails) {
            NSDictionary *deviceEmailContacts = @{ @"address": kandyEmailContactRecord.email,
                                                   @"type": @(kandyEmailContactRecord.valueType)
                                                   };
            [deviceContacts setValue:deviceEmailContacts forKey:@"emails"];
        }
        for (id <KandyPhoneContactRecordProtocol> kandyPhoneContactRecord in kandyContact.phones) {
            NSDictionary *devicePhoneContacts =  @{ @"number" : kandyPhoneContactRecord.phone,
                                                    @"type": @(kandyPhoneContactRecord.valueType)
                                                    };
            [deviceContacts setValue:devicePhoneContacts forKey:@"phones"];
        }
        [contacts addObject:deviceContacts];
    }
    return contacts;
}
@end
