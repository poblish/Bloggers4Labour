/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel;

/**
 *
 * @author andrewregan
 */
public interface ChannelBridgeIF 
{
	ChannelIF bridge( de.nava.informa.core.ChannelIF inOriginal);
	de.nava.informa.core.ChannelIF bridge( ChannelIF inOurs);
}