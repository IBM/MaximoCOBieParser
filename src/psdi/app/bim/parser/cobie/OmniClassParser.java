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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OmniClassParser
{
	public static final String TITLE_LEVEL_1     = "Level 1 Title";
	public static final String TITLE_LEVEL_2     = "Level 2 Title";
	public static final String TITLE_LEVEL_3     = "Level 3 Title";
	public static final String TITLE_LEVEL_4     = "Level 4 Title";
	public static final String TITLE_LEVEL_5     = "Level 5 Title";
	public static final String TITLE_LEVEL_6     = "Level 6 Title";
	public static final String TITLE_LEVEL_7     = "Level 7 Title";
	public static final String TITLE_DEFINITIONS = "DEFINITION";
	public static final String SHEET_OMNICLASS   = "OmniClass";
	
	InputTokenizer _tokenizer;
	int            _idxlevel_1    = -1;
	int            _idxlevel_2    = -1;
	int            _idxlevel_3    = -1;
	int            _idxlevel_4    = -1;
	int            _idxlevel_5    = -1;
	int            _idxlevel_6    = -1;
	int            _idxlevel_7    = -1;
	int            _idxDefinition = -1;
	
	private final MessageLogger _logger;
	private final String        _fileName;
	private OmniClass     _rootClass = null;
	
	public OmniClassParser(
		MessageLogger logger,
		String        fileName
	) {
		_logger   = logger;
		_fileName = fileName;
	}
	
	public void execute()
	{
		if( _rootClass != null )
		{
			return;		// Don't allow to run more than once
		}
		int idx = _fileName.lastIndexOf(  '.' );
		if( idx < 0 )
		{
			String params[] = { _fileName, "", SHEET_OMNICLASS };
			_logger.error( Parser.ERR_UNSUPPORTED_FILE_TYPE, params );
			return;
		}
		String extension = _fileName.substring( idx + 1 ).toLowerCase();
		if( extension.equals( "csv" ))
		{
			FileInputStream fis = null;
			try
			{
		        try
		        {
			        fis = new FileInputStream( _fileName );
		        }
		        catch( FileNotFoundException e )
		        {
			        String params[] = { _fileName, SHEET_OMNICLASS, CobieParser.msgFromException( e ) };
		        	_logger.error( Parser.ERR_FILE_OPEN, params );
			        return;
		        }
				_tokenizer = new CsvInputTokenizer( fis );
				try 
				{
		            _rootClass = parseOmniClass();
	            }
	            catch( ParseException pe ) 
	            {
					if( pe.getKey().length() > 0 )
					{
						_logger.error( pe.getKey(), pe.getParams() );
					}
					else
					{
						_logger.exception( SHEET_OMNICLASS, "", pe );
					}
	            }
			}
			finally
			{
				if( fis != null )
				{
					try 
					{
						fis.close();
					}
					catch( IOException e1 ) { /* Ignore */  }
				}
			}
			return;
		}
		else if( extension.equals( "xls" ) || extension.equals( "xlsx" ) ) 
		{
			FileInputStream fis = null;
			try
			{
		        try
		        {
			        fis = new FileInputStream( _fileName );
		        }
		        catch( FileNotFoundException e )
		        {
			        String params[] = { _fileName, SHEET_OMNICLASS, CobieParser.msgFromException( e ) };
		        	_logger.error( Parser.ERR_FILE_OPEN, params );
			        return;
		        }
				
				try 
				{
					XlsInputTokenizer xlsTokenizer = new XlsInputTokenizer( _fileName,fis ); 
					if( ! xlsTokenizer.setCurrentTabBySubString( "Table" ) )
					{
						xlsTokenizer.setCurrentTab( 0 );
					}
					_tokenizer = xlsTokenizer;
		            _rootClass = parseOmniClass();
		            return;
	            }
	            catch( ParseException pe ) 
	            {
					if( pe.getKey().length() > 0 )
					{
						pe.printStackTrace();
						_logger.error( pe.getKey(), pe.getParams() );
					}
					else
					{
						_logger.exception( SHEET_OMNICLASS, "", pe );
					}
					return;
	            }
			}
			finally
			{
				if( fis != null )
				{
					try 
					{
						fis.close();
					}
					catch( IOException e1 ) { /* Ignore */  }
				}
			}
		}
		
		String params[] = { _fileName, extension, SHEET_OMNICLASS };
		_logger.error( Parser.ERR_UNSUPPORTED_FILE_TYPE, params );
		
	}
	
	public OmniClass getRootClass() { return _rootClass; }

	
	protected OmniClass parseOmniClass() 
		throws ParseException 
	{
		String tableId = "";
        try
        {
			String values[];
	        values = _tokenizer.getRow();
			if( values == null )
			{
				return null;
			}
			
			OmniClass rootClass = null;
			
			// Test the first entry to see if it is a valid OmniClass number.  If it is
			// Assume the file format is a single column of OmniClass number in sequence 
			// with no further formating
			OmniClassNumber ocn = new OmniClassNumber( values[0] );
			int tableNum = ocn.getTable();
			if( tableNum > 0 )
			{
				tableId = "Table " + tableNum;
				rootClass = new OmniClass( tableId, "", 0, "" );
				
				while( values != null )
				{
					values = parseOmniClass( rootClass, values );
				}
			}
			else
			{
				tableId = values[0];

				// Parse header row to find columns
		        values = _tokenizer.getRow();
				if( values == null )
				{
					return null;
				}
				parseHeaderRow( values );
		        values = _tokenizer.getRow();
				rootClass = new OmniClass( tableId, "", 0, "" );
				
				while( values != null )
				{
					values = parseOmniClass( rootClass, values );
				}
			}
			return rootClass;
        }
        catch( IOException e )
        {
        	String params[] = { tableId, CobieParser.msgFromException( e ) };
        	e.printStackTrace();
			throw new ParseException( Parser.ERR_FILE_READ_ERROR, params );
        }
	}
	
	
	public String[] parseOmniClass(
	    OmniClass      parentClass,
	    String[]       values
	) 
		throws IOException, ParseException 
	{
		OmniClass currentClass = null;
		while( values != null )
		{
			String name;
			String title = null;
			int    level = 0;
			String definition = null;
			name = values[0];
			
			// Try to skip blank lines
			if( name == null || name.trim().length() == 0 )
			{
				values = _tokenizer.getRow();
				if( values == null )
				{
					return null;
				}
				continue;
			}
			
			//check for valid Omniclass number for parsing.
			//skip the number with alphabets
			if(name.matches(".*[a-zA-Z]+.*")){
				System.out.println(name);
				return null;
			}
			
			if( _idxDefinition > 0 && values.length > _idxDefinition )
			{
				definition = values[_idxDefinition];
			}
			OmniClassNumber ocn = new OmniClassNumber( name );
			
			if( _idxlevel_1 >= 0 && values.length > _idxlevel_1
				&& values[_idxlevel_1] != null && values[_idxlevel_1].length() > 0 )
			{
				title = values[_idxlevel_1];
				level = 1;
			}
			else if( _idxlevel_2 >= 0 &&  values.length > _idxlevel_2
				&& values[_idxlevel_2] != null && values[_idxlevel_2].length() > 0 )
			{
				title = values[_idxlevel_2];
				level = 2;
			}
			else if( _idxlevel_3 >= 0 &&  values.length > _idxlevel_3
				&& values[_idxlevel_3] != null && values[_idxlevel_3].length() > 0 )
			{
				title = values[_idxlevel_3];
				level = 3;
			}
			else if( _idxlevel_4 >= 0 &&  values.length > _idxlevel_4
				&& values[_idxlevel_4] != null && values[_idxlevel_4].length() > 0 )
			{
				title = values[_idxlevel_4];
				level = 4;
			}
			else if( _idxlevel_5 >= 0 &&  values.length > _idxlevel_5
				&& values[_idxlevel_5] != null && values[_idxlevel_5].length() > 0 )
			{
				title = values[_idxlevel_5];
				level = 5;
			}
			else if( _idxlevel_6 >= 0 &&  values.length > _idxlevel_6
				&& values[_idxlevel_6] != null && values[_idxlevel_6].length() > 0 )
			{
				title = values[_idxlevel_6];
				level = 6;
			}
			else if( _idxlevel_7 >= 0 &&  values.length > _idxlevel_6
				&& values[_idxlevel_7] != null && values[_idxlevel_7].length() > 0 )
			{
				title = values[_idxlevel_7];
				level = 7;
			}
			else
			{
				if( !ocn.isValid() )
				{
					String params[] = { values[0] };
					throw new ParseException( Parser.ERR_INVALID_OMNICLASS_STUCT, params );
				}
			}
			if( ocn.isValid() )
			{
				if( title != null && ocn.getTitle().length() == 0 )
				{
					ocn.setTitle( title );
				}
				level = ocn.getLevel() - 1;
			}
			
			// Completed all the children
			if( level <= parentClass.getLevel() )
			{
				return values;
			}
			
			// Starting a new level
			else if( level == parentClass.getLevel() + 2)
			{
				if( currentClass == null )
				{
					throw new ParseException( "currentClass == null" );
				}
				values = parseOmniClass( currentClass, values );
				continue;
			}
			
			// Add a class at this level
			else if( level == parentClass.getLevel() + 1)
			{
				if( ocn.isValid() )
				{
					currentClass = new OmniClass( ocn, definition );
					ocn.getLevel();
				}
				else
				{
					currentClass = new OmniClass( name, title, level, definition );
				}
				parentClass.addChild( currentClass );
			}
			else
			{
				String params[] = { name };
				throw new ParseException( Parser.ERR_INVALID_OMNICLASS_STUCT, params );
			}
			
			values = _tokenizer.getRow();
			if( values == null )
			{
				return null;
			}
		}
		return null;
	}
	
	public void parseHeaderRow(
			String values[]
			) {
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_1.matches(title))
			{
				_idxlevel_1 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_2.matches(title))
			{
				_idxlevel_2 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_3.matches(title))
			{
				_idxlevel_3 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_4.matches(title))
			{
				_idxlevel_4 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_5.matches(title))
			{
				_idxlevel_5 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_6.matches(title))
			{
				_idxlevel_6 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String title = values[i] + "(.*)";
			if( TITLE_LEVEL_7.matches(title))
			{
				_idxlevel_7 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String def = values[i].toUpperCase(); 
			if( def.startsWith( TITLE_DEFINITIONS ))
			{
				_idxDefinition = i;
				break;
			}
		}
	}
	

	
    public static void main(
    	String[] args
	) 
    	throws FileNotFoundException
	{
    	try
    	{
        	String fileName;
        	if( args.length == 0 )
        	{
        		System.out.println( "<filename>" );
        		return;
        	}
        	fileName = args[0];
        	
        	BasicMessageLogger logger = new BasicMessageLogger();
        	
            OmniClassParser parser = new OmniClassParser( logger, fileName );
            parser.execute();
            if( logger.getErrorCount() > 0 )
            {
            	return;
            }
            OmniClass rootClass = parser.getRootClass();
            System.out.println( "Count: " + rootClass.getMemberCount() );
            System.out.println( rootClass );
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}
    }
}