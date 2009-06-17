/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel;

import java.net.URL;
import java.util.Collection;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewregan
 */
public interface ChannelIF extends de.nava.informa.core.ChannelIF
{
	URL getSite();
	URL getLocation();
	Collection<ItemIF> getItems();
	String getTitle();
	String getDescription();
	String getFormatString();

	// boolean addItem( ItemIF inItem);
	// boolean removeItem( ItemIF inItem);
}