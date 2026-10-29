package com.farm2market.core.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Result<T> {

    private final T value;
    private final String error;
    private final String errorCode;

    private Result(T value, String error, String errorCode) {
        this.value = value;
        this.error = error;
        this.errorCode = errorCode;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, null);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error, "ERROR");
    }

    public static <T> Result<T> failure(String error, String errorCode) {
        return new Result<>(null, error, errorCode);
    }

    public static <T> Result<T> empty() {
        return new Result<>(null, null, null);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getValue() {
        return value;
    }

    public T getValueOrDefault(T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public String getError() {
        return error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (isSuccess() && value != null) {
            return Result.success(mapper.apply(value));
        }
        return Result.failure(error, errorCode);
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (isSuccess() && value != null) {
            return mapper.apply(value);
        }
        return Result.failure(error, errorCode);
    }

    public Result<T> onSuccess(Consumer<T> consumer) {
        if (isSuccess() && value != null) {
            consumer.accept(value);
        }
        return this;
    }

    public Result<T> onFailure(Consumer<String> consumer) {
        if (isFailure()) {
            consumer.accept(error);
        }
        return this;
    }

    public T orElseThrow() throws RuntimeException {
        if (isFailure()) {
            throw new RuntimeException(error);
        }
        return value;
    }

    public <E extends Exception> T orElseThrow(Function<String, E> exceptionMapper) throws E {
        if (isFailure()) {
            throw exceptionMapper.apply(error);
        }
        return value;
    }
}
