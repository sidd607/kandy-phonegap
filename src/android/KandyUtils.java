/*
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
*/

package com.kandy.phonegap;

import android.content.Context;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

/**
 * The common utils
 *
 * @author kodeplusdev
 * @version 1.0.0
 */
public class KandyUtils {

    /* KANDY UTILS SINGLETON */

    private static KandyUtils _instance = new KandyUtils();

    private KandyUtils(){}

    /**
     * Get the instance of the {@link KandyUtils}
     *
     * @param context The {@link Context} to use
     * @return The instance of the {@link KandyUtils}
     */
    public static KandyUtils getInstance(Context context){
        if (context != null){
            _instance.initialize(context);
        }
        return _instance;
    }

    private Context _context;

    private void initialize(Context context){
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
    public int getLayout(String name){
        return getResource(name, "layout");
    }

    /**
     * Get identifier of the id
     *
     * @param name The name of the Id resource
     * @return The identifier value of the id name
     */
    public int getId(String name){
        return getResource(name, "id");
    }

    /**
     * Send a {@link PluginResult} to {@link CallbackContext} and also keep the callback.
     *
     * @param ctx The {@link CallbackContext} to use
     * @param obj The {@link JSONObject} value parameters
     */
    public void sendPluginResultAndKeepCallback(CallbackContext ctx, JSONObject obj) {
        if (ctx != null && obj != null){
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
        if (ctx != null){
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            ctx.sendPluginResult(result);
        }
    }
}
