/*
 * StandardPoller.java
 *
 * Created on 28 October 2006, 17:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import de.nava.informa.utils.poller.*;
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

			// s_Poll_Logger.info("Poller: add Observer: " + m_Observer);
		}
	}
}