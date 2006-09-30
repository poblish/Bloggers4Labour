/*
 * AbstractDisplayable.java
 *
 * Created on June 24, 2005, 12:50 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.jsp;

/**
 *
 * @author andrewre
 */
public abstract class AbstractDisplayable implements Displayable
{
	protected final static String	DEFAULT_DESCRIPTION_STYLE = "item-description";

	/*******************************************************************************
	*******************************************************************************/
	public AbstractDisplayable()
	{
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public String getCreatorsStr()
	{
		if ( getSite() != null)
		{
			return getSite().getCreatorsString("http://www.bloggers4labour.org/images/creators/");
		}

		return null;
	}

	/*******************************************************************************
		(AGR) 25 September 2006
	*******************************************************************************/
	public String getReducedCreatorsStr()
	{
		if ( getSite() != null)
		{
			return getSite().getReducedCreatorsString("http://www.bloggers4labour.org/images/creators/");
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getBlogName()
	{
		if ( getSite() != null)
		{
			return getSite().getName();
		}

		return "???";
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getIconURL()
	{
		if ( getSite() != null)
		{
			return getSite().getFaviconLocation();
		}

		return null;
	}
}
