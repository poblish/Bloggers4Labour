/*
 * StoreHostNameComparator.java
 *
 * Created on May 30, 2005, 11:15 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.links;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author andrewre
 */
public class StoreHostNameComparator implements Comparator, /* (AGR) 3 Feb 2007. FindBugs recommended this */ Serializable
{
	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public int compare( Object a, Object b)
	{
		String	sa = (String) a;
		String	sb = (String) b;

		String	ssa = sa.startsWith("www.") ? sa.substring(4) : sa;
		String	ssb = sb.startsWith("www.") ? sb.substring(4) : sb;

		return ssa.compareToIgnoreCase(ssb);
	}
};