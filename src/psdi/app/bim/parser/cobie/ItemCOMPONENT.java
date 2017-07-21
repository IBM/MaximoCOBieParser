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



public class ItemCOMPONENT extends ItemBase {
	private String TypeName = "";
	private String Space = "";
	private String SerialNumber = "";
	private String InstallationDate = "";
	private String WarrantyStartDate = "";
	private String TagNumber = "";
	private String BarCode = "";
	private String AssetIdentifier = "";
	private String _uniqueIdLoc  = "";
	private ItemCONTACT _vendorReference = null;

	
	private long   _flags = 0;

	private ItemSPACE    _space = null;
	private ItemFLOOR    _floor = null;
	private ItemTYPE     _type  = null;

	@Override
    public void resolveRerefences(
	    Parser project,
	    long   flags
    ) {
		super.resolveRerefences( project, flags );
		String spaceName = getSpace();
		
		if( spaceName == null || spaceName.length() == 0 )
		{
			String[] params = { getPageId(), getName(), Parser.SHEET_SPACE };
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_REF, params );
		}
		else
		{
			// Comma separated list of spaces have been observed.  Best we can do is to
			// just use the first one
			String spaces[] = spaceName.split( "," );
			if( spaces.length > 1 )
			{
				String[] params = { getName() };
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_SPACE_LIST, params );
			}
			if( spaces.length > 0 )
			{
				for( int i = 0; i < spaces.length; i++ )
				{
					spaceName = filterNA(spaces[i] );
					if( spaceName.length() > 0 )
					{
						spaceName = spaces[i];
						break;
					}
				}
			}
		}

		if( spaceName != null && spaceName.length() > 0 )
		{
			Page spacePage = project.getPage( Parser.SHEET_SPACE );
			if( spacePage != null )
			{
				Item item = spacePage.getItem( spaceName );
				if( item != null )
				{
					_space = (ItemSPACE)item;
					_floor = ((ItemSPACE)item).getFloorReference();
					_space.addComponent(this);
					setParentRef( _space );
				}
				else
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_SPACE, spaceName };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
			}
		}

		// If the space isn't found, check and see if it references a level
		// This isn't complaint with the spec, but it a nice thing to do
		if( _space == null )
		{
			Page floorPage = project.getPage("FLOOR");
			if( floorPage != null )
			{
				Item item = floorPage.getItem( getSpace() );
				if( item != null )
				{
					_floor = (ItemFLOOR)item;
					_floor.addComponent(this);
					setParentRef( _floor );
				}
			}
		}
		
		Page typePage = project.getPage("TYPE");
		if( typePage != null )
		{
			if( getTypeName() != null && getTypeName().length() > 0 )
			{
				Item item = typePage.getItem( getTypeName() );
				if( item != null )
				{
					_type = (ItemTYPE) item;
					_type.addComponent(this);
				}
				else
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_TYPE, getTypeName() };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
			}
		}
		
		// Look for an attribute that has a company reference for the vendor
		String vendorAttr = project.getVendorAttribute();
		if( vendorAttr != null && vendorAttr.length() > 0 )
		{
			ItemATTRIBUTE attribute = getAttribute( vendorAttr  );
			if( attribute != null )
			{
				String companyName = attribute.getValue();
				_vendorReference = project.getCompanyFromContact( companyName );
				if( _vendorReference == null )
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_CONTACT, companyName };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
				else
				{
					_vendorReference.makeTypeContact();
				}
			}
		}
	}
	
	/**
	 * Look for an attribute by the name of name and use its value to try and find
	 * a space reference
	 * @return true if the component already had a space reference or one was successfully added
	 */
	public boolean spaceFromAttribute(
	    Parser project,
		String name
	) {
		if( _space != null ) return true;
		
		ItemATTRIBUTE item = getAttribute( name );
		if( item == null || item.getValue() == null )
		{
			return false;
		}

		removeAttribute( name );

		Page spacePage = project.getPage( Parser.SHEET_SPACE );
		if( spacePage != null )
		{
			Item space = spacePage.getItem( item.getValue() );
			if( item != null )
			{
				_space = (ItemSPACE)space;
				setParentRef( _space );
				return true;
			}
		}
		return false;
	}

	/**
	 * Look for an attribute by the name of name and use its value to try and find
	 * a floor reference
	 * @return True if the newly assigned floor is had been filtered out meaning the component
	 * should be deleted
	 */
	public boolean levelFromAttribute(
	    Parser project,
		String  name
	) {
		// Floor may have been set by a previous level attribute of by a floor reference in the
		// Space column.  First set wins
		if( _floor != null || _space != null )
		{
			removeAttribute( name );
			return false;
		}
		
		ItemATTRIBUTE item = getAttribute( name );
		if( item == null || item.getValue() == null )
		{
			return false;
		}

		removeAttribute( name );

		// Test if the floor this would be assigned to is filtered and if so remove the component
		if( isItemFiltered( project.filters(), Parser.SHEET_FLOOR, item.getValue() ) )
		{
			return true;
		}
		
		Page floorPage = project.getPage( Parser.SHEET_FLOOR );
		if( floorPage != null )
		{
			Item floor = floorPage.getItem( item.getValue() );
			if( floor != null )
			{
				_floor = (ItemFLOOR)floor;
				setParentRef( _floor );
				return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		
		String space = "";
		String spaces[] = getSpace().split( "," );
		if( spaces.length > 0 )
		{
			for( int i = 0; i < spaces.length; i++ )
			{
				space = filterNA(spaces[i] );
				if( space.length() > 0 )
				{
					space = spaces[i];
					break;
				}
			}
		}
		
		if( parser.isSpaceSkipped( space )) return true;
		
		// Various ways that a null floor could be filtered.  A null space implies a
		// null floor so it needs to be tested.
		if( space == null || space.length() == 0 )
		{
			if( isItemFiltered( parser.filters(), Parser.SHEET_FLOOR, "" ) )
			{
				return true;
			}
		}
		
		if( isItemFiltered( parser.filters(), Parser.SHEET_SPACE, space ) )
		{
			return true;
		}
		
		if( isItemFiltered( parser.filters(), Parser.SHEET_TYPE, getTypeName() ) )
		{
			return true;
		}

		_flags = flags;
		return super.skip( parser, flags );
	}

	public String getAssetIdentifier() {
		return filterNA( AssetIdentifier );
	}

	public String getBarCode() {
		return filterNA( BarCode );
	}

	public ItemFLOOR getFloorReference()
	{
		return _floor;
	}
	
	public String getInstallationDate() {
		if(    ( _flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 
				&& InstallationDate != null )
		{
			if( InstallationDate.equalsIgnoreCase( "INSTALLATIONDATE" ))
			{
				return "";
			}
		}
		return InstallationDate;
	}


	@Override
    public String getPageId()
	{
		return Parser.SHEET_COMPONENT;
	}

	public String getSerialNumber() {
		return SerialNumber;
	}

	public String getSpace() {
		return Space;
	}
	
	public ItemSPACE getSpaceReference()
	{
		return _space;
	}

	public ItemTYPE getTypeReference()
	{
		return _type;
	}

	public String getTagNumber() {
		return TagNumber;
	}

	public String getTypeName() {
		return TypeName;
	}
	
	public String getUniqueIdLoc()
	{
		return _uniqueIdLoc;
	}
	
	public ItemCONTACT getVendorReference()
	{
		return _vendorReference;
	}

	public String getWarrantyStartDate() {
		return WarrantyStartDate;
	}

	public void setAssetIdentifier(String assetIdentifier) {
		AssetIdentifier = filterNA( assetIdentifier );
	}

	public void setBarCode(String barCode) {
		BarCode = filterNA( barCode );
	}

	public void setInstallationDate(String installationDate) {
		InstallationDate = filterNA( installationDate );
	}

	public void setSerialNumber(String serialNumber) {
		SerialNumber = filterNA( serialNumber );
	}

	public void setSpace(String space) {
		Space = filterNA( space );
	}
	
	/**
	 * Compatibility with older verions of COBie
	 * @param space
	 */
	public void setSpaceNames(String space)
	{
		setSpace( space );
	}


	public void setTagNumber(String tagNumber) {
		TagNumber = filterNA( tagNumber );
	}

	public void setTypeName(String typeName) {
		TypeName = filterNA( typeName );
	}
	
	public void setUniqueIdLoc(
        String uniqueId
    ) {
		_uniqueIdLoc = uniqueId;
    }
	
	public void setWarrantyStartDate(String warrantyStartDate) {
		WarrantyStartDate = filterNA( warrantyStartDate );
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append( super.toString() );

		sb.append("\tTypeName = " + TypeName + "\n");
		sb.append("\tSpace = " + Space + "\t");
		if( _space != null )
		{
			sb.append( "Space Reference: " + _space.getName() + "\t" );
		}
		if( _floor != null )
		{
			sb.append( "Floor Reference: " + _floor.getName() + "\t" );
		}
		if( _floor == null && _space == null )
		{
			System.err.println( this.getName() + " Missing parent" );
		}
		sb.append( "\n" );
		sb.append("\tInstallationDate  = " + InstallationDate + "\t");
		sb.append("WarrantyStartDate = " + WarrantyStartDate + "\n");
		sb.append("\tTagNumber         = " + TagNumber + "\t");
		sb.append("BarCode  		 = " + BarCode + "\t");
		sb.append("SerialNumber      = " + SerialNumber + "\t");
		sb.append("AssetIdentifier   = " + WarrantyStartDate + "\n");
		if( _type != null )
		{
			sb.append( "\tType = " + _type.getName() + "  " + _type.getDescription() +  "\n" );
		}
		if( _vendorReference != null )
		{
			sb.append( "\tVendor = " + _vendorReference.getName() + "  " + _vendorReference.getDescription() +  "\n" );
		}
		

		String attribs = attributeListing();
		if( attribs != null && attribs.length() > 0 )
		{
			sb.append("\n");
			sb.append( attribs );
		}
		String docs = documentListing();
		if( docs != null && docs.length() > 0 )
		{
			sb.append("\n");
			sb.append( docs );
		}
		sb.append("\n");

		return sb.toString();
	}
}