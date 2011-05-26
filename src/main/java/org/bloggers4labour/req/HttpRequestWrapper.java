/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.req;

import com.hiatus.text.UText;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author andrewregan
 */
public class HttpRequestWrapper implements ParametersIF
{
	private HttpServletRequest	m_Request;

	/*******************************************************************************
	*******************************************************************************/
	public HttpRequestWrapper( final HttpServletRequest inReq)
	{
		m_Request = inReq;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getParameter( final String inName)
	{
		return m_Request.getParameter(inName);
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getTrimmedRequestString( final String inKey)
	{
		return getTrimmedRequestString( inKey, "");
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getTrimmedRequestString( final String inKey, final String inDefaultVal)
	{
		String	reqVal = m_Request.getParameter(inKey);

		return UText.isValidString(reqVal) ? reqVal.trim() : inDefaultVal;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int parseRequestInt( final String inKey, final int inDefaultVal)
	{
		try
		{
			return Integer.parseInt( m_Request.getParameter(inKey) );
		}
		catch (Exception e)
		{
			return inDefaultVal;
		}
	}
}