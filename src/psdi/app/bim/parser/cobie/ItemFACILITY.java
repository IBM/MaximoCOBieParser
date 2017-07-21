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

public class ItemFACILITY extends ItemSpaces 
{
	private String DateTimeStamp = "";
	private String ProjectName = "";
	private String SiteName = "";
	private String LinearUnits = "";
	private String AreaUnits = "";
	private String VolumeUnits = "";
	private String CurrencyUnits = "";
	private String AreaMeasurement = "";
	private String ExtProjectObject = "";
	private String ExtProjectIdentifier = "";
	private String ExtSiteObject = "";
	private String ExtSiteIdentifier = "";
	private String ProjectDescription = "";
	private String SiteDescription = "";
	private String Phase = "";

	// ExtFacilityObject              maps to ItemBase._ExtObject
	// ExternalFacilityIdentifier     maps to ItemBase._ExtIdentifier

	
	private boolean _areaUnitsValid = false;
	private boolean _currencyCodeValid = false;
	private boolean _lineraUnitsValid = false;
	private boolean _valumeUnitsValid = false;
	
	private Hashtable<String, ItemFLOOR> _floors = new Hashtable<String, ItemFLOOR>();

	@Override
	public String getKey() 
	{
		if( getName() != null && getName().length() > 0 )
		{
			return getName();
		}
		return getPageId();
	}

	public void resolveRerefences(
		Parser project, 
		long flags 
	) {
		// Nothing to do
	}
	
	@Override
	public boolean isDuplicat(
		Parser parser,
	    Item    item 
    ) {
		// In the current COBie standard, Facility is a singleton so collapse multiple
		// instances into a single instance.  THe most common reason for multiple 
		// instances is that the model is divided into multiple files such as Arch,
		// Structural, and Mechanical and a COBie workbook is generated for each all
		// of which will reference the same facility, but the level of detail may 
		// differ
		if( item instanceof  ItemFACILITY )
		{
			update( item );
			return false; 
		}
		return true; 
	}
	
	@Override
	public void update( Item item )
	{
		super.update( item );
		if( item instanceof ItemFACILITY )
		{
			ItemFACILITY facility = (ItemFACILITY)item; 
			if( getDateTimeStamp() == null || getDateTimeStamp().length() == 0 )
			{
				DateTimeStamp = facility.getDateTimeStamp();
			}
			if( getProjectName() == null || getProjectName().length() == 0 )
			{
				ProjectName = facility.getProjectName();
			}
			if( getSiteName() == null || getSiteName().length() == 0 )
			{
				SiteName = facility.getSiteName();
			}
			if( getLinearUnits() == null || getLinearUnits().length() == 0 )
			{
				LinearUnits = facility.getLinearUnits();
			}
			if( getAreaUnits() == null || getAreaUnits().length() == 0 )
			{
				AreaUnits = facility.getAreaUnits();
			}
			if( getVolumeUnits() == null || getVolumeUnits().length() == 0 )
			{
				VolumeUnits = facility.getVolumeUnits();
			}
			if( getCurrencyUnit() == null || getCurrencyUnit().length() == 0 )
			{
				CurrencyUnits = facility.getCurrencyUnit();
			}
			if( getAreaMeasurement() == null || getAreaMeasurement().length() == 0 )
			{
				AreaMeasurement = facility.getAreaMeasurement();
			}
			if( getExternalProjectObject() == null || getExternalProjectObject().length() == 0 )
			{
				ExtProjectObject = facility.getExternalProjectObject();
			}
			if( getExternalProjectIdentifier() == null || getExternalProjectIdentifier().length() == 0 )
			{
				ExtProjectObject = facility.getExternalProjectIdentifier();
			}
			if( getExternalSiteObject() == null || getExternalSiteObject().length() == 0 )
			{
				ExtSiteObject = facility.getExternalSiteObject();
			}
			if( getExternalSiteIdentifier() == null || getExternalSiteIdentifier().length() == 0 )
			{
				ExtSiteIdentifier = facility.getExternalSiteIdentifier();
			}
			if( getExternalFacilityObject() == null || getExternalFacilityObject().length() == 0 )
			{
				setExternalFacilityObject( facility.getExternalFacilityObject() );
			}
			if( getExternalFacilityIdentifier() == null || getExternalFacilityIdentifier().length() == 0 )
			{
				setExternalFacilityIdentifier( facility.getExternalFacilityIdentifier() );
			}
			if( getProjectDescription() == null || getProjectDescription().length() == 0 )
			{
				ProjectDescription = facility.getProjectDescription();
			}
			if( getSiteDescription() == null || getSiteDescription().length() == 0 )
			{
				SiteDescription = facility.getSiteDescription();
			}
			if( getPhase() == null || getPhase().length() == 0 )
			{
				Phase = facility.getPhase();
			}
		}
	}


