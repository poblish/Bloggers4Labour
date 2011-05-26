/*
 * Location.java
 *
 * Created on 15 February 2007, 22:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.geocode;

import java.io.Serializable;

/**
 *
 * @author andrewre
 */
public class Location implements Comparable<Location>, Serializable
{
	private double		m_Latitude;
	private double		m_Longitude;
	private boolean		m_IsBlank;

	final static Location	BLANK = new Location();

	private static final long serialVersionUID = 1L;

	/*******************************************************************************
	*******************************************************************************/
	private Location()
	{
		m_IsBlank = true;
		m_Latitude = m_Longitude = -1.0;
	}

	/*******************************************************************************
	*******************************************************************************/
	Location( final double inLat, final double inLong)
	{
		m_Latitude = inLat;
		m_Longitude = inLong;

		if ( Double.compare( inLat, -1.0) == 0 && Double.compare( inLong, -1.0) == 0)
		{
			m_IsBlank = true;
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public double getLatitude()
	{
		return m_Latitude;
	}

	/*******************************************************************************
	*******************************************************************************/
	public double getLongitude()
	{
		return m_Longitude;
	}

	/*******************************************************************************
	*******************************************************************************/
	public boolean isBlank()
	{
		return m_IsBlank;
	}

	/*******************************************************************************
	*******************************************************************************/
	public int compareTo( Location inOther)
	{
		int	x = Double.compare( m_Latitude, inOther.m_Latitude);

		if ( x != 0)
		{
			return x;
		}

		return Double.compare( m_Longitude, inOther.m_Longitude);
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public boolean equals( Object inOther)
	{
		if ( inOther == null || !( inOther instanceof Location))
		{
			return false;
		}

		return ( compareTo((Location) inOther) == 0);
	}

	/*******************************************************************************
		Copied from Point2D.java (!)
	*******************************************************************************/
	@Override public int hashCode()
	{
		long bits = java.lang.Double.doubleToLongBits( m_Latitude );
		bits ^= java.lang.Double.doubleToLongBits( m_Longitude ) * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	/*******************************************************************************
	*******************************************************************************/
	@Override public String toString()
	{
		if (m_IsBlank)
		{
			return "[Blank]";
		}

		return ("[Lat: " + m_Latitude + ", Long: " + m_Longitude + "]");
	}
}
