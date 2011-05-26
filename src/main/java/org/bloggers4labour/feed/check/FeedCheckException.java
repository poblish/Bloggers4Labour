/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed.check;

/**
 *
 * @author andrewregan
 */
public class FeedCheckException implements FeedCheckErrorIF
{
	private final String		m_Msg;
	private final Exception		m_Exception;

	/*******************************************************************************
	*******************************************************************************/
	public FeedCheckException( final Exception inE)
	{
		this( null, inE);
	}

	/*******************************************************************************
	*******************************************************************************/
	public FeedCheckException( final String inMsg, final Exception inE)
	{
		m_Msg = inMsg;
		m_Exception = inE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getMessage()
	{
		return ( m_Msg != null) ? m_Msg : m_Exception.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public Exception getException()
	{
		return m_Exception;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		StringBuilder	sb = new StringBuilder();

		sb.append("FeedCheckException: " + getMessage());

		return sb.toString();
	}
}