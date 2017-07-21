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

public class ItemCONTACT
    extends
        ItemBase
{
	private String _email;
	private String _company;
	private String _phone;
	private String _department;
	private String _organizationCode;
	private String _givenName;
	private String _familyName;
	private String _street;
	private String _postalBox;
	private String _town;
	private String _stateRegion;
	private String _postalCode;
	private String _country;
	
	private ItemCompany _companyRef;
	
	/**
	 * True if the contact is referenced from a cratedBy field
	 */
	private boolean _adminContact = false;
	/**
	 * True if the product is referenced from the Type item other
	 * than createdBy;
	 */
	private boolean _typeContact = false;
	/**
	 * True if a type references the contact as a manufacture	
	 */
	private boolean isManufacturue = false;
	
	public boolean isAddressValid()
	{
		if(    getPostalCode().length() == 0 
		    && getStateRegion().length() == 0 )
		{
			return false;
		}
		if(    getPostalBox().length() == 0  
			&& getStreet().length() == 0 )
		{
			return false;
		}
		return true;
	}
	
	public void makeAdminContact()
	{
		_adminContact = true;
	}
	
	public boolean isAdminContact()
	{
		return _adminContact;
	}
	
	public boolean isManufacture()
	{
		return isManufacturue;
	}

	public boolean isTypeContact()
	{
		return _typeContact;
	}
	
	public void makeTypeContact()
	{
		_typeContact = true;
	}
	

	public String getPageId()
	{
		return Parser.SHEET_CONTACT;
	}
	
	public String getDisplayName()
	{
		String displayName = "";
		String givenName = filterNA(_givenName );
		String familyName = filterNA(_familyName );
		if( givenName.length() > 0 )
		{
			displayName = givenName + " ";
		}
		if( familyName.length() > 0 )
		{
			displayName += familyName;
		}
		if( displayName.length() == 0 )
		{
			displayName = getEmail();
		}
		return displayName;
	}

	public String getEmail()
    {
    	return filterNA( _email );
    }

	public void setEmail(
        String email 
    ) {
    	_email = email;
    }

	public String getCompany()
    {
    	return filterNA( _company );
    }

	public void setCompany(
        String company 
    ) {
    	_company = company;
    }
	
	public ItemCompany getCompanyReference()
	{
		return _companyRef;
	}
	
	public void setCompanyReference(
		ItemCompany companyRef
	) {
		_companyRef = companyRef;
	}
	
	public void setExternalIdentifier(
		String externalIdentifier
	) {
		setExtIdentifier( externalIdentifier );
	}
	
	public void setExternalObject(
		String externalObject
	) {
		setExtObject( externalObject );
	}
	
	public void setExternalSystem(
		String externalSystem
	) {
		setExtSystem( externalSystem );
	}

	public void setManufacturer(
		boolean manufacture
	) {
		isManufacturue = manufacture;
		if( _companyRef != null )
		{
			_companyRef.setManufacturer( manufacture );
		}
	}
	
	public void setName(
		String name
	) {
		super.setName( name );
		_email =  filterNA( name ); 
	}

	public String getName() 
	{
		return filterNA( _email );
	}

	public String getPhone()
    {
    	return filterNA( _phone );
    }

	public void setPhone(
        String phone 
    ) {
    	_phone = phone;
    }

	public String getDepartment()
    {
    	return filterNA( _department );
    }

	public void setDepartment(
        String department 
    ) {
    	_department = department;
    }

	public String getOrganizationCode()
    {
    	return filterNA( _organizationCode );
    }

	public void setOrganizationCode(
        String organizationCode 
    ) {
    	_organizationCode = organizationCode;
    }

	public String getGivenName()
    {
    	return filterNA( _givenName );
    }

	public void setGivenName(
        String givenName 
    ) {
    	_givenName = filterNA( givenName );
    }

	public String getFamilyName()
    {
    	return filterNA( _familyName );
    }

	public void setFamilyName(
        String familyName 
    ) {
    	_familyName = filterNA( familyName );
    }

	public String getStreet()
    {
    	return filterNA( _street );
    }

	public void setStreet(
        String street 
    ) {
    	_street = street;
    }

	public String getPostalBox()
    {
    	return filterNA( _postalBox );
    }

	public void setPostalBox(
        String postalBox 
    ) {
    	_postalBox = postalBox;
    }

	public String getTown()
    {
    	return filterNA( _town );
    }

	public void setTown(
        String town 
    ) {
    	_town = town;
    }

	public String getStateRegion()
    {
    	return filterNA( _stateRegion );
    }

	public void setStateRegion(
        String stateRegion 
    ) {
    	_stateRegion = stateRegion;
    }

	public String getPostalCode()
    {
    	return filterNA( _postalCode );
    }

	public void setPostalCode(
        String postalCode 
    ) {
    	_postalCode = postalCode;
    }

	public String getCountry()
    {
    	return filterNA( _country );
    }

	public void setCountry(
        String country )
    {
    	_country = country;
    }
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( super.toString() );
		
		if( isAdminContact()  )
		{
			sb.append("ADMIN ");
		}
		if(  isTypeContact() )
		{
			sb.append("TYPE ");
		}
		if( isAdminContact() || isTypeContact() )
		{
			sb.append("\n");
		}
		if( isManufacturue ) sb.append( "\tManufacture\n" );
		
		sb.append("\tGivenName  = " + _givenName + " n");

		sb.append("\tGivenName  = " + _givenName + " n");
		sb.append("FamilyName = " + _familyName + "\n");
		sb.append("\tEmail = " + _email + "\n");
		sb.append("\tPhone = " + _phone + "\n");
		sb.append("\tCompany = " + _company + " ");
		sb.append("Department = " + _department + " ");
		sb.append("OrganizationCode = " + _organizationCode + "\n");
		sb.append("\tStreet = " + _street + " ");
		sb.append("PostalBox = " + _postalBox + "\n");
		sb.append("\tTown = " + _town + " ");
		sb.append("StateRegion = " + _stateRegion + " ");
		sb.append("DostalCode = " + _postalCode + " ");
		sb.append("Dountry = " + _country + "\n");

		return sb.toString();
	}
}