/**
* Copyright IBM Corporation 2009-2017
*
* Licensed under the Eclipse Public License - v 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.eclipse.org/legal/epl-v10.html
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @Author Doug Wood
**/
package psdi.app.bim.parser.cobie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

public class CobieParser

{
	private Parser	         _project	     = null;
	private InputTokenizer   _inputTokenizer = null;
	private InputFile	     _cobieFile	     = null;
	/**
	 * List of supported COBie Sheet names in the order to be processed
	 */
	private final String[] _pageNames = 
	{ 
		Parser.SHEET_FACILITY, 
		Parser.SHEET_FLOOR, 
		Parser.SHEET_SPACE,
        Parser.SHEET_TYPE, 
        Parser.SHEET_COMPONENT, 
        Parser.SHEET_ZONE, 
        Parser.SHEET_SYSTEM,
        Parser.SHEET_CONTACT, 
        Parser.SHEET_JOB,
        Parser.SHEET_RESOURCE,
        Parser.SHEET_SPARE,
        Parser.SHEET_ASSEMBLY,
        Parser.SHEET_ATTRIBUTE, 
        Parser.SHEET_DOCUMENT
    };

	public CobieParser(
	    Parser       project,
	    String       sheetName,
	    InputFile    cobieFile
    )
	    throws ParseException
	{
		_cobieFile      = cobieFile;
		_project        = project;
		_inputTokenizer = getTokenizer(sheetName, cobieFile);
	}

