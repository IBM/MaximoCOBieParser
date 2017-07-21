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

import java.util.HashMap;

public class CobieSheetSpec
{
	private String	                name;
	private String	                style;
	private HashMap<String, String>	columns;

	public CobieSheetSpec(
	    String name)
	{
		this.name = name;
		columns = new HashMap<String, String>();
	}

	public String getName()
	{
		return name;
	}

	public void setName(
	    String name)
	{
		this.name = name;
	}

	public String getStyle()
	{
		return style;
	}

	public void setStyle(
	    String style)
	{
		this.style = style;
	}

	public void addColumnStyle(
	    String name,
	    String style)
	{
		columns.put(name, style);
	}

	public String getColumnStyle(
	    String name)
	{
		return columns.get(name);
	}
}