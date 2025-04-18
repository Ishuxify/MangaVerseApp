package com.example.mangaverseapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mangaverseapp.data.model.Manga;
import com.example.mangaverseapp.data.repository.MangaRepositoryImpl;
import com.example.mangaverseapp.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final MangaRepositoryImpl mangaRepository;
    private final MutableLiveData<List<Manga>> mangaListLiveData;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mangaRepository = new MangaRepositoryImpl(application);
        mangaListLiveData = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<List<Manga>> getMangaList() {
        return mangaListLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchMangaList(int currentPage, int pageSize) {
        if (isLoading.getValue() != null && isLoading.getValue()) {
            return;
        }

        isLoading.setValue(true);

        if (NetworkUtils.isNetworkAvailable(getApplication())) {
            fetchFromApi(currentPage, pageSize);
        } else {
            fetchFromRoom();
        }
    }

    private void fetchFromApi(int currentPage, int pageSize) {
        mangaRepository.fetchMangaFromApi(currentPage, pageSize, "", false, "all", new MangaRepositoryImpl.MangaDataCallback() {
            @Override
            public void onDataLoaded(List<Manga> mangaList) {
                List<Manga> currentList = mangaListLiveData.getValue() != null ? new ArrayList<>(mangaListLiveData.getValue()) : new ArrayList<>();
                if (currentPage == 1) {
                    currentList.clear();
                }
                currentList.addAll(mangaList);
                mangaListLiveData.postValue(currentList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                fetchFromRoom();
            }
        });
    }

    private void fetchFromRoom() {
        LiveData<List<Manga>> roomData = mangaRepository.getAllManga();
        roomData.observeForever(mangaList -> {
            mangaListLiveData.postValue(mangaList != null ? new ArrayList<>(mangaList) : new ArrayList<>());
            if (mangaList == null || mangaList.isEmpty()) {
                errorMessage.postValue("No offline data available");
            }
            isLoading.postValue(false);
            roomData.removeObserver(observer -> {}); // Clean up
        });
    }
}