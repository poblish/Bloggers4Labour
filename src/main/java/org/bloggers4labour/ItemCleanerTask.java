/*
 * ItemCleanerTask.java
 *
 * Created on May 20, 2005, 10:36 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

import java.util.Date;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewre
 */
public abstract class ItemCleanerTask extends TimerTask
{
	private long			m_MaxItemAgeMS;

	private static Logger		s_Task_Logger = Logger.getLogger( ItemCleanerTask.class );

	/*******************************************************************************
	*******************************************************************************/
	public ItemCleanerTask( long inMaxItemAgeMS)
	{
		m_MaxItemAgeMS = inMaxItemAgeMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void info( Object inStr)
	{
		s_Task_Logger.info(inStr);
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isOutOfDate( long inCurrentTimeMS, final ItemIF inItem)
	{
		Date	theItemDate = FeedUtils.getItemDate(inItem);	// (AGR) 13 March 2005

		if ( theItemDate != null)
		{
			long	itemAgeMSecs = inCurrentTimeMS - theItemDate.getTime();

			// info("itemAgeMSecs = " + itemAgeMSecs  + ", inItem = " + inItem);

			return ( /* itemAgeMSecs < 0 || (AGR) 3 April 2005 - commented out */ itemAgeMSecs >= m_MaxItemAgeMS);
		}
		else
		{
			return true;	// shouldn't have undated items
		}
	}
}
