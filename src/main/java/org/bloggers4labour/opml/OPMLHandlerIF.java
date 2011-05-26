/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.opml;

import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public interface OPMLHandlerIF
{
	void generate( final SiteIF[] ioSitesArray);

	InstallationIF getInstallation();
	String getOPMLString();

	void clear();

	void disconnect();
}