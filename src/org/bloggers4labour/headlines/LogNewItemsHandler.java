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

import de.nava.informa.core.ItemIF;
import org.apache.log4j.Logger;
import org.bloggers4labour.*;

/**
 *
 * @author andrewre
 */
public class LogNewItemsHandler implements AddHandler
{
	/*******************************************************************************
	*******************************************************************************/
	public void onAdd( final Installation inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
	{
		String	channelStr = FeedUtils.channelToString( inItem.getChannel() );

		Logger.getLogger("Main").info("... New post: \"" + FeedUtils.getDisplayTitle(inItem) + "\" for \"" + channelStr + "\"");
	}
}