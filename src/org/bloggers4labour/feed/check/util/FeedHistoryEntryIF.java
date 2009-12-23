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
public interface FeedHistoryEntryIF 
{
	FeedCheckerNotificationIF getLastSuccess();
	FeedCheckerNotificationIF getLastFailure();
	FeedCheckerNotificationIF getLastTimeout();
}