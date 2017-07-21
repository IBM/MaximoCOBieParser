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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;


public interface Parser {
	final static String SHEET_ASSEMBLY    = "ASSEMBLY";
	final static String SHEET_ATTRIBUTE   = "ATTRIBUTE";
	final static String SHEET_COMPONENT   = "COMPONENT";
	final static String SHEET_COMPANY     = "COMPANY"; 
	final static String SHEET_CONTACT     = "CONTACT";
	final static String SHEET_DOCUMENT    = "DOCUMENT";
	final static String SHEET_FACILITY    = "FACILITY";
	final static String SHEET_FLOOR       = "FLOOR";
	final static String SHEET_ISSUE       = "ISSUE";
	final static String SHEET_JOB         = "JOB";
	final static String SHEET_RESOURCE    = "RESOURCE";
	final static String SHEET_SPACE       = "SPACE";
	final static String SHEET_SPARE       = "SPARE";
	final static String SHEET_SYSTEM      = "SYSTEM";
	final static String SHEET_TYPE        = "TYPE";
	final static String SHEET_VALUE_LIST  = "VALUE_LIST";
	final static String SHEET_ZONE        = "ZONE";
	final static String SHEET_ATTRIB_TYPE = "ATTRIBUTE TYPE";
	final static String SHEET_EXCEL       = "EXCEL";
	
	public static final String	ITEM_CLASS_PREFIX	     = "psdi.app.bim.parser.cobie.Item";
	
	/**
	 * Determines if the parser attempts to convert the value found in ExtIdentifier
	 * from a base 64 number to a GUID format
	 * <p>
	 * http://thebuildingcoder.typepad.com/blog/2009/02/uniqueid-dwf-and-ifc-guid.html
	 * 
	 * @param convert
	 */
	public static final long FLAG_CONVERT_GUID       = 0x0001;
	
	/**
	 * The COBie spec assumes that ever space will reference a level.  This is not
	 * always the case with really data and may not be desirable for some service spaces 
	 * that cross levels.  If this flag is set, a space that is not associated with a 
	 * level is associated with the facility.  If it is not set, the space is skipped
	 * on import 
	 */
	public static final long FLAG_PROMOTE_SPACES     = 0x0002;

	/**
	 * The COBie spec associates components with spaces, but the modeling tools only
	 * require a model item to be associated with a level.  If this flag is set
	 * If the space reference is invalid, the parser attempts to find a floor with
	 * the space name (This is an extension to the spec).  If that fails, it associates
	 * the component with the facility.  This insures that all components can be imported
	 */
	public static final long FLAG_PROMOTE_COMPONENTS     = 0x0004;

	/**
	 * IF this flag is set, the parser looks for the level attribute and uses its value
	 * to set the floor reference in components if it is unset.  IF the space reference
	 * is not valid, then the loader uses the floor reference to associate the component
	 * with a floor instead of a space.
	 */
	public static final long FLAG_INFER_LEVELS           = 0x0008;

	/**
	 * The System Name attribute may contain system definition
	 * that are not reflected in the COBie data,  If this flag is 
	 * set the parse processes the SystemName attribute and tries to 
	 * build systems
	 */
	public static final long FLAG_INFER_SYSTEMS          = 0x0010;


	/**
	 * Objects that have a null value are skipped.  Each object can implement 
	 * its own logic to determine if it is null 
	 */
	public static final long FLAG_SKIP_ON_NULL           = 0x0020;

	/**
	 * Objects that don't have a meaningful value are skipped on load.  Each
	 * object type can implement it's own logic to determine when an instance
	 * should be skipped 
	 */
	public static final long FLAG_SKIP_ON_NO_VALUE       = 0x0040;
	
	/**
	 * Convert unrecognized columns which are usually COBie extension columns
	 * to attributes 
	 */
	public static final long FLAG_CONVERT_EXTENSION_COLS = 0x0080;
	
	/**
	 * Identify and convert Revit UniqueIds to Revit export GUIDs 
	 */
	public static final long FLAG_CONVERT_UNIQUE_IDs     = 0x0100;

	/**
	 * Causes the parser to generate Companies for all contacts regardless
	 * of how they are referenced
	 */
	public static final long FLAG_ALL_CONTACTS_ARE_COMPANIES = 0x0101;

	// Cannot connect to URL {0} Error {1}
	public static final String ERR_BAD_URL                    = "bad-url";
	// Import of Excel files from a URL is not supported: {0}
	public static final String ERR_EXCEL_NO_URL               = "excel-no-url";
	// Problem creating export documents Error: {0}
	public static final String ERR_EXPORT_ERROR            = "export_error";
	// Filename for export file is missing
	public static final String ERR_EXPORT_FILE_NAME           = "missing-export-file-name";
	// Error opening file {0} for sheet {1}.  Error: {2}
	public static final String ERR_FILE_OPEN                  = "file-open"; 
	// Read error on file {0} Error: {1}
	public static final String ERR_FILE_READ_ERROR            = "file_read_error";
	// Error getting accessor for {0}.{1}. This column will be skipped.  This may be caused by the .csv template files being our of sync with the code
	public static final String ERR_INVALID_ACCESSOR           = "invalid-accessor";
	// Invalid Omniclass file structure at {0} 
	public static final String ERR_INVALID_OMNICLASS_STUCT    = "invlaid-omniclass-file-struct";
	// Cannot open .xls file {0}, Error: {1}
	public static final String ERR_INVALID_XLS_FILE_ERROR     = "invalid_xls_file";
	// Implementation class not found for sheet: [0]
	public static final String ERR_SHEET_CLASS_NOT_FOUND      = "class-not-found";
	// Implementation class must implement Sheet
	public static final String ERR_SHEET_INVALID_CLASS        = "invalid-item-class";
	// File {0} with extension {1} is not s	supported type for sheet type {2} 
	public static final String ERR_UNSUPPORTED_FILE_TYPE      = "unuspported_file_type";

