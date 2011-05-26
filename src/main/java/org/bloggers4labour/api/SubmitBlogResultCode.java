/*
 * SubmitBlogResultCode.java
 *
 * Created on 27 April 2007, 23:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.api;

/**
 *
 * @author andrewre
 */
public enum SubmitBlogResultCode
{
	UNKNOWN(-999),

	OK(0),

	MISSING_BLOG_NAME(-1),
	UNACCEPTABLE_BLOG_NAME(-2),
	DUPLICATE_BLOG_NAME(-3),

	INVALID_LOCATION(-7),
	INVALID_CREATOR_STATUS(-8),
	INVALID_SITE_CATEGORY(-9),

	MISSING_BLOG_URL(-10),
	UNACCEPTABLE_BLOG_URL(-11),
	UNACCEPTABLE_BLOG_URL_PREFIX(-12),
	DUPLICATE_BLOG_URL(-13),

	UNACCEPTABLE_FEED_URL_PREFIX(-20),
	FEED_URL_NOT_FOUND(-21),

	UNACCEPTABLE_POSTCODE(-30),
	UNACCEPTABLE_DESCRIPTION(-31),

	MISSING_FORENAME(-40),
	MISSING_SURNAME(-41),
	MISSING_EMAIL(-42),

	DB_CONN_ERROR(-50),
	DB_ERROR_1(-51),
	DB_ERROR_2(-52),
	DB_ERROR_3(-53);

	/*******************************************************************************
	*******************************************************************************/
	private final int m_Code;

	/*******************************************************************************
	*******************************************************************************/
//	private int getCode()   { return m_Code; }

	/*******************************************************************************
	*******************************************************************************/
	SubmitBlogResultCode( final int inCode)
	{
		m_Code = inCode;
	}
}
