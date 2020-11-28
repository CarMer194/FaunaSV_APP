package com.mdpustudio.faunasv;

import com.mdpustudio.faunasv.models.Avistamiento;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Avistamiento> selected = new MutableLiveData<Avistamiento>();

    public void select(Avistamiento item){
        selected.setValue(item);
    }

    public LiveData<Avistamiento> getSelected(){
        return selected;
    }

}
