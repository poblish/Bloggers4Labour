/*
 * CreatorAdministration.java
 *
 * Created on 05 June 2006, 19:35
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
import org.apache.log4j.Logger;
import org.bloggers4labour.conf.Configuration;
import org.bloggers4labour.sql.DataSourceConnection;

/**
 *
 * @author andrewre
 */
public class CreatorAdministration
{
	private static Logger	s_Logger = Logger.getLogger( CreatorAdministration.class );

	/*******************************************************************************
	*******************************************************************************/
	public static void main( String[] args)
	{
		Configuration.getInstance().setDirectoryIfNotSet("/Users/andrewre/www/htdocs/bloggers4labour/conf/");

		DataSourceConnection	theConnectionObject = null;
		StringBuffer		theBuf;
//		long			currTimeMSecs = System.currentTimeMillis();
		boolean			isGood = false;

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
					CreatorAdministration	ca = new CreatorAdministration();
					boolean			commitChanges = true;

					theS = theConnectionObject.createStatement();

					new SiteAdministration().cleanupAll( theS, commitChanges);

					s_Logger.info("--- Done intial cleanup");

					ca.creatorAdoptSite( theS, 199, 180, commitChanges);
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
	public CreatorAdministration()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public void creatorAdoptSite(  Statement inStatement, int inCreatorRecno, int inSiteRecno, boolean inCommitChanges) throws SQLException
	{
		ResultSet	theRS = inStatement.executeQuery("SELECT creator_recno,site_creator_recno FROM siteCreators WHERE site_recno=" + inSiteRecno);
		int		prevCreatorRecno;
		int		scRecno;

		if (theRS.next())
		{
			// s_Logger.info( theRS.getInt(1) );
			// s_Logger.info( theRS.next() );

			prevCreatorRecno = theRS.getInt(1);

			boolean	noChange = ( prevCreatorRecno == inCreatorRecno) && !theRS.next();

			if (noChange)
			{
				s_Logger.info( "creatorAdoptSite() site #" + inSiteRecno + " already created by #" + inCreatorRecno);
				theRS.close();
				return;
			}

			scRecno = theRS.getInt(2);
			theRS.close();
		}
		else
		{
			s_Logger.warn( "creatorAdoptSite() site #" + inSiteRecno + " not found.");
			theRS.close();
			return;
		}

		///////////////////////////////////////////////////////////////////////////////////////

		if (inCommitChanges)
		{
			int	updCount = inStatement.executeUpdate("UPDATE siteCreators SET creator_recno=" + inCreatorRecno + " WHERE site_creator_recno=" + scRecno);

			if ( updCount == 1)
			{
				s_Logger.info( "creatorAdoptSite() setting site #" + inSiteRecno + "'s creator to #" + inCreatorRecno + " (previously #" + prevCreatorRecno + ").");

				theRS = inStatement.executeQuery("SELECT * FROM siteCreators WHERE creator_recno=" + prevCreatorRecno);
				if (theRS.next())
				{
					// There's still a reference to that Creator (they must have run more than one site), so do nothing

					// s_Logger.info( "creatorAdoptSite() found prev creator.");
				}
				else
				{
					int	delCount = inStatement.executeUpdate("DELETE FROM creator WHERE creator_recno=" + prevCreatorRecno);

					if ( delCount == 1)
					{
						s_Logger.info( "creatorAdoptSite() deleted leftover creator #" + prevCreatorRecno);
					}
					else
					{
						s_Logger.warn( "creatorAdoptSite() FAILED to delete leftover creator #" + prevCreatorRecno);
					}
				}
			}
			else
			{
				s_Logger.warn( "creatorAdoptSite() updCount == " + updCount);
			}
		}
	}
}
