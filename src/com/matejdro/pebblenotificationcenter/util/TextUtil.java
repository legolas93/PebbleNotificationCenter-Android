package com.matejdro.pebblenotificationcenter.util;

import java.util.HashMap;

import com.matejdro.pebblenotificationcenter.PebbleNotificationCenter;


public class TextUtil {	
	public static String prepareString(String text)
	{
		return prepareString(text, 20);
	}

	public static String prepareString(String text, int length)
	{
		text = fixInternationalAndTrim(text, length);
        
        if (RTLUtility.getInstance().isRTL(text)){            
            text = RTLUtility.getInstance().format(text, 15);        	
        }
        
		return trimString(text, length, true);
	}


	public static String fixInternationalAndTrim(String text, int length)
	{
		StringBuilder builder = new StringBuilder(length);

		length = Math.min(length, text.length());

		HashMap<Character, String> replacementTable = PebbleNotificationCenter.getInMemorySettings().getReplacingStrings();

		for (int i = 0; i < length; i++)
		{
			char ch = text.charAt(i);

			String replacement = replacementTable.get(ch);
			if (replacement != null)
			{
				builder.append(replacement);
			}
			else
			{
				builder.append(ch);
			}
		}

		return builder.toString();		
	}

	public static String trimString(String text)
	{
		return trimString(text, 20, true);
	}

	public static String trimString(String text, int length, boolean trailingElipsis)
	{
		if (text == null)
			return null;

		int targetLength = length;
		if (trailingElipsis)
		{
			targetLength -= 3;
		}

		if (text.getBytes().length > length)
		{
			if (text.length() > targetLength)
				text = text.substring(0, targetLength);

			while (text.getBytes().length > targetLength)
			{
				text = text.substring(0, text.length() - 1);
			}

			if (trailingElipsis)
				text = text + "...";

		}

		return text;

	}
}
