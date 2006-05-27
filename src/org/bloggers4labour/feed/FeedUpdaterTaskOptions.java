/*
 * FeedUpdaterTaskOptions.java
 *
 * Created on May 25, 2005, 1:15 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.feed;

import java.beans.PropertyVetoException;
import org.bloggers4labour.options.AbstractTaskOptionsBean;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class FeedUpdaterTaskOptions extends AbstractTaskOptionsBean
{
	/*******************************************************************************
	*******************************************************************************/
	public FeedUpdaterTaskOptions()
	{
		try
		{
			setDelayMsecs( 5 * ONE_SECOND_MSECS);		// 5 seconds
			setPeriodMsecs( 10 * ONE_MINUTE_MSECS);		// Needn't be very frequent!
		}
		catch (PropertyVetoException e)
		{
			// e.printStackTrace();
		}
	}
}
