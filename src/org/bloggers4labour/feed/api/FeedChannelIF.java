/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.api;

import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public interface FeedChannelIF 
{
	String getURL();
	ChannelIF getChannel();
}