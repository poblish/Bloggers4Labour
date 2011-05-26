/*
 * SampleExpiryHandler.java
 *
 * Created on July 10, 2005, 9:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.headlines;

import org.apache.log4j.Logger;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewre
 */
public class SampleExpiryHandler implements ExpiryHandler
{
	private static Logger		s_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************/
	public void onExpire( HeadlinesIF inHeads, final ItemIF inItem)
	{
		s_Logger.info("@ Removing stale " + inItem);
//		s_Logger.info("@ Removing stale " + inItem + " ... from " + inHeads);
	}
}
