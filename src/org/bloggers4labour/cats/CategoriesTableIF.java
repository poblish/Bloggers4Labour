/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.cats;

import java.util.Collection;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import static org.bloggers4labour.Constants.ONE_DAY_MSECS;

/**
 *
 * @author andrewregan
 */
public interface CategoriesTableIF
{
	public final static long	MAX_CATEGORY_AGE_MSECS = ONE_DAY_MSECS * 5;    // (AGR) 4 March 2006. Was a week. (AGR) 19 May 2005

	public void addCategories( ItemIF inItem);

	public boolean hasEntries();
	public int entriesCount();
	public Collection<String> getCategories();
	public Collection<ItemIF> getCategoryEntries( String inCategoryName);
	public Collection<RankingObject> getRankedCategories();

	public int getFontSize( int inCount, int inMinFontSize, int inDefaultSize, int inMaxFontSize);
}