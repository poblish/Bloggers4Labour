/*
 * TimeEntry.java
 *
 * Created on 08 March 2006, 22:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.activity;

import java.util.HashSet;

/**
 *
 * @author andrewre
 */
public class TimeEntry implements TimerEntryIF
{
	private int				m_PostsCount;
	private int				m_BlogsUsed;

	private transient HashSet<String>	m_SiteURLs;

	/*******************************************************************************
	*******************************************************************************/
	public TimeEntry()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getPostsCount()
	{
		return m_PostsCount;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getBlogsUsed()
	{
		return m_BlogsUsed;
	}

	/*******************************************************************************
	*******************************************************************************/
	void storeBlogEntry( final String inOriginatingSiteURL)
	{
		if ( m_SiteURLs == null)
		{
			m_PostsCount = m_BlogsUsed = 1;

			m_SiteURLs = new HashSet<String>(10);
			m_SiteURLs.add(inOriginatingSiteURL);
		}
		else
		{
			m_PostsCount++;

			if (m_SiteURLs.add(inOriginatingSiteURL))	// a new entry?
			{
				m_BlogsUsed++;
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	void clear()
	{
		if ( m_SiteURLs != null)
		{
			m_SiteURLs.clear();
			m_SiteURLs = null;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return ("[" + m_BlogsUsed + " blogs, " + m_PostsCount + " posts]");
	}
}
