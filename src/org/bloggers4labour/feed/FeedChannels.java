/*
 * FeedChannels.java
 *
 * Created on 12 March 2005, 00:32
 */

package org.bloggers4labour.feed;

import com.hiatus.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import de.nava.informa.parsers.*;
import de.nava.informa.core.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import org.apache.log4j.Logger;
import org.bloggers4labour.Installation;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class FeedChannels
{
	private List<FeedChannel>		m_List = new CopyOnWriteArrayList<FeedChannel>();  // (AGR) 21 June 2005

	private static ChannelBuilderIF		s_CBuilder = new ChannelBuilder();	// (AGR) post-23 May 2005
	private static Logger			s_FC_Logger = Logger.getLogger("Main");

	/*******************************************************************************
		(AGR) 21 June 2005. Changes... generally the list of Channels will
		never change once it is built up, so we can absorb a performance hit
		86 times there in order to have the benefit 86 times every 5-10 minutes
		for the few days the app runs. Hence CopyOnWriteArrayList. Allows us
		also to eliminate the slow .toArray() stuff we added to prevent
		ConcurrentModificationExceptions, plus the manual sync-ing.
	*******************************************************************************/
	public FeedChannels()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelIF findURL( String inURL)
	{
		// s_FC_Logger.info("findURL(): url = " + inURL + ", this = " + this);

		if (UText.isNullOrBlank(inURL))
		{
			return null;
		}

		///////////////////////////////////////////////////////////////  Post-21 June 2005

		for ( FeedChannel fc : m_List)
		{
			if (( fc.m_URL != null) && fc.m_URL.equalsIgnoreCase(inURL))
			{
				return fc.m_Channel;
			}
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return m_List.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void clear()
	{
		m_List.clear();
	}

	/*******************************************************************************
	*******************************************************************************/
	public ConnectResult connectTo( final Installation inInstall, int inThreadID, String inURL) 
	{
		ConnectStatus	theStatus = ConnectStatus.FAILURE;	// (AGR) 30 Nov 2005. Assume the worst...
		String		prefix = inInstall.getLogPrefix();	// (AGR) 20 Feb 2006

		try
		{
/*			// (AGR) 25 March 2005. Why the deprecated bit? Well, some feeds return 403 if *we*
			// try to access them. Can get around it by setting User-Agent to something 'typical'
			// However, the problem comes within the XML parsing, where WE have no control over
			// the URLConnection. What about a custom EntityResolver?
			// Hmm. Just try this bodge for now...

			URLConnection.setDefaultRequestProperty("User-Agent", "mozilla");
*/
			////////////////////////////////////////////////////////////////////

			ParserThreadStorage	theStorage = new ParserThreadStorage();
			ParserThread		theThread = new ParserThread( inThreadID, inURL, theStorage);

//			s_FC_Logger.info( prefix + "connectTo() #" + inThreadID + ": Start looking for " + inURL);

			theThread.start();

			try
			{
				long	numSecs = ONE_MINUTE_SECS;

				while ( numSecs-- > 0L &&	// Still seconds on the clock...
					!theStorage.completed)	// And no Channel got yet, or Exception caught...
				{
//					s_FC_Logger.info( prefix + "connectTo() #" + inThreadID + ": " + numSecs + " secs left!");

					Thread.sleep( ONE_SECOND_MSECS );	// Allow it to run for another second
				}

				//////////////////////////////////////////////// Time up! Still hasn't returned a channel.

				if (theStorage.completed)
				{
					if ( theStorage.channel != null)
					{
						theStatus = ConnectStatus.SUCCESS;

//						s_FC_Logger.info( prefix + "connectTo() #" + inThreadID + ": SUCCESS for " + inURL + " (" + numSecs + " secs left)");
					}
					else
					{
//						s_FC_Logger.info( prefix + "connectTo() #" + inThreadID + ": FAILURE for " + inURL + " (" + numSecs + " secs left)");
					}
				}
				else
				{
					theStatus = ConnectStatus.TIMEOUT;

					s_FC_Logger.info( prefix + "connectTo() #" + inThreadID + ": TIMEOUT for " + inURL);
					theThread.interrupt();
				}
			}
			catch (InterruptedException e)
			{
				s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": InterruptedException for: " + inURL);
			}

			if ( theStorage.exception != null)
			{
				throw theStorage.exception;
			}

			////////////////////////////////////////////////////////////////////

			if ( theStatus == ConnectStatus.SUCCESS)
			{
				m_List.add( new FeedChannel( inURL, theStorage.channel) );

				return new ConnectResult( theStorage.channel, theStatus);
			}
		}
		catch (NoRouteToHostException e)	// (AGR) 14 August 2005
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": NoRouteToHost... " + inURL);
		}
		catch (UnknownHostException e)
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": " + e);
		}
		catch (ParseException e)
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": ParseException for: " + inURL);
		}
		catch (SocketException e)
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": SocketException for \"" + inURL + "\" was " + e );
		}
		catch (FileNotFoundException e)		// (AGR) 5 June 2005
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": " + e);
		}
		catch (IOException e)
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": IOException for: " + inURL, e);
		}
		catch (Exception e)			// (AGR) 30 Nov 2005
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": Exception for: " + inURL, e);
		}

		return new ConnectResult( null, theStatus);
	}

	/*******************************************************************************
	*******************************************************************************/
	private class FeedChannel
	{
		public String		m_URL;
		public ChannelIF	m_Channel;

		/*******************************************************************************
		*******************************************************************************/
		public FeedChannel( String a, ChannelIF b)
		{
			m_URL = a;
			m_Channel = b;
		}

		/*******************************************************************************
		*******************************************************************************/
		public String toString()
		{
			return "[url=\"" + m_URL + "\",Channel=" + m_Channel + "]";
		}
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	private class ParserThreadStorage
	{
		private boolean		completed;
		public ChannelIF	channel;
		public Exception	exception;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	private class ParserThread extends Thread
	{
		private int			m_ThreadID;
		private String			m_URL;
		private ParserThreadStorage	m_Storage;

		/*******************************************************************************
		*******************************************************************************/
		public ParserThread( int inThreadID, String inURL, ParserThreadStorage inStorage)
		{
			m_ThreadID = inThreadID;
			m_URL = inURL;
			m_Storage = inStorage;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void run()
		{
			try
			{
				m_Storage.channel = FeedParser.parse( s_CBuilder, m_URL);
				m_Storage.completed = true;
			}
			catch (NullPointerException ex)    // (AGR) 7 October 2005. Improve error handling
			{
				s_FC_Logger.error("connectTo() #" + m_ThreadID + ": NPE doing: " + m_URL);
			}
			catch (Exception e)
			{
				m_Storage.exception = e;
				m_Storage.completed = true;
			}
		}
	}
}
