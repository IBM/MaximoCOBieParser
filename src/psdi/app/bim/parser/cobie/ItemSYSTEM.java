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

import java.util.Iterator;
import java.util.StringTokenizer;

public class   ItemSYSTEM        
       extends ItemSystemBase<ItemCOMPONENT> 
{
	private static final String COMPONENTNAME = "ComponentNames";
	
	private String _componentNames = "";
	
	public void resolveRerefences(
		Parser project,
		long    flags
	) {
		super.resolveRerefences( project, flags );

		Iterator<String> itr = memberRefs();
		while( itr.hasNext() )
		{
			String name = itr.next();
			ItemCOMPONENT newComponent = (ItemCOMPONENT) project.getItem( Parser.SHEET_COMPONENT, name);
			if( newComponent != null ) 
			{
				addComponent( newComponent );
			}
			else
			{
				String[] params = { getPageId(), getName(), Parser.SHEET_COMPONENT, name };
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
			}
		}
	}
	
	public String getPageId()
	{
		return Parser.SHEET_SYSTEM;
	}
	
	public void addComponent(
	    ItemCOMPONENT component 
    ) {
		addMember( component );
	}

	public String getComponentNames() {
		return _componentNames;
	}

	public void setComponentNames(
	    String componentNames 
    ) {
		componentNames = filterNA( componentNames );
		if( componentNames.length() == 0 ) return;
		
		// Remove surrounding quotes is present.
		if(    componentNames.length() > 1 && componentNames.charAt( 0 ) == '"' 
			&& componentNames.charAt( componentNames.length() - 1 ) == '"' )
		{
			_componentNames = componentNames.substring( 1, componentNames.length() - 1 );
		}
		else
		{
			_componentNames = componentNames;
		}

		StringTokenizer strToken = new StringTokenizer( _componentNames, "," );

		while( strToken.hasMoreElements() )
		{
			String name = strToken.nextToken();
			addMemberRef( name.trim() );
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );
		sb.append("\tComponentNames = " + _componentNames + "\n");
		
		return sb.toString();
	}

	@Override
    String getMemberName()
    {
	    return COMPONENTNAME;
    }
}