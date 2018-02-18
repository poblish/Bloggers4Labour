/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author andrewregan
 */
public class ActivityTableLogStatsTest
{
	private final static String	FILENAME_PREFIX = "B4L_activity.log.";
	private final static Pattern	s_LinePattern = Pattern.compile("([0-9]+):([0-9]+):([0-9]+),[0-9]+ - DEBUG .* Stats: ([0-9]+) <= last hour, ([0-9]+) <= 2 hrs, ([0-9]+) <= 12 hrs, ([0-9]+) <= 1 day, ([0-9]+) <= 2 days, ([0-9]+) <= 3 days, ([0-9]+) <= 1 week, ([0-9]+) <= 2 weeks, ([0-9]+) <= 1 month, ([0-9]+) <= 2 months, ([0-9]+) > 2 months. Total: ([0-9]+)");

	private Map<Date,ActivityStatsEntry>	m_Map = new TreeMap<Date,ActivityStatsEntry>();

	/*******************************************************************************
	*******************************************************************************/
	@Test public void testDates()
	{
		File	theDir = new File("/Users/andrewregan/www/old-log");
		File[]	theActivityFiles = theDir.listFiles( new FilenameFilter()
		{
			public boolean accept( File dir, String name)
			{
				return name.startsWith("B4L_activity.log.");
			}
		} );

		SimpleDateFormat	theSDF = new SimpleDateFormat("yyyy-MM-dd");

		for ( File eachFile : theActivityFiles)
		{
			String	theDateStr = eachFile.getName().substring( FILENAME_PREFIX.length() );

			try
			{
				_handleFile( eachFile, theSDF.parse(theDateStr));
			}
			catch (ParseException ex)
			{
				ex.printStackTrace();
			}
		}

		System.out.println("Count: " + m_Map.size());

		////////////////////////////////////////////////////////////////

		File		theOutputFile = new File("/Users/andrewregan/Desktop/", Long.toString( System.currentTimeMillis() ) + ".txt");
		BufferedWriter	theBW = null;

		try
		{
			theBW = new BufferedWriter( new FileWriter(theOutputFile) );

			int	lastTotal = -1;

			for ( Map.Entry<Date,ActivityStatsEntry> eachEntry : m_Map.entrySet())
			{
				if ( lastTotal < 0 || lastTotal != eachEntry.getValue().m_Total)
				{
					theBW.write( eachEntry.getKey() + "\t" + eachEntry.getValue().m_Total);
					theBW.newLine();

					lastTotal = eachEntry.getValue().m_Total;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( theBW != null)
			{
				try
				{
					theBW.close();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _handleFile( final File inFile, final Date inDate)
	{
		BufferedReader	theBR = null;

		try
		{
			theBR = new BufferedReader( new FileReader(inFile) );

			String	theLineStr;

			while (( theLineStr = theBR.readLine()) != null)
			{
				_handleActivityLine( theLineStr, inDate);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( theBR != null)
			{
				try
				{
					theBR.close();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _handleActivityLine( final String inLineStr, final Date inDate)
	{
		Matcher	theMatcher = s_LinePattern.matcher(inLineStr);
		if (theMatcher.matches())
		{
			int	totalBlogs = Integer.parseInt( theMatcher.group(15) );

			if ( totalBlogs < 200)
			{
				return;
			}

			////////////////////////////////////////////////////////

			Calendar		theCal = Calendar.getInstance( Locale.UK );
			theCal.setTime(inDate);
			theCal.set( Calendar.HOUR, Integer.parseInt( theMatcher.group(1) ));
			theCal.set( Calendar.MINUTE, Integer.parseInt( theMatcher.group(2) ));
			theCal.set( Calendar.SECOND, Integer.parseInt( theMatcher.group(3) ));

			ActivityStatsEntry	theEntry = new ActivityStatsEntry(totalBlogs);
			int			idx = 4;

			for ( int i = 0; i < theEntry.m_Counts.length; i++)
			{
				theEntry.m_Counts[i] = Integer.parseInt( theMatcher.group(idx++) );
			}

			m_Map.put( theCal.getTime(), theEntry);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class ActivityStatsEntry
	{
		private int	m_Total;
		public int[]	m_Counts = new int[11];

		/*******************************************************************************
		*******************************************************************************/
		public ActivityStatsEntry( final int inTotal)
		{
			m_Total = inTotal;
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public String toString()
		{
			return Arrays.toString(m_Counts) + ", total: " + m_Total;
		}
	}
}