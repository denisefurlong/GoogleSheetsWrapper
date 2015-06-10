package denisefurlong.com.drivesheetswrapper;

import com.google.gdata.data.spreadsheet.ListEntry;

import java.util.List;

public class WorksheetsTaskResult extends TaskResult<List<ListEntry>> {

    /**
     * Constructor
     *
     * @param isSuccess boolean indicating whether or not the task executed successfully
     * @param message message representing the status of the task
     * @param retrievedData data returned from the task
     */
    public WorksheetsTaskResult(boolean isSuccess, String message, List<ListEntry> retrievedData){
        super(isSuccess, message, retrievedData);
    }

    /**
     * Constructor
     *
     * @param isSuccess boolean indicating whether or not the task executed successfully
     * @param message message representing the status of the task
     */
    public WorksheetsTaskResult(boolean isSuccess, String message){
        super(isSuccess, message);
    }
}
