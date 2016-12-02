package com.libusy.dillonwastrack.libusy.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;

import com.libusy.dillonwastrack.libusy.activities.MainActivity;
import com.libusy.dillonwastrack.libusy.singletons.NetworkManager;
import com.libusy.dillonwastrack.libusy.R;
import com.libusy.dillonwastrack.libusy.adapters.LibraryListAdapter;
import com.libusy.dillonwastrack.libusy.callbacks.LocationCallback;
import com.libusy.dillonwastrack.libusy.models.Library;

import java.util.ArrayList;


/**
 * Created by dillonwastrack on 10/11/16.
 */

public class ListViewFragment extends Fragment {


    private ArrayList<Library> locations;

    private Activity mainActivity;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener;

    private LibraryListAdapter mAdapter;

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
                mAdapter = new LibraryListAdapter(contentView.getContext(), result);
                mAdapter.notifyDataSetChanged();

                ProgressBar mProgressBar = (ProgressBar) mainActivity.findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.GONE);

                mAdapter.SetOnItemClickListener(new LibraryListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position, String library) {

                        // get Library object based on item clicked
                        Library selectedLibrary = getLibrary(locations, library);

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
                        mAdapter = new LibraryListAdapter(contentView.getContext(), result);

                        mAdapter.notifyDataSetChanged();

                        mAdapter.SetOnItemClickListener(new LibraryListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position, String library) {

                                // get Library object based on item clicked
                                Library selectedLibrary = getLibrary(locations, library);

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

        mDrawerLayout = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
        mActivityTitle = mainActivity.getTitle().toString();

        mDrawerToggle = new ActionBarDrawerToggle(mainActivity,
                mDrawerLayout,
                // R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close);

        mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                syncActionBarArrowState();
            }
        };

        setupDrawer();

        NavigationView nav = (NavigationView)mainActivity.findViewById(R.id.navigation_view);
        nav.getMenu().clear();
        nav.inflateMenu(R.menu.list_view_navigation_menu);

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                switch (id) {

                    case R.id.check_in:
                        MainActivity activity = (MainActivity) mainActivity;
                        activity.checkIn();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.available_computers:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.lib.ua.edu/computers/"));
                        mainActivity.startActivity(intent);
                        return true;
                    }


                    case R.id.library_software:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://guides.lib.ua.edu/software"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                    case R.id.find_a_place_to_study:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.lib.ua.edu/using-the-library/find-a-place-to-study/"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                    case R.id.book_a_team_room:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://ua.libcal.com/booking/groupstudy"));
                        mainActivity.startActivity(intent);
                        return true;
                    }

                    case R.id.library_databases:
                    {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://guides.lib.ua.edu/az.php"));
                        mainActivity.startActivity(intent);
                        return true;
                    }


                }
                return false;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Associate searchable configuration with the SearchView
        final SearchManager searchManager =
                (SearchManager) mainActivity.getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(mainActivity.getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                mAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });


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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (mDrawerToggle.isDrawerIndicatorEnabled() &&
                mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == android.R.id.home &&
                getFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mainActivity.setTitle(R.string.app_name);
        ActionBar ab = ((AppCompatActivity) mainActivity).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(mainActivity,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            }

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                syncActionBarArrowState();
            }
        };

        mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(mainActivity));
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        getFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        syncActionBarArrowState();
    }

    private void syncActionBarArrowState() {
        try {
            int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
            mDrawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
        }catch (NullPointerException e){}

    }
}
