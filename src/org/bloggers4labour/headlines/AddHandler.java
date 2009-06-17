/*
 * AddHandler.java
 *
 * Created on June 25, 2005, 9:39 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.headlines;

import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewre
 */
public interface AddHandler extends Handler
{
	public void onAdd( final InstallationIF inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt);
}
