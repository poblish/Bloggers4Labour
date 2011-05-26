/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bloggers4labour.req;

import com.hiatus.text.UText;
import java.util.Map;

/**
 *
 * @author andrewregan
 */
public class MapParameters implements ParametersIF
{
	private Map<String,String>	m_Map;

	/*******************************************************************************
	*******************************************************************************/
	public MapParameters( final Map<String,String> inMap)
	{
		m_Map = inMap;
	}

	/*******************************************************************************
	*******************************************************************************/
	public String getParameter( final String inName)
	{
		return m_Map.get(inName);
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
		String	reqVal = m_Map.get(inKey);

		return UText.isValidString(reqVal) ? reqVal.trim() : inDefaultVal;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int parseRequestInt( final String inKey, final int inDefaultVal)
	{
		try
		{
			return Integer.parseInt( m_Map.get(inKey) );
		}
		catch (Exception e)
		{
			return inDefaultVal;
		}
	}
}