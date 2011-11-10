/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bloggers4labour.jmx;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public class NullStats implements StatsMBean
{
	@Override
	public long getBlogsCount(Statement inS) throws SQLException
	{
		return -1;
	}

	@Override
	public int getFeedCount()
	{
		return -1;
	}

	@Override
	public int getSuccessfulFeedCount()
	{
		return -1;
	}

	@Override
	public List getFailedFeedsList()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Date getLastUpdateTime()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getBlogsUsedInLast24Hours()
	{
		return -1;
	}

	@Override
	public int getRecentBlogsUsed()
	{
		return -1;
	}

	@Override
	public int getPostsMadeInLast24Hours()
	{
		return -1;
	}

	@Override
	public int getRecentPostsMade()
	{
		return -1;
	}

	@Override
	public int getLinksFoundInLast24Hours()
	{
		return -1;
	}

	@Override
	public int getRecentLinksFound()
	{
		return -1;
	}

	@Override
	public int getRecentCommentsLeft()
	{
		return -1;
	}

	@Override
	public List getFailedCommentFeedsList()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getSuccessfulCommentFeedCount()
	{
		return -1;
	}

	@Override
	public int getFailedCommentFeedCount()
	{
		return -1;
	}

	@Override
	public int getTimedOutCommentFeedCount()
	{
		return -1;
	}

	@Override
	public Date getStartupTime()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getActiveSiteHandlerTasks()
	{
		return -1;
	}

	@Override
	public int getRSSFeedItemCount()
	{
		return -1;
	}

	@Override
	public Date getRSSFeedCompletionTime()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public StringBuffer getRSSFeedCompletionInterval()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String[] getCurrentSiteNames()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<SiteIF> getCurrentSites()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getReferencedItemsCount()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getOPMLOutputStr()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Calendar getLastFeedCheckTime()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SiteIF getSite(long inSiteRecno)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getAgeDifferenceString(long inAgeDiffMSecs)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String stripHTML(String inStr)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}