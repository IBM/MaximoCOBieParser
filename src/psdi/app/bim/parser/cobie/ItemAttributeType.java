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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class ItemAttributeType extends ItemBase
{
	public final static int UNSET   = 0;
	public final static int ALN		= 1;
	public final static int NUMERIC	= 2;
	
	private int             _dataType       = UNSET;
	private String          _description    = "";
	private String          _unitOfMeasure  = "";
	private final ItemValueList   _valueSet;
	private final HashSet<String> _categorySet    = new HashSet<String>(); 
	private final HashSet<String> _useWithSheet   = new HashSet<String>(); 
	private NumberFormat    _formater       = null;
	private Parser          _parser         = null;
	
	public ItemAttributeType(
		Parser        parser,
		ItemATTRIBUTE attrib,
		ItemValueList valueList,
		Locale        local
	) {
		_parser = parser;
		setName( attrib.getName() );
		inferType( attrib.getValue(), local );
		_unitOfMeasure = attrib.getUnit();
		_useWithSheet.add( attrib.getSheetName() );
		_description = attrib.getDescription();
		_valueSet = valueList;
	}
	
	@Override
    public String getPageId()
    {
	    return Parser.SHEET_ATTRIB_TYPE;
    }

	public boolean compareAndUpdate( 
		Parser        project,
		ItemATTRIBUTE attrib 
	) {
		String thisName = _parser.convertCase( getName() );
		if( !thisName.equals( _parser.convertCase( attrib.getName() ) ) ) return false; 
		this.inferType( attrib.getValue(), project.getLocale() );
		_useWithSheet.add( attrib.getSheetName() );
		updateDescritpion( attrib.getDescription() );
		HashSet<String> valueSet = attrib.getAllowedValueSet();
		if( valueSet == null  )
		{
			if( _valueSet == null ) return true;
			return false;
		}
		
		if( _valueSet == null )
		{
			return false;
		}
		if( _valueSet.match( valueSet ) )
		{
			return true;
		}
		String[] params = { attrib.getName() };
		project.getLogger().dataIntegrityMessage( Parser.VALIDATE_VALUELIST_MISMATCH, params );
		return false;
	}
	
	public void   addCategory( 
		String category 
	) {
		_categorySet.add( category );
	}
	
	public int    getDataType()      { return _dataType; }
	public void   setDataType( int dataType )
	{
		_dataType = dataType;
	}
	@Override
    public String getDescription()   { return _description; }
	public String getUnitOfMeasure() { return _unitOfMeasure; }
	public void setUnitOfMeasure( String unitOfMeasure )
	{
		_unitOfMeasure = unitOfMeasure;
	}
	
	/**
	 * Attempt to infer the type from a sample value
	 * @param value
	 */
	private void inferType( 
		String value,
		Locale locale
	) {
		if( value == null || value.length() == 0 ) return;
		if( _dataType == ALN ) return;
		if( _formater == null )
		{
			_formater = NumberFormat.getNumberInstance( locale );
		}
		try
		{
			ParsePosition pos = new ParsePosition( 0 );
			_formater.parse( value, pos );
			if( pos.getErrorIndex() >= 0 )
			{
				_dataType = ALN;
				return;
			}
			if( pos.getIndex() < value.length() )
			{
				_dataType = ALN;
				return;
			}
		}
		catch( NumberFormatException nfe )
		{
			_dataType = ALN;
			return;
		}
		_dataType = NUMERIC;
	}
	
	/**
	 * List of all sheets that are referenced by any attribute of this type.
	 * @return
	 */
	public Iterator<String> useWithSheets()
	{
		return _useWithSheet.iterator();
	}
	
	private void updateDescritpion(
		String desc
	) {
		if( desc == null || desc.length() == 0 )
		{
			_description = "";
			return;
		}
		if( desc.equals( _description ) ) return;
		_description = "";
	}

	@Override
    public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( getUniqueId() );
		buf.append( " \t" );
		buf.append( getName() );
		buf.append( "\t\t" );
		switch( _dataType )
		{
		case UNSET:
			buf.append( "Unset" );
			break;
		case ALN:
			buf.append( "AlphaNumeric" );
			break;
		case NUMERIC:
			buf.append( "Numeric" );
			break;
		}
		
		buf.append( "  Unit = " );
		buf.append( _unitOfMeasure );
		
		buf.append( "\n\tUse With: "  );
		Iterator<String> useWithList = _useWithSheet.iterator();
		while( useWithList.hasNext() )
		{
			buf.append( "  " + useWithList.next() );
		}
		
		buf.append( "\n" );
		buf.append( _description );
		if( _valueSet != null )
		{
			buf.append( "\n\t\tValue List: " );
			buf.append( _valueSet.getUniqueId() );
		}

		Iterator<String> itr = _categorySet.iterator();
		if( itr.hasNext() )
		{
			buf.append( "\n\tCategories: " );
		}
		while( itr.hasNext() )
		{
			buf.append( itr.next()  );
			if( itr.hasNext() )
			{
				buf.append( ", " );
			}
		}

		return buf.toString();
	}
}