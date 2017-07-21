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
import java.util.LinkedList;

public class   ItemASSEMBLY 
       extends ItemBase 
{
	public static final String	COBIE_ASSEMBLY_TYPE_FIXED    = "Fixed";
	public static final String	COBIE_ASSEMBLY_TYPE_OPTIONAL = "Optional";
	public static final String	COBIE_ASSEMBLY_TYPE_INCLUDED = "Included";
	public static final String	COBIE_ASSEMBLY_TYPE_EXCLUDED = "Excluded";
	public static final String	COBIE_ASSEMBLY_TYPE_LAYER    = "Layer";
	public static final String	COBIE_ASSEMBLY_TYPE_PATCH    = "Patch";
	public static final String	COBIE_ASSEMBLY_TYPE_MIX      = "Mix";

	public static final int	ASSEMBLY_TYPE_UNKNOWN  = 0;
	public static final int	ASSEMBLY_TYPE_FIXED    = 1;
	public static final int	ASSEMBLY_TYPE_OPTIONAL = 2; 
	public static final int	ASSEMBLY_TYPE_INCLUDE  = 3;
	public static final int	ASSEMBLY_TYPE_EXCLUDE  = 4;
	public static final int	ASSEMBLY_TYPE_LAYER    = 5;
	public static final int	ASSEMBLY_TYPE_PATCH    = 6;
	public static final int	ASSEMBLY_TYPE_MIX      = 7;

	private String                 _assemblyType = "";
	private String                 _childern[];
	private final LinkedList<Item> _childItemRef;
	private String                 _parentName;
	private Item                   _parentItemRef;
	private String                 _sheetName = "";

	public ItemASSEMBLY()
	{
		_childItemRef = new LinkedList<Item>();
	}
	
	@Override
    public void resolveRerefences(
		Parser project, 
		long   flags 
	) {
		super.resolveRerefences( project, flags );

		boolean badRef = false;
		String[] params = { getPageId(), getName() };
		if(  getSheetName() == null || getSheetName().length() == 0 )
		{
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_SHEET_REF, params );
			badRef = true;
		}

		if(  getParentName() == null || getParentName().length() == 0 )
		{
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_PARENT_REF, params );
			badRef = true;
		}

		if(  _childern == null || _childern.length == 0 )
		{
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_CHILD_REF, params );
			badRef = true;
		}

	    if( badRef ) return;
		
		String params1[] = { getPageId(), getName(), getSheetName(), getParentName() }; 
		Page page = project.getPage( _sheetName.toUpperCase() );
		if( page == null )
		{
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params1 );
			return;
		}
		else
		{
			_parentItemRef = page.getItem( getParentName() );
			if( _parentItemRef == null )
			{
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params1 );
			}
		}
		
		for( int i = 0; i < _childern.length; i++ )
		{
			if( isItemFiltered( project.filters(), getSheetName(),  _childern[i] ) )
			{
				continue;
			}

			Item item = page.getItem( _childern[i] );
			if( item == null )
			{
				String params2[] = { getPageId(), getName(), getSheetName(), _childern[i] }; 
				project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params2 );
				continue;
			}
			_childItemRef.add( item );
		}
	}

	@Override
	public String getPageId() 
	{
		return Parser.SHEET_ASSEMBLY;
	}

	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		
		if( isItemFiltered( parser.filters(), getSheetName(), getParentName() ) )
		{
			return true;
		}

		return super.skip( parser, flags );
	}

	public int getAssemblyType()
	{
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_FIXED) )
			return ASSEMBLY_TYPE_FIXED;
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_OPTIONAL) )
			return ASSEMBLY_TYPE_OPTIONAL;
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_INCLUDED) )
			return ASSEMBLY_TYPE_INCLUDE;
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_EXCLUDED) )
			return ASSEMBLY_TYPE_EXCLUDE;
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_LAYER) )
			return ASSEMBLY_TYPE_LAYER;
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_PATCH) )
			return ASSEMBLY_TYPE_PATCH;
		if( _assemblyType.equalsIgnoreCase( COBIE_ASSEMBLY_TYPE_MIX) )
			return ASSEMBLY_TYPE_MIX;

		return ASSEMBLY_TYPE_UNKNOWN;
	}


	public void setAssemblyType(
		String assemblyType
	) {
		_assemblyType = assemblyType;
	}

	public String[] getChildern() 
	{
		return _childern;
	}

	public void setChildNames(
		String childNames
	) {
		if( childNames == null )
		{
			childNames = "";
		}
		_childern = childNames.split( "," );
		for( int i = 0; i < _childern.length; i++ )
		{
			_childern[i] = _childern[i].trim();
		}
	}
	
	public Iterator<Item> childern()
	{
		return _childItemRef.iterator();
	}

	public String getParentName() 
	{
		return _parentName;
	}

	public void setParentName(
		String parentName
	) {
		_parentName = parentName;
	}

	public String getSheetName() 
	{
		return _sheetName;
	}

	public void setSheetName(
		String sheetName
	) {
		_sheetName = sheetName.toUpperCase();
	}

	public Item getParentItemRef() 
	{
		return _parentItemRef;
	}

	@Override
    public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( super.toString() );
		
		sb.append("\n");
		sb.append("\t\tSheetName = " + getSheetName() + "\t");
		sb.append("AssemblyType = "     + getAssemblyType() + "\t");

		if( getParentItemRef() != null )
		{
			sb.append("\n");
			sb.append("\tParent = " + getParentItemRef().getName() );
		}
		
		if( _childItemRef.size() > 0 )
		{
			sb.append("\n");
			sb.append("\tchildern\n" );
			Iterator<Item> itr = childern();
			while( itr.hasNext() )
			{
				sb.append("\t\t" + itr.next().getName() + "\n| ");
			}
		}
		
		return sb.toString();
	}
	
}
