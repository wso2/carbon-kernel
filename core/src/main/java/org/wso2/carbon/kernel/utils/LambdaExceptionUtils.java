/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Utility which provides a way to throw checked exceptions from the lambda expressions.
 */
public final class LambdaExceptionUtils {

    /**
     * Represents a {@code Consumer} interface which can throw exceptions.
     *
     * @param <T> the type of the input to the operation
     * @param <E> the type of Exception
     */
    @FunctionalInterface
    public interface ConsumerWithExceptions<T, E extends Exception> {
        void accept(T t) throws E;
    }

    /**
     * Represents a {@code BiConsumer} interface which can throw exceptions.
     *
     * @param <T> the type of the first input to the operation
     * @param <U> the type of the second input to the operation
     * @param <E> the type of Exception
     */
    @FunctionalInterface
    public interface BiConsumerWithExceptions<T, U, E extends Exception> {
        void accept(T t, U u) throws E;
    }

    /**
     * Represents a {@code Function} interface which can throw exceptions.
     *
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     * @param <E> the type of Exception
     */
    @FunctionalInterface
    public interface FunctionWithExceptions<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    /**
     * Represents a {@code Supplier} interface which can throw exceptions.
     *
     * @param <T> the type of results supplied by this supplier
     * @param <E> the type of Exception
     */
    @FunctionalInterface
    public interface SupplierWithExceptions<T, E extends Exception> {
        T get() throws E;
    }

    /**
     * Represents a {@code Runnable} interface which can throw exceptions.
     *
     * @param <E> the type of Exception
     */
    @FunctionalInterface
    public interface RunnableWithExceptions<E extends Exception> {
        void run() throws E;
    }

    public static <T, E extends Exception> Consumer<T> rethrowConsumer(ConsumerWithExceptions<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    public static <T, U, E extends Exception> BiConsumer<T, U> rethrowBiConsumer(
            BiConsumerWithExceptions<T, U, E> biConsumer) {
        return (t, u) -> {
            try {
                biConsumer.accept(t, u);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    /**
     * .map(rethrowFunction(name -- Class.forName(name))) or .map(rethrowFunction(Class::forName))
     * @param <T>      Any Object.
     * @param <R>      Any Object
     * @param <E>      Any Exception.
     * @param function Function to apply for given arguments.
     *
     * @return     Any Object that result from the function passed.
     */
    public static <T, R, E extends Exception> Function<T, R> rethrowFunction(
            FunctionWithExceptions<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    /**
     * rethrowSupplier(() -- new StringJoiner(new String(new byte[]{77, 97, 114, 107}, "UTF-8"))),
     * @param <T>      Any Object.
     * @param <E>      Any Exception.
     * @param function Function to apply for given arguments.
     *
     * @return Supplier of the results.
     */
    public static <T, E extends Exception> Supplier<T> rethrowSupplier(SupplierWithExceptions<T, E> function) {
        return () -> {
            try {
                return function.get();
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    /**
     * uncheck(() -- Class.forName("xxx"));
     * @param t RunnableException.
     */
    public static void uncheck(RunnableWithExceptions t) {
        try {
            t.run();
        } catch (Exception exception) {
            throwAsUnchecked(exception);
        }
    }

    /**
     * uncheck(() -- Class.forName("xxx"));
     * @param <R>    Any Object.
     * @param <E>    Any Exception.
     * @param supplier supplier interface which can throw exceptions.
     *
     * @return R     Any Object.
     */
    public static <R, E extends Exception> R uncheck(SupplierWithExceptions<R, E> supplier) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            throwAsUnchecked(exception);
            return null;
        }
    }

    /**
     * uncheck(Class::forName, "xxx");
     * @param <T>     Any Object.
     * @param <R>     Any Object.
     * @param <E>     Any Exception.
     * @param function Any function which can throw an exception.
     * @param t       Any Object.
     *
     * @return R      Any Object.
     */
    public static <T, R, E extends Exception> R uncheck(FunctionWithExceptions<T, R, E> function, T t) {
        try {
            return function.apply(t);
        } catch (Exception exception) {
            throwAsUnchecked(exception);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

}

