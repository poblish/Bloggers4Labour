/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.cats;

import java.util.Collection;
import org.bloggers4labour.bridge.channel.item.ItemIF;

/**
 *
 * @author andrewregan
 */
public interface CategoriesTableIF
{
	public void addCategories( ItemIF inItem);

	public boolean hasEntries();
	public int entriesCount();
	public Collection<String> getCategories();
	public Collection<ItemIF> getCategoryEntries( String inCategoryName);
	public Collection<RankingObject> getRankedCategories();

	public int getFontSize( int inCount, int inMinFontSize, int inDefaultSize, int inMaxFontSize);

	long getMaxAgeMSecs();
}