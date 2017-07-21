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

public class ItemISSUE extends ItemBase {
	private String Type = "";
	private String Risk = "";
	private String Chance = "";
	private String Impact = "";
	private String SheetName1 = "";
	private String RowName1 = "";
	private String SheetName2 = "";
	private String RowName2 = "";
	private String Owner = "";
	private String Mitigation = "";

	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void resolveRerefences(
		Parser project, 
		long flags 
	) {
		super.resolveRerefences( project, flags );
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getRisk() {
		return Risk;
	}

	public void setRisk(String risk) {
		Risk = risk;
	}

	public String getChance() {
		return Chance;
	}

	public void setChance(String chance) {
		Chance = chance;
	}

	public String getImpact() {
		return Impact;
	}

	public void setImpact(String impact) {
		Impact = impact;
	}
	
	public String getPageId()
	{
		return Parser.SHEET_ISSUE;
	}

	public String getSheetName1() {
		return SheetName1;
	}

	public void setSheetName1(String sheetName1) {
		SheetName1 = sheetName1;
	}

	public String getRowName1() {
		return RowName1;
	}

	public void setRowName1(String rowName1) {
		RowName1 = rowName1;
	}

	public String getSheetName2() {
		return SheetName2;
	}

	public void setSheetName2(String sheetName2) {
		SheetName2 = sheetName2;
	}

	public String getRowName2() {
		return RowName2;
	}

	public void setRowName2(String rowName2) {
		RowName2 = rowName2;
	}

	public String getOwner() {
		return Owner;
	}

	public void setOwner(String owner) {
		Owner = owner;
	}

	public String getMitigation() {
		return Mitigation;
	}

	public void setMitigation(String mitigation) {
		Mitigation = mitigation;
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );
		
		sb.append("Type     	 = " + Type + "\n");
		sb.append("Risk     	 = " + Risk + "\n");
		sb.append("Chance   	 = " + Chance + "\n");
		sb.append("Impact    	 = " + Impact + "\n");
		sb.append("SheetName1 	 = " + SheetName1 + "\n");
		sb.append("RowName1      = " + RowName1 + "\n");
		sb.append("SheetName2    = " + SheetName2 + "\n");
		sb.append("RowName2      = " + RowName2 + "\n");
		sb.append("Owner   	  	 = " + Owner + "\n");
		sb.append("Mitigation    = " + Mitigation + "\n");

		return sb.toString();
	}
}