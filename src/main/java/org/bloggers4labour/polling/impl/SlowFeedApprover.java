/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling.impl;

import org.apache.log4j.Logger;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.polling.api.PollerFeedApproverIF;

/**
 *
 * @author andrewregan
 */
public class SlowFeedApprover implements PollerFeedApproverIF
{
	private static Logger		s_Logger = Logger.getLogger( SlowFeedApprover.class );

	/*******************************************************************************
	*******************************************************************************/
	public boolean accept( final String inFeedURL, final ChannelIF inChannel)
	{
		if (!inFeedURL.startsWith("http://twitter.com/") && FeedUtils.isSlowFeed(inChannel))
		{
			s_Logger.debug( "::: Slow feed: " + inChannel + ", last updated @ " + FeedUtils.getLastPubDate(inChannel));
			return true;
		}

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return "SlowFeedApprover@" + Integer.toHexString(hashCode());
	}
}