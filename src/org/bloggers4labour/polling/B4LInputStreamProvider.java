/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.utils.poller.InputStreamProviderIF;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author aregan
 */
public class B4LInputStreamProvider implements InputStreamProviderIF
{
	private static Logger	s_Logger = Logger.getLogger( B4LInputStreamProvider.class );

	/*******************************************************************************
	*******************************************************************************/
	public B4LInputStreamProvider()
	{
		s_Logger.info("Created " + this);
	}

	/*******************************************************************************
	*******************************************************************************/
	public InputStream getInputStreamFor( final ChannelIF inChannel, String inActivity) throws IOException
	{
		URL	theURL = inChannel.getLocation();

		if ( theURL == null)
		{
			return null;

			// s_Logger.info(">> Opening... " + theURL);
		}

		URLConnection	theConn = theURL.openConnection();

		theConn.setConnectTimeout( 20 * (int) ONE_SECOND_MSECS);
		theConn.setReadTimeout( 20 * (int) ONE_SECOND_MSECS);

/*		if ( "" != null && theConn instanceof HttpURLConnection)
		{
			HttpHeaderUtils.setUserAgent((HttpURLConnection) theConn, "");
		}
*/
		return new BufferedInputStream( theConn.getInputStream() );
	}
}
