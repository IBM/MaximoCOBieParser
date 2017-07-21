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


public class UniFormatNumber
{
	public static final int FORMAT_DOT = 1;
	public static final int FORMAT_DASH_SPACE = 2;
	public static final int ALL_LEVELS = Integer.MAX_VALUE;
	
	public static final String ASTM_Z_NOT_SUPPORTED = "Z";

	private boolean  _isValid = false;
	private String   _title   = "";
	private int      _level   = 0;
	private String[] _members = new String[4];

	public UniFormatNumber(String value, int level) {	

		if(value == null) return;

		//Handle ASTM format
		int idx = value.indexOf( ' ' );
		if( idx > 0 )
		{
			String temp = value.substring( 0, idx );
			_title = value.substring( idx + 1 ).trim();
			value = temp;

			//ASTM Uniformat standard doesn't support "Z" structure Entry
			if(value.startsWith(ASTM_Z_NOT_SUPPORTED))
				return;		
		}

		//Handle COBie Spread sheet data
		idx = value.indexOf( ':' );
		if( idx > 0 )
		{
			String temp = value.substring( 0, idx );
			_title = value.substring( idx + 1 ).trim();
			value = temp;
		}

		String[] _results = value.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		if(_results != null && _results.length == 2){
			if( _results[0].length() > 1  || !Character.isLetter( _results[0].charAt(0) ))
				return;

			if( _results[1] != null && !Character.isDigit( _results[1].charAt(0) ))
				return;
		}

		_members[0] = value;
		_level = level;		
		_isValid = true;
	}

	public UniFormatNumber(int level, HashMap<Integer, String> wbs) {	

		StringBuffer buf = new StringBuffer();		
		for (int i = 1; i <=  level; i++){			
			buf.append(wbs.get(i));
		}		

		_members[0] = buf.toString().trim();		
		_level = level;		
		_isValid = true;
	}


	public int getLevel()
	{
		return _level;
	}

	public String getTitle() { return _title; }

	public void setTitle( String title )
	{
		_title = title;
	}
	public boolean isValid() { return _isValid; }

	public String format()
	{	
		return format( FORMAT_DASH_SPACE, true );
	}

	public String format(
			int     format,
			boolean zeroPad
			) {				
		return format( format, zeroPad, ALL_LEVELS );
	}



	public String format(
			int     format,
			boolean zeroPad,
			int     numberOfLevels
			) {
		int level = _level;
		if( level > numberOfLevels )
		{
			level = numberOfLevels;
		}

		if( !_isValid ) return "";
		StringBuffer buf = new StringBuffer();
		if( format == FORMAT_DOT )
		{
			for( int i = 0; i < level; i++ )
			{
				buf.append( _members[i] );
				if( i < level - 1 )
				{
					buf.append( '.' );
				}
			}
		}
		else if( format == FORMAT_DASH_SPACE )
		{
			buf.append( _members[0] );			
		}
		else
		{
			return "";
		}
		return buf.toString();
	}


}

