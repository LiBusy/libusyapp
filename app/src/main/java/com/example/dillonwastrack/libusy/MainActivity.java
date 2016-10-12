package com.example.dillonwastrack.libusy;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

    private BottomBar mBottomBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                FragmentManager fm = getFragmentManager();
                if (tabId == R.id.tab_map) {
                    fm.beginTransaction().replace(R.id.contentContainer, new MapViewFragment()).commit();
                }
                else if (tabId == R.id.tab_list){
                    fm.beginTransaction().replace(R.id.contentContainer, new ListViewFragment()).commit();
                }
            }
        });
    }

}
