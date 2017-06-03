package com.ec.www.view.swiperecycler.utils;

import android.annotation.SuppressLint;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.MotionEvent;
import android.view.View;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import pw.bmyo.www.swiperecycler.BaseItemViewBuild;
import pw.bmyo.www.swiperecycler.SwipeRecyclerView;

import static pw.bmyo.www.swiperecycler.utils.Preconditions.checkMainThread;


/**
 * Created by huang on 2017/1/8.
 */

public class OnSubscribeRecyclerExt {

    public static final class ItemClickOnSubscribe extends Observable<View> {
        final BaseItemViewBuild mAdapter;

        ItemClickOnSubscribe(BaseItemViewBuild adapter) {
            this.mAdapter = adapter;
        }

        @Override
        protected void subscribeActual(Observer<? super View> observer) {
            checkMainThread(observer);
            Listener listener = new Listener(mAdapter, observer);
            observer.onSubscribe(listener);
            mAdapter.setOnItemClickListener(listener);
        }

        static final class Listener extends MainThreadDisposable implements View.OnClickListener {
            private final BaseItemViewBuild adapter;
            private final Observer<? super View> observer;

            Listener(BaseItemViewBuild adapter, Observer<? super View> observer) {
                this.adapter = adapter;
                this.observer = observer;
            }

            @Override
            public void onClick(View v) {
                if (!isDisposed()) {
                    observer.onNext(v);
                }
            }

            @Override
            protected void onDispose() {
                adapter.setOnItemClickListener(null);
            }
        }
    }

    public static final class ItemDataOnSubscribe<T, VH extends RecyclerView.ViewHolder>
            extends Observable<ItemDataOnSubscribe.InnerMsg<T>> {
        final BaseItemViewBuild<T, VH> mAdapter;

        ItemDataOnSubscribe(
                BaseItemViewBuild<T, VH> adapter) {
            mAdapter = adapter;
        }

        @Override
        protected void subscribeActual(Observer<? super InnerMsg<T>> observer) {
            checkMainThread(observer);
            ListenerData<T, VH> listener = new ListenerData<>(mAdapter, observer);
            observer.onSubscribe(listener);
            mAdapter.setOnItemDataClickListener(listener);
        }

        static final class ListenerData<T, VH extends RecyclerView.ViewHolder> extends MainThreadDisposable implements OnItemDataClickListener<T> {
            private final BaseItemViewBuild<T, VH> adapter;
            private final Observer<? super InnerMsg<T>> observer;
            private InnerMsg<T> mMsg;

            ListenerData(BaseItemViewBuild<T, VH> adapter, Observer<? super InnerMsg<T>> observer) {
                this.adapter = adapter;
                this.observer = observer;
                mMsg = new InnerMsg<>();
            }

            @Override
            protected void onDispose() {
                adapter.setOnItemDataClickListener(null);
            }

            @Override
            public void onItemClick(View view, T data) {
                if (!isDisposed()) {
                    observer.onNext(mMsg.set(view, data));
                }
            }
        }

        public static class InnerMsg<T> {
            public View view;
            public T data;

            InnerMsg<T> set(View view, T data) {
                this.view = view;
                this.data = data;
                return this;
            }

        }
    }

    public static final class ItemLongClickOnSubscribe extends Observable<View> {
        final BaseItemViewBuild mAdapter;

        ItemLongClickOnSubscribe(BaseItemViewBuild adapter) {
            this.mAdapter = adapter;
        }

        @Override
        protected void subscribeActual(Observer<? super View> observer) {
            checkMainThread(observer);
            Listener listener = new Listener(mAdapter, observer);
            observer.onSubscribe(listener);
            mAdapter.setOnItemLongClickListener(listener);
        }

        static final class Listener extends MainThreadDisposable implements View.OnLongClickListener {
            private final BaseItemViewBuild adapter;
            private final Observer<? super View> observer;

            Listener(BaseItemViewBuild adapter, Observer<? super View> observer) {
                this.adapter = adapter;
                this.observer = observer;
            }

            @Override
            protected void onDispose() {
                adapter.setOnItemLongClickListener(null);
            }

