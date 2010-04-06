/*
 * InstallationIF.java
 *
 * Created on 02 April 2006, 03:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour;

import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.bloggers4labour.activity.LastPostTableIF;
import org.bloggers4labour.cats.CategoriesTableIF;
import org.bloggers4labour.favicon.FaviconManagerIF;
import org.bloggers4labour.feed.FeedListIF;
import org.bloggers4labour.feed.check.consume.FeedCheckerConsumerDelegateIF;
import org.bloggers4labour.feed.check.consume.FeedCheckerListenerIF;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.index.IndexMgrIF;
import org.bloggers4labour.installation.tasks.InstallationTaskIF;
import org.bloggers4labour.jmx.Management;
import org.bloggers4labour.mail.DigestSenderIF;
import org.bloggers4labour.polling.PollerIF;
import org.bloggers4labour.sql.QueryBuilderIF;

/**
 *
 * @author andrewre
 */
public interface InstallationIF extends FeedCheckerConsumerDelegateIF
{
	public String getName();

	public DataSource getDataSource();
	public HeadlinesMgr getHeadlinesMgr();
	public Management getManagement();
	public FeedListIF getFeedList();
	public IndexMgrIF getIndexMgr();
	public FaviconManagerIF getFaviconManager();

	public boolean hasPollers();
	public Iterable<PollerIF> getPollers();

	public ResourceBundle getBundle();
	public ResourceBundle getBundle( Locale inLocale);

	public CategoriesTableIF getCategories();
	public LastPostTableIF getLastPostDateTable();

	public DigestSenderIF getDigestSender();

	public Iterable<? extends InstallationTaskIF> getTasks();
	public void setTasks( Collection<InstallationTaskIF> inTasks);

	public String getLogPrefix();
	public InstallationStatus getStatus();
	public void restart();
	public void stop();

	QueryBuilderIF getQueryBuilder();

	long getMaxAgeMSecs();

	void setHeadlinesMgr( HeadlinesMgr inHeadsMgr);
	void setIndexMgr( IndexMgr inIndexMgr);
	void complete();

	Collection<FeedCheckerListenerIF> getFeedCheckerListeners();
}