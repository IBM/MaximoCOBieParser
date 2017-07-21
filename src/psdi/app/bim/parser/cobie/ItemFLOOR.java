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

public class ItemFLOOR extends ItemSpaces {

	private String DateTimeStamp = "";
	private String Elevation     = "";
	private String Height        = "";
	private String floorTag      = "";
	
	private final Hashtable<String, ItemSPACE> _spaces = new Hashtable<String, ItemSPACE>();
	private final Hashtable<String, ItemCOMPONENT> _components = new Hashtable<String, ItemCOMPONENT>();

	

	@Override
    public void resolveRerefences(
		Parser project,
		long    flags
	) {
		super.resolveRerefences( project, flags );
		
		// Skip if it has no name
		if( getKey() == null ) return;
		if( getFacilityReference() != null )
		{
			getFacilityReference().addFloor(this);
			setParentRef( getFacilityReference() );
		}
	}
	
	public void addComponent(ItemCOMPONENT component) {
		_components.put(component.getKey(), component);
	}
	
	public void addSpace(ItemSPACE space) {
		_spaces.put(space.getKey(), space);
	}
	
	public Enumeration<ItemCOMPONENT> components() {
		return _components.elements();
	}

	public int getComponentCount()
	{
		return _components.size();
	}
	
	public String getDateTineStamp() {
		return DateTimeStamp;
	}

	public String getElevation() {
		return Elevation;
	}

	public String getFloorTag() {
		return floorTag;
	}

	public String getHeight() {
		return Height;
	}

	@Override
    public String getPageId()
	{
		return Parser.SHEET_FLOOR;
	}
	
	public int getSpacesCount()
	{
		return _spaces.size();
	}

	public void setDateTimeStamp(String dateTimeStamp) {
		DateTimeStamp = dateTimeStamp;
	}

	public void setElevation(String elevation) {
		Elevation = filterNA( elevation );
	}

	public void setFloorTag(String floorTag ) {
		this.floorTag = filterNA( floorTag );
	}

	public void setHeight(String height) {
		Height = filterNA( height );
	}
	
	public Enumeration<ItemSPACE> spaces() {
		return _spaces.elements();
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( super.toString() );

		sb.append("\tDateTimeStamp = " + DateTimeStamp + "\n");
		sb.append("\tElevation     = " + Elevation + "\t");
		sb.append("Height = " + Height + "\n");
		sb.append("Floor Tag = " + floorTag + "\n");
		sb.append("\tSpaces: ");
		Enumeration<ItemSPACE> spaceEnum = _spaces.elements();
		while (spaceEnum.hasMoreElements()) {
			sb.append(spaceEnum.nextElement().getName());
			sb.append(", ");
		}
		sb.append("\n");
		sb.append( attributeListing() );
		sb.append("\n\t");
		sb.append( documentListing() );
		sb.append("\n");

		return sb.toString();
	}
}