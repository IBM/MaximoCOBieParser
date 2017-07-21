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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CsvInputTokenizer implements InputTokenizer
{
	private InputStream _is;

	public CsvInputTokenizer(
		InputStream is
	) {
		_is = is;
	}
	
	public String[] getRow() 
		throws IOException 
	{
	    ArrayList<String> strings = new ArrayList<String>();
	    StringBuilder sb = new StringBuilder();

	    int state = 0;
     	int uc = _is.read();
     	if( uc == -1 )
     	{
     		return null;
     	}
     	char c = (char)uc;
     	boolean lineEnd = false;
	    while( !lineEnd ) 
	    {
	    	switch( state )
	    	{
		    	case 0:					// Start state
		    		if( c == '\n' )
		    		{
		    			lineEnd = true;
		    			continue;
		    		}
		    		if( Character.isWhitespace( c )) 	// Filter white space at start of token
		    		{
		    			break;
		    		}
		    		if( c == '"' )		// See start of quoted string;
		    		{
		    			state = 2;
		    		}
		    		else if( c == ',' )	// See empty token
		    		{
		    			state = 10;
		    		}
		    		else				// Start normal token
		    		{
			            sb.append(c);
			            state = 1;
		    		}
		    		break;
		    	case 1:					// Normal non quoted token
		    		if( c == ',' )	
		    		{
		    			state = 10;
		    		}
		    		else				// Start normal token
		    		{
			            sb.append(c);
		    		}
		    		break;
		    	case 2:					// Start of quoted String
		    		if( c == '\n' )     // Skip over new lines inside of quotes
		    		{
		    	     	uc = _is.read();
		    	     	if( uc == -1 )
		    	     	{
			    			lineEnd = true;
		    	     	}
		    	     	c = (char)uc;
		    	     	continue;
		    		}
		    		if( c == '"' )		// Either end quite or escape
		    		{
		    			state = 3;
		    		}
		    		else				// Add character from quoted string
		    		{
			            sb.append(c);
		    	     	uc = _is.read();
		    	     	if( uc == -1 )
		    	     	{
			    			lineEnd = true;
		    	     	}
		    	     	c = (char)uc;
		    	     	continue;
		    		}
	    			break;
		    	case 3:					// Test for escaped quote
		    		if( c == '"' )		// Second quite so escape
		    		{
		    			state = 2;
			            sb.append(c);
			    		break;
		    		}
		    		else				// Seen quoted string;
		    		{
		    			state = 4;
		    			continue;
		    		}
		    	case 4:					// Seen quoted string throw away until comma
		    		if( c == ',' )	
		    		{
		    			state = 10;
		    		}
		    		break;
		    	case 10:				// Token recognized;
					strings.add(sb.toString().trim());
					sb.delete(0, sb.length());
					state = 0;
		    		continue;
	    	}
	     	uc = _is.read();
	     	if( uc == -1 )
	     	{
	     		break;
	     	}
	     	c = (char)uc;
    		if( c == '\n' )
    		{
    			break;
    		}
	    }
	    
	    if (sb.length() > 0)
	    {
	    	strings.add(sb.toString().trim());
            sb.delete(0, sb.length());
	    }
	    return strings.toArray(new String[strings.size()]);
	}

    public boolean setCurrentTab(
        String name
    ) {
	    return true;
    }

	public void close()
	{
		try
        {
	        _is.close();
        }
        catch( IOException e )
        { /* Ignore */ }
		_is       = null;
	}
}