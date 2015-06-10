package denisefurlong.com.googlesheetswrapper;

public class TaskResult<T> {
    private boolean mIsSuccess;
    private String mMessage;
    T mRetrievedData;

    /**
     * Constructor
     *
     * @param isSuccess boolean indicating whether or not the task executed successfully
     * @param message message representing the status of the task
     */
    public TaskResult(boolean isSuccess, String message){
        this.mIsSuccess = isSuccess;
        this.mMessage = message;
    }

    /**
     * Constructor
     *
     * @param isSuccess boolean indicating whether or not the task executed successfully
     * @param message message describing the status of the task
     * @param retrievedData data returned from the task
     */
    public TaskResult(boolean isSuccess, String message, T retrievedData){
        this.mIsSuccess = isSuccess;
        this.mMessage = message;
        this.mRetrievedData = retrievedData;
    }

    /**
     * Gets the success status of the task
     *
     * @return boolean indicating whether or not the task executed successfully
     */
    public boolean getIsSuccess(){ return mIsSuccess; }

    /**
     * Gets the message of the task
     *
     * @return message describing the status of the task
     */
    public String getMessage(){
        return mMessage;
    }

    /**
     * Gets the data returned from the task
     *
     * @return data returned from the task
     */
    public T getRetrievedData(){
        return mRetrievedData;
    }

    /**
     * Sets the success status of the task
     *
     * @param isSuccess success status returned from the task
     */
    public void setIsSuccess(boolean isSuccess){
        this.mIsSuccess = isSuccess;
    }

    /**
     * Sets the message of the task
     *
     * @param message message describing the status of the task
     */
    public void setMessage(String message){
        this.mMessage = message;
    }

    /**
     * Sets the retrieved data of the task
     *
     * @param retrievedData data returned from the task
     */
    public void setRetrievedData(T retrievedData){
        this.mRetrievedData = retrievedData;
    }
}
