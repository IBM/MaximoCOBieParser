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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class ItemJOB
    extends
        ItemBase
{

	public static final String	            COBIE_JOB_TYPE_ADJUSTMENT	= "Adjustment";
	public static final String	            COBIE_JOB_TYPE_CALIBRATION	= "Calibration";
	public static final String	            COBIE_JOB_TYPE_EMERGENCY	= "Emergency";
	public static final String	            COBIE_JOB_TYPE_INSPECTION	= "Inspection";
	public static final String	            COBIE_JOB_TYPE_OPERATION	= "Operation";
	public static final String	            COBIE_JOB_TYPE_PM	       = "PM";
	public static final String	            COBIE_JOB_TYPE_SAFTY	   = "Safety";
	public static final String	            COBIE_JOB_TYPE_SHUTDOWN	   = "ShutDown";
	public static final String	            COBIE_JOB_TYPE_STARTUP	   = "StartUp";
	public static final String	            COBIE_JOB_TYPE_TESTING	   = "Testing";
	public static final String	            COBIE_JOB_TYPE_TROUBLE	   = "Trouble";

	public static final int	                JOB_TYPE_UNKNOWN	       = 0;
	public static final int	                JOB_TYPE_ADJUSTMENT	       = 1;
	public static final int	                JOB_TYPE_CALIBRATION	   = 2;
	public static final int	                JOB_TYPE_EMERGENCY	       = 3;
	public static final int	                JOB_TYPE_INSPECTION	       = 4;
	public static final int	                JOB_TYPE_OPERATION	       = 5;
	public static final int	                JOB_TYPE_PM	               = 6;
	public static final int	                JOB_TYPE_SAFTY	           = 7;
	public static final int	                JOB_TYPE_SHUTDOWN	       = 8;
	public static final int	                JOB_TYPE_STARTUP	       = 9;
	public static final int	                JOB_TYPE_TESTING	       = 10;
	public static final int	                JOB_TYPE_TROUBLE	       = 11;

	public static final int	                JOB_DURATION_UNKNOWN	   = 0;
	public static final int	                JOB_DURATION_SECOND	       = 1;
	public static final int	                JOB_DURATION_MINUTE	       = 2;
	public static final int	                JOB_DURATION_HOUR	       = 3;
	public static final int	                JOB_DURATION_DAY	       = 4;

	private String	                        _status	                   = "";
	private String	                        _typeName	               = "";
	private String	                        _duration	               = "";
	private String	                        _durationUnit	           = "";
	private String	                        _start	                   = "";
	private String	                        _taskStartUnit	           = "";
	private String	                        _frequency	               = "";
	private String	                        _frequencyUnit	           = "";
	private String	                        _taskNumber	               = "";
	private String	                        _priors	                   = "";

	private HashSet<String>                 _resourceRefs              = new HashSet<String>();
	private final Hashtable<String, ItemRESOURCE>	_resources	               = new Hashtable<String, ItemRESOURCE>();
	private ItemTYPE[]	                    _typeReferences	           = null;
	private final LinkedList<JobTask>	            _tasks	                   = new LinkedList<JobTask>();

	@Override
	public boolean isDuplicat(
	    Parser parser,
	    Item item
    ) {
		if( !getSourceFile().equals(item.getSourceFile()) )
		{
			return true;
		}
		if( item instanceof ItemJOB )
		{
			ItemJOB dup = (ItemJOB) item;
			JobTask task = new JobTask(parser, dup, this );
			addTask(task);
			return false;
		}
		return true;
	}

	@Override
	public void resolveRerefences(
	    Parser project,
	    long   flags
    ) {
		super.resolveRerefences(project, flags);

		if( _typeName == null || _typeName.length() == 0 )
		{
			String[] params = { getPageId(), getName(), Parser.SHEET_TYPE };
			project.getLogger().dataIntegrityMessage(Parser.VALIDATE_MISSING_REF, params);
		}
		else
		{
			Page typePage = project.getPage(Parser.SHEET_TYPE);
			if( typePage != null )
			{

				StringTokenizer strToken = new StringTokenizer(_typeName, ",");
				ArrayList<ItemTYPE> typeList = new ArrayList<ItemTYPE>();

				while( strToken.hasMoreElements() )
				{
					String name = strToken.nextToken().trim();

					Item item = typePage.getItem(name);
					if( item != null )
					{
						ItemTYPE type = (ItemTYPE) item;
						type.addJob(this);
						typeList.add(type);
					}
					else
					{
						String[] params = { getPageId(), getName(), Parser.SHEET_TYPE, _typeName };
						project.getLogger().dataIntegrityMessage(Parser.VALIDATE_UNRESOLVED_REF, params);
					}
				}
				Object array[] = typeList.toArray();

				_typeReferences = new ItemTYPE[array.length];
				for( int i = 0; i < array.length; i++ )
				{
					_typeReferences[i] = (ItemTYPE) array[i];
				}
			}

		}

		Iterator<JobTask> itr = _tasks.iterator();
		while( itr.hasNext() )
		{
			JobTask task = itr.next();
			String preReqs[] = task.getPreReqs();
			for( int i = 0; i < preReqs.length; i++ )
			{
				if( preReqs[i] == null || preReqs[i].length() == 0 )
				{
					continue;
				}
				if( findTaskByName(preReqs[i]) == null )
				{
					String[] params = { getName(), task.getTaskName(), preReqs[i] };
					project.getLogger().dataIntegrityMessage(Parser.VALIDATE_TASK_PREREQ_MISSING, params);
				}
			}
		}
		
		Iterator<String> itrRes = _resourceRefs.iterator();
		while( itrRes.hasNext() )
		{
			String name = itrRes.next().trim();
			ItemRESOURCE newResource = (ItemRESOURCE) project.getItem( Parser.SHEET_RESOURCE, name);
			if (newResource != null)
			{
				_resources.put( name, newResource );
				continue;
			}
			if( name.charAt( name.length() -1 ) == '.' )
			{
				name = name.substring( 0, name.length()- 1 );
				newResource = (ItemRESOURCE) project.getItem( Parser.SHEET_RESOURCE, name);
				if (newResource != null)
				{
					_resources.put( name, newResource );
					continue;
				}
			}
			String[] params = { getPageId(), getName(), Parser.SHEET_RESOURCE, name };
			project.getLogger().dataIntegrityMessage( Parser.VALIDATE_UNRESOLVED_REF, params );
		}
	}

	public void addTask(
	    JobTask task)
	{
		int num = task.getTaskNumber();
		if( num >= 0 )
		{
			for( int i = 0; i < _tasks.size(); i++ )
			{
				JobTask t = _tasks.get( i );
				if( num >= t.getTaskNumber() )
				{
					continue;
				}
				_tasks.add( i + 1, task);
				return;
			}
		}
		_tasks.add(task);
	}

	@Override
	public String getPageId()
	{
		return Parser.SHEET_JOB;
	}

	public JobTask findTaskByName(
	    String name)
	{
		Iterator<JobTask> itr = _tasks.iterator();
		while( itr.hasNext() )
		{
			JobTask task = itr.next();
			if( task.getTaskName().equalsIgnoreCase(name) )
			{
				return task;
			}
		}
		return null;
	}

	public int getJobType()
	{
		String category = getCategory();
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_ADJUSTMENT) )
			return JOB_TYPE_ADJUSTMENT;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_CALIBRATION) )
			return JOB_TYPE_CALIBRATION;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_EMERGENCY) )
			return JOB_TYPE_EMERGENCY;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_INSPECTION) )
			return JOB_TYPE_INSPECTION;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_OPERATION) )
			return JOB_TYPE_OPERATION;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_PM) )
			return JOB_TYPE_PM;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_SAFTY) )
			return JOB_TYPE_SAFTY;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_SHUTDOWN) )
			return JOB_TYPE_SHUTDOWN;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_STARTUP) )
			return JOB_TYPE_STARTUP;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_TESTING) )
			return JOB_TYPE_TESTING;
		if( category.equalsIgnoreCase(COBIE_JOB_TYPE_TROUBLE) )
			return JOB_TYPE_TROUBLE;

		return JOB_TYPE_UNKNOWN;
	}

	public void addResource(
	    ItemRESOURCE resource)
	{
		_resources.put(resource.getKey(), resource);
	}

	public Enumeration<ItemRESOURCE> resources()
	{
		return _resources.elements();
	}

	public String getStatus()
	{
		return _status;
	}

	public void setStatus(
	    String status)
	{
		_status = filterNA( status );
	}

	public String getTypeName()
	{
		return _typeName;
	}

	public void setTypeName(
	    String typeName
    ) {
		// Remove surrounding quotes is present.
		typeName = filterNA( typeName );
		if( typeName.charAt(0) == '"' && typeName.charAt(typeName.length() - 1) == '"' )
			_typeName = typeName.substring(1, typeName.length() - 1);
		else
			_typeName = typeName;
	}

	public ItemTYPE[] getTypeReference()
	{
		return _typeReferences;
	}
	
	public void setTypeReference(
		ItemTYPE[] typeReferences
	) {
		_typeReferences = typeReferences;
	}

	public String getDuration()
	{
		return _duration;
	}

	public void setDuration(
	    String duration)
	{
		_duration = filterNA( duration );
	}

	public String getDurationUnit()
	{
		return _durationUnit;
	}

	public int getDurationUnitType()
	{
		return ItemJOB.durationStringToUnits(_durationUnit);
	}

	public void setDurationUnit(
	    String durationUnit
    ) {
		_durationUnit = durationUnit;
	}

	public String getStart()
	{
		return _start;
	}

	public void setStart(
	    String start
    ) {
		_start = filterNA( start );
	}

	public String getTaskStartUnit()
	{
		return _taskStartUnit;
	}

	public void setTaskStartUnit(
	    String taskStartUnit
    ) {
		_taskStartUnit = filterNA( taskStartUnit );
	}

	public String getFrequency()
	{
		return _frequency;
	}

	public void setFrequency(
	    String frequency
    ) {
		_frequency = filterNA( frequency );
	}

	public String getFrequencyUnit()
	{
		return _frequencyUnit;
	}

	public void setFrequencyUnit(
	    String frequencyUnit
    ) {
		_frequencyUnit = filterNA( frequencyUnit );
	}

	public String getTaskNumber()
	{
		return _taskNumber;
	}

	public void setTaskNumber(
	    String taskNumber
    ) {
		_taskNumber = filterNA( taskNumber );
	}

	public String getPriors()
	{
		return _priors;
	}

	public void setPriors(
	    String priors
    ) {
		priors = filterNA( priors );
		if( priors == null || priors.length() == 0 )
		{
			_priors = "";
			return;
		}
		if( priors.charAt(0) == '"' && priors.charAt(priors.length() - 1) == '"' )
			_priors = priors.substring(1, priors.length() - 1);
		else
			_priors = priors;
	}

	public Enumeration<ItemRESOURCE> getResource()
	{
		return _resources.elements();
	}
	
	public String getResourceNames()
	{
		StringBuffer sb = new StringBuffer();
		Iterator<String> itrRes = _resourceRefs.iterator();
		while( itrRes.hasNext() )
		{
			sb.append( itrRes.next() );
			if( itrRes.hasNext() )
			{
				sb.append( ", " );
			}
		}
		return sb.toString();
	}

	public void setResourceNames(
	    String resourceNames
    ) {
		_resourceRefs = new HashSet<String>();
		addResourceNames( resourceNames );
	}

	public void addResourceNames(
	    String resourceNames
    ) {
		resourceNames = filterNA( resourceNames );
		if( resourceNames == null || resourceNames.length() == 0 )
		{
			return;
		}
		
		//Remove surrounding quotes is present.
		if (resourceNames.charAt(0) == '"'  && resourceNames.charAt(resourceNames.length() -1 ) == '"')
		{
			resourceNames = resourceNames.substring(1,resourceNames.length() -1);
		}
		
		StringTokenizer strToken = new StringTokenizer(resourceNames, ",");
		while (strToken.hasMoreElements())
		{
			_resourceRefs.add( strToken.nextToken() );
		}
	}
	
	
	@Override
	public boolean skip( 
		Parser parser, 
		long flags
	) {
		if( isItemFiltered( parser.filters(), Parser.SHEET_TYPE, getTypeName() ) )
		{
			return true;
		}

		return super.skip( parser, flags );
	}


	public Iterator<JobTask> tasks()
	{
		return _tasks.iterator();
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());

		sb.append("\tStatus        = " + _status + "\t ");
		sb.append("Start     	 = " + _start + ":");
		sb.append(_taskStartUnit + "\t ");
		sb.append("Duration 	 = " + _duration + ":");
		sb.append(_durationUnit + "\t ");
		sb.append("Frequency 	 = " + _frequency + ":");
		sb.append(_frequencyUnit + "\n");
		sb.append("\tTypeName      = " + _typeName + "\n");
		if( _typeReferences != null && _typeReferences.length > 0 )
		{
			sb.append("\t\t");
		}
		for( int i = 0; _typeReferences != null && i < _typeReferences.length; i++ )
		{
			sb.append(_typeReferences[i].getName() + " ");
		}
		if( _typeReferences != null && _typeReferences.length > 0 )
		{
			sb.append("\n");
		}
		sb.append("\tResourceNames = " );
		sb.append( getResourceNames() );
		sb.append("\n" );
		sb.append("\tResources: ");
		Enumeration<ItemRESOURCE> resourceEnum = _resources.elements();
		while( resourceEnum.hasMoreElements() )
		{
			sb.append(resourceEnum.nextElement().getName());
			if( resourceEnum.hasMoreElements() )
			{
				sb.append(", ");
			}
		}
		sb.append("\n");
		sb.append("\tTasks:\n");
		Iterator<JobTask> itr = _tasks.iterator();
		while( itr.hasNext() )
		{
			sb.append(itr.next().toString());
		}

		return sb.toString();
	}

	public static int durationStringToUnits(
	    String duration)
	{
		if( duration.equalsIgnoreCase("SECOND") || duration.equalsIgnoreCase("SECONDS")
		        || duration.equalsIgnoreCase("S") || duration.equalsIgnoreCase("SEC") )
		{
			return JOB_DURATION_SECOND;
		}
		if( duration.equalsIgnoreCase("MINUTE") || duration.equalsIgnoreCase("MINUTES")
		        || duration.equalsIgnoreCase("M") || duration.equalsIgnoreCase("MIN") )
		{
			return JOB_DURATION_MINUTE;
		}
		if( duration.equalsIgnoreCase("HOUR") || duration.equalsIgnoreCase("HOURS") || duration.equalsIgnoreCase("H")
		        || duration.equalsIgnoreCase("HR") )
		{
			return JOB_DURATION_HOUR;
		}
		if( duration.equalsIgnoreCase("DAY") || duration.equalsIgnoreCase("DAYS") || duration.equalsIgnoreCase("D") )
		{
			return JOB_DURATION_DAY;
		}
		return JOB_DURATION_UNKNOWN;
	}

	@Override
	public void export(
	    Exporter exporter)
	{

		short cCnt = 0;
		String colNames[] = exporter.getColumnNames();
		String values[] = new String[colNames.length];
		Iterator<JobTask> iterTasks = tasks();
		HashMap<String, Short> hm = new HashMap<String, Short>();

		for( short j = 0; j < colNames.length; j++ )
		{

			Method g;
			try
			{
				g = super.getMethod(this.getClass(), "get" + colNames[j]);
				if( colNames[j].equalsIgnoreCase("ExtIdentifier") )
				{
					hm.put("ExtIdentifier", j);
				}
				if( colNames[j].equalsIgnoreCase("ExtObject") )
				{
					hm.put("ExtObject", j);
				}
				if( colNames[j].equalsIgnoreCase("TaskNumber") )
				{
					hm.put("TaskNumber", j);
				}
				else if( colNames[j].equalsIgnoreCase("Description") )
				{
					hm.put("Description", j);
				}
				else if( colNames[j].equalsIgnoreCase("Priors") )
				{
					hm.put("Priors", j);
				}
				else if( colNames[j].equalsIgnoreCase("ResourceNames") )
				{
					hm.put("ResourceNames", j);
				}
				else if( colNames[j].equalsIgnoreCase("Frequency") )
				{
					hm.put("Frequency", j);
				}
				else if( colNames[j].equalsIgnoreCase("FrequencyUnit") )
				{
					hm.put("FrequencyUnit", j);
				}
				else if( colNames[j].equalsIgnoreCase("Duration") )
				{
					hm.put("Duration", j);
				}
				else if( colNames[j].equalsIgnoreCase("DurationUnit") )
				{
					hm.put("DurationUnit", j);
				}
			}
			catch( SecurityException e )
			{
				exporter.getLogger().exception(e);
				continue;
			}
			catch( NoSuchMethodException e )
			{
				exporter.getLogger().exception(e);
				continue;
			}

			String value;
			try
			{
				value = (String) g.invoke(this, new Object[] {});
				if( value == null || value.equals("") )
					value = Exporter.NA;
				values[j] = value;
				cCnt++;
			}
			catch( IllegalArgumentException e )
			{
				exporter.getLogger().exception(e);
			}
			catch( IllegalAccessException e )
			{
				exporter.getLogger().exception(e);
			}
			catch( InvocationTargetException e )
			{
				exporter.getLogger().exception(e);
			}
		}
		exporter.addRow(values);

		while( iterTasks.hasNext() )
		{
			JobTask jt = iterTasks.next();

			values[hm.get("ExtIdentifier")] = String.valueOf(jt.getExtIdentifier());
			values[hm.get("ExtObject")] = String.valueOf(jt.getExtObject());
			values[hm.get("TaskNumber")] = String.valueOf(jt.getTaskNumber());
			values[hm.get("Description")] = jt.getDescription();
			values[hm.get("Duration")] = jt.getDuration();
			values[hm.get("DurationUnit")] = jt.getDurationUnits();

			String[] preres = jt.getPreReqs();

			if( preres != null && preres.length > 0 )
			{
				values[hm.get("Priors")] = preres[preres.length - 1];
			}
			// values[(Short) hm.get("ResourceNames")] = jt.getTaskName();
			values[hm.get("Frequency")] = Exporter.NA;
			values[hm.get("FrequencyUnit")] = Exporter.NA;
			values[hm.get("ResourceNames")] = Exporter.NA;

			exporter.addRow(values);
		}
	}
}