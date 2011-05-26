/*
 * HostNameCount.java
 *
 * Created on May 30, 2005, 11:17 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.links;

/**
 *
 * @author andrewre
 */
public class HostNameCount
{
	protected String	m_Name;
	protected int		m_Count;

	/*******************************************************************************
	*******************************************************************************/
	public HostNameCount( String s, int c)
	{
		m_Name = s;
		m_Count = c;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getName()
	{
		return m_Name;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int getCount()
	{
		return m_Count;
	}
};