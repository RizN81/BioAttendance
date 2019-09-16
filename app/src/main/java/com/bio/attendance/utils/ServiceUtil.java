package com.bio.attendance.utils;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Riz on 1/15/2016.
 */
public class ServiceUtil extends android.os.AsyncTask<String, Void, JSONObject> {
    private static final String TAG = "ServiceUtil";
    private Context context;
    private static String error = "";
    private JSONObject jsonObject;

    public ServiceUtil() {

    }

    public ServiceUtil(Context context) {

        this.context = context;
    }

    public static String getError() {

        return error;
    }

    public static void setError(String error) {

        ServiceUtil.error = error;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        String serviceType = params[0];
        if (serviceType.equalsIgnoreCase("studentList")) {
            return getStudentList(params[1], params[2]);
        } else if (serviceType.equalsIgnoreCase("submit")) {
            return submitAttendance(params[1], params[2], params[3], params[4], params[5], params[6]);
        } else if (serviceType.equalsIgnoreCase("subjectList")) {
            return getSubjectList(params[1]);
        } else if (serviceType.equalsIgnoreCase("addAttendance")) {
            return addAttendnace(params[1], params[2], params[3], params[4]);
        }


        return null;
    }

    private JSONObject getStudentList(String serviceURL, String subject) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(serviceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write("&subjectID=" + subject);
            writer.flush();
            writer.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String line;
            Log.d(TAG, "Output from Server .... \n");
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            line = stringBuilder.toString();
            jsonObject = new JSONObject(line);
        } catch (MalformedURLException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (IOException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (JSONException e) {
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        }
        return jsonObject;
    }

    private JSONObject getSubjectList(String serviceURL) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(serviceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");


            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String line;
            Log.d(TAG, "Output from Server .... \n");
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            line = stringBuilder.toString();
            jsonObject = new JSONObject(line);
        } catch (MalformedURLException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (IOException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (JSONException e) {
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        }
        return jsonObject;
    }

    private JSONObject addAttendnace(String serviceURL, String subjectID, String studentName, String firString) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(serviceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write("&subjectID=" + subjectID + "&studentName=" + studentName +
                    "&firString=" + firString);
            writer.flush();
            writer.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String line;

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            line = stringBuilder.toString();
            jsonObject = new JSONObject(line);
        } catch (MalformedURLException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (IOException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (JSONException e) {
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        }
        return jsonObject;
    }

    private JSONObject submitAttendance(String serviceURL, String subjectID, String studentName, String rollNo, String year, String firString) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(serviceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write("&subjectID=" + subjectID + "&studentName=" + studentName + "&rollNo=" + rollNo + "&year=" + year + "&firString=" + firString);
            writer.flush();
            writer.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String line;

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            line = stringBuilder.toString();
            jsonObject = new JSONObject(line);
        } catch (MalformedURLException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (IOException e) {
            setError("Please Check IP/Port Or Make Sure  Server Is Running");
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        } catch (JSONException e) {
            jsonObject = new JSONObject();
            Log.e(TAG, "Error [getStudentList] " + e.getMessage());
        }
        return jsonObject;
    }
}
