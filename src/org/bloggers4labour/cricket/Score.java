/*
 * Score.java
 *
 * Created on 25 October 2006, 22:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.bloggers4labour.cricket;

import java.util.regex.*;

/**
 *
 * @author andrewre
 */
public class Score
{
	private static Pattern	s_1Day1stInningsPattern = Pattern.compile("([A-Za-z]* *[A-Za-z]+) (.*) v ([A-Za-z]* *[A-Za-z]+)$");
	private static Pattern	s_MatchPattern = Pattern.compile("([A-Za-z]* *[A-Za-z]+) (.*) v ([A-Za-z]* *[A-Za-z]+) ([0-9\\-]*)");

	/*******************************************************************************
	*******************************************************************************/
	public static Score parse( final String inStringToParse)
	{
		return new Score(inStringToParse);
	}

	/*******************************************************************************
	*******************************************************************************/
	private Score( final String inStringToParse)
	{
		Matcher		m = s_MatchPattern.matcher(inStringToParse);

		if (m.find())
		{
			battingTeam = m.group(1);
			currentScore = m.group(2);
			fieldingTeam = m.group(3);
			lastScore = m.group(4);

			displayString = battingTeam + " are " + currentScore + ", against " + fieldingTeam + " who got " + lastScore;
		}
		else
		{
			m = s_1Day1stInningsPattern.matcher(inStringToParse);
			if (m.find())
			{
				battingTeam = m.group(1);
				currentScore = m.group(2);
				fieldingTeam = m.group(3);

				displayString = battingTeam + " are " + currentScore + ", against " + fieldingTeam + " (yet to bat)";
			}
			else
			{
				displayString = "\"" + inStringToParse + "\"";
//				unparseableString = "\"" + FeedUtils.getDisplayTitle(inItem) + "\"";
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public String toString()
	{
		return displayString;
	}

	/**
	 * Holds value of property battingTeam.
	 */
	private String battingTeam;

	/**
	 * Getter for property battingTeam.
	 * @return Value of property battingTeam.
	 */
	public String getBattingTeam()
	{
		return this.battingTeam;
	}

	/**
	 * Setter for property battingTeam.
	 * @param battingTeam New value of property battingTeam.
	 */
	public void setBattingTeam(String battingTeam)
	{
		this.battingTeam = battingTeam;
	}

	/**
	 * Holds value of property fieldingTeam.
	 */
	private String fieldingTeam;

	/**
	 * Getter for property fieldingTeam.
	 * @return Value of property fieldingTeam.
	 */
	public String getFieldingTeam()
	{
		return this.fieldingTeam;
	}

	/**
	 * Setter for property fieldingTeam.
	 * @param fieldingTeam New value of property fieldingTeam.
	 */
	public void setFieldingTeam(String fieldingTeam)
	{
		this.fieldingTeam = fieldingTeam;
	}

	/**
	 * Holds value of property currentScore.
	 */
	private String currentScore;

	/**
	 * Getter for property currentScore.
	 * @return Value of property currentScore.
	 */
	public String getCurrentScore()
	{
		return this.currentScore;
	}

	/**
	 * Setter for property currentScore.
	 * @param currentScore New value of property currentScore.
	 */
	public void setCurrentScore(String currentScore)
	{
		this.currentScore = currentScore;
	}

	/**
	 * Holds value of property lastScore.
	 */
	private String lastScore;

	/**
	 * Getter for property lastScore.
	 * @return Value of property lastScore.
	 */
	public String getLastScore()
	{
		return this.lastScore;
	}

	/**
	 * Setter for property lastScore.
	 * @param lastScore New value of property lastScore.
	 */
	public void setLastScore(String lastScore)
	{
		this.lastScore = lastScore;
	}

	private transient String displayString;
	
}
