/*
 * LogNewItemsHandler.java
 *
 * Created on July 12, 2005, 9:53 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.headlines;

import org.apache.log4j.Logger;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewre
 */
public class LogNewItemsHandler implements AddHandler
{
	/*******************************************************************************
	*******************************************************************************/
	public void onAdd( final InstallationIF inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
	{
		String	channelStr = FeedUtils.channelToString( inItem.getOurChannel() );

		org.bloggers4labour.jsp.DisplayItem	d = new org.bloggers4labour.jsp.DisplayItem( inInstall, inItem, System.currentTimeMillis());

		// Logger.getLogger("B4L_NewPosts").info("... New post: \"" + FeedUtils.getDisplayTitle(inItem) + "\" for \"" + channelStr + "\"");
//		Logger.getLogger("B4L_NewPosts").info("... New post @ " + new java.util.Date() + " ... " + FeedUtils.getItemDate(inItem)  + "..." + d.getDateString() + " ... \"" + FeedUtils.getDisplayTitle(inItem) + "\" for \"" + channelStr + "\"");
		Logger.getLogger("B4L_NewPosts").info("... New [" + inInstall.getName() + "] post @ " + new java.util.Date() + " ... " + FeedUtils.getItemDate(inItem)  + "..." + d.getDateString() + " ... \"" + FeedUtils.getDisplayTitle(inItem) + "\" for \"" + channelStr + "\" " + "(" + inItem.getLink().toString() + ")");
	}
}