	public void parse(
		Parser                  parser,
	    IdFactory               idFactory,
	    Hashtable<String, Page> parseTree,
	    String[]                list,
	    String                  packageName,
	    long                    flags
    ) 
		throws ParseException
	{

		if( _cobieFile.isExcel() )
			list = _pageNames;

		Page attributePage   = null;
		attributePage = _project.getPage( Parser.SHEET_ATTRIBUTE );
		if( attributePage == null )
		{
			attributePage = new Page( Parser.SHEET_ATTRIBUTE );
			parseTree.put( attributePage.getPageName(), attributePage);
		}

		for( int pageIndex = 0; pageIndex < list.length; pageIndex++ )
		{

			String pageName = list[pageIndex].toUpperCase();

			// Only check for tabs if reading Excel
			if( _cobieFile.isExcel() )
			{
				if( !_cobieFile.processTab(pageName) )
				{
					continue;
				}
				
				// By default, the UI passes in all tabs unless the user eliminates one
				// so this can quietly skip missing tabs
				if( !_inputTokenizer.setCurrentTab(pageName) )
				{
					continue;
				}
			}

			String className = packageName + pageName;

			@SuppressWarnings("unchecked")
			Class<Item> pageClass = getClass(className);

			Page page = _project.getPage(pageName);
			if( page == null )
			{
				page = new Page( pageName );
			}

			long rowCount = 0;
			
			try
			{
				String values[] = _inputTokenizer.getRow();
				rowCount ++;
				page.setColumnNames(values);
				if( values == null )
				{
					continue;
				}
				String colNames[] = new String[values.length];
				Method setters[]  = new Method[values.length];

				for( int i = 0; i < values.length; i++ )
				{
					if( values[i] == null || values[i].length() == 0 )
					{
						continue;
					}
					
					String mappedField = parser.getConvertedField( pageName, values[i] );
					if( mappedField != null && mappedField.length() > 0 )
					{
						colNames[i] = mappedField;
						continue;
					}
					
					try
					{
						colNames[i] = values[i];
						values[i] = values[i].replace( " ", "" );
						Method m = pageClass.getMethod("set" + values[i], String.class);
						setters[i] = m;
					}
					catch( Exception e )
					{
						if( (flags & Parser.FLAG_CONVERT_EXTENSION_COLS) == 0 )
						{
							String params[] = { values[i], pageName };
							if( _project.getLogger() != null )
							{
								_project.getLogger().error(Parser.WRN_MISSING_PROPERTY, params);
							}
						}
					}
				}
				boolean rowError = false;
				
				// try and get a row, inc rowCount always, set rowError on a caught exception
				try {
					values = _inputTokenizer.getRow();
				}
				catch( CellReadException c) 
				{
					rowError = true;
					int cellNum = c.getCellNum();
					String params[] = { list[pageIndex].toUpperCase(), Long.toString(rowCount), colNames[cellNum], Integer.toString(cellNum) };
					if( _project.getLogger() != null )
					{
						_project.getLogger().error(Parser.WRN_INVALID_CELL_VALUE, params);
					}
				}
				finally {
					rowCount ++;
				}
				
				while( values != null )
				{

					// Try to detect and skip blank lines. A line of the same
					// length
					// as values should have
					// only Commas
					boolean content = false;
					for( int i = 0; i < values.length; i++ )
					{
						if( values[i] != null && values[i].length() > 0 )
						{
							content = true;
							break;
						}
					}
					if( !content || rowError)
					{
						// try and get a row, inc rowCount always, set rowError on a caught exception
						try {
							values = _inputTokenizer.getRow();
							rowError = false;  	// reset rowError on a successful read or it'll get stuck here
						}
						catch( CellReadException c) {
							rowError = true;
							int cellNum = c.getCellNum();
							String params[] = { list[pageIndex].toUpperCase(), Long.toString(rowCount), colNames[cellNum], Integer.toString(cellNum) };
							if( _project.getLogger() != null )
							{
								_project.getLogger().error(Parser.WRN_INVALID_CELL_VALUE, params);
							}
						}
						finally {
							rowCount ++;
						}
						continue;
					}

					Item item;
					try
					{
						item = pageClass.newInstance();
					}
					catch( Exception e )
					{
						throw new ParseException(e);
					}

					item.setSourceFile( _cobieFile.getFileName() );
					item.setGuidConversionFlag((flags & Parser.FLAG_CONVERT_GUID) != 0);
					item.setUniqueIdConversionFlag((flags & Parser.FLAG_CONVERT_UNIQUE_IDs) != 0);
					for( int i = 0; i < values.length && i < setters.length; i++ )
					{
						if( values[i] == null )
						{
							continue;
						}
						if( setters[i] != null  )
						{
							try
							{
								setters[i].invoke(item, values[i]);
							}
							catch( Exception e )
							{
								throw new ParseException(e);
							}
						}
						else if( (flags & Parser.FLAG_CONVERT_EXTENSION_COLS) != 0 )
						{
							if(    values[i] != null 
							    && values[i].length() > 0 
								&& !values[i].equalsIgnoreCase( "n/a" ))
							{
								addAttribute( attributePage, item, pageName, colNames[i], values[i] );
							}
						}
					}
					boolean skip = item.skip( parser, flags );
					if( !skip )
					{
						Item dup = page.getItem(item.getKey());
						if(  dup == null || dup.isDuplicat( _project,  item ) )
						{
							item.setUniqueId(idFactory.getUniqueId(pageName));
							page.put(item);
						}
					}
					
					// try and get a row, inc rowCount always, set rowError on a caught exception
					try {
						values = _inputTokenizer.getRow();
						rowError = false;
					}
					catch( CellReadException c) {
						rowError = true;
						int cellNum = c.getCellNum();
						String params[] = { list[pageIndex].toUpperCase(), Long.toString(rowCount), colNames[cellNum], Integer.toString(cellNum) };
						if( _project.getLogger() != null )
						{
							_project.getLogger().error(Parser.WRN_INVALID_CELL_VALUE, params);
						}
					}
					finally {
						rowCount ++;
					}
				}
				
				Thread.yield();
				System.gc();
			}
			catch( IOException e )
			{
				String params[] = { pageName, msgFromException(e) };
				e.printStackTrace();
				throw new ParseException(Parser.ERR_FILE_READ_ERROR, params);
			}
			parseTree.put(page.getPageName(), page );
		}
	}
	
