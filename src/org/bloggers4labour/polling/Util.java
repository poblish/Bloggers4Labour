/*
 * Util.java
 *
 * Created on 28 October 2006, 17:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.polling;

import java.util.Date;
import org.bloggers4labour.AddResult;
import org.bloggers4labour.AgeResult;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.Headlines;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.site.SiteIF;

/**
 *
 * @author andrewre
 */
public class Util
{
	/*******************************************************************************
	*******************************************************************************/
	public static AgeResult getItemAgeMsecs( ItemIF inItem, Date inItemDate, long inCurrentTimeMSecs)
	{
		if ( inItemDate == null)
		{
			return new AgeResult(0);	// !!! This value should never, in practice, be used.
		}

		////////////////////////////////////////////////////////

		long	itemAgeMSecs = inCurrentTimeMSecs - inItemDate.getTime();

// System.out.println( FeedUtils.getDisplayTitle(inItem) + "\t\t" + inItemDate);

		if ( itemAgeMSecs >= 0)
		{
			return new AgeResult(itemAgeMSecs);
		}

// System.out.println( FeedUtils.getDisplayTitle(inItem) + "\t\t" + itemAgeMSecs);

		////////////////////////////////////////////////////////

		Date	d = FeedUtils.adjustFutureItemDate( inItem, inItemDate, itemAgeMSecs);

// System.out.println( "===>\t\t" + d);

		return new AgeResult( inCurrentTimeMSecs - d.getTime());
	}

	/*******************************************************************************
	*******************************************************************************/
	public static AddResult processItem( Headlines ioHeadlines, ItemIF inItem, final SiteIF inSite, long inAgeMSecs, ItemContext inCtxt)
	{
		if (ioHeadlines.isItemAgeOK(inAgeMSecs))
		{
			return ioHeadlines.put( inItem, inSite, inCtxt);
		}

		return AddResult.FAILED_BAD_DATE;
	}
}
