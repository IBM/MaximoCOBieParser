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

public class OmniClassNumber
{
	public static final int FORMAT_DOT = 1;
	public static final int FORMAT_DASH_SPACE = 2;
	
	public static final int ALL_LEVELS = Integer.MAX_VALUE;
	
	private boolean  _isValid = false;
	private String[] _members = null;
	private String   _title   = "";
	private int      _level   = 0;
	
	public OmniClassNumber(
		String number
	) {
    	int idx = number.indexOf( ':' );
		if( idx > 0 )
		{
			String temp = number.substring( 0, idx );
			_title = number.substring( idx + 1 ).trim();
			number = temp;
		}
		
		// Some of the published OmniClass tables have un-printable characters where there should be spaces
		for( int i = 0; i < number.length(); i++ )
		{
			char c = number.charAt( i );
			if( c > 127 )
			{
				number = number.replace( c, ' ' );
			}
		}
		
		boolean zerosFound = false;
		
		_members = number.split( "[-]|[ ]++|[.]" );
		if( _members == null ) return;
		if( _members.length < 1 ) return;
		for( int i = 0; i < _members.length; i++ )
		{
			// Every entry is exactly 2 digits long
			String s = _members[i];
			if( _members[i].length() != 2 )
			{
				return;
			}
			if( !Character.isDigit( s.charAt(0) ) || !Character.isDigit( s.charAt(1) ) )
			{
				return;
			}
			if( s.charAt(0) == '0' && s.charAt(1) == '0' )
			{
				_members[i] = null;
				zerosFound = true;
			}
			else
			{
				// Once one set of zeros is found.  that all we should see
				if( zerosFound ) return;
			}
		}
		for( int i = 0; i < _members.length && _members[i] != null; i++ )
		{
			_level++;
		}

		_isValid = true;
	}
	
	public int getLevel()
	{
		if( !_isValid ) return -1;
		return _level;
	}
	
	public String getNumber(
		int level
	) {
		if( !_isValid ) return null;
		if( level >= _members.length ) return null;
		return _members[ level ];
	}
	
	public String getPrefix(
		int level
	) {
		if( !_isValid ) return null;
		if( level >= _members.length ) return null;
		return format( FORMAT_DASH_SPACE, true, level );
	}
	
	public int getTable()
	{
		if( !_isValid ) return -1;
		if( _members.length < 1 ) return -1;
		try
		{
			return Integer.parseInt( _members[0] );
		}
		catch( Throwable t )
		{
			return -1;
		}
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
			if( _members.length > 1 || zeroPad )
			{
				buf.append( '-' );
			}
			for( int i = 1; i < level; i++ )
			{
				buf.append( _members[i] );
				if( i < level - 1 )
				{
					buf.append( ' ' );
				}
			}
		}
		else
		{
			return "";
		}
		if( zeroPad )
		{
			for( int i = 4 - level; i > 0; i-- )
			{
				if( format == FORMAT_DOT )
				{
					buf.append( ".00" );
				}
				else if( format == FORMAT_DASH_SPACE )
				{
					buf.append( " 00" );
				}
			}
		}
		
		return buf.toString();
	}
	
	@Override
    public String toString()
	{
		if( !_isValid ) return "";
		String s = format();
		if( _title != null && _title.length() > 0 )
		{
			return s + ":" + _title;
		}
		return s;
	}
	
	public static void main(
		String args[]
	) {
		for( int i = 0; i < args.length; i++ )
		{
			OmniClassNumber ocn = new OmniClassNumber( args[i] );
			if( ocn.isValid() )
			{
				System.out.println( "Standard:\t" + ocn.toString() + "  " + ocn.getLevel() );
				System.out.println( "Dash, no pad:\t" + ocn.format(FORMAT_DASH_SPACE, false ) );
				System.out.println( "Dot:\t\t" + ocn.format(FORMAT_DOT, true ) );
				System.out.println( "Dot no pat:\t" + ocn.format(FORMAT_DOT, false ) );
			}
			else
			{
				System.out.println( "Invalid:\t" + args[i] );
			}
		}
	}
}