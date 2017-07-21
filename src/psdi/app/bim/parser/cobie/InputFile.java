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
import java.io.InputStream;

public class InputFile
{
	private  boolean _isExcel   = false;
	
	protected String      _fileName  = null;
	protected InputStream _fileStream = null;
	protected String      _sheetName = null;
	
	// These values are used when the sheet type is EXCEL to control
	// which tabs from the work book are loaded
	protected boolean _useTabAttribute = false;
	protected boolean _useTabAssembly  = false;
	protected boolean _useTabComponent = false;
	protected boolean _useTabContact   = false;
	protected boolean _useTabDocument  = false;
	protected boolean _useTabFacility  = false;
	protected boolean _useTabFloor     = false;
	protected boolean _useTabIssue     = false;
	protected boolean _useTabJob       = false;
	protected boolean _useTabResource  = false;
	protected boolean _useTabSpace     = false;
	protected boolean _useTabSpare     = false;
	protected boolean _useTabSystem    = false;
	protected boolean _useTabType      = false;
	protected boolean _useTabZone      = false;
	
	public InputFile(
	    String fileName,
	    String sheetName 
    ) {
		_fileName  = fileName;
		_sheetName = sheetName;
	}
	
	public InputFile(
	    InputStream fileStream,
	    String      sheetName 
    ) {
		_fileStream  = fileStream;
		_sheetName   = sheetName;
	}
	
	public String      getFileName()       { return _fileName; }
	public InputStream getFileStream()     { return _fileStream; }
	public String      getSheetName()      { return _sheetName; }
	public boolean     isExcel()           { return _isExcel; }
	public boolean     isUseTabAttribute() { return _useTabAttribute; }
	public boolean     isUseTabAssembly()  { return _useTabAssembly; }
	public boolean     isUseTabComponent() { return _useTabComponent; }
	public boolean     isUseTabContact()   { return _useTabContact;	}
	public boolean     isUseTabDocument()  { return _useTabDocument; }
	public boolean     isUseTabFacility()  { return _useTabFacility; }
	public boolean     isUseTabFloor()     { return _useTabFloor; }
	public boolean     isUseTabIssue()     { return _useTabIssue; }
	public boolean     isUseTabJob()       { return _useTabJob;	}
	public boolean     isUseTabResource()  { return _useTabResource; }
	public boolean     isUseTabSpace()     { return _useTabSpace; }
	public boolean     isUseTabSpare()     { return _useTabSpare; }
	public boolean     isUseTabSystem()    { return _useTabSystem; }
	public boolean     isUseTabType()      { return _useTabType; }
	public boolean     isUseTabZone()      { return _useTabZone; }
	
	
	
	void validate() 
		throws ParseException 
	{
		if( _fileStream != null )
		{
			if( _sheetName.equals(  Parser.SHEET_EXCEL )  )
			{
				String params[] = { _fileStream.toString() }; 
				throw new ParseException( Parser.ERR_EXCEL_NO_URL, params );
			}
			return;
		}

		// Blank file names are legal in the UI and should be skipped here
		if( _fileName == null || _fileName.trim().length() == 0 )
		{
			return;
		}
		int idx = _fileName.lastIndexOf(  '.' );
		if( idx < 0 )
		{
			File file = new File( _fileName );
			file.delete();

			String params[] = { _fileName, "", _sheetName };
			throw new ParseException( Parser.ERR_UNSUPPORTED_FILE_TYPE, params );
		}
		String extension = _fileName.substring( idx + 1 ).toLowerCase();
		if( _sheetName.equals(  Parser.SHEET_EXCEL ))
		{
			_isExcel = true;
			if( extension.equals( "xls" )) return;
			if( extension.equals( "xlsx" )) return;
		}
		else
		{
			if( extension.equals( "csv" )) return;
		}
		
		File file = new File( _fileName );
		file.delete();

		String params[] = { _fileName, extension, _sheetName };
		throw new ParseException( Parser.ERR_UNSUPPORTED_FILE_TYPE, params );
	}

	public void setUseTabAttribute(
		boolean useTabAttribute
	) {
		_useTabAttribute = useTabAttribute;
	}

	public void setUseTabAssembly(
		boolean useTabAssembly
	) {
		_useTabAssembly = useTabAssembly;
	}

	public void setUseTabComponent(
		boolean useTabComponent
	) {
		_useTabComponent = useTabComponent;
	}

	public void setUseTabContact(
		boolean useTabContact
	) {
		_useTabContact = useTabContact;
	}

	public void setUseTabDocument(
		boolean useTabDocument
	) {
		_useTabDocument = useTabDocument;
	}

	public void setUseTabFacility(
		boolean useTabFacility
	) {
		_useTabFacility = useTabFacility;
	}

	public void setUseTabFloor(
		boolean useTabFloor
	) {
		_useTabFloor = useTabFloor;
	}

	public void setUseTabIssue(
		boolean useTabIssue
	) {
		_useTabIssue = useTabIssue;
	}

	public void setUseTabJob(
		boolean useTabJob
	) {
		_useTabJob = useTabJob;
	}

	public void setUseTabResource(
		boolean useTabResource
	) {
		_useTabResource = useTabResource;
	}

	public void setUseTabSpace(
		boolean useTabSpace
	) {
		_useTabSpace = useTabSpace;
	}

	public void setUseTabSpare(
		boolean useTabSpare
	) {
		_useTabSpare = useTabSpare;
	}

	public void setUseTabSystem(
		boolean useTabSystem
	) {
		_useTabSystem = useTabSystem;
	}

	public void setUseTabType(
		boolean useTabType
	) {
		_useTabType = useTabType;
	}

	public void setUseTabZone(boolean useTabZone) {
		_useTabZone = useTabZone;
	}

	boolean processTab(
        String pageName
    ) {
		if( pageName.equals( Parser.SHEET_FACILITY ) )
		{
			return _useTabFacility;
		}
		else if( pageName.equals( Parser.SHEET_FLOOR ) )
		{
			return _useTabFloor;
		}
		else if( pageName.equals( Parser.SHEET_SPACE ) )
		{
			return _useTabSpace;
		}
		else if( pageName.equals( Parser.SHEET_TYPE ) )
		{
			return _useTabType;
		}
		else if( pageName.equals( Parser.SHEET_COMPONENT ) )
		{
			return _useTabComponent;
		}
		else if( pageName.equals( Parser.SHEET_ZONE ) )
		{
			return _useTabZone;
		}
		else if( pageName.equals( Parser.SHEET_SYSTEM ) )
		{
			return _useTabSystem;
		}
		else if( pageName.equals( Parser.SHEET_CONTACT ) )
		{
			return _useTabContact;
		}
		else if( pageName.equals( Parser.SHEET_ATTRIBUTE ) )
		{
			return _useTabAttribute;
		}
		else if( pageName.equals( Parser.SHEET_DOCUMENT ) )
		{
			return _useTabDocument;
		}
		else if( pageName.equals( Parser.SHEET_JOB ) )
		{
			return _useTabJob;
		}
		else if( pageName.equals( Parser.SHEET_RESOURCE ) )
		{
			return _useTabResource;
		}
		else if( pageName.equals( Parser.SHEET_SPARE ) )
		{
			return _useTabSpare;
		}
		else if( pageName.equals( Parser.SHEET_ASSEMBLY ) )
		{
			return _useTabAssembly;
		}
	   
	   return false;
    }
}