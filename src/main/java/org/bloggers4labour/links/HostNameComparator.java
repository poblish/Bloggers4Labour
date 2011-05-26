/*
 * HostNameComparator.java
 *
 * Created on May 30, 2005, 11:12 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.links;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author andrewre
 */
public class HostNameComparator implements Comparator<HostNameCount>, /* (AGR) 3 Feb 2007. FindBugs recommended this */ Serializable
{
	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public int compare( HostNameCount sa, HostNameCount sb)
	{
		if ( sa.m_Count != sb.m_Count)
		{
			return ( sa.m_Count < sb.m_Count) ? 1 : -1;
		}

		return _fixString(sa.m_Name).compareToIgnoreCase( _fixString(sb.m_Name) );
	}

	/*******************************************************************************
		(AGR) 31 July 2005. Factored-out and added "The " logic
	*******************************************************************************/
	private String _fixString( final String inStr)
	{
		String	temp = inStr.startsWith("www.") ? inStr.substring(4) : inStr;

		return temp.startsWith("The ") ? temp.substring(4) : temp;
	}
};