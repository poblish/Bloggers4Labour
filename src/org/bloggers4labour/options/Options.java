/*
 * Options.java
 *
 * Created on May 24, 2005, 11:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.options;

import org.bloggers4labour.cats.ExpiryTaskOptions;
import org.bloggers4labour.feed.FeedUpdaterTaskOptions;

/**
 *
 * @author andrewre
 */
public class Options
{
/*
	public final static boolean	USE_POLLER = true;			// MUST BE true
	public final static boolean	STORE_CATEGORIES = true;		// MUST BE true
	public final static boolean	CATEGORY_STORE_ITEM_CLONE = true;	// false is SAFER
	public final static boolean	HEADLINES_BAN_ADDING = false;		// MUST BE false

	public final static boolean	CLEANER_MEMORY_CHECKS = false;		// MUST BE false
*/

	private TaskOptionsBeanIF	m_HeadlinesCleanerTaskOptions;
	private TaskOptionsBeanIF	m_ExpiryTaskOptions;
	private TaskOptionsBeanIF	m_FeedUpdaterTaskOptions;
	private boolean			m_IsStoringCategories;
	private int			m_NumSiteHandlerThreads;

	private static Options		s_Opts = new Options();

	/*******************************************************************************
	*******************************************************************************/
	public Options()
	{
		m_HeadlinesCleanerTaskOptions = new HeadlinesCleanerTaskOptions();
		m_ExpiryTaskOptions = new ExpiryTaskOptions();
		m_FeedUpdaterTaskOptions = new FeedUpdaterTaskOptions();

		m_IsStoringCategories = true;
		m_NumSiteHandlerThreads = 12;	// (AGR) 18 Feb 2007. Increased from 6. Cut startup time by 40%!
	}

	/*******************************************************************************
	*******************************************************************************/
	public static Options getOptions()
	{
		return s_Opts;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setIsStoringCategories( boolean inVal)
	{
		m_IsStoringCategories = inVal;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean getIsStoringCategories()
	{
		return m_IsStoringCategories;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setNumSiteHandlerThreads( int inVal)
	{
		m_NumSiteHandlerThreads = inVal;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getNumSiteHandlerThreads()
	{
		return m_NumSiteHandlerThreads;
	}

	/*******************************************************************************
	*******************************************************************************/
	public TaskOptionsBeanIF getHeadlinesCleanerTaskOptions()
	{
		return m_HeadlinesCleanerTaskOptions;
	}

	/*******************************************************************************
	*******************************************************************************/
	public TaskOptionsBeanIF getExpiryTaskOptions()
	{
		return m_ExpiryTaskOptions;
	}

	/*******************************************************************************
	*******************************************************************************/
	public TaskOptionsBeanIF getFeedUpdaterTaskOptions()
	{
		return m_FeedUpdaterTaskOptions;
	}
}
