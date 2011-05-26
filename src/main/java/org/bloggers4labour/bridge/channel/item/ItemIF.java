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
public interface ItemIF extends Cloneable, de.nava.informa.core.ItemIF
{
	String	DUMMY_ITEM_CONTENT = "*DUMMY*";

	///////////////////////////////////////

	ItemIF clone();

	ChannelIF getOurChannel();

	String getAuthorName();

	boolean matchesTitleAndLink( final ItemIF inOther);
	boolean matchesDescription( final ItemIF inOther);
}