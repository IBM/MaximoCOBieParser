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

import java.util.Enumeration;
import java.util.Hashtable;

public class ItemTYPE extends ItemBase {

	private String      _AssetType = "";
	private String      _DurationUnit = "";
	private String      _ExpectedLife = "";
	private String      _Manufacturer = "";
	private ItemCONTACT _ManufacturerRef;
	private String      _ModelNumber = "";
	private String      _ReplacementCost = "";
	private String      _WarrantyGuarantorParts = "";
	private ItemCONTACT _WarrantyGuarantorPartsRef;
	private String      _WarrantyDurationParts = "";
	private String      _WarrantyGuarantorLabor = "";
	private ItemCONTACT _WarrantyGuarantorLaborRef;
	private String      _WarrantyDurationLabor = "";
	private String      _WarrantyDurationUnit = "";
	private String      _WarrantyDescription = "";
	
	
	private String _nominalLength;
	private String _nominalWidth;
	private String _nominalHeight;
	private String _modelReference;
	private String _shape;
	private String _size;
	private String _color;
	private String _finish;
	private String _grade;
	private String _material;
	private String _constituents;
	private String _features;
	
	
	private String AccessibilityPerformance;
	private String CodePerformance;
	private String SustainabilityPerformance;
	
	private long   _flags = 0;

	private Hashtable<String, ItemCOMPONENT> _components = new Hashtable<String, ItemCOMPONENT>();
	private Hashtable<String, ItemSPARE> _spares = new Hashtable<String, ItemSPARE>();
	private Hashtable<String, ItemJOB> _jobs = new Hashtable<String, ItemJOB>();

	public void resolveRerefences(
	    Parser project,
	    long    flags
    ) {
		super.resolveRerefences( project, flags );
		
		if( project.getPage( Parser.SHEET_CONTACT ) != null )
		{
			boolean skip = false;
			if( valueHasContent( _Manufacturer ) )
			{
				if( (flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 )
				{
					if( _Manufacturer.equalsIgnoreCase( "Manufacturer" ) )
					{
						skip = true;
					}
				}
			}
			else
			{
				skip = true;
			}
			if( !skip )
			{
				_ManufacturerRef = (ItemCONTACT)project.getItem( Parser.SHEET_CONTACT, _Manufacturer );

				// Try matching against company
				if( _ManufacturerRef == null )
				{
					_ManufacturerRef = project.getCompanyFromContact( _Manufacturer );
				}
				if( _ManufacturerRef == null )
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_CONTACT, _Manufacturer };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
				else
				{
					_ManufacturerRef.makeTypeContact();
					_ManufacturerRef.setManufacturer( true );
				}
			}
			
			skip = false;
			if( valueHasContent( _WarrantyGuarantorParts ) )
			{
				if( (flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 )
				{
					if( _WarrantyGuarantorParts.equals( "WarrantyGuarantorParts" ) )
					{
						skip = true;
					}
				}
			}
			else
			{
				skip = true;
			}
			if( !skip )
			{
				_WarrantyGuarantorPartsRef = (ItemCONTACT)project.getItem( Parser.SHEET_CONTACT, _WarrantyGuarantorParts );
				// Try matching against company
				if( _WarrantyGuarantorPartsRef == null )
				{
					_WarrantyGuarantorPartsRef = project.getCompanyFromContact( _WarrantyGuarantorParts );
				}
				if( _WarrantyGuarantorPartsRef == null )
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_CONTACT, _WarrantyGuarantorParts };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
				else
				{
					_WarrantyGuarantorPartsRef.makeTypeContact();
				}
			}
			skip = false;
			if( valueHasContent( _WarrantyGuarantorLabor ) )
			{
				if( (flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 )
				{
					if( _WarrantyGuarantorLabor.equals( "WarrantyGuarantorLabor" ) )
					{
						skip = true;
					}
				}
			}
			else
			{
				skip = true;
			}
			if( !skip )
			{
				_WarrantyGuarantorLaborRef = (ItemCONTACT)project.getItem( Parser.SHEET_CONTACT, _WarrantyGuarantorLabor );
				// Try matching against company
				if( _WarrantyGuarantorLaborRef == null )
				{
					_WarrantyGuarantorLaborRef = project.getCompanyFromContact( _WarrantyGuarantorLabor );
				}
				if( _WarrantyGuarantorLaborRef == null )
				{
					String[] params = { getPageId(), getName(), Parser.SHEET_CONTACT, _WarrantyGuarantorLabor };
					project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
				}
				else
				{
					_WarrantyGuarantorLaborRef.makeTypeContact();
				}
			}
		}
	}

	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		_flags = flags;
		return super.skip( parser, flags );
	}
	
