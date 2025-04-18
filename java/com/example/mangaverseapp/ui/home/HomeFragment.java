package com.example.mangaverseapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.mangaverseapp.R;
import com.example.mangaverseapp.adapter.MangaAdapter;
import com.example.mangaverseapp.data.model.Manga;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements MangaAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MangaAdapter mangaAdapter;
    private int currentPage = 1;
    private final int PAGE_SIZE = 20;
    private HomeViewModel homeViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        mangaAdapter = new MangaAdapter(requireContext(), this);
        recyclerView.setAdapter(mangaAdapter);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe manga list
        homeViewModel.getMangaList().observe(getViewLifecycleOwner(), mangaList -> {
            if (mangaList != null) {
                if (currentPage == 1) {
                    mangaAdapter.setMangaList(mangaList);
                } else {
                    mangaAdapter.addMangaList(mangaList);
                }
            }
        });

        // Observe loading state
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && !isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Observe errors
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load initial data
        loadMangaData();

        // Swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            loadMangaData();
        });

        // Pagination scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && homeViewModel.getIsLoading().getValue() != null && !homeViewModel.getIsLoading().getValue()) {
                    loadMangaData();
                }
            }
        });

        return view;
    }

    private void loadMangaData() {
        swipeRefreshLayout.setRefreshing(true);
        homeViewModel.fetchMangaList(currentPage, PAGE_SIZE);
        currentPage++;
    }

    @Override
    public void onItemClick(Manga manga) {
        // Get NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Navigate to MangaDetailFragment with manga object
        Bundle bundle = new Bundle();
        bundle.putParcelable("manga", manga);
        navController.navigate(R.id.action_homeFragment_to_mangaDetailFragment, bundle);
    }


    @Override
    public void onLoadMore() {
        loadMangaData();
    }
}