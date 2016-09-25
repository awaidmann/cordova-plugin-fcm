var exec = require('cordova/exec');

function FCMPlugin() {
	console.log("FCMPlugin.js: is created");
}





// GET TOKEN //
FCMPlugin.prototype.getToken = function( success, error ){
	exec(success, error, "FCMPlugin", 'getToken', []);
}
// SUBSCRIBE TO TOPIC //
FCMPlugin.prototype.subscribeToTopic = function( topic, success, error ){
	exec(success, error, "FCMPlugin", 'subscribeToTopic', [topic]);
}
// UNSUBSCRIBE FROM TOPIC //
FCMPlugin.prototype.unsubscribeFromTopic = function( topic, success, error ){
	exec(success, error, "FCMPlugin", 'unsubscribeFromTopic', [topic]);
}
// SEND UPSTREAM PUSH NOTIFICATION //
FCMPlugin.prototype.sendUpstreamPush = function(pushData, options, callback, success, error){
	FCMPlugin.prototype.onPushSuccess = onPushSuccessHandler(success);
	FCMPlugin.prototype.onPushError = onPushErrorHandler(error);
	var params = options || {}
	exec(callback, FCMPlugin.onPushError, "FCMPlugin", 'sendUpstreamPush', [pushData, params.msgId, params.ttl, params.msgType]);
}
// NOTIFICATION CALLBACK //
FCMPlugin.prototype.onNotification = function( callback, success, error ){
	FCMPlugin.prototype.onNotificationReceived = onNotificationReceivedHandler(callback);
	exec(success, error, "FCMPlugin", 'registerNotification',[]);
}
// DEFAULT NOTIFICATION CALLBACK //
FCMPlugin.prototype.onNotificationReceived = onNotificationReceivedHandler();
FCMPlugin.prototype.onPushSuccess = onPushSuccessHandler();
FCMPlugin.prototype.onPushError = onPushErrorHandler();

function onNotificationReceivedHandler(callback) {
	return payload => {
		console.log('noti handler')
		if (callback) {
			return callback(payload);
		} else {
			console.log("Received push notification");
			console.log(payload);
		}
	}
}

function onPushSuccessHandler(callback) {
	return payload => {
		if (callback) {
			return callback(payload.msgId);
		} else {
			console.log("Successfully sent upstream push");
			console.log(payload);
		}
	}
}

function onPushErrorHandler(callback) {
	return payload => {
		if (callback) {
			var err = new Error();
			err.name = payload.error;
			err.code = payload.errorCode;
			return callback(payload.msgId, err);
		} else {
			console.log("Upstream push has failed");
			console.log(payload);
		}
	}
}

// FIRE READY //
exec(function(result){ console.log("FCMPlugin Ready OK") }, function(result){ console.log("FCMPlugin Ready ERROR") }, "FCMPlugin",'ready',[]);





var fcmPlugin = new FCMPlugin();
module.exports = fcmPlugin;
