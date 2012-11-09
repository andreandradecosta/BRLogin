package com.andrecosta.weblogin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

    public static final String LOG_TAG = "com.andrecosta.weblogin";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNetStatus();
        ((TextView) findViewById(R.id.textViewUsername)).setText(SettingsUtil.getUser(this));
        if (!"".equals(SettingsUtil.getPassword(this))) {
            ((TextView) findViewById(R.id.textViewPassword)).setText("**********");
        }
        if (!SettingsUtil.isSettingsDefined(this)) {
            showDialog(0);
        }
        ((TextView) findViewById(R.id.textViewStat)).setText("" + SettingsUtil.getStat(this));
    }

    protected void setNetStatus() {
        ((CheckBox) findViewById(R.id.checkBoxWIFI)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkBoxInternet)).setChecked(false);
        
        final ConnectionHelper connectionHelper = new ConnectionHelper(this);
        ((CheckBox) findViewById(R.id.checkBoxWIFI)).setChecked(connectionHelper.isConnectedToKnownWifi());
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return connectionHelper.isNetConnectionPossible();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                ((CheckBox) MainActivity.this.findViewById(R.id.checkBoxInternet)).setChecked(result);
            }

        }.execute((Void) null);
    }

    public void showAuthDialog(View paramView) {
        showDialog(0);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_text_entry, null);
        final EditText editUser = (EditText) dialogView.findViewById(R.authDialog.username_edit);
        final EditText editPass = (EditText) dialogView.findViewById(R.authDialog.password_edit);
        editUser.setText(SettingsUtil.getUser(this));
        editPass.setText(SettingsUtil.getPassword(this));
        return new AlertDialog.Builder(this).setTitle(R.string.authcategory_title).setView(dialogView)
                .setPositiveButton(R.string.okButton_title, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        MainActivity.this.dialogConfirmed(editUser.getText().toString(), editPass.getText().toString());
                    }
                }).create();
    }

    private void dialogConfirmed(String username, String password) {
        ((TextView) findViewById(R.id.textViewUsername)).setText(username);
        ((TextView) findViewById(R.id.textViewPassword)).setText("**********");
        SettingsUtil.setUser(this, username);
        SettingsUtil.setPassword(this, password);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                ConnectionHelper connMan = new ConnectionHelper(MainActivity.this);
                try {
                    connMan.authenticate();
                    return connMan.isNetConnectionPossible();
                } catch (Exception e) {}
                return false;
            }
            
            @Override
            protected void onPostExecute(Boolean result) {
                ((CheckBox) MainActivity.this.findViewById(R.id.checkBoxInternet)).setChecked(result);
            }
        }.execute((Void)null);
    }
}