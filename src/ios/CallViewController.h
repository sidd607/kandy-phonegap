//
//  CallViewController.h
//
//  Created by Genband Ltd on 04/13/15.
//  Copyright (c) 2014 Genband Ltd. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <KandySDK/KandySDK.h>


@protocol ActiveCallOptionDelegate <NSObject>
-(void)hangupCall:(NSString *)callid;
- (void)muteCall:(NSString *)callid mute:(NSString *)mute;
- (void)holdCall:(NSString *)callid hold:(NSString *)hold;
- (void)enableVideoCall:(NSString *)callid video:(NSString *)videoOn;
- (void) switchCamera:(NSString *)callid;
- (void) speakerOnOff:(NSString *)callid;
@end


@interface CallViewController : UIViewController
@property (nonatomic, strong) id <KandyCallProtocol> kandyCall;
@property (assign) id<ActiveCallOptionDelegate> delegate;
- (void) setUserName:(NSString *)name andUserImageURL:(NSString *)url;
@end
