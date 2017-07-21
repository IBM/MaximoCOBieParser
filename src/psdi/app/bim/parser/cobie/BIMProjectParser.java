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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.StringTokenizer;

public class BIMProjectParser
    implements
        Parser
{
	private long	                 _flags	   = 0;
	private IdFactory	             _idFactory;
	private Hashtable<String, Page>	 _parseTree;
	private LinkedList<InputFile>	 _files;
	private final AttributeTypeMap   _categoryMap;
	private final LinkedList<Filter> _filters = new LinkedList<Filter>();
	private final Locale             _locale;
	private final HashSet<String>    _spaceSet = new HashSet<String>();
	private String                   _vendorAttribute = null;
	
	// It appears that the COBie 3.0 spec will eliminate many/most of the optional
	// fields (green columns) in favor of attributes.  This list provides a mechanism
	// for an application to specify a list of columns to be treated as attributes
	private final Hashtable<String, String> _convertedFields = new Hashtable<String, String>();
	
	// Lookup for company name to contact to support vendor and future need for contacts to
	// be referenced by company name
	private final Hashtable<String, ItemCONTACT> _companyLookup = new Hashtable<String, ItemCONTACT>();
	 


	/**
	 * A count of the total number of items found. This value is set by the call
	 * to resolveReferences
	 */
	private int	                    _itemCount	= -1;
	private MessageLogger	        _logger;
	/**
	 * List of supported COBie Sheet names in the order to be processed
	 */
	public static final String[]	_pageNames	= { 
		Parser.SHEET_EXCEL, 
		Parser.SHEET_FACILITY, 
		Parser.SHEET_FLOOR,
	    Parser.SHEET_SPACE, 
	    Parser.SHEET_TYPE, 
	    Parser.SHEET_COMPONENT, 
	    Parser.SHEET_ZONE, 
	    Parser.SHEET_SYSTEM,
	    Parser.SHEET_CONTACT, 
	    Parser.SHEET_JOB, 
	    Parser.SHEET_RESOURCE, 
	    Parser.SHEET_SPARE, 
	    Parser.SHEET_ASSEMBLY,
	    Parser.SHEET_ATTRIBUTE, 
	    Parser.SHEET_DOCUMENT };

	public BIMProjectParser(
	    long flags
    ) {
		_idFactory   = new BasicIdFactory();
		_logger      = new BasicMessageLogger();
		_parseTree   = new Hashtable<String, Page>();
		_files       = new LinkedList<InputFile>();
		_categoryMap = new AttributeTypeMap();
		_locale      = Locale.getDefault();
		_flags       = flags;
	}

	public BIMProjectParser(
	    IdFactory     idFactory,
	    MessageLogger logger,
	    Locale        locale,
	    long          flags
    ) {
		_idFactory   = idFactory;
		_logger      = logger;
		_parseTree   = new Hashtable<String, Page>();
		_files       = new LinkedList<InputFile>();
		_categoryMap = new AttributeTypeMap();
		_locale      = locale;
		_flags       = flags;
	}
	
	/**
	 * Fields in this list are converted to COBie attributes instead of treated
	 * as standard parts of the COBie table.  Use of this mechanism should 
	 * Normally be restricted to the green optional columns
	 * @param tableName
	 * @param fieldName
	 * @param attributeName COBie attribute name to which the field is converted
	 */
	@Override
    public void addConvertedField(
		String tableName,
		String fieldName,
		String attributeName
	) {
		String key = tableName.toUpperCase() + fieldName;
		_convertedFields.put( key, attributeName );
	}

	@Override
    public String getConvertedField(
		String tableName,
		String fieldName
	) {
		String key = tableName.toUpperCase() + fieldName;
		return _convertedFields.get( key );
	}

	/**
	 * Specify the file name for a single sheet from a COBie spread sheet to
	 * loading
	 * 
	 * @param fileName
	 * @param sheetName
	 * @throws ParseException
	 */
	public void addFile(
	    InputFile cobieFile) throws ParseException
	{
		cobieFile.validate();
		_files.add(cobieFile);
	}
	
	@Override
	public void addSkippedSpace(
		String spaceName
	) {
		_spaceSet.add( spaceName.toUpperCase() );
	}
	
	@Override
	public boolean isSpaceSkipped(
		String spaceName
	) {
		return _spaceSet.contains( spaceName.toUpperCase() );
	}
	
	public void addFilter(
		Filter filter
	) {
		_filters.add( filter );
	}
	
	@Override
	public Iterator<Filter> filters()
	{
		return _filters.iterator();
	}

	public void execute()
	{
		// Search for each individual sheet so the upload order is deterministic
		// This determines how IDs are assigned
		for( int i = 0; i < _pageNames.length; i++ )
		{
			parseFile(_pageNames[i]);
		}
		extractAttributeTypes();
		resolveReferences();
		extractCompanies();
	}

	public void deleteFiles(
	    String workingDirRoot)
	{
		Iterator<InputFile> itr = _files.iterator();
		File workDir = new File(workingDirRoot);

		while( itr.hasNext() )
		{
			InputFile inputFile = itr.next();
			File file = new File(inputFile.getFileName());

			File currentDirectory = file.getParentFile();
			file.delete();

			while( !currentDirectory.equals(workDir) )
			{
				File deleteDirectory = currentDirectory;
				currentDirectory = deleteDirectory.getParentFile();

				if( !deleteDirectory.delete() )
				{
					break;
				}
			}
		}
		_files = new LinkedList<InputFile>();
	}

	public void addPage(
	    Page page)
	{
		_parseTree.put(page.getPageName(), page);
	}

	public void clenup()
	{
		_idFactory = null;
		_parseTree = null;
		_files = null;
	}

	/**
	 * Find any instance of the requested sheet (Maybe more than one) and load
	 * into the parser
	 * 
	 * @param sheetName
	 */
	protected void parseFile(
	    String sheetName
    ) {
		Iterator<InputFile> itr = _files.iterator();
		while( itr.hasNext() )
		{
			InputFile cobieFile = itr.next();
			if( cobieFile.getSheetName().equalsIgnoreCase(sheetName) )
			{
				InputStream is = cobieFile.getFileStream();
				if( is != null )
				{
					try
					{
						load(cobieFile, ITEM_CLASS_PREFIX);
					}
					catch( ParseException pe )
					{
						if( pe.getKey().length() > 0 )
						{
							_logger.error(pe.getKey(), pe.getParams());
						}
						else
						{
							_logger.exception(cobieFile.getSheetName(), "", pe);
						}
					}
					continue;
				}
				String fileName = cobieFile.getFileName();
				if( fileName == null || fileName.length() == 0 )
				{
					continue;
				}
				try
				{
					load(cobieFile, ITEM_CLASS_PREFIX);
				}
				catch( ParseException pe )
				{
					if( pe.getKey().length() > 0 )
					{
						_logger.error(pe.getKey(), pe.getParams());
					}
					else
					{
						_logger.exception(cobieFile.getSheetName(), "", pe);
					}
					continue;
				}
				String params[] = { cobieFile.getFileName() };
				_logger.message(Parser.MSG_FILE_PARSED, params);
				System.out.println("Load: " + cobieFile.getFileName());
				Thread.yield();
			}
		}
	}

	protected void load(
	    InputFile cobieFile,
	    String packageName
    ) 
		throws ParseException
	{
		String[] list = new String[1];
		list[0] = cobieFile.getSheetName();

		CobieParser cobieParser = new CobieParser(this, cobieFile.getSheetName(), cobieFile );
		cobieParser.parse( this, _idFactory, _parseTree, list, packageName, _flags);
		cobieParser.close();
	}

	/**
	 * Lookup the attribute type record by name;
	 * 
	 * @param name
	 * @return
	 */
	protected ItemAttributeType findAttributeType(
	    String name
    ) {
		if( name == null || name.length() == 0 )
		{
			return null;
		}
		Page page = getPage(Parser.SHEET_ATTRIB_TYPE);
		if( page == null )
		{
			return null;
		}
		Iterator<Item> itr = page.iterator();
		while( itr.hasNext() )
		{
			Item item = itr.next();
			String itemName = item.getName();
			if( itemName == null || itemName.length() == 0 )
			{
				continue;
			}
			if( itemName.equalsIgnoreCase(name) )
			{
				if( item instanceof ItemAttributeType )
				{
					return (ItemAttributeType) item;
				}
				else
				{
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Walk the parse tree and call resolve reference on every item found Must be
	 * called after all data is loaded.
	 */
	public void resolveReferences()
	{
		Page page;
		
		// Build company list from contact list
		page = _parseTree.get( Parser.SHEET_CONTACT );
		Iterator<Item> itr;
		if( page != null )
		{
			itr = page.iterator();
			while( itr.hasNext() )
			{
				ItemCONTACT item = (ItemCONTACT)itr.next();
			    String companyName = item.getCompany();
			    if( companyName != null && companyName.length() > 0 )
			    {
					_companyLookup.put( companyName.toUpperCase(), item );
			    }
			}
		}

		_itemCount = 0;
		Enumeration<Page> pages = _parseTree.elements();
		Hashtable<String, Page> tempPages = new Hashtable<String, Page>(); 
		while( pages.hasMoreElements() )
		{
			page = pages.nextElement();
			tempPages.put( page.getPageName(), page );
		}
		@SuppressWarnings("unused")			// For debugging
		long pageCount = 0;
		page = tempPages.get( Parser.SHEET_ATTRIBUTE );
		if( page != null )
		{
			itr = page.iterator();
			while( itr.hasNext() )
			{
				Item item = itr.next();
				item.resolveRerefences(this, _flags);
				_itemCount++;
			}
			pageCount++;
			tempPages.remove(  Parser.SHEET_ATTRIBUTE  );
		}
		page = tempPages.get( Parser.SHEET_FACILITY );
		if( page != null )
		{
			itr = page.iterator();
			while( itr.hasNext() )
			{
				Item item = itr.next();
				item.resolveRerefences(this, _flags);
				_itemCount++;
			}
			pageCount++;
			tempPages.remove(  Parser.SHEET_FACILITY  );
		}
		page = tempPages.get( Parser.SHEET_FLOOR );
		if( page != null )
		{
			itr = page.iterator();
			while( itr.hasNext() )
			{
				Item item = itr.next();
				item.resolveRerefences(this, _flags);
				_itemCount++;
			}
			pageCount++;
			tempPages.remove(  Parser.SHEET_FLOOR  );
		}
		page = tempPages.get( Parser.SHEET_SPACE );
		if( page != null )
		{
			itr = page.iterator();
			while( itr.hasNext() )
			{
				Item item = itr.next();
				item.resolveRerefences(this, _flags);
				_itemCount++;
			}
			pageCount++;
			tempPages.remove(  Parser.SHEET_SPACE  );
		}
		pages = tempPages.elements();
		while( pages.hasMoreElements() )
		{
			page = pages.nextElement();
			boolean addToCount = true;
			if( page.getPageName().equalsIgnoreCase(Parser.SHEET_ATTRIBUTE) )
			{
				addToCount = false;
			}
			itr = page.iterator();
			while( itr.hasNext() )
			{
				Item item = itr.next();
				item.resolveRerefences(this, _flags);
				if( addToCount )
					_itemCount++;
				pageCount++;
			}
		}
	}

	/**
	 * Populate the COBie category field from the specified attribute
	 * 
	 * @param omniClassAttribute
	 */
	public void categoryFromAttribute(
	    String omniClassAttribute)
	{
		if( omniClassAttribute == null || omniClassAttribute.length() == 0 )
		{
			return;
		}

		// Case insensitive search for name
		ItemAttributeType attribType = findAttributeType(omniClassAttribute);
		if( attribType == null )
		{
			return;
		}

		omniClassAttribute = attribType.getName();

		Enumeration<Page> pageEnum = _parseTree.elements();
		while( pageEnum.hasMoreElements() )
		{
			Page page = pageEnum.nextElement();
			// Mostly just skip attributes for efficiency
			if( page.getPageName().equals(Parser.SHEET_ATTRIB_TYPE)
			        || page.getPageName().equals(Parser.SHEET_ATTRIB_TYPE)
			        || page.getPageName().equals(Parser.SHEET_VALUE_LIST) )
			{
				continue;
			}

			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				Item item = itr.next();
				if( item instanceof ItemBase )
				{
					ItemBase ib = (ItemBase) item;
					ib.categoryFromAttribute(omniClassAttribute);
				}
			}
		}
	}

	public void spacesFromAttribute(
	    String spaceAttrib
    ) {
		if( spaceAttrib == null || spaceAttrib.length() == 0 )
		{
			return;
		}

		Page page = getPage(Parser.SHEET_COMPONENT);
		if( page != null )
		{
			Item item;
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemCOMPONENT )
				{
					((ItemCOMPONENT)item).levelFromAttribute( this, spaceAttrib );
				}
			}
		}
	}

	public void levelsFromAttribute(
	    String levelAttrib
    ) {
		if( levelAttrib == null || levelAttrib.length() == 0 )
		{
			return;
		}

		Page page = getPage(Parser.SHEET_COMPONENT);
		if( page != null )
		{
			Item item;
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemCOMPONENT )
				{
					if( ((ItemCOMPONENT)item).levelFromAttribute( this, levelAttrib ) )
					{
						itr.remove();
						page.remove(item);
					}
				}
			}
		}

		page = getPage(Parser.SHEET_SPACE);
		if( page != null )
		{
			Item item;
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSPACE )
				{
					if( ((ItemSPACE)item).levelFromAttribute( this, levelAttrib ) )
					{
						itr.remove();
						page.remove(item);
					}
				}
			}
		}
	}
	
	public void areaFromAttribute(
	    String areaAttribute)
	{
		if( areaAttribute == null || areaAttribute.length() == 0 )
		{
			return;
		}

		Item item;
		Page page = getPage(Parser.SHEET_FACILITY);
		if( page != null )
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSpaces )
				{
					((ItemSpaces) item).areaFromAttribute(this, areaAttribute);
				}
			}
		}

		page = getPage(Parser.SHEET_FLOOR);
		if( page != null )
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSpaces )
				{
					((ItemSpaces) item).areaFromAttribute(this, areaAttribute);
				}
			}
		}

		page = getPage(Parser.SHEET_SPACE);
		if( page != null )
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSpaces )
				{
					((ItemSpaces) item).areaFromAttribute(this, areaAttribute);
				}
			}
		}
	}

	public void perimeterFromAttribute(
	    String perimeterAttribute)
	{
		if( perimeterAttribute == null || perimeterAttribute.length() == 0 )
		{
			return;
		}

		Item item;
		Page page = getPage(Parser.SHEET_FACILITY);
		if( page != null )
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSpaces )
				{
					((ItemSpaces) item).perimeterFromAttribute(this, perimeterAttribute);
				}
			}
		}

		page = getPage(Parser.SHEET_FLOOR);
		if( page != null )
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSpaces )
				{
					((ItemSpaces) item).perimeterFromAttribute(this, perimeterAttribute);
				}
			}
		}

		page = getPage(Parser.SHEET_SPACE);
		if( page != null )
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item instanceof ItemSpaces )
				{
					((ItemSpaces) item).perimeterFromAttribute(this, perimeterAttribute);
				}
			}
		}
	}

	public void systemsFromAttribute(
	    String systemNameAttrib)
	{
		if( systemNameAttrib == null || systemNameAttrib.length() == 0 )
		{
			return;
		}

		// Case insensitive search for name
		ItemAttributeType attribType = findAttributeType(systemNameAttrib);
		if( attribType == null )
		{
			return;
		}

		systemNameAttrib = attribType.getName();

		Page page = getPage(Parser.SHEET_COMPONENT);
		if( page == null )
			return;

		Page pageSystem = getPage(Parser.SHEET_SYSTEM);
		if( pageSystem == null )
		{
			pageSystem = new Page(Parser.SHEET_SYSTEM);
			_parseTree.put(pageSystem.getPageName(), pageSystem);
		}

		Item item;
		Iterator<Item> itr = page.iterator();
		while( itr.hasNext() )
		{
			item = itr.next();
			if( !(item instanceof ItemCOMPONENT) )
			{
				continue;
			}
			ItemATTRIBUTE attrib;
			attrib = ((ItemBase) item).getAttribute(systemNameAttrib);
			if( attrib == null )
				continue;

			String value = attrib.getValue();
			if( value == null || value.length() == 0 )
			{
				continue;
			}
			StringTokenizer strToken = new StringTokenizer(value, ",");

			while( strToken.hasMoreElements() )
			{
				String name = strToken.nextToken();
				ItemSYSTEM system = (ItemSYSTEM) pageSystem.getItem(name);
				if( system == null )
				{
					system = new ItemSYSTEM();
					system.setName(name);
					system.setUniqueId(_idFactory.getUniqueId(Parser.SHEET_SYSTEM));
					pageSystem.put(system);
					system.resolveRerefences(this, _flags);
				}
				system.addComponent((ItemCOMPONENT) item);
			}
		}
	}
	
	public void associateCategoriesWithAttributeTypes(
		String pageList[]
	) {
		for( int i = 0; i < pageList.length; i++ )
		{
			Page page = getPage( pageList[i] );
			if( page == null ) continue;
			Iterator<Item> itemList = page.iterator();
			while( itemList.hasNext() )
			{
				ItemBase item = (ItemBase)itemList.next();  
				String category = item.getCategory();
				if( category.length() == 0 ) continue;

				OmniClassNumber ocn = new OmniClassNumber( category );
				if( ocn.isValid() )
				{
					category = ocn.format();
				}

				Enumeration<ItemATTRIBUTE> enumAttrib = item.attributes();
				while( enumAttrib.hasMoreElements() )
				{
					ItemATTRIBUTE itemAttrib = enumAttrib.nextElement();
					itemAttrib.getType().addCategory( category );
					_categoryMap.addMapping( category, pageList[i], itemAttrib.getType() );
				}
			}
		}
	}

	public ItemValueList getValueList(
	    HashSet<String> valueSet)
	{
		if( valueSet == null )
			return null;
		Page page = getPage(Parser.SHEET_VALUE_LIST);
		if( page == null )
			return null;
		Iterator<Item> itr = page.iterator();
		while( itr.hasNext() )
		{
			Object o = itr.next();
			if( !(o instanceof ItemValueList) )
				return null;
			ItemValueList valueList = (ItemValueList) o;
			if( valueList.match(valueSet) )
			{
				return valueList;
			}
		}
		ItemValueList valueList = new ItemValueList(valueSet);
		valueList.setUniqueId(_idFactory.getUniqueId(Parser.SHEET_VALUE_LIST));
		page.put(valueList);

		return valueList;
	}

	protected void extractAttributeTypes()
	{
		Page page = getPage(Parser.SHEET_ATTRIBUTE);
		if( page == null )
			return;

		Page attribTyePage = new Page(Parser.SHEET_ATTRIB_TYPE);
		_parseTree.put(attribTyePage.getPageName(), attribTyePage);

		Page valueListPage = new Page(Parser.SHEET_VALUE_LIST);
		_parseTree.put(valueListPage.getPageName(), valueListPage);

		Iterator<Item> itr = page.iterator();
		while( itr.hasNext() )
		{
			Item item = itr.next();
			if( item.getName() == null || item.getName().length() == 0 )
			{
				continue;
			}
			if( !(item instanceof ItemATTRIBUTE) )
				continue; // Should never happen
			ItemATTRIBUTE attrib = (ItemATTRIBUTE) item;
			String name = convertCase( attrib.getName() );
			ItemAttributeType type = (ItemAttributeType) attribTyePage.getItem( name );
			if( type != null )
			{
				if( type.compareAndUpdate(this, attrib) )
				{
					attrib.setType(type);
					continue;
				}
			}
			HashSet<String> valueSet = attrib.getAllowedValueSet();
			ItemValueList valueList = getValueList(valueSet);
			type = new ItemAttributeType( this, attrib, valueList, getLocale() );
			type.setUniqueId(_idFactory.getUniqueId(Parser.SHEET_ATTRIB_TYPE));
			attrib.setType(type);
			attribTyePage.put(type);
			_itemCount++;
		}
	}

	protected void consolidateComponetAttribsOnType()
	{
		Page page = getPage(Parser.SHEET_TYPE);
		if( page == null )
			return;

		Iterator<Item> itrType = page.iterator();
		while( itrType.hasNext() )
		{
			Item item = itrType.next();
			if( !(item instanceof ItemTYPE) )
				return;

			Hashtable<String, ItemATTRIBUTE> attributes = null;

			ItemTYPE type = (ItemTYPE) item;
			Enumeration<ItemCOMPONENT> components = type.components();
			while( components.hasMoreElements() )
			{
				ItemCOMPONENT comp = components.nextElement();
				Enumeration<ItemATTRIBUTE> compAttribs = comp.attributes();

				// For the first component get the full list of attributes
				if( attributes == null )
				{
					attributes = new Hashtable<String, ItemATTRIBUTE>();
					while( compAttribs.hasMoreElements() )
					{
						ItemATTRIBUTE testAttrib = compAttribs.nextElement();
						attributes.put(testAttrib.getKey(), testAttrib);
					}
					continue;
				}

				// Test each attribute for set membership, and if it is a member
				// for
				// equality. if it is not equal remove from the set.
				while( compAttribs.hasMoreElements() )
				{
					ItemATTRIBUTE testAttrib = compAttribs.nextElement();
					ItemATTRIBUTE match = attributes.get(testAttrib.getKey());
					if( match == null )
						continue;
					if( match.equals(testAttrib) )
						continue;
					attributes.remove(match.getKey());
				}

				if( attributes.isEmpty() )
					break;
			}

			if( attributes == null || attributes.isEmpty() )
				continue;

			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println(type.getName());
			Enumeration<ItemATTRIBUTE> attribEnum = attributes.elements();
			while( attribEnum.hasMoreElements() )
			{
				System.out.println(attribEnum.nextElement());
			}
		}
	}

	protected void extractCompanies()
	{
		Page page = getPage(Parser.SHEET_CONTACT);
		if( page == null )
			return;

		Page companyPage = new Page(Parser.SHEET_COMPANY);
		_parseTree.put(companyPage.getPageName(), companyPage);

		Iterator<Item> itr = page.iterator();
		while( itr.hasNext() )
		{
			Item item = itr.next();
			if( !(item instanceof ItemCONTACT) )
				return; // Should never happen
			ItemCONTACT contact = (ItemCONTACT) item;
			if( (this._flags & Parser.FLAG_ALL_CONTACTS_ARE_COMPANIES) == 0 )
			{
				if( !contact.isTypeContact() )
				{
					continue;
				}
			}
			String companyName = contact.getCompany();
			if( companyName == null || companyName.length() == 0 )
			{
				continue;
			}
			ItemCompany company = (ItemCompany) companyPage.getItem(companyName.toUpperCase());
			if( company != null )
			{
				contact.setCompanyReference(company);
				company.addContact(contact);
				company.setManufacturer( contact.isManufacture() );
				continue;
			}
			company = new ItemCompany(contact);
			company.setUniqueId( _idFactory.getUniqueId( Parser.SHEET_COMPANY ) );
			companyPage.put(company);
			contact.setCompanyReference(company);
		}
	}

	public void caculateItemCount()
	{
		_itemCount = 0;
		Enumeration<Page> pages = _parseTree.elements();
		System.out.println("================================================================================");
		while( pages.hasMoreElements() )
		{
			Page page = pages.nextElement();
			long pageCount = page.getItemCount();
			System.out.println(page.getPageName() + " = " + pageCount);
			if( page.getPageName().equalsIgnoreCase(Parser.SHEET_ATTRIBUTE)
			        || page.getPageName().equalsIgnoreCase(Parser.SHEET_VALUE_LIST) )
			{
				continue;
			}
			_itemCount += pageCount;
		}
		System.out.println("================================================================================");
	}

	@Override
	public Page getPage(
	    String pageName)
	{
		return _parseTree.get(pageName);
	}

	@Override
	public Item getItem(
	    String pageName,
	    String key
    ) {
		Page page = _parseTree.get(pageName.toUpperCase());
		if( page == null )
			return null;
		return page.getItem(key.toUpperCase());
	}

	public ItemAttributeType getAttributeType(
	    String typeName
    ) {
		Item item = getItem(SHEET_ATTRIB_TYPE, typeName);
		if( item == null || !(item instanceof ItemAttributeType) )
		{
			return null;
		}
		return (ItemAttributeType) item;
	}

	@Override
	public ItemCONTACT getCompanyFromContact(
		String companyName
	) {
		return _companyLookup.get( companyName.toUpperCase() );
	}
	
	/**
	 * Provides a centralized place to control if the parser is case sensitive.
	 * Only partially implemented.
	 * @param s
	 * @return
	 */
	@Override
	public String convertCase(
		String s
	) {
		if( s == null ) return "";
		return s.toUpperCase();	
	}
	
	public AttributeTypeMap getCategoryMap()
	{
		return _categoryMap;
	}
	
	@Override
	public Locale getLocale()
	{
		return _locale;
	}

	@Override
	public int getItemCount()
	{
		return _itemCount;
	}
	
	public String getVendorAttribute()
	{
		return _vendorAttribute;
	}

	public void printPage(
	    Page page)
	{
		if( page == null )
			return;

		System.out.println("\n================================================================================");
		System.out.println(page.getPageName());
		System.out.println("================================================================================");
		Iterator<Item> itr = page.iterator();
		while( itr.hasNext() )
		{
			System.out.println("================================================================================\n");
			Item item = itr.next();
			System.out.println(item);
		}
	}

	public void printAll()
	{
		caculateItemCount();

		Page page = getPage(Parser.SHEET_VALUE_LIST);
		printPage(page);
		page = getPage(Parser.SHEET_ATTRIB_TYPE);
		printPage(page);
		page = getPage(Parser.SHEET_FACILITY);
		printPage(page);
		page = getPage(Parser.SHEET_FLOOR);
		printPage(page);
		page = getPage(Parser.SHEET_SPACE);
		printPage(page);
		page = getPage(Parser.SHEET_COMPONENT);
		printPage(page);
		page = getPage(Parser.SHEET_ZONE);
		printPage(page);
		page = getPage(Parser.SHEET_SYSTEM);
		printPage(page);
		page = getPage(Parser.SHEET_TYPE);
		printPage(page);
		page = getPage(Parser.SHEET_CONTACT);
		printPage(page);
		page = getPage(Parser.SHEET_COMPANY);
		printPage(page);
		page = getPage(Parser.SHEET_JOB);
		printPage(page);
		page = getPage(Parser.SHEET_RESOURCE);
		printPage(page);
		page = getPage(Parser.SHEET_ASSEMBLY);
		printPage(page);
		
		System.out.println( _categoryMap.toString() );
	}

	@Override
	public MessageLogger getLogger()
	{
		return _logger;
	}

	public void setLogger(
	    MessageLogger logger)
	{
		_logger = logger;
	}

	public void setVendorAttribute(
	    String vendorAttribute
    ) {
		_vendorAttribute = vendorAttribute;
	}

	@Override
	public void export(
	    String                fileName,
	    String                pageList[],
	    InputStream           templateStream,
	    ExportProgressTracker tracker,
	    Exporter.ExportFormat fileFormat
    ) {
		Exporter exporter = null;

		switch( fileFormat )
		{
		case XLS:
		case XLSX:
			exporter = new XLSExport(_logger, templateStream, fileFormat);
			break;

		case CSV:
			exporter = new CSVExport(_logger, templateStream);
		}
		try
		{
			exportParseTree(exporter, _parseTree, pageList, fileName, tracker );
		}
		catch( IOException e )
		{
			String params[] = { CobieParser.msgFromException(e) };
			_logger.error( Parser.ERR_EXPORT_ERROR, params);
		}
	}

	private void exportParseTree(
	    Exporter                exporter,
	    Hashtable<String, Page> parseTree,
	    String[]                pageNames,
	    String                  fileName,
	    ExportProgressTracker   tracker
    ) 
		throws IOException
	{

		if( (fileName == null) || fileName.length() <= 0 )
		{
			_logger.message( Parser.ERR_EXPORT_FILE_NAME );
			return;
		}

		for( int i = 0; i < pageNames.length; i++ )
		{
			Page page = parseTree.get(pageNames[i]);
			if( page == null )
				continue;

			String[] colNames = page.getColumnNames();

			exporter.processPage(page.getPageName(), colNames);

			Iterator<Item> itr = page.iterator();

			while( itr.hasNext() )
			{
				Item item = itr.next();
				item.export(exporter);
				if( tracker != null )
				{
					if(    !item.getPageId().equals( SHEET_ATTRIBUTE )
						&& !item.getPageId().equals( SHEET_DOCUMENT ) )
					{
						tracker.itemWritenToFile( item );
					}
				}
			}

		}

		exporter.write(fileName);
	}
	
	
	public static String messageFromException(
		Throwable t
	) {
		String msg = t.getLocalizedMessage();
		if( msg == null || msg.length() == 0 )
		{
			msg = t.getMessage();
		}
		if( msg == null || msg.length() == 0 )
		{
			msg = t.getClass().getName();
		}
		return msg;
	}


	public static void main(
	    String[] args)
	{
		BIMProjectParser parser = null;
		BasicMessageLogger msgCat = new BasicMessageLogger();
		long flags = Parser.FLAG_CONVERT_GUID | Parser.FLAG_PROMOTE_COMPONENTS | Parser.FLAG_SKIP_ON_NULL 
		        | Parser.FLAG_SKIP_ON_NO_VALUE | Parser.FLAG_CONVERT_EXTENSION_COLS | Parser.FLAG_CONVERT_UNIQUE_IDs;
		parser = new BIMProjectParser(flags);

		/**
		 * List of supported COBie Sheet names in the order to be processed
		 */
		String[] pageNames = { 
				Parser.SHEET_FACILITY, 
				Parser.SHEET_FLOOR, 
				Parser.SHEET_SPACE, 
				Parser.SHEET_TYPE,
		        Parser.SHEET_COMPONENT, 
		        Parser.SHEET_ZONE, 
		        Parser.SHEET_SYSTEM, 
		        Parser.SHEET_CONTACT,
		        Parser.SHEET_ATTRIBUTE, 
		        Parser.SHEET_DOCUMENT, 
		        Parser.SHEET_JOB, 
		        Parser.SHEET_RESOURCE,
		        Parser.SHEET_SPARE,
		        Parser.SHEET_ASSEMBLY};

		int i = 0;
		while( i + 1 < args.length )
		{
			TestInputFile cobieFile = new TestInputFile(args[i++], args[i++], pageNames);
			try
			{
				parser.addFile(cobieFile);
			}
			catch( ParseException e )
			{
				String msg = msgCat.get(e.getKey(), e.getParams());
				System.err.println(msg);
				e.printStackTrace();
			}
		}
		
		parser.addConvertedField( Parser.SHEET_FLOOR, "Height",       "Height" );
		parser.addConvertedField( Parser.SHEET_FLOOR, "Elevation",    "Elevation" );

		parser.addConvertedField( Parser.SHEET_SPACE, "UsableHeight", "UsableHeight" );
		parser.addConvertedField( Parser.SHEET_SPACE, "GrossArea",    "GrossArea" );
		parser.addConvertedField( Parser.SHEET_SPACE, "NetArea",      "NetArea" );

		Filter filter = new Filter( Filter.Type.exclude );
		filter.addFilterEntry( Parser.SHEET_COMPONENT, "Duplex Receptacle", Filter.Match.substring );
		filter.addFilterEntry( Parser.SHEET_COMPONENT, "Lighting Switches", Filter.Match.substring );
		filter.addFilterEntry( Parser.SHEET_SPACE, "", Filter.Match.exact );
		filter.addFilterEntry( Parser.SHEET_ATTRIBUTE, "ObjectType", Filter.Match.exact );
		filter.addFilterEntry( Parser.SHEET_ATTRIBUTE, "^M.*", Filter.Match.regexp );
//		filter.addFilterEntry( Parser.SHEET_FLOOR,  "Level 2", Filter.Match.exact );
		filter.addFilterEntry( Parser.SHEET_SPACE, "104", Filter.Match.exact );
//		filter.addFilterEntry( Parser.SHEET_TYPE, "1220 x 1500", Filter.Match.exact );
//		filter.addFilterEntry( Parser.SHEET_TYPE, "1000 x 2000", Filter.Match.exact );
//		filter.addFilterEntry( Parser.SHEET_TYPE, "4000 x 1000", Filter.Match.exact );
//		filter.addFilterEntry( Parser.SHEET_TYPE, "1000 x 1000", Filter.Match.exact );
		
//		parser.addFilter( filter );

//		filter = new Filter( Filter.Type.include );
//		filter.addFilterEntry( Parser.SHEET_FLOOR,  "Level 1", Filter.Match.exact );
//		filter.addFilterEntry( Parser.SHEET_SPACE, ".+", Filter.Match.regexp );
//
//		parser.addFilter( filter );

		parser.execute();

		parser.spacesFromAttribute("Room Name");

		String levelAttribute = null;
		if( parser.getAttributeType("Level") != null )
		{
			levelAttribute = "Level";
		}
		if( parser.getAttributeType("Schedule Level") != null )
		{
			levelAttribute = "Schedule Level";
		}
		if( levelAttribute != null )
		{
			parser.levelsFromAttribute(levelAttribute);
		}
		parser.areaFromAttribute("Area");

		parser.perimeterFromAttribute("Perimeter");

		parser.systemsFromAttribute("System Name");

		parser.categoryFromAttribute("OmniClass Number");
		
		String Sheets[] = { Parser.SHEET_TYPE };
		parser.associateCategoriesWithAttributeTypes( Sheets );

		System.err.flush();

		parser.printAll();

		Class<?> clazz = parser.getClass();
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		String pkg = clazz.getPackage().getName();
		pkg = pkg.replace(".", "/");
		System.out.println( pkg );
		InputStream is = loader.getResourceAsStream(pkg + "/" + "cobie-spec-v2.26.json");

		parser.export("b:/temp/bimexport.xlsx", pageNames, is, null, Exporter.ExportFormat.XLSX);

//		parser.consolidateComponetAttribsOnType();
	}
}