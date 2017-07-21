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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public abstract class ItemBase
       implements     Item
{
	private final static String guidString     = "^(\\{){0,1}[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}(\\}){0,1}$";
	// 65244c00-5eee-452a-b3b7-e73a38825f23-000f55ba
	private final static String revitIIDString = "^(\\{){0,1}[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}\\-[0-9a-fA-F]{8}(\\}){0,1}$";

	private String _category      = "";
	private String _createdBy     = "";
	private String _createdOn     = "";
	private String _description   = "";
	private String _extIdentifier = "";
	private String _extSystem     = "";
	private String _extObject     = "";
	private String _name          = "";
	private String _sourceFile    = "";
	private String _uniqueId      = "";

	private ItemFACILITY _facility        = null;
	private ItemBase     _parent          = null;
	private ItemCONTACT  _creatingContact = null;
	/**
	 * Used by the loader to indicate a match to an existing record
	 */
	private boolean      _match           = false;
	
	protected boolean _convertGuid           = true;
	protected boolean _convertRevitUniqueIds = false;
	private final Hashtable<String, ItemATTRIBUTE> _attributes = new Hashtable<String, ItemATTRIBUTE>();
	private final Hashtable<String, ItemDOCUMENT>  _documents  = new Hashtable<String, ItemDOCUMENT>();
	
	/**
	 * 
	 * @return false to prevent the item from being added, true to allow item to be added
	 */
	@Override
    public boolean isDuplicat( 
		Parser parser,
		Item  item 
	) { 
		if( !getKey().equals( item.getKey() )) return false;
		if( !getSourceFile().equals( item.getSourceFile() )) 
		{
			update( item );
			return false;
		}
		String params[] = { getPageId(), getKey() };
		parser.getLogger().dataIntegrityMessage( Parser.VALIDATE_DUPLICATE_ITEM, params );
		return true; 
	}
	
	@Override
    public void update( Item item )
	{
		if( item instanceof ItemBase )
		{
			ItemBase ib = (ItemBase)item;
			if( getCategory() == null || getCategory().length() == 0 )
			{
				_category = ib.getCategory();
			}
			if( getCreatedBy() == null || getCreatedBy().length() == 0 )
			{
				_createdBy = ib.getCreatedBy();
			}
			if( getCreatedOn() == null || getCreatedOn().length() == 0 )
			{
				_createdOn = ib.getCreatedOn();
			}
			if( getDescription() == null || getDescription().length() == 0 )
			{
				_description = ib.getDescription();
			}
			if( getExternalObject() == null || getExternalObject().length() == 0 )
			{
				_extIdentifier = ib.getExternalIdentifier();
			}
			if( getExternalObject() == null || getExternalObject().length() == 0 )
			{
				_extObject = ib.getExternalObject();
			}
			if( getExternalSystem() == null || getExternalSystem().length() == 0 )
			{
				_extSystem = ib.getExternalSystem();
			}
		}
	}
	
	public boolean categoryFromAttribute(
		String omniClassAttribute
	) {
		if( getCategory().length() > 0 ) return  false;
		ItemATTRIBUTE item = getAttribute( omniClassAttribute );
		if( item == null || item.getValue() == null )
		{
			return false;
		}
		
		setCategory( item.getValue() );
		
		return true;
	}

	
	public void addAttribute(ItemATTRIBUTE attrib ) 
	{
		_attributes.put(attrib.getName(), attrib);
	}
	
	public ItemATTRIBUTE getAttribute(
		String name
	) {
		return _attributes.get( name );
	}

	public void removeAttribute(
		String name
	) {
		_attributes.remove( name );
	}

	public Enumeration<ItemATTRIBUTE> attributes() 
	{
		return _attributes.elements();
	}
	
	public int getAttributeCount()
	{
		if( _attributes == null ) return 0;
		return _attributes.size();
	}
	
	public String getCategory() 
	{
		return filterNA( _category );
	}

	public void setCategory(
		String category
	) {
		if( category == null )
		{
			category = "";
		}
		else if( _name != null && category.equals(  _name ))
		{
			category = "";
		}
		else if( category.equalsIgnoreCase( "category" ))
		{
			category = "";
		}
		else
		{
			_category = filterNA( category );
		}
	}

	public String getCreatedBy() {
		return _createdBy;
	}

	public void setCreatedBy(
	    String createdBy 
    ) {
		_createdBy = filterNA( createdBy );
	}

	public String getCreatedOn()
    {
    	return _createdOn;
    }

	public void setCreatedOn(
        String createdOn )
    {
    	_createdOn = createdOn;
    }
	
	public void setCreatingContact( ItemCONTACT contact )
	{
		_creatingContact = contact;
	}
	
	@Override
    public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		_description = filterNA( description );
	}
	
	void addDocument(ItemDOCUMENT document ) 
	{
		_documents.put(document.getName(), document);
	}
	
	public ItemDOCUMENT getDocument(
		String name
	) {
		return _documents.get( name );
	}

	public void removeDocument(
		String name
	) {
		_documents.remove( name );
	}

	public Enumeration<ItemDOCUMENT> documents() 
	{
		return _documents.elements();
	}

	public String getExtIdentifier() 
	{
		return _extIdentifier;
	} 
	
	public String getExternalIdentifier() {
		return getExtIdentifier();
	}

	public ItemFACILITY getFacilityReference()
	{
		return _facility;
	}

	public void setExtIdentifier(String extIdentifier) 
	{
		if( getName().startsWith( "3_1015250" ))
		{
			System.out.println( getName());
		}
		_extIdentifier =  extIdentifier;
		// 65244c00-5eee-452a-b3b7-e73a38825f23-000f55ba
		if( _convertGuid )
		{
			_extIdentifier = processGUID( _extIdentifier );
		}
		if( _convertRevitUniqueIds )
		{
			_extIdentifier = processREvitUID( _extIdentifier );
		}
	}
	
	public String getExtSystem() {
		return _extSystem;
	}
	
	public String getExternalSystem() {
		return getExtSystem();
	}

	public void setExtSystem(String extSystem) {
		_extSystem = filterNA( extSystem );
	}
	
	public void setFacilityReference( ItemFACILITY facilityRef )
	{
		_facility = facilityRef;
	}



	public String getExtObject() 
	{
		return _extObject;
	}

	public String getExternalObject() 
	{
		return getExtObject();
	} 
	
	public void setExtObject(String extObject) {
		_extObject = extObject;
	}

	@Override
    public void setGuidConversionFlag( boolean convert )
	{
		_convertGuid = convert;
	}

	@Override
    public void setUniqueIdConversionFlag( boolean convert )
	{
		_convertRevitUniqueIds = convert;
	}

	@Override
    public String getKey() 
	{
		if( getName() != null && getName().length() > 0 )
		{
			return getName().toUpperCase();
		}
		return _uniqueId;
	}
	
	@Override
    public boolean isMatch() { return _match; }
	@Override
    public void setMatch( boolean match )
	{
		_match = match;
	}

	@Override
    public String getName() 
	{
		return filterNA( _name );
	}

	public void setName(String name) {
		_name = name;
		if( _name != null )
		{
			_name = _name.trim();
		}
	}
	
	public ItemBase getParentRef() { return _parent; }
	protected void setParentRef( ItemBase parent )
	{
		_parent = parent;
	}
	
	@Override
    public String getSourceFile() {
		return _sourceFile;
	}

	@Override
    public String getUniqueId()
    {
	    return _uniqueId;
    }
	
	@Override
    public void setSourceFile(
		String sourceFile
	) {
		_sourceFile = sourceFile;
	}
	
	@Override
    public void setUniqueId(
        String uniqueId)
    {
    	_uniqueId = uniqueId;
    }
	
	@Override
    public void resolveRerefences(
	    Parser project,
	    long    flags
    ) {
		_facility = resolveFacilityRef( project );
		if( _createdBy != null && _createdBy.length() > 0 )
		{
			Page page = project.getPage( Parser.SHEET_CONTACT );
			if( page != null )
			{
				Item item = page.getItem( _createdBy );
				if( item != null && item instanceof ItemCONTACT )
				{
					_creatingContact = (ItemCONTACT)item;
					_creatingContact.makeAdminContact();
				}
				else
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_CONTACT, _createdBy };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
			}
		}
	}
	
	public ItemFACILITY resolveFacilityRef(
		Parser project
	) {
	
		Page page = project.getPage( Parser.SHEET_FACILITY );
		if( page != null )
		{
			Item item;
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item.getKey() != null && item.getKey().trim().length() > 0 )
				{
					if( item instanceof ItemFACILITY )
					{
						// There should only be one facility and there is no
						// reference from floor to facility so take the first
						// well formed instance and hope its right
						return (ItemFACILITY) item;
					}
				}
			}
		}
		return null;		// Should never happen
	}

	@Override
    public boolean skip( 
		Parser parser, 
		long flags
	) {
		return isItemFiltered( parser.filters(), getPageId(), getName() );
	}

	
	/**
	 * Filters out all variants of the string n/a
	 * @return "" of the starting matches "n/a? else the input string
	 */
	protected String filterNA(
		String value
	) {
		if( value == null ) return "";
		if( value.trim().equalsIgnoreCase( "n/a" ))
		{
			return "";
		}
		if( value.trim().equalsIgnoreCase( "n\\a" ))
		{
			return "";
		}
		return value.trim();
	}
	
	protected String attributeListing()
	{
		StringBuffer buf = new StringBuffer();
		Enumeration<ItemATTRIBUTE> items = _attributes.elements();
		while( items.hasMoreElements() )
		{
			buf.append( items.nextElement() );
		}
		return buf.toString();
	}
	
	protected String convertCurrency(
		String currency
	) {
		currency = filterNA( currency );
		if( currency.equalsIgnoreCase( "DOLLAR" ) || currency.equalsIgnoreCase( "DOLLARS" ) )
		{
			return "USD";
		}
		if( currency.equalsIgnoreCase( "EURO" ) || currency.equalsIgnoreCase( "EUROS" ) )
		{
			return "EUR";
		}
		return currency;
	}
	
	protected String documentListing()
	{
		StringBuffer buf = new StringBuffer();
		Enumeration<ItemDOCUMENT> items = _documents.elements();
		if( items.hasMoreElements() )
		{
			buf.append( "\tDOCUMENTS =====================================\n" );
		}
		while( items.hasMoreElements() )
		{
			buf.append( items.nextElement() );
		}
		return buf.toString();
	}
	
	protected boolean isItemFiltered( 
		Iterator<Filter> filters,
		String           pageId,
		String           itemName
	) {
		if( filters == null )
		{
			return false;
		}
		while( filters.hasNext() )
		{
			Filter filter = filters.next();
			if( filter.isItemFiltered( pageId, itemName ) )
			{
				return true;
			}
		}
		return false;
	}

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( getPageId() + "\t");
		sb.append( getUniqueId() + "\t");
		sb.append("Name = "          + getName() + "\t");
		sb.append("Description = "   + _description + "\n");
		sb.append("\tCreatedBy = "   + _createdBy + " ");
		if( _creatingContact != null )
		{
			sb.append("\treference" );
		}
		sb.append("CreatedOn = "      + _createdOn + " ");
		sb.append("Category  = "      + _category + "\n");
		sb.append("\tExtSystem = "    + _extSystem + "\t");
		sb.append("ExtObject = "      + _extObject + "\t");
		sb.append("ExtIdentifier = "  + _extIdentifier + "\n");
		sb.append( "\tSource File = " + _sourceFile + "\n" );
		return sb.toString();
	}
	
	public boolean valueHasContent(
		String value
	) {
		if( value == null ) return false;
		if( value.length() == 0 ) return false;
		if( value.equalsIgnoreCase( "N/A")) return false;
		return true;
	}

	@Override
    public void export(
        Exporter exporter)
    {
		short cCnt = 0;
		String colNames[] = exporter.getColumnNames();
		String values[] = new String[colNames.length];
		
		for( int j = 0; j < colNames.length; j++ )
		{
			Method g;
			try
			{
				String name = colNames[j];
				if( name == null || name.length() == 0 )
				{
					continue;
				}
				g = getMethod( this.getClass(), "get" + name );
			}
			catch( SecurityException e )
			{
				
				exporter.getLogger().exception( e );		
				continue;
			}
			catch( NoSuchMethodException e )
			{
				
				exporter.getLogger().exception( e );	
				continue;
			}

			String value;
            try
            {
	            value = (String) g.invoke(this, new Object[] {});
				if (value == null || value.equals(""))
					value = Exporter.NA;
				values[j] = value;
				cCnt++;
            }
            catch( ClassCastException e )
            {
            	exporter.getLogger().exception( e );
            }
            catch( IllegalArgumentException e )
            {
            	exporter.getLogger().exception( e );
            }
            catch( IllegalAccessException e )
            {
            	exporter.getLogger().exception( e );
            }
            catch( InvocationTargetException e )
            {
            	exporter.getLogger().exception( e );
            }

		}  
		
		exporter.addRow(values);
    }
	
	protected Method getMethod(
	    Class<?> clazz,
	    String name) throws NoSuchMethodException
	{
		try
		{
			return clazz.getDeclaredMethod(name);
		}
		catch( NoSuchMethodException e )
		{
			if( clazz == Object.class )
			{
				// rethrow
				throw e;
			}
			return getMethod(clazz.getSuperclass(), name);
		}
	}
	protected static String processGUID(
        String uniqueId
	) {
		if( uniqueId == null || uniqueId.equalsIgnoreCase( "n/a" ))
		{
			return null;
		}
		if( !isStringGUID( uniqueId ))
		{
			if( uniqueId.length() == 22 )
			{
				String uGuid = base64toGUID( uniqueId );
				if( isStringGUID( uGuid ) )
				{
					return uGuid;
				}
			}
		}
		return uniqueId;
	}
	
	protected static String processREvitUID(
        String uniqueId
	) {
		if( uniqueId == null || uniqueId.equalsIgnoreCase( "n/a" ))
		{
			return null;
		}
		if( isStringReviUID( uniqueId ))
		{
			String uGuid = reviUIDroExportGUID( uniqueId );
			if( isStringGUID( uGuid ) )
			{
				return uGuid;
			}
		}
		return uniqueId;
	}

	public static boolean isStringGUID(
		String value
	) {
        if( value == null || value.length() == 0 )
        {
        	return false;
        }
		if( value.matches(guidString) )
		{
			return true;
		}
		return false;
	}
	
	public static boolean isStringReviUID(
		String value
	) {
        if( value == null || value.length() == 0 )
        {
        	return false;
        }
		if( value.matches(revitIIDString) )
		{
			return true;
		}
		return false;
	}
	
	public static String base64toGUID(
		String base64
	) {
		BigInteger result = new BigInteger( "0" );
		BigInteger radius = new BigInteger( "64" );
		for( int i = 0; i < base64.length(); i++ )
		{
			result = result.multiply( radius );
			int c = base64.charAt( i );
			
			int value;
			// Is it a valid base 64 digit?
			if( c >= '0' && c <= '9' )	value = c - '0';
			else if( c >= 'A' && c <= 'Z' )	value = c - 'A' + 10;
			else if( c >= 'a' && c <= 'z' )	value = c - 'a' + 36;
			else if( c == '_' )			value = 62;
			else if( c == '$' )			value = 63;
			else						return null;
			BigInteger digit = new BigInteger( "" + value );
			result = result.add( digit );
		}
		String rawGuid = result.toString( 16 );
		int padding = 32 - rawGuid.length();
		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < 32; i++ )
		{
			if( i < padding )
			{
				buf.append( '0' );
			}
			else
			{
				buf.append( rawGuid.charAt( i - padding ));
			}
			if(  i == 7 || i == 11 || i == 15 || i == 19 )
			{
				buf.append( '-' );
			}
		}
		String guid = buf.toString();
		if( isStringGUID( guid ) )
		{
			return guid;
		}
		return null;
	}
	
	public static String reviUIDroExportGUID(
		String uniqueId
	) {
		String elementId = uniqueId.substring( 37 );
		String guidBase  = uniqueId.substring( 0, 28 );
		String suffix    = uniqueId.substring( 28, 36 );
		StringBuffer buf = new StringBuffer();
		
		long v1 = Long.parseLong( elementId, 16 );
		long v2 = Long.parseLong( suffix, 16 );
		String xor = Long.toHexString( v1 ^ v2 );
		while( xor.length() < 8 )
		{
			xor = "0" + xor;
		}
		return guidBase +  xor;
	}
}