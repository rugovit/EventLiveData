package com.rugovit.eventlivedata;

import android.os.Build;
import android.os.Looper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static androidx.lifecycle.Lifecycle.State.STARTED;

/**
 * DESIGNED TO SUPPORT  PROPAGATION OF  "ONE TIME" EVENTS ONLY WHEN THEY HAPPEN. UNLIKE REGULAR {@link LiveData} IT WILL NOT PROPAGATE "OLD" EVENTS TO SUBSCRIBERS THAT JUST BECOME ACTIVE.(BUTTON CLICK FROM OLD FRAGMENT  WILL NOT REACT IN NEW FRAGMENT  IF SUBSCRIBING TO SAME LIVE DATA)
 * <p>
 * CLASS ALSO SOLVES MULTIPLE SUBSCRIBERS BUG WHEN RETURNING BACK TO SAME FRAGMENT IN BACKSTACK  USING {@link EventLiveData#observeInOnStart(LifecycleOwner, Observer)} METHOD  THAT KEEPS OBSERVER ALIVE BETWEEN {@link Lifecycle.State#STARTED} AND {@link Lifecycle.Event#ON_STOP}
 * ENSURING CORRECT REMOVAL OF OBSERVERS.
 * <p>
 * <p>
 * <p>
 * EventLiveData is a data holder class that can be observed within a given lifecycle.
 * This means that an {@link Observer} can be added in a pair with a {@link LifecycleOwner}, and
 * this observer will be notified about modifications of the wrapped data only if the paired
 * LifecycleOwner is in active state. LifecycleOwner is considered as active, if its state is
 * {@link Lifecycle.State#STARTED} or {@link Lifecycle.State#RESUMED} in default configuration.
 * <p>
 * USING ADDITIONAL PARAMETERS WHILE SUBSCRIBING COULD BE MADE TO WORK WITH DIFFERENT START STATE AND REMOVED ON ONE OF THE EVENTS AFTER {@link Lifecycle.Event#ON_PAUSE}.
 * <p>
 * An observer added via
 * {@link #observeForever(Observer)} is considered as always active and thus will be always notified
 * about modifications. For those observers, you should manually call
 * {@link #removeObserver(Observer)}.
 *
 * <p> An observer added with a Lifecycle will be automatically removed if the corresponding
 * Lifecycle moves to {@link Lifecycle.State#DESTROYED} state OR ON SPECIFIC EVENT DEFINED WITH ADDITIONAL PARAMETER removeObserverEvent . This is especially useful for
 * activities and fragments where they can safely observe EventLiveData and not worry about leaks:
 * they will be instantly unsubscribed when they are destroyed.
 *
 * <p>
 * In addition, EventLiveData has {@link EventLiveData#onActiveEvent()} and {@link EventLiveData#onInactive()} methods
 * to get notified when number of active {@link Observer}s change between 0 and 1.
 * This allows EventLiveData to release any heavy resources when it does not have any Observers that
 * are actively observing.
 * <p>
 * This class is designed to hold individual data fields of {@link ViewModel},
 * but can also be used for sharing data between different modules in your application
 * in a decoupled fashion.
 *
 * @param <T> The type of data held by this instance
 * @see ViewModel
 */
public  class EventLiveData<T> extends LiveData<T> {

    private final HashMap<Observer<? super T>, EventObserverWrapper> observers= new HashMap<>();
    private final Observer<T> internalObserver;
    private int mActiveCount = 0;

    public EventLiveData() {
        this.internalObserver =  (new Observer<T>() {
            @Override
            public void onChanged(T t) {
                Iterator<Map.Entry<Observer<? super T>, EventObserverWrapper>> iterator = EventLiveData.this.observers.entrySet().iterator();
                while (iterator.hasNext()){
                    EventObserverWrapper wrapper= iterator.next().getValue();
                    if(wrapper.shouldBeActive())
                        wrapper.getObserver().onChanged(t);
                }
            }
        });
    }
    private void internalObserve(){
        super.observeForever(this.internalObserver);

    }

