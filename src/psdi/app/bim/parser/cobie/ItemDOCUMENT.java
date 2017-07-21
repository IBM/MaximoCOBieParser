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

import java.util.Iterator;

public class ItemDOCUMENT extends ItemBase {

	private String _createdOn = "";
	private String _category = "";
	private String _approvalBy = "";
	private String _stage = "";
	private String _sheetName = "";
	private String _rowName = "";
	private String _directory = "";
	private String _file = "";
	private String _reference = "";

	private ItemBase _item;		// Referenced item

	public String getKey() {
		return _sheetName + ":" + _rowName + ":" + getName();
	}

	public String getPageId()
	{
		return Parser.SHEET_DOCUMENT;
	}

	
	public void resolveRerefences(
		Parser project, 
		long flags 
	) {
		if(  getSheetName() == null || getSheetName().length() == 0 )
		{
			String[] params = { Parser.SHEET_DOCUMENT, getName() };
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_SHEET_REF, params );
			return;
		}

		Page page = project.getPage( _sheetName.toUpperCase() );
		if( page == null )
		{
			String params[] = { getPageId(), getName(), _sheetName, _rowName }; 
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
			return;
		}

		// Facility is a singleton so there may not be a valid row reference
		Item item;
		if( _sheetName.equalsIgnoreCase( Parser.SHEET_FACILITY ))
		{
			Iterator<Item> itr = page.iterator();
			while( itr.hasNext() )
			{
				item = itr.next();
				if( item.getKey() != null && item.getKey().trim().length() > 0 )
				{
					if( item instanceof ItemFACILITY )
					{
						// There should only be one facility and there is no so take the first
						// well formed instance and hope its right
						_item = (ItemBase)item;
						_item.addDocument( this );
						return;
					}
				}
			}
		}
		
	    if( getRowName()   == null || getRowName().length() == 0 )
		{
			String[] params = { Parser.SHEET_DOCUMENT, getName() };
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_MISSING_ROW_REF, params );
			return;
		}
			
		item = page.getItem( _rowName );
		if( item == null ) return;
		if( !( item instanceof ItemBase )) return;
		_item = (ItemBase)item;
		_item.addDocument( this );
	}
	
	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		if( super.skip( parser, flags ) ) return true;

		if( isItemFiltered( parser.filters(), getSheetName(), getRowName() ) )
		{
			return true;
		}

		if( ( flags & Parser.FLAG_SKIP_ON_NULL ) == 0 ) return false;
		if( ( flags & Parser.FLAG_SKIP_ON_NO_VALUE ) == 0 ) return false;
		if( _file.length() > 0 ) return false;
		if( _reference.length() > 0 ) return false;
		return true;
	}


	public String getCreatedOn() {
		return _createdOn;
	}

	public void setCreatedOn(String createdOn) {
		_createdOn = filterNA( createdOn );
	}

	public String getCategory() {
		return _category;
	}

	public void setCategory(String category) {
		_category = filterNA( category );
	}

	public String getApprovalBy() {
		return _approvalBy;
	}

	public void setApprovalBy(String approvalBy) {
		_approvalBy = filterNA( approvalBy );
	}

	public String getStage() {
		return _stage;
	}

	public void setStage(String stage) {
		_stage = filterNA( stage );
	}

	public String getSheetName() {
		return _sheetName;
	}

	public void setSheetName(String sheetName) {
		_sheetName = filterNA( sheetName );
	}

	public String getRowName() {
		return _rowName;
	}

	public void setRowName(String rowName) {
		_rowName = filterNA( rowName );
	}

	public String getDirectory() {
		return _directory;
	}

	public void setDirectory(String directory) {
		_directory = filterNA( directory );
	}

	public String getFile() {
		return _file;
	}

	public void setFile(String file) {
		_file = filterNA( file );
	}
	
	public String getQualifiedFileName()
	{
		if( _file.length() == 0 ) return "";
		String dir = "";
		if( _directory.length() > 0 )
		{
			dir = _directory.replace( '\\', '/' );
			return dir + '/' + _file;
		}
		return _file;
	}

	public String getReference() {
		return _reference;
	}

	public void setReference(String reference) {
		_reference = filterNA( reference );
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );
		sb.append("CreatedOn 	 = " + _createdOn + "\t\t");
		sb.append("ApprovalBy    = " + _approvalBy + "\t");
		sb.append("Category      = " + _category + "\t" );
		sb.append("Stage     	 = " + _stage + "\n");
		sb.append("Directory     = " + _directory + "\t\t");
		sb.append("File 		 = " + _file + "\t");
		sb.append("Reference 	 = " + _reference + "\n");

		return sb.toString();
	}
}