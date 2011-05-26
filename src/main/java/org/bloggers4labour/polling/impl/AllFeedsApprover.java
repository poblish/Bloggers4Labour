/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling.impl;

import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.polling.api.PollerFeedApproverIF;

/**
 *
 * @author andrewregan
 */
public class AllFeedsApprover implements PollerFeedApproverIF
{
	/*******************************************************************************
	*******************************************************************************/
	public boolean accept( final String inFeedURL, final ChannelIF inChannel)
	{
		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return "AllFeedsApprover@" + Integer.toHexString(hashCode());
	}
}