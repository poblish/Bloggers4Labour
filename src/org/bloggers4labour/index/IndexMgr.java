/*
 * IndexMgr.java
 *
 * Created on June 20, 2005, 8:17 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.bloggers4labour.index;

import com.hiatus.dates.UDates;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.bloggers4labour.FeedUtils;
import org.bloggers4labour.ItemContext;
import org.bloggers4labour.InstallationIF;
import org.bloggers4labour.bridge.channel.ChannelIF;
import org.bloggers4labour.bridge.channel.item.ItemIF;
import org.bloggers4labour.headlines.AddHandler;
import org.bloggers4labour.headlines.HeadlinesIF;
import org.bloggers4labour.headlines.RemoveHandler;

/**
 *
 * @author andrewre
 */
public class IndexMgr
{
	private InstallationIF		m_Install;
	private int			m_DocCount;
	private int			m_Dirtiness = 0;
	private File			m_LuceneRootIndexDir;

	private static Logger		s_Idx_Logger = Logger.getLogger( IndexMgr.class );
	private static File		s_LuceneRootIndexDir = new File("/Users/andrewregan/Development/java/Bloggers4Labour/lucene_index");

	/*******************************************************************************
		(AGR) 22 June 2005. Should really sync, in case someone calls
		setDirectory() again, but that's the last thing we want to do!
	*******************************************************************************/
	public IndexMgr( final InstallationIF inInstall)
	{
		m_Install = inInstall;
		m_LuceneRootIndexDir = new File( s_LuceneRootIndexDir, m_Install.getName());

		////////////////////////////////////////////////////////////////

		IndexWriter	iw = null;
		String		prefix = m_Install.getLogPrefix();


		try
		{
			HeadlinesIF	h = m_Install.getHeadlinesMgr().getIndexablePostsInstance();
			s_Idx_Logger.info( prefix + "IndexMgr: storing in \"" + m_LuceneRootIndexDir + "\"");
			iw = getIndexWriter(true);

			////////////////////////////////////////////////////////  (AGR) 25 June 2005

			if ( h != null)
			{
				NewHeadlineHandler	ah = new NewHeadlineHandler();
				RemoveHandler		rh = new ExpiredHeadlineHandler();

				s_Idx_Logger.info( prefix + "IndexMgr: registering: " + ah + " with " + h);
				h.addHandler(ah);

				s_Idx_Logger.info( prefix + "IndexMgr: making NewHeadlineHandler listen to FeedList events...");
				inInstall.getFeedList().addObserver(ah);

				s_Idx_Logger.info( prefix + "IndexMgr: registering: " + rh + " with " + h);
				h.addHandler(rh);
			}
			else
			{
				s_Idx_Logger.error( prefix + "IndexMgr: no Headlines object to attach to!");
			}
		}
		catch (IOException e)
		{
			s_Idx_Logger.error( prefix + "Clearing Index failed", e);
		}
		finally
		{
			closeIndexWriter(iw);
		}
	}

	/*******************************************************************************
		(AGR) 22 June 2005
	*******************************************************************************/
	public synchronized static void setDirectory( String inDir)
	{
		s_LuceneRootIndexDir = new File(inDir);
	}

