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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
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

		if (theEventsRS.next())
		{
			StringBuilder	textBuilder = new StringBuilder();
			StringBuilder	htmlBuilder = new StringBuilder();

			MessageBuilder	textB = new TextMessageBuilder( inInstall, inDF);
			MessageBuilder	htmlB = new HTMLMessageBuilder( inInstall, inDF);

			do
			{
				m_GotEvents = true;

				String		locStr = theEventsRS.getString("event_location");
				String		pcStr = theEventsRS.getString("event_postcode");
				StringBuilder	locationStr = new StringBuilder();

				if (locStr.endsWith("."))
				{
					locationStr.append(locStr).append(" ").append(pcStr);
				}
				else	locationStr.append(locStr).append(". ").append(pcStr);

				////////////////////////////////////////////////////////////////////////

				textB.put( "event_name", theEventsRS.getString("event_name"));
				textB.put( "event_start_date", theEventsRS.getTimestamp("event_start"));
				textB.put( "event_end_date", theEventsRS.getTimestamp("event_end"));
				textB.put( "event_desc", theEventsRS.getString("event_description"));
				textB.put( "event_location", locStr);
				textB.put( "event_postcode", pcStr);
				textB.put( "event_location_str", locationStr);

				textBuilder.append( textB.mergeTemplate(s_EachTextEventTemplate) );

				htmlB.put( "event_name", theEventsRS.getString("event_name"));
				htmlB.put( "event_start_date", theEventsRS.getTimestamp("event_start"));
				htmlB.put( "event_end_date", theEventsRS.getTimestamp("event_end"));
				htmlB.put( "event_desc", theEventsRS.getString("event_description"));
				htmlB.put( "event_location", locStr);
				htmlB.put( "event_postcode", pcStr);
				htmlB.put( "event_location_str", locationStr);

				htmlBuilder.append( textB.mergeTemplate(s_EachHTMLEventTemplate) );
			}
			while (theEventsRS.next());

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
