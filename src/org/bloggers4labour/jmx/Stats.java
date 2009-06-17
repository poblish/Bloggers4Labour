/**
 * Stats.java
 *
 * Created on Wed Jun 01 00:21:55 BST 2005
 */
package org.bloggers4labour.jmx;

import com.hiatus.locales.ULocale2;
import com.hiatus.sql.USQL_Utils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;
import org.bloggers4labour.headlines.HeadlinesIF;
import org.bloggers4labour.site.SiteIF;
import org.bloggers4labour.sql.DataSourceConnection;
import org.bloggers4labour.sql.QueryBuilder;

/**
 * Class Stats
 * B4L Stats
 * @author andrewre
 */
public class Stats implements StatsMBean
{
	private Date			m_StartupTime = new Date();

	private Date			m_LastUpdateTime;
	private long			m_BlogsCount = -1;
	private int			m_FeedCount;
	private int			m_SuccessfulFeedCount;		// (AGR) 7 October 2005
	private List			m_FailedFeedsList;		// " " "

	private int			m_SuccessfulCommentFeedCount;	// (AGR) 29 Nov 2005
	private int			m_FailedCommentFeedsCount;	// " " "
	private int			m_TimedOutCommentFeedsCount;	// " " "

	private List			m_FailedCommentFeedsList;	// " " "
	private int			m_RSSFeed_Count = -1;
	private StringBuffer		m_RSSFeed_Duration;
	private Date			m_RSSFeed_Completed;
	private int			m_ActiveSiteHandlerTasks;
	private Calendar		m_LastFeedCheckTime;		// (AGR) 23 July 2005

	private Installation		m_Install;			// (AGR) 19 Feb 2006, (AGR) 29 Jan 2007. Removed pointless 'transient'

	private static Logger		s_Stats_Logger = Logger.getLogger("Main");
	private static TimeZone		s_TZ = ULocale2.getBestTimeZone( Locale.UK );

