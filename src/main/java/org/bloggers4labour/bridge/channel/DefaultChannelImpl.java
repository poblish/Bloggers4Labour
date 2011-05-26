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
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.bloggers4labour.bridge.channel.item.DefaultItemBridgeFactory;
import org.bloggers4labour.bridge.channel.item.ItemBridgeIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewregan
 */
public class DefaultChannelImpl implements ChannelIF
{
	private long				m_Id;
	private String				m_Title;
	private String				m_Description;
	private URL				m_Location;
	private URL				m_Site;
	private ChannelFormat			m_Format;

	@SuppressWarnings("unchecked")
	protected Long2ObjectMap<ItemIF>	m_Coll = Long2ObjectMaps.EMPTY_MAP;	// (AGR) 18 Dec 2010. Use new fastutils impl.

	private Collection<CategoryIF>		m_Categories = Collections.emptyList();

	protected final static ItemBridgeIF	s_Bridge = new DefaultItemBridgeFactory().getInstance();

	/*******************************************************************************
	*******************************************************************************/
	public DefaultChannelImpl()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	@SuppressWarnings("unchecked")
	public DefaultChannelImpl( final de.nava.informa.core.ChannelIF inOriginal)
	{
		m_Coll = new Long2ObjectLinkedOpenHashMap<ItemIF>();

		init(inOriginal);

		@SuppressWarnings("unchecked")
		Collection<de.nava.informa.core.ItemIF>		c = (Collection<de.nava.informa.core.ItemIF>) inOriginal.getItems();

		for ( de.nava.informa.core.ItemIF each : c)
		{
			addItem(each);	// NOPMD
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	@SuppressWarnings("unchecked")
	protected final void init( final de.nava.informa.core.ChannelIF inOriginal)
	{
		m_Id = inOriginal.getId();
		m_Title = inOriginal.getTitle();
		m_Description = inOriginal.getDescription();
		m_Location = inOriginal.getLocation();
		m_Site = inOriginal.getSite();
		m_Format = inOriginal.getFormat();

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
	@Override public URL getSite()
	{
		return m_Site;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public URL getLocation()
	{
		return m_Location;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override
	@SuppressWarnings("unchecked")
	public Collection<ItemIF> getItems()
	{
		return m_Coll.values();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String getTitle()
	{
		return m_Title;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String getDescription()
	{
		return m_Description;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String getFormatString()
	{
		return m_Format.toString();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public void addItem( final de.nava.informa.core.ItemIF inItem)
	{
		m_Coll.put( inItem.getId(), s_Bridge.bridge( inItem, this));
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean addItemWithResult( final de.nava.informa.core.ItemIF inItem)
	{
		return ( m_Coll.put( inItem.getId(), s_Bridge.bridge( inItem, this)) != null);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public void removeItem( final de.nava.informa.core.ItemIF inItem)
	{
		m_Coll.remove( inItem.getId() );
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public ChannelFormat getFormat()
	{
		return m_Format;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public long getId()
	{
		return m_Id;
	}

	@Override public String getLanguage()
	{
		return null;
	}

	@Override public void setLanguage(String language)
	{
		// NOOP
	}

	@Override public String getPublisher()
	{
		return null;
	}

	@Override public void setPublisher(String publisher)
	{
		// NOOP
	}

	@Override public String getRating()
	{
		return null;
	}

	@Override public void setRating(String rating)
	{
		// NOOP
	}

	@Override public String getGenerator()
	{
		return null;
	}

	@Override public void setGenerator(String generator)
	{
		// NOOP
	}

	@Override public String getDocs()
	{
		return null;
	}

	@Override public void setDocs(String docs)
	{
		// NOOP
	}

	@Override public int getTtl()
	{
		return 0;
	}

	@Override public void setTtl(int ttl)
	{
		// NOOP
	}

	@Override public void setFormat(ChannelFormat x)
	{
		m_Format = x;
	}

	@Override public de.nava.informa.core.ItemIF getItem(long id)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public ImageIF getImage()
	{
		return null;
	}

	@Override public void setImage(ImageIF image)
	{
		// NOOP
	}

	@Override public TextInputIF getTextInput()
	{
		return null;
	}

	@Override public void setTextInput(TextInputIF textInput)
	{
		// NOOP
	}

	@Override public Date getLastUpdated()
	{
		return null;
	}

	@Override public void setLastUpdated(Date lastUpdated)
	{
		// NOOP
	}

	@Override public Date getLastBuildDate()
	{
		return null;
	}

	@Override public void setLastBuildDate(Date lastBuild)
	{
		// NOOP
	}

	@Override public Date getPubDate()
	{
		return null;
	}

	@Override public void setPubDate(Date pubDate)
	{
		// NOOP
	}

	@Override public CloudIF getCloud()
	{
		return null;
	}

	@Override public void setCloud(CloudIF cloud)
	{
		// NOOP
	}

	@Override public String getUpdatePeriod()
	{
		return null;
	}

	@Override public void setUpdatePeriod(String updatePeriod)
	{
		// NOOP
	}

	@Override public int getUpdateFrequency()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public void setUpdateFrequency(int updateFrequency)
	{
		// NOOP
	}

	@Override public Date getUpdateBase()
	{
		return null;
	}

	@Override public void setUpdateBase(Date updateBase)
	{
		// NOOP
	}

	@Override public void setId(long id)
	{
		m_Id = id;
	}

	@Override public void setTitle(String title)
	{
		m_Title = title;
	}

	@Override public String getElementValue(String path)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public String[] getElementValues(String path, String[] elements)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public String getAttributeValue(String path, String attribute)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public String[] getAttributeValues(String path, String[] attributes)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public void setLocation(URL location)
	{
		m_Location = location;
	}

	@Override public String getCreator()
	{
		return null;
	}

	@Override public void setCreator(String creator)
	{
		// NOOP
	}

	@Override public Collection getCategories()
	{
		return m_Categories;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setCategories(Collection categories)
	{
		m_Categories = categories;
	}

	@Override public void addCategory(CategoryIF category)
	{
		// NOOP: Never used.
	}

	@Override public void removeCategory(CategoryIF category)
	{
		// NOOP: Never used.
	}

	@Override public void setDescription(String description)
	{
		m_Description = description;
	}

	@Override public void setSite(URL site)
	{
		m_Site = site;
	}

	@Override public String getCopyright()
	{
		return null;
	}

	@Override public void setCopyright(String copyright)
	{
		// NOOP
	}

	@Override public void addObserver(ChannelObserverIF o)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public void removeObserver(ChannelObserverIF o)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
