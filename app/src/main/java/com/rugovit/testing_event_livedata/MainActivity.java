package com.rugovit.testing_event_livedata;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;

import com.rugovit.testing_event_livedata.databinding.MainActivityBinding;
import com.rugovit.testing_event_livedata.ui.FragmentOne;
import com.rugovit.testing_event_livedata.ui.FragmentTwo;
import com.rugovit.testing_event_livedata.ui.main.MainFragment;
import com.rugovit.testing_event_livedata.ui.main.MainViewModel;

public class MainActivity extends AppCompatActivity {
    MainActivityBinding mainActivityBinding;
    MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
        mainActivityBinding.fragmentOne.setOnClickListener(v -> MainActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, FragmentOne.newInstance())
                .addToBackStack(null)
                .commit());
        mainActivityBinding.fragmentTwo.setOnClickListener(v -> MainActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, FragmentTwo.newInstance())
                .addToBackStack(null)
                .commit());
        mainActivityBinding.setViewModel(mainViewModel);

        mainViewModel.getEvent1EventLiveData().observe(this, o -> {
            Log.d("TEST_EVENT","MainActivity ==>observer EventLiveData data: "+mainViewModel.getEvent1EventLiveData().getValue());
        });
        mainViewModel.getEvent1LiveData().observe(this, o -> {
            Log.d("TEST_EVENT","MainActivity observer LiveData data: "+mainViewModel.getEvent1LiveData().getValue());
        });
        mainViewModel.getEvent1LiveData().observeForever( o -> {
            Log.d("TEST_EVENT","MainActivity observer (((Forever))) LiveData data: "+mainViewModel.getEvent1LiveData().getValue());
        });
        mainViewModel.getEvent1EventLiveData().observeForever( o -> {
            Log.d("TEST_EVENT","MainActivity ==>observer (((Forever))) EventLiveData data: "+mainViewModel.getEvent1EventLiveData().getValue());
        });
    }
}
