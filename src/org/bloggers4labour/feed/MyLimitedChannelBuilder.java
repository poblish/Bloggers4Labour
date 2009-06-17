/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bloggers4labour.feed;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import java.net.URL;
import org.jdom.Element;

/**
 *
 * @author andrewregan
 */
public class MyLimitedChannelBuilder extends ChannelBuilder
{
	public final static int	MAX_POSTS_PER_CHANNEL = 10;

	/*******************************************************************************
	*******************************************************************************/
	@Override public ItemIF createItem( Element itemElement, final ChannelIF inChannel, String inTitle, String description, URL link)
	{
		ItemIF item = new Item( itemElement, inChannel, inTitle, description, link);

		if ( inChannel != null && inChannel.getItems().size() < MAX_POSTS_PER_CHANNEL)
		{
			inChannel.addItem(item);
		}

		return item;
	}
}