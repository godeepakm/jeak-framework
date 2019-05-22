package de.fearnixx.jeak.controller.testImpls;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DummyObject {
    @JsonProperty
    private String name;
    private int age;

    public DummyObject() {
    }

    public DummyObject(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
