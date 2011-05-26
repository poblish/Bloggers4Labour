/*
 * Constants.java
 *
 * Created on June 17, 2005, 11:46 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

/**
 *
 * @author andrewre
 */
public interface Constants
{
	public final static long		ONE_MINUTE_SECS  = 60;
	public final static long		ONE_HOUR_SECS    = 60 * ONE_MINUTE_SECS;
	public final static long		ONE_DAY_SECS     = 24 * ONE_HOUR_SECS;
	public final static long		ONE_WEEK_SECS    = 7 * ONE_DAY_SECS;

	public final static long		ONE_SECOND_MSECS = 1000;
	public final static long		ONE_MINUTE_MSECS = ONE_MINUTE_SECS * 1000;
	public final static long		ONE_HOUR_MSECS   = ONE_HOUR_SECS * 1000;
	public final static long		ONE_DAY_MSECS    = ONE_DAY_SECS * 1000;
	public final static long		ONE_WEEK_MSECS   = ONE_WEEK_SECS * 1000;

	public final static int			MAX_LIST_COUNT	 = 500;
}