	public void close()
	{
		if( _inputTokenizer != null )
		{
			_inputTokenizer.close();
		}
		_project	    = null;
		_inputTokenizer	= null;
		_cobieFile	    = null;
	}

	/**
	 * 
	 * @param className
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Class getClass(
	    String className) throws ParseException
	{
		Class<Item> pageClass;
		Class<Item> clazz;
		try
		{
			clazz = (Class<Item>) Class.forName(className);
			try
			{
				pageClass = clazz;
			}
			catch( Throwable t )
			{
				throw new ParseException(Parser.ERR_SHEET_INVALID_CLASS);
			}
		}
		catch( ClassNotFoundException e )
		{
			String params[] = { className };
			throw new ParseException(Parser.ERR_SHEET_CLASS_NOT_FOUND, params, e);
		}

		return pageClass;
	}

	private InputTokenizer getTokenizer(
	    String    type,
	    InputFile cobieFile
    ) 
		throws ParseException
	{
		InputStream is = cobieFile.getFileStream();

		if( is != null )
		{
    		if( type.equals("EXCEL") )
    		{
    			String params[] = {cobieFile.getFileName() };
    			throw new ParseException( Parser.ERR_EXCEL_NO_URL, params );
    		}
			return new CsvInputTokenizer(is);
		}

		String fileName = cobieFile.getFileName();
		URL url = null;
		try
        {
	        url = new URL( fileName );
        }
        catch( MalformedURLException e1 )
        {
	        // Do nothing - Not a URL will be processed as a file
        }
		FileInputStream fis = null;
        if( url != null )
        {
    		if( type.equals("EXCEL") )
    		{
    			String params[] = {cobieFile.getFileName() };
    			throw new ParseException( Parser.ERR_EXCEL_NO_URL, params );
    		}
        	try
            {
	            is = url.openStream();
            }
            catch( IOException e )
            {
    			String params[] = {cobieFile.getFileName(), msgFromException( e ) };
    			throw new ParseException( Parser.ERR_BAD_URL, params );
            }
        }
        else
        {
    		File cobieInputFile = new File( fileName );
    		try
    		{
    			fis = new FileInputStream(cobieInputFile);
    			is = fis;
    		}
    		catch( FileNotFoundException e )
    		{
    			String params[] = {cobieFile.getFileName()};
    			throw new ParseException( Parser.MSG_FILE_NOT_FOUND_ERROR, params );
    		}
        }

		if( type.equals("EXCEL") )
		{
			return new XlsInputTokenizer(cobieFile.getFileName(), fis );
		}
		else
		{
			return new CsvInputTokenizer(is);
		}
	}
	
	private void addAttribute(
		Page   attributePage,
		Item   item,
		String sheetName,
		String attribName,
		String attribValue
	) {
		if( !(item instanceof ItemBase))
		{
			return;
		}
		if( attribName == null || attribValue == null )
		{
			return;
		}
		attribValue = attribValue.trim();
		if( attribValue.length() == 0 )
		{
			return;
		}
		ItemBase itemBase = (ItemBase)item;
		ItemATTRIBUTE attrib = new ItemATTRIBUTE();
		attrib.setSourceFile( _cobieFile.getFileName() );
		attrib.setName( attribName.trim() );
		attrib.setValue( attribValue );
		attrib.setSheetName( sheetName );
		attrib.setRowName( itemBase.getName() );
		attrib.setCreatedOn( itemBase.getCreatedOn() );
		attrib.setCreatedBy( itemBase.getCreatedBy() );
		attrib.setExtSystem( itemBase.getExternalSystem() );
		attrib.setExtObject( itemBase.getExternalObject() );
		
		attributePage.put( attrib );
	}

	public static String msgFromException(
	    Exception e)
	{
		String errMsg = e.getLocalizedMessage();
		if( errMsg != null )
		{
			return errMsg;
		}
		errMsg = e.getMessage();
		if( errMsg != null )
		{
			return errMsg;
		}
		return e.getClass().getName();
	}
}