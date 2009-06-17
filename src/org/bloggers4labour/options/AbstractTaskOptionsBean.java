/*
 * AbstractTaskOptionsBean.java
 *
 * Created on May 25, 2005, 12:11 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.options;

import com.hiatus.dates.UDates;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;

/**
 *
 * @author andrewre
 */
public abstract class AbstractTaskOptionsBean implements TaskOptionsBeanIF,
				Serializable, VetoableChangeListener //, PropertyChangeListener
{
	protected long			m_Delay;
	protected long			m_Period;
	private PropertyChangeSupport	m_PropChangeSupport;
	private VetoableChangeSupport	m_VetoSupport;

	private final static String	PROPERTY_DELAY = "delayMsecs";
	private final static String	PROPERTY_PERIOD = "periodMsecs";

	/*******************************************************************************
	*******************************************************************************/
	public AbstractTaskOptionsBean()
	{
		m_PropChangeSupport = new PropertyChangeSupport(this);
		// m_PropChangeSupport.addPropertyChangeListener(this);

		m_VetoSupport = new VetoableChangeSupport(this);
		addVetoableChangeListener(this);
	}
	
	/*******************************************************************************
	*******************************************************************************/
	public long getDelayMsecs()
	{
		return m_Delay;
	}

	/*******************************************************************************
	*******************************************************************************/
	public long getPeriodMsecs()
	{
		return m_Period;
	}

	/*******************************************************************************
		(AGR) 29 Jan 2007. FindBugs told me to replace "new Long()" calls
	*******************************************************************************/
	public void setDelayMsecs( long inVal) throws PropertyVetoException
	{
		Long	oldValue = Long.valueOf(m_Delay);

		m_VetoSupport.fireVetoableChange( PROPERTY_DELAY, oldValue, Long.valueOf(inVal));
		m_Delay = inVal;
		m_PropChangeSupport.firePropertyChange( PROPERTY_DELAY, oldValue, Long.valueOf(m_Delay));
	}

	/*******************************************************************************
		(AGR) 29 Jan 2007. FindBugs told me to replace "new Long()" calls
	*******************************************************************************/
	public void setPeriodMsecs( long inVal) throws PropertyVetoException
	{
		Long	oldValue = Long.valueOf(m_Period);

		m_VetoSupport.fireVetoableChange( PROPERTY_PERIOD, oldValue, Long.valueOf(inVal));
		m_Period = inVal;
		m_PropChangeSupport.firePropertyChange( PROPERTY_PERIOD, oldValue, Long.valueOf(m_Period));
	}

	/*******************************************************************************
	*******************************************************************************/
	public void vetoableChange( PropertyChangeEvent inEvent) throws PropertyVetoException
	{
		long	newVal = ((Long) inEvent.getNewValue()).longValue();

		if (inEvent.getPropertyName().equals(PROPERTY_DELAY))
		{
			if ( newVal < 0)
			{
				throw new PropertyVetoException( "Delay value must not be negative.", inEvent);
			}
		}
		else if (inEvent.getPropertyName().equals(PROPERTY_PERIOD))
		{
			if ( newVal <= 0)
			{
				throw new PropertyVetoException( "Period value must be positive.", inEvent);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************
	public void propertyChange( PropertyChangeEvent inEvent)
	{
		System.out.println("Changed \"" + this + "\" from " + inEvent.getOldValue() + " to " + inEvent.getNewValue() + " !");
	}/

	/*******************************************************************************
	*******************************************************************************/
	public void addVetoableChangeListener( VetoableChangeListener listener)
	{
		m_VetoSupport.addVetoableChangeListener(listener);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void removeVetoableChangeListener( VetoableChangeListener listener)
	{
		m_VetoSupport.removeVetoableChangeListener(listener);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		return ( "starting in " + UDates.getFormattedTimeDiff(m_Delay) + " and running every " + UDates.getFormattedTimeDiff(m_Period));
	}
}
