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
	ItemIF clone();

	ChannelIF getOurChannel();

	String getAuthorName();

	boolean matchesTitleAndLink( final ItemIF inOther);

/*	long getId();

	String getElementValue( String inName);
	String getSubject();
	String getDescription();
	ArrayList getCategories();
	URL getLink();

	String getTitle();
	void setTitle( String inTitle);

	Date getDate();
	void setDate( Date inDate); */
}