	// Error opening the style sheet for the COBied export file.  No visual styles will be applied.  The error is {0}
	public static final String WRN_OPEN_STYLE_TEMPLATE        = "open-style-template"; 
	// COBie Export missing column names for page {0}.  The page will not be exported
	public static final String WRN_MISSING_COL_NAME           = "missing-col-name";
	// Property {0} is not supported for sheet {1}
	public static final String WRN_MISSING_PROPERTY           = "missing-property";
	// Invalid cell value for sheet {0}, row {1}, column {2}, cell {3}
	public static final String WRN_INVALID_CELL_VALUE           = "invalid-cell-value";

    // Export file {0} created 
	public static final String MSG_FILE_CREATED            = "export_file_created";
	// File {0] loaded 
	public final static String MSG_FILE_PARSED                = "file-parsed";
	// File {0} not found 
	public static final String MSG_FILE_NOT_FOUND_ERROR       = "file_not_found";
	// Loading COBie styles version {0}
	public static final String MSG_LOADING_STYLES             = "loading-cobie-styles";
	// Processing COBie Specification {0}
    public static final String MSG_VERSION                 = "export_version";
	
	// Attribute {0}:{1}:{2} skipped because its value is the same as its name
	public final static String VALIDATE_ATTRIB_VALUE_IS_NAME   = "vdt_value_is_name";
	// {0}:{1} is a duplicate
	public final static String VALIDATE_DUPLICATE_ITEM         = "vdt_dup";
	// {0}:{1} is missing a reference to {2}
	public final static String VALIDATE_MISSING_REF            = "vdt_missing_ref";
	// {0}:{1} is missing its row reference
	public final static String VALIDATE_MISSING_ROW_REF        = "vdt_missing_row_ref";
	// {0}:{1} is missing its sheet reference
	public final static String VALIDATE_MISSING_SHEET_REF      = "vdt_missing_sheet_ref";
	// Component {0} references multiple spaces
	public final static String VALIDATE_SPACE_LIST             = "vdt_space_list";
	// The reference in {0}:{1} to {2}:{3} cannot be resolved
	public final static String VALIDATE_UNRESOLVED_REF         = "vdt_unresolved_ref";
	// Occurrences of attribute {0} have different value lists
	public final static String VALIDATE_VALUELIST_MISMATCH     = "vdt_value_list_mismatch";
	// JOB:{0} Task{1} prerequisite {2} unresolved
	public final static String VALIDATE_TASK_PREREQ_MISSING    = "vdt_task_prereq_missing";
	// JOB:{0} Task{1} has a non numeric task number {2}
	public final static String VALIDATE_TASK_NUMBER            = "vdt_task_number";
	// {0}:{1} is missing its child reference
	public final static String VALIDATE_MISSING_CHILD_REF      = "vdt_missing_child_ref";
	// {0}:{1} is missing its parent reference
	public final static String VALIDATE_MISSING_PARENT_REF     = "vdt_missing_parent_ref";

	/**
	 * The parser tracks all spaces that are skipped so that things that reference a skipped space can also be skipped
	 * @param spaceName Name of the skipped pace;
	 */
	void addSkippedSpace( String spaceName );
	
	/**
	 * Parsers should implement this method to support export functionality
	 * @param fileName  Name of export file
	 * @param pageList  List of pages to export
	 * @param template	JSOM configuration object describing excel formating
	 * @param tracker   Progress tracker
	 * @param fileFormat Export file format xls or xlsx
	 */
	void export( String                fileName, 
		         String                pageList[], 
		         InputStream           template, 	    
		         ExportProgressTracker tracker,
                 Exporter.ExportFormat fileFormat ); 

	/**
	 * List of filters to restrict value on input.
	 * @return
	 */
	Iterator<Filter> filters();
	
	MessageLogger getLogger();
	
	ItemCONTACT getCompanyFromContact( String companyName );
	
	/**
	 * Fields in this list are converted to COBie attributes instead of treated
	 * as standard parts of the COBie table.  Use of this mechanism should 
	 * Normally be restricted to the green optional columns
	 * @param tableName
	 * @param fieldName
	 * @param attributeName COBie attribute name to which the field is converted
	 */
	public void addConvertedField(
		String tableName,
		String fieldName,
		String attributeName );

	public String getConvertedField(
		String tableName,
		String fieldName );
	
	/**
	 * An attribute name can be specified for components that has company reference for
	 * the supplier/vendor of the component.  The reference is to the company name not
	 * the contact email
	 * @return
	 */
	public String getVendorAttribute();
	
	/**
	 * Retrieves an item from the parse tree
	 * @param pageName
	 * @param key
	 * @return
	 */
	Item getItem(String pageName, String key);
	
	/**
	 * This is set by  resolveReferences, and is used to show a % complete
	 * @return The total number of items loaded by the parser
	 */
	int getItemCount();
	
	Locale getLocale();

	Page getPage(String pageName);
	
	/**
	 * Test if spaceName is in the skipped spaces list
	 * @param spaceName
	 * @return
	 */
	boolean isSpaceSkipped( String spaceName );
	
	/**
	 * Provides a centralized place to control if the parser is case sensitive.
	 * Only partially implemented.
	 * @param s
	 * @return
	 */
	public String convertCase( String s );

	
}