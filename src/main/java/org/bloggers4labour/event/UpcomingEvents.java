/*
 * UpcomingEvents.java
 *
 * Created on 26 January 2007, 16:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.event;

import com.hiatus.sql.ResultSetList;
import com.hiatus.sql.USQL_Utils;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.sql.DataSourceConnection;

/**
 *
 * @author andrewre
 */
public abstract class UpcomingEvents
{
	private ResultSetList	m_ResultsCopy;

	private static Logger	s_Logger = Logger.getLogger( UpcomingEvents.class );

	/*******************************************************************************
		(AGR) 27 Jan 2007
	*******************************************************************************/
	public UpcomingEvents( final InstallationIF inInstall, int inNumEvents)
	{
		DataSourceConnection	theConnectionObject = null;

		try
		{
			theConnectionObject = new DataSourceConnection( inInstall.getDataSource() );
			if (theConnectionObject.Connect())
			{
				Statement	theS = null;

				try
				{
					theS = _getEvents( theConnectionObject, inNumEvents);
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
			if ( theConnectionObject != null)
			{
				theConnectionObject.CloseDown();
			}
		}
	}
	
	/*******************************************************************************
		(AGR) 27 Jan 2007
	*******************************************************************************/
	private CallableStatement _getEvents( final DataSourceConnection inConn, int inNumEvents) throws SQLException
	{
		CallableStatement	theS = inConn.prepareCall("getNextEvents( ? )");

		theS.setInt( 1, inNumEvents);

		boolean	x = theS.execute();

		s_Logger.info("ok? " + x);

		ResultSet	theRS = theS.getResultSet();

		s_Logger.info("theRS " + theRS);

		m_ResultsCopy = new ResultSetList(theRS);

		s_Logger.info("m_ResultsCopy " + m_ResultsCopy);

		theRS.close();

		return theS;
	}

	/*******************************************************************************
	*******************************************************************************/
	public ResultSetList getResults()
	{
		return m_ResultsCopy;
	}
}
