/*
 * PollerConfig.java
 *
 * Created on 28 October 2006, 20:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class PollerConfig
{
	private Constructor	m_Ctor;
	private long		m_FreqMS;

	private static Logger	s_PC_Logger = Logger.getLogger("Main");

	/*******************************************************************************
		(AGR) 28 October 2006
	*******************************************************************************/
	public PollerConfig( final Constructor inCtor, long inPollerFrequencyMS)
	{
		m_Ctor = inCtor;
		m_FreqMS = inPollerFrequencyMS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Poller newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Poller	p = (Poller) m_Ctor.newInstance( new Object[]{ m_FreqMS } );

		s_PC_Logger.info("Created Poller: " + p);

		return p;
	}
}