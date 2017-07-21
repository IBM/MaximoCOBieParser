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

import java.util.Hashtable;

public class  BasicMessageLogger
    implements MessageLogger
{
	
	private static MessageDef[] messageList = {
	    new MessageDef( Parser.MSG_FILE_CREATED,
	    	"Read error on COBie format file Error: {0}" ),
		new MessageDef( Parser.MSG_FILE_PARSED,
       		"File {0} loaded" ),
		new MessageDef( Parser.MSG_FILE_NOT_FOUND_ERROR,
		    "File {0} not found" ),
		new MessageDef( Parser.MSG_LOADING_STYLES,
		    "Loading COBie styles version {0}"),
		new MessageDef( Parser.MSG_VERSION,
		    "Processing COBie Specification {0}" ), 

		new MessageDef( Parser.ERR_BAD_URL,
      	    "Cannot connect to URL {0} Error {1}" ),
       	new MessageDef( Parser.ERR_EXCEL_NO_URL,
       	    "Import of Excel files from a URL is not supported: {0}" ),
   	    new MessageDef( Parser.ERR_EXPORT_ERROR,
		    "Problem creating export documents Error: {0}" ),
   		new MessageDef( Parser.ERR_EXPORT_FILE_NAME,
			"Missing export file name"),
		new MessageDef( Parser.ERR_FILE_OPEN,
           	"Error opening file {0} for sheet {1}.  Error: {2}"),  
		new MessageDef( Parser.ERR_FILE_READ_ERROR,
			"Read error on file {0} Error: {1}"),
		new MessageDef( Parser.ERR_INVALID_ACCESSOR,
			"Error getting accessor for {0}.{1}. This column will be skipped.  This may be caused by the .csv template files being our of sync with the code"),
		new MessageDef( Parser.ERR_INVALID_OMNICLASS_STUCT,
		    "Invlaid Omniclass file structure at {0}" ),
	    new MessageDef( Parser.ERR_INVALID_XLS_FILE_ERROR,
	        "Cannot open .xls file {0}, Error: {1}"),
   		new MessageDef( Parser.WRN_MISSING_PROPERTY,
		   	"Property {0} is not supported for sheet {1}"),
		new MessageDef( Parser.ERR_SHEET_CLASS_NOT_FOUND,
			"Implemention class not found for sheet:"),
		new MessageDef(Parser.ERR_SHEET_INVALID_CLASS,
			"Implemention class must implement Sheet"),
       	new MessageDef( Parser.ERR_UNSUPPORTED_FILE_TYPE,
           	"File {0} with extension {1} is not s supported type for sheet type {2}"),
           	
		new MessageDef( Parser.WRN_INVALID_CELL_VALUE,
				"Invalid cell value for sheet {0}, row {1}, column {2}, cell {3}" ),
       	new MessageDef( Parser.WRN_MISSING_COL_NAME,
        	"COBie Export missing column names for page {0}.  The page will not be exported"),
       	new MessageDef( Parser.WRN_OPEN_STYLE_TEMPLATE,
            	"Error opeing the style sheet for the COBied export file.  No visual styles will be applied.  The error is {0}"),
           	
		new MessageDef( Parser.VALIDATE_ATTRIB_VALUE_IS_NAME,
		   	"Atrribute {0}:{1}:{2} skipped because its value is the same as its name" ),
	    new MessageDef( Parser.VALIDATE_DUPLICATE_ITEM, 
	        "{0}:{1} is a duplicate" ),
		new MessageDef( Parser.VALIDATE_MISSING_REF,
           	"{0}:{1} is missing a reference to {2}"),
		new MessageDef( Parser.VALIDATE_MISSING_ROW_REF,
           	"{0}:{1} is missing its row reference"),
   		new MessageDef( Parser.VALIDATE_MISSING_SHEET_REF,
   			"{0}:{1} is missing its sheet reference"),
		new MessageDef( Parser.VALIDATE_SPACE_LIST,
			"Component {0} references multiple spaces"),
		new MessageDef( Parser.VALIDATE_UNRESOLVED_REF,
        	"The reference in {0}:{1} to {2}:{3} cannot be resolved"),
		new MessageDef( Parser.VALIDATE_VALUELIST_MISMATCH,
			"Occurances of attibute {0} have different value lists"),
		new MessageDef( Parser.VALIDATE_TASK_PREREQ_MISSING,
		    "JOB:{0} Task{1} prerequisite {2} unresolved"),
		new MessageDef( Parser.VALIDATE_TASK_NUMBER,
		    "JOB:{0} Task{1} has a non numeric task number {2}" ),
	    new MessageDef( Parser.VALIDATE_MISSING_CHILD_REF,
	   		"{0}:{1} is missing its child reference" ),
	    new MessageDef( Parser.VALIDATE_MISSING_PARENT_REF,
	   		"{0}:{1} is missing its parent reference" )

	}; 
	
	private final Hashtable<String, String> messages = new Hashtable<String, String>();
	private int _errorCount = 0;

	
	public BasicMessageLogger(	) 
	{
		for (int i = 0; messageList != null && i < messageList.length; i++) {
			MessageDef msg = messageList[i];
			messages.put(msg._key, msg._message);
		}
	}

	@Override
    public void progressMsg(
	    String msg
    ) {
		msg = get( msg );
		System.out.println( msg );
	}

	@Override
    public void progressMsg(
	    String msg,
	    String[] params
	) {
		msg = get( msg, params );
		System.out.println( msg );
	}

	@Override
    public void message(
	    String msg
    ) {
		msg = get( msg );
		System.out.println( msg );
	}

	@Override
    public void message(
	    String msg,
	    String[] params
	) {
		msg = get( msg, params );
		System.out.println( msg );
	}

	@Override
    public void warning(
	    String msg
    ) {
		msg = get( msg );
		System.out.println( msg );
	}

	@Override
    public void warning(
	    String msg,
	    String[] params
    ) {
		msg = get( msg, params );
		System.out.println( msg );
	}

	@Override
    public void error(
	    String msg
    ) {
		msg = get( msg );
		System.err.println( msg );
		_errorCount++;
	}

	@Override
    public void error(
	    String msg,
	    String[] params
    ) {
		msg = get( msg, params );
		System.err.println( msg );
		_errorCount++;
	}

	@Override
    public void exception(
        Throwable t
    ) {
		System.err.println( t.getLocalizedMessage() );
		_errorCount++;
    }

	@Override
    public void exception(
        String pageName,
        String itemName,
        Throwable t
    ) {
		System.err.println( pageName + " " + itemName + "  " + t.getLocalizedMessage() );
		_errorCount++;
    }

	@Override
    public void exception(
        String pageName,
        String itemName,
        String fieldName,
        Throwable t
    ) {
		System.err.println( pageName + " " + itemName + "  " + fieldName + " " + t.getLocalizedMessage() );
		_errorCount++;
    }

	@Override
    public void dataIntegrityMessage(
	    String msg 
    ) {
		msg = get( msg );
		System.err.println( msg );
	}
	
	@Override
    public void dataIntegrityMessage(
	    String msg,
	    String[] params 
	) {
		msg = get( msg, params );
		System.err.println( msg );
	}

	public String get(String key) {
		String msg = messages.get(key);
		if (msg == null)
			return "";
		return msg;
	}

	public String get(
	    String key,
	    String params[] 
    ) {
		String msg = messages.get(key);
		if (msg == null)
			return key;

		for( int i = 0; params != null && i < params.length; i ++ )
		{
			String marker = "{" + i + "}";
			int idx = msg.indexOf(  marker );
			if( idx >= 0 )
			{
				String start = msg.substring( 0, idx );
				String end   = msg.substring( idx + marker.length() );
				msg = start + params[i] + end;
			}
		}
		return msg;
	}

	private static class MessageDef {
		MessageDef(String key, String message) {
			_key = key;
			_message = message;
		}

		String _key;
		String _message;
	}

	@Override
    public int getErrorCount()
	{
		return _errorCount;
	}
}