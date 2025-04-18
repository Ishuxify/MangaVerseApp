package com.example.mangaverseapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FaceViewModel extends ViewModel {
    private final MutableLiveData<Float> currentThreshold = new MutableLiveData<>(0.5f);
    private final MutableLiveData<Integer> currentDelegate = new MutableLiveData<>(FaceFragment.DELEGATE_CPU);

    public void setThreshold(float threshold) {
        currentThreshold.setValue(threshold);
    }

    public void setDelegate(int delegate) {
        currentDelegate.setValue(delegate);
    }

    public Float getCurrentThreshold() {
        return currentThreshold.getValue() != null ? currentThreshold.getValue() : 0.5f;
    }

    public Integer getCurrentDelegate() {
        return currentDelegate.getValue() != null ? currentDelegate.getValue() : FaceFragment.DELEGATE_CPU;
    }

    public LiveData<Float> getThreshold() {
        return currentThreshold;
    }

    public LiveData<Integer> getDelegate() {
        return currentDelegate;
    }
}