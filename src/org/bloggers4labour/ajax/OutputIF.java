/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.ajax;

import org.bloggers4labour.jsp.Displayable;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public interface OutputIF 
{
	public void add( final OutputElementIF inElement);
	public void addElement( final String inElementName, final Object inContent);
	public void addCDataElement( final String inElementName, final Object inContent);
	public void addDisplayable( final Displayable inObj, final SiteIF inSite, int inReccCount);

	public StringBuilder complete();
}