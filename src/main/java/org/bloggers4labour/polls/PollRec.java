/*
 * PollRec.java
 *
 * Created on 03 December 2006, 19:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polls;

/**
 *
 * @author andrewre
 */
public class PollRec
{
	private String	m_Candidate;
	private int	m_Id;

	/*******************************************************************************
	*******************************************************************************/
	public PollRec( final String a, int b)
	{
		m_Candidate = a;
		m_Id = b;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getCandidate()
	{
		return m_Candidate;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getId()
	{
		return m_Id;
	}
}
