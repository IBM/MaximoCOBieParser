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


public class TestInputFile
    extends psdi.app.bim.parser.cobie.InputFile
{
	public TestInputFile(
	    String filename,
	    String sheetName,
	    String[] tabs 
    ) {
		super( filename, sheetName );
		_fileName  = filename;
		_sheetName = sheetName;
		
		if (( _sheetName.equals( Parser.SHEET_EXCEL ) && tabs != null))
		{
			for (String s: tabs)
			{
				if( s.equals( Parser.SHEET_FACILITY ) )
				{
					_useTabFacility = true;
				}
				else if( s.equals( Parser.SHEET_FLOOR ) )
				{
					_useTabFloor = true;
				}
				else if( s.equals( Parser.SHEET_SPACE ) )
				{
					_useTabSpace = true;
				}
				else if( s.equals( Parser.SHEET_TYPE ) )
				{
					_useTabType = true;
				}
				else if( s.equals( Parser.SHEET_COMPONENT ) )
				{
					_useTabComponent = true;
				}
				else if( s.equals( Parser.SHEET_ZONE ) )
				{
					_useTabZone = true;
				}
				else if( s.equals( Parser.SHEET_SYSTEM ) )
				{
					_useTabSystem = true;
				}
				else if( s.equals( Parser.SHEET_CONTACT ) )
				{
					_useTabContact = true;
				}
				else if( s.equals( Parser.SHEET_ATTRIBUTE ) )
				{
					_useTabAttribute = true;
				}
				else if( s.equals( Parser.SHEET_JOB ) )
				{
					_useTabJob = true;
				}
				else if( s.equals( Parser.SHEET_RESOURCE ) )
				{
					_useTabResource = true;
				}
				else if( s.equals( Parser.SHEET_SPARE ) )
				{
					_useTabSpare = true;
				}
				else if( s.equals( Parser.SHEET_ASSEMBLY ) )
				{
					_useTabAssembly = true;
				}
			}
		}
	}
}