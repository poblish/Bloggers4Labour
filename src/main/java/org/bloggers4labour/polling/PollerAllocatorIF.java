/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public interface PollerAllocatorIF 
{
	Iterable<PollerIF> allocate( final String inFeedURL, final ChannelIF inChannel);

	public void success( final String inFeedURL, final ChannelIF inChannel);
	public void failed( final String inFeedURL);
	public void timedOut( final String inFeedURL);
}
