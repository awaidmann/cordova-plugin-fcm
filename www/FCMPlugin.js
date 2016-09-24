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
FCMPlugin.prototype.sendUpstreamPush = function(pushData, callback, success, error){
	FCMPlugin.prototype.onPushSuccess = success || FCMPlugin.onPushSuccess;
	FCMPlugin.prototype.onPushError = error || FCMPlugin.onPushError;
	exec(callback, FCMPlugin.onPushError, "FCMPlugin", 'sendUpstreamPush', [pushData, Boolean(success), Boolean(error)]);
}
// NOTIFICATION CALLBACK //
FCMPlugin.prototype.onNotification = function( callback, success, error ){
	FCMPlugin.prototype.onNotificationReceived = callback;
	exec(success, error, "FCMPlugin", 'registerNotification',[]);
}
// DEFAULT NOTIFICATION CALLBACK //
FCMPlugin.prototype.onNotificationReceived = function(payload){
	console.log("Received push notification");
	console.log(payload);
}

FCMPlugin.prototype.onPushSuccess = function(payload){
	console.log("Successfully sent upstream push");
	console.log(payload);
}

FCMPlugin.prototype.onPushError = function(payload){
	console.log("Upstream push has failed");
	console.log(payload);
}

// FIRE READY //
exec(function(result){ console.log("FCMPlugin Ready OK") }, function(result){ console.log("FCMPlugin Ready ERROR") }, "FCMPlugin",'ready',[]);





var fcmPlugin = new FCMPlugin();
module.exports = fcmPlugin;
