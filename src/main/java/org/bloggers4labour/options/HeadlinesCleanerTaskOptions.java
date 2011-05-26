/*
 * HeadlinesCleanerTaskOptions.java
 *
 * Created on May 24, 2005, 11:46 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.options;

import java.beans.PropertyVetoException;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class HeadlinesCleanerTaskOptions extends AbstractTaskOptionsBean
{
	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public HeadlinesCleanerTaskOptions()
	{
		try
		{
			setDelayMsecs( 5 * ONE_SECOND_MSECS);
			setPeriodMsecs( 2 * ONE_MINUTE_MSECS);
		}
		catch (PropertyVetoException e)
		{
			// e.printStackTrace();
		}
	}
}