	public String getNominalLength()
    {
    	return filterNA( _nominalLength );
    }

	public void setNominalLength(
        String nominalLength
    ) {
		if( nominalLength.equalsIgnoreCase( "nominalLength" ))
		{
			_nominalLength = "";
		}
		else
		{
	    	_nominalLength = nominalLength;
		}
    }

	public String getNominalWidth()
    {
    	return filterNA( _nominalWidth );
    }

	public void setNominalWidth(
        String nominalWidth)
    {
		if( nominalWidth.equalsIgnoreCase( "nominalWidth" ))
		{
			_nominalWidth = "";
		}
		else
		{
	    	_nominalWidth = nominalWidth;
		}
    }

	public String getNominalHeight()
    {
    	return filterNA( _nominalHeight );
    }

	public void setNominalHeight(
        String nominalHeight
    ) {
		if( nominalHeight.equalsIgnoreCase( "nominalHeight" ))
		{
			_nominalHeight = "";
		}
		else
		{
	    	_nominalHeight = nominalHeight;
		}
    }

	public String getModelReference()
    {
    	return filterNA( _modelReference );
    }

	public void setModelReference(
        String modelReference)
    {
    	_modelReference = modelReference;
    }

	public String getShape()
    {
    	return filterNA( _shape );
    }

	public void setShape(
        String shape)
    {
    	_shape = shape;
    }

	public String getSize()
    {
    	return filterNA( _size );
    }

	public void setSize(
        String size)
    {
    	_size = size;
    }

	public String getColor()
    {
    	return filterNA( _color );
    }

	public void setColor(
        String color)
    {
    	_color = color;
    }

	public String getFinish()
    {
    	return filterNA( _finish );
    }

	public void setFinish(
        String finish)
    {
    	_finish = finish;
    }

	public String getGrade()
    {
    	return filterNA( _grade );
    }

	public void setGrade(
        String grade)
    {
    	_grade = grade;
    }
	
	public String getMaterial()
    {
    	return filterNA( _material );
    }

	public void setMaterial(
        String material)
    {
    	_material = material;
    }

	public String getConstituents()
    {
    	return filterNA( _constituents );
    }

	public void setConstituents(
        String constituents)
    {
    	_constituents = constituents;
    }

	public String getFeatures()
    {
    	return filterNA( _features );
    }

	public void setFeatures(
        String features)
    {
    	_features = features;
    }

	public String getPageId()
	{
		return Parser.SHEET_TYPE;
	}
	
	public void addComponent(ItemCOMPONENT component) 
	{
		_components.put(component.getKey(), component);
	}

	public Enumeration<ItemCOMPONENT> components() 
	{
		return _components.elements();
	}
	
	public void addSpare(ItemSPARE spare) 
	{
		_spares.put(spare.getKey(), spare);
	}

	public Enumeration<ItemSPARE> spares() 
	{
		return _spares.elements();
	}
	
	public void addJob(ItemJOB job) 
	{
		_jobs.put(job.getKey(), job);
	}

	public Enumeration<ItemJOB> jobs() 
	{
		return _jobs.elements();
	}

