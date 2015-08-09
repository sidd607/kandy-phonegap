package com.kandy.phonegap;

import android.content.Context;
import android.location.Location;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The common utils
 *
 * @author kodeplusdev
 * @version 1.2.0
 */
public class KandyUtils {

    /* KANDY UTILS SINGLETON */

    private static KandyUtils _instance = new KandyUtils();

    private KandyUtils() {
    }

    /**
     * Get the instance of the {@link KandyUtils}
     *
     * @param context The {@link Context} to use
     * @return The instance of the {@link KandyUtils}
     */
    public static KandyUtils getInstance(Context context) {
        if (context != null) {
            _instance.initialize(context);
        }
        return _instance;
    }

    private Context _context;

    private void initialize(Context context) {
        _context = context;
    }

    /**
     * Get identifier of the resource
     *
     * @param name The name of the resource
     * @param type The type of the resource
     * @return The identifier of the resource
     */
    public int getResource(String name, String type) {
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
    public String getString(String name) {
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
    public int getLayout(String name) {
        return getResource(name, "layout");
    }

    /**
     * Get identifier of the id
     *
     * @param name The name of the Id resource
     * @return The identifier value of the id name
     */
    public int getId(String name) {
        return getResource(name, "id");
    }

    /**
     * Send a {@link PluginResult} to {@link CallbackContext} and also keep the callback.
     *
     * @param ctx The {@link CallbackContext} to use
     * @param obj The {@link JSONObject} value parameters
     */
    public void sendPluginResultAndKeepCallback(CallbackContext ctx, JSONObject obj) {
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
    public void sendPluginResultAndKeepCallback(CallbackContext ctx) {
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
    public JSONObject getJsonObjectFromKandyRecord(KandyRecord record) {
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
    public JSONObject getJsonObjectFromKandyGroup(KandyGroup group) {
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
    public JSONObject getJsonObjectFromKandyGroupParticipant(KandyGroupParticipant participant) {
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
    public Location getLocationFromJson(JSONObject obj) {
        Location location = new Location("Kandy");

        location.setAccuracy((float) getObjectValueFromJson(obj, "accuracy", 0.0f));
        location.setAltitude((double) getObjectValueFromJson(obj, "altitude", 0.0));
        location.setBearing((float) getObjectValueFromJson(obj, "bearing", 0.0f));
        location.setLatitude((double) getObjectValueFromJson(obj, "latitude", 0.0));
        location.setLongitude((double) getObjectValueFromJson(obj, "longitude", 0.0));
        location.setProvider((String) getObjectValueFromJson(obj, "provider", "Kandy"));
        location.setTime((long) getObjectValueFromJson(obj, "time", 0));
        location.setSpeed((float) getObjectValueFromJson(obj, "speed", 0.0f));

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
    public Object getObjectValueFromJson(JSONObject obj, String key, Object def) {
        try {
            return obj.get(key);
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
    public JSONObject getJsonObjectFromKandyCall(IKandyCall call) {
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
//            obj.put("cameraForVideo", call.getCameraForVideo().name());
//            obj.put("isCallStartedWithVideo", call.isCallStartedWithVideo());
//            obj.put("isIncomingCall", call.isIncomingCall());
//            obj.put("isMute", call.isMute());
//            obj.put("isOnHold", call.isOnHold());
//            obj.put("isOtherParticipantOnHold", call.isOtherParticipantOnHold());
//            obj.put("isReceivingVideo", call.isReceivingVideo());
//            obj.put("isSendingVideo", call.isSendingVideo());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
