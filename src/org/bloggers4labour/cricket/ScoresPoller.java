/*
 * ScoresPoller.java
 *
 * Created on 28 October 2006, 23:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.cricket;

import com.hiatus.dates.UDates;
import de.nava.informa.utils.poller.PollerObserverIF;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.DefaultChannelBridgeFactory;
import org.bloggers4labour.polling.MyObserver;

/**
 *
 * @author andrewre
 *
 * BAsed upon StandardPoller
 */
public class ScoresPoller extends org.bloggers4labour.polling.Poller
{
	private PollerObserverIF			m_Observer;
	private de.nava.informa.utils.poller.Poller	m_InformaPoller;

	private static Logger				s_Logger = Logger.getLogger( ScoresPoller.class );

	/*******************************************************************************
		(AGR) 28 October 2006
	*******************************************************************************/
	public ScoresPoller( String inName, long inPollerFrequencyMS)
	{
		super(inName);

		m_InformaPoller = new de.nava.informa.utils.poller.Poller();
		m_InformaPoller.setPeriod( inPollerFrequencyMS );

		s_Logger.info("ScoresPoller created Informa Poller, freq = " + UDates.getFormattedTimeDiff( inPollerFrequencyMS ));
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setInstallation( final InstallationIF inInstallation)
	{
		m_Installation = inInstallation;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void startPolling()
	{
		if ( m_Observer == null)
		{
			m_Observer = new MyObserver( m_Installation );

			m_InformaPoller.addObserver(m_Observer);

			// s_Logger.info("Poller: add Observer: " + m_Observer);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean registerChannelWithInforma( final ChannelIF inChannel)
	{
		s_Logger.debug( getLogPrefix() + "Registering: " + inChannel);

		m_InformaPoller.registerChannel( new DefaultChannelBridgeFactory().getInstance().bridge(inChannel) );

		return true;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void unregisterChannelWithInforma( final ChannelIF inChannel)
	{
		m_InformaPoller.unregisterChannel( new DefaultChannelBridgeFactory().getInstance().bridge(inChannel) );
	}
}