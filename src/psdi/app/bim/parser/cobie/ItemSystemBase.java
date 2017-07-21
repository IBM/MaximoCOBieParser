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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public abstract class ItemSystemBase<ItemType extends ItemBase>
       extends        ItemBase
{
	private HashSet<String> _memberRefs = new HashSet<String>();
	
	private Hashtable<String, ItemType> _members = new Hashtable<String, ItemType>();

	
	public void addMember(
		ItemType member
	) {
		_members.put(member.getKey(), member);
	}

	protected void addMemberRef(
		String ref
	) {
		_memberRefs.add( ref );
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean isDuplicat(
		Parser parser,
	    Item    item 
    ) {
		if( !getSourceFile().equals( item.getSourceFile() ))
		{
			return true; 
		}
		if( item instanceof  ItemSystemBase )
		{
			ItemSystemBase dup = (ItemSystemBase)item;
			_memberRefs.addAll( dup._memberRefs );
			return false; 
		}
		return true; 
	}

	
	protected Iterator<String> memberRefs()
	{
		return _memberRefs.iterator();
	}
	
	public Enumeration<ItemType> members() {
		return _members.elements();
	}
	
	public int getMemberCount()
	{
		return _members.size();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );
		
		if( getFacilityReference() != null )
		{
			sb.append( getFacilityReference().getName() );
		}
		sb.append("\tMembers: ");
		Enumeration<ItemType> memberEnum = _members.elements();
		while( memberEnum.hasMoreElements() )
		{
			sb.append( memberEnum.nextElement().getName() );
			sb.append( ", " );
		}
		sb.append("\n");
		
		String docs = documentListing();
		if( docs != null && docs.length() > 0 )
		{
			sb.append("\n");
			sb.append( docs );
		}
		sb.append("\n");
		
		return sb.toString();
	}

	abstract String getMemberName();
	
	@Override
    public void export(
        Exporter exporter)
    {
		short cCnt = 0;
		String colNames[] = exporter.getColumnNames();
		String values[] = new String[colNames.length];
		short memberCol = -1;
		
		for( short j = 0; j < colNames.length; j++ )
		{
			Method g;
			try
			{
				String name = colNames[j];
				if(name == null || name.length() == 0  )
				{
					continue;
				}
				g = super.getMethod(this.getClass(), "get" + name );
				if( name.equalsIgnoreCase( this.getMemberName() ) )
				{
					memberCol = j;
					cCnt++;
					continue;
				}
			}
			catch( SecurityException e )
			{
				
				exporter.getLogger().exception( e );		
				continue;
			}
			catch( NoSuchMethodException e )
			{
				
				exporter.getLogger().exception( e );	
				continue;
			}

			String value;
            try
            {
	            value = (String) g.invoke(this, new Object[] {});
				if (value == null || value.equals(""))
					value = Exporter.NA;
				values[j] = value;
				cCnt++;
            }
            catch( IllegalArgumentException e )
            {
            	exporter.getLogger().exception( e );
            }
            catch( IllegalAccessException e )
            {
            	exporter.getLogger().exception( e );
            }
            catch( InvocationTargetException e )
            {
            	exporter.getLogger().exception( e );
            }

		}  
		
		if (memberCol > 0)
		{
			Enumeration<ItemType> members = _members.elements();
			
			while( members.hasMoreElements() )
			{
				values[memberCol] = members.nextElement().getName();
				exporter.addRow(values);
			}
		}
		else
		{
			exporter.addRow(values);
	    }
    }
}