	public void addFloor(ItemFLOOR floor) {
		_floors.put(floor.getKey(), floor);
	}

	public Enumeration<ItemFLOOR> floors() {
		return _floors.elements();
	}

	public String getAreaMeasurement() {
		return filterNA( AreaMeasurement );
	}

	public String getAreaUnits() {
		return filterNA( AreaUnits );
	}
	
	public boolean isCurrencyCodeValid()
    {
    	return _currencyCodeValid;
    }

	public void setCurrencyCodeValid(
        boolean currencyCodeValid 
    ) {
    	_currencyCodeValid = currencyCodeValid;
    }
	
	public String getCurrencyUnit() {
		return filterNA( CurrencyUnits );
	}

	public String getCurrencyUnits() {
		return filterNA( CurrencyUnits );
	}

	public void setExternalSystem(
		String externalSystem
	) {
		setExtSystem( externalSystem );
	}

	public String getDateTimeStamp()
    {
    	return DateTimeStamp;
    }

	public String getExternalFacilityIdentifier() {
		return super.getExternalIdentifier();
	}
	
	public String getExternalFacilityObject() 
	{
		return super.getExternalObject();
	}

	@Override
	public String getExternalIdentifier() {
		return getExternalFacilityIdentifier();
	}

	public String getExternalProjectIdentifier() {
		return ExtProjectIdentifier;
	}

	public String getExternalProjectObject() {
		return filterNA( ExtProjectObject );
	}

	public String getExternalSiteIdentifier() {
		return ExtSiteIdentifier;
	}

	public String getExternalSiteObject() {
		return filterNA( ExtSiteObject );
	}

	public int getFloorCount()
	{
		return _floors.size();
	}

	public String getLinearUnits() {
		return filterNA( LinearUnits );
	}

	public String getName() 
	{
		// Loader expects all locations to have a name, but in the currentCOBie standard
		// Facility is a singleton so fill in the sheet name "FACILITY" if no name is
		// provided
		String name = super.getName();
		if( name != null && name.length() > 0 )
		{
			return name;
		}
		return getPageId();
	}

	public String getPageId()
	{
		return Parser.SHEET_FACILITY;
	}

	public String getPhase() {
		return filterNA( Phase );
	}

	public String getProjectDescription() {
		return filterNA( ProjectDescription );
	}

	public String getProjectName() {
		return filterNA( ProjectName );
	}

	public String getSiteDescription() {
		return filterNA( SiteDescription );
	}

	public String getSiteName() {
		return filterNA( SiteName );
	}

	public String getVolumeUnits() {
		return filterNA( VolumeUnits );
	}

	public boolean isAreaUnitsValid()
    {
    	return _areaUnitsValid;
    }

	public boolean isLineraUnitsValid()
    {
    	return _lineraUnitsValid;
    }
	
	public boolean isValumeUnitsValid()
    {
    	return _valumeUnitsValid;
    }
	
	public void setAreaMeasurement(String areaMeasurement) {
		AreaMeasurement = areaMeasurement;
	}

	public void setAreaUnits(String areaUnits) {
		AreaUnits = areaUnits;
	}

	public void setAreaUnitsValid(
        boolean areaUnitsValid 
    ) {
    	_areaUnitsValid = areaUnitsValid;
    }

	public void setCurrencyUnit(
		String currencyUnit
	) {
		setCurrencyUnits( currencyUnit );
	}

	public void setCurrencyUnits(
	    String currencyUnits 
    ) {
		CurrencyUnits = convertCurrency( currencyUnits ); 
	}

	public void setDateTimeStamp(
	    String dateTimeStamp
    ) {
		DateTimeStamp = dateTimeStamp;
	}

	public void setExternalFacilityIdentifier(
	    String extExternalFacilityIdentifier 
    ) {
		setExtFacilityIdentifier( extExternalFacilityIdentifier );
	}
	
