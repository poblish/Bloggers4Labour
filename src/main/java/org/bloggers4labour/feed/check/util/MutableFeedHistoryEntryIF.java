/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check.util;

import org.bloggers4labour.feed.check.FeedCheckerNotificationIF;

/**
 *
 * @author andrewregan
 */
public interface MutableFeedHistoryEntryIF extends FeedHistoryEntryIF
{
	void setLastSuccess( final FeedCheckerNotificationIF x);
	void setLastFailure( final FeedCheckerNotificationIF x);
	void setLastTimeout( final FeedCheckerNotificationIF x);
}