	public String getAccessibilityPerformance()
    {
    	return AccessibilityPerformance;
    }

	public void setAccessibilityPerformance(
        String accessibilityPerformance)
    {
    	AccessibilityPerformance = accessibilityPerformance;
    }

	public String getCodePerformance()
    {
    	return CodePerformance;
    }

	public void setCodePerformance(
        String codePerformance)
    {
    	CodePerformance = codePerformance;
    }

	public String getSustainabilityPerformance()
    {
    	return SustainabilityPerformance;
    }

	public void setSustainabilityPerformance(
        String sustainabilityPerformance)
    {
    	SustainabilityPerformance = sustainabilityPerformance;
    }

	public String getAssetType() 
	{
		return filterNA( _AssetType );
	}

	public void setAssetType(String assetType) {
		_AssetType = assetType;
	}

	public String getManufacturer() 
	{
		return filterNA( _Manufacturer );
	}

	public void setManufacturer(String manufacturer) 
	{
		_Manufacturer = filterNA( manufacturer );
	}
	
	public ItemCONTACT getManufacturerReference()
	{
		return _ManufacturerRef;
	}

	public String getModelNumber() 
	{
		return filterNA( _ModelNumber );
	}

	public void setModelNumber(String modelNumber) 
	{
		_ModelNumber = modelNumber;
	}

	public String getWarrantyGuarantorParts() 
	{
		return filterNA( _WarrantyGuarantorParts );
	}

	public ItemCONTACT getWarrantyGuarantorPartsRef() 
	{
		return _WarrantyGuarantorPartsRef;
	}

	public void setWarrantyGuarantorParts(String warrantyGuarantorParts) 
	{
		_WarrantyGuarantorParts = filterNA( warrantyGuarantorParts );
	}

