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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

public class   ItemSPARE 
	   extends ItemBase
{
	private String _typeName = "";
	private String _supplierNames = "";
	private String _setNumber = "";
	private String _partNumber = "";
	
	private ItemTYPE _type = null;
	
	
	private HashSet<String> _supplierRefs = new HashSet<String>();
	
	private Hashtable<String, ItemCONTACT> _suppliers = new Hashtable<String, ItemCONTACT>();

	
	public void addSupplier(
		ItemCONTACT supplier
	) {
		_suppliers.put(supplier.getKey(), supplier);
	}

	protected void addSupplierRef(
		String ref
	) {
		_supplierRefs.add( ref );
	}


	
	public void resolveRerefences(
		Parser project,
		long    flags
	) {
		super.resolveRerefences( project, flags );

		if( project.getPage( Parser.SHEET_TYPE ) != null )
		{
			ItemTYPE type = (ItemTYPE)project.getItem( Parser.SHEET_TYPE, getTypeName() );
			if( type == null )
			{
				String[] params = { getPageId(), getName(), Parser.SHEET_TYPE, getTypeName() };
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
			}
			else
			{
				_type = type;
				_type.addSpare(this);
			}
		}

		if( project.getPage( Parser.SHEET_CONTACT ) != null )
		{
			Iterator<String> suppliers =  _supplierRefs.iterator();
			while( suppliers.hasNext() )
			{
				String name = suppliers.next();
				ItemCONTACT contact = (ItemCONTACT)project.getItem( Parser.SHEET_CONTACT, name );
				if( contact == null )
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_CONTACT, name };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
				else
				{
					contact.makeTypeContact();
					addSupplier( contact );
				}
			}
		}
	}
	
	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		if( isItemFiltered( parser.filters(), Parser.SHEET_TYPE, getTypeName() ) )
		{
			return true;
		}
		
		if( super.skip( parser, flags ) ) return true;

		return false;
	}
	
	public String getPageId()
	{
		return Parser.SHEET_SPARE;
	}


	public String getTypeName() {
		return _typeName;
	}

	public void setTypeName(String typeName) {
		_typeName = typeName;
	}

	public String getSuppliers() {
		return _supplierNames;
	}

	public void setSuppliers(
		String suppliers
	) {
		suppliers = filterNA( suppliers );
		if( suppliers.length() == 0 ) return;

		// Remove surrounding quotes is present.
		if(    suppliers.length() > 1 
			&& suppliers.charAt( 0 ) == '"' 
			&& suppliers.charAt( suppliers.length() - 1 ) == '"' )
		{
			_supplierNames = suppliers.substring( 1, suppliers.length() - 1 );
		}
		else
		{
			_supplierNames = suppliers;
		}

		StringTokenizer strToken = new StringTokenizer( _supplierNames, "," );

		while( strToken.hasMoreElements() )
		{
			addSupplierRef( strToken.nextToken() );
		}

	}

	public String getSetNumber() {
		return _setNumber;
	}

	public void setSetNumber(String setNumber) {
		_setNumber = setNumber;
	}

	public String getPartNumber() {
		return _partNumber;
	}

	public void setPartNumber(String partNumber) {
		_partNumber = partNumber;
	}
	
	public Enumeration<ItemCONTACT> suppliers()
	{
		return _suppliers.elements();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( super.toString() );

		sb.append("TypeName      = " + _typeName );
		sb.append("\tSetNumber     = " + _setNumber );
		sb.append("\tPartNumber    = " + _partNumber + "\n");
		sb.append("Suppliers     = " + _supplierNames + "\n");
		Enumeration<ItemCONTACT> suppliers =  _suppliers.elements();
		while( suppliers.hasMoreElements() )
		{
			ItemCONTACT contact = suppliers.nextElement();
			ItemCompany company = contact.getCompanyReference();
			if( company != null )
			{
				sb.append( company.getName() + " " );
			}
		}
		sb.append("\n");

		return sb.toString();
	}
}