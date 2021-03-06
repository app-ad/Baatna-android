package com.application.baatna.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.application.baatna.receivers.GcmBroadcastReceiver;
import com.application.baatna.utils.CommonLib;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public static boolean notificationDismissed = false;

	public Context context;
	private SharedPreferences prefs;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public static final int NOTIFICATION_ID = 1;

	public GcmIntentService() {
		super("GcmIntentService");
		context = this;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		prefs = getSharedPreferences("application_settings", 0);

		if (extras != null && !extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				CommonLib.ZLog("Send error:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				CommonLib.ZLog("Deleted messages on server:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

				if (extras.containsKey("Notification"))
					com.application.baatna.utils.NotificationManager.getInstance(getApplicationContext()).sendNotification(extras);

			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

//	private void sendNotification(Bundle extras) {
//
//		String msg = extras.getString("Notification");
//		String type = extras.getString("type");
//		Intent notificationActivity = null;
//		boolean showNotification = true;
//		if (type != null && type.equals("message")) {
//			JSONObject message = null;
//			try {
//				message = new JSONObject(msg);
//				Message messageObj = ParserJson.parse_Message(message);
//				if (messageObj != null && messageObj.getFromUser() != null && messageObj.getWish() != null) {
//					MessageDBWrapper.addMessage(messageObj, messageObj.getFromUser().getUserId(),
//							messageObj.getWish().getWishId(), System.currentTimeMillis());
//					msg = messageObj.getFromUser().getUserName() + " has sent you a message";
//					notificationActivity = new Intent(this, MessagesActivity.class);
//					notificationActivity.putExtra("user", messageObj.getFromUser());
//					notificationActivity.putExtra("wish", messageObj.getWish());
//					if(messageObj.getWish().getUserId()==messageObj.getFromUser().getUserId())
//					notificationActivity.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
//					else if(messageObj.getWish().getUserId()==getSharedPreferences(CommonLib.APP_SETTINGS, 0).getInt("uid",0))
//						notificationActivity.putExtra("type", CommonLib.CURRENT_USER_WISH_ACCEPTED);
//					else
//						notificationActivity.putExtra("type", type);
//
//					notificationActivity.putExtra("message", messageObj);
//
//					boolean object = CommonLib.getCurrentActiveActivity(context);
//					if (object) {
//						showNotification = true;
//						Intent mIntent = new Intent(CommonLib.LOCAL_BROADCAST_NOTIFICATION);
//						mIntent.putExtra("user", messageObj.getFromUser());
//						mIntent.putExtra("wish", messageObj.getWish());
//						mIntent.putExtra("type", type);
//						mIntent.putExtra("message", messageObj);
//						LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
//					}
//
//				}
//
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		} else if (type != null && type.equals("newWish")) {
//			JSONObject message = null;
//			try {
//				message = new JSONObject(msg);
//				notificationActivity = new Intent(this, WishActivity.class);
//				User mUser = null;
//				Wish mWish = null;
//				if (message.has("message")) {
//					msg = String.valueOf(message.get("message"));
//				}
//				if (message.has("wish") && message.get("wish") instanceof JSONObject
//						&& message.getJSONObject("wish").has("wish")) {
//					mWish = ParserJson.parse_Wish(message.getJSONObject("wish").getJSONObject("wish"));
//				}
//
//				if (message.has("user") && message.getJSONObject("user").has("user")) {
//					mUser = ParserJson.parse_User(message.getJSONObject("user").getJSONObject("user"));
//				}
//
//				notificationActivity.putExtra("user", mUser);
//				notificationActivity.putExtra("wish", mWish);
//				if(mUser!=null && mWish!=null)
//				{
//					Intent intent = new Intent(CommonLib.LOCAL_FEED_BROADCAST_NOTIFICATION);
//					FeedItem feedItem = new FeedItem();
//					feedItem.setFeedId(-1);
//					feedItem.setLatitude(0);
//					feedItem.setLongitude(0);
//					feedItem.setTimestamp(System.currentTimeMillis());
//					feedItem.setType(CommonLib.FEED_TYPE_NEW_REQUEST);
//					feedItem.setUserFirst(mUser);
//					feedItem.setWish(mWish);
//					intent.putExtra("feed",feedItem);
//					LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//				}
//
//
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		} else if (type != null && type.equals("wish")) {
//			JSONObject message = null;
//
//			try {
//				message = new JSONObject(msg);
//				if (message.has("wish") && message.getJSONObject("wish").has("wish")) {
//					Wish messageObj = ParserJson.parse_Wish(message.getJSONObject("wish").getJSONObject("wish"));
//					if (messageObj != null) {
//						if (message.has("from_user") && message.getJSONObject("from_user").has("user")) {
//							User user = ParserJson.parse_User(message.getJSONObject("from_user").getJSONObject("user"));
//							boolean isFriendOnFacebook = FriendChecker.isFriendOnFacebook(prefs.getString("fbId", ""),
//									user.getFbId(), prefs.getString("fb_token", ""));
//							if (isFriendOnFacebook) {
//								msg = user.getUserName() + " offered you a " + messageObj.getTitle()
//										+ ". Start chatting!";
//								notificationActivity = new Intent(this, MessagesActivity.class);
//								notificationActivity.putExtra("user", user);
//								notificationActivity.putExtra("wish", messageObj);
//								notificationActivity.putExtra("type", CommonLib.CURRENT_USER_WISH_ACCEPTED);
//							} else if (CommonLib.hasContact(this, user.getContact())) {
//								msg = user.getUserName() + " offered you a " + messageObj.getTitle()
//										+ ". Start chatting!";
//								notificationActivity = new Intent(this, MessagesActivity.class);
//								notificationActivity.putExtra("user", user);
//								notificationActivity.putExtra("wish", messageObj);
//								notificationActivity.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
//							} else
//								showNotification = false;
//						}
//					}
//				}
//
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		// check if app is alive, do not push the message notifiication then
//		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//		if (notificationActivity == null)
//			notificationActivity = new Intent(this, Splash.class);
//		int flags = PendingIntent.FLAG_CANCEL_CURRENT;
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationActivity, flags);
//		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
//				.setContentTitle("Baatna").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//				.setAutoCancel(true).setContentText(msg).setSound(soundUri);
//		mBuilder.setContentIntent(contentIntent);
//		if (showNotification)
//			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//	}
//
//	public static String decompress(byte[] compressed, int len) {
//		String outputStr = null;
//		try {
//			Inflater decompresor = new Inflater();
//			decompresor.setInput(compressed, 0, compressed.length);
//			byte[] result = new byte[len];
//			int resultLength = decompresor.inflate(result);
//			decompresor.end();
//
//			outputStr = new String(result, 0, resultLength, "UTF-8");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return outputStr;
//	}
}