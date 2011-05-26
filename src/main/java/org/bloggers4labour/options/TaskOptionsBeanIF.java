/*
 * TaskOptionsBean.java
 *
 * Created on May 24, 2005, 11:40 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.options;

import java.beans.PropertyVetoException;

/**
 *
 * @author andrewre
 */
public interface TaskOptionsBeanIF
{
	/*******************************************************************************
	*******************************************************************************/
	public long	getDelayMsecs();
	public long	getPeriodMsecs();

	/*******************************************************************************
	*******************************************************************************/
	public void	setDelayMsecs( long x) throws PropertyVetoException;
	public void	setPeriodMsecs( long x) throws PropertyVetoException;
}
