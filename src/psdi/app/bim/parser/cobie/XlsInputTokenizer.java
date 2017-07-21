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
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class   XlsInputTokenizer
    implements InputTokenizer
{
	
	private Workbook	     _workbook	  = null;
	private DataFormatter	 _formatter	  = null;
	private FormulaEvaluator _evaluator	  = null;
	private Iterator<Row>	 _it	      = null;
   
	public XlsInputTokenizer(
		String          fileName,
		FileInputStream is
	)
	    throws ParseException
	{
		
		try
		{
			// Open the workbook and then create the FormulaEvaluator and
			// DataFormatter instances that will be needed to, respectively,
			// force evaluation of formula found in cells and create a
			// formatted String encapsulating the cells contents.
			try 
			{
	            _workbook = WorkbookFactory.create(is);
            }
            catch( IOException ioe ) 
            {
            	String params[] = {fileName, Parser.SHEET_EXCEL, CobieParser.msgFromException( ioe ) };
            	throw new ParseException( Parser.ERR_FILE_OPEN, params );
            }
			_evaluator = _workbook.getCreationHelper().createFormulaEvaluator();
			_formatter = new DataFormatter();
		}
		catch( InvalidFormatException e )
		{
			String params[] = { fileName, e.getLocalizedMessage() };
			throw new ParseException( Parser.ERR_INVALID_XLS_FILE_ERROR, params );
		}

		finally
		{
			if( is != null )
			{
				try 
				{
	                is.close();
                }
                catch( IOException e ) { /* Do Nothing */ }
			}
		}
	}

	public boolean setCurrentTab(
	    String name
    ) {
		Sheet currentSheet = _workbook.getSheet( name );
		if( currentSheet == null )
		{
			return false;
		}
		_it = currentSheet.rowIterator();
		return true;
	}


	public boolean setCurrentTab(
	    int idx
    ) {
		Sheet currentSheet = _workbook.getSheetAt( idx );
		if( currentSheet == null )
		{
			return false;
		}
		_it = currentSheet.rowIterator();
		return true;
	}

	public boolean setCurrentTabBySubString(
	    String name
    ) {
		int count = _workbook.getNumberOfSheets();
		name = name.toUpperCase();
		for( int i = 0; i < count; i++ )
		{
			String sheetName = _workbook.getSheetName( i );
			if( sheetName == null )
			{
				continue;
			}
			sheetName = sheetName.toUpperCase();
			if( sheetName.startsWith( name ))
			{
				if( setCurrentTab( sheetName ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	public String[] getRow() throws CellReadException
	{
		if( !_it.hasNext() )
		{
			return null;
		}

		Cell cell = null;
		Row row = _it.next();
		// Get the index for the right most cell on the row and then
		// step along the row from left to right recovering the contents
		// of each cell, converting that into a formatted String and
		// then storing the String into the csvLine ArrayList.
		int lastCellNum = row.getLastCellNum();
		if( lastCellNum < 0 )
		{
			return null;
		}
		String values[] = new String[lastCellNum];
		
		for( int i = 0; i <= lastCellNum; i++ )
		{
			cell = row.getCell(i);
			if( cell == null )
				continue;

			String value = "";

			try {
				if( cell.getCellType() != Cell.CELL_TYPE_FORMULA )
				{
					value = _formatter.formatCellValue(cell);
				}
				else
				{
					value = _formatter.formatCellValue(cell, _evaluator);
				}
			}
			catch (Throwable t) {
				throw new CellReadException(i);
			}
			values[ cell.getColumnIndex() ] = value;
		}

		return values;
	}
	
	public void close()
	{
		_it       = null;
		_workbook = null;
	}
}