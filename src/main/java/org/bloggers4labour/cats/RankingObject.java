/*
 * RankingObject.java
 *
 * Created on 14 March 2006, 00:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.cats;

/*******************************************************************************
	(AGR) 15 Jan 2006
*******************************************************************************/
public class RankingObject implements Comparable<RankingObject>
{
	public String	m_Key;
	public int	m_Score;

	/*******************************************************************************
	*******************************************************************************/
	RankingObject( String inKey, int inScore)
	{
		m_Key = inKey;
		m_Score = inScore;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getKey()
	{
		return m_Key;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getCount()
	{
		return m_Score;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int compareTo( RankingObject inOther)
	{
		if ( m_Score > inOther.m_Score)
		{
			return -1;
		}

		if ( m_Score < inOther.m_Score)
		{
			return 1;
		}

		return m_Key.compareToIgnoreCase( inOther.m_Key );
	}

	/*******************************************************************************
		(AGR) 29 Jan 2007. FindBugs made me add this!
	*******************************************************************************/
	@Override public boolean equals( Object inOther)
	{
		if ( inOther == null || !( inOther instanceof RankingObject))
		{
			return false;
		}

		return ( compareTo((RankingObject) inOther) == 0);
	}

	/*******************************************************************************
		(AGR) 3 Feb 2007. FindBugs made me add (something like!) this! Yuk?
	*******************************************************************************/
	@Override public int hashCode()
	{
		return ( Integer.toString(m_Score) + ":" + m_Key).hashCode();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		StringBuilder	sb = new StringBuilder();
		sb.append( m_Key ).append(" [").append(m_Score).append("]");
		return sb.toString();
	}
}