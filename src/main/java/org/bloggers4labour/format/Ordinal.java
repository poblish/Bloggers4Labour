/*
 * Ordinal.java
 *
 * Created on 05 October 2006, 23:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.format;

import java.text.NumberFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author andrewre
 *
 * Originally from here: http://www.javalobby.org/forums/thread.jspa?threadID=16906&tstart=0
 * But I then totally rewrote it!
 *
 */
public class Ordinal
{
	/*******************************************************************************
	*******************************************************************************/
	public static String formatOrdinal( LocaleHelper inHelper, int inValue)
	{
		return formatOrdinal( inHelper, inValue, false);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static String formatOrdinal( LocaleHelper inHelper, int inValue, boolean inIsEqual)
	{
		Formatter	theFormatter = new Formatter( inHelper.m_Locale );
		String		theKey;

		if (inHelper.m_Cache_IsEnglish)
		{
			if ( inValue == 0)
			{
				return "0";
			}

			int	hundredRemainder = inValue % 100;
			int	tenRemainder = inValue % 10;

			if ( hundredRemainder - tenRemainder == 10)
			{
				theKey = "ordinal_th";
			}
			else if ( tenRemainder == 1)
			{
				theKey = "ordinal_st";
			}
			else if ( tenRemainder == 2)
			{
				theKey = "ordinal_nd";
			}
			else if ( tenRemainder == 3)
			{
				theKey = "ordinal_rd";
			}
			else
			{
				theKey = "ordinal_th";
			}
		}
		else
		{
			theKey = "ordinal_default";
		}

		////////////////////////////////////////////////////////////////

		theFormatter.format( inHelper.m_Locale, inHelper.m_Bundle.getString(theKey), inHelper.m_Format.format(inValue));

		if (inIsEqual)
		{
			theFormatter.format( inHelper.m_Locale, inHelper.m_Bundle.getString("ordinal_equal"), theFormatter.toString());
		}

		return theFormatter.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public static LocaleHelper getHelper( Locale inLocale)
	{
		return new LocaleHelper(inLocale);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void main( String[] args)
	{
		LocaleHelper	theHelper = getHelper(Locale.UK);

		for ( int i = 900; i <= 1200; i++)
		{
			System.out.println( i + " => " + formatOrdinal( theHelper, i, Math.random() >= 0.5));
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static class LocaleHelper
	{
		Locale		m_Locale;
		NumberFormat	m_Format;
		ResourceBundle	m_Bundle;
		boolean		m_Cache_IsEnglish;

		/*******************************************************************************
		*******************************************************************************/
		public LocaleHelper( Locale inLocale)
		{
			m_Locale = inLocale;

			m_Format = NumberFormat.getInstance(inLocale);
			m_Format.setGroupingUsed(true);

			m_Bundle = ResourceBundle.getBundle( "org/bloggers4labour/format/OrdinalFormats", inLocale);
			m_Cache_IsEnglish = inLocale.getLanguage().equals("en");
		}
	}
}
