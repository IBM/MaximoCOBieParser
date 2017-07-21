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


public interface Item
{
	/**
	 * Called if an item with an identical name is found. 
	 * @param item
	 * @return true to add to the parse tree false to ignore
	 */
	public boolean isDuplicat( Parser parser, Item item );
	
	public String getDescription();
	
	public String getKey();
	
	public String getName(); 
	
	/**
	 * @return The COBie tab name for the object
	 */
	public String getPageId();
	
	/**
	 * It is desirable to combine multiple COBie work books into a single import
	 * such as a s work book from the architectural model and one from the mechanical
	 * model.  Items with the same name but different source files are assumed to 
	 * be the same item and are merged
	 * @return
	 */
	public String getSourceFile();
	public void setSourceFile( String sourceFile );

	/**
	 * Determines if the parser attempts to convert base 64 ids to GUIDs
	 * @param convert
	 */
	public void setGuidConversionFlag( boolean convert );
	
	/**
	 * Revid uses a UniqueID for its internal object identifications. This is a GUID plus the 8 digit Hex
	 * Revit element ID.  Revit uses a export GUID for most identity in most external representations.
	 * However the COBie toolkit exports the UniqueId.  The UniqueId can be converted to the export GUID by
	 * Xoring the element id portion with the last 8 characters of the GUID.
	 * <p>
	 * If this flag is set each external identifier is tested to determine if it is the UniqueID format
	 * and if it is, it is converted to the and export GUID. 
	 * @param convert
	 */
    public void setUniqueIdConversionFlag( boolean convert );
	
	/**
	 * Called after the item is loaded but before it is added to a page.  
	 * @return true causes the item to be discarded
	 */
	public boolean skip( Parser parser, long flags );
	
	public String getUniqueId();
	public void setUniqueId(String uniqueIdentifier);

	/**
	 * Attempts to establish all the links specified in the various COBie sheet
	 * @param project
	 * @param flags
	 */
	public void resolveRerefences( Parser project, long flags );
	
	/**
	 * Merge two instances of an object
	 * <p> Fields that are blank in the target object are updates with values for
	 * fields in the source object 
	 * @param item	The source object
	 */
	public void update( Item item );
	
	/**
	 * Used by the loader to indicate a match to an existing record in the target system
	 */
	boolean isMatch();
	void setMatch( boolean match );

	/**
	 * Exported representation of Item
	 * @param exporter tool used to export item
	 */
	public void export(Exporter exporter);
}