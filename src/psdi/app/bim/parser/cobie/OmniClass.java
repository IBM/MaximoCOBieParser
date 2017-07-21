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

import java.util.Enumeration;
import java.util.Hashtable;

import psdi.app.bim.parser.cobie.BIMClassification;

public class OmniClass implements BIMClassification
{
	String _name       = "";
	String _title      = "";
	String _definition = "";
	int    _level;
	Hashtable<String, OmniClass> _children;
	
	
	/**
     * @param _name the _name to set
     */
    public OmniClass(
        String name, 
        String title,
        int    level,
        String definition
    ) {
    	_title = title;
		OmniClassNumber ocn = new OmniClassNumber( name );
		if( ocn.isValid() )
		{
	    	_name = ocn.format();
	    	_level = ocn.getLevel() - 1;
	    	if( _title == null || _title.length() == 0 )
	    	{
	    		_title = ocn.getTitle();
	    	}
	    	if( _title == null ) _title = "";
		}
		else
		{
	    	_name = name;
	    	_level = level;
		}
    	_definition = definition;
    	if( _definition == null ) _definition = "";
    	_children = new Hashtable<String, OmniClass>();
    }

    public OmniClass(
    	OmniClassNumber oomniclassNumber,
        String definition
    ) {
		if( oomniclassNumber.isValid() )
		{
	    	_name = oomniclassNumber.format();
	    	_level = oomniclassNumber.getLevel() - 1;
	    	if( _title == null || _title.length() == 0 )
	    	{
	    		_title = oomniclassNumber.getTitle();
	    	}
		}
    	_definition = definition;
    	_children = new Hashtable<String, OmniClass>();
    }

    public void addChild(
		OmniClass child
	) {
		_children.put(  child.getName(), child );
	}
	
    public Enumeration<OmniClass> getChildern()
   	{
   		return _children.elements();
   	}
	
	/**
     * @return the _name
     */
    public String getName()
    {
    	return _name;
    }
    
    public long getMemberCount()
    {
    	long count = 1;
    	Enumeration<OmniClass> childern = _children.elements();
    	while( childern.hasMoreElements() )
    	{
    		count += childern.nextElement().getMemberCount();
    	}
    	return count;
    }
    
	/**
     * @return the _title
     */
    public String getTitle()
    {
    	return _title;
    }
	/**
     * @return the _definition
     */
    public String getDefinition()
    {
    	return _definition;
    }
	/**
     * @return the _level
     */
    public int getLevel()
    {
    	return _level;
    }
	
	
    @Override
    public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    	for( int i = 0; i < _level; i++ ) sb.append( "\t" );
    	sb.append( _name );
    	int level = _level;
    	if( level < 4 )  level = 4;
    	int tabcount = 12 - ((3 * level -1 ) / 4) - _level ;
    	for( int i = 0; i < tabcount; i++ ) sb.append( "\t" );
    	sb.append( _level );
    	sb.append( "\t\t" );
    	sb.append( _title );
    	sb.append( "\t\t" );
    	sb.append( _definition );
    	sb.append( "\n" );
    	Enumeration<?> ec = getChildern();
    	while( ec.hasMoreElements() )
    	{
    		sb.append( ec.nextElement().toString() );
    	}
    	return sb.toString();
    }
}