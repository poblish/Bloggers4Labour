/*
 * InstallationIF.java
 *
 * Created on 02 April 2006, 03:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.bloggers4labour.cats.CategoriesTable;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.jmx.Management;

/**
 *
 * @author andrewre
 */
public interface InstallationIF
{
	public DataSource getDataSource();
	public HeadlinesMgr getHeadlinesMgr();
	public Management getManagement();
	public FeedList getFeedList();
	public IndexMgr getIndexMgr();

	public ResourceBundle getBundle();
	public ResourceBundle getBundle( Locale inLocale);

	public CategoriesTable getCategories();

	public String getLogPrefix();
	public void restart();
	public void stop();
}