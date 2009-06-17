/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.bridge.channel.item;

import com.hiatus.text.UText;
import de.nava.informa.core.ItemEnclosureIF;
import de.nava.informa.core.ItemGuidIF;
import de.nava.informa.core.ItemSourceIF;
import de.nava.informa.utils.ParserUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.bloggers4labour.bridge.cats.DefaultCategoryImpl;
import org.bloggers4labour.bridge.channel.ChannelBridgeIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.DefaultChannelBridgeFactory;

/**
 *
 * @author andrewregan
 */
public class DefaultItemImpl implements ItemIF
{
	private ChannelIF			m_OurChannel;
	private String				m_Title;
	private String				m_Description;
	private Date				m_Date;
	private long				m_Id;
	private URL				m_Link;
	private String				m_AuthorName;	// (AGR) 4 June 2009

	private Collection<de.nava.informa.core.CategoryIF>	m_Categories = Collections.emptyList();

	private final static TimeZone		s_GMTZone = TimeZone.getTimeZone("GMT");	// (AGR) 24 October 2006
	private final static ChannelBridgeIF	s_Bridge = new DefaultChannelBridgeFactory().getInstance();

	private static Logger			s_Logger = Logger.getLogger( ItemIF.class );

	/*******************************************************************************
	*******************************************************************************/
	public DefaultItemImpl( final de.nava.informa.core.ItemIF inOriginal)
	{
		this( inOriginal, s_Bridge.bridge( inOriginal.getChannel() ));
	}

	/*******************************************************************************
	*******************************************************************************/
	@SuppressWarnings("unchecked")
	public DefaultItemImpl( final de.nava.informa.core.ItemIF inOriginal, ChannelIF inChannel)
	{
		this( inOriginal.getId(),
			_getItemDate(inOriginal),	// Correct these sooner rather than later!!!
			inOriginal.getTitle(),
			inOriginal.getDescription(),
			inOriginal.getLink(),
			inOriginal.getElementValue("author/name"),	// (AGR) 4 June 2009
			inChannel,
			inOriginal.getCategories());
	}

