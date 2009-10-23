/*
 * Management.java
 *
 * Created on May 31, 2005, 11:23 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.jmx;

import java.lang.management.ManagementFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;

/**
 *
 * @author andrewre
 */
public class Management
{
	private String			m_BeanName;
	private ObjectName		m_StatsObjectName;
	private Stats			m_Stats;
	private MBeanServer		m_DefaultMBeanServer;

	private static Logger		s_Management_Logger = Logger.getLogger( Management.class );

	/*******************************************************************************
	*******************************************************************************/
	public Management( InstallationIF inInstallation, String inBeanName)
	{
		try
		{
			m_BeanName = inBeanName;
			m_DefaultMBeanServer = ManagementFactory.getPlatformMBeanServer();

			logContents(inInstallation);
		}
		catch (Exception e)
		{
			s_Management_Logger.error( inInstallation.getLogPrefix() + "createMBeanServer", e);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public Stats getStats()
	{
		return m_Stats;
	}

	/*******************************************************************************
	*******************************************************************************/
	private void logContents( InstallationIF inInstallation)
	{
		String	prefix = inInstallation.getLogPrefix();

		s_Management_Logger.info( prefix + "MBeanServer: " + m_DefaultMBeanServer);

		try
		{
			m_StatsObjectName = new ObjectName("org.bloggers4labour", "name", m_BeanName);
			m_Stats = new Stats(inInstallation);
			m_DefaultMBeanServer.registerMBean( m_Stats, m_StatsObjectName);

			// s_Management_Logger.info("theBean = " + m_Stats);
		}
		catch (MalformedObjectNameException e)
		{
			s_Management_Logger.error( prefix + "object name", e);
		}
		catch (InstanceAlreadyExistsException e)
		{
			// Fine, no worries...
		}
		catch (MBeanRegistrationException e)
		{
			s_Management_Logger.error( prefix + "creating bean", e);
		}
		catch (MBeanException e)
		{
			s_Management_Logger.error( prefix + "creating bean", e);
		}
		catch (NotCompliantMBeanException e)
		{
			s_Management_Logger.error( prefix + "creating bean", e);
		}

		////////////////////////////////////////////////////////////////

		// s_Management_Logger.info("count = " + s_Server.getMBeanCount());

/*		Set	theSet = s_Server.queryNames( null, null);
		s_Management_Logger.info("set = " + theSet);

 		for ( Object o : theSet)
		{
			ObjectName	on = (ObjectName) o;

			try
			{
				s_Management_Logger.info("MBean = " + s_Server.getObjectInstance(on));
			}
			catch (InstanceNotFoundException e)
			{
				s_Management_Logger.error( "getting beans", e);
			}
		}
*/
	}
}
