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

import com.hiatus.dates.UDates;
import com.hiatus.locales.ULocale2;
import de.nava.informa.exporters.RSS_2_0_Exporter;
import de.nava.informa.impl.basic.ChannelBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.bloggers4labour.bridge.channel.item.DefaultItemBridgeFactory;
import org.bloggers4labour.bridge.channel.item.ItemBridgeIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.feed.GeneratedFeed;
import org.bloggers4labour.feed.RSS_2_0_GeneratedFeed;
import org.bloggers4labour.jmx.Stats;

/**
 *
 * @author andrewre
 */
public class FeedCreator
{
	private Logger	m_Logger;

	/*******************************************************************************
	*******************************************************************************/
	public FeedCreator( Logger inLogger)
	{
		m_Logger = inLogger;
	}

	/*******************************************************************************
		(AGR) 5 June 2005 - from now on, inItems could be null!
	*******************************************************************************/
	public GeneratedFeed createChannel( Stats ioStats, String inTitle, String inDescrStr, ItemIF[] inItems)
	{
		int	numEntries = ( inItems != null) ? inItems.length : 0;	// (AGR) 5 June 2005

//		int	itemsCount = m_Coll.size();
//		s_Headlines_Logger.info("Do snapshot: " + numEntries + " items.");

		long				start_ms = System.currentTimeMillis();
		long				end_ms;
		de.nava.informa.core.ChannelIF	theChannel = new ChannelBuilder().createChannel(inTitle);

		theChannel.setDescription(inDescrStr);

		try
		{
			theChannel.setSite( new URL("http://www.bloggers4labour.org") );
			theChannel.setPubDate( ULocale2.getGregorianCalendar( Locale.UK ).getTime() );
		}
		catch (MalformedURLException e)
		{
		}

		///////////////////////////////////////////////////

		ItemBridgeIF	theBridge = new DefaultItemBridgeFactory().getInstance();

		for ( int i = 0; i < numEntries; i++)
		{
			theChannel.addItem( theBridge.bridge( inItems[i].clone() ) /* FeedUtils.cloneItem( theChannel, inItems[i], false) */ );
		}

		///////////////////////////////////////////////////

		StringWriter		theWriter = new StringWriter();
		RSS_2_0_Exporter	theExporter = new RSS_2_0_Exporter( theWriter, "UTF-8");
		GeneratedFeed		theNewFeed = null;

		try
		{
			theExporter.write(theChannel);

			theNewFeed = new RSS_2_0_GeneratedFeed( theWriter.toString() );
		}
		catch (IOException e)
		{
			m_Logger.error("IOException writing Channel", e);
		}
		catch (Throwable e)	// (AGR) 28 Nov 2005. Catch horrible NoSuchMethodErrors when mixing up different JDOM versions!
		{
			m_Logger.error("Exception writing Channel", e);
		}

		///////////////////////////////////////////////////

		end_ms = System.currentTimeMillis();

		if ( ioStats != null)
		{
			ioStats.setRSSFeedDetails( numEntries, end_ms, UDates.getFormattedTimeDiff( end_ms - start_ms));

			// m_Logger.info("Snapshot: did " + numEntries + " items. Exporting took " + UDates.getFormattedTimeDiff( end_ms - start_ms));
		}

		return theNewFeed;
	}
}
