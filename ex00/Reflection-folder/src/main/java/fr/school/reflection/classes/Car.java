package fr.school.reflection.classes;

import java.util.StringJoiner;

public class Car {
    private String brand;
    private String model;
    private Double price;
    private Boolean isElectric;

    public Car() {
        this.brand = "Default brand";
        this.model = "Default model";
        this.price = 0.0;
        this.isElectric = false;
    }

    public Car(String brand, String model, Double price, Boolean isElectric) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.isElectric = isElectric;
    }

    public Double updatePrice(Double newPrice) {
        this.price = newPrice;
        return price;
    }

    public void startEngine() {
        if (isElectric) {
            System.out.println("Electric car " + brand + " " + model + " started silently!");
        } else {
            System.out.println("Gas car " + brand + " " + model + " engine roars!");
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Car.class.getSimpleName() + "[", "]")
                .add("brand='" + brand + "'")
                .add("model='" + model + "'")
                .add("price=" + price)
                .add("isElectric=" + isElectric)
                .toString();
    }
}