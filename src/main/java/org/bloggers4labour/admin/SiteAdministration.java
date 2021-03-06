/*
 * SiteAdministration.java
 *
 * Created on 27 May 2006, 09:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.admin;

import com.hiatus.sql.USQL_Utils;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.sql.DataSourceConnection;

/**
 *
 * @author andrewre
 */
public class SiteAdministration
{
	private static Logger		s_Logger = Logger.getLogger( SiteAdministration.class );

	private final static String	LINE_PREFIX = ">>> cleanup(): ";

	/*******************************************************************************
	*******************************************************************************/
	public static void main( String[] args)
	{
		Configuration.getInstance().setDirectoryIfNotSet("/Users/andrewre/www/htdocs/bloggers4labour/conf/");

		DataSourceConnection	theConnectionObject = null;

		try
		{
			MysqlDataSource	theSource = new MysqlDataSource();
			theSource.setUrl("jdbc:mysql://localhost:3306/Bloggers4Labour?user=root&password=Militant&useUnicode=true");

			theConnectionObject = new DataSourceConnection(theSource);
			if (theConnectionObject.Connect())
			{
				// s_Logger.info("conn = " + theConnectionObject);

				Statement	theS = null;

				try
				{
					SiteAdministration	sa = new SiteAdministration();
					boolean			commitChanges = true;

					theS = theConnectionObject.createStatement();

					sa.cleanupAll( theS, commitChanges);

					s_Logger.info("--- Done intial cleanup");

					sa.deleteSite( theS, 306, commitChanges);

/*					for ( int siteRecno = 350; siteRecno > 0; siteRecno--)
					{
						sa.deleteSite( theS, siteRecno, commitChanges);
					}
*/
				}
				catch (SQLException e)
				{
					s_Logger.error("creating statement", e);
				}
				finally
				{
					USQL_Utils.closeStatementCatch(theS);
				}
			}
			else
			{
				s_Logger.warn("Cannot connect!");
			}
		}
		catch (RuntimeException err)
		{
			s_Logger.error("???", err);
		}
		finally
		{
			// s_FL_Logger.info("m_FeedChannels = " + m_FeedChannels);

			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public SiteAdministration()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public ResultSet getUnapprovedBlogs( final InstallationIF inInstall, Statement inStatement) throws SQLException
	{
		ResultSet	theRS = inStatement.executeQuery( inInstall.getQueryBuilder().getUnapprovedBlogsQuery() );

		return theRS; // new ResultSetList(theRS);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void approveSite( Statement inStatement, int inSiteRecno, boolean inCommitChanges) throws SQLException
	{
		ResultSet	theRS = inStatement.executeQuery("SELECT site_recno FROM site WHERE site_recno=" + inSiteRecno);

		if (theRS.next())
		{
			if (inCommitChanges)
			{
				int	numUpdates = inStatement.executeUpdate("UPDATE site SET approved=1 WHERE site_recno=" + inSiteRecno);

				if ( numUpdates > 0)
				{
					s_Logger.info(">>> approveSite(): Site " + inSiteRecno + " approved OK.");
				}
				else
				{
					s_Logger.error(">>> approveSite(): Site " + inSiteRecno + " approval FAILED.");
				}
			}
			else
			{
				s_Logger.info(">>> approveSite(): Site " + inSiteRecno + " found OK.");
			}
		}
		else
		{
			s_Logger.warn(">>> approveSite(): Site " + inSiteRecno + " not found.");
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public void deleteSite( Statement inStatement, int inSiteRecno, boolean inCommitChanges) throws SQLException
	{
		ResultSet	theRS = inStatement.executeQuery("SELECT site_recno FROM site WHERE site_recno=" + inSiteRecno);

		if (theRS.next())
		{
			if (inCommitChanges)
			{
				int	numDeletions = inStatement.executeUpdate("DELETE FROM site WHERE site_recno=" + inSiteRecno);

				if ( numDeletions > 0)
				{
					s_Logger.info(">>> deleteSite(): Site " + inSiteRecno + " deleted OK.");
				}
				else
				{
					s_Logger.error(">>> deleteSite(): Site " + inSiteRecno + " deletion FAILED.");
				}
			}
			else
			{
				s_Logger.info(">>> deleteSite(): Site " + inSiteRecno + " found OK.");
			}
		}
		else
		{
			s_Logger.warn(">>> deleteSite(): Site " + inSiteRecno + " not found.");
		}

//		if ( Math.random() >= 0.5)
		{
			cleanupAll( inStatement, inCommitChanges);
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean cleanupAll( Statement inStatement, boolean inCommitChanges) throws SQLException
	{
		CleanupContext	theCtxt = new CleanupContext();
		boolean		neededCleanup = false;

		while (cleanup( inStatement, theCtxt, inCommitChanges))
		{
			neededCleanup = true;
		}

		return neededCleanup;
	}

	/*******************************************************************************
	*******************************************************************************/
	private boolean cleanup( Statement inStatement, CleanupContext ioCtxt, boolean inCommitChanges) throws SQLException
	{
		ResultSet	rs;
		String		sitesQuery = ioCtxt.getSitesQuery();
		boolean		uncleanRecsFound = false;

//		s_Logger.info("sitesQuery ==> " + sitesQuery);

		if ( sitesQuery == null)
		{
			StringBuffer	srBuf = new StringBuffer(500);

			rs = inStatement.executeQuery("SELECT site_recno FROM site");

			while (rs.next())
			{
				if ( srBuf.length() < 1)
				{
					srBuf.append("(" + rs.getLong(1));
				}
				else	srBuf.append("," + rs.getLong(1));
			}

			if ( srBuf.length() >= 1)
			{
				srBuf.append(")");
			}

			sitesQuery = srBuf.toString();
			ioCtxt.setSitesQuery(sitesQuery);	// cache this, as the site doesn't change for a CleanupContext
		}

//		s_Logger.info("ste ==> " + sitesQuery);

		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////

		rs = inStatement.executeQuery("SELECT creator_recno FROM creator");

		List<Number>	crList = new ArrayList<Number>(100);

		while (rs.next())
		{
			crList.add( Long.valueOf( rs.getLong("creator_recno") ) );	// (AGR) 29 Jan 2007. FindBugs: changed from new Long
		}

		// %><p><%= crList %> = <%= crList.size() %> "creator" recs</p><%

		////////////////////////////////////////////////////////////////////////

		rs = inStatement.executeQuery("SELECT creator_recno FROM siteCreators");

		List<Number>	crList2 = new ArrayList<Number>(100);

		while (rs.next())
		{
			crList2.add( Long.valueOf( rs.getLong("creator_recno") ) );	// (AGR) 29 Jan 2007. FindBugs: changed from new Long
		}

		// %><p><%= crList2 %> = <%= crList2.size() %> "creator" recs</p><%

		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////

		String	scrQuery;

		if ( sitesQuery.length() > 0)
		{
			scrQuery = "SELECT * FROM siteCreators WHERE site_recno NOT IN " + sitesQuery;
		}
		else	// (AGR) 27 May 2006. Could only happen if there were no Site records but left-over ancillary recs
		{
			scrQuery = "SELECT * FROM siteCreators";
		}

		// s_Logger.info("scr ==> " + scrQuery);

		rs = inStatement.executeQuery(scrQuery);

		StringBuffer	scrBuf = new StringBuffer(500);
		int		scrCount = 0;

		while (rs.next())
		{
			if ( scrBuf.length() < 1)
			{
				scrBuf.append("(" + rs.getLong(1));
			}
			else	scrBuf.append("," + rs.getLong(1));

			scrCount++;
		}

		if ( scrBuf.length() >= 1)
		{
			scrBuf.append(")");
		}

		// %><p><%= scrBuf %> = <%= scrCount %> "siteCreators" recs to delete</p><%

		////////////////////////////////////////////////////////////////////////

		if ( scrCount > 0)
		{
			String	delSCQuery = "DELETE FROM siteCreators WHERE site_creator_recno IN " + scrBuf;

			if (inCommitChanges)
			{
				int	numUpdates = inStatement.executeUpdate(delSCQuery);
				s_Logger.info( LINE_PREFIX + numUpdates + " updates from running \"" + delSCQuery + "\"");
			}
			else
			{
				s_Logger.info( LINE_PREFIX + "Changes required: " + delSCQuery);
			}

			uncleanRecsFound = true;
		}

		////////////////////////////////////////////////////////////////////////

		StringBuffer	crDelBuf = new StringBuffer(500);
		int		crDelCount = 0;

		for ( Number each : crList)
		{
			if (!crList2.contains(each))
			{
				if ( crDelBuf.length() < 1)
				{
					crDelBuf.append("(" + each);
				}
				else	crDelBuf.append("," + each);

				crDelCount++;
			}
		}

		if ( crDelBuf.length() >= 1)
		{
			crDelBuf.append(")");
		}

		// %><p><%= crDelBuf %> = <%= crDelCount %> "creator" recs to delete</p><%

		if ( crDelCount > 0)
		{
			String	q = "DELETE FROM creator WHERE creator_recno IN " + crDelBuf;

			if (inCommitChanges)
			{
				int	numUpdates = inStatement.executeUpdate(q);
				s_Logger.info( LINE_PREFIX + numUpdates + " updates from running \"" + q + "\"");
			}
			else
			{
				s_Logger.info( LINE_PREFIX + "Changes required: " + q);
			}

			deleteOrphanedGLocRecords(inStatement);

			return true;
		}
		else if ( scrCount == 0)
		{
			s_Logger.debug( LINE_PREFIX + "No cleanup required.");
		}

		deleteOrphanedGLocRecords(inStatement);

		return uncleanRecsFound;
	}

	/*******************************************************************************
		(AGR) 11 Feb 2007
	*******************************************************************************/
	private boolean deleteOrphanedGLocRecords( Statement inStatement) throws SQLException
	{
		String		theQuery = "SELECT g.gloc_recno FROM geoLocation g LEFT JOIN site s ON s.gloc_recno=g.gloc_recno WHERE g.gloc_recno > 2 AND s.gloc_recno IS NULL";
		ResultSet	rs = inStatement.executeQuery(theQuery);
//		List<Long>	theRecnos = new ArrayList<Long>(3);
		StringBuilder	sb = new StringBuilder();

		if (rs.next())
		{
			sb.append("DELETE FROM geoLocation WHERE gloc_recno IN (");

			boolean	gotOne = false;

			do
			{
				if (gotOne)
				{
					sb.append(",");
				}
				else	gotOne = true;

				sb.append( Long.valueOf( rs.getLong("gloc_recno") ));
			}
			while (rs.next());

			sb.append(")");

			////////////////////////////////////////////////////////

			int	numDeletions = inStatement.executeUpdate( sb.toString() );

			if ( numDeletions > 0)
			{
				s_Logger.warn(">>> deleteSite(): Deleted " + numDeletions + " orphaned geoLocation recs.");
				return true;
			}
		}

		rs.close();

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	private static class CleanupContext
	{
		private boolean	m_NeedsCleaning;
		private String	m_SitesQuery;

		/*******************************************************************************
		*******************************************************************************/
		public String getSitesQuery()
		{
			return m_SitesQuery;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setSitesQuery( String x)
		{
			m_SitesQuery = x;
		}

		/*******************************************************************************
		*******************************************************************************/
		public void setUnclean()
		{
			m_NeedsCleaning = true;
		}

		/*******************************************************************************
		*******************************************************************************/
		public boolean needsCleaning()
		{
			return m_NeedsCleaning;
		}
	}
}
