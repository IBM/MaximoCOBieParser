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
import java.util.Set;

public class AttributeTypeMap
{
	Hashtable<String, HashSet<ItemAttributeType>>                    _categoryMap;
	Hashtable<String, Hashtable<String, HashSet<ItemAttributeType>>> _sheetMap;
	
	AttributeTypeMap()
	{
		super();
		_categoryMap = new Hashtable<String, HashSet<ItemAttributeType>>();
		_sheetMap = new Hashtable<String, Hashtable<String, HashSet<ItemAttributeType>>>();
	}
	
	void addMapping(
		String            category,
		String            sheet,
		ItemAttributeType attribType
	) {
		HashSet<ItemAttributeType> typeSet = _categoryMap.get( category );
		if( typeSet == null )
		{
			typeSet = new HashSet<ItemAttributeType>();
			_categoryMap.put( category, typeSet );
		}
		typeSet.add( attribType );
		
		Hashtable<String, HashSet<ItemAttributeType>> sheetSet = _sheetMap.get( sheet );
		if( sheetSet == null )
		{
			sheetSet = new Hashtable<String, HashSet<ItemAttributeType>>();
			_sheetMap.put( sheet, sheetSet );
		}
		typeSet = sheetSet.get( category );
		if( typeSet == null )
		{
			typeSet = new HashSet<ItemAttributeType>();
			sheetSet.put( category, typeSet );
		}
		typeSet.add( attribType );
	}
	
	public Enumeration<String> categories()
	{
		return _categoryMap.keys();
	}
	
	public Enumeration<String> categoriesBySheet(
        String sheet	                                             
    ) {
		Hashtable<String, HashSet<ItemAttributeType>> sheetSet = _sheetMap.get( sheet );
		if( sheetSet == null )
		{
			sheetSet = new Hashtable<String, HashSet<ItemAttributeType>>();
			_sheetMap.put( sheet, sheetSet );
		}
		return sheetSet.keys();
	}
	
	public Set<ItemAttributeType> getAttributeTypesByCategory(
        String category	                                                           
    ) {
		return _categoryMap.get( category );
	}
	
	public Set<ItemAttributeType> getAttributeTypesByCategoryAndSheet(
	    String sheet,
		String category	                                                           
	) {
		Hashtable<String, HashSet<ItemAttributeType>> sheetSet = _sheetMap.get( sheet );
		if( sheetSet == null )
		{
			return null;
		}

		return sheetSet.get( category );
	}
	                                                  	
	public Enumeration<String> sheets()
	{
		return _sheetMap.keys();
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "================================================================================\n" );
		sb.append( "Caregory Map\n" );
		sb.append( "================================================================================\n" );
		Enumeration<String> sheetNames = _sheetMap.keys();
		while( sheetNames.hasMoreElements() )
		{
			String sheet = sheetNames.nextElement();
			Hashtable<String, HashSet<ItemAttributeType>> sheetSet = _sheetMap.get( sheet );
			sb.append( sheet );
			sb.append( "\n" );
			Enumeration<String> keys = sheetSet.keys();
			while( keys.hasMoreElements() )
			{
				String category = keys.nextElement();
				HashSet<ItemAttributeType> typeSet = _categoryMap.get( category );
				sb.append( "\t" );
				sb.append( category );
				sb.append( "\n" );
				Iterator<ItemAttributeType> types = typeSet.iterator();
				while( types.hasNext() )
				{
					ItemAttributeType attribType = types.next();
					sb.append( "\t\t" );
					sb.append( attribType.getName() );
					sb.append( "\n" );
				}
				sb.append( "\n" );
			}
		}
		return sb.toString();
	}
}