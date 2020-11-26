package com.mdpustudio.faunasv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.mdpustudio.faunasv.ui.dashboard.DashboardViewModel;

public class ShowListFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    String[] filters = {"Ningun filtro seleccionado","Distancia","Animal","Especie","Custom"};
    LinearLayout distance;
    LinearLayout animal;
    LinearLayout species;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_showlist, container, false);

        Spinner filterSpinner = (Spinner)root.findViewById(R.id.filer_spinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(root.getContext(), R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(this);

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        distance = getView().findViewById(R.id.filterDistanceTextView);
        animal = getView().findViewById(R.id.filterAnimalTextView);
        species = getView().findViewById(R.id.filterSpeciesTextView);
        switch (i){
            case 0:
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                species.setVisibility(View.GONE);
                species.animate().translationX(species.getWidth());
                break;
            case 1:
                distance.setVisibility(View.VISIBLE);
                distance.animate().translationX(0);
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                species.setVisibility(View.GONE);
                species.animate().translationX(species.getWidth());
                break;
            case 2:
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.VISIBLE);
                animal.animate().translationX(0);
                species.setVisibility(View.GONE);
                species.animate().translationX(species.getWidth());
                break;
            case 3:
                distance.setVisibility(View.GONE);
                distance.animate().translationX(distance.getWidth());
                animal.setVisibility(View.GONE);
                animal.animate().translationX(animal.getWidth());
                species.setVisibility(View.VISIBLE);
                species.animate().translationX(0);
                break;
            case 4:
                distance.setVisibility(View.VISIBLE);
                distance.animate().translationX(0);
                animal.setVisibility(View.VISIBLE);
                animal.animate().translationX(0);
                species.setVisibility(View.VISIBLE);
                species.animate().translationX(0);
                break;
            default:

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}