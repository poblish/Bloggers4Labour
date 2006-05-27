/*
 * FeedCreator.java
 *
 * Created on May 21, 2005, 5:40 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

import com.hiatus.UDates;
import com.hiatus.ULocale2;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.core.*;
import de.nava.informa.exporters.RSS_2_0_Exporter;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.bloggers4labour.jmx.*;

/**
 *
 * @author andrewre
 */
public class FeedCreator
{
	private Logger		m_Logger;
	private ChannelIF	m_Channel;
	private ChannelBuilder	m_ChannelBuilder;
	private String		m_XMLString;

	/*******************************************************************************
	*******************************************************************************/
	public FeedCreator( Logger inLogger)
	{
		m_Logger = inLogger;
	}

	/*******************************************************************************
		(AGR) 5 June 2005 - from now on, inItems could be null!
	*******************************************************************************/
	public void createChannel( Stats ioStats, String inTitle, String inDescrStr, ItemIF[] inItems)
	{
		int	numEntries = ( inItems != null) ? inItems.length : 0;	// (AGR) 5 June 2005

//		int	itemsCount = m_Coll.size();
//		s_Headlines_Logger.info("Do snapshot: " + numEntries + " items.");

		long	start_ms = System.currentTimeMillis();
		long	end_ms;

		m_ChannelBuilder = new ChannelBuilder();

		m_Channel = m_ChannelBuilder.createChannel(inTitle);
		m_Channel.setDescription(inDescrStr);

		try
		{
			m_Channel.setSite( new URL("http://www.bloggers4labour.org") );

			m_Channel.setPubDate( ULocale2.getGregorianCalendar( Locale.UK ).getTime() );
		}
		catch (MalformedURLException e)
		{
			;
		}

		///////////////////////////////////////////////////

		for ( int i = 0; i < numEntries; i++)
		{
			m_Channel.addItem( FeedUtils.cloneItem( m_Channel, inItems[i], false) );
		}

		///////////////////////////////////////////////////

		StringWriter		theWriter = new StringWriter();
		RSS_2_0_Exporter	theExporter = new RSS_2_0_Exporter( theWriter, "UTF-8");

		try
		{
			theExporter.write(m_Channel);

			m_XMLString = theWriter.toString();
			theWriter = null;
		}
		catch (IOException e)
		{
			m_Logger.error("IOException writing Channel", e);
		}
		catch (Throwable e)	// (AGR) 28 Nov 2005. Catch horrible NoSuchMethodErrors when mixing up different JDOM versions!
		{
			m_Logger.error("Exception writing Channel", e);
		}

		theExporter = null;	// (AGR) 23 May 2005

		///////////////////////////////////////////////////

		end_ms = System.currentTimeMillis();

		if ( ioStats != null)
		{
			ioStats.setRSSFeedDetails( numEntries, end_ms, UDates.getFormattedTimeDiff( end_ms - start_ms));

			// m_Logger.info("Snapshot: did " + numEntries + " items. Exporting took " + UDates.getFormattedTimeDiff( end_ms - start_ms));
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void clear()
	{
		m_Channel = null;
		m_ChannelBuilder = null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getString()
	{
		return m_XMLString;
	}
}
