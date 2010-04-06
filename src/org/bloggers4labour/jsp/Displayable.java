/*
 * Displayable.java
 *
 * Created on June 24, 2005, 12:33 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.jsp;

import java.net.URL;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemType;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public interface Displayable
{
	public SiteIF getSite();

	public String getDescription();
	public String getDateString();
	public String getSiteURL();
	public String getDescriptionStyle();
	public String getIconURL( InstallationIF inInstall);
	public String getCreatorsStr();
	public String getEncodedTitle();
	public String getDispTitle();
	public String getBlogName();
	public String getEncodedLink();
	public URL getLink();

	public ItemType getItemType();		// (AGR) 1 Dec 2005
	public String getCommentTitle();	// " " "
	public String getCommentAuthor();	// " " "
}
