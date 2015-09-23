//
//  CallViewController.m
//
//  Created by Genband Ltd on 04/13/15.
//  Copyright (c) 2014 Genband Ltd. All rights reserved.
//

#import "CallViewController.h"

@interface CallViewController () <KandyCallServiceNotificationDelegate, UIActionSheetDelegate>
@property (weak, nonatomic) IBOutlet UILabel *lblCallee;
@property (weak, nonatomic) IBOutlet UIView *viewRemoteVideo;
@property (weak, nonatomic) IBOutlet UIView *viewLocalVideo;
@property (weak, nonatomic) IBOutlet UILabel *lblCallState;
@property (weak, nonatomic) IBOutlet UIImageView *userImage;
@end

@implementation CallViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = @"Active Call View";
    UIBarButtonItem * btnClose = [[UIBarButtonItem alloc]
                                  initWithTitle:@"Close" style:UIBarButtonItemStyleBordered target:self action:@selector(didTapClose:)];
    self.navigationItem.leftBarButtonItem = btnClose;

    [[Kandy sharedInstance].services.call registerNotifications:self];
}
-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self setupGui];
}
-(void)viewDidDisappear:(BOOL)animated{
    [[Kandy sharedInstance].services.call unregisterNotifications:self];
    [super viewDidDisappear:animated];
}

#pragma mark - Public

-(void)refresh{
    [self setupGui];
}

#pragma mark - Using Kandy SDK

- (void) setUserName:(NSString *)name andUserImageURL:(NSString *)url {

    NSString * strCalleeTitle;
    if (self.kandyCall.isIncomingCall) {
        strCalleeTitle = @"Caller :";
    } else {
        strCalleeTitle = @"Destination :";
    }
    self.lblCallee.text = [NSString stringWithFormat:@"%@ %@", strCalleeTitle, (name ? name :self.kandyCall.callee.uri)];
    
    if (url) {
        self.userImage.image = [UIImage imageNamed:url];
    }
}


-(void)setupGui{
    [self setUserName:nil andUserImageURL:nil];
    self.kandyCall.remoteVideoView = self.viewRemoteVideo;
    self.kandyCall.localVideoView = self.viewLocalVideo;
}

-(void)hangupCall{
    [self.delegate hangupCall:self.kandyCall.callee.uri];
    [self dismissViewControllerAnimated:YES completion:^{}];
    
//    [self.kandyCall hangupWithResponseCallback:^(NSError *error) {
//        if (error) {
//            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error"
//                                                            message:error.localizedDescription
//                                                           delegate:self
//                                                  cancelButtonTitle:@"OK"
//                                                  otherButtonTitles:nil];
//            [alert show];
//        }
//
//    }];
}

- (void)didTapClose:(id)sender {
    if (self.kandyCall)
    {
        [self hangupCall];
    }
    [self dismissViewControllerAnimated:YES completion:^{}];
}

#pragma mark - IBActions

- (IBAction)didTapHangup:(id)sender {
    [self hangupCall];
}

