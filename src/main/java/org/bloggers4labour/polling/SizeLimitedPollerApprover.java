/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import de.nava.informa.utils.poller.PollerApproverIF;
import java.util.Collection;
import java.util.Date;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.bridge.channel.item.DefaultItemBridgeFactory;
import org.bloggers4labour.bridge.channel.item.ItemBridgeIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.feed.MyLimitedChannelBuilder;

/**
 *
 * @author andrewregan
 */
public class SizeLimitedPollerApprover implements PollerApproverIF
{
	/*******************************************************************************
	*******************************************************************************/
	public boolean canAddItem( final de.nava.informa.core.ItemIF inItem, final de.nava.informa.core.ChannelIF ioChannel)
	{
		@SuppressWarnings("unchecked")
		Collection<ItemIF>	theColl = ioChannel.getItems();

		if ( theColl.size() < MyLimitedChannelBuilder.MAX_POSTS_PER_CHANNEL)
		{
			return true;
		}

		/////////////////////////////////////////////////////////////////////////

		ItemBridgeIF	theBridge = new DefaultItemBridgeFactory().getInstance();
		Date		theNewItemsDate = FeedUtils.getItemDate( theBridge.bridge(inItem) );

		if ( theNewItemsDate == null)
		{
			return true;	// Null dates? What can we do here? Let's just let all the posts in - there won't be many feeds like this.
		}

		/////////////////////////////////////////////////////////////////////////

		ItemIF	oldestItem = null;
		long	oldestPostMS = theNewItemsDate.getTime();

		/////////////////////////////////////////////////////////////////////////

		for ( ItemIF eachItem : theColl)
		{
			Date	theDate = FeedUtils.getItemDate(eachItem);

			if ( theDate == null)
			{
				return true;	// See comment above.
			}

			/////////////////////////////////////////////////////////////////////////

			long	currItemsTimeMS = theDate.getTime();

			if ( currItemsTimeMS < oldestPostMS)
			{
				oldestPostMS = currItemsTimeMS;
				oldestItem = eachItem;
			}
		}

		/////////////////////////////////////////////////////////////////////////

		if ( oldestItem != null)	// An existing Item is older. Remove it and let the new one in!
		{
			ioChannel.removeItem( theBridge.bridge(oldestItem) );

			return true;
		}

		// The 'new' Item is actually the oldest, so disapprove it

		return false;
	}
}