	/*******************************************************************************
	*******************************************************************************/
	private synchronized boolean insert( HeadlinesIF inHeads, Document inDoc) // , String inTitle)
	{
		IndexWriter	theWriter = null;

		try
		{
			theWriter = getIndexWriter(false);

			theWriter.addDocument(inDoc);
			m_DocCount = theWriter.docCount();

			// s_Idx_Logger.info("IDX: added  " + (m_DocCount-x0) + " doc ('" + inTitle + "'), total is now " + m_DocCount); // + ", " + inHeads.size() + " in headlines");

			m_Dirtiness++;

			return true;
		}
		catch (IOException err)
		{
			s_Idx_Logger.error( "insert()", err);
		}
		finally
		{
			closeIndexWriter(theWriter);
		}

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	private synchronized boolean insertDocuments( List<Document> inDocsList)
	{
		IndexWriter	theWriter = null;

		try
		{
			theWriter = getIndexWriter(false);

		//	int	x0 = theWriter.docCount();

			for ( Document eachDoc : inDocsList)
			{
				theWriter.addDocument(eachDoc);
			}

			m_DocCount = theWriter.docCount();

			// s_Idx_Logger.info("IDX: added  " + (m_DocCount-x0) + " doc ('" + inTitle + "'), total is now " + m_DocCount); // + ", " + inHeads.size() + " in headlines");

			m_Dirtiness += inDocsList.size();

			return true;
		}
		catch (IOException err)
		{
			s_Idx_Logger.error( "insert()", err);
		}
		finally
		{
			closeIndexWriter(theWriter);
		}

		return false;
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized void optimise()
	{
		if ( m_Dirtiness >= 5)
		{
			IndexWriter	theWriter = null;

			try
			{
				theWriter = getIndexWriter(false);

				long	beforeMS = System.currentTimeMillis();
				theWriter.optimize();
				long	afterMS = System.currentTimeMillis();

				m_Dirtiness = 0;
				m_DocCount = theWriter.docCount();
				s_Idx_Logger.info( m_Install.getLogPrefix() + "IDX: optimised ->  " + m_DocCount + " docs in " + UDates.getFormattedTimeDiff( afterMS - beforeMS) + ".");
			}
			catch (IOException err)
			{
				s_Idx_Logger.error( m_Install.getLogPrefix() + "optimise()", err);
			}
			finally
			{
				closeIndexWriter(theWriter);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized int remove( HeadlinesIF inHeads, Term inTerm)
	{
		if (IndexReader.indexExists(m_LuceneRootIndexDir))
		{
			IndexReader	theReader = null;

			try
			{
				theReader = IndexReader.open(m_LuceneRootIndexDir);

				int	x = theReader.deleteDocuments(inTerm);

				m_DocCount = theReader.numDocs();
				m_Dirtiness++;

				return x;
			}
			catch (IOException e)
			{
				s_Idx_Logger.error( m_Install.getLogPrefix() + "remove", e);
			}
			finally
			{
				closeIndexReader(theReader);
			}
		}

		return 0;
	}

	/*******************************************************************************
	*******************************************************************************/
	private IndexWriter getIndexWriter( boolean inClearContents) throws IOException
	{
		IndexWriter	theWriter;

		if ( !inClearContents && IndexReader.indexExists(m_LuceneRootIndexDir))
		{
			theWriter = new IndexWriter( m_LuceneRootIndexDir, new StandardAnalyzer(), false);

			// m_Logger.info("existing writer = " + theWriter);
		}
		else
		{
			theWriter = new IndexWriter( m_LuceneRootIndexDir, new StandardAnalyzer(), true);

			s_Idx_Logger.info( m_Install.getLogPrefix() + "new writer = " + theWriter);
		}

		return theWriter;
	}

	/*******************************************************************************
	*******************************************************************************/
	private void closeIndexReader( IndexReader inReader)
	{
		if ( inReader != null)
		{
			try
			{
				inReader.close();
			}
			catch (IOException err)
			{
				s_Idx_Logger.error( m_Install.getLogPrefix() + "closeIndexReader()", err);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void closeIndexSearcher( IndexSearcher inSearcher)
	{
		if ( inSearcher != null)
		{
			try
			{
				inSearcher.close();
			}
			catch (IOException err)
			{
				s_Idx_Logger.error( m_Install.getLogPrefix() + "closeIndexSearcher()", err);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	private void closeIndexWriter( IndexWriter inWriter)
	{
		if ( inWriter != null)
		{
			try
			{
				inWriter.close();
			}
			catch (IOException err)
			{
				s_Idx_Logger.error( m_Install.getLogPrefix() + "closeIndexWriter()", err);
			}
		}
	}

	/*******************************************************************************
	*******************************************************************************/
	public synchronized int numDocs()
	{
		return m_DocCount;
	}

	/*******************************************************************************
	*******************************************************************************/
	public List<SearchMatch> runQuery( final String inQueryStr)
	{
		if (IndexReader.indexExists(m_LuceneRootIndexDir))
		{
			IndexReader	theReader = null;
			IndexSearcher	theSearcher = null;

			try
			{
				theReader = IndexReader.open(m_LuceneRootIndexDir);
				theSearcher = new IndexSearcher(theReader);

				// int	theNumDocs = theReader.numDocs();

				QueryParser	theParser = new QueryParser( "desc", new StandardAnalyzer());

				return _runQuery( theReader, theSearcher, theParser.parse(inQueryStr));
			}
			catch (IOException e)
			{
				s_Idx_Logger.error("", e);
			}
			catch (ParseException e)
			{
				s_Idx_Logger.error("", e);
			}
			finally
			{
				closeIndexSearcher(theSearcher);
				closeIndexReader(theReader);
			}
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	public List<SearchMatch> runQuery( final Query inQuery)
	{
		if (IndexReader.indexExists(m_LuceneRootIndexDir))
		{
			IndexReader	theReader = null;
			IndexSearcher	theSearcher = null;

			try
			{
				theReader = IndexReader.open(m_LuceneRootIndexDir);
				theSearcher = new IndexSearcher(theReader);

				return _runQuery( theReader, theSearcher, inQuery);
			}
			catch (IOException e)
			{
				s_Idx_Logger.error("", e);
			}
			finally
			{
				closeIndexSearcher(theSearcher);
				closeIndexReader(theReader);
			}
		}

		return null;
	}

	/*******************************************************************************
	*******************************************************************************/
	private List<SearchMatch> _runQuery( final IndexReader inReader, final IndexSearcher inSearcher, Query inQuery) throws IOException
	{
		Hits	theHits = inSearcher.search(inQuery);

		if ( theHits != null)
		{
			int			theLength = theHits.length();
			List<SearchMatch>	theList = new ArrayList<SearchMatch>(theLength);

			for ( int i = 0; i < theLength; i++)
			{
				if (!inReader.isDeleted(i))
				{
					theList.add( new SearchMatch( m_Install, theHits.score(i), theHits.doc(i)) );
				}
			}

			return theList;
		}

		return null;
	}

	/*******************************************************************************
		(AGR) 25 June 2005
	*******************************************************************************/
	private class NewHeadlineHandler implements AddHandler, Observer
	{
		private ArrayList<Document>	m_ListOfSnapshotDocuments = new ArrayList<Document>(200);

		/*******************************************************************************
		*******************************************************************************/
		NewHeadlineHandler()
		{
		}

		/*******************************************************************************
		*******************************************************************************/
		public void update( Observable inFeedList, Object inUnusedArgs)
		{
			completedSnapshot();
		}

		/*******************************************************************************
		*******************************************************************************/
		public synchronized void completedSnapshot()
		{
			if ( m_ListOfSnapshotDocuments != null && m_ListOfSnapshotDocuments.size() > 0)
			{
				s_Idx_Logger.info( m_Install.getLogPrefix() + "NewHeadlineHandler: inserting " + m_ListOfSnapshotDocuments.size() + " Documents into index...");

				insertDocuments(m_ListOfSnapshotDocuments);

				m_ListOfSnapshotDocuments.clear();
			}
			else
			{
				// s_Idx_Logger.info( m_Install.getLogPrefix() + "NewHeadlineHandler: nothing to store in index...");
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		public void onAdd( final InstallationIF inInstall, HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
		{
			try
			{
				_onAdd( inHeads, inItem, inCtxt);
			}
			catch (Exception e)	// Yuk, but necessary!
			{
				s_Idx_Logger.error( "", e);
			}
		}

		/*******************************************************************************
		*******************************************************************************/
		private void _onAdd( HeadlinesIF inHeads, final ItemIF inItem, final ItemContext inCtxt)
		{
			ChannelIF	theChannel = inItem.getOurChannel();
			Date		itemDate = FeedUtils.getItemDate(inItem);

			if ( itemDate == null)	// (AGR) 13 August 2008
			{
				return;		// No! We do not accept posts with NULL dates, so might as well skip indexing now.
			}

			////////////////////////////////////////////////////////

			Document	theDocument = new Document();
			String		theDescr = FeedUtils.stripHTML( inItem.getDescription() );
			String		theTitle = FeedUtils.adjustTitle(inItem);

			theDocument.add( new Field( "desc", theDescr, Field.Store.YES, Field.Index.TOKENIZED) );
			theDocument.add( new Field( "title", theTitle, Field.Store.YES, Field.Index.TOKENIZED) );

			// Need to store this so we can use it as a key for deleting this entry later!

			theDocument.add( new Field( "item_id", Long.toString( inItem.getId() ), Field.Store.YES, Field.Index.UN_TOKENIZED) );
			theDocument.add( new Field( "item_time_ms", Long.toString( itemDate.getTime() ), Field.Store.YES, Field.Index.NO) );

			URL	theLink = inItem.getLink();

			if ( theLink != null)	// (AGR) 13 Jan 2006. Groan...
			{
				theDocument.add( new Field( "item_link", theLink.toString(), Field.Store.YES, Field.Index.NO) );
			}

			try {
				theDocument.add( new Field( "channel_site", theChannel.getSite().toString(), Field.Store.YES, Field.Index.NO) );
			}
			catch (RuntimeException e)	// (AGR) 26 July 2005
			{
			}

			// System.out.println("Adding Doc '" + theTitle + "' to " + this);

			////////////////////////////////////////////////////////  (AGR) 23 Feb 2006. For performance reasons...

			if ( inCtxt == ItemContext.SNAPSHOT)
			{
				synchronized (this)
				{
					m_ListOfSnapshotDocuments.add(theDocument);

//					s_Idx_Logger.info( m_Install.getLogPrefix() + "NewHeadlineHandler.onAdd: " +
//							   m_ListOfSnapshotDocuments.size() + " in list, skipping SNAPSHOT for " + FeedUtils.adjustTitle(inItem));
				}

				return;	
			}
			
			////////////////////////////////////////////////////////

			insert( inHeads, theDocument); //, theTitle);
		}
	}

	/*******************************************************************************
		(AGR) 25 June 2005
	*******************************************************************************/
	private class ExpiredHeadlineHandler implements RemoveHandler
	{
		/*******************************************************************************
		*******************************************************************************/
		public void onRemove( final InstallationIF inInstall, HeadlinesIF inHeads, final ItemIF inItem)
		{
			remove( inHeads, new Term( "item_id", Long.toString( inItem.getId() ) ) );
		}
	}
}
