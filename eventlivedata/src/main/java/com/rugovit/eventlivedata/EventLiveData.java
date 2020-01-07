package com.rugovit.eventlivedata;

import android.os.Build;
import android.os.Looper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static androidx.lifecycle.Lifecycle.State.STARTED;

//TODO test live data from fragment view model
//TODO test onActive onInactive
//TODO see if it is posible  to make  custum lifecycle observers so i can remove hacky observe in fragment method
//TODO rename internal classes
//TODO dodati dokumentaciju

/**
 * Ne šalje event nakon subscribanja i ponovnog subscribanja
 * @param <T>
 */
public  class EventLiveData<T> extends LiveData<T> {

   // private final AtomicBoolean pending = new AtomicBoolean(false);
    private final HashMap<Observer<? super T>, EventLiveData.ObserverWrapper> observers= new HashMap<>();
    private final Observer<T> internalObserver;
    int mActiveCount = 0;

    public EventLiveData() {
        this.internalObserver =  (new Observer<T>() {
            @Override
            public void onChanged(T t) {
                //if (EventLiveData.this.pending.compareAndSet(true, false)) {
                Iterator<Map.Entry<Observer<? super T>,EventLiveData.ObserverWrapper>> iterator = EventLiveData.this.observers.entrySet().iterator();
                while (iterator.hasNext()){
                    EventLiveData.ObserverWrapper wrapper= iterator.next().getValue();
                    if(wrapper.shouldBeActive())
                        wrapper.getObserver().onChanged(t);
                }
                //}
            }
        });
    }
    private void internalObserve(){
         super.observeForever(this.internalObserver);

    }
    private void internalRemoveObserver(){
         super.removeObserver(internalObserver);
    }



    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull  Observer observer) {
        observe(owner, observer,STARTED,null);
    }
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull  Observer observer,@NonNull Lifecycle.State minimumStateForSendingEvent) {
        observe(owner, observer,minimumStateForSendingEvent,null);
    }
    @MainThread
    public void observeInOnStart(@NonNull LifecycleOwner owner, @NonNull  Observer observer) {
        observe(owner, observer,STARTED, Lifecycle.Event.ON_STOP);
    }
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull  Observer observer,@NonNull Lifecycle.State minimumStateForSendingEvent,Lifecycle.Event maximumEventForRemovingEvent) {
        assertMainThread("observe");
        assertNotNull(owner, "owner");
        assertNotNull(observer, "observer");
        assertNotNull(owner, "minimumStateForSendingEvent");
        assertDestroyedState(minimumStateForSendingEvent);
        assertMaximumEvent(maximumEventForRemovingEvent);
        if(minimumStateForSendingEvent==DESTROYED){
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stackTraceElements[3];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            IllegalArgumentException exception =
                    new IllegalArgumentException("State can not be equal to DESTROYED! : " +
                            "method " + className + "." + methodName +
                            ", parameter " + minimumStateForSendingEvent);
            throw sanitizeStackTrace(exception);
        }

        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }

        EventLiveData.LifecycleBoundObserver wrapper = new EventLiveData.LifecycleBoundObserver(owner, observer);
        wrapper.setMinimumStateForSendingEvent(minimumStateForSendingEvent);
        wrapper.setMaximumEventForRemovingEvent(maximumEventForRemovingEvent);
        EventLiveData.ObserverWrapper existing = wrapper;
        if(!observers.containsKey(observer))existing = observers.put(observer, wrapper);
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        owner.getLifecycle().addObserver(wrapper);

        if (!super.hasObservers()) {
            internalObserve();
        }

    }
    @MainThread
    @Override
    public void observeForever(@NonNull Observer observer) {
        assertMainThread("observeForever");
        assertNotNull(observer, "observer");
        EventLiveData.AlwaysActiveObserver wrapper = new EventLiveData.AlwaysActiveObserver(observer);
        EventLiveData.ObserverWrapper existing = wrapper;
        if(!observers.containsKey(observer))existing = observers.put(observer, wrapper);
        if (existing != null && existing instanceof EventLiveData.LifecycleBoundObserver) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        if (!super.hasObservers()) {
            internalObserve();
        }
        wrapper.activeStateChanged(true);
    }
    @Override
    public void removeObservers(@NonNull  LifecycleOwner owner) {
        assertMainThread("removeObservers");
        assertNotNull(owner, "owner");
        Iterator<Map.Entry<Observer<? super T>,EventLiveData.ObserverWrapper>> iterator = EventLiveData.this.observers.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Observer<? super T>,EventLiveData.ObserverWrapper> entry=iterator.next();
            if(entry.getValue() instanceof EventLiveData.LifecycleBoundObserver){
                EventLiveData.LifecycleBoundObserver lifecycleBoundObserver=(EventLiveData.LifecycleBoundObserver) entry.getValue();
                if(lifecycleBoundObserver.isAttachedTo(owner))this.observers.remove(entry.getKey());
            }
        }
    }
    @Override
    public void removeObserver(@NonNull  Observer observer) {
        assertMainThread("removeObserver");
        assertNotNull(observer, "observer");
        this.observers.remove(observer);

     }
    /**
     * Called when the number of active observers change to 1 from 0.
     * <p>
     * This callback can be used to know that this LiveData is being used thus should be kept
     * up to date.
     */
    protected void onActive() {

    }

    /**
     * Called when the number of active observers change from 1 to 0.
     * <p>
     * This does not mean that there are no observers left, there may still be observers but their
     * lifecycle states aren't {@link Lifecycle.State#STARTED} or {@link Lifecycle.State#RESUMED}
     * (like an Activity in the back stack).
     * <p>
     * You can check if there are observers via {@link #hasObservers()}.
     */
    protected void onInactive() {

    }

    /**
     * Returns true if this LiveData has observers.
     *
     * @return true if this LiveData has observers
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasObservers() {
        return observers.size() > 0;
    }

    /**
     * Returns true if this LiveData has active observers.
     *
     * @return true if this LiveData has active observers
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasActiveObservers() {
        return mActiveCount > 0;
    }
    class LifecycleBoundObserver extends EventLiveData.ObserverWrapper implements LifecycleObserver {
        @NonNull
       private final LifecycleOwner mOwner;
        private Lifecycle.State MINIMUM_STATE_FOR_SENDING_EVENT= STARTED;
        private Lifecycle.Event MAXIMUM_EVENT_FOR_REMOVING_EVENT= null;
        LifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(observer);
            mOwner = owner;
        }

        public Lifecycle.State getMinimumStateForSendingEvent() {
            return MINIMUM_STATE_FOR_SENDING_EVENT;
        }

        public Lifecycle.Event getMaximumStateForRemovingEvent() {
            return MAXIMUM_EVENT_FOR_REMOVING_EVENT;
        }

        public void setMaximumEventForRemovingEvent(Lifecycle.Event MAXIMUM_EVENT_FOR_REMOVING_EVENT) {
            this.MAXIMUM_EVENT_FOR_REMOVING_EVENT = MAXIMUM_EVENT_FOR_REMOVING_EVENT;
        }

        public void setMinimumStateForSendingEvent(Lifecycle.State MINIMUM_STATE_FOR_SENDING_EVENT) {
            this.MINIMUM_STATE_FOR_SENDING_EVENT = MINIMUM_STATE_FOR_SENDING_EVENT;
        }

        @Override
        boolean shouldBeActive() {
            Lifecycle.State state=mOwner.getLifecycle().getCurrentState();
            return state.isAtLeast(MINIMUM_STATE_FOR_SENDING_EVENT);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (mOwner.getLifecycle().getCurrentState() == DESTROYED||(MAXIMUM_EVENT_FOR_REMOVING_EVENT!=null&&MAXIMUM_EVENT_FOR_REMOVING_EVENT==event)) {
                removeObserver(mObserver);
                return;
            }
            activeStateChanged(shouldBeActive());
        }

        @Override
        boolean isAttachedTo(LifecycleOwner owner) {
            return mOwner == owner;
        }

        @Override
        void detachObserver() {
            mOwner.getLifecycle().removeObserver(this);
        }
    }

    private abstract class ObserverWrapper {
        protected final Observer<? super T> mObserver;
        boolean mActive;

        ObserverWrapper(Observer<? super T> observer) {
            mObserver = observer;
        }

        abstract boolean shouldBeActive();

        boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        void detachObserver() {
        }

        public Observer<? super T> getObserver() {
            return mObserver;
        }

        void activeStateChanged(boolean newActive) {
            if (newActive == mActive) {
                return;
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive;
            boolean wasInactive = EventLiveData.this.mActiveCount == 0;
            EventLiveData.this.mActiveCount += mActive ? 1 : -1;
            if (wasInactive && mActive) {
                onActive();
            }
            if (EventLiveData.this.mActiveCount == 0 && !mActive) {
                onInactive();
            }
            //if (mActive) {  //calls observer
            //    dispatchingValue(this);
            // }
        }
    }




    private class AlwaysActiveObserver extends EventLiveData.ObserverWrapper {

        AlwaysActiveObserver(Observer<? super T> observer) {
            super(observer);
        }

        @Override
        boolean shouldBeActive() {
            return true;
        }
    }
    private void assertDestroyedState(@NonNull Lifecycle.State minimumStateForSendingEvent){
        if(minimumStateForSendingEvent==DESTROYED){
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stackTraceElements[3];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            IllegalArgumentException exception =
                    new IllegalArgumentException("State can not be equal to "+ minimumStateForSendingEvent +
                            "method " + className + "." + methodName +
                            ", parameter   minimumStateForSendingEvent");
            throw sanitizeStackTrace(exception);
        }
    }
    private void assertMaximumEvent(@NonNull Lifecycle.Event maximumEventForRemovingEvent){
        if(maximumEventForRemovingEvent== Lifecycle.Event.ON_START||maximumEventForRemovingEvent== Lifecycle.Event.ON_CREATE
        ||maximumEventForRemovingEvent== Lifecycle.Event.ON_RESUME){
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stackTraceElements[3];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            IllegalArgumentException exception =
                    new IllegalArgumentException("State can not be equal to "+maximumEventForRemovingEvent +
                            "method " + className + "." + methodName +
                            ", parameter  maximumEventForRemovingEvent" );
            throw sanitizeStackTrace(exception);
        }
    }
    private  void assertMainThread(String methodName) {
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? Looper.getMainLooper().isCurrentThread()
                : Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isUiThread) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on a background"
                    + " thread");
        }
    }

    private  void assertNotNull(Object value, String paramName) {
        if (value == null) {
            throwParameterIsNullException(paramName);
        }
    }
    private  void throwParameterIsNullException(String paramName) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[3];
        String className = caller.getClassName();
        String methodName = caller.getMethodName();

        IllegalArgumentException exception =
                new IllegalArgumentException("Parameter specified as non-null is null: " +
                        "method " + className + "." + methodName +
                        ", parameter " + paramName);
        throw sanitizeStackTrace(exception);
    }
    private   <T extends Throwable> T sanitizeStackTrace(T throwable) {
        return sanitizeStackTrace(throwable, this.getClass().getName());
    }

     <T extends Throwable> T sanitizeStackTrace(T throwable, String classNameToDrop) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int size = stackTrace.length;

        int lastIntrinsic = -1;
        for (int i = 0; i < size; i++) {
            if (classNameToDrop.equals(stackTrace[i].getClassName())) {
                lastIntrinsic = i;
            }
        }

        StackTraceElement[] newStackTrace = Arrays.copyOfRange(stackTrace, lastIntrinsic + 1, size);
        throwable.setStackTrace(newStackTrace);
        return throwable;
    }
}

