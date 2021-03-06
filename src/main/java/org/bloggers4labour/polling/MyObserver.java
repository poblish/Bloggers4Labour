/*
 * MyObserver.java
 *
 * Created on 28 October 2006, 16:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import de.nava.informa.utils.poller.PollerObserverIF;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import org.apache.log4j.Logger;
import org.bloggers4labour.AddResult;
import org.bloggers4labour.AgeResult;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.HeadlinesMgr;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.DefaultChannelBridgeFactory;
import org.bloggers4labour.bridge.channel.item.DefaultItemBridgeFactory;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.cats.CategoriesTableIF;
import org.bloggers4labour.feed.FeedListIF;
import org.bloggers4labour.feed.check.DefaultFeedCheckerNotification;
import org.bloggers4labour.feed.check.FeedCheckException;
import org.bloggers4labour.feed.check.FeedCheckSuccess;
import org.bloggers4labour.feed.check.FeedCheckerAgentIF;
import org.bloggers4labour.feed.check.FeedCheckerNotificationIF;
import org.bloggers4labour.headlines.HeadlineFilter;
import org.bloggers4labour.headlines.HeadlinesIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
/*******************************************************************************
*******************************************************************************/
public class MyObserver implements PollerObserverIF, FeedCheckerAgentIF
{
	protected InstallationIF	m_Installation;
	protected String		m_LogPrefix;
	private HeadlinesMgr		m_HMgr;

	protected static Logger		s_Poll_Logger = Logger.getLogger( MyObserver.class );
	private final static byte[]	s_itemFound_locker = new byte[0];	// (AGR) 21 October 2006

