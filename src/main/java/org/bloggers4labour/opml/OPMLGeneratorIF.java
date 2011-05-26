/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.opml;

import java.io.IOException;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewregan
 */
public interface OPMLGeneratorIF 
{
	public String generate( SiteIF[] ioSitesArray) throws IOException;
}