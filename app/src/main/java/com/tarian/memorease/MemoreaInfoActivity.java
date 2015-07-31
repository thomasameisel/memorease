package com.tarian.memorease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;


public class MemoreaInfoActivity extends AppCompatActivity implements MemoreaDialog.OnSaveMemoreaDialog {
    private static SharedPreferences sSharedPreferences;

    private String mMemoreaId;
    private String[] mMemoreaFields;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_info);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sSharedPreferences = getSharedPreferences(getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
        mMemoreaId = getIntent().getStringExtra(MemoreaListActivity.ID);
        mMemoreaFields = getIntent().getStringArrayExtra(MemoreaListActivity.MEMOREA_INFO);

        MemoreaInfoFragment memoreaInfoFragment = (MemoreaInfoFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_info);
        memoreaInfoFragment.updateFields(mMemoreaFields);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Intent memoreaListIntent = new Intent(this, MemoreaListActivity.class);
        startActivity(memoreaListIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Opens the dialog fragment to edit a memorea
     */
    public void editMemorea(final View view) {
        // dialog to edit memorea
        final Bundle memoreaInfoBundle = new Bundle();
        memoreaInfoBundle.putString(MemoreaListActivity.DIALOG_TITLE, getString(R.string.edit_memorea_title));
        memoreaInfoBundle.putBoolean(MemoreaListActivity.IS_EDITING, true);
        memoreaInfoBundle.putStringArray(MemoreaListActivity.MEMOREA_INFO, mMemoreaFields);

        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(memoreaInfoBundle);
        memoreaDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSaveMemoreaDialog(final String[] fields) {
        for (int i = 0; i < 4; ++i) {
            mMemoreaFields[i] = fields[i];
        }
        MemoreaInfoFragment memoreaInfoFragment = (MemoreaInfoFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_info);
        memoreaInfoFragment.updateFields(mMemoreaFields);
        addMemoreaSharedPref(mMemoreaId, mMemoreaFields);
    }

    private static void addMemoreaSharedPref(final String id, final String[] fields) {
        final SharedPreferences.Editor sharedPreferencesEditor = sSharedPreferences.edit();
        sharedPreferencesEditor.putString(id, getJSONStringFromArray(fields).toString());
        sharedPreferencesEditor.apply();
    }

    /**
     * Generates a JSON array from a String array
     * @param values String array to convert to JSON
     */
    private static JSONArray getJSONStringFromArray(final String[] values) {
        final JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }
}
