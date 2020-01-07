package com.rugovit.testing_event_livedata.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rugovit.eventlivedata.EventLiveData;
import com.rugovit.eventlivedata.MutableEventLiveData;


public class MainViewModel extends ViewModel {

     private MutableLiveData<String>  event1LiveData =new MutableLiveData<>();
     private MutableEventLiveData<String> event1MutableEventLiveData = new MutableEventLiveData<>();
     private int counter=0;
     private int counterProgrammatically =0;
     public  boolean runLifecycleTest=true;
     public void event1Click(){
          Log.d("TEST_EVENT", "" + " " + "**************************************************************************");
          Log.d("TEST_EVENT", "" + " " + "***************************> MainViewModel event1Click() counter "+counter);
          Log.d("TEST_EVENT", "" + " " + "**************************************************************************");
          event1LiveData.setValue("Event 1 counter: "+counter);
           event1MutableEventLiveData.setValue("Event 1 counter: "+counter);
          counter++;
     }
     public void eventProgrammatically(String location){
          Log.d("TEST_EVENT", "" + " " + "**************************************************************************");
          Log.d("TEST_EVENT", "" + " " + "***************************> MainViewModel eventProgrammatically() location "+location+" counter "+counterProgrammatically);
          Log.d("TEST_EVENT", "" + " " + "**************************************************************************");
          event1LiveData.setValue("Event 1 counter: "+counterProgrammatically);
          event1MutableEventLiveData.setValue("Event 1 counter: "+counterProgrammatically);
          counterProgrammatically++;
     }
     public LiveData<String> getEvent1LiveData() {
          return event1LiveData;
     }

     public EventLiveData<String> getEvent1EventLiveData() {
          return event1MutableEventLiveData;
     }
}
