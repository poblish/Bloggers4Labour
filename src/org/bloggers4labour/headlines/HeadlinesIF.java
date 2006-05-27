/*
 * HeadlinesIF.java
 *
 * Created on June 26, 2005, 1:22 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.headlines;

import java.util.List;
import java.util.Set;
import org.bloggers4labour.tag.Link;

/**
 *
 * @author andrewre
 */
public interface HeadlinesIF
{
	public Set getBlogs();
	public int getBlogsCount();
	public int size();

	public List<Link> getLinksByName();
	public List<Link> getLinksByURL();
}
