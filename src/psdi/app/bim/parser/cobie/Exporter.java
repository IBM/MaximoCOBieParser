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


public interface Exporter {
	
	/**
	 * COBie specification fields
	 * 
	 */
	public final static String	              YELLOW	          = "yellow";
	public final static String	              ORANGE	          = "orange";
	public final static String	              PURPLE	          = "purple";
	public final static String	              GREEN	              = "green";
	public final static String	              GREY	              = "grey";
    public final static String                STYLE_NAME          = "name";
    public final static String                COLOR               = "color";
    public final static String                FONT_NAME           = "font";
    public final static String                FONT_SIZE           = "fontSize";
    public final static String                TYPEFACE            = "typeFace";
    public final static String                HORIZONTAL_ALIGN     = "hAlign";
    public final static String                VERTICAL_ALIGN      = "vAlign";
    public final static String                ROTATION            = "rotation";
    public final static String                ALIGN_CENTER        = "center";
    public final static String                ALIGN_BOTTOM        = "bottom";
    public final static String                BOLD                = "bold";
    public final static String                ITALIC              = "italic";
    public static final String                NA   	              = "n/a";
    
    /**
     * Enumeration to represent export file types
     *
     */
    public enum ExportFormat {
        XLS, XLSX, CSV 
    }
	
	
	MessageLogger getLogger();
	
	void processPage(String pageName, String[] colNames);
	void addRow(String[] columnNames);
	String[] getColumnNames();

	void write(String fileName) throws IOException;
}