	public void setExtFacilityIdentifier(
	    String extFacilityIdentifier 
    ) {
		super.setExtIdentifier( extFacilityIdentifier );
	}

	public void setExternalFacilityObject(
	    String extExternalFacilityObject 
    ) {
		setExtFacilityObject( extExternalFacilityObject );
	}

	public void setExtFacilityObject(
	    String extFacilityObject 
    ) {
		super.setExtObject( extFacilityObject );
	}
	
	public void setExternalProjectIdentifier(
	    String externalProjectIdentifier
    ) {
		setExtProjectIdentifier( externalProjectIdentifier );
	}
	
	public void setExtProjectIdentifier(
	    String extProjectIdentifier
    ) {
		if( !_convertGuid )
		{
			ExtProjectIdentifier =  extProjectIdentifier;
		}
		else
		{
			ExtProjectIdentifier = processGUID( extProjectIdentifier );
		}
	}

	public void setExternalProjectObject(
	    String externalProjectObject 
    ) {
		setExtProjectObject( externalProjectObject );
	}

	public void setExtProjectObject(
	    String extProjectObject 
    ) {
		ExtProjectObject = extProjectObject;
	}

	public void setExternalSiteIdentifier(
	    String externalSiteIdentifier
    ) {
		setExtSiteIdentifier( externalSiteIdentifier );
	}
	
	public void setExtSiteIdentifier(
	    String extSiteIdentifier
    ) {
		if( !_convertGuid )
		{
			ExtSiteIdentifier =  extSiteIdentifier;
		}
		else
		{
			ExtSiteIdentifier = processGUID( extSiteIdentifier );
		}
	}

	public void setExternalSiteObject(
	    String extExternalSiteObject 
    ) {
		setExtSiteObject( extExternalSiteObject );
	}

	public void setExtSiteObject(
	    String extSiteObject 
    ) {
		ExtSiteObject = extSiteObject;
	}

	public void setLinearUnits(String linearUnits) {
		LinearUnits = linearUnits;
	}

	public void setLineraUnitsValid(
        boolean lineraUnitsValid 
    ) {
    	_lineraUnitsValid = lineraUnitsValid;
    }

	public void setPhase(
	    String phase
    ) {
		Phase = phase;
	}

	public void setProjectDescription(String projectDescription) {
		ProjectDescription = projectDescription;
	}

	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}

	public void setSiteDescription(String siteDescription) {
		SiteDescription = siteDescription;
	}

	public void setSiteName(String siteName) {
		SiteName = siteName;
	}

	public void setValumeUnitsValid(
        boolean valumeUnitsValid 
    ) {
    	_valumeUnitsValid = valumeUnitsValid;
    }

	public void setVolumeUnits(String volumeUnits) {
		VolumeUnits = volumeUnits;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );
		
		sb.append("\tDateTimeStamp         = " + DateTimeStamp + "\t");
		sb.append("Phase                   = " + Phase + "\t");
		sb.append("\tProjectName           = " + ProjectName + "\t");
		sb.append("ProjectDescription      = " + ProjectDescription + "\t");
		sb.append("ExtProjectObject        = " + ExtProjectObject + "\t");
		sb.append("ExtProjectIdentifier    = " + ExtProjectIdentifier + "\n");
		sb.append("\tSiteName              = " + SiteName + "\t");
		sb.append("SiteDescription         = " + SiteDescription + "\t");
		sb.append("ExtSiteObject           = " + ExtSiteObject + "\t");
		sb.append("\tExtSiteIdentifier     = " + ExtSiteIdentifier + "\n");
		sb.append("\tLinearUnits           = " + LinearUnits + "\t");
		sb.append("AreaUnits               = " + AreaUnits + "\t");
		sb.append("VolumeUnits             = " + VolumeUnits + "\t");
		sb.append("CurrencyUnits           = " + CurrencyUnits + "\n");
		sb.append("\tAreaMeasurement       = " + AreaMeasurement + "\n");
		sb.append("\tFloors: ");
		Enumeration<ItemFLOOR> floorEnum = _floors.elements();
		while (floorEnum.hasMoreElements()) {
			sb.append(floorEnum.nextElement().getName());
			sb.append(", ");
		}
		sb.append("\n\t");
		sb.append( attributeListing() );
		sb.append("\n\t");
		sb.append( documentListing() );
		sb.append("\n");

		return sb.toString();
	}
}