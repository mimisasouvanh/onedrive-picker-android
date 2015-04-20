// ------------------------------------------------------------------------------
// Copyright (c) 2014 Microsoft Corporation
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
// ------------------------------------------------------------------------------

package com.example.onedrivesdk.saversample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.microsoft.onedrivesdk.saver.ISaver;
import com.microsoft.onedrivesdk.saver.Saver;
import com.microsoft.onedrivesdk.saver.SaverException;

/**
 * Activity that shows how the OneDrive SDK can be used for file saving
 * 
 * @author pnied
 */
public class SaverMain extends Activity {

    public static final int PICK_FROM_GALLERY_REQUEST_CODE = 4;

    /**
     * Registered Application id for OneDrive {@see http://go.microsoft.com/fwlink/p/?LinkId=193157}
     */
    private static final String ONEDRIVE_APP_ID = "48122D4E";

    /**
     * The onClickListener that will start the OneDrive Picker
     */
    private final OnClickListener mStartPickingListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("*/*");
            startActivityForResult(galleryIntent, PICK_FROM_GALLERY_REQUEST_CODE);
        }
    };

    /**
     * The OneDrive saver instance used by this activity
     */
    private ISaver mSaver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saver_main);

        // Create the picker instance
        mSaver = Saver.createSaver(ONEDRIVE_APP_ID);

        // Add the start saving listener
        findViewById(R.id.startSaverButton).setOnClickListener(mStartPickingListener);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(requestCode == mSaver.getRequestCode()) {
            // Check that we were able to save the file on OneDrive
            final TextView overallResult = (TextView) findViewById(R.id.overall_result);
            final TextView errorResult = (TextView) findViewById(R.id.error_type_result);
            final TextView debugErrorResult = (TextView) findViewById(R.id.debug_error_result);

            try {
                mSaver.handleSave(requestCode, resultCode, data);
                overallResult.setText(getString(R.string.overall_result_success));
                errorResult.setText(getString(R.string.error_message_none));
                debugErrorResult.setText(getString(R.string.error_message_none));
            } catch (final SaverException e) {
                overallResult.setText(getString(R.string.overall_result_failure));
                errorResult.setText(e.getErrorType().toString());
                debugErrorResult.setText(e.getDebugErrorInfo());
            }
            findViewById(R.id.result_table).setVisibility(View.VISIBLE);
        } else if(requestCode == PICK_FROM_GALLERY_REQUEST_CODE) {
            saveFileToDrive(data.getData(), this);
        } else {
            Log.e(getClass().getSimpleName(), "Unable to resolve onActivityResult request code " + requestCode);
        }
    }

    private void saveFileToDrive(final Uri data, final Activity activity) {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(final Void... voids) {
                // Create URI from real path
                final String path = getPathFromUri(data);

                mSaver.startSaving(activity, path, Uri.parse(data.toString()));
                return null;
            }
        }.execute((Void)null);
    }

    /**
     * Gets the path from a URI
     * @param uri The uri fro the item to look up its full path
     * @return The path
     */
    public String getPathFromUri(final Uri uri)
    {
        final String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);
            final int data_column = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(data_column);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