            @Override
            public boolean onLongClick(View v) {
                if (!isDisposed()) {
                    observer.onNext(v);
                }
                return adapter.isLongIntercept();
            }
        }
    }

    public static final class ItemTouchOnSubscribe extends Observable<ItemTouchOnSubscribe.TouchMsg> {
        final BaseItemViewBuild mAdapter;

        ItemTouchOnSubscribe(BaseItemViewBuild adapter) {
            this.mAdapter = adapter;
        }

        @Override
        protected void subscribeActual(Observer<? super TouchMsg> observer) {
            checkMainThread(observer);
            Listener listener = new Listener(mAdapter, observer);
            observer.onSubscribe(listener);
            mAdapter.setTouchListener(listener);
        }

        static final class Listener extends MainThreadDisposable implements View.OnTouchListener {
            private final BaseItemViewBuild adapter;
            private final Observer<? super TouchMsg> observer;
            private TouchMsg mMsg;

            Listener(BaseItemViewBuild adapter, Observer<? super TouchMsg> observer) {
                this.adapter = adapter;
                this.observer = observer;
                mMsg = new TouchMsg();
            }

            @Override
            protected void onDispose() {
                adapter.setTouchListener(null);
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isDisposed()) {
                    observer.onNext(mMsg.set(v, event));
                }
                return adapter.isTouchIntercept();
            }
        }

        public static class TouchMsg {
            public View view;
            public MotionEvent event;

            TouchMsg set(View view, MotionEvent event) {
                this.event = event;
                this.view = view;
                return this;
            }

        }
    }

    public static final class CanScrollOnSubscribe extends Observable<RecyclerView> {
        public static final int CAN_UP = 1;
        public static final int CAN_DOWN = -1;

        final RecyclerView view;
        final int direction;

        CanScrollOnSubscribe(RecyclerView view, int direction) {
            this.view = view;
            this.direction = direction;
        }

        CanScrollOnSubscribe(RecyclerView view) {
            this.view = view;
            this.direction = CAN_UP;
        }

        @Override
        protected void subscribeActual(Observer<? super RecyclerView> observer) {
            checkMainThread(observer);
            Listener listener = new Listener(view);
            OnScrollListener scrollListener = new OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!ViewCompat.canScrollVertically(recyclerView, direction)) {
                        if (!listener.isDisposed()) {
                            observer.onNext(recyclerView);
                        }
                    }
                }
            };
            view.addOnScrollListener(scrollListener);
        }

        static final class Listener extends MainThreadDisposable {
            private final RecyclerView mView;

            Listener(RecyclerView adapter) {
                this.mView = adapter;
            }

            @Override
            protected void onDispose() {
                mView.setOnScrollListener(null);
            }
        }
    }

    public static final class SwipeRefreshOnSubscribe extends Observable<SwipeRecyclerView> {
        final SwipeRecyclerView view;

        SwipeRefreshOnSubscribe(SwipeRecyclerView view) {
            this.view = view;
        }

        @Override
        protected void subscribeActual(Observer<? super SwipeRecyclerView> observer) {
            checkMainThread(observer);
            Listener listener = new Listener(view, observer);
            observer.onSubscribe(listener);
            view.setOnRefreshListener(listener);
        }

        static final class Listener extends MainThreadDisposable implements SwipeRecyclerView.OnRefreshListener {
            private final SwipeRecyclerView view;
            private final Observer<? super SwipeRecyclerView> observer;

            Listener(SwipeRecyclerView view, Observer<? super SwipeRecyclerView> observer) {
                this.view = view;
                this.observer = observer;
            }


            @Override
            public void onRefresh() {
                if (!isDisposed()) {
                    view.setSlideState(SwipeRecyclerView.SLIDE_DOWN);
                    observer.onNext(view);
                }
            }

            @Override
            public void onLoading() {
                if (!isDisposed()) {
                    view.setSlideState(SwipeRecyclerView.SLIDE_UP);
                    observer.onNext(view);
                }
            }

            @Override
            public void onErrorReload() {
                if (!isDisposed()) {
                    view.setSlideState(SwipeRecyclerView.LOAD_ERROR);
                    observer.onNext(view);
                }
            }

            @Override
            protected void onDispose() {
                view.setOnRefreshListener(null);
            }

        }
    }

}