- (IBAction)didTapCallOPtions:(id)sender {
    UIActionSheet *calloption = [[UIActionSheet alloc] initWithTitle:@"Active Call Options" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Switch Camera", @"Mute/Unmute", @"Speaker", @"Hold/Unhold", @"Video On/Off", nil];
    [calloption showInView:[UIApplication sharedApplication].keyWindow];
}

- (void) hold {
    if (self.kandyCall == nil) {
        NSLog(@"No active call. Please try again...");
        return;
    }
    [self.delegate holdCall:self.kandyCall.callee.uri hold:[NSString stringWithFormat:@"%d",!self.kandyCall.isOnHold]];
    
//    if (self.kandyCall.isOnHold) {
//        [self.kandyCall unHoldWithResponseCallback:^(NSError *error) {
//            
//            if (error) {
//                NSLog(@"Error %@", error);
//            }
//            
//        }];
//    }
//    else {
//        [self.kandyCall holdWithResponseCallback:^(NSError *error) {
//            if (error) {
//                NSLog(@"Error %@", error);
//            }
//        }];
//    }

}

- (void) speaker {
    if (self.kandyCall == nil) {
        NSLog(@"No active call. Please try again...");
        return;
    }
    [self.delegate speakerOnOff:self.kandyCall.callee.uri];
    
//    if (self.kandyCall.audioRoute == EKandyCallAudioRoute_speaker) {
//        [self.kandyCall changeAudioRoute:EKandyCallAudioRoute_speaker withResponseCallback:^(NSError *error){
//            if (error) {
//                NSLog(@"Error %@", error);
//            }
//        }];
//    } else {
//        [self.kandyCall changeAudioRoute:EKandyCallAudioRoute_speaker withResponseCallback:^(NSError *error){
//            if (error) {
//            }
//        }];
//    }
}

- (void) enableVideo {
    if (self.kandyCall == nil) {
        NSLog(@"No active call. Please try again...");
        return;
    }
    [self.delegate enableVideoCall:self.kandyCall.callee.uri video:[NSString stringWithFormat:@"%d",!self.kandyCall.isSendingVideo]];

//    if (self.kandyCall.isSendingVideo) {
//        [self.kandyCall stopVideoSharingWithResponseCallback:^(NSError *error) {
//            if (error) {
//                NSLog(@"Error %@", error);
//            }
//        }];
//    }
//    else {
//        [self.kandyCall startVideoSharingWithResponseCallback:^(NSError *error) {
//            if (error) {
//                NSLog(@"Error %@", error);
//            }
//        }];
//    }
}

- (void) doMute {
    
    if (self.kandyCall == nil) {
        NSLog(@"No active call. Please try again...");
        return;
    }
    [self.delegate muteCall:self.kandyCall.callee.uri mute:[NSString stringWithFormat:@"%d",!self.kandyCall.isMute]];

//    if (self.kandyCall.isMute) {
//        [self.kandyCall unmuteWithResponseCallback:^(NSError *error) {
//        }];
//    }
//    else {
//        [self.kandyCall muteWithResponseCallback:^(NSError *error) {
//        }];
//    }
}

- (void)switchCamera {
    if (self.kandyCall == nil) {
        NSLog(@"No active call. Please try again...");
        return;
    }
    [self.delegate switchCamera:self.kandyCall.callee.uri];
    
//    [self.kandyCall switchCameraWithResponseCallback:^(NSError *error) {
//        if (error) {
//        }
//    }];
}

#pragma mark - UIActionSheetDelegate
- (void)actionSheet:(UIActionSheet *)popup clickedButtonAtIndex:(NSInteger)buttonIndex {
    
     switch (buttonIndex) {
         case 0: {
             [self switchCamera];
            break;
         }
         case 1: {
             [self doMute];
            break;
         }
         case 2: {
             [self speaker];
            break;
         }
         case 3: {
             [self hold];
            break;
         }
         case 4: {
             [self enableVideo];
             break;
         }
         default:
            break;
            
    }
    [popup dismissWithClickedButtonIndex:buttonIndex animated:YES];
}

#pragma mark - KandyCallServiceNotificationDelegate

-(void) gotIncomingCall:(id<KandyIncomingCallProtocol>)call{
}

-(void) gotMissedCall:(id<KandyCallProtocol>)call{
}

-(void) stateChanged:(EKandyCallState)callState forCall:(id<KandyCallProtocol>)call{
    
    if (callState == EKandyCallState_terminated) {
        [self dismissViewControllerAnimated:YES completion:nil];
        /*KandyCallActivityRecord * kandyCallActivityRecord = [call createActivityRecord];
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Call Information"
                                                        message:kandyCallActivityRecord.description
                                                       delegate:self
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
         [alert show];
         */
    }
}

-(void) participantsChanged:(NSArray*)participants forCall:(id<KandyCallProtocol>)call{
}
-(void) videoStateChangedForCall:(id<KandyCallProtocol>)call{
}
-(void) audioRouteChanged:(EKandyCallAudioRoute)audioRoute forCall:(id<KandyCallProtocol>)call{
}
-(void) videoCallImageOrientationChanged:(EKandyVideoCallImageOrientation)newImageOrientation forCall:(id<KandyCallProtocol>)call{
}
-(void) GSMCallIncoming{
    NSLog(@"************************* Incoming GSM *************************");
}

-(void) GSMCallDialing{
}
-(void) GSMCallConnected{
    NSLog(@"************************* Connected GSM *************************");
}
-(void) GSMCallDisconnected{
}

#pragma mark - GUI

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{

}

#pragma mark - CallOptionsViewControllerDelegate

-(void)callOptionsDidClose{
}

#pragma mark - UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if ([alertView.title isEqualToString:@"Call Information"]) {
        [self.navigationController popViewControllerAnimated:YES];   
    }
}

@end
