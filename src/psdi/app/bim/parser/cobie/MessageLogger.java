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

public interface MessageLogger
{
	void progressMsg( String msg );
	void progressMsg( String msg, String[] params  );
	void message( String msg );
	void message( String msg, String[] params );
	void warning( String msg );
	void warning( String msg, String[] params );
	void error(   String msg );
	void error(   String msg, String[] params );
	void exception( Throwable t );
	void exception( String pageName, String itemName, Throwable t );
	void exception( String pageName, String itemName, String fieldName, Throwable t );
	
	void dataIntegrityMessage( String msg );
	void dataIntegrityMessage( String msg, String[] params );

	int getErrorCount();
}
