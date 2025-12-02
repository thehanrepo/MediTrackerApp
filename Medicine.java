package meditracker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Medicine {
    private int serialNo;
    private String name, category, dosage;
    private LocalDate expiryDate;
    private int stock;
    private double price;

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Medicine(int serialNo, String name, String category, String dosage,
                    String expiry, int stock, double price) {
        this.serialNo = serialNo;
        this.name = name;
        this.category = category;
        this.dosage = dosage;
        this.expiryDate = LocalDate.parse(expiry, fmt);
        this.stock = stock;
        this.price = price;
    }

    // Getters
    public int getSerialNo() { return serialNo; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDosage() { return dosage; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getExpiryString() { return expiryDate.format(fmt); }
    public int getStock() { return stock; }
    public double getPrice() { return price; }

    // Setters
    public void setCategory(String c) { category = c; }
    public void setDosage(String d) { dosage = d; }
    public void setExpiry(String e) { expiryDate = LocalDate.parse(e, fmt); }
    public void setStock(int s) { stock = s; }
    public void setPrice(double p) { price = p; }

    @Override
    public String toString() {
        return serialNo+" | "+name+" | "+category+" | "+dosage+
                " | Exp: "+getExpiryString()+" | Stock: "+stock+" | Price: "+price;
    }
}
