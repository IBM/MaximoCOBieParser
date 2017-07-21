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
import java.util.regex.Pattern;

public class Filter
{
	public enum Type
	{
		include,
		exclude
	};

	public enum Match
	{
		exact,
		substring,
		regexp
	};
	
	Type _type;
	Hashtable<String, LinkedList<Entry>> _entryMap = new Hashtable<String, LinkedList<Entry>>(); 
	
	public Filter(
	) {
	}
	
	public Filter(
		Type type
	) {
		_type = type;
	}
	
	public void addFilterEntry(
		String sheetName,
		String expression,
		Match  match
	) {
		LinkedList<Entry> sheetList = _entryMap.get( sheetName );
		if( sheetList == null )
		{
			sheetList = new LinkedList<Entry>();
			_entryMap.put( sheetName, sheetList );
		}
		
		Entry entry = new Entry( expression, match );
		sheetList.add( entry );
	}
	
	public boolean isItemFiltered(
		String sheet,
		String name
	) {
		if( _type == Type.include )
		{
			return !isItemMarch( sheet, name );
		}
		else
		{
			return isItemMarch( sheet, name );
		}
	}
	
	public void setType(
		Type type
	) {
		_type = type;
	}
	
	private boolean isItemMarch(
		String sheet,
		String name
	) {
		LinkedList<Entry> sheetList = _entryMap.get( sheet );
		if( sheetList == null )
		{
			if( _type == Type.include ) return true;
			return false;
		}
		Iterator<Entry> itr = sheetList.iterator();
		while( itr.hasNext() )
		{
			Entry entry = itr.next();
			if( entry.isMatch( name ))
			{
				return true;
			}
		}
		return false;
	}
	
	private class Entry
	{
		String  _expression;
		Match   _match;
		Pattern _pattern;

		Entry(
			String expression,
			Match  match
        ) {
			if( match == Match.exact || match == Match.substring )
			{
				expression = expression.toUpperCase();
			}
			_expression = expression;
			_match      = match;
			
			if( _match == Match.regexp )
			{
				_pattern = Pattern.compile( _expression );
			}
		}
		
		boolean isMatch(
			String key
		) {
			key = key.toUpperCase().trim();
			switch( _match )
			{
			case exact:
				return key.equals( _expression );
			case substring:
				return key.contains( _expression );
			case regexp:
				return _pattern.matcher( key ).matches();
			}
			return false;
		}
	}
}