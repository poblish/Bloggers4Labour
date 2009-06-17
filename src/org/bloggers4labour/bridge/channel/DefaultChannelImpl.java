/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel;

import de.nava.informa.core.CategoryIF;
import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelObserverIF;
import de.nava.informa.core.CloudIF;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.TextInputIF;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.apache.commons.collections.map.LinkedMap;
import org.bloggers4labour.bridge.channel.item.DefaultItemBridgeFactory;
import org.bloggers4labour.bridge.channel.item.ItemBridgeIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewregan
 */
public class DefaultChannelImpl implements ChannelIF
{
	private long			m_Id;
	private String			m_Title;
	private String			m_Description;
	private URL			m_Location;
	private URL			m_Site;
	private ChannelFormat		m_Format;
	private LinkedMap		m_Coll = new LinkedMap();		// <Long,ItemIF>();
	private Collection<CategoryIF>	m_Categories = Collections.emptyList();

	private final static ItemBridgeIF	s_Bridge = new DefaultItemBridgeFactory().getInstance();

	/*******************************************************************************
	*******************************************************************************/
	@SuppressWarnings("unchecked")
	public DefaultChannelImpl( final de.nava.informa.core.ChannelIF inOriginal)
	{
		m_Id = inOriginal.getId();
		m_Title = inOriginal.getTitle();
		m_Description = inOriginal.getDescription();
		m_Location = inOriginal.getLocation();
		m_Site = inOriginal.getSite();
		m_Format = inOriginal.getFormat();

		@SuppressWarnings("unchecked")
		Collection<de.nava.informa.core.ItemIF>		c = (Collection<de.nava.informa.core.ItemIF>) inOriginal.getItems();

		for ( de.nava.informa.core.ItemIF each : c)
		{
			m_Coll.put( each.getId(), s_Bridge.bridge( each, this));
		}

		////////////////////////////////////////////////////////////////

		Collection	theCats = inOriginal.getCategories();

		if (!theCats.isEmpty())
		{
			m_Categories = new ArrayList<CategoryIF>(theCats);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals( final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ChannelIF))
		{
			return false;
		}

		final ChannelIF channel = (ChannelIF) o;

		// Comparison of links uses synchronized code of Java-NET.
		// This may hurt multi-threaded applications. So, please think twice
		// before using direct comparison of links.

		final URL channelLocation = channel.getLocation();
		if ( m_Location != null
			? channelLocation == null || !m_Location.toString().equalsIgnoreCase(channelLocation.toString())
			: channelLocation != null)
		{
			return false;
		}

		final String channelTitle = channel.getTitle();
		if ( m_Title != null ? !m_Title.equals(channelTitle) : channelTitle != null)
		{
			return false;
		}

		final String channelDescription = channel.getDescription();
		if ( m_Description != null ? !m_Description.equals(channelDescription) : channelDescription != null)
		{
			return false;
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int hashCode()
	{
		StringBuilder sb = new StringBuilder(64);
		sb.append(m_Title).append(m_Description).append(m_Location);
		return sb.toString().hashCode();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return "[Channel (" + m_Id + "): " + m_Title + " (" + m_Location + " )]";
	}

	/*******************************************************************************
	*******************************************************************************/
	public URL getSite()
	{
		return m_Site;
	}

	/*******************************************************************************
	*******************************************************************************/
	public URL getLocation()
	{
		return m_Location;
	}

	/*******************************************************************************
	*******************************************************************************/
	@SuppressWarnings("unchecked")
	public Collection<ItemIF> getItems()
	{
		return m_Coll.values();
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getTitle()
	{
		return m_Title;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getDescription()
	{
		return m_Description;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getFormatString()
	{
		return m_Format.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	public void addItem( final de.nava.informa.core.ItemIF inItem)
	{
		// return ( m_Coll.put( inItem.getId(), inItem) != null);

		m_Coll.put( inItem.getId(), s_Bridge.bridge( inItem, this));
	}

	/*******************************************************************************
	*******************************************************************************/
	public void removeItem( final de.nava.informa.core.ItemIF inItem)
	{
		// return ( m_Coll.remove( inItem.getId() ) != null);

		m_Coll.remove( inItem.getId() );
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelFormat getFormat()
	{
		return m_Format;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getId()
	{
		return m_Id;
	}

	public String getLanguage()
	{
		return null;
	}

	public void setLanguage(String language)
	{
		// NOOP
	}

	public String getPublisher()
	{
		return null;
	}

	public void setPublisher(String publisher)
	{
		// NOOP
	}

	public String getRating()
	{
		return null;
	}

	public void setRating(String rating)
	{
		// NOOP
	}

	public String getGenerator()
	{
		return null;
	}

	public void setGenerator(String generator)
	{
		// NOOP
	}

	public String getDocs()
	{
		return null;
	}

	public void setDocs(String docs)
	{
		// NOOP
	}

	public int getTtl()
	{
		return 0;
	}

	public void setTtl(int ttl)
	{
		// NOOP
	}

	public void setFormat(ChannelFormat x)
	{
		m_Format = x;
	}

	public de.nava.informa.core.ItemIF getItem(long id)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ImageIF getImage()
	{
		return null;
	}

	public void setImage(ImageIF image)
	{
		// NOOP
	}

	public TextInputIF getTextInput()
	{
		return null;
	}

	public void setTextInput(TextInputIF textInput)
	{
		// NOOP
	}

	public Date getLastUpdated()
	{
		return null;
	}

	public void setLastUpdated(Date lastUpdated)
	{
		// NOOP
	}

	public Date getLastBuildDate()
	{
		return null;
	}

	public void setLastBuildDate(Date lastBuild)
	{
		// NOOP
	}

	public Date getPubDate()
	{
		return null;
	}

	public void setPubDate(Date pubDate)
	{
		// NOOP
	}

	public CloudIF getCloud()
	{
		return null;
	}

	public void setCloud(CloudIF cloud)
	{
		// NOOP
	}

	public String getUpdatePeriod()
	{
		return null;
	}

	public void setUpdatePeriod(String updatePeriod)
	{
		// NOOP
	}

	public int getUpdateFrequency()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setUpdateFrequency(int updateFrequency)
	{
		// NOOP
	}

	public Date getUpdateBase()
	{
		return null;
	}

	public void setUpdateBase(Date updateBase)
	{
		// NOOP
	}

	public void setId(long id)
	{
		m_Id = id;
	}

	public void setTitle(String title)
	{
		m_Title = title;
	}

	public String getElementValue(String path)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String[] getElementValues(String path, String[] elements)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getAttributeValue(String path, String attribute)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String[] getAttributeValues(String path, String[] attributes)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setLocation(URL location)
	{
		m_Location = location;
	}

	public String getCreator()
	{
		return null;
	}

	public void setCreator(String creator)
	{
		// NOOP
	}

	public Collection getCategories()
	{
		return m_Categories;
	}

	@SuppressWarnings("unchecked")
	public void setCategories(Collection categories)
	{
		m_Categories = categories;
	}

	public void addCategory(CategoryIF category)
	{
		// NOOP: Never used.
	}

	public void removeCategory(CategoryIF category)
	{
		// NOOP: Never used.
	}

	public void setDescription(String description)
	{
		m_Description = description;
	}

	public void setSite(URL site)
	{
		m_Site = site;
	}

	public String getCopyright()
	{
		return null;
	}

	public void setCopyright(String copyright)
	{
		// NOOP
	}

	public void addObserver(ChannelObserverIF o)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void removeObserver(ChannelObserverIF o)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
