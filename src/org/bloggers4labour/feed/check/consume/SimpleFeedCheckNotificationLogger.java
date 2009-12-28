/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check.consume;

import org.apache.log4j.Logger;
import org.bloggers4labour.feed.check.FeedCheckerNotificationIF;
import org.bloggers4labour.feed.check.util.DefaultFeedHistory;
import org.bloggers4labour.feed.check.util.FeedHistoryIF;

/**
 *
 * @author andrewregan
 */
public class SimpleFeedCheckNotificationLogger implements FeedCheckerListenerIF
{
	private FeedHistoryIF	m_History = new DefaultFeedHistory();

	private static Logger	s_Logger = Logger.getLogger( SimpleFeedCheckNotificationLogger.class );

	/*******************************************************************************
	*******************************************************************************/
	public boolean onNotify( final FeedCheckerNotificationIF inNotification)
	{
		boolean	addedEntry = m_History.onNotify(inNotification);

		if (addedEntry)
		{
			s_Logger.debug( inNotification + " ... " + m_History.size() + " entries");
		}

		return addedEntry;
	}
}