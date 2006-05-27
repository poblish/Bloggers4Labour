/*
 * DataSourceConnTag.java
 *
 * Created on 02 April 2006, 00:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import com.hiatus.UText;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.bloggers4labour.InstallationManager;

/**
 *
 * @author andrewre
 */
public class DataSourceConnTag extends TagSupport implements TryCatchFinally
{
	private String			m_InstallationStr;
	private Logger			m_Logger;

	private DataSourceConnection	m_Connection;
	private boolean			m_ConnectedOK;
	private Exception		m_Exception;

	private static Logger		s_Logger = Logger.getLogger("Main");

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnTag()
	{
		super();

		s_Logger = Logger.getLogger( getClass() );
		// System.out.println("in DataSourceConnTag ctor");
	}

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnection getConnectionOpened()
	{
		return m_Connection;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isConnectedOK()
	{
		return m_ConnectedOK;
	}

	/*******************************************************************************
	*******************************************************************************/
	public Exception getExceptionThrown()
	{
		return m_Exception;
	}

	/*******************************************************************************
 	*******************************************************************************/
	public void setInstallation( String x)
	{
		m_InstallationStr = x;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getInstallation()
	{
		return m_InstallationStr;
	}

	/*******************************************************************************
 	*******************************************************************************/
	public int doStartTag() throws JspException
	{
		// System.out.println("in doStartTag(), m_InstallationStr = " + m_InstallationStr);

		try
		{
		//	System.out.println("in doStartTag(), def = " + InstallationManager.getDefaultInstallation());
		//	System.out.println("install = " + InstallationManager.getInstallation(m_InstallationStr));
		//	DataSource	theDS = InstallationManager.getInstallation(m_InstallationStr).getDataSource();

			MysqlDataSource	theDS = new MysqlDataSource();

			if (UText.isValidString(m_InstallationStr))
			{
				theDS.setUrl(m_InstallationStr);
			}
		//	System.out.println("theDS = " + theDS);

			m_Connection = new DataSourceConnection(theDS);
		//	System.out.println("m_Connection = " + m_Connection);
			m_ConnectedOK = m_Connection.Connect();

	//		System.out.println("in doStartTag(), conn = " + m_Connection.getConnection() + ", " + m_ConnectedOK);
		} 
		catch ( Exception err)
		{
			m_Exception = err;
		//	m_Logger.debug("store exception = " + err);
			err.printStackTrace( System.out );
		}

		return BodyTag.EVAL_BODY_INCLUDE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int doEndTag() throws JspException
	{
		s_Logger.debug("in doEndTag(), conn = " + m_Connection);

		releaseConnection();

		return BodyTag.EVAL_PAGE;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void doCatch( Throwable inThrowable)
	{
		s_Logger.error( "doCatch()", inThrowable);
	}

	/*******************************************************************************
	*******************************************************************************/
	public void doFinally()
	{
		releaseConnection();
	}

	/*******************************************************************************
	*******************************************************************************/
	private void releaseConnection()
	{
		if ( m_Connection != null)
		{
			m_Connection.CloseDown();
			m_Connection = null;
		}

		m_ConnectedOK = false;	// (AGR) 22 Feb 2006
	}
}