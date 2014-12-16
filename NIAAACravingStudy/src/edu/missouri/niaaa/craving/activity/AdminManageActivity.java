/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.missouri.niaaa.craving.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
@SuppressWarnings("deprecation")
public class AdminManageActivity extends TabActivity {

	private TabHost tabHost;
	private String TAG = "TAG~~~~~~~~~~~~~~~~~~~";
	String AsIdHint;
	String RmIdHint;
	String currentAssID;
	SharedPreferences shp;
	private Editor editor;
	EditText asID;
    EditText deasID;
    Button AssignButton;
    Button RemoveButton;
    Context ctx;
    InputMethodManager imm;
    EditText adminPin;
    TextView alert_text;
    Dialog DialadminPin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate!!~~~");

        ctx = this;
        // Setup the window
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        ////////////////////////////////////////////////////////////////////

        tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.activity_admin_manage, tabHost.getTabContentView(), true);
        tabHost.addTab(tabHost.newTabSpec("Assign ID").setIndicator("Assign ID", null).setContent(R.id.tab_assign));
        tabHost.addTab(tabHost.newTabSpec("Remove ID").setIndicator("Remove ID", null).setContent(R.id.tab_logoff));

        setContentView(tabHost);


        shp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
        editor = shp.edit();
        asID = (EditText) findViewById(R.id.assigned_ID);
        deasID = (EditText) findViewById(R.id.deassigned_ID);
        AssignButton = (Button) findViewById(R.id.btn_assign);
        RemoveButton = (Button) findViewById(R.id.btn_remove);


        imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);

        //imm.showSoftInput(asID, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, InputMethodManager.RESULT_HIDDEN);

        asID.setFocusable(true);
        asID.setFocusableInTouchMode(true);

        asID.requestFocus();

        setListeners();

        DialadminPin = AdminPinSetDialog(this);
        DialadminPin.show();

        setHints();
    }

	private Dialog AdminPinSetDialog(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View textEntryView = inflater.inflate(R.layout.pin_input, null);
		TextView pinText = (TextView) textEntryView.findViewById(R.id.pin_text);
		pinText.setText(R.string.admin_set_msg);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle(R.string.admin_set_title);
		builder.setView(textEntryView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText pinEdite = (EditText) textEntryView.findViewById(R.id.pin_edit);
				String pinStr = pinEdite.getText().toString();
				Utilities.Log("Pin Dialog", "pin String is "+pinStr);

				String data = null;
				try {
					data = Utilities.encryption("0000" + "," + "1" + "," + pinStr);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

/*				check network*/

/*				prepare params for server*/
				HttpPost request = new HttpPost(Utilities.VALIDATE_ADDRESS);

 		        List<NameValuePair> params = new ArrayList<NameValuePair>();

				params.add(new BasicNameValuePair("data", data));

// 		        //file_name
// 		        params.add(new BasicNameValuePair("userID","0000"));
// 		        //function
// 		        params.add(new BasicNameValuePair("pre","1"));
// 		        //data
// 		        params.add(new BasicNameValuePair("password",pinStr));

/*				check identity*/

 		        try {
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

					HttpResponse response = new DefaultHttpClient().execute(request);
					if(response.getStatusLine().getStatusCode() == 200){
						String result = EntityUtils.toString(response.getEntity());
						Log.d("~~~~~~~~~~http post result",result);

						if (result.equals("AdminIsChecked")) {
							//do nothing

						} else if (result.equals("AdminPinIsInvalid")) {

							imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
							imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

							Toast.makeText(getApplicationContext(), R.string.input_apin_failed, Toast.LENGTH_SHORT).show();
							finish();
						} else {

							imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
							imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

							Toast.makeText(getApplicationContext(), R.string.input_apin_error, Toast.LENGTH_SHORT).show();
							finish();
						}
					}
					else{
						Toast.makeText(getApplicationContext(), R.string.input_apin_return, Toast.LENGTH_SHORT).show();
						finish();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

 		        	imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
 		        	imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

 		        	Toast.makeText(getApplicationContext(), R.string.input_apin_net_error, Toast.LENGTH_SHORT).show();;
	            	finish();
				}

			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int whichButton) {

		    	imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
		    	imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                finish();
		    }
		});

		return builder.create();
	}


    private void setHints() {
		// TODO Auto-generated method stub

    	asID.setText("");
		currentAssID = shp.getString(Utilities.SP_KEY_LOGIN_USERID, "");
		Log.d(TAG, "set Hints is "+shp.getString(Utilities.SP_KEY_LOGIN_USERID,""));
        if(currentAssID.equals("")){
        	AsIdHint = getString(R.string.assign_hint);
        	RmIdHint = getString(R.string.remove_hint);
        }
        else{
        	AsIdHint = "Current " + shp.getString(Utilities.SP_KEY_LOGIN_USERID, "");
        	RmIdHint = shp.getString(Utilities.SP_KEY_LOGIN_USERID, "");
        }

        asID.setHint(AsIdHint);
        deasID.setHint(RmIdHint);
	}


    private void setListeners() {
		// TODO Auto-generated method stub
    	Log.d(TAG, "Ontabchangedlistener!!~~~");
    	 tabHost.setOnTabChangedListener(new OnTabChangeListener(){



 			@Override
 			public void onTabChanged(String arg0) {
 				// TODO Auto-generated method stub
 				Log.d(TAG,"~~"+arg0);


 				setHints();

 				if(arg0.equals("Assign ID")){
 					imm.toggleSoftInput(0, InputMethodManager.RESULT_HIDDEN);

 					Log.d(TAG ,"assign id ");


 				}else{

 					imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
 					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

 					Log.d(TAG,"deassign id");

 				}
 			}


         });


         AssignButton.setOnClickListener(new View.OnClickListener() {

 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Log.d(TAG ,"assign btn "+asID.getText().toString());

 				//editor.putString(ASID, asID.getText().toString());
 				//format check

 				//editor.putString(ASPWD, "");
 				//editor.commit();
 				//setHints();

/*				check network*/

/*				prepare params for server*/
            	String asedID = asID.getText().toString();
            	Log.d(TAG, "get from edittext is "+asedID);

            	String data = null;
				try {
					data = Utilities.encryption(asedID + "," + "2");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

            	HttpPost request = new HttpPost(Utilities.VALIDATE_ADDRESS);

 		        List<NameValuePair> params = new ArrayList<NameValuePair>();

				params.add(new BasicNameValuePair("data", data));

				// 		        //file_name
				// 		        params.add(new BasicNameValuePair("userID",asedID));
				// 		        //function
				// 		        params.add(new BasicNameValuePair("pre","2"));
				// 		        //data
				// 		        //params.add(new BasicNameValuePair("password",""));

/*				check identity*/

 		        try {
 					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

	 		        HttpResponse response = new DefaultHttpClient().execute(request);
	 		        if(response.getStatusLine().getStatusCode() == 200){
	 		            String result = EntityUtils.toString(response.getEntity());
	 		            Log.d("~~~~~~~~~~http post result2 ",result);

	 		            if(result.equals("UserIDIsNotSet")){
	 		            	//add in web page first

	 		            	String s1 = getString(R.string.assign_id_null);
	 		            	assignFailDialog(ctx, s1).show();

	 		            }else if(result.equals("UserIDIsUsed")){
	 		            	String s2 = getString(R.string.assing_id_exist)+asedID;
	 		            	assignConfirmDialog(ctx, s2, true).show();

	 		            }else if(result.equals("UserIDIsNotActive")){
	 		            	//assign
	 		            	String s3 = getString(R.string.assign_id_new)+asedID;
	 		            	assignConfirmDialog(ctx, s3, false).show();

	 		            }else{
	 		            	String s4 = getString(R.string.assign_id_wrong);
							assignFailDialog(ctx, s4).show();

	 		            }

	 		        }
 		        } catch (Exception e) {
 					// TODO Auto-generated catch block
 		        	e.printStackTrace();
 					String s4 = getString(R.string.assign_id_net_error);
 					assignFailDialog(ctx, s4).show();
 		        }
 			}
         });



         RemoveButton.setOnClickListener(new View.OnClickListener() {

 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Log.d(TAG ,"remove btn ");

 				//add a confirm dialog

 				setHints();
 				Log.d(TAG,"cur is "+currentAssID);

 				if(!currentAssID.equals("")){
 					removeDialog(ctx).show();
 				}
 			}
 		});
	}


    private void cleanUp(Context context){
    	Utilities.getSP(context, Utilities.SP_BED_TIME).edit().clear().commit();
    	Utilities.getSP(context, Utilities.SP_RANDOM_TIME).edit().clear().commit();
    	Utilities.getSP(context, Utilities.SP_SURVEY).edit().clear().commit();
    	Utilities.getSP(context, Utilities.SP_LOGIN).edit().clear().commit();
    }

    private Dialog removeDialog(Context context){

		LayoutInflater inflater = LayoutInflater.from(context);
		final View textEntryView = inflater.inflate(R.layout.remove_id, null);
		final CheckBox rm_check = (CheckBox) textEntryView.findViewById(R.id.rm_local);
		rm_check.setText(R.string.remove_local);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(textEntryView);
		builder.setCancelable(false);
		builder.setTitle(R.string.assign_remove_title);
		builder.setMessage(R.string.remove_msg);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            	cleanUp(ctx);//replace following

//                 	editor.putString(Utilities.SP_KEY_LOGIN_USERID, "");
//     				editor.putString(Utilities.SP_KEY_LOGIN_USERPWD, "");
//     				editor.putString(Utilities.SP_KEY_LOGIN_STUDY_STARTTIME, "");
//     				editor.commit();

				// remove local file, if checked
            	Log.d(TAG, "is checked "+rm_check.isChecked());
            	if(rm_check.isChecked()){
            		deleteDirectory(Utilities.PHONE_BASE_PATH);
            	}

				setHints();
				finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

           @Override
           public void onClick(DialogInterface dialog, int which) {
               // TODO Auto-generated method stub

           }
        });

		return builder.create();

    }

    public void deleteDirectory(String dest)
    {
        File f = new File(dest);
        if (f.exists())
        {
            if (f.isDirectory())
            {
                File[] fs = f.listFiles();
                if (fs.length > 0)
                {
                    for (File file : fs)
                    {
                        deleteDirectory(file.getAbsolutePath());
                    }
                }
            }
            f.delete();
        }
    }

    private Dialog assignFailDialog(Context context, String str) {
        AlertDialog.Builder builder = new  AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.assign_confirm_title);
        builder.setMessage(str);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	@Override
			public void onClick(DialogInterface dialog, int whichButton) {
        		setHints();
        	}
        });
        return builder.create();
    }

    private Dialog assignConfirmDialog(Context context, String str, boolean startNewWeek) {

    	LayoutInflater inflater = LayoutInflater.from(context);
		final View textEntryView = inflater.inflate(R.layout.remove_id, null);
		final CheckBox rm_check = (CheckBox) textEntryView.findViewById(R.id.rm_local);
		rm_check.setText(R.string.assign_new_week);
    	AlertDialog.Builder builder = new  AlertDialog.Builder(context);
    	if(startNewWeek){
    		builder.setView(textEntryView);
    	}
        builder.setCancelable(false);
        builder.setTitle(R.string.assign_confirm_title);
        builder.setMessage(str);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				editor.putString(Utilities.SP_KEY_LOGIN_USERID, asID.getText().toString());
				Log.d("here!!!", "id is "+asID.getText().toString());
				//format check

				editor.putString(Utilities.SP_KEY_LOGIN_USERPWD, "");
				editor.putString(Utilities.SP_KEY_LOGIN_STUDY_STARTTIME, ""+Calendar.getInstance().getTimeInMillis());
				editor.commit();

				//start new study week, if checked
				if(rm_check.isChecked()){

					String UID = null;
					try {
						UID = Utilities.encryption(asID.getText().toString());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					ChangeStudyWeek changeStudyWeek = new ChangeStudyWeek();
					changeStudyWeek.execute(UID);
				}

				setHints();
				//continue with set user pin (8)
				setResult(Activity.RESULT_OK);
				finish();
			}
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				setHints();
			}
        });
        return builder.create();
    }

	private class ChangeStudyWeek extends AsyncTask<String,Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(String... strings) {
			// TODO Auto-generated method stub
	         String UID=strings[0];
	         HttpPost request = new HttpPost(Utilities.STUDY_DAY_MODIFY_ADDRESS);
	         List<NameValuePair> params = new ArrayList<NameValuePair>();
	         params.add(new BasicNameValuePair("UID",UID));
	         //params.add(new BasicNameValuePair("userID", UID));
	         try {

	             request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	             HttpResponse response = new DefaultHttpClient().execute(request);
	             if(response.getStatusLine().getStatusCode() == 200){
//	                 String result = EntityUtils.toString(response.getEntity());
	             }
	             return true;
	         }
	         catch (Exception e)
	         {
	             e.printStackTrace();
	             return false;
	         }

		}

	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        DialadminPin.dismiss();

        imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
     	imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

}