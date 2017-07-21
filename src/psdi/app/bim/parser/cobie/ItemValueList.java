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

import java.util.HashSet;
import java.util.Iterator;

public class ItemValueList
    extends ItemBase
{
	HashSet<String> _valueSet;
	
	ItemValueList(
       	HashSet<String> valueSet
	) {
		_valueSet = valueSet;
	}

	public String getPageId()
	{
		return Parser.SHEET_VALUE_LIST;
	}
	
	public Iterator<String> values()
	{
		return _valueSet.iterator();
	}
	
	public boolean match(
		HashSet<String> valueSet
	) {
		return _valueSet.equals( valueSet );
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( getUniqueId() );
		buf.append( " \t" );
		buf.append( getName() );
		buf.append( "\t\t" );
		
		
		buf.append( "  " );
		buf.append( getDescription() );
		if( _valueSet != null )
		{
			buf.append( "\n\t\tValue List: " );
			Iterator<String> itr = _valueSet.iterator();
			while( itr.hasNext() ) 
			{
				buf.append( itr.next() );
				buf.append( ",  " );
			}
		}
		return buf.toString();
	}
}