/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.headlines;

import java.util.Collection;
import java.util.Collections;
import org.bloggers4labour.AddResult;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.jmx.Stats;
import org.bloggers4labour.site.SiteIF;
import org.bloggers4labour.tag.Link;

/**
 *
 * @author andrewregan
 */
public class DummyHeadlinesImpl implements HeadlinesIF
{
	/*******************************************************************************
	*******************************************************************************/
	public DummyHeadlinesImpl( final InstallationIF inInstall, String inName, String inDescr, long inMinAgeMsecs, long inMaxAgeMsecs)
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public DummyHeadlinesImpl()
	{
	}

	public String getName()
	{
		return null;
	}

	public String getHeadlinesXMLString()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public long getMaxAgeMsecs()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<String> getBlogs()
	{
		return Collections.emptyList();
	}

	public int getBlogsCount()
	{
		return 0;
	}

	public int size()
	{
		return 0;
	}

	public int countLinks()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<Link> getLinksByName()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<Link> getLinksByURL()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<ItemIF> createSortedCollection()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void addHandler(Handler inHandler)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void publishSnapshot()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void publishSnapshot(Stats ioStats)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String publishSnapshot_Included(ItemIF[] inItems, String inIncludeOnlyTheseBlogs)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String publishSnapshot_Excluded(ItemIF[] inItems, String inExcludeOnlyTheseBlogs)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String publishSnapshot_Filtered(ItemIF[] inItems, String inBase36BitmapString, boolean inIncludeNotExclude)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ItemIF[] toArray()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void shutdown()
	{
		// NOOP
	}

	public void removeFor(ChannelIF inChannel)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean allowsPosts()
	{
		return false;
	}

	public boolean allowsComments()
	{
		return false;
	}

	public Collection<Number> getFilterCreatorStatuses()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isItemAgeOK(long inItemAgeMSecs)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public AddResult put(ItemIF inNewItem, SiteIF inItemsOwnerSite, ItemContext inCtxt)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setAllowPosts(boolean x)
	{
		// NOOP
	}

	public void setAllowComments(boolean x)
	{
		// NOOP
	}

	public void setFilterCreatorStatuses(Collection<Number> inList)
	{
		// NOOP
	}
}