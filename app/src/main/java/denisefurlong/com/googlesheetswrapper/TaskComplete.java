package denisefurlong.com.googlesheetswrapper;

public interface TaskComplete {
    /**
     * Method which is called when a task has completed.
     *
     * @param result result from task method
     */
    public void onComplete(TaskResult result);
}
