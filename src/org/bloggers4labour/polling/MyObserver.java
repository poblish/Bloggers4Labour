/*
 * MyObserver.java
 *
 * Created on 28 October 2006, 16:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.poller.*;
import java.net.URL;
import java.util.Date;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;
import org.bloggers4labour.activity.LastPostTable;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.headlines.*;

/**
 *
 * @author andrewre
 */
/*******************************************************************************
*******************************************************************************/
public class MyObserver implements PollerObserverIF
{
	private Installation	m_Installation;
	private String		m_LogPrefix;
	private HeadlinesMgr	m_HMgr;

	private static Logger	s_Poll_Logger = Logger.getLogger("Main");
	private static byte[]	s_itemFound_locker = new byte[0];	// (AGR) 21 October 2006

	/*******************************************************************************
	*******************************************************************************/
	public MyObserver( final Installation inInstallation)
	{
		m_Installation = inInstallation;
		m_LogPrefix = inInstallation.getLogPrefix();
		m_HMgr = inInstallation.getHeadlinesMgr();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void pollStarted( ChannelIF inChannel)
	{
		// s_Poll_Logger.info("... Polling started for " + inChannel);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void pollFinished( ChannelIF inChannel)
	{
		s_Poll_Logger.warn("... Polling FINISHED for " + inChannel);	// (AGR) 28 October 2006
	}

	/*******************************************************************************
	*******************************************************************************/
	public void channelChanged( ChannelIF inChannel)
	{
		// s_Poll_Logger.info("... " + inChannel + " CHANGED!");
	}

	/*******************************************************************************
	*******************************************************************************/
	public void channelErrored( ChannelIF inChannel, Exception inE)
	{
		s_Poll_Logger.warn( m_LogPrefix + "... ERROR for " + inChannel + "! ", inE);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void itemFound( ItemIF inItem, ChannelIF ioChannel)
	{
		////////////////////////////////////////////////////////  (AGR) 21 October 2006. Categories *seemed* to show sync problems. Perhaps not, but I think the sync here is justified.

		CategoriesTable		theCatsTable;
		FeedList		theFL;

		synchronized (s_itemFound_locker)
		{
			theCatsTable = m_Installation.getCategories();
			theFL = m_Installation.getFeedList();
		}

		//////////////////////////////////////////////////////////////////

		long		currTimeMSecs = System.currentTimeMillis();
		URL		theChannelLoc = ioChannel.getLocation();
		Date		itemDate = FeedUtils.getItemDate(inItem);
		AgeResult	theAgeResult = org.bloggers4labour.Poller.getItemAgeMsecs( inItem, itemDate, currTimeMSecs);

		//////////////////////////////////////////////////////////////////  (AGR) 10 Sep 2006

		Site		thisChannelsSite = theFL.lookupChannel(ioChannel);	// Moved up...
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

		if (org.bloggers4labour.Poller.ITEMS_PREPEND_INSTALL_NAME)
		{
			inItem.setTitle( m_LogPrefix + "- " + inItem.getTitle());
			// System.out.println("@@@  " + inItem.getTitle() + " / " + inItem.hashCode());
		}

		//////////////////////////////////////////////////////////////////

		// s_Poll_Logger.info("... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - add it");

		ioChannel.addItem(inItem);

		//////////////////////////////////////////////////////////////////  (AGR) 19 May 2005

		if (itemIsAPost)	// (AGR) 21 October 2006. Discovered that both B4L and Euston posts (one Poller each) were being
					// added to *both* CategoryTables, whether or not the post belonged to a feed in that Install!
					// This test ensures - see above - (a) item is not a comment, and (b) it's a post from a feed
					// in the current Install!
		{
			if (( theCatsTable != null) && ( itemDate != null) && theAgeResult.getAgeMSecs() < CategoriesTable.getMaxPermissibleItemAge())
			{
				theCatsTable.addCategories(inItem);
			}
		}

		//////////////////////////////////////////////////////////////////  (AGR) 30 Nov 2005. A comment or a post???

		if ( thisChannelsSite == null)
		{
			// (AGR) 21 Feb 2006. Disabled the error messgae. Why? Well, if we get a new post for a
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

//			boolean	itemIsAPost = thisChannelsSite.getChannel().getLocation().equals( theChannelLoc );

		//////////////////////////////////////////////////////////////////

		HeadlineFilter	theFilter = new HeadlineFilter( m_Installation, ioChannel);

		for ( Headlines	h : m_HMgr.getHeadlinesList())
		{
			if (( itemIsAPost && !h.allowsPosts()) ||
			   ( !itemIsAPost && !h.allowsComments()))
			{
				continue;
			}

			//////////////////////////////////////////////////////////////////  (AGR) 21 March 2006

			if (!theFilter.filterMessage( h, inItem))
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
			else	itemResult = org.bloggers4labour.Poller.processItem( h, inItem, theAgeResult.getAgeMSecs(), ItemContext.UPDATE);

			//////////////////////////////////////////////////////////////////

			if ( itemResult == AddResult.SUCCEEDED)
			{
				// s_Poll_Logger.info( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - ADDED OK to " + h);
			}
			else if ( itemResult == AddResult.FAILED_NO_DATE)
			{
				s_Poll_Logger.warn( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED - Date is NULL, for " + h);
			}
			else if ( itemResult == AddResult.FAILED_BAD_DATE)
			{
				// s_Poll_Logger.warn( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED - Date out of range! " + m_LastInfoMsg);
			}
			else if ( itemResult == AddResult.FAILED_GENERAL)
			{
				s_Poll_Logger.warn( m_LogPrefix + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED, somehow, for " + h);
			}
			else if ( itemResult == AddResult.FAILED_DUPLICATE)	// (AGR) 3 March 2006
			{
				// No need for an error - already done by Headlines!
			}
		}
	}
}
