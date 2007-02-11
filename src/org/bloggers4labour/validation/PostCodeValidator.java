/*
 * PostCodeValidator.java
 *
 * Created on 11 February 2007, 13:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.validation;

import com.hiatus.UText;
import java.util.regex.Pattern;
import static org.bloggers4labour.validation.PostCodeResult.*;

/**
 *
 * @author andrewre
 */
public class PostCodeValidator
{
	// Found this pattern here: http://www.govtalk.gov.uk/gdsc/schemaHtml/bs7666-v2-0-xsd-PostCodeType.htm

	private final static Pattern	s_CheckPattern = Pattern.compile("(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})", Pattern.CASE_INSENSITIVE);

	/*******************************************************************************
	*******************************************************************************/
	public PostCodeValidator()
	{
	}

	/*******************************************************************************
	*******************************************************************************/
	public PostCodeResult validate( final String inStr)
	{
		if ( inStr == null)
		{
			return BLANK_IS_OK;
		}

		String	s = inStr.trim();

		if ( s.length() < 1)
		{
			return BLANK_IS_OK;
		}

		return s_CheckPattern.matcher(s).matches() ? VALID : INVALID;
	}

	/*******************************************************************************
	*******************************************************************************/
	public static void main( String[] args)
	{
		doCheck( VALID, "BN3 3JN");
		doCheck( VALID, " WC2H 9DL");
		doCheck( VALID, "EC2A 4BT");
		doCheck( VALID, "sw1 0aa");
	//	doCheck( INVALID, "XX3 3JN");
		doCheck( VALID, "BN3 3JN");
		doCheck( VALID, "W1 4LJ");
		doCheck( BLANK_IS_OK, "");
		doCheck( BLANK_IS_OK, null);
		doCheck( BLANK_IS_OK, "  ");
		doCheck( INVALID, "BN3");
	}

	/*******************************************************************************
	*******************************************************************************/
	private static void doCheck( final PostCodeResult inExpected, final String inStr)
	{
		PostCodeValidator instance = new PostCodeValidator();

		System.out.println("validate..." + inStr);
		assert( inExpected == instance.validate(inStr));
	}
}
