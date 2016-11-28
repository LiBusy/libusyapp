package com.example.dillonwastrack.libusy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.models.Library;

/**
 * Created by dillonwastrack on 10/31/16.
 */

public class LibraryDetailsFragment extends Fragment {

    private Library library;

    private Activity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        if(arguments.containsKey("library"))
        {
            this.library = getArguments().getParcelable("library");
            mainActivity.setTitle(this.library.libraryName);
        }

        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        return inflater.inflate(R.layout.library_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final WebView myWebView = (WebView) mainActivity.findViewById(R.id.library_web_page);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.setInitialScale((int) (myWebView.getScaleX()));
        myWebView.getSettings().setJavaScriptEnabled(true);

        final ProgressBar Pbar;
        Pbar = (ProgressBar) mainActivity.findViewById(R.id.pB1);

        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress < 100 && Pbar.getVisibility() == ProgressBar.GONE){
                    Pbar.setVisibility(ProgressBar.VISIBLE);
                }

                Pbar.setProgress(progress);
                if(progress == 100) {
                    Pbar.setVisibility(ProgressBar.GONE);
                }
            }
        });

        Bundle arguments = getArguments();
        String libraryUrl;
        if(arguments.containsKey("library_url"))
        {
             libraryUrl = arguments.getString("library_url");
        }
        else
        {
             libraryUrl = "https://www.lib.ua.edu/libraries/"+this.library.libraryId+"/";
        }
        myWebView.loadUrl(libraryUrl);


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mainActivity =(Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = activity;
    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return mainActivity.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }
}
