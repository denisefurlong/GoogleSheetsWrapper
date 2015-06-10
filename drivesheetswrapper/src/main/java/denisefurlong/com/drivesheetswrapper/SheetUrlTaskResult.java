package denisefurlong.com.drivesheetswrapper;

import java.net.URL;

public class SheetUrlTaskResult extends TaskResult<URL> {

    public SheetUrlTaskResult(boolean isSuccess, String message, URL retrievedData){
        super(isSuccess, message, retrievedData);
    }

    public SheetUrlTaskResult(boolean isSuccess, String message){
        super(isSuccess, message);
    }
}
