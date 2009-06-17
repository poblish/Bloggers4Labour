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

import java.util.Collection;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.jmx.Stats;
import org.bloggers4labour.tag.Link;

/**
 *
 * @author andrewre
 */
public interface HeadlinesIF
{
	public String getName();
	public String getHeadlinesXMLString();
	public long getMaxAgeMsecs();

	public Collection<String> getBlogs();
	public int getBlogsCount();
	public int size();

	public int countLinks();

	public Collection<Link> getLinksByName();
	public Collection<Link> getLinksByURL();
	public Collection<ItemIF> createSortedCollection();

	public void addHandler( Handler inHandler);

	public void publishSnapshot();
	public void publishSnapshot( Stats ioStats);
	public String publishSnapshot_Included( ItemIF[] inItems, final String inIncludeOnlyTheseBlogs);
	public String publishSnapshot_Excluded( ItemIF[] inItems, final String inExcludeOnlyTheseBlogs);
	public String publishSnapshot_Filtered( ItemIF[] inItems, final String inBase36BitmapString, boolean inIncludeNotExclude);

	public ItemIF[] toArray();
}
