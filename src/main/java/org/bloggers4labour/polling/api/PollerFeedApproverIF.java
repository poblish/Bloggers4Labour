/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling.api;

import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public interface PollerFeedApproverIF 
{
	boolean accept( final String inFeedURL, final ChannelIF inChannel);
}