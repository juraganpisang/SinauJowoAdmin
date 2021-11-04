package com.juraganpisang.sinaujowoadmin.Model;

public class CategoryModel {

    private String id;
    private String name;
    private int noOfTest;

    public CategoryModel(String id, String name, int noOfTest) {
        this.id = id;
        this.name = name;
        this.noOfTest = noOfTest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoOfTest() {
        return noOfTest;
    }

    public void setNoOfTest(int noOfTest) {
        this.noOfTest = noOfTest;
    }
}