    /**
     * Acts as regular  observer method witch it overrides with exception that events are only receded when they are set with {@link EventLiveData#setValue(Object)} or {@link EventLiveData#postValue(Object)}
     * <p> If Observer becomes active after EventLiveData is set to some value it will not call Observer
     * <p>
     * <p>
     * Adds the given observer to the observers list within the lifespan of the given
     * owner. The events are dispatched on the main thread. If EventLiveData already has data
     * set, it will be delivered to the observer.
     * <p>
     * The observer will only receive events if the owner is in {@link Lifecycle.State#STARTED}
     * or {@link Lifecycle.State#RESUMED} state (active).
     * <p>
     * If the owner moves to the {@link Lifecycle.State#DESTROYED} state, the observer will
     * automatically be removed.
     * <p>
     * When data changes while the {@code owner} is not active, it will not receive any updates.
     * If it becomes active again, IT WILL NOT RECEIVE (!) the last available data.
     * <p>
     * EventLiveData keeps a strong reference to the observer and the owner as long as the
     * given LifecycleOwner is not destroyed. When it is destroyed, EventLiveData removes references to
     * the observer &amp; the owner.
     * <p>
     * If the given owner is already in {@link Lifecycle.State#DESTROYED} state, EventLiveData
     * ignores the call.
     * <p>
     * If the given owner, observer tuple is already in the list, the call is ignored.
     * If the observer is already in the list with another owner, EventLiveData throws an
     * {@link IllegalArgumentException}.
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer observer) {
        observe(owner, observer,STARTED,null);
    }
    /**
     * Acts as regular  observer method witch it overrides with exception that events are only receded when they are set with {@link MutableEventLiveData#setValue(Object)} or {@link MutableEventLiveData#postValue(Object)}
     * <p> If Observer becomes active after EventLiveData is set to some value it will not call Observer.
     * <p>
     * <p>
     * Adds the given observer to the observers list within the lifespan of the given
     * owner. The events are dispatched on the main thread. If EventLiveData already has data
     * set, it will be delivered to the observer.
     * <p>
     * The observer will only receive events if the owner is in minimumStateForSendingEvent state (active).
     * <p>
     * If the owner moves to the {@link Lifecycle.State#DESTROYED} state, the observer will
     * automatically be removed.
     * <p>
     * When data changes while the {@code owner} is not active, it will not receive any updates.
     * If it becomes active again, IT WILL NOT RECEIVE (!) the last available data.
     * <p>
     * EventLiveData keeps a strong reference to the observer and the owner as long as the
     * given LifecycleOwner is not destroyed. When it is destroyed, EventLiveData removes references to
     * the observer &amp; the owner.
     * <p>
     * If the given owner is already in {@link Lifecycle.State#DESTROYED} state, EventLiveData
     * ignores the call.
     * <p>
     * If the given owner, observer tuple is already in the list, the call is ignored.
     * If the observer is already in the list with another owner, EventLiveData throws an
     * {@link IllegalArgumentException}.
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     * @param minimumStateForSendingEvent minimum lifecycle state in what owner has to be for Observer to be updated (Default state for regular EventLiveData is {@link Lifecycle.State#STARTED} )
     */
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer observer, @NonNull Lifecycle.State minimumStateForSendingEvent) {
        observe(owner, observer,minimumStateForSendingEvent,null);
    }

    /** Same as calling {@link MutableEventLiveData} {@link MutableEventLiveData#observe( LifecycleOwner owner, Observer observer, Lifecycle.State minimumStateForSendingEvent,Lifecycle.Event removeObserverEvent) observe(LifecycleOwner owner,  Observer observer, Lifecycle.State minimumStateForSendingEvent,Lifecycle.Event removeObserverEvent)}
     *<p> With parameters:
     *<p> minimumStateForSendingEvent= {@link Lifecycle.State#STARTED}
     *<p> removeObserverEvent={@link Lifecycle.Event#ON_STOP}
     * Should be called in  {@link Fragment#onStart()} method. Use to fix strange behavior  of EventLiveData when going back  to previous  Fragment  in backstack
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    @MainThread
    public void observeInOnStart(@NonNull LifecycleOwner owner, @NonNull Observer observer) {
        observe(owner, observer,STARTED, Lifecycle.Event.ON_STOP);
    }
    /**
     * Acts as regular  observer method witch it overrides with exception that events are only receded when they are set with {@link MutableEventLiveData#setValue(Object)} or {@link MutableEventLiveData#postValue(Object)}
     * <p> If Observer becomes active after EventLiveData is set to some value it will not call Observer.
     * <p>
     * <p>
     * Adds the given observer to the observers list within the lifespan of the given
     * owner. The events are dispatched on the main thread. If EventLiveData already has data
     * set, it will be delivered to the observer.
     * <p>
     * The observer will only receive events if the owner is in minimumStateForSendingEvent state (active).
     * <p> When removeObserverEvent is set  to some value greater then {@link Lifecycle.Event#ON_PAUSE} observer will be removed when that event is triggered
     * <p>
     * If the owner moves to the {@link Lifecycle.State#DESTROYED} state  and removeObserverEvent is set to null , the observer will
     * automatically be removed.
     * <p>
     * When data changes while the {@code owner} is not active, it will not receive any updates.
     * If it becomes active again, IT WILL NOT RECEIVE (!) the last available data.
     * <p>
     * EventLiveData keeps a strong reference to the observer and the owner as long as the
     * given LifecycleOwner is not destroyed. When it is destroyed, EventLiveData removes references to
     * the observer &amp; the owner.
     * <p>
     * If the given owner is already in {@link Lifecycle.State#DESTROYED} state, EventLiveData
     * ignores the call.
     * <p>
     * If the given owner, observer tuple is already in the list, the call is ignored.
     * If the observer is already in the list with another owner, EventLiveData throws an
     * {@link IllegalArgumentException}.
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     * @param minimumStateForSendingEvent minimum lifecycle state in what owner has to be for Observer to be updated (Default state for regular EventLiveData is {@link Lifecycle.State#STARTED} )
     * @param removeObserverEvent maximum {@link Lifecycle.Event} that triggers removal of Event
     */
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer observer, @NonNull Lifecycle.State minimumStateForSendingEvent, Lifecycle.Event removeObserverEvent) {
        assertMainThread("observe");
        assertNotNull(owner, "owner");
        assertNotNull(observer, "observer");
        assertNotNull(owner, "minimumStateForSendingEvent");
        assertDestroyedState(minimumStateForSendingEvent);
        assertMaximumEvent(removeObserverEvent);
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

        EventLifecycleBoundEventObserver wrapper = new EventLifecycleBoundEventObserver(owner, observer);
        wrapper.setMinimumStateForSendingEvent(minimumStateForSendingEvent);
        wrapper.setMaximumEventForRemovingEvent(removeObserverEvent);
        EventObserverWrapper existing = wrapper;
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

    /**
     {@inheritDoc}
     */
    @MainThread
    @Override
    public void observeForever(@NonNull Observer observer) {
        assertMainThread("observeForever");
        assertNotNull(observer, "observer");
        EventAlwaysActiveEventObserver wrapper = new EventAlwaysActiveEventObserver(observer);
        EventObserverWrapper existing = wrapper;
        if(!observers.containsKey(observer))existing = observers.put(observer, wrapper);
        if (existing != null && existing instanceof EventLiveData.EventLifecycleBoundEventObserver) {
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
    /**
     {@inheritDoc}
     */
    @Override
    public void removeObservers(@NonNull  LifecycleOwner owner) {
        assertMainThread("removeObservers");
        assertNotNull(owner, "owner");
        Iterator<Map.Entry<Observer<? super T>, EventObserverWrapper>> iterator = EventLiveData.this.observers.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Observer<? super T>, EventObserverWrapper> entry=iterator.next();
            if(entry.getValue() instanceof EventLiveData.EventLifecycleBoundEventObserver){
                EventLifecycleBoundEventObserver eventLifecycleBoundObserver =(EventLifecycleBoundEventObserver) entry.getValue();
                if(eventLifecycleBoundObserver.isAttachedTo(owner))this.observers.remove(entry.getKey());
            }
        }
    }
    /**
     {@inheritDoc}
     */
    @Override
    public void removeObserver(@NonNull Observer observer) {
        assertMainThread("removeObserver");
        assertNotNull(observer, "observer");
        this.observers.remove(observer);

    }
    /** Final because OnActive is being used by superclass, override  {@link EventLiveData#onActiveEvent()} instead
     *<p>
     {@inheritDoc}
     */
    final protected void onActive() {}
    /**
     * Called when the number of active observers change to 1 from 0.
     * <p>
     * This callback can be used to know that this EventLiveData is being used thus should be kept
     * up to date.
     */
    protected void onActiveEvent() {}
    /**
     {@inheritDoc}
     */
    protected void onInactive() {

    }

    /**
     {@inheritDoc}
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasObservers() {
        return observers.size() > 0;
    }

    /**
     {@inheritDoc}
     */
    @SuppressWarnings("WeakerAccess")
    public boolean hasActiveObservers() {
        return mActiveCount > 0;
    }
    class EventLifecycleBoundEventObserver extends EventObserverWrapper implements LifecycleObserver {
        @NonNull
        private final LifecycleOwner mOwner;
        private Lifecycle.State MINIMUM_STATE_FOR_SENDING_EVENT= STARTED;
        private Lifecycle.Event MAXIMUM_EVENT_FOR_REMOVING_EVENT= null;
        EventLifecycleBoundEventObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
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

    private abstract class EventObserverWrapper {
        protected final Observer<? super T> mObserver;
        boolean mActive;

        EventObserverWrapper(Observer<? super T> observer) {
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
                onActiveEvent();
            }
            if (EventLiveData.this.mActiveCount == 0 && !mActive) {
                onInactive();
            }
        }
    }




    private class EventAlwaysActiveEventObserver extends EventObserverWrapper {

        EventAlwaysActiveEventObserver(Observer<? super T> observer) {
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

