/*
 * Image.java
 *
 * Created on May 30, 2005, 12:08 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.tag;

/********************************************************************
	(AGR) 15 May 2005
********************************************************************/
public class Image extends Tag
{
	public String	m_HRef;

	/********************************************************************
	********************************************************************/
	public Image( int inStartPos, int inEndPos, String inHRef)
	{
		super( inStartPos, inEndPos, "");
		m_HRef = inHRef;
	}

	/********************************************************************
	********************************************************************/
	public String getHRef()
	{
		return m_HRef;
	}
}