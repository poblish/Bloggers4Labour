/*
 * ScoresPoller.java
 *
 * Created on 28 October 2006, 23:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.cricket;

import de.nava.informa.utils.poller.*;
import org.bloggers4labour.Installation;
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

	private static long				s_TempVal;

	/*******************************************************************************
		(AGR) 28 October 2006
	*******************************************************************************/
	public ScoresPoller( long inPollerFrequencyMS)
	{
		m_InformaPoller = new de.nava.informa.utils.poller.Poller();
		m_InformaPoller.setPeriod( inPollerFrequencyMS );
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
		return m_InformaPoller;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void startPolling()
	{
		if ( m_Observer == null)
		{
			m_Observer = new MyObserver( m_Installation );

			m_InformaPoller.addObserver(m_Observer);

			// s_Poll_Logger.info("Poller: add Observer: " + m_Observer);
		}
	}
}