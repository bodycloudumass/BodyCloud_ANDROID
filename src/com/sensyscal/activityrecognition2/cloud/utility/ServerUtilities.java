package com.sensyscal.activityrecognition2.cloud.utility;
/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidgetHandler;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {
	
	private static Message msg = Message.obtain();
	private static ActivityRecognitionWidgetHandler ARWH;
    private static final int MAX_ATTEMPTS = 5;
    private static int BACKOFF_MILLI_SECONDS = 2000;
    private static int BACKOFF_MAX_ATTEMPTS_MILLI_SECONDS = 60000 * 1;

    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    static boolean register(final Context context, final String regId) {
        Log.i(ActivityRecognitionApp.TAG, "registering device (regId = " + regId + ")");
        String serverUrl = ActivityRecognitionApp.SERVER_URL + "register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			//MainActivity.getInstance().handler.sendEmptyMessage(MainActivity.ATT);
            Log.d(ActivityRecognitionApp.TAG, "Attempt #" + i + " to register");
            try {
                post(serverUrl, params);
                GCMRegistrar.setRegisteredOnServer(context, true);
            	
            	//ActivityRecognitionApp.getInstance().setAppRegistrationStatus(true);
            	
            	
                return true;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(ActivityRecognitionApp.TAG, "Failed to register on attempt " + i, e);
                if (i == MAX_ATTEMPTS) {
                	i = 0;
                    Log.d(ActivityRecognitionApp.TAG, "Sleeping for " + BACKOFF_MAX_ATTEMPTS_MILLI_SECONDS + " ms before retry");
                    try {
						Thread.sleep(BACKOFF_MAX_ATTEMPTS_MILLI_SECONDS);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    BACKOFF_MAX_ATTEMPTS_MILLI_SECONDS *= 2;
                }
                try {
                    Log.d(ActivityRecognitionApp.TAG, "Sleeping for " + BACKOFF_MILLI_SECONDS + " ms before retry");
                    Thread.sleep(BACKOFF_MILLI_SECONDS);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(ActivityRecognitionApp.TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                BACKOFF_MILLI_SECONDS *= 2;
            }
        }
        msg.obj=ActivityRecognitionWidgetHandler.MSG_REC;
		ARWH.sendMessage(msg);
    	//MainActivity.getInstance().handler.sendEmptyMessage(MainActivityHandler.REG_SERVER_ERROR);
        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
    	/*
        Log.i(ActivityRecognitionApp.TAG, "unregistering device (regId = " + regId + ")");
        String serverUrl = CommonUtilities.SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
        }
        */
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(ActivityRecognitionApp.TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
            
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
}
