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

import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ItemATTRIBUTE extends ItemBase {

	private String _allowedValues = "";
	private String _sheetName = "";
	private String _rowName = "";
	private String _value = "";
	private String _unit = "";
	
	private ItemBase          _item;
	private ItemAttributeType _type;

	@Override
	public boolean equals(
		Object o
	) {
		if( !(o instanceof ItemATTRIBUTE ) )     return false;
		ItemATTRIBUTE attrib = (ItemATTRIBUTE)o;
		if( !getKey().equals( attrib.getKey() )) return false;
		if( !_type.equals( attrib.getType() ))   return false;
		if( !_value.equals( attrib.getValue() )) return false;
		return true;
	}

	@Override
    public void resolveRerefences(
		Parser project, 
		long flags 
	) {
		boolean badRef = false;
		if(  getSheetName() == null || getSheetName().length() == 0 )
		{
			String[] params = { getPageId(), getName() };
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_SHEET_REF, params );
			badRef = true;
		}
		
	    if( getRowName()   == null || getRowName().length() == 0 )
		{
	    	if( !( getSheetName() != null && _sheetName.equalsIgnoreCase( Parser.SHEET_FACILITY ) ) )
	    	{
				String[] params = { getPageId(), getName() };
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_ROW_REF, params );
				badRef = true;
	    	}
		}
	    
	    if( badRef ) return;
			
		Page page = project.getPage( _sheetName.toUpperCase() );
		if( page == null )
		{
			String params[] = { getPageId(), getName(), _sheetName, _rowName }; 
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
			return;
		}
		
		// Filter out attributes where name == value as meaningless
		if( getName().equals( _value ))
		{
			String params[] = { getName(), _sheetName, _rowName }; 
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_ATTRIB_VALUE_IS_NAME, params );
			return;
		}

		// Facility is a singleton so there may not be a valid row reference
		Item item;
		if( _sheetName.equalsIgnoreCase( Parser.SHEET_FACILITY ))
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item.getKey() != null && item.getKey().trim().length() > 0 )
				{
					if( item instanceof ItemFACILITY )
					{
						// There should only be one facility and there is no so take the first
						// well formed instance and hope its right
						_item = (ItemBase)item;
						_item.addAttribute( this );
						setParentRef( (ItemBase)item );
						return;
					}
				}
			}
		}
		
		item = page.getItem( _rowName );
		if( item == null ) return;
		if( !( item instanceof ItemBase )) return;
		_item = (ItemBase)item;
		_item.addAttribute( this );
		setParentRef( (ItemBase)item );
	}

	@Override
    public String getKey() {
		return _sheetName + ":" + _rowName + ":" + getName();
	}
	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		if( (flags & Parser.FLAG_SKIP_ON_NULL) != 0 )
		{
			if( _value == null || _value.length() == 0 )
			{
				return true;
			}
		}
		if( (flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 )
		{
			if( _value != null && _value.equals( getName() ))
			{
				return true;
			}
		}
		
		if( isItemFiltered( parser.filters(), getSheetName(), getRowName() ) )
		{
			return true;
		}
		
		return super.skip( parser, flags );
	}

	
	public void setAllowedValues(
		String allowedValues
	) {
		_allowedValues = filterNA( allowedValues );
	}
	
	public String getAllowedValues()
	{
		return filterNA( _allowedValues );
	}
	
	public HashSet<String> getAllowedValueSet()
	{
		if( _allowedValues == null || _allowedValues.length() == 0 )
		{
			return null;
		}
		
		HashSet<String> valueSet = new HashSet<String>();
		StringTokenizer strToken = new StringTokenizer( _allowedValues, ",");
		while (strToken.hasMoreElements())
		{
			String token = filterNA( strToken.nextToken() );
			if( token.length() == 0 ) continue;
			valueSet.add( token );
		}
		if( valueSet.size() == 0 ) return null;
		return valueSet;
	}

	@Override
    public String getPageId()
	{
		return Parser.SHEET_ATTRIBUTE;
	}


	public String getSheetName() {
		return _sheetName;
	}

	public void setSheetName(
	    String sheetName 
    ) {
		_sheetName = filterNA( sheetName ).toUpperCase();
	}
	
	public ItemBase getReference() { return _item; }

	public void setReference(
		ItemBase reference
	) { 
		_item = reference; 
	}

	public String getRowName() {
		return _rowName;
	}

	public void setRowName(String rowName) {
		_rowName = filterNA( rowName );
	}

	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = filterNA( value );
	}

	public String getUnit() {
		return _unit;
	}

	public void setUnit(
	    String unit 
    ) {
		_unit = filterNA( unit );
	}
	
	

	
	public ItemAttributeType getType()
    {
    	return _type;
    }

	public void setType(
        ItemAttributeType _type)
    {
    	this._type = _type;
    }

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( getUniqueId() + "\t");
		sb.append("Name = "          + getName() + "\t");
		sb.append("Description = "   + getDescription() + "\t");
		sb.append("Value = "         + _value + "\t");
		sb.append("Unit = "          + _unit + "\n");
		
		if( _type != null )
		{
			sb.append("\t\tType = "  + _type.getDescription() + "\n");
		}

		sb.append("\t\tExtSystem = " + getExternalSystem() + "\t");
		sb.append("ExtObject  = "    + getExternalObject() + "\t");
		sb.append("ExtIdentifier = " + getExternalIdentifier() + "\t");
		if( getParentRef() != null )
		{
			sb.append("Parent Reference = " + getParentRef().getPageId() + ":" + getParentRef().getName() + "\t");
		}
		sb.append("\n");

		sb.append("\t\tCreatedBy = " + getCreatedBy() + "\t");
		sb.append("CreatedOn = "     + getCreatedOn() + "\t");
		sb.append("Category = "      + getCategory() + "\n");
		sb.append("\t\tSheetName = " + _sheetName + "\t");
		sb.append("RowName  = "      + _rowName + "\n");

		return sb.toString();
	}
}