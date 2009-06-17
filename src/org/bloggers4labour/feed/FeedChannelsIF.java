/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public interface FeedChannelsIF 
{
	public ChannelIF findURL( String inURL);
	public ConnectResult connectTo( final InstallationIF inInstall, int inThreadID, String inURL);
	public void clear();
}