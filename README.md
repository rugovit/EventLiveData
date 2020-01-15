
# EventLiveData 



This is extension of LiveData that allows propagation of events  that are consumed only once to multiple observers.


## Communication

 Find me here :


- Twitter: [@Rugovit][twitter]
- [GitHub Issues][issues]
- [Blog]


# Binaries

```
implementation 'com.rugovit.eventlivedata:eventlivedata:1.0'

```

* EventLiveData: [ ![Download](https://api.bintray.com/packages/rugovit/android/eventlivedata/images/download.svg) ](https://bintray.com/rugovit/android/eventlivedata/_latestVersion)


# Sample usage

A sample project which provides runnable code examples that demonstrate uses of EventLiveData is available in the `EventLiveData/app/` folder of this repository.
## Basic usage
 In it's basic use case EventLiveDate is observed in a same manner as regular LiveData

```java
MutableEventLiveData<String>  eventLiveData =new MutableEventLiveData<>();
    // ...
    //Fragment 1
viewModel.event.observe(this, Observer {
    // ...
})
    //Fragment 2
viewModel.event.observe(this, Observer {
    // ...
})
```
##  onStart

In case where you are observing LiveData in fragment that that is on backstack there is common error where upon going back to previous fragment you add new observer on a same LiveData. 
That is because  fragment never reaches DESTROYED state therefore observer is never released. Calling  observeInOnStart() method instead of observe() in  onStart()  fragments or Activity's method you are ensuring that when fragment or activity reaches STOPED state your observer is released. 

```java

   //...

    @Override
    public void onStart() {
    
        eventLiveData().observeInOnStart(this, o -> {
            //..
        });
    
    }

    //...
```
## Custom observer lifecycle limitation 

When you need to limit your observer lifecycle range from when it will receive events to when it will be released from EventLiveData, use  **observe( LifecycleOwner, Observer ,State,Event)** method.

```java
    //..

    eventLiveData().observe(this,o->{
            //..
            }, Lifecycle.State.RESUMED, Lifecycle.Event.ON_STOP);
    
    //..
```

It might create unexpected behaviors so use only if you are familiar with Android components lifecycle.
Lifecycle State change happens after lifecycle Event so you need to keep that in mind when setting this method.
For example if you observe your EventLiveData with this lifecycle range:

```java
    eventLiveData().observe(this,o->{
            //..
            }, Lifecycle.State.STARTED, Lifecycle.Event.ON_RESUME); // observer will not receive any events!!!
    
```
Observer will not receive any events. That is because  STARTED state  will change right before ON_RESUME  is triggered, leaving no time for your observer to react to possible events.  




## Bugs and Feedback

For bugs, feature requests, and discussion please use [GitHub Issues][issues].

## LICENSE

    Copyright 2015 The RxAndroid authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.




 [twitter]: http://twitter.com/Rugovit
 [issues]: https://github.com/rugovit/EventLiveData/issues
 [Blog]: https://medium.com/@darko.martinovicc/eventlivedata-singlelivedata-with-multiple-observers-7e3e8d4a78fb
 
