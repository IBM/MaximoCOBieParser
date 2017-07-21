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
import java.util.HashMap;
import java.util.List;

public class UniFormatParser
{
	public static final String TITLE_LEVEL_1     = "Level 1";
	public static final String TITLE_LEVEL_2     = "Level 2";
	public static final String TITLE_LEVEL_3     = "Level 3";
	public static final String TITLE_LEVEL_4     = "Level 4";
	public static final String TITLE_LEVEL_5     = "Level 5";
	public static final String TITLE_LEVEL_6     = "Level 6";
	public static final String TITLE_LEVEL_7     = "Level 7";

	public static final int LEVEL_1     = 1;
	public static final int LEVEL_2     = 2;
	public static final int LEVEL_3     = 3;
	public static final int LEVEL_4     = 4;

	public static final String SHEET_UNIFORMAT   = "UniFormat";
	public static final String CATALOG_TAG   = "Catalog";
	public static final String ITEM_GROUP_TAG   = "ItemGroup";
	public static final String WBS_TAG   = "WBS";
	public static final String NAME_TAG   = "Name";	

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
	private UniFormat     _rootClass = null;



	public UniFormatParser(
			MessageLogger logger,
			String        fileName
			) {
		_logger   = logger;
		_fileName = fileName;
	}


	public void execute() throws Exception
	{
		if( _rootClass != null )
		{			
			return;		// Don't allow to run more than once
		}
		int idx = _fileName.lastIndexOf(  '.' );
		if( idx < 0 )
		{
			String params[] = { _fileName, "", SHEET_UNIFORMAT };
			_logger.error( Parser.ERR_UNSUPPORTED_FILE_TYPE, params );
			return;
		}
		String extension = _fileName.substring( idx + 1 ).toLowerCase();
		if( extension.equals( "xml" ))
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
					String params[] = { _fileName, SHEET_UNIFORMAT, CobieParser.msgFromException( e ) };
					_logger.error( Parser.ERR_FILE_OPEN, params );
					return;
				}

