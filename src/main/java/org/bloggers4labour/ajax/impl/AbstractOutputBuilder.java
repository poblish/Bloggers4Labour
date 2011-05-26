/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.ajax.impl;

import org.bloggers4labour.ajax.*;
import org.bloggers4labour.jsp.Displayable;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public abstract class AbstractOutputBuilder implements OutputBuilderIF
{
	protected OutputElementIF	m_Root;

	/*******************************************************************************
	*******************************************************************************/
	public AbstractOutputBuilder( final String inRootName)
	{
		m_Root = newElement( inRootName, null);
	}

	/*******************************************************************************
	*******************************************************************************/
	public OutputElementIF getRoot()
	{
		return m_Root;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void add( final OutputElementIF inElement)
	{
		getRoot().add(inElement);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addElement( final String inElementName, final Object inContent)
	{
		getRoot().addElement( inElementName, inContent);
	}

	/*******************************************************************************
	******************************************************************************
	public void addElement( final String inElementName, final String inAttrs, final Object inContent)
	{
		getRoot().addElement( inElementName, inAttrs, inContent);
	}*/

	/*******************************************************************************
	*******************************************************************************/
	public void addCDataElement( final String inElementName, final Object inContent)
	{
		getRoot().addCDataElement( inElementName, inContent);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addDisplayable( final Displayable inObj, final SiteIF inSite, final int inReccCount)
	{
		getRoot().addDisplayable( inObj, inSite, inReccCount);
	}
}