/*
 * LogNewCommentHandler.java
 *
 * Created on November 29, 2005, 8:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.headlines;

import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewre
 */
public class LogNewCommentHandler implements AddHandler
{
	/*******************************************************************************
	*******************************************************************************/
	public void onAdd( final InstallationIF inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
	{
		org.bloggers4labour.jsp.DisplayItem	d = new org.bloggers4labour.jsp.DisplayItem( inInstall, inItem, System.currentTimeMillis());

//		Logger.getLogger("B4L_NewPosts").info("... New comment: \"" + FeedUtils.getDisplayTitle(inItem) + "\" from \"" + FeedUtils.getCommentAuthor(inItem) + "\"");
		Logger.getLogger("B4L_NewPosts").info("... New comment: \"" + d.getCommentTitle() + "\" by \"" + d.getCommentAuthor() + "\"");
	}
}