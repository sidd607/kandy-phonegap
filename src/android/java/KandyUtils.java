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

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import com.genband.kandy.api.services.billing.IKandyBillingPackage;
import com.genband.kandy.api.services.billing.IKandyBillingPackageProperty;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

/**
 * The common utils
 *
 * @author kodeplusdev
 * @version 1.3.4
 */
public class KandyUtils {

    private static String[] mSupportedFiles = new String[]{".png", ".pdf"};

    private static Context _context;

    public static void initialize(Context context) {
        _context = context;
    }

    /**
     * Get identifier of the resource
     *
     * @param name The name of the resource
     * @param type The type of the resource
     * @return The identifier of the resource
     */
    public static int getResource(String name, String type) {
        int res;
        String packageName = _context.getPackageName();
        res = _context.getResources().getIdentifier(name, type, packageName);
        return res;
    }

    /**
     * Get string resource from the identifier
     *
     * @param name The name of the string resource
     * @return The string value of the identifier string resource
     */
    public static String getString(String name) {
        String str;
        int resId = getResource(name, "string");
        str = _context.getString(resId);
        return str;
    }

    /**
     * Get identifier of the layout
     *
     * @param name The name of the layout resource
     * @return The identifier value of the layout resource
     */
    public static int getLayout(String name) {
        return getResource(name, "layout");
    }

    /**
     * Get identifier of the id
     *
     * @param name The name of the Id resource
     * @return The identifier value of the id name
     */
    public static int getId(String name) {
        return getResource(name, "id");
    }

    /**
     * Send a {@link PluginResult} to {@link CallbackContext} and also keep the callback.
     *
     * @param ctx The {@link CallbackContext} to use
     * @param obj The {@link JSONObject} value parameters
     */
    public static void sendPluginResultAndKeepCallback(CallbackContext ctx, JSONObject obj) {
        if (ctx != null && obj != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            ctx.sendPluginResult(result);
        }
    }

    /**
     * Send a {@code PluginResult.Status.OK} to {@link CallbackContext} and also keep the callback.
     *
     * @param ctx The {@link CallbackContext} to use
     */
    public static void sendPluginResultAndKeepCallback(CallbackContext ctx) {
        if (ctx != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            ctx.sendPluginResult(result);
        }
    }

