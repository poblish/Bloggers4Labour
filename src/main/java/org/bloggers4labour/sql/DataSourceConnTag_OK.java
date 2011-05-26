/*
 * DataSourceConnTag_OK.java
 *
 * Created on 02 April 2006, 00:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.sql;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.log4j.Logger;

/**
 *
 * @author andrewre
 */
public class DataSourceConnTag_OK extends BodyTagSupport
{
	private DataSourceConnTag	m_ParentTag;
//	private String			m_ConnStr;
	private Logger			m_Logger;

	private static final long	serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	public DataSourceConnTag_OK()
	{
		super();

		m_Logger = Logger.getLogger( getClass() );
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int doStartTag() throws JspException
	{
		int	theResult = BodyTag.SKIP_BODY;

		pageContext.removeAttribute("ds_conn");	// m_ConnStr

		try
		{
			if ( m_ParentTag == null)
			{
				m_ParentTag = (DataSourceConnTag) findAncestorWithClass( this, Class.forName("org.bloggers4labour.sql.DataSourceConnTag") );
				// m_Logger.debug("m_ParentTag = " + m_ParentTag);
			}

			if (m_ParentTag.isConnectedOK())
			{
				theResult = EVAL_BODY_BUFFERED;

				// m_Logger.debug("connected ok, include body");

				pageContext.setAttribute( "ds_conn", m_ParentTag.getConnectionOpened());
				// pageContext.setAttribute( m_ConnStr, m_ParentTag.getConnectionOpened());
			}
			else
			{
				m_Logger.info("couldn't connect, SKIP body");
			}
		} 
		catch (Exception err)
		{
			m_Logger.error( "DataSourceConnTag_OK::doStartTag()", err);
		}

		return theResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int doEndTag() throws JspException
	{
//		m_Logger.debug("in doEndTag()");

		return BodyTag.EVAL_PAGE;
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public int doAfterBody() throws JspException
	{
		BodyContent	theBody = getBodyContent();

		try
		{
			// m_Logger.debug("in doAfterBody(), writing to " + theBody);

			theBody.writeOut( getPreviousOut() );
		}
		catch (IOException err)
		{
			m_Logger.error( "in DataSourceConnTag_OK::doAfterBody()", err);
		}

		theBody.clearBody();

		return BodyTag.SKIP_BODY;
	}
}