/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.installation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.bloggers4labour.HeadlinesMgr;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.InstallationStatus;
import org.bloggers4labour.activity.LastPostTableIF;
import org.bloggers4labour.cats.CategoriesTableIF;
import org.bloggers4labour.feed.FeedList;
import org.bloggers4labour.feed.FeedListIF;
import org.bloggers4labour.index.IndexMgr;
import org.bloggers4labour.installation.tasks.InstallationTaskIF;
import org.bloggers4labour.jmx.Management;
import org.bloggers4labour.mail.DigestSenderIF;
import org.bloggers4labour.polling.PollerIF;
import org.bloggers4labour.sql.QueryBuilderIF;

/**
 *
 * @author andrewregan
 */
public class DummyInstallationImpl implements InstallationIF
{
	private final FeedList		m_FeedList;
	private final Management	m_Management;
	private HeadlinesMgr		m_HMgr;

	/*******************************************************************************
	*******************************************************************************/
	public DummyInstallationImpl( String inName, String inBundleName, DataSource inDataSource, String inStatsBeanName,
					final long inMaxItemAgeMSecs,
					Collection<PollerIF> inPollers)
	{
		m_Management = new Management( this, inStatsBeanName);

		m_FeedList = new FeedList(this);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getName()
	{
		return "Dummy";
	}

	/*******************************************************************************
	*******************************************************************************/
	public DataSource getDataSource()
	{
		return null;
	}

	public HeadlinesMgr getHeadlinesMgr()
	{
		return m_HMgr;
	}

	public Management getManagement()
	{
		return m_Management;
	}

	public FeedListIF getFeedList()
	{
		return m_FeedList;
	}

	public IndexMgr getIndexMgr()
	{
		return null;
	}

	public boolean hasPollers()
	{
		return false;
	}

	public Iterable<PollerIF> getPollers()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ResourceBundle getBundle()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ResourceBundle getBundle(Locale inLocale)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public CategoriesTableIF getCategories()
	{
		return null;
	}

	public LastPostTableIF getLastPostDateTable()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public DigestSenderIF getDigestSender()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Iterable<? extends InstallationTaskIF> getTasks()
	{
		return new Iterable<InstallationTaskIF>() {

			public Iterator<InstallationTaskIF> iterator()
			{
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}

	public void setTasks(Collection<InstallationTaskIF> inTasks)
	{
		// NOOP
	}

	public String getLogPrefix()
	{
		return "Dummy";
	}

	public InstallationStatus getStatus()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void restart()
	{
		// NOOP
	}

	public void stop()
	{
		// NOOP
	}

	public QueryBuilderIF getQueryBuilder()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public long getMaxAgeMSecs()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setHeadlinesMgr(HeadlinesMgr inHeadsMgr)
	{
		m_HMgr = inHeadsMgr;
	}

	public void setIndexMgr(IndexMgr inIndexMgr)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void complete()
	{
		// NOOP
	}
}