    /**
     * Get {@link JSONObject} from {@link KandyRecord}.
     *
     * @param record The {@link KandyRecord} to use.
     * @return The {@link JSONObject}.
     */
    public static JSONObject getJsonObjectFromKandyRecord(KandyRecord record) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("uri", record.getUri());
            obj.put("type", record.getType().toString());
            obj.put("domain", record.getDomain());
            obj.put("username", record.getUserName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Get {@link JSONObject} from {@link KandyGroup}
     *
     * @param group The {@link KandyGroup} to use.
     * @return The {@link JSONObject}
     */
    public static JSONObject getJsonObjectFromKandyGroup(KandyGroup group) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("id", getJsonObjectFromKandyRecord(group.getGroupId()));
            obj.put("name", group.getGroupName());
            obj.put("creationDate", group.getCreationDate().getTime());
            obj.put("maxParticipantsNumber", group.getMaxParticipantsNumber());
            obj.put("selfParticipant", getJsonObjectFromKandyGroupParticipant(group.getSelfParticipant()));
            obj.put("isGroupMuted", group.isGroupMuted());
            JSONArray list = new JSONArray();
            if (group.getGroupParticipants() != null) {
                for (KandyGroupParticipant participant : group.getGroupParticipants())
                    list.put(getJsonObjectFromKandyGroupParticipant(participant));
            }
            obj.put("participants", list);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Get {@link JSONObject} from {@link KandyGroupParticipant}
     *
     * @param participant The {@link KandyGroupParticipant} to use.
     * @return The {@link JSONObject}
     */
    public static JSONObject getJsonObjectFromKandyGroupParticipant(KandyGroupParticipant participant) {
        JSONObject obj = getJsonObjectFromKandyRecord(participant.getParticipant());

        try {
            obj.put("isAdmin", participant.isAdmin());
            obj.put("isMuted", participant.isMuted());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }


    /**
     * Get {@link Location} from {@link JSONObject}.
     *
     * @param obj The {@link JSONObject} to use.
     * @return The {@link Location}
     */
    public static Location getLocationFromJson(JSONObject obj) {
        Location location = new Location("Kandy");

        location.setAccuracy(getFloatValueFromJson(obj, "accuracy", 0.0f));
        location.setAltitude(getDoubleValueFromJson(obj, "altitude", 0.0));
        location.setBearing(getFloatValueFromJson(obj, "bearing", 0.0f));
        location.setLatitude(getDoubleValueFromJson(obj, "latitude", 0.0));
        location.setLongitude(getDoubleValueFromJson(obj, "longitude", 0.0));
        location.setProvider(getStringValueFromJson(obj, "provider", "Kandy"));
        location.setTime(getLongValueFromJson(obj, "time", 0));
        location.setSpeed(getFloatValueFromJson(obj, "speed", 0.0f));

        return location;
    }

    /**
     * Get the value of the key from {@link JSONObject}.
     *
     * @param obj The {@link JSONObject} to use.
     * @param key The key to use.
     * @param def The default value if not exists.
     * @return The {@link Object} value.
     */

    public static boolean getBoolValueFromJson(JSONObject obj, String key, boolean def) {
        try {
            return obj.getBoolean(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static String getStringValueFromJson(JSONObject obj, String key, String def) {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static int getIntValueFromJson(JSONObject obj, String key, int def) {
        try {
            return obj.getInt(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static long getLongValueFromJson(JSONObject obj, String key, long def) {
        try {
            return obj.getLong(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    public static float getFloatValueFromJson(JSONObject obj, String key, float def) {
        return (float) getDoubleValueFromJson(obj, key, def);
    }

    public static double getDoubleValueFromJson(JSONObject obj, String key, double def) {
        try {
            return obj.getDouble(key);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return def;
    }

    /**
     * Get {@link JSONObject} from {@link IKandyCall}.
     *
     * @param call The {@link IKandyCall} to use.
     * @return The {@link JSONObject}
     */
    public static JSONObject getJsonObjectFromKandyCall(IKandyCall call) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("callId", call.getCallId());
            obj.put("callee", getJsonObjectFromKandyRecord(call.getCallee()));
            obj.put("via", call.getVia());
            obj.put("type", call.getCallType().name());
            obj.put("state", call.getCallState().name());
//            obj.put("startTime", call.getStartTime());
//            obj.put("endTime", call.getEndTime());
//            obj.put("duration", call.getDurationString());
            obj.put("cameraForVideo", call.getCameraForVideo().name());
            obj.put("isCallStartedWithVideo", call.isCallStartedWithVideo());
            obj.put("isIncomingCall", call.isIncomingCall());
            obj.put("isMute", call.isMute());
            obj.put("isOnHold", call.isOnHold());
            obj.put("isOtherParticipantOnHold", call.isOtherParticipantOnHold());
            obj.put("isReceivingVideo", call.isReceivingVideo());
            obj.put("isSendingVideo", call.isSendingVideo());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Get {@link JSONObject} from {@link IKandyBillingPackage}
     *
     * @param billingPackage The {@link IKandyBillingPackage} to use.
     * @return The {@link JSONObject}
     */
    public static JSONObject getJsonObjectFromKandyPackagesCredit(IKandyBillingPackage billingPackage) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("currency", billingPackage.getCurrency());
            obj.put("balance", billingPackage.getBalance());
            obj.put("exiparyDate", billingPackage.getExiparyDate().toString());
            obj.put("packageId", billingPackage.getPackageId());
            obj.put("remainingTime", billingPackage.getRemainingTime());
            obj.put("startDate", billingPackage.getStartDate());
            obj.put("packageName", billingPackage.getPackageName());

            JSONArray properties = new JSONArray();
            if (billingPackage.getProperties().size() > 0) {
                ArrayList<IKandyBillingPackageProperty> billingPackageProperties = billingPackage.getProperties();
                for (IKandyBillingPackageProperty p : billingPackageProperties)
                    properties.put(getJsonObjectFromKandyBillingPackageProperty(p));
            }

            obj.put("properties", properties);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Get {@link JSONObject} from {@link IKandyBillingPackageProperty}
     *
     * @param property The {@link IKandyBillingPackageProperty} to use.
     * @return The {@link JSONObject}
     */
    private static JSONObject getJsonObjectFromKandyBillingPackageProperty(IKandyBillingPackageProperty property) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("packageName", property.getPackageName());
            obj.put("quotaUnits", property.getQuotaUnits());
            obj.put("remainingQuota", property.getRemainingQuota());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static void copyAssets(Context context, File targetDir) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {

            if (isSupported(filename)) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(filename);

                    File outFile = new File(targetDir, filename);

                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e("tag", "Failed to close inputstream: " + filename, e);
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            Log.e("tag", "Failed to close outputstream: " + filename, e);
                        }
                    }
                }
            }
        }
    }

    public static File getFilesDirectory(String name) {
        File file = new File(Environment.getExternalStorageDirectory(), name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static boolean isSupported(String fileName) {
        for (int i = 0; i < mSupportedFiles.length; i++) {
            if (fileName.endsWith(mSupportedFiles[i])) {
                return true;
            }
        }
        return false;
    }

    public static void clearDirectory(File dir) {
        String[] list = dir.list();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File file = new File(dir, list[i]);
                file.delete();
            }
        }
    }
}
