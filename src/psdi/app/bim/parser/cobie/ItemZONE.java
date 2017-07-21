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

public class   ItemZONE 
       extends ItemSystemBase<ItemSPACE> 
{
	private static final String SPACENAMES = "SpaceNames";
	
	private String SpaceNames = "";
	
	public void resolveRerefences(
		Parser project,
		long    flags
	) {
		super.resolveRerefences( project, flags );

		Iterator<String> itr = memberRefs();
		while( itr.hasNext() )
		{
			String name = itr.next();
			ItemSPACE newSpace = (ItemSPACE) project.getItem( Parser.SHEET_SPACE, name);
			if (newSpace != null)
			{
				addSpace(newSpace);
			}
			else
			{
				String[] params = { getPageId(), getName(), Parser.SHEET_SPACE, name };
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
			}
		}
	}
	
	public String getPageId()
	{
		return Parser.SHEET_ZONE;
	}

	public void addSpace(
	    ItemSPACE space 
    ) {
		addMember( space );
	}

	public void setSpaceNames(String spaceNames) {
		
		spaceNames = filterNA( spaceNames );
		if( spaceNames.length() == 0 ) return;

		//Remove surrounding quotes is present.
		if(    spaceNames.length() > 1 
		    && spaceNames.charAt(0) == '"'  
			&& spaceNames.charAt(spaceNames.length() -1 ) == '"')
		{
			spaceNames = spaceNames.substring(1,spaceNames.length() -1);
		}
		StringTokenizer strToken = new StringTokenizer( spaceNames, ",");
		
		while (strToken.hasMoreElements())
		{
			String name = strToken.nextToken();
			addMemberRef( name.trim() );
		}
	}

	public String getSpaceNames() {
		
		Iterator<String> it = memberRefs();
		StringBuffer str = new StringBuffer();
		while (it.hasNext())
		{
			str.append(it.next());
			str.append(',');
		}
		
		return str.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append( super.toString() );

		sb.append("\tSpaceNames    = " + SpaceNames + "\n");
		
		return sb.toString();
	}

	@Override
    String getMemberName()
    {
	    return SPACENAMES;
    }
}