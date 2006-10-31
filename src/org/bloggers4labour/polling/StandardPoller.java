/*
 * StandardPoller.java
 *
 * Created on 28 October 2006, 17:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import com.hiatus.UDates;
import de.nava.informa.utils.poller.*;
import org.apache.log4j.Logger;
import org.bloggers4labour.Installation;

/**
 *
 * @author andrewre
 */
public class StandardPoller extends org.bloggers4labour.polling.Poller
{
	private PollerObserverIF				m_Observer;

	private static long					s_TempVal;

	private static de.nava.informa.utils.poller.Poller	s_InformaPoller;
	private static Logger					s_Logger = Logger.getLogger( StandardPoller.class );

	/*******************************************************************************
		(AGR) 28 October 2006
	*******************************************************************************/
	public StandardPoller( long inPollerFrequencyMS)
	{
		synchronized (this)
		{
			if ( s_InformaPoller == null)
			{
				s_InformaPoller = new de.nava.informa.utils.poller.Poller();
				s_InformaPoller.setPeriod( inPollerFrequencyMS );

				s_Logger.info("StandardPoller created Informa Poller, freq = " + UDates.getFormattedTimeDiff( inPollerFrequencyMS ));
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setInstallation( final Installation inInstallation)
	{
		m_Installation = inInstallation;
	}

	/*******************************************************************************
	*******************************************************************************/
	public de.nava.informa.utils.poller.Poller getInformaPoller()
	{
		return s_InformaPoller;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void startPolling()
	{
		if ( m_Observer == null)
		{
			m_Observer = new MyObserver( m_Installation );

			s_InformaPoller.addObserver(m_Observer);

			// s_Logger.info("Poller: add Observer: " + m_Observer);
		}
	}
}