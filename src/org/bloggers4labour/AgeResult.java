/*
 * AgeResult.java
 *
 * Created on 26 October 2006, 01:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

/*******************************************************************************
	(AGR) 14 Jan 2006

	Why? Because some item ages are no longer permissible (items a week or
	more in the future)
*******************************************************************************/
public class AgeResult
{
	private long		m_AgeMSecs;
	private boolean		m_Allowable;

	/*******************************************************************************
	*******************************************************************************/
	public AgeResult( final InstallationIF inInstall, long inMSecs)
	{
		m_AgeMSecs = inMSecs;
		m_Allowable = FeedUtils.isAcceptableFutureDate(inMSecs) && inMSecs <= inInstall.getMaxAgeMSecs();
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
