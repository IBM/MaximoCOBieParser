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

public class ParseException extends Exception {

	private static final long serialVersionUID = 8755551576569957734L;
	
	private String _key = "";
	private String _params[] = {};

	public ParseException() {
	}

	public ParseException( String key, String params[] ) 
	{
		super();
		_key    = key;
		_params = params;
	}

	public ParseException( 
		String    key, 
		String    params[],
		Throwable cause
	) {
		super( cause );
		_key    = key;
		_params = params;
	}

	public ParseException(
		String key
	) {
		super();
		_key    = key;
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
	
	public String getKey() { return _key; }
	public String[] getParams() { return _params; }
}