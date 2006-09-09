/*
 * LastPostTable.java
 *
 * Created on 09 September 2006, 18:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.activity;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author andrewre
 */
public class LastPostTable
{
//	private Map<Number,Date>	m_Datax = new TreeMap<Number,Date>();
	private Map<Number,Number>	m_Data = new TreeMap<Number,Number>();

	/*******************************************************************************
	*******************************************************************************/
	public LastPostTable()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public Number store( long inSiteRecno, long inTimeMSecs)
	{
		return m_Data.put( inSiteRecno, inTimeMSecs);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		StringBuilder	sb = new StringBuilder("Data = " + m_Data);
/*		int		i = 0;

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
*/
		return sb.toString();
	}
}