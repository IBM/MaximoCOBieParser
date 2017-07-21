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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class Page
{
	private Hashtable<String, Item> _itemHash;
	private LinkedList<Item>        _itemList;
	private String                  _pageName = null;
	private String[]                _columnNames = null;   

	public Page(
	     String sheetName
     ) {
		super();
		_pageName   = sheetName;
		_itemHash   = new Hashtable<String, Item>();
		_itemList   = new LinkedList<Item>();
	}
	
	public Iterator<Item> iterator()
	{
		return _itemList.iterator();
	}

	public Item getItem(
		String key
	) {
		if( key != null )
		{
			key = key.toUpperCase();
		}
		return _itemHash.get( key );
	}
	
	public int getItemCount()
	{
		return _itemList.size();
	}
	
	public String getPageName() {
		return _pageName;
	}
	
	public void put(
		Item item
	) {
		String key = item.getKey();
		if( key != null && key.length() > 0 )
		{
			_itemHash.put(key.toUpperCase(), item );
		}
		_itemList.add( item );
	}
	
	public void remove(
			Item item
	) {
		String key = item.getKey();
		if( key != null && key.length() > 0 )
		{
			_itemHash.remove( key.toUpperCase() );
		}
		_itemList.remove( item );
	}

	public String[] getColumnNames()
	{
		return _columnNames;
	}
	
	public void setColumnNames(
        String[] values)
    {
	    _columnNames = values;	    
    }
}