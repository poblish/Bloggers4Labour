/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.api;

import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.feed.check.FeedCheckerAgentIF;

/**
 *
 * @author andrewregan
 */
public interface FeedChannelsIF extends FeedCheckerAgentIF
{
	Iterable<FeedChannelIF> getChannels();

	ChannelIF findURL( String inURL);
}