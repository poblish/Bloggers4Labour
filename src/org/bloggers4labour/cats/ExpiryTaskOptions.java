/*
 * ExpiryTaskOptions.java
 *
 * Created on May 25, 2005, 12:40 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.cats;

import java.beans.PropertyVetoException;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.options.AbstractTaskOptionsBean;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class ExpiryTaskOptions extends AbstractTaskOptionsBean
{
	/*******************************************************************************
	*******************************************************************************/
	public ExpiryTaskOptions()
	{
		try
		{
			setDelayMsecs( ONE_MINUTE_MSECS );
			setPeriodMsecs( 2 * ONE_MINUTE_MSECS);
		}
		catch (PropertyVetoException e)
		{
			// e.printStackTrace();
		}
	}
}
