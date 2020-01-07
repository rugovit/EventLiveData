package com.rugovit.testing_event_livedata.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rugovit.testing_event_livedata.R;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }


    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d("TEST_LIFECYCLE","==========================================> onAttachFragment()   fragment: "+this.getClass().getName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("TEST_LIFECYCLE","==========================================> onAttach()   fragment: "+this.getClass().getName());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TEST_LIFECYCLE","==========================================> onCreate()   fragment: "+this.getClass().getName());
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("TEST_LIFECYCLE","==========================================> onCreateView()   fragment: "+this.getClass().getName());
        return inflater.inflate(R.layout.main_fragment, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TEST_LIFECYCLE","==========================================> onViewCreated()   fragment: "+this.getClass().getName());
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        Log.d("TEST_LIFECYCLE","==========================================> onActivityCreated()   fragment: "+this.getClass().getName());
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onActivityCreated "+this.getClass().getName());

    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d("TEST_LIFECYCLE","==========================================> onStart()   fragment: "+this.getClass().getName());
        mViewModel.getEvent1EventLiveData().observeInOnStart(this, o -> {
            Log.d("TEST_EVENT","MainFragment observer ==>EventLiveData data: "+mViewModel.getEvent1EventLiveData().getValue());
        });
        mViewModel.getEvent1LiveData().observe(this, o -> {
            Log.d("TEST_EVENT","MainFragment observer LiveData data: "+mViewModel.getEvent1LiveData().getValue());
        });
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onStart() "+this.getClass().getName());

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("TEST_LIFECYCLE","==========================================> onResume()   fragment: "+this.getClass().getName());
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onResume() "+this.getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TEST_LIFECYCLE","==========================================> onPause()   fragment: "+this.getClass().getName());
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onPause() "+this.getClass().getName());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("TEST_LIFECYCLE","==========================================> onStop()   fragment: "+this.getClass().getName());
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onStop() "+this.getClass().getName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("TEST_LIFECYCLE","==========================================> onDestroyView()   fragment: "+this.getClass().getName());
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onDestroyView() "+this.getClass().getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TEST_LIFECYCLE","==========================================> onDestroy()   fragment: "+this.getClass().getName());
        if(mViewModel.runLifecycleTest) mViewModel.eventProgrammatically("onDestroy() "+this.getClass().getName());
    }
}
