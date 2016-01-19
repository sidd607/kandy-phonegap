/*******************************************************************************
 * Copyright 2015 © GENBAND US LLC, All Rights Reserved
 * <p/>
 * This software embodies materials and concepts which are
 * proprietary to GENBAND and/or its licensors and is made
 * available to you for use solely in association with GENBAND
 * products or services which must be obtained under a separate
 * agreement between you and GENBAND or an authorized GENBAND
 * distributor or reseller.
 * <p/>
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * AND/OR ITS LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * THE WARRANTY AND LIMITATION OF LIABILITY CONTAINED IN THIS
 * AGREEMENT ARE FUNDAMENTAL PARTS OF THE BASIS OF GENBAND’S BARGAIN
 * HEREUNDER, AND YOU ACKNOWLEDGE THAT GENBAND WOULD NOT BE ABLE TO
 * PROVIDE THE PRODUCT TO YOU ABSENT SUCH LIMITATIONS.  IN THOSE
 * STATES AND JURISDICTIONS THAT DO NOT ALLOW CERTAIN LIMITATIONS OF
 * LIABILITY, GENBAND’S LIABILITY SHALL BE LIMITED TO THE GREATEST
 * EXTENT PERMITTED UNDER APPLICABLE LAW.
 * <p/>
 * Restricted Rights legend:
 * Use, duplication, or disclosure by the U.S. Government is
 * subject to restrictions set forth in subdivision (c)(1) of
 * FAR 52.227-19 or in subdivision (c)(1)(ii) of DFAR 252.227-7013.
 *******************************************************************************/
package com.kandy.phonegap.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.google.android.gcm.GCMBaseIntentService;
import com.kandy.phonegap.KandyConstant;

/**
 * @author kodeplusdev
 * @version 1.3.4
 */
public class KandyPushService extends GCMBaseIntentService {
    private static final String TAG = KandyPushService.class.getSimpleName();

    public KandyPushService() {
        super(KandyConstant.GCM_PROJECT_ID);
        Log.d(TAG, "KandyPushService: ");
    }

    protected KandyPushService(String senderId) {
        super(senderId);
        Log.d(TAG, "KandyPushService: senderId: " + senderId);
    }

    @Override
    protected void onError(Context context, String errorId) {
        Log.d(TAG, "onError: context: " + context + " errorId: " + errorId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Kandy.getServices().getPushService().handlePushNotification(context, intent, new KandyResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {

                Log.d(TAG, "onMessage: handlePushNotification: not KANDY related push message, ignored, responseCode: " + responseCode + " error: " + err);
            }

            @Override
            public void onRequestSucceded() {
                Log.d(TAG, "onMessage: handlePushNotification: Handled by KANDY");
            }
        });
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.d(TAG, "onRegistered: context: " + context + " registrationId: " + registrationId);

    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(TAG, "onUnregistered: context: " + context + " registrationId: " + registrationId);

    }

}
