package com.matejdro.pebblenotificationcenter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.matejdro.pebblenotificationcenter.PebbleNotificationCenter;

public class SettingsMemoryStorage {
	private Context context;	

	private boolean dirty = true;

	private SharedPreferences preferences;
	private HashSet<String> selectedPackages;
	private List<Pattern> regexPatterns;
	private HashMap<Character, String> replacingStrings;

	public SettingsMemoryStorage(Context context)
	{
		this.context = context;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.selectedPackages = new HashSet<String>();
		this.replacingStrings = new HashMap<Character, String>();
		this.regexPatterns = new ArrayList<Pattern>();
	}

	public void markDirty()
	{
		dirty = true;
	}

	private void loadSettings()
	{
		selectedPackages.clear();
		regexPatterns.clear();
		replacingStrings.clear();

		preferences = PreferenceManager.getDefaultSharedPreferences(context);

		Iterator<String> packages = ListSerialization.getDirectIterator(preferences, PebbleNotificationCenter.SELECTED_PACKAGES);
		while (packages.hasNext())
		{
			selectedPackages.add(packages.next());
		}


		Iterator<String> blacklistRegexes = ListSerialization.getDirectIterator(preferences, PebbleNotificationCenter.REGEX_LIST);
		while (blacklistRegexes.hasNext())
		{
			regexPatterns.add(Pattern.compile(blacklistRegexes.next()));
		}

		Iterator<String> replacingKeys = ListSerialization.getDirectIterator(preferences, PebbleNotificationCenter.REPLACING_KEYS_LIST);
		Iterator<String> replacingValues = ListSerialization.getDirectIterator(preferences, PebbleNotificationCenter.REPLACING_VALUES_LIST);
		while (replacingKeys.hasNext() && replacingValues.hasNext())
		{
			String keyString = replacingKeys.next();
			if (keyString.isEmpty())
				continue;

			char keyCharacter = keyString.charAt(0);
			String valueString = replacingValues.next();

			replacingStrings.put(keyCharacter, valueString);
		}

		dirty = false;
	}

	public SharedPreferences getSharedPreferences()
	{
		if (dirty)
			loadSettings();

		return preferences;
	}

	public HashSet<String> getSelectedPackages()
	{
		if (dirty)
			loadSettings();

		return selectedPackages;
	}

	public List<Pattern> getRegexPatterns()
	{
		if (dirty)
			loadSettings();

		return regexPatterns;
	}
	public List<Pattern> getRegexPatterns(String appName, boolean include)
	{
		List<Pattern> regexPatternsTmp = new ArrayList<Pattern>();
		Iterator<String> blacklistRegexes;
		if(include){
			blacklistRegexes = ListSerialization.getDirectIterator(preferences, PebbleNotificationCenter.REGEX_LIST+"_IN"+appName);
		} else {
			blacklistRegexes = ListSerialization.getDirectIterator(preferences, PebbleNotificationCenter.REGEX_LIST+"_EX"+appName);
		}
		while (blacklistRegexes.hasNext())
		{
			regexPatternsTmp.add(Pattern.compile(blacklistRegexes.next()));
		}
		return regexPatternsTmp;
	}	

	public HashMap<Character, String> getReplacingStrings()
	{
		if (dirty)
			loadSettings();

		return replacingStrings;
	}
}
