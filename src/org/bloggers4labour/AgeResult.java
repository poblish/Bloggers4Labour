/*
 * AgeResult.java
 *
 * Created on 26 October 2006, 01:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

import org.bloggers4labour.cats.CategoriesTable;

/**
 *
 * @author andrewre
 */
/*******************************************************************************
	(AGR) 14 Jan 2006

	Why? Because some item ages are no longer permissible (items a week or
	more in the future)
*******************************************************************************/
class AgeResult
{
	private long		m_AgeMSecs;
	private boolean		m_Allowable;

	/*******************************************************************************
	*******************************************************************************/
	public AgeResult( long inMSecs)
	{
		m_AgeMSecs = inMSecs;
		m_Allowable = FeedUtils.isAcceptableFutureDate(inMSecs) &&
				/* (AGR) 3 March 2006 */ inMSecs <= CategoriesTable.getMaxPermissibleItemAge();

/*			if (!m_Allowable)
		{
			s_Poll_Logger.info("Skipping future post: " + FeedUtils.getAgeDifferenceString(inMSecs));
		}
*/		}

	/*******************************************************************************
	*******************************************************************************/
	public AgeResult( long inMSecs, boolean isAllowable)
	{
		m_AgeMSecs = inMSecs;
		m_Allowable = isAllowable;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getAgeMSecs()
	{
		return m_AgeMSecs;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isAllowable()
	{
		return m_Allowable;
	}
}
