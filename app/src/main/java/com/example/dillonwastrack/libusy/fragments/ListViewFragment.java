package com.example.dillonwastrack.libusy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;

import com.example.dillonwastrack.libusy.activities.MainActivity;
import com.example.dillonwastrack.libusy.singletons.NetworkManager;
import com.example.dillonwastrack.libusy.R;
import com.example.dillonwastrack.libusy.adapters.LibraryListAdapter;
import com.example.dillonwastrack.libusy.callbacks.LocationCallback;
import com.example.dillonwastrack.libusy.models.Library;

import java.util.ArrayList;


/**
 * Created by dillonwastrack on 10/11/16.
 */

public class ListViewFragment extends Fragment {


    private ArrayList<Library> locations;

    private Activity mainActivity;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View contentView = inflater.inflate(R.layout.fragment_list, container, false);

        NetworkManager.getInstance().readLocationsIntoList(new ArrayList<Library>(), mainActivity, new LocationCallback() {
            @Override
            public void onSuccess(ArrayList<Library> result) {
                locations = result;
                RecyclerView listView = (RecyclerView) contentView.findViewById(R.id.rv);
                LibraryListAdapter mAdapter = new LibraryListAdapter(contentView.getContext(), result);

                mAdapter.notifyDataSetChanged();

                mAdapter.SetOnItemClickListener(new LibraryListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position, String library) {

                        // get Library object based on item clicked
                        Library selectedLibrary = getLibrary(locations, library);

                        // set Library object as argument to LibraryDetailsFragment
                        LibraryDetailsFragment detailsFragment = new LibraryDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("library", selectedLibrary);
                        detailsFragment.setArguments(bundle);

                        // add the fragment
                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction().replace(R.id.contentContainer, detailsFragment)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .addToBackStack(null)
                                .commit();

                    }
                });

                listView.setVisibility(View.VISIBLE);
                listView.setAdapter(mAdapter);
                listView.setHasFixedSize(true);
                listView.setLayoutManager(new LinearLayoutManager(mainActivity));
            }
        });

        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NetworkManager.getInstance().readLocationsIntoList(new ArrayList<Library>(), mainActivity, new LocationCallback() {
                    @Override
                    public void onSuccess(ArrayList<Library> result) {
                        locations = result;
                        RecyclerView listView = (RecyclerView) contentView.findViewById(R.id.rv);
                        LibraryListAdapter mAdapter = new LibraryListAdapter(contentView.getContext(), result);

                        mAdapter.notifyDataSetChanged();

                        mAdapter.SetOnItemClickListener(new LibraryListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position, String library) {

                                // get Library object based on item clicked
                                Library selectedLibrary = getLibrary(locations, library);

                                // set Library object as argument to LibraryDetailsFragment
                                LibraryDetailsFragment detailsFragment = new LibraryDetailsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("library", selectedLibrary);
                                detailsFragment.setArguments(bundle);

                                // add the fragment
                                FragmentManager fm = getFragmentManager();
                                fm.beginTransaction().replace(R.id.contentContainer, detailsFragment)
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                        .addToBackStack(null)
                                        .commit();

                            }
                        });

                        listView.setVisibility(View.VISIBLE);
                        listView.setAdapter(mAdapter);
                        listView.setHasFixedSize(true);
                        listView.setLayoutManager(new LinearLayoutManager(mainActivity));
                        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipeRefreshLayout);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        });

        return contentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Log.d("activity", "activity");
        if (context instanceof Activity){
            mainActivity = (Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = activity;
    }

    public Library getLibrary(ArrayList<Library> libraries, String key)
    {
        for (Library library : libraries)
        {
            if (library.libraryName.equals(key))
            {
                return library;
            }
        }

        return libraries.get(0); // TODO should probably handle this error
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        ProgressBar mProgressBar = (ProgressBar) mainActivity.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) mainActivity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(mainActivity.getComponentName()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.check_in:
                MainActivity activity = (MainActivity) mainActivity;
                activity.checkIn();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.setTitle(R.string.app_name);
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
    }
}