	/*******************************************************************************
	*******************************************************************************/
	private DefaultItemImpl( long inId, final Date inDate, final String inTitle, final String inDesc, final URL inLink,
					final String inAuthorName,	// (AGR) 4 June 2009
					final ChannelIF inChannel,
					final Collection<de.nava.informa.core.CategoryIF> inCats)
	{
		m_Date = inDate;
		m_Title = inTitle;
		m_Description = inDesc;
		m_Id = inId;
		m_Link = inLink;
		m_AuthorName = UText.isNullOrBlank(inAuthorName) ? null : inAuthorName;		// (AGR) 4 June 2009
		m_OurChannel = inChannel;

		if (!inCats.isEmpty())
		{
			m_Categories = new ArrayList<de.nava.informa.core.CategoryIF>();

			for ( de.nava.informa.core.CategoryIF eachCatEntry : inCats)
			{
				m_Categories.add( new DefaultCategoryImpl(eachCatEntry) );
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean matchesTitleAndLink( final ItemIF inOther)
	{
		final String itemTitle = inOther.getTitle();
		if ( m_Title != null ? !m_Title.equals(itemTitle) : itemTitle != null)
		{
			return false;
		}

		// Comparison of links uses synchronized code of Java-NET.
		// This may hurt multi-threaded applications. So, please think twice
		// before using direct comparison of links.

		final URL itemLink = inOther.getLink();
		if ( m_Link != null ? itemLink == null || !m_Link.toString().equalsIgnoreCase(itemLink.toString()) : itemLink != null)
		{
			return false;
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ItemIF))
		{
			return false;
		}

		final ItemIF item = (ItemIF) o;

		if (!matchesTitleAndLink(item))
		{
			return false;
		}

		final String itemDescription = item.getDescription();
		if ( m_Description != null ? !m_Description.equals(itemDescription) : itemDescription != null)
		{
			// s_Logger.info("==>   Our desc '" + m_Description + "'");
			// s_Logger.info("==> THEIR desc '" + item.getDescription() + "'");

			return false;
		}

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int hashCode()
	{
		StringBuilder sb = new StringBuilder(64);
		sb.append(m_Title).append(m_Description).append(m_Link);
		return sb.toString().hashCode();
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return "[Item (" + m_Id + "): '" + m_Title + "' @ " + m_Link + "]";
//		return "[Item (" + m_Id + "): '" + m_Title + "']";
	}

	/*******************************************************************************
	*******************************************************************************/
	public Date getDate()
	{
		return m_Date;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setDate( Date x)
	{
		m_Date = x;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ChannelIF getOurChannel()
	{
		return m_OurChannel;
	}

	/*******************************************************************************
	*******************************************************************************/
	public de.nava.informa.core.ChannelIF getChannel()
	{
		return s_Bridge.bridge(m_OurChannel);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public ItemIF clone()
	{
		return new DefaultItemImpl( m_Id, m_Date, m_Title, m_Description, m_Link, m_AuthorName, m_OurChannel, m_Categories);
	}

	public ItemGuidIF getGuid()
	{
		return null;	// new ItemGuid(this);
	}

	public void setGuid(ItemGuidIF guid)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public URL getComments()
	{
		return null;
	}

	public void setComments(URL comments)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ItemSourceIF getSource()
	{
		return null;
	}

	public void setSource(ItemSourceIF source)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ItemEnclosureIF getEnclosure()
	{
		return null;
	}

	public void setEnclosure(ItemEnclosureIF enclosure)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getSubject()
	{
		return null;
	}

	public void setSubject(String subject)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Date getFound()
	{
		return null;
	}

	public void setFound(Date found)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public long getId()
	{
		return m_Id;
	}

	public void setId(long id)
	{
		m_Id = id;
	}

	public String getTitle()
	{
		return m_Title;
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
	public void setCategories(Collection x)
	{
		m_Categories = x;
	}

	public void addCategory( final de.nava.informa.core.CategoryIF x)
	{
		// NOOP: Never used.
	}

	public void removeCategory( final de.nava.informa.core.CategoryIF x)
	{
		// NOOP: Never used.
	}

	public String getDescription()
	{
		return m_Description;
	}

	public void setDescription(String x)
	{
		m_Description = x;
	}

	public URL getLink()
	{
		return m_Link;
	}

	public void setLink(URL x)
	{
		m_Link = x;
	}

	public void setChannel(de.nava.informa.core.ChannelIF inNewChannel)
	{
		m_OurChannel = s_Bridge.bridge(inNewChannel);
	}

	public boolean getUnRead()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setUnRead(boolean val)
	{
		// NOOP
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getAuthorName()
	{
		return m_AuthorName;
	}

	/*******************************************************************************
		Atom 0.3 feeds like NormBlog weren't setting a date tag in the
		expected way, but I found a workaround...
	*******************************************************************************/
	private static Date _getItemDate( final de.nava.informa.core.ItemIF inItem)
	{
		Date	theDate = inItem.getDate();

		if ( theDate != null)
		{
			Calendar	theCal = Calendar.getInstance(s_GMTZone);

			theCal.setTimeInMillis( theDate.getTime() );	// (AGR) 24 October 2006. For the ECB cricket score feed's benefit

			if ( theCal.get( Calendar.YEAR ) < 10)
			{
				theCal.roll( Calendar.YEAR, 2000);
				// s_Utils_Logger.info("From \"" + theDate + "\" to \"" + theCal.getTime() + "\"");
				return theCal.getTime();
			}

			return theDate;	// (AGR) 24 October 2006. Change as little as possible!
		}

		try
		{
			return ParserUtils.getDate( inItem.getElementValue("created") );
		}
		catch (Exception e)
		{
			try	// (AGR) 4 March 2006. "http://geoffblog.co.uk/?atom=1" is Atom v.???, and only has <published> and <updated> !!!
			{
				String	theUpdatedStr = inItem.getElementValue("updated");

				if (theUpdatedStr.endsWith("Z"))	// As if this wasn't enough, a trailing 'Z' would kill any date parsing
				{
					return ParserUtils.getDate( theUpdatedStr.substring( 0, theUpdatedStr.length() - 1));
				}

				return ParserUtils.getDate(theUpdatedStr);
			}
			catch (Exception e2)
			{
				// (AGR) 4 March 2006. Haven't we tried hard enough???
			}
		}

		return null;
	}
}
