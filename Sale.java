package meditracker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Sale {
    private final String name;
    private final String category;
    private final String dosage;
    private final int quantity;
    private final LocalDateTime dateTime;

    public Sale(String name, String category, String dosage, int quantity) {
        this.name = name;
        this.category = category;
        this.dosage = dosage;
        this.quantity = quantity;
        this.dateTime = LocalDateTime.now();
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDosage() { return dosage; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDateTime() { return dateTime; }

    // NEW method for GUI table
    public String getDateTimeString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }
}
