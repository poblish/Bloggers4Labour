/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel.item;

import org.bloggers4labour.bridge.channel.ChannelIF;

/**
 *
 * @author andrewregan
 */
public interface ItemBridgeIF 
{
	ItemIF bridge( de.nava.informa.core.ItemIF inOriginal);
	ItemIF bridge( de.nava.informa.core.ItemIF inOriginal, ChannelIF inChannel);
	de.nava.informa.core.ItemIF bridge( ItemIF inOurs);
}