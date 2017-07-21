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

// Not Implemented
public class   CSVExport
    implements Exporter
{

	private MessageLogger _logger = null;
	
	public CSVExport(
        MessageLogger logger,
        InputStream templateStream)
    {
	    _logger = logger;
    }


	public MessageLogger getLogger()
	{
		return _logger;
	}

	public void addRow(
	    String[] columnNames
    ) {
		// TODO Auto-generated method stub
	}


	public void processPage(
        String pageName,
        String[] colNames
    ) {
	    // TODO Auto-generated method stub
    }


	public String[] getColumnNames()
    {
	    // TODO Auto-generated method stub
	    return null;
    }
	
	public void write(
        String fileName
    ) 
		throws IOException
    {
	    // TODO Auto-generated method stub
    }
}