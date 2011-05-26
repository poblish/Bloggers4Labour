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
	public final static int	MAX_POSTS_PER_CHANNEL = 30;	// (AGR) 9 Jan 2011. Raised this to 50, but then restored to 30. Waste of resources.

//	private static Logger	s_Logger = Logger.getLogger( MyLimitedChannelBuilder.class );

	/*******************************************************************************
	*******************************************************************************/
	@Override public ItemIF createItem( final Element itemElement, final ChannelIF inChannel, final String inTitle, final String description, final URL inLink)
	{
		final ItemIF item = new Item( itemElement, inChannel, inTitle, description, inLink);

		if ( inChannel != null)
		{
			if ( inChannel.getItems().size() <= MAX_POSTS_PER_CHANNEL)
			{
				inChannel.addItem(item);
			}
		/*	else
			{
				s_Logger.warn("Too many articles (" + inChannel.getItems().size() + ") for " + inChannel.getSite() + ". Skip: " + inLink);
			} */
		}

		return item;
	}
}