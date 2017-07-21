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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class XLSExport
    implements
        Exporter
{

	private Workbook	                      _wb	              = null;
	private MessageLogger                     _logger             = null;
	private Hashtable<String, CellStyle>	  _cobieStyles	      = new Hashtable<String, CellStyle>();
	private Hashtable<String, CobieSheetSpec> _sheetStyles	      = new Hashtable<String, CobieSheetSpec>();
	private boolean	                          _supportStyleFormat = false;
	private Sheet                             _currentSheet       = null; 
	private String[]                          _currentColumns     = null; 
	private String                            _currentPage        = null;
	private int                               _currentRow         = 0;
	private Row                               _header             = null;
	
	public XLSExport(
        MessageLogger logger,
        InputStream   templateStream,
        ExportFormat fileFormat)
    {
		_logger = logger;
	
		if (fileFormat == ExportFormat.XLS)
			_wb = new HSSFWorkbook();
		else
			_wb = new XSSFWorkbook();
		
		_supportStyleFormat = getTemplateStyles( templateStream );
    }
	

	public MessageLogger getLogger()
	{
		return _logger;
	}

	public void addRow(
	    String[] columnValues)
	{
			Row row = _currentSheet.createRow(_currentRow++);
			
			int cCnt = 0;
			
			for( int j = 0; j < columnValues.length; j++ )
			{
			Cell cell = row.createCell(cCnt);
			String value = (String) columnValues[j];
			

			cell.setCellValue(value);

			if( _supportStyleFormat )
			{
				CobieSheetSpec cobieSheetSpec = _sheetStyles.get(_currentPage);
				if( cobieSheetSpec != null )
				{
					String columnStyle = cobieSheetSpec.getColumnStyle(_currentColumns[j]);

					if( columnStyle != null )
					{
						CellStyle cellStyle = _cobieStyles.get(columnStyle);
						cell.setCellStyle(cellStyle);
					}
				}
			}
			cCnt++;
		}
	
		
		CellRangeAddress cra = new CellRangeAddress(0, 0, _header.getFirstCellNum(),
		                                            _header.getLastCellNum() - 1);
		_currentSheet.setAutoFilter(cra);
//		_currentSheet.autoSizeColumn(cCnt);

	}

	public void processPage(
        String pageName,
        String[] colNames
    ) {
		_currentPage   = pageName;
		_currentSheet  = _wb.createSheet(_currentPage);
		_currentColumns = colNames;
		_header = _currentSheet.createRow(0);
		_currentRow = 1;
		
		int cCnt = 0;
		for( int j = 0; _currentColumns != null && j < _currentColumns.length; j++ )
		{
			Cell cell = _header.createCell(cCnt++);
			cell.setCellValue(colNames[j]);

			if( _supportStyleFormat )
			{
				cell.setCellStyle(_cobieStyles.get("header"));
			}
		}
    }
	
	private boolean getTemplateStyles(
		InputStream is
	) {
		boolean supportStyleFormat = true;

		if( is == null )
		{
			return false;
		}
		
	try
		{
			JSONObject jsonObject = JSONObject.parse(is);

			JSONObject cobie = (JSONObject) jsonObject.get("cobie");
			String version = (String) cobie.get("version");
			String params[] = { version }; 
			_logger.message(Parser.MSG_VERSION, params);
			
			JSONArray styles = (JSONArray) cobie.get("styles");
			@SuppressWarnings( "unchecked" )
            Iterator<JSONObject> it = styles.iterator();

			while( it.hasNext() )
			{
				JSONObject style = it.next();

				String name = (String) style.get(STYLE_NAME);
				String color = (String) style.get(COLOR);
				String rotation = (String) style.get(ROTATION);
				String font = (String) style.get(FONT_NAME);
				String fontSize = (String) style.get(FONT_SIZE);
				String typeFace = (String) style.get(TYPEFACE);
				String hAlign = (String) style.get(HORIZONTAL_ALIGN);
				String vAlign = (String) style.get(VERTICAL_ALIGN);

				CellStyle cellStyle = _wb.createCellStyle();
				if( color != null )
				{
					cellStyle.setFillPattern((short) 1);
					cellStyle.setFillForegroundColor(getPoiColor(color));
				}

				cellStyle.setRotation(Short.valueOf(rotation));

				if( (hAlign != null) && hAlign.equals(ALIGN_CENTER) )
					cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

				if( (vAlign != null) && vAlign.equals(ALIGN_BOTTOM) )
					cellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);

				Font f = getPoiFont(font, fontSize, typeFace);
				if( f != null )
				{
					cellStyle.setFont(f);
				}

				cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
				cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
				cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
				cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				cellStyle.setBorderRight(CellStyle.BORDER_THIN);
				cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				cellStyle.setBorderTop(CellStyle.BORDER_THIN);
				cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				_cobieStyles.put(name, cellStyle);
			}

			JSONArray sheets = (JSONArray) cobie.get("sheets");
			@SuppressWarnings( "unchecked" )
			Iterator<JSONObject> sit = sheets.iterator();

			while( sit.hasNext() )
			{
				JSONObject sheet = sit.next();

				String name = (String) sheet.get("name");
				CobieSheetSpec sheetSpec = new CobieSheetSpec(name);
				String sheetStyle = (String) sheet.get("style");
				sheetSpec.setStyle(sheetStyle);

				JSONArray columns = (JSONArray) sheet.get("columns");
				@SuppressWarnings( "unchecked" )
				Iterator<JSONObject> cit = columns.iterator();
				while( cit.hasNext() )
				{

					JSONObject colObj = (JSONObject) cit.next();
					String colName = (String) colObj.get("name");
					String style = (String) colObj.get("style");
					sheetSpec.addColumnStyle(colName, style);
				}

				_sheetStyles.put(name.toUpperCase(), sheetSpec);
			}
		}
		catch( IOException e )
		{
			
			String params[] = { CobieParser.msgFromException( e ) };
			_logger.warning( Parser.ERR_FILE_READ_ERROR, params );
			supportStyleFormat = false;
		}

		
		return supportStyleFormat;
	}
	private short getPoiColor(
	    String color)
	{
		String normaizedColor = color.toLowerCase();

		if( normaizedColor.equals(YELLOW) )
			return IndexedColors.LIGHT_YELLOW.getIndex();
		if( normaizedColor.equals(GREEN) )
			return IndexedColors.LIGHT_GREEN.getIndex();
		if( normaizedColor.equals(PURPLE) )
			return IndexedColors.LAVENDER.getIndex();
		if( normaizedColor.equals(ORANGE) )
			return IndexedColors.LIGHT_ORANGE.getIndex();
		if( normaizedColor.equals(GREY) )
			return IndexedColors.GREY_25_PERCENT.getIndex();

		return IndexedColors.BLACK.getIndex();

	}

	private Font getPoiFont(
	    String fontName,
	    String fontSize,
	    String typeFace)
	{
		Font font = null;

		if( fontName != null )
		{
			font = _wb.createFont();
			font.setFontName(fontName);

			if( fontSize != null )
				font.setFontHeightInPoints((short) Short.parseShort(fontSize));

			if( typeFace != null )
			{
				if( typeFace.equals(BOLD) )
					font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				else if( typeFace.equals(ITALIC) )
					font.setItalic(true);

			}
		}
		return font;
	}


	public String[] getColumnNames()
    {
	   
	    return _currentColumns;
    }


	public void write(String fileName) throws IOException
    {
		if (fileName != null)
		{
			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream(fileName);
			_wb.write(fileOut);
			fileOut.close();
			_wb = null;
			_cobieStyles.clear();
			_cobieStyles = null;
			_sheetStyles.clear();
			_sheetStyles = null;
			
			String params[] = { fileName };
			_logger.message(Parser.MSG_FILE_CREATED, params);
		}
    }
}