/*
 * Poller.java
 *
 * Created on 12 March 2005, 01:11
 */

package org.bloggers4labour.polling;

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
import org.bloggers4labour.*;
import org.bloggers4labour.activity.*;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.headlines.HeadlineFilter;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public abstract class Poller
{
//	static de.nava.informa.utils.poller.Poller		s_InformaPoller;

	protected Installation					m_Installation;
	private PollerObserverIF				m_Observer;

	private static Logger					s_Poll_Logger = Logger.getLogger( Poller.class );

	private static Item[]					s_EmptyItemsArray = new Item[0];

	private final static int				MAX_POSTS_PER_BLOG	= 40;	// (AGR) 3 April 2005. Raised from 10 because of MadMusingsOfMe !
	private final static int				MAX_COMMENTS_PER_POST	= 50;	// (AGR) 29 Nov 2005

	private final static long				MAX_AGE_FOR_RECENT_POST = Constants.ONE_DAY_MSECS * 14;

	final static boolean					ITEMS_PREPEND_INSTALL_NAME = false;	// (AGR) 2 March 2006

	/*******************************************************************************
	*******************************************************************************/
	public abstract void startPolling();
	public abstract void setInstallation( final Installation inInstallation);
	public abstract de.nava.informa.utils.poller.Poller getInformaPoller();

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void cancelPolling()
	{
		if ( m_Observer != null)
		{
			s_Poll_Logger.info( m_Installation.getLogPrefix() + "Poller: removing Observer: " + m_Observer + " and stopping");

			getInformaPoller().removeObserver(m_Observer);

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
				AgeResult	theAgeResult = Util.getItemAgeMsecs( theItemsArray[i], itemDate, inCurrentTimeMSecs);

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
					else	theResult = Util.processItem( h, theItemsArray[i], theAgeResult.getAgeMSecs(), ItemContext.SNAPSHOT);

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
			AgeResult	theAgeResult = Util.getItemAgeMsecs( theItemsArray[i], itemDate, inCurrentTimeMSecs);

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
				else	theResult = Util.processItem( h, theItemsArray[i], theAgeResult.getAgeMSecs(), ItemContext.SNAPSHOT);

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
	public boolean registerChannel( ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		s_Poll_Logger.debug( m_Installation.getLogPrefix() + "Registering: " + inChannel);

		if ( inChannel == null)
		{
			s_Poll_Logger.error( m_Installation.getLogPrefix() + "Poller.registerChannel: inChannel is NULL");
			return false;
		}

		getInformaPoller().registerChannel(inChannel);	// (AGR) 29 October 2006. Removed custom schedule so each uses the configured value for the Poller

		_processChannelItems( inChannel, inCurrentTimeMSecs);

		return true;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public boolean registerCommentsChannel( ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		getInformaPoller().registerChannel(inChannel);	// (AGR) 29 October 2006. Removed custom schedule so each uses the configured value for the Poller

		_processCommentChannelItems( inChannel, inCurrentTimeMSecs);

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean unregisterChannel( ChannelIF inChannel)
	{
		s_Poll_Logger.info("Poller: UN-registering " + inChannel);

		getInformaPoller().unregisterChannel(inChannel);

		return true;
	}
}
