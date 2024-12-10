package com.example.alarm;

@FunctionalInterface
public interface OnItemClickListener<T> {
    void onClick(T item);
}