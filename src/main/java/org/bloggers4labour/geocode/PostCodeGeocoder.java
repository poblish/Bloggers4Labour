/*
 * PostCodeGeocoder.java
 *
 * Created on 15 February 2007, 22:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.geocode;

import com.hiatus.dates.UDates;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Scanner;
import org.apache.log4j.Logger;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class PostCodeGeocoder
{
	private PostCodeGeocoderCacheIF		m_Cache;

	private static Logger			s_Logger = Logger.getLogger( PostCodeGeocoder.class );

	/*******************************************************************************
	*******************************************************************************/
	public PostCodeGeocoder()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public PostCodeGeocoder( final PostCodeGeocoderCacheIF inCache)
	{
		m_Cache = inCache;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Location lookupLocation( final String inPostCodeStr) throws IOException
	{
//		long	startTimeMSecs = System.currentTimeMillis();

		if ( inPostCodeStr == null)
		{
			return Location.BLANK;
		}

		////////////////////////////////////////////////////////////////

		String	thePC = inPostCodeStr.trim().toUpperCase( Locale.UK );

		if ( thePC.length() < 1)
		{
			return Location.BLANK;
		}

		////////////////////////////////////////////////////////////////

		if ( m_Cache != null)
		{
		Location	cachedLoc = m_Cache.get(thePC);

		if ( cachedLoc != null)
		{
			return cachedLoc;
			}
		}

		////////////////////////////////////////////////////////////////

		InputStream	theStream = null;
		Scanner		theScanner = null;
		URL		theURL = null;
		long		startTimeMS = System.currentTimeMillis();

		try
		{
			theURL = new URL("http://geo.localsearchmaps.com/?zip=" + URLEncoder.encode( thePC, "UTF-8") + "&country=UK&format=txt");

			// System.out.println("Connecting to: " + theURL);

			URLConnection	theConn = theURL.openConnection();

			System.out.println("Connected: " + theConn);

			theConn.setConnectTimeout( 30 * (int) ONE_SECOND_MSECS);
			theConn.setReadTimeout( 30 * (int) ONE_SECOND_MSECS);
			theConn.connect();

			theStream = theConn.getInputStream();
			theScanner = new Scanner(theStream).useDelimiter(", *");

			if (!theScanner.hasNextDouble())
			{
				throw new UnexpectedFormatException("1");
			}

			double	theLat = theScanner.nextDouble();

			if (!theScanner.hasNextDouble())
			{
				throw new UnexpectedFormatException("2");
			}

			double	theLong = theScanner.nextDouble();

			System.out.println("Communication took " + UDates.getFormattedTimeDiff( System.currentTimeMillis() - startTimeMS));

			/////////////////////////////////////////////////////////////////////////

			Location	theLoc = new Location( theLat, theLong);

			if ( m_Cache != null)
			{
			m_Cache.put( thePC, theLoc);
			}

			return theLoc;
		}
		catch (SocketTimeoutException se)
		{
			s_Logger.error("lookupLocation() socket TIMEOUT for '" + theURL + "' after " + UDates.getFormattedTimeDiff( System.currentTimeMillis() - startTimeMS));
			throw se;
		}
		catch (UnexpectedFormatException e)
		{
			s_Logger.error("lookupLocation() " + e + " for " + theURL);

			return Location.BLANK;	// Note: don't store this in the map - so retries may succeed.
		}
		catch (IOException e)
		{
			s_Logger.error("lookupLocation() for " + theURL, e);
			throw e;
		}
		finally
		{
			if ( theScanner != null)
			{
				theScanner.close();
				// theScanner = null;
			}

			if ( theStream != null)
			{
				try
				{
					theStream.close();
				}
				catch (IOException e2)
				{
					s_Logger.error("connection close", e2);
				}
				finally
				{
					// theStream = null;
				}
			}

			// System.out.println("lookupLocation() took " + UDates.getFormattedTimeDiff( System.currentTimeMillis() - startTimeMSecs));
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class UnexpectedFormatException extends Exception
	{
		private static final long serialVersionUID = 1L;

		/*******************************************************************************
		*******************************************************************************/
		public UnexpectedFormatException( String inS)
		{
			super("Unexpected format! " + inS);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void main( String[] args)
	{
		PostCodeGeocoder	pcg = new PostCodeGeocoder();

		for ( int i = 0; i < 5; i++)
		{
			lookup( pcg, " ");
			lookup( pcg, "Sw10AA");
			lookup( pcg, "BN3 3JN");
			lookup( pcg, "EC2A 4BT");
			lookup( pcg, "BN3 3JN");
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void lookup( PostCodeGeocoder ioPCG, final String s)
	{
		System.out.println("Looking up: " + s);

		Location	l = null;

		try
		{
			l = ioPCG.lookupLocation(s);
		}
		catch (IOException e)
		{
			// NOOP
		}

		System.out.println("=> got " + l);
	}
}
