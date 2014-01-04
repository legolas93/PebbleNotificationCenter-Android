package com.matejdro.pebblenotificationcenter.notifications;

import java.util.List;
import java.util.regex.Pattern;

import timber.log.Timber;
import android.R.bool;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.matejdro.pebblenotificationcenter.PebbleNotificationCenter;
import com.matejdro.pebblenotificationcenter.PebbleTalkerService;
import com.matejdro.pebblenotificationcenter.ui.PerAppActivity;
import com.matejdro.pebblenotificationcenter.util.SettingsMemoryStorage;

public class NotificationHandler {
	public static boolean active = false;

	public static void newNotification(Context context, String pack, Notification notification, Integer id, String tag, boolean isDismissible)
	{
		Timber.i("Processing notification from package %s", pack);

		SettingsMemoryStorage settings = PebbleNotificationCenter.getInMemorySettings();
		SharedPreferences preferences = settings.getSharedPreferences();

		boolean enableOngoing = preferences.getBoolean("enableOngoing", false);
		boolean isOngoing = (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0;

		if (isOngoing && !enableOngoing) {
			Timber.d("Discarding notification from %s because FLAG_ONGOING_EVENT is set.", pack);
			return;
		}

		boolean includingMode = preferences.getBoolean(PebbleNotificationCenter.APP_INCLUSION_MODE, false);
		boolean notificationExist = settings.getSelectedPackages().contains(pack);

		if (includingMode != notificationExist) {
			Timber.d("Discarding notification from %s because package is not selected", pack);
			return;
		}

		final String title = getAppName(context, pack);

		NotificationParser parser = new NotificationParser(context, notification);

		String secondaryTitle = parser.title;
		String text = parser.text.trim();

		if (notification.tickerText != null && (text == null || text.trim().length() == 0)) {
			text = notification.tickerText.toString();
		}

		if (!preferences.getBoolean("sendBlank", false)) {
			if (text.length() == 0 && (secondaryTitle == null || secondaryTitle.length() == 0)) {
				Timber.d("Discarding notification from %s because it is empty", pack);
				return;
			}
		}

		String patternMatched = null;
		for (Pattern pattern : settings.getRegexPatterns()) {	

			if (pattern.matcher(title).find() || (secondaryTitle != null && pattern.matcher(secondaryTitle).find()) || (pattern.matcher(text).find())) {
				patternMatched = pattern.toString();
				break;
			}
		}

		/**
		 * Logic for regex matching is as follows:
		 * regexMode == false    patternMatched == null       exclude, no match, send
		 * regexMode == false    patternMatched != null       exclude, matched, don't send
		 * regexMode == true     patternMatched == null       include, no match, don't send
		 * regexMode == true     patternMatched != null       include, matched, send
		 */
		boolean regexMode = preferences.getBoolean(PebbleNotificationCenter.REGEX_INCLUSION_MODE, false);
		if (regexMode == (patternMatched == null))
		{
			if (regexMode) Timber.d("Discarding notification from %s because it hasn't matched and regex mode is exclude.", pack);
			else           Timber.d("Discarding notification from %s because it has matched '%s' and regex mode is include.", pack, patternMatched);
			return;
		}

		int perAppMode = preferences.getInt(PebbleNotificationCenter.REGEX_LIST+"_MODE"+title, 0);
		/* Logic
		 * <string-array name="per_app_strongest_spinner">
		 * <item>Both</item>
		 * <item>Include ONLY of the Excluded</item>
		 * <item>Exclude ONLY of the Included</item>
		 * </string-array>
		 */
		List<Pattern> patternsIn = settings.getRegexPatterns(title, true);
		List<Pattern> patternsEx = settings.getRegexPatterns(title, false);
		Log.i(title, String.valueOf(patternsIn.size()));
		Log.i(title, String.valueOf(patternsEx.size()));
		Log.i(title, String.valueOf(perAppMode));
		if(perAppMode == 0){
			if(matchRegexVerifier(patternsIn, true, title, secondaryTitle, text) && matchRegexVerifier(patternsEx, false, title, secondaryTitle, text)){
				if (isDismissible)
					PebbleTalkerService.notify(context, id, pack, tag, title, secondaryTitle, text, !isOngoing);
				else
					PebbleTalkerService.notify(context, title, secondaryTitle, text);
			} else {
				return;
			}
		} else if (perAppMode == 1){
			if(!matchRegexVerifier(patternsEx, false, title, secondaryTitle, text) && matchRegexVerifier(patternsIn, true, title, secondaryTitle, text)){
				//Prevent the case of empty In list
				if(patternsIn.size()==0) return;
				if (isDismissible)
					PebbleTalkerService.notify(context, id, pack, tag, title, secondaryTitle, text, !isOngoing);
				else
					PebbleTalkerService.notify(context, title, secondaryTitle, text);
			} else if(matchRegexVerifier(patternsEx, false, title, secondaryTitle, text)){
				if (isDismissible)
					PebbleTalkerService.notify(context, id, pack, tag, title, secondaryTitle, text, !isOngoing);
				else
					PebbleTalkerService.notify(context, title, secondaryTitle, text);
			} else {
				return;
			}
		} else if (perAppMode == 2){
			if(matchRegexVerifier(patternsIn, true, title, secondaryTitle, text) && !matchRegexVerifier( patternsEx, false, title, secondaryTitle, text)){
				return;
			} else if(matchRegexVerifier(patternsIn, true, title, secondaryTitle, text)){
				if (isDismissible)
					PebbleTalkerService.notify(context, id, pack, tag, title, secondaryTitle, text, !isOngoing);
				else
					PebbleTalkerService.notify(context, title, secondaryTitle, text);
			} else {
				return;
			}
		} 


		//I think we never pass this line
		if (isDismissible)
			PebbleTalkerService.notify(context, id, pack, tag, title, secondaryTitle, text, !isOngoing);
		else
			PebbleTalkerService.notify(context, title, secondaryTitle, text);
	}

	// Return true when notification is ok, false when has not to be displayed
	public static boolean matchRegexVerifier(List<Pattern> patterns, boolean mode, String title, String secondaryTitle, String text){
		String patternMatched = null;
		if(patterns.size()==0){
			return true;
		}
		for (Pattern pattern : patterns) {	

			if (pattern.matcher(title).find() || (secondaryTitle != null && pattern.matcher(secondaryTitle).find()) || (pattern.matcher(text).find())) {
				patternMatched = pattern.toString();
				break;
			}
		}
		if(mode){
			return patternMatched != null;
		} else {
			return patternMatched == null;
		}
	}

	public static void notificationDismissedOnPhone(Context context, String pkg, String tag, int id)
	{		
		PebbleTalkerService.dismissOnPebble(id, pkg, tag);
	}

	public static String getAppName(Context context, String packageName)
	{
		final PackageManager pm = context.getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo( packageName, 0);
		} catch (final NameNotFoundException e) {
			ai = null;
		}
		final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Notification");
		return applicationName;

	}

	public static boolean isNotificationListenerSupported()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}
}