	public String getWarrantyDurationParts() 
	{
		if(    ( _flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 
				&& _WarrantyDurationParts != null )
		{
			if( _WarrantyDurationParts.equalsIgnoreCase( "WARRANTYDURATIONPARTS" ))
			{
				return "";
			}
		}
		return  _WarrantyDurationParts;
	}

	public void setWarrantyDurationParts(String warrantyDurationParts) 
	{
		_WarrantyDurationParts = filterNA( warrantyDurationParts );
	}

	public String getWarrantyGuarantorLabor() 
	{
		return filterNA( _WarrantyGuarantorLabor );
	}

	public ItemCONTACT getWarrantyGuarantorLaborRef() 
	{
		return _WarrantyGuarantorLaborRef;
	}

	public void setWarrantyGuarantorLabor(String warrantyGuarantorLabor) 
	{
		_WarrantyGuarantorLabor = filterNA( warrantyGuarantorLabor );
	}

	public String getWarrantyDurationLabor() 
	{
		if(    ( _flags & Parser.FLAG_SKIP_ON_NO_VALUE) != 0 
				&& _WarrantyDurationLabor != null )
		{
			if( _WarrantyDurationLabor.equalsIgnoreCase( "WARRANTYDURATIONLABOR" ))
			{
				return "";
			}
		}
		return  _WarrantyDurationLabor;
	}

	public void setWarrantyDurationLabor(String warrantyDurationLabor) 
	{
		_WarrantyDurationLabor = filterNA( warrantyDurationLabor );
	}

	public String getWarrantyDurationUnit() 
	{
		return filterNA( _WarrantyDurationUnit );
	}

	public void setWarrantyDurationUnit(String warrantyDurationUnit) 
	{
		_WarrantyDurationUnit = filterNA( warrantyDurationUnit );
	}

	public String getReplacementCost() 
	{
		return filterNA( _ReplacementCost );
	}

	public void setReplacementCost(String replacementCost) 
	{
		_ReplacementCost = replacementCost;
	}

	public String getExpectedLife() 
	{
		return filterNA( _ExpectedLife );
	}

	public void setExpectedLife(String expectedLife) 
	{
		_ExpectedLife = expectedLife;
	}

	public String getDurationUnit() 
	{
		return filterNA( _DurationUnit );
	}

	public void setDurationUnit(String durationUnit) 
	{
		_DurationUnit = durationUnit;
	}

	public String getWarrantyDescription() 
	{
		return filterNA( _WarrantyDescription );
	}

	public void setWarrantyDescription(String warrantyDescription) 
	{
		_WarrantyDescription = warrantyDescription;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( super.toString() );
		
		sb.append("AssetType           	  = " + _AssetType + "\n");
		sb.append("Manufacturer           = " + _Manufacturer + "\t");
		if( _ManufacturerRef != null )
		{
			sb.append("Company = " + _ManufacturerRef.getCompany() + "\t");
		}
		sb.append("ModelNumber            = " + _ModelNumber + "\n");
		sb.append("\tWarrantyGuarantorParts = " + _WarrantyGuarantorParts + "\t");
		if( _WarrantyGuarantorPartsRef != null )
		{
			sb.append("Company = " + _WarrantyGuarantorPartsRef.getCompany() + "\t");
		}
		sb.append("WarrantyDurationParts  = " + _WarrantyDurationParts + "\n");
		sb.append("\tWarrantyGuarantorLabor = " + _WarrantyGuarantorLabor + "\t");
		if( _WarrantyGuarantorLaborRef != null )
		{
			sb.append("Company = " + _WarrantyGuarantorLaborRef.getCompany() + "\t");
		}
		sb.append("WarrantyDurationLabor  = " + _WarrantyDurationLabor + "\n");
		sb.append("\tWarrantyDurationUnit = " + _WarrantyDurationUnit + "\n");
		sb.append("\tReplacementCost      = " + _ReplacementCost + "\t");
		sb.append("ExpectedLife     	  = " + _ExpectedLife + "\t");
		sb.append("DurationUnit 		  = " + _DurationUnit + "\t");
		sb.append("WarrantyDescription    = " + _WarrantyDescription + "\n");
		
		sb.append("\tNominalLength = " + _nominalLength + "\t");
		sb.append("NominalWidth = "    + _nominalWidth + "\t");
		sb.append("NominalHeight = "   + _nominalHeight + "\t");
		sb.append("ModelReference = "  + _modelReference + "\n");
		sb.append("\tShape = "         + _shape + "\t");
		sb.append("Size  = "           + _size + "\t");
		sb.append("Color  = "          + _color + "\t");;
		sb.append("Finish = "          + _finish + "\t");
		sb.append("\tGrade = "         + _grade + "\n");
		sb.append("\tMaterial = "        + _material + "\t");
		sb.append("Constituents = "    + _constituents + "\t");
		sb.append("Features = "        + _features + "\n");
		
		
		sb.append("\tComponents: ");
		Enumeration<ItemCOMPONENT> componentEnum = _components.elements();
		while (componentEnum.hasMoreElements()) {
			sb.append(componentEnum.nextElement().getName());
			sb.append(", ");
		}
		sb.append("\n");
		
		sb.append("\tSpares: ");
		Enumeration<ItemSPARE> spareEnum = _spares.elements();
		while (spareEnum.hasMoreElements()) {
			sb.append(spareEnum.nextElement());
		}
		sb.append("\n");
		
		sb.append("\tJobs: ");
		Enumeration<ItemJOB> jobEnum = _jobs.elements();
		while (jobEnum.hasMoreElements()) {
			sb.append(jobEnum.nextElement().getName());
			sb.append(", ");
		}
		sb.append("\n");
		
		String attribs = attributeListing();
		if( attribs != null && attribs.length() > 0 )
		{
			sb.append("\n");
			sb.append( attribs );
		}
		
		String docs = documentListing();
		if( docs != null && docs.length() > 0 )
		{
			sb.append("\n");
			sb.append( docs );
		}
		sb.append("\n");
		
		return sb.toString();
	}
}