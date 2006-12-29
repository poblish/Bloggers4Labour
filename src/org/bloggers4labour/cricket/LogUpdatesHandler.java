/*
 * LogUpdatesHandler.java
 *
 * Created on 24 October 2006, 16:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.cricket;

import de.nava.informa.core.ItemIF;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;
import org.bloggers4labour.headlines.AddHandler;
import org.bloggers4labour.headlines.HeadlinesIF;
import org.bloggers4labour.jsp.DisplayItem;

/**
 *
 * @author andrewre
 */
public class LogUpdatesHandler implements AddHandler
{
	/*******************************************************************************
	*******************************************************************************/
	public void onAdd( final Installation inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
	{
		DisplayItem	d = new DisplayItem( inInstall, inItem, System.currentTimeMillis());
		String		channelStr = FeedUtils.channelToString( inItem.getChannel() );
		String		scoreStr = inItem.getDescription().trim();
		Score		theScore = Score.parse( inItem.getDescription().trim() );

		Logger.getLogger("B4L_NewPosts").info("... New post @ " + new java.util.Date() + " ... " + FeedUtils.getItemDate(inItem)  + " ... " + d.getDateString() + " ... " + theScore);
	}
}