package com.gae.scaffolder.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Application;
import android.content.res.Resources;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Iterator;

public class FCMPlugin extends CordovaPlugin {

	private static final String TAG = "FCMPlugin";

	public static CordovaWebView gWebView;
	public static String notificationCallBack = "FCMPlugin.onNotificationReceived";
	public static String pushSuccessCallBack = "FCMPlugin.onPushSuccess";
	public static String pushErrorCallBack = "FCMPlugin.onPushError";
	public static Boolean notificationCallBackReady = false;
	public static Boolean pushSuccessCallBackReady = false;
	public static Boolean pushErrorCallBackReady = false;

	public static String senderId;
	public static Map<String, Object> lastPush = null;

	public static CallbackContext onReceivedContext;
	public static CallbackContext onTokenRefreshContext;

	public FCMPlugin() {}

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		gWebView = webView;
		Log.d(TAG, "==> FCMPlugin initialize");
		FirebaseMessaging.getInstance().subscribeToTopic("android");
		FirebaseMessaging.getInstance().subscribeToTopic("all");

		Application app = cordova.getActivity().getApplication();
		int id = app.getResources().getIdentifier("gcm_defaultSenderId", "string", app.getPackageName());

		senderId = cordova.getActivity().getString(id);
	}

	public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

		Log.d(TAG,"==> FCMPlugin execute: "+ action);

		try{
			// READY //
			if (action.equals("ready")) {
				//
				callbackContext.success();
			}
			// GET TOKEN //
			else if (action.equals("getToken")) {
				cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						try{
							String token = FirebaseInstanceId.getInstance().getToken();
							callbackContext.success( FirebaseInstanceId.getInstance().getToken() );
							Log.d(TAG,"\tToken: "+ token);
						}catch(Exception e){
							Log.d(TAG,"\tError retrieving token");
						}
					}
				});
			}
			// NOTIFICATION CALLBACK REGISTER //
			else if (action.equals("registerNotification")) {
				notificationCallBackReady = true;
				cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						if(lastPush != null) FCMPlugin.sendNotification( lastPush );
						lastPush = null;
						// FCMPlugin.handleUnsent(notificationCallBack);
						callbackContext.success();
					}
				});
			}
			// UN/SUBSCRIBE TOPICS //
			else if (action.equals("subscribeToTopic")) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{
							FirebaseMessaging.getInstance().subscribeToTopic( args.getString(0) );
							callbackContext.success();
						}catch(Exception e){
							callbackContext.error(e.getMessage());
						}
					}
				});
			}
			else if (action.equals("unsubscribeFromTopic")) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{
							FirebaseMessaging.getInstance().unsubscribeFromTopic( args.getString(0) );
							callbackContext.success();
						}catch(Exception e){
							callbackContext.error(e.getMessage());
						}
					}
				});
			}
      else if (action.equals("sendUpstreamPush")) {
        cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            try{
              JSONObject pushData = args.getJSONObject(0);
              String messageID = String.valueOf(new Random().nextLong());

							pushSuccessCallBackReady = args.optBoolean(1, false);
							pushErrorCallBackReady = args.optBoolean(2, false);

              RemoteMessage.Builder msg = new RemoteMessage.Builder(senderId + "@gcm.googleapis.com")
                .setMessageId(messageID)
								.setTtl(86400);

              Iterator<String> dataKeys = pushData.keys();
              while(dataKeys.hasNext()) {
                String keyName = dataKeys.next();
                if (!pushData.isNull(keyName)) {
                  msg.addData(keyName, String.valueOf(pushData.get(keyName)));
                }
              }

              FirebaseMessaging.getInstance().send(msg.build());
              callbackContext.success(messageID);
            }catch(Exception e){
              callbackContext.error(e.getMessage());
            }
          }
        });
      }
			else{
				callbackContext.error("Method not found");
				return false;
			}
		}catch(Exception e){
			Log.d(TAG, "ERROR: onPluginAction: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}

		//cordova.getThreadPool().execute(new Runnable() {
		//	public void run() {
		//	  //
		//	}
		//});

		//cordova.getActivity().runOnUiThread(new Runnable() {
        //    public void run() {
        //      //
        //    }
        //});
		return true;
	}

	public static void sendPushSuccess(Map<String, Object> payload) {
		Log.d(TAG, "==> FCMPlugin sendPushSuccess");
		FCMPlugin.sendPushPayload(pushSuccessCallBack, pushSuccessCallBackReady, payload);
	}

	public static void sendPushError(Map<String, Object> payload) {
		Log.d(TAG, "==> FCMPlugin sendPushError");
		FCMPlugin.sendPushPayload(pushErrorCallBack, pushErrorCallBackReady, payload);
	}

	public static void sendNotification(Map<String, Object> payload) {
		Log.d(TAG, "==> FCMPlugin sendNotification");
		FCMPlugin.sendPushPayload(notificationCallBack, notificationCallBackReady, payload);
	}

	private static void sendPushPayload(String handler, Boolean handlerReady, Map<String, Object> payload) {
		Log.d(TAG, "\tcallBackReady: " + handlerReady);
		Log.d(TAG, "\tgWebView: " + gWebView);
	  try {
		  JSONObject jo = new JSONObject();
			for (String key : payload.keySet()) {
				jo.put(key, payload.get(key));
				Log.d(TAG, "\tpayload: " + key + " => " + payload.get(key));
      }

			String callBack = "javascript:" + handler + "(" + jo.toString() + ")";
			if(handlerReady && gWebView != null) {
				Log.d(TAG, "\tSent PUSH to view: " + callBack);
				gWebView.sendJavascript(callBack);
			} else {
				Log.d(TAG, "\tView not ready. SAVED NOTIFICATION: " + callBack);
				lastPush = payload;
			}
		} catch (Exception e) {
			Log.d(TAG, "\tERROR: " + e.getMessage());
			// can throw, was either a JSON or Map exception. Which means something is misformatted s
			// lastPush = payload;
		}
	}
}
