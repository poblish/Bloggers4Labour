/*
 * Poller.java
 *
 * Created on 12 March 2005, 01:11
 */

package org.bloggers4labour.polling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.bloggers4labour.AddResult;
import org.bloggers4labour.AgeResult;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.Headlines;
import org.bloggers4labour.HeadlinesMgr;
import org.bloggers4labour.Installation;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.activity.LastPostTableIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.cats.CategoriesTableIF;
import org.bloggers4labour.headlines.HeadlineFilter;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public abstract class Poller implements PollerIF
{
	protected Installation		m_Installation;
	protected String		m_Name;

	private static Logger		s_Poll_Logger = Logger.getLogger( Poller.class );

	private static ItemIF[]		s_EmptyItemsArray = new ItemIF[0];

	private final static int	MAX_COMMENTS_PER_POST	= 50;	// (AGR) 29 Nov 2005

	final static boolean		ITEMS_PREPEND_INSTALL_NAME = false;	// (AGR) 2 March 2006

	/*******************************************************************************
	*******************************************************************************/
	protected Poller( String inName)
	{
		m_Name = inName;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void cancelPolling()
	{
/*		if ( m_Observer != null)
		{
			s_Poll_Logger.info( getLogPrefix() + "Poller: removing Observer: " + m_Observer + " and stopping");

			getInformaPoller().removeObserver(m_Observer);

			m_Observer = null;
		}
*/	}

	/*******************************************************************************
	*******************************************************************************/
	private void _processChannelItems( final SiteIF inSite, final ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		ItemIF[]	theItemsArray = _getChannelItemsArray(inChannel);

		if ( theItemsArray.length > 0)
		{
			HeadlinesMgr		theHMgr = m_Installation.getHeadlinesMgr();
			HeadlineFilter		theFilter = new HeadlineFilter( m_Installation, inChannel);

			LastPostTableIF		theLastPostTable = m_Installation.getLastPostDateTable();	// (AGR) 9 Sep 2006
			CategoriesTableIF	theCatsTable = m_Installation.getCategories();
			long			mostRecentPostAgeMsecs = Long.MIN_VALUE;			// (AGR) 26 Feb 2006
			List<ItemIF>		theItemsToRemoveFromChannel = new ArrayList<ItemIF>();		// (AGR) 3 March 2006
			int			datelessPostCount = 0;						// (AGR) 4 March 2006

			for ( int i = 0; i < theItemsArray.length; i++)
			{
				Date		itemDate = FeedUtils.getItemDate( theItemsArray[i] );
				AgeResult	theAgeResult = Util.getItemAgeMsecs( theItemsArray[i], itemDate, inCurrentTimeMSecs);

				//////////////////////////////////////////////////////////////////  (AGR) 26 Feb 2006, 9 Sep 2006

				if ( itemDate != null)	// (AGR) 25 Feb 2007. Don't bother trying to work out most recent, if we never found a valid post date!
				{
					long	adjustedPostMSecs = inCurrentTimeMSecs - theAgeResult.getAgeMSecs();

					if ( adjustedPostMSecs > mostRecentPostAgeMsecs)
					{
						mostRecentPostAgeMsecs = adjustedPostMSecs;
					}
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
					theItemsArray[i].setTitle( getLogPrefix() + "- " + theItemsArray[i].getTitle());
					// System.out.println("+++  " + theItemsArray[i].getTitle() + " / " + theItemsArray[i].hashCode());
				}

				//////////////////////////////////////////////////////////////////  (AGR) 19 May 2005

				if (( theCatsTable != null) && ( itemDate != null) && theAgeResult.getAgeMSecs() < CategoriesTableIF.MAX_CATEGORY_AGE_MSECS)
				{
					theCatsTable.addCategories( theItemsArray[i] );
				}

				//////////////////////////////////////////////////////////////////

				int	hIndex = 0;

				for ( Headlines	h : theHMgr.getHeadlinesList())
				{
					if (!h.allowsPosts())	// (AGR) 29 Nov 2005
					{
						continue;
					}

					///////////////////////////////////////////////////////

				/*	if ( hCountArray[hIndex] > MAX_POSTS_PER_BLOG)
					{
						continue;
					} */

					//////////////////////////////////////////////////////////////////  (AGR) 21 March 2006

					if (!theFilter.filterMessage( h, theItemsArray[i]))
					{
						// s_Poll_Logger.info( getLogPrefix() + ":: Filtering out.a... \"" + theItemsArray[i] + "\"");
						continue;
					}

					///////////////////////////////////////////////////////

				//	AddResult	theResult;

					if ( itemDate == null)
					{
						datelessPostCount++;

						// theResult = AddResult.FAILED_NO_DATE;
					}
					else
					{
						Util.processItem( h, theItemsArray[i], inSite, theAgeResult.getAgeMSecs(), ItemContext.SNAPSHOT);
					}

					///////////////////////////////////////////////////////

					hIndex++;
				}
			}

			_handleRemovalsFromChannel( inChannel, theItemsToRemoveFromChannel, true);

			////////////////////////////////////////////////////////  (AGR) 4 March 2006

			if ( datelessPostCount > 1)
			{
				s_Poll_Logger.info( getLogPrefix() + "WARNING. Found " + datelessPostCount + " undated posts in \"" + FeedUtils.channelToString(inChannel) + "\"");
			}
			else if ( datelessPostCount == 1)
			{
				s_Poll_Logger.info( getLogPrefix() + "WARNING. Found an undated post in \"" + FeedUtils.channelToString(inChannel) + "\"");
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
	private void _processCommentChannelItems( final SiteIF inSite, final ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		ItemIF[]	theItemsArray = _getChannelItemsArray(inChannel);
		HeadlinesMgr	theHMgr = m_Installation.getHeadlinesMgr();
		int[]		hCommentsCountArray = new int[ theHMgr.getHeadlinesCount() ];
		AddResult	theResult;
		List<ItemIF>	theItemsToRemoveFromChannel = new ArrayList<ItemIF>();	// (AGR) 3 March 2006

//		s_Poll_Logger.info( getLogPrefix() + "#### location = " + inChannel.getLocation());
//		s_Poll_Logger.info( getLogPrefix() + "####     site = " + inChannel.getSite());

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
					s_Poll_Logger.info( getLogPrefix() + "Cannot add date-less COMMENT for \"" + FeedUtils.channelToString(inChannel) + "\"");

					theResult = AddResult.FAILED_NO_DATE;
				}
				else	theResult = Util.processItem( h, theItemsArray[i], inSite, theAgeResult.getAgeMSecs(), ItemContext.SNAPSHOT);

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
				s_Poll_Logger.info( getLogPrefix() + "!!! " + ioList.size() + " posts removed from \"" + FeedUtils.channelToString(ioChannel) + "\"");
			}
			else	s_Poll_Logger.info( getLogPrefix() + "!!! " + ioList.size() + " comments removed from \"" + FeedUtils.channelToString(ioChannel) + "\"");
*/
			ioList.clear();
		}
	}

	/*******************************************************************************
		(AGR) 12 Dec 2005
		A result of seeing yet more ConcurrentModificationExceptions
	*******************************************************************************/
	@SuppressWarnings("unchecked")
	private synchronized ItemIF[] _getChannelItemsArray( final ChannelIF inChannel)
	{
		Collection<ItemIF>	c = inChannel.getItems();

		return ( c != null) ? c.toArray(s_EmptyItemsArray) : s_EmptyItemsArray;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean registerChannel( final SiteIF inSite, final ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		if ( inChannel == null)
		{
			s_Poll_Logger.error( getLogPrefix() + "Poller.registerChannel: inChannel is NULL");
			return false;
		}

		if (registerChannelWithInforma(inChannel))	// (AGR) 29 October 2006. Removed custom schedule so each uses the configured value for the Poller
		{
			_processChannelItems( inSite, inChannel, inCurrentTimeMSecs);
		}

		return true;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public boolean registerCommentsChannel( final SiteIF inSite, final ChannelIF inChannel, long inCurrentTimeMSecs)
	{
		if (registerChannelWithInforma(inChannel))	// (AGR) 29 October 2006. Removed custom schedule so each uses the configured value for the Poller
		{
			_processCommentChannelItems( inSite, inChannel, inCurrentTimeMSecs);
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean unregisterChannel( ChannelIF inChannel)
	{
		s_Poll_Logger.info( getLogPrefix() + "UN-registering " + inChannel);

		unregisterChannelWithInforma(inChannel);

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	protected String getLogPrefix()
	{
		return (( m_Installation != null) ? m_Installation.getLogPrefix() : "[???]") + "['" + m_Name + "'] ";
	}
}
