package com.bio.attendance.fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bio.attendance.MainActivity;
import com.bio.attendance.R;
import com.bio.attendance.utils.Constants;
import com.bio.attendance.utils.ServiceUtil;
import com.bio.attendance.utils.Student;
import com.bio.attendance.utils.StudentAdapter;
import com.bio.attendance.utils.Subject;
import com.bio.attendance.utils.SubjectAdapter;
import com.bio.attendance.utils.Utils;
import com.nitgen.SDK.AndroidBSP.NBioBSPJNI;
import com.rey.material.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AttendanceFragment.class.getName();

    private NBioBSPJNI bsp;
    private DialogFragment sampleDialogFragment;
    Activity caller;
    Button OnBtnOpenDevice;
    Button btnCapture;
    TextView txtVersion, txtDevice, txtInfo;
    ImageView img_fp_src;
    private Context context;
    private static final String OPEN_DEVICE = "openDevice";
    private static final String CAPTURE = "capture";
    Spinner subjectSpinner;
    Spinner studentSpinner;
    public static final int QUALITY_LIMIT = 80;
    ProgressDialog dialog = null;
    private int serverResponseCode = 0;
    private static String textCode;

    public AttendanceFragment() {
        // Required empty public constructor
    }


    public static AttendanceFragment newInstance(Activity caller, DialogFragment sampleDialogFragment, NBioBSPJNI bsp) {

        AttendanceFragment fragment = new AttendanceFragment();
        fragment.sampleDialogFragment = sampleDialogFragment;
        fragment.bsp = bsp;
        fragment.caller = caller;
        fragment.context = caller;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_attendance, container, false);
        OnBtnOpenDevice = (Button) inflate.findViewById(R.id.btnOpenDevice);
        OnBtnOpenDevice.setOnClickListener(this);
        OnBtnOpenDevice.setTag(OPEN_DEVICE);

        btnCapture = (Button) inflate.findViewById(R.id.btnCapture);
        btnCapture.setEnabled(false);
        btnCapture.setTag(CAPTURE);
        btnCapture.setOnClickListener(this);

        txtVersion = (TextView) inflate.findViewById(R.id.textVer);
        txtVersion.setText("Version " + bsp.GetVersion());
        txtDevice = (TextView) inflate.findViewById(R.id.textDevice);
        txtInfo = (TextView) inflate.findViewById(R.id.textInfo);
        img_fp_src = (ImageView) inflate.findViewById(R.id.img_fp_src);

        // Inflate the layout for this fragment

        subjectSpinner = (Spinner) inflate.findViewById(R.id.subjectSpinner);
        studentSpinner = (Spinner) inflate.findViewById(R.id.studentSpinner);

        String[] branchItem = {"Java", "Android", "Embedded"};
        String[] yearItem = {"Rizwan Ahmad Khan", "Maheen Khan", "Student 3"};


        //Initialize Spinner Adapters
        initializeSpinner();
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Subject item = (Subject) adapterView.getItemAtPosition(i);
                Toast.makeText(adapterView.getContext(), "Subject: " + item.getName(), Toast.LENGTH_LONG).show();
                initializeStudentSpinner(String.valueOf(item.getId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Student item = (Student) adapterView.getItemAtPosition(i);
                Toast.makeText(adapterView.getContext(), "Student: " + item.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return inflate;
    }

    private void initializeSpinner() {
        try {
            SharedPreferences sharedPreferences = caller.getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
            String serverPort = sharedPreferences.getString(Constants.SERVER_PORT, "8080");
            String serverIP = sharedPreferences.getString(Constants.SERVER_IP, "127.0.0.10");
            if (serverIP.equalsIgnoreCase("127.0.0.10")) {
                Utils.showAlertDialog(context, getString(R.string.app_name), "Invalid Server Ip Address. Please Update The Ip");
                return;
            }
            String serviceURL = Constants.URL_PREFIX + serverIP + ":" + serverPort + getString(R.string.baseUrl) + getString(R.string.getSubjectList);

            SubjectAdapter subjectAdapter;
            ServiceUtil serviceUtil = new ServiceUtil();
            serviceUtil.execute("subjectList", serviceURL);
            JSONObject jsonObject = serviceUtil.get();

            ArrayList<Subject> subjectList;
            if (jsonObject.has("data")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");
                subjectList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject subjectObject = jsonArray.getJSONObject(i);
                    Subject subject = new Subject(subjectObject.getLong("id"), subjectObject.getString("name"));
                    subjectList.add(subject);
                }
                subjectAdapter = new SubjectAdapter(caller, R.layout.subject_spinner_item, subjectList);
                subjectSpinner.setAdapter(subjectAdapter);

            }


        } catch (Exception e) {
            Log.e(TAG, "Error While Getting subject list" + e);
        }
    }

    private void initializeStudentSpinner(String subjectID) {
        try {

            studentSpinner.setAdapter(null);
            SharedPreferences sharedPreferences = caller.getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
            String serverPort = sharedPreferences.getString(Constants.SERVER_PORT, "8080");
            String serverIP = sharedPreferences.getString(Constants.SERVER_IP, "127.0.0.10");
            if (serverIP.equalsIgnoreCase("127.0.0.10")) {
                Utils.showAlertDialog(context, getString(R.string.app_name), "Invalid Server Ip Address. Please Update The Ip");
                return;
            }
            String serviceURL = Constants.URL_PREFIX + serverIP + ":" + serverPort + getString(R.string.baseUrl) + getString(R.string.getStudentBySubjectID);

            StudentAdapter subjectAdapter;
            ServiceUtil serviceUtil = new ServiceUtil();
            serviceUtil.execute("studentList", serviceURL, subjectID);
            JSONObject jsonObject = serviceUtil.get();

            ArrayList<Student> studentArrayList;
            if (jsonObject.has("data")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");
                studentArrayList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject subjectObject = jsonArray.getJSONObject(i);
                    Student subject = new Student(subjectObject.getLong(Constants.ID_KEY), subjectObject.getString(Constants.NAME_KEY), subjectObject.getString(Constants.ROLLNO_KEY)
                            , subjectObject.getString(Constants.YEAR_KEY), subjectObject.getLong(Constants.SUBJECT_KEY), "");
                    studentArrayList.add(subject);
                }
                subjectAdapter = new StudentAdapter(caller, R.layout.student_spinner_item, studentArrayList);
                studentSpinner.setAdapter(subjectAdapter);

            }

        } catch (Exception e) {
            Log.e(TAG, "Error While Getting student list for subject id " + subjectID + "" + e);
        }
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String tag = (String) button.getTag();
        switch (tag) {
            case OPEN_DEVICE:
                ((MainActivity) caller).openDevice();
                break;
            case CAPTURE:
                captureFingerPrint();
                break;
        }

    }


    private void captureFingerPrint() {
        try {
            sampleDialogFragment.show(caller.getFragmentManager(), "DIALOG_TYPE_PROGRESS");
            sampleDialogFragment.setCancelable(false);
            MainActivity activity = (MainActivity) caller;
            activity.capture(10000, txtInfo);

            if (activity.getByCapturedRaw1() != null) {
                textCode = activity.getTextSavedFIR().TextFIR;
                processAttendance();

            } else {
                Log.i(TAG, "No Byte Data Received ");
            }


        } catch (JSONException e) {
            Log.e(TAG, "Error While Capturing Fingerprint " + e);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error While Capturing Fingerprint " + e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Error While Capturing Fingerprint " + e);
        } catch (Exception e) {
            Log.e(TAG, "Error While Capturing Fingerprint " + e);
        }
    }

    private void processAttendance() throws ExecutionException, InterruptedException, JSONException {

        final Subject subject = (Subject) subjectSpinner.getSelectedItem();
        final Student student = (Student) studentSpinner.getSelectedItem();

        Log.i(TAG, "FIR Text" + textCode);
        SharedPreferences sharedPreferences = caller.getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
        String serverPort = sharedPreferences.getString(Constants.SERVER_PORT, "8080");
        String serverIP = sharedPreferences.getString(Constants.SERVER_IP, "127.0.0.10");
        if (serverIP.equalsIgnoreCase("127.0.0.10")) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Invalid Server Ip Address. Please Update The Ip");
            return;
        }
        if (!Utils.isNetworkAvailable(context)) {
            Utils.showAlertDialog(context, getString(R.string.app_name), getString(R.string.network_enabled_msg));
            return;
        }
        String serviceURL = Constants.URL_PREFIX + serverIP + ":" + serverPort + getString(R.string.baseUrl) + getString(R.string.submit_attendance);
        ServiceUtil serviceUtil = new ServiceUtil();
        String subjectID = String.valueOf(subject.getId());
        serviceUtil.execute("addAttendance", serviceURL, subjectID, student.getName(), textCode);
        JSONObject jsonObject = serviceUtil.get();
        if (jsonObject.getBoolean("result")) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Attendances Submitted");
        } else {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Attendances Failed Fingerprint Not Match");
        }

        if (jsonObject.length() == 0) {
            Utils.showAlertDialog(context, getString(R.string.app_name), ServiceUtil.getError());
        }
    }

    //Public Callback Methods
    public void onConnectedCallback() {
        if (sampleDialogFragment != null)
            sampleDialogFragment.dismiss();

        String message = "Device Connected Success";
        txtDevice.setText(message);
        btnCapture.setEnabled(true);
    }

    public void onDisconnectedCallback() {
        NBioBSPJNI.CURRENT_PRODUCT_ID = 0;

        if (sampleDialogFragment != null)
            sampleDialogFragment.dismiss();

        String message = "Device Disconnected: " + bsp.GetErrorCode();
        txtDevice.setText(message);
        btnCapture.setEnabled(false);
    }


    public int OnCapturedCallback(NBioBSPJNI.CAPTURED_DATA capturedData) {
        int defaultValue = NBioBSPJNI.ERROR.NBioAPIERROR_NONE;
        try {

            txtDevice.setText("IMAGE Quality: " + capturedData.getImageQuality());

            if (capturedData.getImage() != null) {

                img_fp_src.setImageBitmap(capturedData.getImage());


            }

            // quality : 40~100
            if (capturedData.getImageQuality() >= QUALITY_LIMIT) {
                if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
                    sampleDialogFragment.dismiss();
                return NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL;
            } else if (capturedData.getDeviceError() != NBioBSPJNI.ERROR.NBioAPIERROR_NONE) {
                if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
                    sampleDialogFragment.dismiss();
                return capturedData.getDeviceError();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error In On Capture Callback " + e);
        }
        return defaultValue;
    }

    public void OnStopCallback(DialogFragment dialogFragment) {
        if (sampleDialogFragment != null)
            sampleDialogFragment.dismiss();
    }

    public void OnPositiveCallback(DialogFragment dialogFragment, String id) {

    }


    private void sendFileToServer(final String firString, final String studentName, String subjectName) {

        try {

            caller.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog = ProgressDialog.show(context, getString(R.string.app_name), "Uploading attendances of " + studentName, true);
                }
            });

            SharedPreferences sharedPreferences = caller.getSharedPreferences(Constants.APP_PREFERENCE, Activity.MODE_PRIVATE);
            String serverPort = sharedPreferences.getString(Constants.SERVER_PORT, "8080");
            String serverIP = sharedPreferences.getString(Constants.SERVER_IP, "127.0.0.10");
            String serviceURL = Constants.URL_PREFIX + serverIP + ":" + serverPort + getString(R.string.baseUrl) + getString(R.string.submit_attendance);

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(firString);
            final String fileName = sourceFile.getName();
            JSONObject jsonObject;

            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(serviceURL);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("subjectName", subjectName);
            conn.setRequestProperty("studentName", studentName);
            conn.setRequestProperty("fileName", fileName);

            dos = new DataOutputStream(conn.getOutputStream());


            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            fileInputStream.close();
            dos.flush();


            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            Log.d(TAG, "Output from Server .... \n");
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                Log.d(TAG, "Service Result : " + line);
            }
            line = stringBuilder.toString();
            jsonObject = new JSONObject(line);
            Log.i(TAG, "HTTP Response is : "
                    + line + ": " + serverResponseCode);

            if (jsonObject != null && jsonObject.length() != 0) {
                final boolean result = jsonObject.getBoolean("success");
                final String response = jsonObject.getString("result");
                caller.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, response, Toast.LENGTH_LONG);
                    }
                });

            } else {
                Log.d(TAG, "Unknown Error While  Uploading File :");
            }
            //close the streams //
            caller.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });

            dos.flush();
            dos.close();

        } catch (JSONException e) {
            Log.e(TAG, "Error While  Uploading File : " + e);
        } catch (MalformedURLException e) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Please Check IP/Port Or Make Sure " + getString(R.string.app_name) + " Server Is Running");
            Log.e(TAG, "Error While  Uploading File : " + e);

        } catch (ConnectException e) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Please Check IP/Port Or Make Sure " + getString(R.string.app_name) + " Server Is Running or You have a working internet connection.");
            Log.e(TAG, "Error While  Uploading File : " + e);

        } catch (NullPointerException e) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Please Check IP/Port Or Make Sure " + getString(R.string.app_name) + " Server Is Running");
            Log.e(TAG, "Error While  Uploading File : " + e);

        } catch (IOException e) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Please Check IP/Port Or Make Sure " + getString(R.string.app_name) + " Server Is Running");
            Log.e(TAG, "Error While  Uploading File : " + e);

        } catch (Exception e) {
            Utils.showAlertDialog(context, getString(R.string.app_name), "Error While Uploading File. Please Check File Is Exist or You have a working internet connection.");
            Log.e(TAG, "Error While  Uploading File : " + e);

        }
    }

    private boolean saveFIR(Bitmap bitmap, File filePath) {
        FileOutputStream fos = null;
        boolean isSaved = false;
        try {

            if (!filePath.exists()) {
                filePath.delete();
                Log.i(TAG, "Existing File Deleted " + filePath.getName());
            }
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Log.i(TAG, "Finger Print File Saved " + filePath.getName());

            isSaved = true;

        } catch (Exception e) {
            Log.e(TAG, "Error while saving bitmap to internal application folder  " + e);
        }
        return isSaved;
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
