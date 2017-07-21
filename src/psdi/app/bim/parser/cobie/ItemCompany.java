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
import java.util.Hashtable;

public class ItemCompany
    extends ItemBase
{
	private boolean isManufacturue = false;

	private final Hashtable<String, ItemCONTACT> _contacts = new Hashtable<String, ItemCONTACT>();
	
	ItemCompany(
	    ItemCONTACT contact
    ) {
		super();
		setName( contact.getCompany() );
		addContact( contact );
		setManufacturer( contact.isManufacture() );
		setExtSystem( contact.getExtSystem() );
		setExtObject( contact.getExtObject() );
		setExtIdentifier( contact.getExtIdentifier() );
		setCreatedOn( contact.getCreatedOn() );
		setCreatedBy( contact.getCreatedBy() );
		setDescription( contact.getDescription() );
		setCategory( contact.getCategory() );
	}
	
	@Override
    public String getPageId()
	{
		return Parser.SHEET_COMPANY;
	}
	
	public void addContact(
		ItemCONTACT contact
	) {
		_contacts.put( contact.getKey(), contact );
	}
	
	public Enumeration<ItemCONTACT> contacts()
	{
		return _contacts.elements();
	}
	
	@Override
	public String getKey()
	{
		return getName().toUpperCase();
	}
	
	public boolean isManufacture()
	{
		return isManufacturue;
	}

	public void setManufacturer(
		boolean manufacture
	) {
		// Once true stays true
		if( manufacture )
		{
			isManufacturue = manufacture;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( super.toString() );
		if( isManufacturue ) sb.append( "\tManufacture\n" );
		
		Enumeration<ItemCONTACT> el = _contacts.elements();
		while( el.hasMoreElements() )
		{
			ItemCONTACT contact = el.nextElement(); 
			sb.append( contact.getName() + "  " );
		}
		
		sb.append( "\n" );
		
		return sb.toString();
	}
}