package denisefurlong.com.drivesheetswrapper;

import com.google.gdata.data.spreadsheet.WorksheetEntry;

public class WorksheetTaskResult extends TaskResult<WorksheetEntry> {

    /**
     * Constructor
     *
     * @param isSuccess boolean indicating whether or not the task executed successfully
     * @param message message representing the status of the task
     * @param retrievedData data returned from the task
     */
    public WorksheetTaskResult(boolean isSuccess, String message, WorksheetEntry retrievedData){
        super(isSuccess, message, retrievedData);
    }

    /**
     * Constructor
     *
     * @param isSuccess boolean indicating whether or not the task executed successfully
     * @param message message representing the status of the task
     */
    public WorksheetTaskResult(boolean isSuccess, String message){
        super(isSuccess, message);
    }
}
