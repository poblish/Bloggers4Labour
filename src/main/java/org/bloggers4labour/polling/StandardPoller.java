/*
 * StandardPoller.java
 *
 * Created on 28 October 2006, 17:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import com.hiatus.dates.UDates;
import de.nava.informa.utils.poller.CompositeApprover;
import de.nava.informa.utils.poller.PollerObserverIF;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.DefaultChannelBridgeFactory;
import org.bloggers4labour.polling.api.PollerFeedApproverIF;

/**
 *
 * @author andrewre
 */
public class StandardPoller extends org.bloggers4labour.polling.Poller
{
	protected PollerObserverIF				m_Observer;

	private final PollerFeedApproverIF			m_Approver;

	protected de.nava.informa.utils.poller.Poller		m_InformaPoller;
//	protected static de.nava.informa.utils.poller.Poller	s_InformaPoller;
	protected static Logger					s_Logger = Logger.getLogger( StandardPoller.class );

	/*******************************************************************************
		(AGR) 28 October 2006
	*******************************************************************************/
	public StandardPoller( String inName, long inPollerFrequencyMS, final PollerFeedApproverIF inApprover)
	{
		super(inName);

		m_Approver = inApprover;

	//	synchronized (this)
		{
	//		if ( s_InformaPoller == null)
			{
				///////////////////////////////////////////////////////////////////////  (AGR) 25 October 2008

				CompositeApprover	theApprover = new CompositeApprover();

				theApprover.add( new SizeLimitedPollerApprover() );

				///////////////////////////////////////////////////////////////////////

				m_InformaPoller = new de.nava.informa.utils.poller.Poller( 5 /* worker threads */,
							de.nava.informa.utils.poller.Poller.POLICY_SCAN_ALL,
							null, null, new B4LInputStreamProvider(), theApprover);

				m_InformaPoller.setPeriod( inPollerFrequencyMS );

				s_Logger.info( getLogPrefix() + "Created Informa Poller, freq = " + UDates.getFormattedTimeDiff( inPollerFrequencyMS ));
			}
		}
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

	/*******************************************************************************
	*******************************************************************************/
	public PollerFeedApproverIF getFeedApprover()
	{
		return m_Approver;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return "[StandardPoller@" + Integer.toHexString(hashCode()) + ", using " + getFeedApprover() + "]";
	}
}