/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling.impl;

import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.polling.api.PollerFeedApproverIF;

/**
 *
 * @author andrewregan
 */
public class TwitterFeedApprover implements PollerFeedApproverIF
{
	/*******************************************************************************
	*******************************************************************************/
	public boolean accept( final String inFeedURL, final ChannelIF inChannel)
	{
		return inFeedURL.startsWith("http://twitter.com/") && !FeedUtils.isSlowFeed(inChannel);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return "TwitterFeedApprover@" + Integer.toHexString(hashCode());
	}
}