package com.licheedev.demo.utils;

import android.support.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

public class RxUtilEx {

    public static <T> ObservableTransformer<T, T> rxIoMain() {
        return new ObservableTransformer<T, T>() {
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> SingleTransformer<T, T> rxSingleIoMain() {
        return new SingleTransformer<T, T>() {
            public SingleSource<T> apply(@io.reactivex.annotations.NonNull Single<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 重试和重复执行
     *
     * @param retryDelay
     * @param repeatDelay
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T, T> retryRepeat(final long retryDelay,
        final long repeatDelay) {
        return new ObservableTransformer<T, T>() {
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream.retryWhen(
                    new Function<Observable<Throwable>, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(Observable<Throwable> throwableObservable)
                            throws Exception {
                            return throwableObservable.delay(retryDelay, TimeUnit.MILLISECONDS);
                        }
                    })
                    // 一定时间后重新查询
                    .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(Observable<Object> objectObservable)
                            throws Exception {
                            return objectObservable.delay(repeatDelay, TimeUnit.MILLISECONDS);
                        }
                    });
            }
        };
    }

    /**
     * 重试
     *
     * @param delay
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T, T> retry(final long delay) {
        return new ObservableTransformer<T, T>() {
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream.retryWhen(
                    new Function<Observable<Throwable>, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(Observable<Throwable> throwableObservable)
                            throws Exception {
                            return throwableObservable.delay(delay, TimeUnit.MILLISECONDS);
                        }
                    });
            }
        };
    }

    /**
     * 重复执行
     *
     * @param delay
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T, T> repeat(final long delay) {
        return new ObservableTransformer<T, T>() {
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                    // 一定时间后重新查询
                    .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(Observable<Object> objectObservable)
                            throws Exception {
                            return objectObservable.delay(delay, TimeUnit.MILLISECONDS);
                        }
                    });
            }
        };
    }

    public static Scheduler io() {
        return Schedulers.io();
    }

    public static Scheduler main() {
        return AndroidSchedulers.mainThread();
    }
}
