# Maximo COBie Parser
The COBie parser provides support for reading and writing Excel spreadsheets that implement the [COBie Standard](https://www.nibs.org/?page=bsa_cobie).  Input files may be in .xls, .xlsx or .csv format with each file being a single COBIe table.  It is intended to be embedded in a larger application.  Reading the COBie files produces an in-memory parser tree that must then be processed by the controlling application. All cross-table references are resolved and expressed as Java object references.

The reverse is also possible:  The controlling application can build the parse tree from its data, then call the parse export method to write a .xls or .xlsx COBie format file.

## Features

- **COBie file Validation:** The parse can run in validate mode. WHen it does, it messages about internal structure issues including:
	- Missing and duplicate names
	- Missing and invalid references
	- Components that reference multiple spaces. This is legal in COBie, but Maximo only processes the first valid reference
	- Job takes numbers and references.
	- Attributes whose value is their name.  This typically means the value was never set.
	- Attributes that have the same name but different value lists
	If the parse is merging multiple files, that validation is performed on the result of the merge.

- **Merge:** Any number of COBie files may be specified on input.  The input is treated as if all the data was contained in a single file.  Except, when entries with duplicate names are encountered in more than one file, they are merged.  The first instance encountered takes precedence for base values and reference against any of the duplicates are resolved to the merged record.  This is particularly useful for merging rooms from architectural models with spaces from MEP models.

- **Filters:** Filters can be applied against the name columm of any COBie table. The filter is an expression match between the filter string and the value of the name column.  There are three type of matching expressions:


	- Exact: A case insensitive comparison is performed on the filter and the key value for each row tested.  If they are identical, the filter matches the row.
	- Substring:  Both the filter and the key value for each row are folded to upper case. Then the key value is searched for the filter.  If the filter string appears anywhere in the key value, the filter matches the row.
	- Regular Expression:  The key value is folded to upper case.  The filter is treated as a regular expression defined here:
		http://docs.oracle.com/javase/1.4.2/docs/api/java/util/regex/Pattern.html
	If the key matches the regular expression, then the filter matches the row.

	A filter may be either an inclusive filter or an exclusive filter.  The type applies to all entries in the filter.
	-	Exclude: For each row read from a COBie spreadsheet or CSV file, the row is tested against every entry in the filter.  If the row matches any entry it is discarded.
	-	Include: For each row read from a COBie spreadsheet or CSV file, the row is tested against every entry in the filter.  If it does not match any entry, it is discarded.  However, if there is no entry for a given COBie table, then that table is not filtered and all rows are used.

	Filters process references that are inherent in a single row of data.  References are tested before the row is tested and if the referenced row is excluded, the row being tested is also excluded.  The following references are tested:
	-	Attribute references using both sheet and row
	-	Document references using both sheet and row
	-	Component references to a space
	-	Component references to a type
	-	Job reference to a Type
	Additional processing is provided to support filtering by floor.  Spaces that reference an excluded floor are excluded, and then references to the Space are also excluded as described above.

	Since the filter is applied immediately upon reading a row, references are not yet resolved and in fact referenced objects need not have been read.  Therefor the above behavior is only one level deep.  For example, if a space is excluded causing components that reference it to be excluded, attributes and documents that reference the components are not excluded.  However, the attribute and documents will not ultimately be imported since attributes and documents are imported as part of the object they reference.  A Validate session will report unresolved references for these attributes and documents.

	Filters are applied immediately upon reading a row from the input file so use of filters can reduce memory usage.

- **Special attribute processing:** It has been observed over the years that many of the attributes contain data that is germaine to the structure of the COBie file.  Historically many of these were not prcessed by export tools.  The current generation of tools is much better, however the future remains in the parser. 
	
	The following special processing can be performed on attributes:

	-	Level Attribute:  Each item in the Space and the Component page is searched for the named attribute.  If it is found it is assumed to be the attribute that has a level reference. For each component that has an instance of that attribute type, an attempt is made to resolve the value of the attribute as a floor reference.  If it succeeds, and if the component does not have a valid space reference, then the component is associated with that floor.

	The level reference is explicitly checked against any Floor filters and excluded or included as specified by the filter

	- Use Space Attribute:  Each item in the Component page is searched for an attribute of the name provided.  For each component that has an instance of that attribute type, an attempt is made to resolve the value of the attribute as a space reference.  If it succeeds, and if the component does not have a valid space reference, then the component is associated with that space.

	- Use system Name Attribute:  Each component is search for an attribute of the name provided. The attribute is assumed to contain a comma separated list of systems to which the component belongs.  For each item in the list, the list of existing systems is searched and if a match is found, the component is added to the system.  If no march is found, a new system is created with the component as its first member.

	- Use OmniClass Number Attribute:  Each item in it that has a category value (Facility, Floor, Space, System, Zone, and Type) is searched for an attribute that matches the value of the OmniClass number.  If the category field is blank, it is set to the value of the attribute.  No validation is done that it is a valid OmniClass number so this mechanism can be used with any classification scheme.  

- **COBie Extension Columns:** The COBie specification allows additional user defined columns to be added to any table.  These columns may be converted to attributes on the first step of the import and treated exactly like attributes for all further processing.

- **Export:** Write the COBie parse tree to .xls or .xlsx format

## How to Build
The project is setup to build as an Eclipse project, but it can be built by any Java development tool.  In addition to the code provided, it requires Appche POI which can be found here:
https://poi.apache.org/
Apache POI require xmlbeans whcih can be found here:
http://xmlbeans.apache.org/sourceAndBinaries/

## How to call  
```
	public static void main(
	    String[] args)
	{
		BIMProjectParser parser = null;
		BasicMessageLogger msgCat = new BasicMessageLogger();
		long flags =  Parser.FLAG_CONVERT_GUID 				// Convert Base64 IFC GUIDS to Hex
					| Parser.FLAG_PROMOTE_COMPONENTS 		// Assignate components missing a space reference with a floor or the facility
					| Parser.FLAG_SKIP_ON_NULL 
					| Parser.FLAG_SKIP_ON_NO_VALUE 			
					| Parser.FLAG_CONVERT_EXTENSION_COLS 
					| Parser.FLAG_CONVERT_UNIQUE_IDs;		// Convert Revit UniqueID to export GUIDs
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

```

#### Accessing the parse tree
```	for( int i = 0; i < pageNames.length; i++ )
	{
		Page page = parser.getPage( pageNames[i] );
		if( page == null )
			continue;

		Iterator<Item> itr = page.iterator();

		while( itr.hasNext() )
		{
			Item item = itr.next();
			// Do something with the item
		}
	}
'''
