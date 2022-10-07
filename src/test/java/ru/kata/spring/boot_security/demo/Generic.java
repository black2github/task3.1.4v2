package ru.kata.spring.boot_security.demo;

import java.util.LinkedList;

public class Generic {

    public static void main(String[] arg) {
        Long c = 0L;
        System.out.println("Class name = " + c.getClass());
        Class<Long> clazz = (Class<Long>) c.getClass();
        System.out.println("Class = " + clazz.getClass());
        //new Account<Long>();
    }

    private static class Account<T> {

        private Class<T> id;

        Account(){
            //id = new T();
            System.out.println(id.getClass().getSimpleName());
        }

        public T getId() { return (T) id; }
    }
}
