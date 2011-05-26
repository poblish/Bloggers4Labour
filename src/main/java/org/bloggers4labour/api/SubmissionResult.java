/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.api;

import com.hiatus.sql.ResultSetList;
import org.bloggers4labour.geocode.GeocodingResultCode;

/**
 *
 * @author andrewregan
 */
public class SubmissionResult
{
	protected String		m_Message;
	private SubmitBlogResultCode	m_Code;
	protected GeocodingResultCode	m_GeocodingResult = GeocodingResultCode.NOT_AVAILABLE;
	protected ResultSetList		m_Results;
	protected Long			m_SiteRecno;
	protected long			m_ElapsedTimeMSecs;

	/*******************************************************************************
	*******************************************************************************/
	SubmissionResult()
	{
		m_Code = SubmitBlogResultCode.UNKNOWN;
	}

	/*******************************************************************************
	*******************************************************************************/
	SubmissionResult( final String inS, final SubmitBlogResultCode inCode)
	{
		m_Message = inS;
		m_Code = inCode;
	}

	/*******************************************************************************
	*******************************************************************************/
	public SubmitBlogResultCode getCode()
	{
		return m_Code;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setMessageAndCode( final String inS, final SubmitBlogResultCode inCode)
	{
		m_Message = inS;
		m_Code = inCode;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setGeocodingResult( final GeocodingResultCode x)
	{
		m_GeocodingResult = x;
	}

	/*******************************************************************************
	*******************************************************************************/
	public GeocodingResultCode getGeocodingResult()
	{
		return m_GeocodingResult;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setNearbyBloggers( final ResultSetList x)
	{
		m_Results = x;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setNewSiteRecno( final Long x)
	{
		m_SiteRecno = x;
	}

	/*******************************************************************************
	*******************************************************************************/
	public void setElapsedTime( final long inMS)
	{
		m_ElapsedTimeMSecs = inMS;
	}
}