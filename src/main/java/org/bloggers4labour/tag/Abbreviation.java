/*
 * Abbreviation.java
 *
 * Created on May 30, 2005, 12:07 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.tag;

/********************************************************************
	(AGR) 8 May 2005
********************************************************************/
public class Abbreviation extends Tag
{
	public String	m_Title;

	/********************************************************************
	********************************************************************/
	public Abbreviation( int inStartPos, int inEndPos, String inTitle, String inName)
	{
		super( inStartPos, inEndPos, inName);

		m_Title = inTitle;
	}

	/********************************************************************
		(AGR) 13 April 2005
	********************************************************************/
	public String getTitle()
	{
		return m_Title;
	}
}