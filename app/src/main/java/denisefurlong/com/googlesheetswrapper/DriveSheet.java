package denisefurlong.com.googlesheetswrapper;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.common.collect.Lists;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.android.gms.drive.DriveId;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;


public class DriveSheet {
    private SpreadsheetEntry mSheetEntry;
    private DriveId mDriveId;
    private GoogleApiClient mApiClient;
    private String mAccountName;
    private Context mContext;
    private TaskCallback mConnectionCallback;
    private TaskCallback mAddWorkSheetCallback;
    private TaskCallback mDeleteWorksheetCallback;
    private TaskCallback mWriteRowCallback;
    private TaskCallback mUpdateRowCallback;
    private TaskCallback mGetWorksheetRowsCallback;
    private TaskCallback mDeleteRowCallback;

    /**
     * Constructor
     *
     * @param  context  application context
     * @param  googleApiClient api client
     * @param  googleDriveId drive id of the spreadsheet
     */
    public DriveSheet(Context context, GoogleApiClient googleApiClient, DriveId googleDriveId){
        mContext = context;
        mApiClient = googleApiClient;
        mAccountName = Plus.AccountApi.getAccountName(mApiClient);
        mDriveId = googleDriveId;
        mConnectionCallback = new TaskCallback();
        mAddWorkSheetCallback = new TaskCallback(new TaskComplete(){public void onComplete(TaskResult result){}});
        mDeleteWorksheetCallback = new TaskCallback(new TaskComplete(){public void onComplete(TaskResult result){}});
        mWriteRowCallback = new TaskCallback(new TaskComplete(){public void onComplete(TaskResult result){}});
        mUpdateRowCallback = new TaskCallback(new TaskComplete(){public void onComplete(TaskResult result){}});
        mGetWorksheetRowsCallback = new TaskCallback(new TaskComplete(){public void onComplete(TaskResult result){}});
        mDeleteRowCallback = new TaskCallback(new TaskComplete(){public void onComplete(TaskResult result){}});
    }

