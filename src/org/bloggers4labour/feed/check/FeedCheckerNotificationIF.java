/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check;

import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public interface FeedCheckerNotificationIF 
{
	FeedCheckerAgentIF getAgent();

	FeedCheckResultIF getResult();

	ChannelIF getAffectedChannel();
	String getAffectedURL();

	long getTaskStartTime();
	long getTaskFinishTime();

	boolean wasSuccess();
	boolean wasFailure();
	boolean wasTimeout();
}