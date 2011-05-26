/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check.consume;

import org.bloggers4labour.feed.check.FeedCheckerNotificationIF;

/**
 *
 * @author andrewregan
 */
public interface FeedCheckerConsumerDelegateIF 
{
	void addFeedCheckListener( final FeedCheckerListenerIF inListener);
	void removeFeedCheckListener( final FeedCheckerListenerIF inListener);

	void notifyFeedCheckListeners( final FeedCheckerNotificationIF inNotification);
}