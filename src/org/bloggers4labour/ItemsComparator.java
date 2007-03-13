/*
 * ItemsComparator.java
 *
 * Created on May 21, 2005, 6:15 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour;

import de.nava.informa.core.ItemIF;
import java.io.Serializable;
// import de.nava.informa.impl.basic.Item;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author andrewre
 */
public class ItemsComparator implements Comparator<ItemIF>, /* (AGR) 3 Feb 2007. FindBugs recommended this */ Serializable
{
	/*******************************************************************************
	*******************************************************************************/
	public int compare( ItemIF ia, ItemIF ib)
	{
		Date	da = FeedUtils.getItemDate(ia);		// (AGR) 13 March 2005
		Date	db = FeedUtils.getItemDate(ib);		// (AGR) 13 March 2005			

		if ( da == null && db == null)	// shouldn't happen
		{
			return 0;
		}

		if ( da == null)
		{
			return 1;
		}

		if ( db == null)
		{
			return -1;
		}

		/////////////////////////////////////////////////////////

		// (AGR) 4 April 2005. Before, we would do this:  return db.compareTo(da);
		//
		// Horrible! What it meant was that if multiple items were posted at the same
		// time, we could claim they were the SAME. So, when adding them to the TreeMap,
		// we'd end up with ONE item. the key would be the *first* Item added, while the
		// value, would be the *last* one added. Crazy. Now, if the dates are equal, we
		// sort by *title*

		int	dateResult = db.compareTo(da);

		if ( dateResult == 0)	// same date/time, so sort by title
		{
			String	ta = FeedUtils.adjustTitle(ia);		// Strip out any returns, newlines, tabs, etc.
			String	tb = FeedUtils.adjustTitle(ib);		// "

			return ta.compareTo(tb);
		}

		return dateResult;	// different dates...
	}
}
