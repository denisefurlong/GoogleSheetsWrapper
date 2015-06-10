package denisefurlong.com.drivesheetswrapper;

public class TaskCallback {

    private TaskComplete mTaskComplete;

    /**
     * Constructor
     *
     * @param taskCompleteCallback class implementing the TaskComplete interface whose onComplete method will be
     *                 executed on task completion
     */
    public TaskCallback(TaskComplete taskCompleteCallback){
        mTaskComplete = taskCompleteCallback;
    }

    /**
     * Constructor
     */
    public TaskCallback(){
        mTaskComplete = null;
    }

    /**
     * Set the callback to be executes upon task completion
     *
     * @param taskCompleteCallback class implementing the TaskComplete interface whose onComplete method will be
     *                 executed on task completion
     */
    public void setTaskComplete(TaskComplete taskCompleteCallback){
        mTaskComplete = taskCompleteCallback;
    }

    /**
     * Get the callback to be executes upon task completion
     *
     * @return class implementing the TaskComplete interface whose onComplete method will be
     *         executed on task completion
     */
    public TaskComplete getTaskComplete(){
        return mTaskComplete;
    }

    public void callCallback(TaskResult result){
        mTaskComplete.onComplete(result);
    }
}
