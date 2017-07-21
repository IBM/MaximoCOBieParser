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

public class ItemSPACE extends ItemSpaces 
{
	private String _dateTimeStamp = "";
	private String _floorName = "";
	private String _roomTag = "";
	private String _usableHeight = "";
	
	private long   _flags = 0;

	private Hashtable<String, ItemCOMPONENT> _components = new Hashtable<String, ItemCOMPONENT>();
	private ItemFLOOR    _floor = null;
	

	@Override
	public void resolveRerefences(
	    Parser project,
	    long    flags
    ) {
		super.resolveRerefences( project, flags );

		// Skip if it has no name
		if( getKey() == null ) return;
		
		Page floorPage = project.getPage( Parser.SHEET_FLOOR );
		if( floorPage != null )
		{
			if( getFloorName() == null || getFloorName().length() == 0 )
			{
				String[] params = { getPageId(), getName(), Parser.SHEET_FLOOR };
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_REF, params );
			}
			else
			{
				Item item = floorPage.getItem( getFloorName() );
				if( (item instanceof ItemFLOOR) && (this.getFloorName().equals(((ItemFLOOR) item).getName())) )
				{
					_floor = (ItemFLOOR) item;
					_floor.addSpace(this);
					setParentRef( _floor );
				}
				else
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_FLOOR, getFloorName() };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
			}
		}
		if( _floor != null && ( _floorName == null || _floorName.length()  == 0  ))
		{
			_floorName = _floor.getName();
		}
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
		if( _floor != null ) return false;
		
		ItemATTRIBUTE item = getAttribute( name );
		removeAttribute( name );
		if( item == null || item.getValue() == null )
		{
			return false;
		}

		// Test if the floor this would be assigned to is filtered and if so remove the component
		if( isItemFiltered( project.filters(), Parser.SHEET_FLOOR, item.getValue() ) )
		{
			return true;
		}
		
		Page floorPage = project.getPage("FLOOR");
		if( floorPage != null )
		{
			Item floor = floorPage.getItem( item.getValue() );
			if( item != null )
			{
				_floor = (ItemFLOOR)floor;
				setParentRef( _floor );
				return false;
			}
		}
		return false;
	}

	/**
	 * Look for an attribute by the name of name name use its value to set the gross area
	 * @return
	 */
	public boolean areaFromAttribute(
	    Parser project,
		String name
	) {
		ItemATTRIBUTE item = getAttribute( name );
		removeAttribute( name );
		if( item == null || item.getValue() == null )
		{
			return false;
		}
		if( getGrossArea().length() != 0 )
		{
			return false;
		}
		setGrossArea( item.getValue() );
		return true;
	}
	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		_flags = flags;

		if( isItemFiltered( parser.filters(), Parser.SHEET_FLOOR, getFloorName() ) )
		{
			parser.addSkippedSpace( getName() );
			return true;
		}

		if( super.skip( parser, flags ) )
		{
			parser.addSkippedSpace( getName() );
			return true;
		}
		return false;
	}


	public void addComponent(ItemCOMPONENT component) {
		_components.put(component.getKey(), component);
	}

	public Enumeration<ItemCOMPONENT> components() {
		return _components.elements();
	}
	
	public int getComponentCount()
	{
		return _components.size();
	}

	public String getDateTimeStamp() {
		return _dateTimeStamp;
	}

	public String getFloorName() {
		return filterNA( _floorName );
	}

	public ItemFLOOR getFloorReference()
	{
		return _floor;
	}

	public String getPageId()
	{
		return Parser.SHEET_SPACE;
	}

	public String getRoomTag() 
	{
		if(    ( _flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 
			&& _roomTag != null )
		{
			if( _roomTag.equalsIgnoreCase( "ROOMTAG" ))
			{
				return "";
			}
		}
		return _roomTag;
	}

	public String getUsableHeight() {
		return _usableHeight;
	}

	public void setDateTimeStamp(String dateTimeStamp) {
		_dateTimeStamp = dateTimeStamp;
	}
	
	public void setFloorReference( ItemFLOOR floorRef )
	{
		_floor = floorRef;
	}



	public void setFloorName(String floorName) {
		_floorName = floorName;
	}

	public void setRoomTag(String roomTag) {
		if( roomTag.equalsIgnoreCase( "ROOM TAG" )
			|| roomTag.equalsIgnoreCase( "ROOMTAG" ))
		{
			_roomTag = "";
		}
		else
		{
			_roomTag = filterNA( roomTag );
		}
	}

	public void setUsableHeight(String usableHeight) {
		_usableHeight = filterNA( usableHeight );
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );

		sb.append("\tFloorName     = " + _floorName + "\t");
		if( _floor != null ) sb.append( "Floor Reference = " + _floor.getName() );
		sb.append( '\n' );
		
		sb.append("\tDateTimeStamp = " + _dateTimeStamp + "\t");
		sb.append("RoomTag       = " + getRoomTag() + "\n");
		
		sb.append("UsableHeight  = " + _usableHeight + "\n");
		
		sb.append("\tComponents: ");
		Enumeration<ItemCOMPONENT> componentEnum = _components.elements();
		while (componentEnum.hasMoreElements()) {
			sb.append(componentEnum.nextElement().getName());
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