    /**
     * Searches google drive for the spreadsheet corresponding to the DriveId passed in the
     * constructor.
     *
     * @return   pending task on which a callback can be set
     */
    public PendingTask connectToSheet(){
        new Thread() {
            @Override
            public void run() {
                TaskResult result = null;

                try {
                    result = getService();
                    SpreadsheetService service = (SpreadsheetService) result.getRetrievedData();
                    if (service == null){
                        callCallback(mConnectionCallback, result);
                        return;
                    }

                    result = getSpreadsheetURL();
                    URL feedUrl = (URL) result.getRetrievedData();
                    if (feedUrl == null){
                        callCallback(mConnectionCallback, result);
                        return;
                    }

                    SpreadsheetFeed spreadsheetFeed = service.getFeed(
                            feedUrl,
                            SpreadsheetFeed.class);
                    List<SpreadsheetEntry> spreadsheets = spreadsheetFeed.getEntries();
                    for (SpreadsheetEntry spreadsheet : spreadsheets) {
                        if (spreadsheet.getKey().equals(mDriveId.getResourceId())) {
                            Log.e(ConstantValues.APP_TAG, "Assigning sheet entry");
                            mSheetEntry = spreadsheet;
                        }
                    }
                    result = new TaskResult(true, "Found Spreadsheet");
                }
                catch (IOException e) {
                    Log.e(ConstantValues.APP_TAG, "IO Error retrieving spreadsheet: " +
                            e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }
                catch (ServiceException e) {
                    Log.e(ConstantValues.APP_TAG, "Service Error retrieving spreadsheet: " +
                            e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }

                callCallback(mConnectionCallback, result);
            }
        }.start();
        return new PendingTask(mConnectionCallback);
    }

    /**
     * Adds worksheet to the spreadsheet.
     *
     * @param worksheetName name of worksheet to add
     * @param columnCount number of columns in worksheet
     * @param rowCount number of rows in worksheet
     * @return   pending task on which a callback can be set
     */
    public PendingTask addWorksheet(final String worksheetName, final int columnCount, final int rowCount){
        new Thread(){
            @Override
            public void run() {
                TaskResult result = null;
                try {
                    result = getService();
                    SpreadsheetService service = (SpreadsheetService) result.getRetrievedData();
                    if (service == null){
                        callCallback(mAddWorkSheetCallback, result);
                        return;
                    }

                    WorksheetEntry worksheet = new WorksheetEntry();
                    worksheet.setTitle(new PlainTextConstruct(worksheetName));
                    worksheet.setColCount(columnCount);
                    worksheet.setRowCount(rowCount);
                    service.insert(mSheetEntry.getWorksheetFeedUrl(), worksheet);
                    result = new TaskResult(true, "Added Worksheet");
                }
                catch (IOException e){
                    Log.e(ConstantValues.APP_TAG, "IO Error creating worksheet: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }
                catch (ServiceException e){
                    Log.e(ConstantValues.APP_TAG, "Service Error creating worksheet: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }

                callCallback(mAddWorkSheetCallback, result);
            }
        }.start();
        return new PendingTask(mAddWorkSheetCallback);
    }

    /**
     * Deletes worksheet from the spreadsheet.
     *
     * @param worksheetName name of worksheet to delete
     * @return   pending task on which a callback can be set
     */
    public PendingTask deleteWorksheet(final String worksheetName){
        new Thread(){
            @Override
            public void run() {
                TaskResult result = getWorksheetEntry(worksheetName);
                WorksheetEntry worksheet = (WorksheetEntry) result.getRetrievedData();
                if (worksheet == null){
                    callCallback(mDeleteWorksheetCallback, result);
                    return;
                }

                try{
                    worksheet.delete();
                    result = new TaskResult(true, "Worksheet deleted");
                }
                catch (IOException e){
                    Log.e(ConstantValues.APP_TAG, "IO Error deleting worksheet: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }
                catch (ServiceException e){
                    Log.e(ConstantValues.APP_TAG, "Service Error deleting worksheet: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }

                callCallback(mDeleteWorksheetCallback, result);
            }
        }.start();
        return new PendingTask(mDeleteWorksheetCallback);
    }

    /**
     * Updates a row in a worksheet.
     *
     * @param worksheetName name of worksheet to delete
     * @param rowIndex index of row to update. Google sheets API regards the first row as index 1,
     *                 not 0.
     * @param startingColumnIndex index of first column to be updated in row. Any preceeding columns
     *                            will not be changed. Google sheets API regards the first column as
     *                            index 1, not 0.
     * @param rowContent List of values to be inserted into the row. The first value is inserted
     *                   at startingColumnIndex, the second value inserted into the following column
     *                   and so on.
     * @return   pending task on which a callback can be set
     */
    public PendingTask updateRow(final String worksheetName, final int rowIndex, final int startingColumnIndex,
                          final ArrayList<String> rowContent){
        new Thread() {
            @Override
            public void run() {
                TaskResult result = getWorksheetEntry(worksheetName);
                WorksheetEntry worksheet = (WorksheetEntry) result.getRetrievedData();
                if (worksheet == null){
                    callCallback(mUpdateRowCallback, result);
                    return;
                }

                try {
                    result = getService();
                    SpreadsheetService service = (SpreadsheetService) result.getRetrievedData();
                    if (service == null){
                        callCallback(mUpdateRowCallback, result);
                        return;
                    }

                    URL cellFeedUrl = new URL(worksheet.getCellFeedUrl().toString() + "?start-index="+rowIndex+"&max-row=1");
                    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
                    List<CellEntry> cellEntries = cellFeed.getEntries();

                    int colIndex = startingColumnIndex;
                    for (String rowValue : rowContent) {
                        if (colIndex + 1 > cellEntries.size()) {
                            CellEntry cell = new CellEntry(rowIndex, colIndex, rowValue);
                            service.insert(cellFeedUrl, cell);
                        } else {
                            CellEntry cell = cellEntries.get(colIndex);
                            cell.changeInputValueLocal(rowValue);
                            cell.update();
                        }
                        colIndex++;
                    }
                    result = new TaskResult(true, "Row updated");
                }
                catch (IOException e){
                    Log.e(ConstantValues.APP_TAG, "IO Error updating row: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }
                catch (ServiceException e){
                    Log.e(ConstantValues.APP_TAG, "Service Error updating row: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }

                callCallback(mUpdateRowCallback, result);
            }
        }.start();
        return new PendingTask(mUpdateRowCallback);
    }

    /**
     * Writes a row to the first blank line of a worksheet.
     *
     * @param worksheetName name of worksheet to delete
     * @param startingColumnIndex index of first column to be updated in row. Any preceeding columns
     *                            will not be changed. Google sheets API regards the first column as
     *                            index 1, not 0.
     * @param rowContent List of values to be inserted into the row. The first value is inserted
     *                   at startingColumnIndex, the second value inserted into the following column
     *                   and so on.
     * @return   pending task on which a callback can be set
     */
    public PendingTask writeRow(final String worksheetName, final int startingColumnIndex,
                         final ArrayList<String> rowContent){
        return writeRow(worksheetName, -1, startingColumnIndex, rowContent);
    }

    /**
     * Writes a row to a worksheet.
     *
     * @param worksheetName name of worksheet to delete
     * @param startingColumnIndex index of first column to be updated in row. Any preceeding columns
     *                            will not be changed.
     * @param rowIndex index at which the row will be written in the worksheet. Google sheets API
     *                 regards the first row as index 1, not 0.
     * @param rowContent list of values to be inserted into the row. The first value is inserted
     *                   at startingColumnIndex, the second value inserted into the following column
     *                   and so on.
     * @return   pending task on which a callback can be set
     */
    public PendingTask writeRow(final String worksheetName, final int rowIndex, final int startingColumnIndex,
                         final ArrayList<String> rowContent){
        new Thread() {
            @Override
            public void run() {
                TaskResult result = getWorksheetEntry(worksheetName);
                WorksheetEntry worksheet = (WorksheetEntry) result.getRetrievedData();
                if (worksheet == null){
                    callCallback(mWriteRowCallback, result);
                    return;
                }

                try {
                    result = getService();
                    SpreadsheetService service = (SpreadsheetService) result.getRetrievedData();
                    if (service == null){
                        callCallback(mWriteRowCallback, result);
                        return;
                    }

                    URL listFeedUrl = null;
                    int insertRowIndex = rowIndex;
                    if (insertRowIndex > 0) {
                        String urlString = worksheet.getListFeedUrl() + "?start-index=" + insertRowIndex;
                        listFeedUrl = new URL(urlString);
                    }
                    else {
                        // we are either inserting  at the end or a header row
                        listFeedUrl = worksheet.getListFeedUrl();
                    }
                    List<ListEntry> copied = new ArrayList<ListEntry>();
                    ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
                    List<ListEntry> retrievedEntries = listFeed.getEntries();
                    if (insertRowIndex != -1) {
                        copied = retrievedEntries;
                        for (ListEntry row : Lists.reverse(copied)) {
                            Log.e(ConstantValues.APP_TAG, "deleting row " + row.getPlainTextContent());
                            row.delete();
                        }
                    }
                    else {
                        insertRowIndex = retrievedEntries.size();
                    }

                    URL cellFeedUrl = new URL(worksheet.getCellFeedUrl().toString() + "?min-row=1&max-row=1");
                    int colIndex = startingColumnIndex;
                    for (String rowValue : rowContent) {
                        CellEntry cell = new CellEntry(insertRowIndex, colIndex, rowValue);
                        colIndex++;
                        service.insert(cellFeedUrl, cell);
                        Log.e(ConstantValues.APP_TAG, "inserted new cell");
                    }

                    for (ListEntry copiedRow : copied) {
                        service.insert(worksheet.getListFeedUrl(), copiedRow);
                        Log.e(ConstantValues.APP_TAG, "inserted copied row");
                    }
                    result = new TaskResult(false, "Row has been written");
                }
                catch (IOException e){
                    Log.e(ConstantValues.APP_TAG, "IO Error writing row: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }
                catch (ServiceException e){
                    Log.e(ConstantValues.APP_TAG, "Service Error writing row: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }

                callCallback(mWriteRowCallback, result);
            }
        }.start();
        return new PendingTask(mWriteRowCallback);
    }

    /**
     * Deletes a row from a worksheet.
     *
     * @param worksheetName name of worksheet to delete row from
     * @param rowIndex index of the row to be deleted. Google sheets API prohibits the deletion of
     *                 the first row  of a worksheet and regards the first row as index 1, not 0.
     * @return   pending task on which a callback can be set
     */
    public PendingTask deleteRow(final String worksheetName, final int rowIndex){
        new Thread() {
            @Override
            public void run(){
                if (rowIndex == 1){
                    TaskResult result = new TaskResult(false, "Can not delete header row");
                    callCallback(mDeleteRowCallback, result);
                    return;
                }

                TaskResult result = getWorksheetEntry(worksheetName);
                WorksheetEntry worksheet = (WorksheetEntry) result.getRetrievedData();
                if (worksheet == null){
                    callCallback(mDeleteRowCallback, result);
                    return;
                }

                try{
                    result = getService();
                    SpreadsheetService service = (SpreadsheetService) result.getRetrievedData();
                    if (service == null){
                        callCallback(mDeleteRowCallback, result);
                        return;
                    }

                    URL listFeedUrl = worksheet.getListFeedUrl();
                    ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
                    List<ListEntry> retrievedEntries = listFeed.getEntries();
                    if (retrievedEntries.size() >= rowIndex-1){
                        for (ListEntry r: retrievedEntries){
                            Log.e(ConstantValues.APP_TAG, "Delete row loop: " + r.getPlainTextContent());
                        }
                        ListEntry row = retrievedEntries.get(rowIndex-1);
                        Log.e(ConstantValues.APP_TAG, "Deleting this: " + row.getPlainTextContent());
                        row.delete();
                        result = new TaskResult(true, "Deleted row");
                    }
                    else{
                        result = new TaskResult(true, "Could not find row");
                    }
                }
                catch (IOException e){
                    Log.e(ConstantValues.APP_TAG, "IO Error deleting row: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }
                catch (ServiceException e){
                    Log.e(ConstantValues.APP_TAG, "Service Error deleting row: " + e.getMessage());
                    result = new TaskResult(false, e.getMessage());
                }

                callCallback(mDeleteRowCallback, result);
            }
        }.start();
        return new PendingTask(mDeleteRowCallback);
    }

    /**
     * Retrieves all rows from a worksheet.
     *
     * @param worksheetName name of worksheet to delete row from
     * @return   pending task on which a callback can be set
     */
    public PendingTask getWorksheetRows(final String worksheetName){
        new Thread() {
            @Override
            public void run() {
                TaskResult result = getWorksheetEntry(worksheetName);
                WorksheetEntry worksheet = (WorksheetEntry) result.getRetrievedData();
                if (worksheet == null){
                    callCallback(mGetWorksheetRowsCallback, result);
                    return;
                }

                try {
                    result = getService();
                    SpreadsheetService service = (SpreadsheetService) result.getRetrievedData();
                    if (service == null){
                        callCallback(mGetWorksheetRowsCallback, result);
                        return;
                    }

                    URL listFeedUrl = worksheet.getListFeedUrl();
                    ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
                    List<ListEntry> retrievedEntries = listFeed.getEntries();
                    result = new WorksheetsTaskResult(true, "Retrieved worksheet rows", retrievedEntries);
                }
                catch (IOException e){
                    Log.e(ConstantValues.APP_TAG, "IO Error reading worksheet: " + e.getMessage());
                    result = new WorksheetsTaskResult(false, e.getMessage());
                }
                catch (ServiceException e){
                    Log.e(ConstantValues.APP_TAG, "Service Error reading worksheet: " + e.getMessage());
                    result = new WorksheetsTaskResult(false, e.getMessage());
                }

                callCallback(mGetWorksheetRowsCallback, result);
            }
        }.start();
        return new PendingTask(mGetWorksheetRowsCallback);
    }

    /**
     * Retrieves worksheet
     *
     * @param worksheetName name of worksheet
     * @return   pending task on which a callback can be set
     */
    private TaskResult getWorksheetEntry(String worksheetName){
        TaskResult result = null;
        WorksheetEntry worksheet = null;
        try {
            List <WorksheetEntry> allWorksheets = mSheetEntry.getWorksheets();
            for (WorksheetEntry sheet: allWorksheets){
                if (sheet.getTitle().getPlainText().equals(worksheetName)){
                    worksheet = sheet;
                }
            }

            if (worksheet != null){
                result = new WorksheetTaskResult(true, "Found worksheet", worksheet);
            }
            else{
                result = new WorksheetTaskResult(true, "Worksheet not found");
            }
        }
        catch (IOException e){
            Log.e(ConstantValues.APP_TAG, "IO Error deleting worksheet: " + e.getMessage());
            result = new WorksheetTaskResult(false, e.getMessage());
        }
        catch (ServiceException e){
            Log.e(ConstantValues.APP_TAG, "Service Error deleting worksheet: " + e.getMessage());
            result = new WorksheetTaskResult(false, e.getMessage());
        }
        return result;
    }

    /**
     * Retrieves spreadsheet service
     *
     * @return   pending task on which a callback can be set
     */
    private TaskResult getService(){
        TaskResult result = null;
        try{
            String token = GoogleAuthUtil.getToken(mContext, mAccountName, ConstantValues.SCOPE_URL);
            SpreadsheetService service = new SpreadsheetService("MySpreadsheetIntegration-v1");
            service.setProtocolVersion(SpreadsheetService.Versions.V3);
            service.setHeader("Authorization", "Bearer " + token);
            result = new ServiceTaskResult(true, "Retrieved service", service);
        }
        catch (UserRecoverableAuthException e){
            Log.e(ConstantValues.APP_TAG, "Authentication error creating service: " + e.getMessage());
            result = new ServiceTaskResult(false, e.getMessage());
        }
        catch (GoogleAuthException e){
            Log.e(ConstantValues.APP_TAG, "Authentication error creating service: " + e.getMessage());
            result = new ServiceTaskResult(false, e.getMessage());
        }
        catch (IOException e){
            Log.e(ConstantValues.APP_TAG, "IO error creating service: " + e.getMessage());
            result = new ServiceTaskResult(false, e.getMessage());
        }
        catch (Exception e){
            Log.e(ConstantValues.APP_TAG, "Error creating service: " + e.getMessage());
            result = new ServiceTaskResult(false, e.getMessage());
        }
        return result;
    }

    private TaskResult getSpreadsheetURL(){
        TaskResult result = null;
        try{
            URL spreadsheet_url = new URL(ConstantValues.SPREADSHEET_FEED_URL);
            result = new SheetUrlTaskResult(true, "Retrieved spreadsheet URL", spreadsheet_url);
        }
        catch (MalformedURLException e){
            Log.e(ConstantValues.APP_TAG, "Error creating spreadsheet url: " + e.getMessage());
            result = new SheetUrlTaskResult(false, "Error retrieving spreadsheet URL");
        }
        return result;
    }

    private void callCallback(TaskCallback callback, TaskResult result){
        if (callback.getTaskComplete() != null){
            callback.callCallback(result);
        }
    }
}
