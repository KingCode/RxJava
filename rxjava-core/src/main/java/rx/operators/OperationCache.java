/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.operators;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subjects.ReplaySubject;

/**
 * This method has similar behavior to {@link Observable#replay()} except that this auto-subscribes
 * to the source Observable rather than returning a connectable Observable.
 * <p>
 * <img width="640" src="https://github.com/Netflix/RxJava/wiki/images/rx-operators/cache.png">
 * <p>
 * This is useful with an Observable that you want to cache responses when you can't control the
 * subscribe/unsubscribe behavior of all the Observers.
 * <p>
 * NOTE: You sacrifice the ability to unsubscribe from the origin when you use this operator, so be
 * careful not to use this operator on Observables that emit infinite or very large numbers of
 * items, as this will use up memory.
 */
public class OperationCache {

    public static <T> OnSubscribeFunc<T> cache(final Observable<? extends T> source) {
        return new OnSubscribeFunc<T>() {

            final AtomicBoolean subscribed = new AtomicBoolean(false);
            private final ReplaySubject<T> cache = ReplaySubject.create();

            @Override
            public Subscription onSubscribe(Observer<? super T> observer) {
                if (subscribed.compareAndSet(false, true)) {
                    // subscribe to the source once
                    source.subscribe(cache);
                    /*
                     * Note that we will never unsubscribe from 'source' as we want to receive and cache all of its values.
                     * 
                     * This means this should never be used on an infinite or very large sequence, similar to toList().
                     */
                }

                return cache.subscribe(observer);
            }

        };
    }
}
