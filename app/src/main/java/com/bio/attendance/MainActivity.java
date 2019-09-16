package com.bio.attendance;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bio.attendance.fragments.AttendanceFragment;
import com.bio.attendance.fragments.EnrollFragment;
import com.bio.attendance.utils.Constants;
import com.bio.attendance.utils.Utils;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nitgen.SDK.AndroidBSP.NBioBSPJNI;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.EditText;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener, NBioBSPJNI.CAPTURE_CALLBACK, SampleDialogFragment.SampleDialogListener, UserDialog.UserDialogListener {
    private static final String TAG = MainActivity.class.getName();
    private Drawer result = null;
    private Context context;
    private TextView titleText;
    private NBioBSPJNI bsp;
    private NBioBSPJNI.Export exportEngine;
    android.app.DialogFragment sampleDialogFragment;
    UserDialog userDialog;
    AttendanceFragment attendanceFragment;
    EnrollFragment enrollFragment;
    private int MENU_POSITION = 1;
    private String msg;
    private byte[] byCapturedRaw1;
    private NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
    boolean isDeviceOpen;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setBackgroundColor(getResources().getColor(R.color.teal));

        setSupportActionBar(toolbar);

        result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)

                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawwler_menu_add_attendance).withIcon(FontAwesome.Icon.faw_book),
                        new PrimaryDrawerItem().withName(R.string.drawwler_menu_enroll).withIcon(FontAwesome.Icon.faw_files_o),
                        new SectionDrawerItem().withName(R.string.drawer_item_section_header),
                        new SecondaryDrawerItem().withName(R.string.drawwler_menu_setting).withIcon(FontAwesome.Icon.faw_gear)

                ).withHeader(R.layout.drawler_header)
                .withFooter(R.layout.drawler_footer)
                .withOnDrawerItemClickListener(this)
                .withSelectedItemByPosition(1)
                .withSavedInstance(savedInstanceState)
                .withSliderBackgroundColor(getResources().getColor(R.color.cyan))
                .build();

        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_drawler_toggle);

        titleText = (TextView) result.getHeader().findViewById(R.id.txtProfileTitle);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(Constants.PREF_USER_DATA_SAVE, false)) {
            getUserDetails();
        } else {
            titleText.setText(sharedPreferences.getString(Constants.PREF_USER_NAME, "Welcome To Bio Attendance"));
        }
        //Initialize Device SDK
        initData();

        //by default display attendance view
        initializeAttendanceView();

        if (!Utils.isNetworkAvailable(context)) {
            Utils.showAlertDialog(context, getString(R.string.app_name), getString(R.string.network_enabled_msg));
        }
    }

    private void getUserDetails() {

        String userName = "Welcome To Bio Attendance";
        String userPic = "";
        try {
            final String[] projection = new String[]{ContactsContract.Profile.DISPLAY_NAME, ContactsContract.Profile.PHOTO_URI};

            final Uri dataUri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
            final ContentResolver contentResolver = getContentResolver();
            final Cursor cursor = contentResolver.query(dataUri, projection, null, null, null);
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            try {
                if (cursor.moveToFirst()) {
                    userName = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
                    titleText.setText(userName);

                    edit.putString(Constants.PREF_USER_NAME, userName);
                    edit.putBoolean(Constants.PREF_USER_DATA_SAVE, true);
                    edit.commit();

                }
            } finally {
                cursor.close();
            }
        } catch (Exception e) {
            titleText.setText(userName);
            Log.e(TAG, "Error While Getting User Details : " + e);
        }
    }

    private void showSettingDialog(final Context context) {


        Dialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {

            @Override
            protected void onBuildDone(Dialog dialog) {
                dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.findViewById(R.id.serverIP);

                SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);

                EditText serverIP = (EditText) dialog.findViewById(R.id.serverIP);
                EditText serverPort = (EditText) dialog.findViewById(R.id.serverPort);

                serverIP.setText(sharedPreferences.getString(Constants.SERVER_IP, "127.0.0.10"));
                serverPort.setText(sharedPreferences.getString(Constants.SERVER_PORT, "8080"));
            }

            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                EditText serverIP = (EditText) fragment.getDialog().findViewById(R.id.serverIP);
                EditText serverPort = (EditText) fragment.getDialog().findViewById(R.id.serverPort);
                if (TextUtils.isEmpty(serverIP.getText())) {
                    serverIP.setError("Please Provide Ip Address of Server");
                    return;
                }
                if (TextUtils.isEmpty(serverPort.getText())) {
                    serverPort.setError("Please Provide Port of Server");
                    return;
                }
                saveSettings(context, serverIP.getText().toString(), serverPort.getText().toString());
                super.onPositiveActionClicked(fragment);
            }

            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                Toast.makeText(context, "Setting Discard", Toast.LENGTH_SHORT).show();
                super.onNegativeActionClicked(fragment);
            }

            @Override
            protected Dialog onBuild(Context context, int styleId) {
                return super.onBuild(context, styleId);
            }
        };

        builder.title("Server Settings")
                .positiveAction("Save")
                .negativeAction("CANCEL")
                .contentView(R.layout.layout_dialog_custom);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getSupportFragmentManager(), null);


    }

    private void saveSettings(Context context, String serverIP, String serverPort) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(Constants.SERVER_IP, serverIP);
            edit.putString(Constants.SERVER_PORT, serverPort);
            edit.commit();
            Toast.makeText(context, "Settings Is Saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error While Saving Settings : " + e);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:

                if (result != null) {
                    result.openDrawer();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!doubleBackToExitPressedOnce) {

            if (result != null) {
                if (result.isDrawerOpen()) {
                    result.closeDrawer();
                }
            }
        } else {

            finish();
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * @param view
     * @param position
     * @param drawerItem
     * @return true if the event was consumed
     */
    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        MENU_POSITION = position;
        switch (position) {
            case Constants.ADD_ATTENDANCE:
                initializeAttendanceView();
                getSupportActionBar().setTitle(getString(R.string.drawwler_menu_add_attendance));
                break;
            case Constants.ENROLL_STUDENTS:
                getSupportActionBar().setTitle(getString(R.string.drawwler_menu_enroll));
                initializeEnrollView();
                break;
            case Constants.SETTINGS:
                showSettingDialog(context);
                break;
            default:
                getSupportActionBar().setTitle(getString(R.string.app_name));
                break;

        }
        if (result != null) {
            if (result.isDrawerOpen()) {
                result.closeDrawer();
            }

        }
        return false;
    }

    private void initializeAttendanceView() {
        try {

            attendanceFragment = AttendanceFragment.newInstance(this, sampleDialogFragment, bsp);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_layout, attendanceFragment);
            fragmentTransaction.commit();

        } catch (Exception e) {
            Log.e(TAG, "Error While Initializing Attendance" + e);
        }
    }

    private void initializeEnrollView() {
        try {

            enrollFragment = EnrollFragment.newInstance(this, sampleDialogFragment, bsp);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_layout, enrollFragment);
            fragmentTransaction.commit();

        } catch (Exception e) {
            Log.e(TAG, "Error While Initializing Attendance" + e);
        }
    }

    public void initData() {

        NBioBSPJNI.CURRENT_PRODUCT_ID = 0;
        if (bsp == null) {
            bsp = new NBioBSPJNI("010701-613E5C7F4CC7C4B0-72E340B47E034015", MainActivity.this);
            String msg = "";
            if (bsp.IsErrorOccured())
                Utils.showAlertDialog(context, getString(R.string.app_name), "Error Occurred While Initializing Device " + bsp.GetErrorCode());

            else {
                msg = "SDK Version: " + bsp.GetVersion();
                exportEngine = bsp.new Export();
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "Init Data" + msg);
        }

        sampleDialogFragment = new SampleDialogFragment();
        userDialog = new UserDialog();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (bsp != null) {
            bsp.dispose();
            bsp = null;
        }

    }


    @Override
    public void onClickStopBtn(android.app.DialogFragment dialogFragment) {
        switch (MENU_POSITION) {
            case Constants.ADD_ATTENDANCE:
                attendanceFragment.OnStopCallback(dialogFragment);
                break;
            case Constants.ENROLL_STUDENTS:
                enrollFragment.OnStopCallback(dialogFragment);
                break;
        }
    }

    @Override
    public void onClickPositiveBtn(android.app.DialogFragment dialogFragment, String id) {

    }

    @Override
    public int OnCaptured(NBioBSPJNI.CAPTURED_DATA captured_data) {
        int capture = 1;
        switch (MENU_POSITION) {
            case Constants.ADD_ATTENDANCE:
                capture = attendanceFragment.OnCapturedCallback(captured_data);
                break;
            case Constants.ENROLL_STUDENTS:
                capture = enrollFragment.OnCapturedCallback(captured_data);
                break;
        }
        return capture;
    }

    @Override
    public void OnConnected() {
        switch (MENU_POSITION) {
            case Constants.ADD_ATTENDANCE:
                attendanceFragment.onConnectedCallback();
                break;
            case Constants.ENROLL_STUDENTS:
                enrollFragment.onConnectedCallback();
                break;
        }
    }

    @Override
    public void OnDisConnected() {
        switch (MENU_POSITION) {
            case Constants.ADD_ATTENDANCE:
                attendanceFragment.onDisconnectedCallback();
                break;
            case Constants.ENROLL_STUDENTS:
                enrollFragment.onDisconnectedCallback();
                break;
        }
    }

    public synchronized void capture(int timeout, final TextView txtInfo) {

        NBioBSPJNI.FIR_HANDLE hCapturedFIR, hAuditFIR;
        NBioBSPJNI.CAPTURED_DATA capturedData;

        hCapturedFIR = bsp.new FIR_HANDLE();
        hAuditFIR = bsp.new FIR_HANDLE();
        capturedData = bsp.new CAPTURED_DATA();
        final NBioBSPJNI.FIR_TEXTENCODE textSavedFIR = bsp.new FIR_TEXTENCODE();
        bsp.Capture(NBioBSPJNI.FIR_PURPOSE.ENROLL, hCapturedFIR, timeout, hAuditFIR, capturedData, MainActivity.this, 0, null);


        if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
            sampleDialogFragment.dismiss();
        if (bsp.IsErrorOccured()) {
            msg = "Capture Error: " + bsp.GetErrorCode();

        } else {
            NBioBSPJNI.INPUT_FIR inputFIR;
            inputFIR = bsp.new INPUT_FIR();

            NBioBSPJNI.Export.AUDIT exportAudit;
            inputFIR.SetFIRHandle(hAuditFIR);
            exportAudit = exportEngine.new AUDIT();

            exportEngine.ExportAudit(inputFIR, exportAudit);
            if (bsp.IsErrorOccured()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        txtInfo.setText("Capture Error: " + bsp.GetErrorCode());
                    }
                });
                return;
            }

            if (byCapturedRaw1 != null)
                byCapturedRaw1 = null;

            byCapturedRaw1 = new byte[exportAudit.FingerData[0].Template[0].Data.length];
            byCapturedRaw1 = exportAudit.FingerData[0].Template[0].Data;
            bsp.GetTextFIRFromHandle(hCapturedFIR, textSavedFIR);
            setTextSavedFIR(textSavedFIR);
            msg = "Capture Success";

        }

        runOnUiThread(new Runnable() {
            public void run() {
                txtInfo.setText(msg);
            }
        });

    }

    public void openDevice() {
        if (!isDeviceOpen) {
            sampleDialogFragment.show(getFragmentManager(), "DIALOG_TYPE_PROGRESS");
            bsp.OpenDevice();
            isDeviceOpen = true;
            Log.d(TAG, "Device Open Success");
        } else {
            OnConnected();
        }

    }

    public NBioBSPJNI.FIR_TEXTENCODE getTextSavedFIR() {
        return textSavedFIR;
    }

    public void setTextSavedFIR(NBioBSPJNI.FIR_TEXTENCODE textSavedFIR) {
        this.textSavedFIR = textSavedFIR;
    }

    public byte[] getByCapturedRaw1() {
        return byCapturedRaw1;
    }


    public void setByCapturedRaw1(byte[] byCapturedRaw1) {
        this.byCapturedRaw1 = byCapturedRaw1;
    }
}
