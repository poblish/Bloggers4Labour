/*
 * EventsSection.java
 *
 * Created on 17 January 2007, 16:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.mail;

import com.hiatus.htl.*;
import com.hiatus.sql.ResultSetList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Map;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.sql.QueryBuilder;

/**
 *
 * @author andrewre
 */
public class EventsSection
{
	private boolean			m_GotEvents;
	private StringBuffer		m_TextEventsBuf;
	private StringBuffer		m_HTMLEventsBuf;

	private static HTLTemplate	s_EachTextEventTemplate = HTL.createTemplate( "event_each.txt", MessageBuilder.s_Locale);	// (AGR) 16 Jan 2007
	private static HTLTemplate	s_TextEventsTemplate = HTL.createTemplate( "events_container.txt", MessageBuilder.s_Locale);	// (AGR) 16 Jan 2007
	private static HTLTemplate	s_EachHTMLEventTemplate = HTL.createTemplate( "event_each.html", MessageBuilder.s_Locale);	// (AGR) 16 Jan 2007
	private static HTLTemplate	s_HTMLEventsTemplate = HTL.createTemplate( "events_container.html", MessageBuilder.s_Locale);	// (AGR) 16 Jan 2007

	/*******************************************************************************
	*******************************************************************************/
	public EventsSection( final InstallationIF inInstall, final DateFormat inDF, final Statement inS) throws SQLException
	{
		String		theUpcomingEventsQuery = QueryBuilder.getUpcomingEventsQuery();
		ResultSet	theEventsRS = inS.executeQuery(theUpcomingEventsQuery);
		ResultSetList	theRSL = new ResultSetList(theEventsRS);

		theEventsRS.close();

		////////////////////////////////////////////////////////////////////////

		if (!theRSL.isEmpty())
		{
			StringBuilder	textBuilder = new StringBuilder();
			StringBuilder	htmlBuilder = new StringBuilder();

			MessageBuilder	textB = new TextMessageBuilder( inInstall, inDF);
			MessageBuilder	htmlB = new HTMLMessageBuilder( inInstall, inDF);

			int		count = 1;
			Integer		eventsCount = new Integer( theRSL.getRowsList().size() );

			m_GotEvents = true;

			for ( Object obj : theRSL.getRowsList())
			{
				Map	m = (Map) obj;

				String		locStr = (String) m.get("event_location");
				String		pcStr = (String) m.get("event_postcode");
				StringBuilder	locationStr = new StringBuilder();

				if (locStr.endsWith("."))
				{
					locationStr.append(locStr).append(" ").append(pcStr);
				}
				else	locationStr.append(locStr).append(". ").append(pcStr);

				////////////////////////////////////////////////////////////////////////

				String	theNameStr = (String) m.get("event_name");
				String	theDescStr = (String) m.get("event_description");
				String	urlStr = (String) m.get("event_URL");
				String	theStartTimeStr = inDF.format( (Timestamp) m.get("event_start") );
				String	theEndTimeStr = inDF.format( (Timestamp) m.get("event_end") );

				textB.put( "event_index", Integer.toString(count));
				textB.put( "event_count", eventsCount);
				textB.put( "event_name", theNameStr);
				textB.put( "event_start_date", theStartTimeStr);
				textB.put( "event_end_date", theEndTimeStr);
				textB.put( "event_desc", theDescStr);
				textB.put( "event_location", locStr);
				textB.put( "event_postcode", pcStr);
				textB.put( "event_location_str", locationStr);
				textB.put( "event_URL", urlStr);

				textBuilder.append( textB.mergeTemplate(s_EachTextEventTemplate) );

				htmlB.put( "event_index", Integer.toString(count));
				htmlB.put( "event_count", eventsCount);
				htmlB.put( "event_name", theNameStr);
				htmlB.put( "event_start_date", theStartTimeStr);
				htmlB.put( "event_end_date", theEndTimeStr);
				htmlB.put( "event_desc", theDescStr);
				htmlB.put( "event_location", locStr);
				htmlB.put( "event_postcode", pcStr);
				htmlB.put( "event_location_str", locationStr);
				htmlB.put( "event_URL", urlStr);

				htmlBuilder.append( textB.mergeTemplate(s_EachHTMLEventTemplate) );

				count++;
			}

			////////////////////////////////////////////////////////////////////////

			textB.put( "events_body", textBuilder);
			htmlB.put( "events_body", htmlBuilder);

			m_TextEventsBuf = textB.mergeTemplate(s_TextEventsTemplate);
			m_HTMLEventsBuf = htmlB.mergeTemplate(s_HTMLEventsTemplate);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean gotEvents()
	{
		return m_GotEvents;
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuffer getTextContent()
	{
		return m_TextEventsBuf;
	}

	/*******************************************************************************
	*******************************************************************************/
	public StringBuffer getHTMLContent()
	{
		return m_HTMLEventsBuf;
	}
}
