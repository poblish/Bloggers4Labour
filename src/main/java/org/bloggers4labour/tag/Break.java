/*
 * Break.java
 *
 * Created on May 30, 2005, 12:04 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.tag;

/********************************************************************
	@author andrewre
	(AGR) 15 May 2005
********************************************************************/
public class Break extends Tag
{
	/********************************************************************
	********************************************************************/
	public Break( int inStartPos, int inEndPos)
	{
		super( inStartPos, inEndPos, "");
	}

	/********************************************************************
	********************************************************************/
	public String toString()
	{
		return "<br />";
	}
}