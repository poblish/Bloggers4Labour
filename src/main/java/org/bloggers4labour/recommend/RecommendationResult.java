/*
 * RecommendationResult.java
 *
 * Created on 21 June 2006, 21:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.recommend;

/**
 *
 * @author andrewre
 */
public class RecommendationResult
{
	private RecommendationStatus	m_Status;
	private long			m_VoteCount;

	/********************************************************************
	********************************************************************/
	public RecommendationResult( RecommendationStatus inStatus, long inVoteCount)
	{
		m_Status = inStatus;
		m_VoteCount = inVoteCount;
	}

	/********************************************************************
	********************************************************************/
	public static RecommendationResult newErrorResult()
	{
		return new RecommendationResult( RecommendationStatus.ERROR, -1L);
	}

	/********************************************************************
	********************************************************************/
	public RecommendationStatus getStatus()
	{
		return m_Status;
	}

	/********************************************************************
	********************************************************************/
	public long getVoteCount()
	{
		return m_VoteCount;
	}
}
