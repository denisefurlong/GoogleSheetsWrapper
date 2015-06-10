package denisefurlong.com.drivesheetswrapper;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.plus.Plus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.model.File;
import com.google.gdata.data.spreadsheet.ListEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeScreen extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mApiClient;
    private DriveSheet sheet;
    private String mNextPageToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mApiClient == null) {
            Scope sheets = new Scope("https://spreadsheets.google.com/feeds/");
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addScope(sheets)
                    .addConnectionCallbacks(this)
                    .addApi(Plus.API)
                    .setAccountName("dave.frisby@gmail.com")
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mApiClient.connect();
    }

    private void checkForFile(){
        Query query = new Query.Builder()
                .setPageToken(mNextPageToken)
                .addFilter(Filters.or(
                        Filters.contains(SearchableField.TITLE, "hij"),
                        Filters.contains(SearchableField.TITLE, "hij")))
                .build();
        Drive.DriveApi.query(mApiClient, query).setResultCallback(metadataBufferCallback);
    }

    private final ResultCallback<DriveApi.MetadataBufferResult> metadataBufferCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        //baseActivity.showMessage("Problem while retrieving files");
                        return;
                    }
                    DriveId driveId = null;
                    MetadataBuffer buffer = result.getMetadataBuffer();
                    mNextPageToken = buffer.getNextPageToken();
                    for(Metadata meta : buffer){
                        Log.e(ConstantValues.APP_TAG, "Found file " + meta.getTitle());
                        driveId = meta.getDriveId();
                    }

                    if (driveId == null){
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential crd =
                                            GoogleAccountCredential
                                                    .usingOAuth2(HomeScreen.this, Arrays.asList("https://spreadsheets.google.com/feeds/", "https://www.googleapis.com/auth/drive.file"));
                                    crd.setSelectedAccountName("dave.frisby@gmail.com");
                                    com.google.api.services.drive.Drive driveServ = new com.google.api.services.drive.Drive.Builder(
                                            AndroidHttp.newCompatibleTransport(), new GsonFactory(), crd).build();
                                    File body = new File();
                                    body.setTitle("hij");
                                    body.setDescription("Budget File");
                                    body.setMimeType("application/vnd.google-apps.spreadsheet");
                                    File newFile = driveServ.files().insert(body).execute();
                                    Log.e(ConstantValues.APP_TAG, "New filed ID generated: " + newFile.getId());
                                    Drive.DriveApi.fetchDriveId(mApiClient, newFile.getId()).setResultCallback(driveIdCallback);
                                }
                                catch (Exception e) {
                                    Log.e(ConstantValues.APP_TAG, "Exception " + e.getMessage());
                                }
                            }
                        }.start();
                    }
                    else{
                        Log.e(ConstantValues.APP_TAG, "drive id " + driveId.getResourceId());
                        sheet = new DriveSheet(HomeScreen.this, mApiClient, driveId);
                        sheet.connectToSheet().setCallback(connectionCallback);
                    }
                }
            };

    /**
     * Called when mApiClient successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(ConstantValues.APP_TAG, "Google Client connected");
        checkForFile();
    }

    final private ResultCallback<DriveApi.DriveIdResult> driveIdCallback = new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                //showMessage("Cannot find DriveId. Are you authorized to view this file?");
                Log.e(ConstantValues.APP_TAG, "Error retrieving new file id");
                return;

            }
            // add listener here to wait for
            // file resource Id to become available
            DriveFile createdFile = Drive.DriveApi.getFile(mApiClient, result.getDriveId());
            createdFile.addChangeListener(mApiClient, fileListener);
            MetadataChangeSet newChangeSet = new MetadataChangeSet.Builder()
                    .setMimeType("application/vnd.google-apps.spreadsheet")
                    .build();
            createdFile.updateMetadata(mApiClient, newChangeSet);
        }
    };

    final private ChangeListener fileListener = new ChangeListener() {
        @Override
        public void onChange(ChangeEvent event) {
            //todo we should limit number of tries to get file id
            DriveFile driveFile = Drive.DriveApi.getFile(mApiClient, event.getDriveId());
            if (event.getDriveId().getResourceId() == null){
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setStarred(false).build();
                driveFile.updateMetadata(mApiClient, changeSet);
            }
            else{
                Log.e(ConstantValues.APP_TAG, "File ID has arrived "+ event.getDriveId().getResourceId());
                driveFile.removeChangeListener(mApiClient, fileListener);
                sheet = new DriveSheet(HomeScreen.this, mApiClient, driveFile.getDriveId());
                sheet.connectToSheet().setCallback(connectionCallback);
            }
        }
    };

    final private TaskCallback connectionCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            Log.e(ConstantValues.APP_TAG, "Callback: "+ result.getMessage());
            sheet.addWorksheet("sam", 4, 60).setCallback(addWorksheetCallback);
        }
    });

    final private TaskCallback addWorksheetCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            Log.e(ConstantValues.APP_TAG, " Add Worksheet Callback: "+ result.getMessage());
            sheet.deleteWorksheet("sam").setCallback(deleteWorksheetCallback);
        }
    });

    final private TaskCallback deleteWorksheetCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            Log.e(ConstantValues.APP_TAG, "Delete worksheet Callback: "+ result.getMessage());
            ArrayList<String> rowValues = new ArrayList<String>();
            rowValues.add("ababababa");
            rowValues.add("ababababab");
            sheet.updateRow("Sheet2", 2, 2, rowValues).setCallback(updateRowCallback);
        }
    });

    final private TaskCallback updateRowCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            Log.e(ConstantValues.APP_TAG, " Update Row Callback: ");
            ArrayList<String> rowValues = new ArrayList<String>();
            rowValues.add("Denise");
            rowValues.add("Furlong");
            sheet.writeRow("Sheet2", 2, 2, rowValues).setCallback(writeRowCallback);
        }
    });

    final private TaskCallback writeRowCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            Log.e(ConstantValues.APP_TAG, " Write Row Callback: ");
            sheet.getWorksheetRows("Sheet2").setCallback(readRowCallback);
        }
    });

    final private TaskCallback readRowCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            for (ListEntry row : (List<ListEntry>)result.getRetrievedData()){
                for(String header : row.getCustomElements().getTags()){
                    Log.e(ConstantValues.APP_TAG, " Row Header: " + header);
                    Log.e(ConstantValues.APP_TAG, " Row Value: " + row.getCustomElements().getValue(header));
                }
            }
            sheet.deleteRow("Sheet2", 2).setCallback(deleteRowCallback);
        }
    });

    final private TaskCallback deleteRowCallback = new TaskCallback(new TaskComplete(){
        public void onComplete(TaskResult result){
            Log.e(ConstantValues.APP_TAG, " Delete Row Callback: ");
        }
    });

    /**
     * Called when mApiClient disconnects.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(ConstantValues.APP_TAG, "Google Client connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(ConstantValues.APP_TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, 1);
        } catch (IntentSender.SendIntentException e) {
            Log.e(ConstantValues.APP_TAG, "Exception while starting resolution activity", e);
        }
    }

}
