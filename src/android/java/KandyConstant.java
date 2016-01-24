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
package com.kandy.phonegap;

/**
 * The Kandy constant to use.
 *
 * @author kodeplusdev
 * @version 1.3.4
 */
public abstract class KandyConstant {

    public static final String API_KEY_PREFS_KEY = "api_key";
    public static final String API_SECRET_PREFS_KEY = "api_secret";

    public static final String KANDY_HOST_PREFS_KEY = "kandy_host";

    public static final String GCM_PROJECT_ID = "876416103603";

    public static final String PREF_KEY_PATH = "download_path_preference";
    public static final String PREF_KEY_CUSTOM_PATH = "download_custom_path_preferences";
    public static final String PREF_KEY_MAX_SIZE = "media_size_picker_preference";
    public static final String PREF_KEY_POLICY = "download_policy_preference";
    public static final String PREF_KEY_THUMB_SIZE = "auto_download_thumbnail_size_preference";

    public final static String LOCAL_STORAGE = "Kandy//Local storage";

    public static final int CONTACT_PICKER_RESULT = 1001;
    public static final int IMAGE_PICKER_RESULT = 1002;
    public static final int VIDEO_PICKER_RESULT = 1003;
    public static final int AUDIO_PICKER_RESULT = 1004;
    public static final int FILE_PICKER_RESULT = 1005;
}
