/*
 * Poller.java
 *
 * Created on 12 March 2005, 01:11
 */

package org.bloggers4labour;

import com.hiatus.UDates;
import de.nava.informa.parsers.*;
import de.nava.informa.core.*;
import de.nava.informa.utils.poller.*;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.bloggers4labour.activity.*;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.headlines.HeadlineFilter;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class Poller
{
	private Installation					m_Installation;
	private PollerObserverIF				m_Observer;

	private static Logger					s_Poll_Logger = Logger.getLogger("Main");
	private static de.nava.informa.utils.poller.Poller	s_InformaPoller;

	private static Item[]					s_EmptyItemsArray = new Item[0];

	private final static int				MAX_POSTS_PER_BLOG	= 40;	// (AGR) 3 April 2005. Raised from 10 because of MadMusingsOfMe !
	private final static int				MAX_COMMENTS_PER_POST	= 50;	// (AGR) 29 Nov 2005

	private final static long				MAX_AGE_FOR_RECENT_POST = Constants.ONE_DAY_MSECS * 14;

	private final static boolean				ITEMS_PREPEND_INSTALL_NAME = false;	// (AGR) 2 March 2006

	/*******************************************************************************
		(AGR) 27 May 2005
		Only want one of these - it contains a Thread Pool
	*******************************************************************************/
	static
	{
		s_InformaPoller = new de.nava.informa.utils.poller.Poller();
		s_InformaPoller.setPeriod( 4 * ONE_MINUTE_MSECS );	// (AGR) 21 Sept 2006. Temporary increase from '2'. Default is 1 HOUR!

		System.out.println("Informa Poller: created " + s_InformaPoller);
	}

	/*******************************************************************************
	*******************************************************************************/
	public Poller( final Installation inInstallation)
	{
		m_Installation = inInstallation;

		_startPolling();
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _startPolling()
	{
		m_Observer = new MyObserver();

		s_InformaPoller.addObserver(m_Observer);

		// s_Poll_Logger.info("Poller: add Observer: " + m_Observer);
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void startPolling()
	{
		if ( m_Observer == null)
		{
			_startPolling();
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void cancelPolling()
	{
		if ( m_Observer != null)
		{
			s_Poll_Logger.info( m_Installation.getLogPrefix() + "Poller: removing Observer: " + m_Observer + " and stopping");

			s_InformaPoller.removeObserver(m_Observer);

			m_Observer = null;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void _processChannelItems( ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		Item[]	theItemsArray = _getChannelItemsArray(inChannel);

		if ( theItemsArray.length > 0)
		{
			HeadlinesMgr		theHMgr = m_Installation.getHeadlinesMgr();
			HeadlineFilter		theFilter = new HeadlineFilter( m_Installation, inChannel);

			int[]			hCountArray = new int[ theHMgr.getHeadlinesCount() ];		// (AGR) 22 May 2005
//			ActivityTable		theActivityTable = m_Installation.getActivityTable();		// (AGR) 8 March 2006
			LastPostTable		theLastPostTable = m_Installation.getLastPostDateTable();	// (AGR) 9 Sep 2006
			String			theChannelSiteURL = inChannel.getSite().toString();		// (AGR) 8 March 2006
			CategoriesTable		theCatsTable = m_Installation.getCategories();
			long			mostRecentPostAgeMsecs = Long.MIN_VALUE;			// (AGR) 26 Feb 2006
			int			recentPosts = 0;						// (AGR) 26 Feb 2006
			List<ItemIF>		theItemsToRemoveFromChannel = new ArrayList<ItemIF>();		// (AGR) 3 March 2006
			int			datelessPostCount = 0;						// (AGR) 4 March 2006

			for ( int i = 0; i < theItemsArray.length; i++)
			{
				Date		itemDate = FeedUtils.getItemDate( theItemsArray[i] );
				AgeResult	theAgeResult = _getItemAgeMsecs( theItemsArray[i], itemDate, inCurrentTimeMSecs);

				//////////////////////////////////////////////////////////////////  (AGR) 26 Feb 2006, 9 Sep 2006

				long	adjustedPostMSecs = inCurrentTimeMSecs - theAgeResult.getAgeMSecs();

				if ( adjustedPostMSecs > mostRecentPostAgeMsecs)
				{
					mostRecentPostAgeMsecs = adjustedPostMSecs;
				}

				//////////////////////////////////////////////////////////////////

				if (!theAgeResult.isAllowable())	// (AGR) 14 Jan 2006
				{
					theItemsToRemoveFromChannel.add( theItemsArray[i] );
					continue;
				}

				//////////////////////////////////////////////////////////////////

				if (ITEMS_PREPEND_INSTALL_NAME)
				{
					theItemsArray[i].setTitle( m_Installation.getLogPrefix() + "- " + theItemsArray[i].getTitle());
					// System.out.println("+++  " + theItemsArray[i].getTitle() + " / " + theItemsArray[i].hashCode());
				}

				//////////////////////////////////////////////////////////////////  (AGR) 19 May 2005

				if (( theCatsTable != null) && ( itemDate != null) && theAgeResult.getAgeMSecs() < CategoriesTable.getMaxPermissibleItemAge())
				{
					theCatsTable.addCategories( theItemsArray[i] );
				}

				//////////////////////////////////////////////////////////////////  (AGR) 8 March 2006

/*				if ( theActivityTable != null)
				{
					theActivityTable.store( theAgeResult.getAgeMSecs(), theChannelSiteURL);
				}
*/
				//////////////////////////////////////////////////////////////////

				int	hIndex = 0;

				for ( Headlines	h : theHMgr.getHeadlinesList())
				{
					if (!h.allowsPosts())	// (AGR) 29 Nov 2005
					{
						continue;
					}

					///////////////////////////////////////////////////////

					if ( hCountArray[hIndex] > MAX_POSTS_PER_BLOG)
					{
						continue;
					}

					//////////////////////////////////////////////////////////////////  (AGR) 21 March 2006

					if (!theFilter.filterMessage( h, theItemsArray[i]))
					{
						// s_Poll_Logger.info( m_Installation.getLogPrefix() + ":: Filtering out.a... \"" + theItemsArray[i] + "\"");
						continue;
					}

					///////////////////////////////////////////////////////

					AddResult	theResult;

					if ( itemDate == null)
					{
						datelessPostCount++;

						theResult = AddResult.FAILED_NO_DATE;
					}
					else	theResult = _processItem( h, theItemsArray[i], theAgeResult.getAgeMSecs(), ItemContext.SNAPSHOT);

					///////////////////////////////////////////////////////

					if ( theResult == AddResult.SUCCEEDED)
					{
						hCountArray[hIndex]++;
					}

					hIndex++;
				}
			}

			_handleRemovalsFromChannel( inChannel, theItemsToRemoveFromChannel, true);

			////////////////////////////////////////////////////////  (AGR) 4 March 2006

			if ( datelessPostCount > 1)
			{
				s_Poll_Logger.info( m_Installation.getLogPrefix() + "WARNING. Found " + datelessPostCount + " undated posts in \"" + FeedUtils.channelToString(inChannel) + "\"");
			}
			else if ( datelessPostCount == 1)
			{
				s_Poll_Logger.info( m_Installation.getLogPrefix() + "WARNING. Found an undated post in \"" + FeedUtils.channelToString(inChannel) + "\"");
			}

			////////////////////////////////////////////////////////  (AGR) 9 Sep 2006

			if ( mostRecentPostAgeMsecs > 0)
			{
				// s_Poll_Logger.info("### " + inChannel.getSite() + " / " + inChannel.getLocation());

				theLastPostTable.store( inChannel.getLocation(), mostRecentPostAgeMsecs);
			}
		}
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	private void _processCommentChannelItems( ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		Item[]		theItemsArray = _getChannelItemsArray(inChannel);
		HeadlinesMgr	theHMgr = m_Installation.getHeadlinesMgr();
		int[]		hCommentsCountArray = new int[ theHMgr.getHeadlinesCount() ];
		AddResult	theResult;
		List<ItemIF>	theItemsToRemoveFromChannel = new ArrayList<ItemIF>();	// (AGR) 3 March 2006

//		s_Poll_Logger.info( m_Installation.getLogPrefix() + "#### location = " + inChannel.getLocation());
//		s_Poll_Logger.info( m_Installation.getLogPrefix() + "####     site = " + inChannel.getSite());

		for ( int i = 0; i < theItemsArray.length; i++)
		{
			Date		itemDate = FeedUtils.getItemDate( theItemsArray[i] );
			AgeResult	theAgeResult = _getItemAgeMsecs( theItemsArray[i], itemDate, inCurrentTimeMSecs);

			if (!theAgeResult.isAllowable())	// (AGR) 14 Jan 2006
			{
				theItemsToRemoveFromChannel.add( theItemsArray[i] );
				continue;
			}

			//////////////////////////////////////////////////////////////////

			int	hIndex = 0;

			for ( Headlines	h : theHMgr.getHeadlinesList())
			{
				if (!h.allowsComments())	// (AGR) 29 Nov 2005
				{
					continue;
				}

				///////////////////////////////////////////////////////

				if ( hCommentsCountArray[hIndex] > MAX_COMMENTS_PER_POST)
				{
					continue;
				}

				///////////////////////////////////////////////////////

				if ( itemDate == null)
				{
					s_Poll_Logger.info( m_Installation.getLogPrefix() + "Cannot add date-less COMMENT for \"" + FeedUtils.channelToString(inChannel) + "\"");

					theResult = AddResult.FAILED_NO_DATE;
				}
				else	theResult = _processItem( h, theItemsArray[i], theAgeResult.getAgeMSecs(), ItemContext.SNAPSHOT);

				///////////////////////////////////////////////////////

				if ( theResult == AddResult.SUCCEEDED)
				{
					hCommentsCountArray[hIndex]++;
				}

				hIndex++;
			}
		}

		_handleRemovalsFromChannel( inChannel, theItemsToRemoveFromChannel, false);
	}

	/*******************************************************************************
		(AGR) 3 March 2006
	*******************************************************************************/
	private void _handleRemovalsFromChannel( ChannelIF ioChannel, List<ItemIF> ioList, boolean inIsPosts)
	{
		if ( ioList.size() > 0)
		{
			synchronized (ioChannel)	// I think...
			{
				for ( ItemIF eachItem : ioList)
				{
					ioChannel.removeItem(eachItem);
				}
			}

/*			if (inIsPosts)
			{
				s_Poll_Logger.info( m_Installation.getLogPrefix() + "!!! " + ioList.size() + " posts removed from \"" + FeedUtils.channelToString(ioChannel) + "\"");
			}
			else	s_Poll_Logger.info( m_Installation.getLogPrefix() + "!!! " + ioList.size() + " comments removed from \"" + FeedUtils.channelToString(ioChannel) + "\"");
*/
			ioList.clear();
		}
	}

	/*******************************************************************************
		(AGR) 12 Dec 2005
		A result of seeing yet more ConcurrentModificationExceptions
	*******************************************************************************/
	private synchronized Item[] _getChannelItemsArray( final ChannelIF inChannel)
	{
		Collection	c = inChannel.getItems();

		return ( c != null) ? (Item[]) c.toArray(s_EmptyItemsArray) : s_EmptyItemsArray;
	}

	/*******************************************************************************
	*******************************************************************************/
	private AgeResult _getItemAgeMsecs( ItemIF inItem, Date inItemDate, long inCurrentTimeMSecs)
	{
		if ( inItemDate == null)
		{
			return new AgeResult(0);	// !!! This value should never, in practice, be used.
		}

		////////////////////////////////////////////////////////

		long	itemAgeMSecs = inCurrentTimeMSecs - inItemDate.getTime();

// System.out.println( FeedUtils.getDisplayTitle(inItem) + "\t\t" + inItemDate);

		if ( itemAgeMSecs >= 0)
		{
			return new AgeResult(itemAgeMSecs);
		}

// System.out.println( FeedUtils.getDisplayTitle(inItem) + "\t\t" + itemAgeMSecs);

		////////////////////////////////////////////////////////

		Date	d = FeedUtils.adjustFutureItemDate( inItem, inItemDate, itemAgeMSecs);

// System.out.println( "===>\t\t" + d);

		return new AgeResult( inCurrentTimeMSecs - d.getTime());
	}

	/*******************************************************************************
	*******************************************************************************/
	private AddResult _processItem( Headlines ioHeadlines, ItemIF inItem, long inAgeMSecs, ItemContext inCtxt)
	{
		if (ioHeadlines.isItemAgeOK(inAgeMSecs))
		{
			return ioHeadlines.put( inItem, inCtxt);
		}

		return AddResult.FAILED_BAD_DATE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean registerChannel( ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		// s_Poll_Logger.info( m_Installation.getLogPrefix() + "Poller: registering " + inChannel + " @ " + inCurrentTimeMSecs);

		if ( inChannel == null)
		{
			s_Poll_Logger.error( m_Installation.getLogPrefix() + "Poller.registerChannel: inChannel is NULL");
			return false;
		}

		s_InformaPoller.registerChannel( inChannel, /* Note Channel polling every 5 minutes! */ 5 * ONE_MINUTE_MSECS);

		_processChannelItems( inChannel, inCurrentTimeMSecs);

		return true;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public boolean registerCommentsChannel( ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		s_InformaPoller.registerChannel( inChannel, /* Note Channel polling every 5 minutes! */ 5 * ONE_MINUTE_MSECS);

		_processCommentChannelItems( inChannel, inCurrentTimeMSecs);

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean unregisterChannel( ChannelIF inChannel)
	{
		s_Poll_Logger.info("Poller: UN-registering " + inChannel);

		s_InformaPoller.unregisterChannel(inChannel);

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	private class MyObserver implements PollerObserverIF
	{
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
			;
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
			s_Poll_Logger.warn( m_Installation.getLogPrefix() + "... ERROR for " + inChannel + "! ", inE);
		}

		/*******************************************************************************
		*******************************************************************************/
		public void itemFound( ItemIF inItem, ChannelIF ioChannel)
		{
			long		currTimeMSecs = System.currentTimeMillis();
			URL		theChannelLoc = ioChannel.getLocation();
			Date		itemDate = FeedUtils.getItemDate(inItem);
			AgeResult	theAgeResult = _getItemAgeMsecs( inItem, itemDate, currTimeMSecs);

			//////////////////////////////////////////////////////////////////  (AGR) 10 Sep 2006

			Site		thisChannelsSite = m_Installation.getFeedList().lookupChannel(ioChannel);	// Moved up...
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

			if (ITEMS_PREPEND_INSTALL_NAME)
			{
				inItem.setTitle( m_Installation.getLogPrefix() + "- " + inItem.getTitle());
				// System.out.println("@@@  " + inItem.getTitle() + " / " + inItem.hashCode());
			}

			//////////////////////////////////////////////////////////////////

			// s_Poll_Logger.info("... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - add it");

			ioChannel.addItem(inItem);

			//////////////////////////////////////////////////////////////////  (AGR) 19 May 2005

			CategoriesTable		theCatsTable = m_Installation.getCategories();

			if (( theCatsTable != null) && ( itemDate != null) && theAgeResult.getAgeMSecs() < CategoriesTable.getMaxPermissibleItemAge())
			{
				theCatsTable.addCategories(inItem);
			}

			//////////////////////////////////////////////////////////////////  (AGR) 30 Nov 2005. A comment or a post???

//			Site	thisChannelsSite = m_Installation.getFeedList().lookupChannel(ioChannel);

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

			HeadlinesMgr	theHMgr = m_Installation.getHeadlinesMgr();
			HeadlineFilter	theFilter = new HeadlineFilter( m_Installation, ioChannel);

			for ( Headlines	h : theHMgr.getHeadlinesList())
			{
				if (( itemIsAPost && !h.allowsPosts()) ||
				   ( !itemIsAPost && !h.allowsComments()))
				{
					continue;
				}

				//////////////////////////////////////////////////////////////////  (AGR) 21 March 2006

				if (!theFilter.filterMessage( h, inItem))
				{
					// s_Poll_Logger.info( m_Installation.getLogPrefix() + ":: Filtering out.b... \"" + inItem + "\"");
					continue;
				}

				//////////////////////////////////////////////////////////////////

				AddResult	itemResult;

				if ( itemDate == null)
				{
					itemResult = AddResult.FAILED_NO_DATE;
				}
				else	itemResult = _processItem( h, inItem, theAgeResult.getAgeMSecs(), ItemContext.UPDATE);

				//////////////////////////////////////////////////////////////////

				if ( itemResult == AddResult.SUCCEEDED)
				{
//					s_Poll_Logger.info( m_Installation.getLogPrefix() + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - ADDED OK to " + h);
				}
				else if ( itemResult == AddResult.FAILED_NO_DATE)
				{
					s_Poll_Logger.warn( m_Installation.getLogPrefix() + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED - Date is NULL, for " + h);
				}
				else if ( itemResult == AddResult.FAILED_BAD_DATE)
				{
					// s_Poll_Logger.warn( m_Installation.getLogPrefix() + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED - Date out of range! " + m_LastInfoMsg);
				}
				else if ( itemResult == AddResult.FAILED_GENERAL)
				{
					s_Poll_Logger.warn( m_Installation.getLogPrefix() + "... found \"" + FeedUtils.adjustTitle(inItem) + "\" for \"" + FeedUtils.channelToString(ioChannel) + "\" - FAILED, somehow, for " + h);
				}
				else if ( itemResult == AddResult.FAILED_DUPLICATE)	// (AGR) 3 March 2006
				{
					// No need for an error - already done by Headlines!
				}
			}
		}
	}

	/*******************************************************************************
		(AGR) 14 Jan 2006

		Why? Because some item ages are no longer permissible (items a week or
		more in the future)
	*******************************************************************************/
	private class AgeResult
	{
		private long		m_AgeMSecs;
		private boolean		m_Allowable;

		/*******************************************************************************
		*******************************************************************************/
		public AgeResult( long inMSecs)
		{
			m_AgeMSecs = inMSecs;
			m_Allowable = FeedUtils.isAcceptableFutureDate(inMSecs) &&
					/* (AGR) 3 March 2006 */ inMSecs <= CategoriesTable.getMaxPermissibleItemAge();

/*			if (!m_Allowable)
			{
				s_Poll_Logger.info("Skipping future post: " + FeedUtils.getAgeDifferenceString(inMSecs));
			}
*/		}

		/*******************************************************************************
		*******************************************************************************/
		public AgeResult( long inMSecs, boolean isAllowable)
		{
			m_AgeMSecs = inMSecs;
			m_Allowable = isAllowable;
		}

		/*******************************************************************************
		*******************************************************************************/
		public long getAgeMSecs()
		{
			return m_AgeMSecs;
		}

		/*******************************************************************************
		*******************************************************************************/
		public boolean isAllowable()
		{
			return m_Allowable;
		}
	}
}
