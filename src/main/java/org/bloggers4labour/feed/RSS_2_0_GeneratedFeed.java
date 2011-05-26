/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.feed;

/**
 *
 * @author aregan
 */
public class RSS_2_0_GeneratedFeed implements GeneratedFeed
{
	private String m_XMLString;

	/*******************************************************************************
	*******************************************************************************/
	public RSS_2_0_GeneratedFeed( final String inXML)
	{
		m_XMLString = inXML;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getXML()
	{
		return m_XMLString;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return getXML();
	}
}