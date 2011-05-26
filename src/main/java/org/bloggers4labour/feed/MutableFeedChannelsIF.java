/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import org.bloggers4labour.feed.api.FeedChannelsIF;
import org.bloggers4labour.InstallationIF;

/**
 *
 * @author andrewregan
 */
public interface MutableFeedChannelsIF extends FeedChannelsIF
{
	public ConnectResult connectTo( final InstallationIF inInstall, int inThreadID, String inURL);
	public void clear();
}