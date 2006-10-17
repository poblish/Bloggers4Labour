/**
 * StatsMBean.java
 *
 * Created on Wed Jun 01 00:21:55 BST 2005
 */
package org.bloggers4labour.jmx;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.bloggers4labour.*;

/**
 * Interface StatsMBean
 * B4L Stats
 * @author andrewre
 */
public interface StatsMBean
{
	/*******************************************************************************
	*******************************************************************************/
	public long getBlogsCount( final java.sql.Statement inS) throws java.sql.SQLException;
	public int getFeedCount();
	public int getSuccessfulFeedCount();	// (AGR) 7 October 2005
	public List getFailedFeedsList();	// (AGR) 7 October 2005
	public Date getLastUpdateTime();

	public int getBlogsUsedInLast24Hours();
	public int getRecentBlogsUsed();

	public int getPostsMadeInLast24Hours();
	public int getRecentPostsMade();

	public int getLinksFoundInLast24Hours();
	public int getRecentLinksFound();

	public int getRecentCommentsLeft();		// (AGR) 29 Nov 2005
	public List getFailedCommentFeedsList();	// " " "
	public int getSuccessfulCommentFeedCount();	// " " "
	public int getFailedCommentFeedCount();		// " " "
	public int getTimedOutCommentFeedCount();	// " " "

	public Date getStartupTime();

	public int getActiveSiteHandlerTasks();

	public int getRSSFeedItemCount();
	public Date getRSSFeedCompletionTime();
	public StringBuffer getRSSFeedCompletionInterval();

	public String[] getCurrentSiteNames();
	public List<Site> getCurrentSites();
	public int getReferencedItemsCount();
	public String getOPMLOutputStr();

	public Calendar getLastFeedCheckTime();

	// Operations

	public Site getSite( long inSiteRecno);
	public String getAgeDifferenceString( long inAgeDiffMSecs);
	public String stripHTML( String inStr);
}
