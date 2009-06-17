/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.ajax.impl;

import org.bloggers4labour.ajax.*;
import com.hiatus.text.UText;
import org.bloggers4labour.jsp.Displayable;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public abstract class AbstractOutputElement implements OutputElementIF
{
	protected StringBuilder		m_Builder = new StringBuilder();

	/*******************************************************************************
	*******************************************************************************/
	public void addElement( final String inElementName, final Object inContent)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addCDataElement( final String inElementName, final Object inContent)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuilder complete()
	{
		return m_Builder;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addDisplayable( final Displayable inObj, final SiteIF inSite, int inReccCount)
	{
		addCDataElement( "blogName", inObj.getBlogName());
		addElement( "siteID", ( inSite != null) ? inSite.getRecno() : -1L);
		addElement( "siteURL", inObj.getSiteURL());

		///////////////////////////////////////////////////////

		if ( inObj.getLink() != null)	// (AGR) 28 October 2006. Something wrong with Warren Morgan's blog produces null links. So test here.
		{
			addCDataElement( "link", inObj.getLink().toString());
		}
		else	addCDataElement( "link", "");	// Is this acceptable?

		///////////////////////////////////////////////////////  (AGR) 30 Nov 2006. May be "< 1 min", so may need CDATA!

		String	dateStr = inObj.getDateString();

		if ( UText.isValidString(dateStr) && dateStr.contains("<"))	// (AGR) 21 Feb 2007. Bug-fix. Previous was causing "(< 1 min)" to go through unencoded, breaking validation.
		{
			addCDataElement( "date", dateStr);
		}
		else	addElement( "date", dateStr);

		///////////////////////////////////////////////////////

		addCDataElement( "displayTitle", UText.isValidString( inObj.getDispTitle() ) ? inObj.getDispTitle() : "<i>Untitled</i>");
		addCDataElement( "desc", inObj.getDescription());
		addElement( "iconURL", UText.isValidString( inObj.getIconURL() ) ? inObj.getIconURL() : "");

		addElement( "votes", Integer.toString(inReccCount));    // (AGR) 1 October 2006
	}
}