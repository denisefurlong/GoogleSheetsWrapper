package denisefurlong.com.googlesheetswrapper;

import com.google.gdata.client.spreadsheet.SpreadsheetService;

public class ServiceTaskResult extends TaskResult<SpreadsheetService>{

    public ServiceTaskResult(boolean isSuccess, String message, SpreadsheetService retrievedData){
        super(isSuccess, message, retrievedData);
    }

    public ServiceTaskResult(boolean isSuccess, String message){
        super(isSuccess, message);
    }
}
