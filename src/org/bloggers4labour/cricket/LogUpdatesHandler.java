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
import java.util.regex.*;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;
import org.bloggers4labour.headlines.AddHandler;
import org.bloggers4labour.headlines.HeadlinesIF;

/**
 *
 * @author andrewre
 */
public class LogUpdatesHandler implements AddHandler
{
	private static Pattern	s_MatchPattern = Pattern.compile("([A-Za-z]* *[A-Za-z]+) (.*) v ([A-Za-z]* *[A-Za-z]+) (.*)");

	/*******************************************************************************
	*******************************************************************************/
	public void onAdd( final Installation inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
	{
		String	channelStr = FeedUtils.channelToString( inItem.getChannel() );
		String	scoreStr = inItem.getTitle();
		Matcher	m = s_MatchPattern.matcher(scoreStr);

		org.bloggers4labour.jsp.DisplayItem	d = new org.bloggers4labour.jsp.DisplayItem( inInstall, inItem, System.currentTimeMillis());

		if (m.find())
		{
			Logger.getLogger("Main").info("... New post @ " + new java.util.Date() + " ... " + FeedUtils.getItemDate(inItem)  + "..." + d.getDateString() + " ... " + m.group(1) + " are " + m.group(2) + ", against " + m.group(3) + " who got " + m.group(4));
		}
		else
		{
			Logger.getLogger("Main").info("... New post @ " + new java.util.Date() + " ... " + FeedUtils.getItemDate(inItem)  + "..." + d.getDateString() + " ... \"" + FeedUtils.getDisplayTitle(inItem) + "\"");
		}
	}
}