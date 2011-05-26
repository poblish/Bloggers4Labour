/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import com.hiatus.dates.UDates;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.poller.InputStreamProviderIF;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author aregan
 */
public class B4LInputStreamProvider implements InputStreamProviderIF
{
	private final static Logger	s_Logger = Logger.getLogger( B4LInputStreamProvider.class );

	private final static TimeUnit	s_PollerTimeoutUnit = TimeUnit.SECONDS;
	private final static long	s_PollerTimeoutPeriod = 30;

	/*******************************************************************************
	*******************************************************************************/
	public B4LInputStreamProvider()
	{
		s_Logger.info("Created " + this);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static long getTimeoutMSecs()
	{
		return TimeUnit.MILLISECONDS.convert( s_PollerTimeoutPeriod, s_PollerTimeoutUnit);
	}

	/*******************************************************************************
	*******************************************************************************/
	public static CharSequence getTimeoutValueString()
	{
		return UDates.getFormattedTimeDiff( B4LInputStreamProvider.getTimeoutMSecs() );
	}

	/*******************************************************************************
	*******************************************************************************/
	public InputStream getInputStreamFor( final ChannelIF inChannel, final String inActivity) throws IOException
	{
		final URL	theURL = inChannel.getLocation();

		if ( theURL == null)
		{
			return null;

			// s_Logger.info(">> Opening... " + theURL);
		}

		final URLConnection	theConn = theURL.openConnection();
		final int		theTimeoutMSecs = (int) getTimeoutMSecs();

		theConn.setConnectTimeout(theTimeoutMSecs);
		theConn.setReadTimeout(theTimeoutMSecs);

/*		if ( "" != null && theConn instanceof HttpURLConnection)
		{
			HttpHeaderUtils.setUserAgent((HttpURLConnection) theConn, "");
		}
*/
		return new BufferedInputStream( theConn.getInputStream() );
	}
}