	/*******************************************************************************
	*******************************************************************************/
	public Stats( Installation inInstall)
	{
		m_Install = inInstall;

		///////////////////////////////

		DataSourceConnection	theConnectionObject = null;

		try
		{
			theConnectionObject = new DataSourceConnection( m_Install.getDataSource() );
			if (theConnectionObject.Connect())
			{
				Statement	theS = null;

				try
				{
					theS = theConnectionObject.createStatement();

					getBlogsCount(theS);
				}
				catch (SQLException e)
				{
					s_Stats_Logger.error("creating statement", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theS);
				}
			}
			else
			{
				s_Stats_Logger.warn("Cannot connect!");
			}
		}
		catch (RuntimeException err)
		{
			s_Stats_Logger.error("???", err);
		}
		finally
		{
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
			}
		}
	}

	/*******************************************************************************
		(AGR) 15 October 2006
	*******************************************************************************/
	public long getBlogsCount( final Statement inS) throws java.sql.SQLException
	{
		ResultSet	theRS = null;

		try
		{
			theRS = inS.executeQuery( QueryBuilder.getBlogsTotalQuery() );
			if (theRS.next())
			{
				m_BlogsCount = theRS.getLong(1);

				return m_BlogsCount;
			}
		}
		finally
		{
			USQL_Utils.closeResultSetCatch(theRS);
		}

		return -1;	//Don't ever return a cached value from this function
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getCachedBlogsCount()
	{
		return m_BlogsCount;
	}

	/*******************************************************************************
		(AGR) 3 Feb 2007. FindBugs said I should sync in accordance with the
		corresponding set...() method. Eeek!
	*******************************************************************************/
	public synchronized int getFeedCount()
	{
		return m_FeedCount;
	}

	/*******************************************************************************
		(AGR) 7 October 2005
		(AGR) 3 Feb 2007. FindBugs said I should sync in accordance with the
		corresponding set...() method. Eeek!
	*******************************************************************************/
	public synchronized int getSuccessfulFeedCount()
	{
		return m_SuccessfulFeedCount;
	}

	/*******************************************************************************
		(AGR) 7 October 2005
	*******************************************************************************/
	public List getFailedFeedsList()
	{
		return m_FailedFeedsList;
	}

	/*******************************************************************************
		(AGR) 7 October 2005
	*******************************************************************************/
	public void setFailedFeedsList( List inList)
	{
		m_FailedFeedsList = inList;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public void setFailedCommentFeedsList( List inList)
	{
		m_FailedCommentFeedsList = inList;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public List getFailedCommentFeedsList()
	{
		return m_FailedCommentFeedsList;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	public void setFailedCommentFeedCount( int x)
	{
		m_FailedCommentFeedsCount = x;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	public int getFailedCommentFeedCount()
	{
		return m_FailedCommentFeedsCount;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	public void setTimedOutCommentFeedCount( int x)
	{
		m_TimedOutCommentFeedsCount = x;
	}

	/*******************************************************************************
		(AGR) 30 Nov 2005
	*******************************************************************************/
	public int getTimedOutCommentFeedCount()
	{
		return m_TimedOutCommentFeedsCount;
	}

	/*******************************************************************************
		(AGR) 3 Feb 2007. FindBugs said I should sync in accordance with the
		corresponding set...() method. Eeek!
	*******************************************************************************/
	public synchronized int getSuccessfulCommentFeedCount()
	{
		return m_SuccessfulCommentFeedCount;
	}

	/*******************************************************************************
		(AGR) 29 Nov 2005
	*******************************************************************************/
	public synchronized void setSuccessfulCommentFeedCount( int inCount)
	{
		m_SuccessfulCommentFeedCount = inCount;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getBlogsUsedInLast24Hours()
	{
		HeadlinesIF	theHeads = m_Install.getHeadlinesMgr().get24HourInstance();

		return ( theHeads != null) ? theHeads.getBlogsCount() : -1;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getRecentBlogsUsed()
	{
		return m_Install.getHeadlinesMgr().getRecentPostsInstance().getBlogsCount();
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getPostsMadeInLast24Hours()
	{
		HeadlinesIF	theHeads = m_Install.getHeadlinesMgr().get24HourInstance();

		return ( theHeads != null) ? theHeads.size() : -1;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getRecentPostsMade()
	{
		return m_Install.getHeadlinesMgr().getRecentPostsInstance().size();
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getLinksFoundInLast24Hours()
	{
		HeadlinesIF	theHeads = m_Install.getHeadlinesMgr().get24HourInstance();

		return ( theHeads != null) ? theHeads.countLinks() : -1;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getRecentCommentsLeft()
	{
		return m_Install.getHeadlinesMgr().getCommentsInstance().size();
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getRecentLinksFound()
	{
		return m_Install.getHeadlinesMgr().getRecentPostsInstance().countLinks();
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void setFeedCount( int inCount)
	{
		m_FeedCount = inCount;
		m_LastUpdateTime = new Date();
	}

	/*******************************************************************************
		(AGR) 7 October 2005
	*******************************************************************************/
	public synchronized void setSuccessfulFeedCount( int inCount)
	{
		m_SuccessfulFeedCount = inCount;
		m_LastUpdateTime = new Date();
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized Date getLastUpdateTime()
	{
		return (Date) m_LastUpdateTime.clone();		// FindBugs recommended this...
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void setLastUpdateTime( Date x)
	{
		m_LastUpdateTime = (Date) x.clone();		// FindBugs recommended this...
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void setRSSFeedDetails( int inCount, long inDoneMS, StringBuffer inTimeStr)
	{
		m_RSSFeed_Count = inCount;
		m_RSSFeed_Duration = inTimeStr;
		m_RSSFeed_Completed = new Date(inDoneMS);
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized int getRSSFeedItemCount()
	{
		return m_RSSFeed_Count;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Date getRSSFeedCompletionTime()
	{
		return (Date) m_RSSFeed_Completed.clone();		// FindBugs recommended this...
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuffer getRSSFeedCompletionInterval()
	{
		return m_RSSFeed_Duration;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Date getStartupTime()
	{
		return (Date) m_StartupTime.clone();		// FindBugs recommended this...
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setActiveSiteHandlerTasks( int x)
	{
		m_ActiveSiteHandlerTasks = x;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getActiveSiteHandlerTasks()
	{
		return m_ActiveSiteHandlerTasks;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String[] getCurrentSiteNames()
	{
		SiteIF[]	theSitesArray;

		synchronized (this)
		{
			List<SiteIF>	theList = m_Install.getFeedList().getSites();

			theSitesArray = theList.toArray( new SiteIF[ theList.size() ] );
		}

		java.util.Arrays.sort(theSitesArray);	// by recno

		String[]	theResult = new String[ theSitesArray.length ];
		int		i = 0;

		for ( SiteIF s : theSitesArray)
		{
			theResult[i++] = ((Site) s).toMiniString();
		}

		return theResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	public List<SiteIF> getCurrentSites()
	{
		return m_Install.getFeedList().getSites();
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getReferencedItemsCount()
	{
		return m_Install.getFeedList().countReferencedItems();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getOPMLOutputStr()
	{
		return m_Install.getFeedList().getOPMLOutputStr();
	}

	/*******************************************************************************
		(AGR) 23 July 2005
	*******************************************************************************/
	public Calendar getLastFeedCheckTime()
	{
		return m_LastFeedCheckTime;
	}

	/*******************************************************************************
		(AGR) 23 July 2005
	*******************************************************************************/
	public void setLastFeedCheckTimeNow()
	{
		m_LastFeedCheckTime = Calendar.getInstance( s_TZ, Locale.UK);
	}

	/*******************************************************************************
		(AGR) 23 July 2005
	*******************************************************************************/
	public void setLastFeedCheckTime( Calendar x)
	{
		m_LastFeedCheckTime = x;
	}

	/*******************************************************************************
		(AGR) 6 June 2005. My first Operation
	*******************************************************************************/
	public SiteIF getSite( long inSiteRecno)
	{
		return m_Install.getFeedList().lookup(inSiteRecno);
	}

	/*******************************************************************************
		(AGR) 6 June 2005. My 2nd Operation
	*******************************************************************************/
	public String getAgeDifferenceString( long inAgeDiffMSecs)
	{
		return FeedUtils.getAgeDifferenceString(inAgeDiffMSecs);
	}

	/*******************************************************************************
		(AGR) 6 June 2005. My 3rd Operation
	*******************************************************************************/
	public String stripHTML( String inStr)
	{
		return FeedUtils.stripHTML(inStr);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return "[Stats for \"" + m_Install.getName() + "\"]";
	}
}
