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

import android.app.Activity;
import android.content.Context;

/**
 * The common utils
 *
 * @author Kodeplusdev
 * @version 0.0.2
 */
public abstract class KandyUtils {
    /**
     * Get identifier of the resource
     *
     * @param context
     * @param name
     * @param type
     * @return
     */
    public static int getResource(Context context, String name, String type) {
        int res = -1;
        String packageName = context.getPackageName();
        res = context.getResources().getIdentifier(name, type, packageName);
        return res;
    }

    /**
     * Get string resource from the identifier
     *
     * @param context
     * @param name
     * @return
     */
    public static String getString(Context context, String name) {
        String str = "";
        int resId = getResource(context, name, "string");
        str = context.getString(resId);
        return str;
    }

    /**
     * Get identifier of the layout
     *
     * @param context
     * @param name
     * @return
     */
    public static int getLayout(Context context, String name){
        return getResource(context, name, "layout");
    }

    /**
     * Get identifier of the id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getId(Context context, String name){
        return getResource(context, name, "id");
    }
}