	/*******************************************************************************
	*******************************************************************************/
	public MyObserver( final InstallationIF inInstallation)
	{
		m_Installation = inInstallation;
		m_LogPrefix = inInstallation.getLogPrefix();
		m_HMgr = inInstallation.getHeadlinesMgr();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void pollStarted( de.nava.informa.core.ChannelIF inChannel)
	{
		// s_Poll_Logger.info( m_LogPrefix + "... Polling started for " + inChannel);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void pollFinished( de.nava.informa.core.ChannelIF inChannel)
	{
		// s_Poll_Logger.warn( m_LogPrefix + "... Polling FINISHED for " + inChannel);	// (AGR) 28 October 2006
	}

	/*******************************************************************************
	*******************************************************************************/
	public void channelChanged( de.nava.informa.core.ChannelIF inChannel)
	{
		// s_Poll_Logger.info("... " + inChannel + " CHANGED!");
	}

	/*******************************************************************************
	*******************************************************************************/
	public void channelErrored( de.nava.informa.core.ChannelIF inChannel, Exception inE)
	{
		if ( inE instanceof SocketTimeoutException)	// (AGR) 13 August 2008
		{
			String	theErrorMsg = "POLLER READ TIMEOUT for " + inChannel;

			notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckException( theErrorMsg, inE), System.currentTimeMillis()) );

			s_Poll_Logger.warn( m_LogPrefix + "... " + theErrorMsg + " after " + B4LInputStreamProvider.getTimeoutValueString() + "!");
			return;
		}

		if ( inE instanceof IOException)		// (AGR) 2 November 2009
		{
			if ( inE.getMessage().contains("HTTP response code: 401"))
			{
				String	theErrorMsg = "POLLER ACCESS DENIED for " + inChannel;

				notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckException( theErrorMsg, inE), System.currentTimeMillis()) );

				s_Poll_Logger.warn( m_LogPrefix + "... " + theErrorMsg + "!");
				return;
			}
			else if ( inE.getMessage().contains("HTTP response code: 500"))
			{
				String	theErrorMsg = "POLLER got INTERNAL SERVER ERROR from " + inChannel;

				notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckException( theErrorMsg, inE), System.currentTimeMillis()) );

				s_Poll_Logger.warn( m_LogPrefix + "... " + theErrorMsg + "!");
				return;
			}
			else if ( inE.getMessage().contains("HTTP response code: 503"))
			{
				String	theErrorMsg = "POLLER got SERVICE UNAVAILABLE from " + inChannel;

				notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckException( theErrorMsg, inE), System.currentTimeMillis()) );

				s_Poll_Logger.warn( m_LogPrefix + "... " + theErrorMsg + "!");
				return;
			}
		}

		if ( inE instanceof UnknownHostException)	// (AGR) 25 November 2009
		{
			String	theErrorMsg = "POLLER UnknownHostException for " + inChannel;

			notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckException( theErrorMsg, inE), System.currentTimeMillis()) );

			s_Poll_Logger.warn( m_LogPrefix + "... " + theErrorMsg + "!");
			return;
		}

		//////////////////////////////////////////////////////////////////////////////////////////

		String	theErrorMsg = "POLLER ERROR for " + inChannel;

		notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckException( theErrorMsg, inE), System.currentTimeMillis()) );

		s_Poll_Logger.warn( m_LogPrefix + "... " + theErrorMsg + "! ", inE);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void itemFound( final de.nava.informa.core.ItemIF inItem, final de.nava.informa.core.ChannelIF inChannel)
	{
		notifyFeedCheckListeners( new DefaultFeedCheckerNotification( this, inChannel, new FeedCheckSuccess(), System.currentTimeMillis()) );

		////////////////////////////////////////////////////////  (AGR) 21 October 2006. Categories *seemed* to show sync problems. Perhaps not, but I think the sync here is justified.

		CategoriesTableIF	theCatsTable;
		FeedListIF		theFL;

		synchronized (s_itemFound_locker)
		{
			theCatsTable = m_Installation.getCategories();
			theFL = m_Installation.getFeedList();
		}

		//////////////////////////////////////////////////////////////////

		ChannelIF	theBridgedChannel = new DefaultChannelBridgeFactory().getInstance().bridge(inChannel);
		ItemIF		theBridgedItem = new DefaultItemBridgeFactory().getInstance().bridge(inItem);

		long		currTimeMSecs = System.currentTimeMillis();
		URL		theChannelLoc = inChannel.getLocation();
		Date		itemDate = FeedUtils.getItemDate(theBridgedItem);
		AgeResult	theAgeResult = Util.getItemAgeMsecs( m_Installation, theBridgedItem, itemDate, currTimeMSecs);

		//////////////////////////////////////////////////////////////////  (AGR) 10 Sep 2006

		SiteIF		thisChannelsSite = theFL.lookupChannel(theBridgedChannel);	// Moved up...
		boolean		itemIsAPost;

		if ( thisChannelsSite != null)
		{
			itemIsAPost = thisChannelsSite.getChannel().getLocation().equals( theChannelLoc );

			if (itemIsAPost)
			{
				m_Installation.getLastPostDateTable().store( theChannelLoc, currTimeMSecs - theAgeResult.getAgeMSecs());
			}
		}
		else
		{
			itemIsAPost = false;	// This FALSE value should never be used due to return below.
		}

		//////////////////////////////////////////////////////////////////

		if (!theAgeResult.isAllowable())	// (AGR) 14 Jan 2006
		{
			return;
		}

		//////////////////////////////////////////////////////////////////

		if (org.bloggers4labour.polling.Poller.ITEMS_PREPEND_INSTALL_NAME)
		{
			inItem.setTitle( m_LogPrefix + "- " + inItem.getTitle());
			// System.out.println("@@@  " + inItem.getTitle() + " / " + inItem.hashCode());
		}

		//////////////////////////////////////////////////////////////////

		// s_Poll_Logger.info("... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - add it");

	//	s_Poll_Logger.info("Channel: " + inChannel + " (" + inChannel.getClass() + ")");

		if ( inChannel instanceof ChannelIF)
		{
			if (!((ChannelIF) inChannel).addItemWithResult(inItem))
			{
				// s_Poll_Logger.info("NO CHANGE for " + inChannel + " - SKIP!");

				return;		// a bit crap, I know...
			}
		}
		else
		{
			inChannel.addItem(inItem);
		}

		// s_Poll_Logger.debug("Still here with " + inChannel + " / " + theBridgedItem);

		//////////////////////////////////////////////////////////////////  (AGR) 19 May 2005

		if (itemIsAPost)	// (AGR) 21 October 2006. Discovered that both B4L and Euston posts (one Poller each) were being
					// added to *both* CategoryTables, whether or not the post belonged to a feed in that Install!
					// This test ensures - see above - (a) item is not a comment, and (b) it's a post from a feed
					// in the current Install!
		{
			if (( theCatsTable != null) && ( itemDate != null) && theAgeResult.getAgeMSecs() < theCatsTable.getMaxAgeMSecs())
			{
				theCatsTable.addCategories(theBridgedItem);
			}
		}

		//////////////////////////////////////////////////////////////////  (AGR) 30 Nov 2005. A comment or a post???

		if ( thisChannelsSite == null)
		{
			// (AGR) 21 Feb 2006. Disabled the error message. Why? Well, if we get a new post for a
			// site that is in one Installation and not another, this method will be called for
			// both Poller objects, and one or the other won't contain that site, so inevitably this'll
			// provide a NULL Channel. Perhaps there's a better way, though, as the original error
			// was not unknown.

			// s_Poll_Logger.error("... CANNOT add \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" as Site lookup returned NULL");
			return;
		}

		// (AGR) 5 March 2006. There was a problem whereby new posts for sites that appeared in multiple Installations
		// were incorrectly tagged as *comments*. Why? Well, if there are TWO Site objects in existence for that site
		// (and OK there should only be ONE per Install/FeedList - the root cause???), then our @lookupChannel call above
		// might return the Site object from a different Install. So, even though this Item is a post, it's feed
		// Channel object is not the SAME ONE as the Channel from the found Site. It must, of course, have the same URL
		// and have identical contents, but it's a different object. So the old test:
		//
		//             boolean    itemIsAPost = ( thisChannelsSite.getChannel() == ioChannel);
		//
		// ... would incorrectly fail to match the two. Should compare URLs instead!

//		boolean	itemIsAPost = thisChannelsSite.getChannel().getLocation().equals( theChannelLoc );

		//////////////////////////////////////////////////////////////////

		final HeadlineFilter	theFilter = new HeadlineFilter( m_Installation, theBridgedChannel);

		for ( HeadlinesIF h : m_HMgr.getHeadlinesList())
		{
			if (( itemIsAPost && !h.allowsPosts()) ||
			   ( !itemIsAPost && !h.allowsComments()))
			{
				continue;
			}

			//////////////////////////////////////////////////////////////////  (AGR) 21 March 2006

			if (!theFilter.filterMessage( h, theBridgedItem))
			{
				// s_Poll_Logger.info( m_LogPrefix + ":: Filtering out.b... \"" + inItem + "\"");
				continue;
			}

			//////////////////////////////////////////////////////////////////

			AddResult	itemResult;

			if ( itemDate == null)
			{
				itemResult = AddResult.FAILED_NO_DATE;
			}
			else
			{
				// s_Poll_Logger.debug("Passing on... " + theBridgedItem + ", desc = " + theBridgedItem.getDescription().length() + " chars");

				itemResult = Util.processItem( h, theBridgedItem, thisChannelsSite, theAgeResult.getAgeMSecs(), ItemContext.UPDATE);
			}

			//////////////////////////////////////////////////////////////////

			if ( itemResult == AddResult.SUCCEEDED)
			{
				// s_Poll_Logger.info( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - ADDED OK to " + h);
			}
			else if ( itemResult == AddResult.FAILED_NO_DATE)
			{
				s_Poll_Logger.warn( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(theBridgedItem) + "\" for \"" + FeedUtils.channelToString(theBridgedChannel) + "\" - FAILED - Date is NULL, for " + h);
			}
			else if ( itemResult == AddResult.FAILED_BAD_DATE)
			{
				// s_Poll_Logger.warn( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED - Date out of range! " + m_LastInfoMsg);
			}
			else if ( itemResult == AddResult.FAILED_GENERAL)
			{
				s_Poll_Logger.warn( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(theBridgedItem) + "\" for \"" + FeedUtils.channelToString(theBridgedChannel) + "\" - FAILED, somehow, for " + h);
			}
		/*	else if ( itemResult == AddResult.FAILED_DUPLICATE)	// (AGR) 3 March 2006
			{
				// No need for an error - already done by Headlines!
			} */
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void notifyFeedCheckListeners( final FeedCheckerNotificationIF inNotification)
	{
		try
		{
			m_Installation.notifyFeedCheckListeners(inNotification);
		}
		catch (Throwable t)
		{
			s_Poll_Logger.error( "notifyFeedCheckListeners()", t);
		}
	}
}