				try 
				{
					XmlnputTokenizer model =  XmlnputTokenizer.parse(fis);
					_rootClass = processUniformatXML(model);
				}
				catch( ParseException pe ) 
				{
					if( pe.getKey().length() > 0 )
					{
						_logger.error( pe.getKey(), pe.getParams() );
					}
					else
					{
						_logger.exception( SHEET_UNIFORMAT, "", pe );
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
					String params[] = { _fileName, SHEET_UNIFORMAT, CobieParser.msgFromException( e ) };
					_logger.error( Parser.ERR_FILE_OPEN, params );
					return;
				}

				try 
				{
					XlsInputTokenizer xlsTokenizer = new XlsInputTokenizer( _fileName,fis ); 
					if( ! xlsTokenizer.setCurrentTabBySubString( "Uniformat" ) )
					{
						xlsTokenizer.setCurrentTab( 0 );
					}
					_tokenizer = xlsTokenizer;
					_rootClass = parseUniFormat();
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
						_logger.exception( SHEET_UNIFORMAT, "", pe );
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

		String params[] = { _fileName, extension, SHEET_UNIFORMAT };
		_logger.error( Parser.ERR_UNSUPPORTED_FILE_TYPE, params );

	}

	public UniFormat getRootClass() { return _rootClass; }




	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	protected UniFormat parseUniFormat() 
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

			UniFormat rootClass = null;

			if(values[0] != null)
				tableId = values[0];
			else
				tableId = "Uniformat";	

			values = _tokenizer.getRow();

			if( values == null )
			{
				return null;
			}

			//check for poorly formatted header row.
			values = validateHeaderRow(values);

			//set the index value for each coloumn heading
			values = parseHeaderRow(values);



			rootClass = new UniFormat( tableId, "", 0, "" );		

			while( values != null )
			{				
				values = parseUniFormat( rootClass, values );				
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


	/**
	 * 
	 * Parse Uniformat Exel sheet and build Uniformat Tree object structure
	 * 
	 * @param parentClass
	 * @param values
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public String[] parseUniFormat(
			UniFormat      parentClass,
			String[]       values) throws IOException, ParseException 
			{	

		String name  = "";

		// Use a below Uniformat objects  as a  marker/pointer to get the parent object reference for each level.
		UniFormat _uflevel_1 = null;
		UniFormat _uflevel_2 = null;
		UniFormat _uflevel_3 = null;

		while(values != null){


			if( values.length < LEVEL_3 ) 
				return null;

			name = values[_idxlevel_1];

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


			//Build Level 1
			if(values[_idxlevel_1] != null && values[_idxlevel_1].length() >= 0){
				_uflevel_1 = buildUniformatClass(_idxlevel_1, values , LEVEL_1);
				if(_uflevel_1 != null)
					parentClass.addChild(_uflevel_1);				
			}

			///Build Level 2			
			if(values[_idxlevel_2] != null && values[_idxlevel_2].length() >= 0){
				_uflevel_2 = buildUniformatClass(_idxlevel_2, values , LEVEL_2);
				if(_uflevel_2 != null)
					_uflevel_1.addChild(_uflevel_2);			
			}

			//Build Level 3
			if(values[_idxlevel_3] != null && values[_idxlevel_3].length() >= 0){
				_uflevel_3 = buildUniformatClass(_idxlevel_3, values , LEVEL_3);			
				if(_uflevel_3 != null)
					_uflevel_2.addChild(_uflevel_3);				
			}	


			values = _tokenizer.getRow();		


			//Read the row data and build children for each level. 
			while(values != null){	


				if(values[_idxlevel_1] != null && values[_idxlevel_1].length() > 0){				
					//New structure
					return values;
				}

				if(values[_idxlevel_1] != null && values[_idxlevel_1].length() > 0){	
					values = buildLevelOneTree(parentClass, values, _idxlevel_1);					
					continue;
				}

				if(values != null && ( values[_idxlevel_1] != null && values[_idxlevel_1].length() <= 0 ) &&
						( values[_idxlevel_2] != null && values[_idxlevel_2].length() > 0 ) &&
						( values[_idxlevel_3] != null && values[_idxlevel_3].length() > 0 )){					
					values = buildLevelTwoTree(_uflevel_1, values, _idxlevel_2);						
					continue;
				}

				if(values != null && ( values[_idxlevel_1] != null && values[_idxlevel_1].length() <= 0 ) &&
						( values[_idxlevel_2] != null && values[_idxlevel_2].length() <= 0 )&&
						( values[_idxlevel_3] != null && values[_idxlevel_3].length() > 0 )){						
					values = buildLevelThreeTree(_uflevel_2, values, _idxlevel_3);
					continue;
				}	

				//Read next row 
				values = _tokenizer.getRow();
				if(values != null)			
					continue;
			}
		}
		return values;
			}


	public String[] buildLevelOneTree(UniFormat      parentClass,
			String[]       values , int idx_level) throws IOException, ParseException{	


		UniFormat _ufLevel_1 = null;
		UniFormat _uflevel_2 = null;
		UniFormat _uflevel_3 = null;	

		//Build Level 1
		_ufLevel_1 =  buildUniformatClass(idx_level, values , LEVEL_1);
		if(_ufLevel_1 !=null)
			parentClass.addChild(_ufLevel_1);


		//Build Level 2
		_uflevel_2 =  buildUniformatClass(_idxlevel_2, values , LEVEL_2);
		if(_uflevel_2 !=null)
			_ufLevel_1.addChild(_uflevel_2);		


		//Build Level 3
		_uflevel_3 =  buildUniformatClass(_idxlevel_3, values , LEVEL_3);
		if(_uflevel_3 !=null)
			_uflevel_2.addChild(_uflevel_3);		

		values = _tokenizer.getRow();

		//Build  Level 3
		while(values != null && ( values[_idxlevel_1] != null && values[_idxlevel_1].length() <= 0 ) &&
				( values[_idxlevel_2] != null && values[_idxlevel_2].length() <= 0 ) &&
				( values[_idxlevel_3] != null && values[_idxlevel_3].length() >= 0 )){
			values = buildLevelThreeTree(_uflevel_2, values, _idxlevel_3);
		}



		return values;
	}


	public String[] buildLevelTwoTree(UniFormat      parentClass,
			String[]       values , int idx_level) throws IOException, ParseException{

		UniFormat _uflevel_2 = null;
		UniFormat _uflevel_3 = null;

		//Build Level 2
		_uflevel_2 =  buildUniformatClass(_idxlevel_2, values , LEVEL_2);
		if(_uflevel_2 !=null)
			parentClass.addChild(_uflevel_2);	

		//Build Level 3
		_uflevel_3 =  buildUniformatClass(_idxlevel_3, values , LEVEL_3);
		if(_uflevel_3 !=null)
			_uflevel_2.addChild(_uflevel_3);			


		values = _tokenizer.getRow();

		//Build Level 3
		while(values != null && ( values[_idxlevel_1] != null && values[_idxlevel_1].length() <= 0 ) &&
				( values[_idxlevel_2] != null && values[_idxlevel_2].length() <= 0 ) &&
				( values[_idxlevel_3] != null && values[_idxlevel_3].length() >= 0 )){
			values = buildLevelThreeTree(_uflevel_2, values, _idxlevel_3);
		}		
		return values;	
	}


	public String[] buildLevelThreeTree(UniFormat      parentClass,
			String[]       values , int idx_level) throws IOException, ParseException{		

		UniFormat uniformat = null;			
		uniformat =  buildUniformatClass(_idxlevel_3, values , LEVEL_3);
		if(uniformat != null)			
			parentClass.addChild(uniformat);			
		values = _tokenizer.getRow();		
		return values;		
	}


	/**
	 * This method will check for poorly formatted header row and 
	 * return the valid row for processing the header row index.
	 * 
	 * @param values
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public String[] validateHeaderRow(String values[]) throws IOException, ParseException{

		while(values != null){
			int count = 0;
			for( int i = 0; i < values.length; i++ )
			{	
				if(values[i] == null) 
					continue;
				String _title = values[i] + "(.*)";
				if( values[i].length() >= 1 && ( values[i].equalsIgnoreCase( "Level I" ) || TITLE_LEVEL_1.matches( _title )))
				{
					count = 1;
					break;
				}
			}

			if(count == 1) 
				break;
			else
				values = _tokenizer.getRow();
		}		
		return values;
	}


	/**
	 * set the column index for each level
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public String[] parseHeaderRow(String values[]) throws IOException, ParseException {	

		for( int i = 0; i < values.length; i++ )
		{
			if(values[i] == null) continue;
			String _title = values[i] + "(.*)";
			if( TITLE_LEVEL_1.matches( _title ) || values[i].equalsIgnoreCase("Level I"))
			{
				_idxlevel_1 = i;
				break;
			}			
		}

		for( int i = 0; i < values.length; i++ )
		{

			if( values[i] != null &&  values[i].length() > 0){
				String _title = values[i] + "(.*)";

				if( TITLE_LEVEL_2.matches( _title ) || values[i].equalsIgnoreCase("Level II"))
				{
					_idxlevel_2 = i;
					break;
				}
			}

		}
		for( int i = 0; i < values.length; i++ )
		{
			if( values[i] != null &&  values[i].length() > 0){
				String _title = values[i] + "(.*)";
				if( TITLE_LEVEL_3.matches( _title ) || values[i].equalsIgnoreCase("Level III"))
				{
					_idxlevel_3 = i;
					break;
				}
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			if(values[i] == null) continue;

			String _title = values[i] + "(.*)";
			if( TITLE_LEVEL_4.matches( _title ) || values[i].equalsIgnoreCase("Level IV"))
			{
				_idxlevel_4 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String _title = values[i] + "(.*)";
			if( TITLE_LEVEL_5.matches( _title ))
			{
				_idxlevel_5 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String _title = values[i] + "(.*)";
			if( TITLE_LEVEL_6.matches( _title ))
			{
				_idxlevel_6 = i;
				break;
			}
		}
		for( int i = 0; i < values.length; i++ )
		{
			String _title = values[i] + "(.*)";
			if( TITLE_LEVEL_7.matches( _title ))
			{
				_idxlevel_7 = i;
				break;
			}
		}

		
		values = _tokenizer.getRow();	
		
		//check for Poorly formated in GSA xls sheet
		//If 0th index has no data, point _idxlevel_1 level to 1st index.
		if(values[0] != null && values[0].length() <= 0){
			_idxlevel_1 = 1;
		}	
		
		return values;
	}





	/**
	 * 
	 * Build Uniformat object for xls data
	 * 
	 * @param idx_level
	 * @param values
	 * @return
	 */
	public UniFormat buildUniformatClass(int idx_level,String[] values , int level){
		UniFormat uobj = null;

		if(values[idx_level] != null && values[idx_level].length() > 0){

			UniFormatNumber  ufn = new UniFormatNumber(values[idx_level] , level);
			if(ufn.isValid()){				
				uobj = new UniFormat( ufn, null );
				if(uobj._title != null && uobj._title.length() <= 0){
					if(idx_level == _idxlevel_1 || idx_level == _idxlevel_2 || idx_level == _idxlevel_3)
						uobj._title = values[idx_level+1];
					else
						uobj._title = values[idx_level];
				}	

				return uobj;
			}
		}

		return null;
	}


	/**
	 * 
	 * This method will build Uniformat Tree structure for XML file
	 * 
	 * @param model
	 * @return
	 */
	protected UniFormat processUniformatXML(XmlnputTokenizer model){


		String tableId = "";

		if( model == null )
		{				
			return null;
		}

		UniFormat rootClass = null;

		tableId = "Uniformat";

		rootClass = new UniFormat( tableId, "", 0, "" );		


		List<XmlnputTokenizer> rootList = model.getChildren(CATALOG_TAG);	
		for (XmlnputTokenizer root: rootList){				

			//LEVEL - 1
			List<XmlnputTokenizer> level1_list = root.getChildren(ITEM_GROUP_TAG);
			for (XmlnputTokenizer level1: level1_list){				
				HashMap<Integer, String> map = new HashMap<Integer, String>();				
				map.put(LEVEL_1, level1.getAttribute(WBS_TAG));
				UniFormat level_1 = buildUniformatXMLClass(level1, LEVEL_1, map);
				rootClass.addChild(level_1);

				//LEVEL - 2				
				List<XmlnputTokenizer> level2_list = level1.getChildren(ITEM_GROUP_TAG);
				for (XmlnputTokenizer level2: level2_list){
					map.put(LEVEL_2, level2.getAttribute(WBS_TAG));
					UniFormat level_2 = buildUniformatXMLClass(level2, LEVEL_2, map);
					level_1.addChild(level_2);

					//LEVEL - 3
					List<XmlnputTokenizer> level3_list = level2.getChildren(ITEM_GROUP_TAG);
					for (XmlnputTokenizer level3: level3_list){
						map.put(LEVEL_3, level3.getAttribute(WBS_TAG));
						UniFormat level_3 = buildUniformatXMLClass(level3, LEVEL_3, map);
						level_2.addChild(level_3);	
					}
				}					
			}	
		}
		return rootClass;
	}


	/**
	 * 
	 * Build Uniformat object for XML data
	 * 
	 * @param _tokenizer
	 * @param level
	 * @return
	 */
	public UniFormat buildUniformatXMLClass(XmlnputTokenizer _tokenizer , int level,  HashMap<Integer, String> wbs){
		UniFormat uobj = null;		

		if(wbs != null){
			UniFormatNumber  ufn = new UniFormatNumber(level, wbs);
			if(ufn.isValid()){
				uobj = new UniFormat( ufn, null );	
				uobj._title = _tokenizer.getAttribute(NAME_TAG);
				return uobj;
			}
		}
		return null;
	}




	public static void main(
			String[] args
			) 
					throws FileNotFoundException
					{
		try
		{

			String fileName;
			//String definition = null;
			if( args.length == 0 )
			{
				System.out.println( "<filename>" );
				return;
			}
			fileName = args[0];

			BasicMessageLogger logger = new BasicMessageLogger();

			UniFormatParser parser = new UniFormatParser( logger, fileName );
			parser.execute();
			if( logger.getErrorCount() > 0 )
			{
				return;
			}

			UniFormat rootClass = parser.getRootClass();		
			System.out.println(rootClass);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
					}
}