/*
 * FeedChannels.java
 *
 * Created on 12 March 2005, 00:32
 */

package org.bloggers4labour.feed;

import com.hiatus.text.UText;
import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.parsers.FeedParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.io.UTFDataFormatException;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.DefaultChannelBridgeFactory;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class FeedChannels implements FeedChannelsIF
{
	private List<FeedChannel>		m_List = new CopyOnWriteArrayList<FeedChannel>();  // (AGR) 21 June 2005

	private static ChannelBuilderIF		s_CBuilder = new MyLimitedChannelBuilder();
	private static Logger			s_FC_Logger = Logger.getLogger( FeedChannels.class );

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
	@Override public String toString()
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
	public ConnectResult connectTo( final InstallationIF inInstall, int inThreadID, String inURL) 
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
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": SocketException for \"" + inURL + "\" was " + e);
		}
		catch (FileNotFoundException e)		// (AGR) 5 June 2005
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": " + e);
		}
		catch (UTFDataFormatException e)	// (AGR) 23 August 2008
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": UTFDataFormatException for \"" + inURL + "\" was " + e.getMessage());
		}
		catch (IOException e)
		{
			if ( e.getMessage().contains("HTTP response code: 401"))	// (AGR) 2 November 2009
			{
				s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": Access DENIED for: " + inURL);
			}
			else
			{
				s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": IOException for: " + inURL, e);
			}
		}
		catch (Exception e)			// (AGR) 30 Nov 2005
		{
			s_FC_Logger.error( prefix + "connectTo() #" + inThreadID + ": Exception for: " + inURL, e);
		}

		return new ConnectResult( null, theStatus);
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class FeedChannel
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
		@Override public String toString()
		{
			return "[url=\"" + m_URL + "\",Channel=" + m_Channel + "]";
		}
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	private static class ParserThreadStorage
	{
		private boolean		completed;
		public ChannelIF	channel;
		public Exception	exception;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	private static class ParserThread extends Thread
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

			setDaemon(true);	// (AGR) 16 August 2008
		}

		/*******************************************************************************
		*******************************************************************************/
		@Override public void run()
		{
			try
			{
				m_Storage.channel = new DefaultChannelBridgeFactory().getInstance().bridge( FeedParser.parse( s_CBuilder, m_URL) );
				m_Storage.completed = true;
			}
			catch (NullPointerException ex)    // (AGR) 7 October 2005. Improve error handling
			{
				if ( s_FC_Logger != null)
				{
					ex.printStackTrace();
				s_FC_Logger.error("connectTo() #" + m_ThreadID + ": NPE doing: " + m_URL);
				}
			}
			catch (Exception e)
			{
				m_Storage.exception = e;
				m_Storage.completed = true;
			}
		}
	}
}
