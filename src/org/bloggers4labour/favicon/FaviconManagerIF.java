/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.favicon;

import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public interface FaviconManagerIF 
{
	String findURL( SiteIF inSiteObj);
	void rememberFavicon( SiteIF inSiteObj);
}