/*
 * ExpiryHandler.java
 *
 * Created on June 26, 2005, 10:56 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.headlines;

import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewre
 */
public interface ExpiryHandler extends Handler
{
	public void onExpire( HeadlinesIF inHeads, final ItemIF inItem);
}
