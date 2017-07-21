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


public class JobTask
{
	private String   _description   = "";
	private String   _extIdentifier = "";
	private String   _extObject     = "";
	private int      _taskNumber    = -1;
	private String   _taskName      = "";
	private String   _duration      = "";
	private String   _durationUnit;
	private String[] _preReqs;

	public JobTask(
	) {
	}
	
	public JobTask(
		Parser  parser,
		ItemJOB job,
		ItemJOB owningJob
	) {
		_description = job.getDescription();
		_taskName = job.getTaskNumber();
		try
		{
			_taskNumber  = Integer.parseInt( job.getTaskNumber() );
		}
		catch( Throwable  t )
		{ 
			/* Ignore  No guarantee It is really in numeric format */ 
			String[] params = { job.getName(), getTaskName(), _taskName };
			parser.getLogger().dataIntegrityMessage( Parser.VALIDATE_TASK_NUMBER, params );
		}
		_duration     = job.getDuration();
		_durationUnit = job.getDurationUnit();
		
		String prior = job.getPriors();
		_preReqs = prior.split( "," );
		
		owningJob.addResourceNames( job.getResourceNames() );
	}

	public String getDescription()
    {
    	return _description;
    }

	public String getDuration()
    {
    	return _duration;
    }

	public void getDuration(
		String duration
	) {
    	_duration = duration;
    }

	public String getDurationUnits()
    {
    	return _durationUnit;
    }
	
	public int getDurationUnitType() 
	{
		return ItemJOB.durationStringToUnits( _durationUnit );
	}

	public String getExtIdentifier() 
	{
		return _extIdentifier;
	} 
	
	public String getExtObject() 
	{
		return _extObject;
	}

	public String[] getPreReqs()
    {
    	return _preReqs;
    }
	

	public String getTaskName()
    {
    	return _taskName;
    }

	public int getTaskNumber()
    {
    	return _taskNumber;
    }

	public void setDescription(
		String description
	) {
    	 _description = description;
    }

	public void setDuration(
		String duration
	) {
    	_duration = duration;
    }
	
	public void setDurationUnits(
		String durationUnit
	) {
    	_durationUnit = durationUnit;
    }

	public void setExtIdentifier(String extIdentifier) 
	{
		_extIdentifier =  extIdentifier;
	}
	
	public void setExtObject(String extObject) 
	{
		_extObject = extObject;
	}

	public void setTaskName(
		String name
	) {
    	_taskName = name;
    }
	
	public void setTaskNumber(
		int taskNumber
	) {
		_taskNumber = taskNumber;
	}

	

	@Override
	public String toString() 
	{
		StringBuffer sb = new StringBuffer();

		if( _taskNumber >= 0 )
		{
			sb.append("\t\tTaskNumber = " + _taskNumber + "\t ");
		}
		else
		{
			sb.append("\t\tTaskName = " + _taskName + "\t ");
		}
		sb.append("Description = " + _description + "\t ");
		sb.append("Duration = " + _duration + " ");
		sb.append("DurationUnit = " + _durationUnit + "\t ");
		sb.append("Pre Requeists = " );
		for( int i = 0; i < _preReqs.length; i++ )
		{
			sb.append( _preReqs[i] );
			if( i + 1 < _preReqs.length ) sb.append( ", " );
		}
		sb.append("\n");

		return sb.toString();
	}
}