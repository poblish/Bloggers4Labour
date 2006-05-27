/*
 * ActivityTable.java
 *
 * Created on 08 March 2006, 22:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.activity;

import org.bloggers4labour.cats.CategoriesTable;
import static org.bloggers4labour.Constants.*;

/**
 *
 * @author andrewre
 */
public class ActivityTable
{
	private int		m_TotalPosts;

	private TimeEntry[]	m_Entries = new TimeEntry[ (int)( CategoriesTable.getMaxPermissibleItemAge() / ONE_HOUR_MSECS) ];

	/*******************************************************************************
	*******************************************************************************/
	public ActivityTable()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public void store( long inPostAgeMsecs, final String inOriginatingSiteURL)
	{
		int	x = (int)( inPostAgeMsecs / ONE_HOUR_MSECS);
// System.out.println("age = " + inPostAgeMsecs + ", url = " + inOriginatingSiteURL + ", x = " + x);
		if ( m_Entries[x] == null)
		{
			m_Entries[x] = new TimeEntry();
		}

		m_Entries[x].storeBlogEntry(inOriginatingSiteURL);
		m_TotalPosts++;
	}

	/*******************************************************************************
	*******************************************************************************/
	public TimerEntryIF[] getEntries()
	{
		return m_Entries;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void complete()
	{
		for ( TimeEntry eachEntry : m_Entries)
		{
			if ( eachEntry != null)
			{
				eachEntry.clear();
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		StringBuilder	sb = new StringBuilder("Total = " + m_TotalPosts);
		int		i = 0;

		for ( TimeEntry eachEntry : m_Entries)
		{
			if ( eachEntry != null)
			{
				if ( sb.length() > 0)
				{
					sb.append(", ");
				}

				sb.append( Integer.toString(i) + "-" + (i+1) + " hrs: " + eachEntry);
			}

			i++;
		}

		return sb.toString();
	}
}