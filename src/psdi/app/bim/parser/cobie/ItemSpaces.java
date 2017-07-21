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


public abstract class ItemSpaces
    extends ItemBase
{
	private String _grossArea = "";
	private String _netArea   = "";
	private String _perimeter = "";


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

	public String getGrossArea() {
		return _grossArea;
	}

	public String getNetArea() {
		return _netArea;
	}

	public String getPerimeter() {
		return _perimeter;
	}

	public void setGrossArea(String grossArea) 
	{
		_grossArea = filterNA( grossArea );
	}

	public void setNetArea(String netArea) 
	{
		_netArea = filterNA( netArea );
	}

	public void setPerimeter(String perimeter) 
	{
		_perimeter = filterNA( perimeter );
	}


	/**
	 * Look for an attribute by the name of name name use its value to set the gross area
	 * @return
	 */
	public boolean perimeterFromAttribute(
	    Parser project,
		String name
	) {
		ItemATTRIBUTE item = getAttribute( name );
		removeAttribute( name );
		if( item == null || item.getValue() == null )
		{
			return false;
		}
		if( getPerimeter().length() != 0 )
		{
			return false;
		}
		setPerimeter( item.getValue() );
		return true;
	}

	@Override
	public String toString() 
	{
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );

		
		sb.append("\tGrossArea = " + _grossArea + "\t");
		sb.append("NetArea = " + _netArea + "\t");
		sb.append("Perimeter = " + _perimeter + "\n");
		
		return sb.toString();
	}
}