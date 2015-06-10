package denisefurlong.com.googlesheetswrapper;

public class PendingTask {

    private TaskCallback mCallback;

    /**
     * Constructor
     *
     * @param taskCallback callback to be executed when task completes
     */
    public PendingTask(TaskCallback taskCallback){
        mCallback = taskCallback;
    }

    /**
     * Sets callback to be executed when task completes.
     *
     * @param taskCallback callback to be executed
     */
    public void setCallback(TaskCallback taskCallback){
        mCallback.setTaskComplete(taskCallback.getTaskComplete());
    }
}
