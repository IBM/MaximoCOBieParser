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

public class ItemRESOURCE extends ItemBase {

	public static final int RESOURCE_TYPE_UNKNOWN  = 0;
	public static final int RESOURCE_TYPE_LABOR    = 1;
	public static final int RESOURCE_TYPE_MATERIAL = 2;
	public static final int RESOURCE_TYPE_TOOLS    = 3;
	public static final int RESOURCE_TYPE_TRAINING = 4;
	
	public static final String RESOURCE_NAME_LABOR    = "Labor";
	public static final String RESOURCE_NAME_MATERIAL = "Material";
	public static final String RESOURCE_NAME_TOOLS    = "Tools";
	public static final String RESOURCE_NAME_TRAINING = "Training";
	
	public int getResourceType()
	{
		String type = getCategory();
		if( type == null || type.length() == 0 )
		{
			return RESOURCE_TYPE_UNKNOWN;
		}
		if( type.equalsIgnoreCase( "Labor" ) ) 
		{
			return RESOURCE_TYPE_LABOR;
		}
		if( type.equalsIgnoreCase( "Material" ) ) 
		{
			return RESOURCE_TYPE_MATERIAL;
		}
		if( type.equalsIgnoreCase( "Tools" ) ) 
		{
			return RESOURCE_TYPE_TOOLS;
		}
		if( type.equalsIgnoreCase( "Training" ) ) 
		{
			return RESOURCE_TYPE_TRAINING;
		}

		return RESOURCE_TYPE_UNKNOWN;
	}
	
	public String getPageId()
	{
		return Parser.SHEET_RESOURCE;
	}
}