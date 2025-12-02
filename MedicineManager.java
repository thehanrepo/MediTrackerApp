package meditracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineManager {
    private final List<Medicine> meds = new ArrayList<>();
    private final List<Sale> sales = new ArrayList<>();

    // Add medicine; return false if serial exists
    public boolean add(Medicine m) {
        for (Medicine med : meds)
            if (med.getSerialNo() == m.getSerialNo()) return false;
        meds.add(m);
        return true;
    }

    public List<Medicine> getAll() { return meds; }
    public List<Sale> getSales() { return sales; }

    // Search by serial, name, category
    public List<Medicine> search(Integer serial, String name, String category) {
        List<Medicine> result = new ArrayList<>();
        for (Medicine m : meds) {
            boolean match = true;
            if (serial != null && m.getSerialNo() != serial) match = false;
            if (name != null && !name.isEmpty() && !m.getName().equalsIgnoreCase(name)) match = false;
            if (category != null && !category.isEmpty() && !m.getCategory().equalsIgnoreCase(category)) match = false;
            if (match) result.add(m);
        }
        return result;
    }

    // Update by serial or name
    public boolean update(Integer serial, String name, String dosage, String expiry, Integer stock, Double price) {
        List<Medicine> found = search(serial, name, null);
        if (found.isEmpty()) return false;
        for (Medicine m : found) {
            if (dosage != null) m.setDosage(dosage);
            if (expiry != null) m.setExpiry(expiry);
            if (stock != null) m.setStock(stock);
            if (price != null) m.setPrice(price);
        }
        return true;
    }

    public boolean delete(Integer serial, String name, String category) {
        List<Medicine> found = search(serial, name, category);
        if (!found.isEmpty()) {
            meds.removeAll(found);
            return true;
        }
        return false;
    }

    // Sell medicine and record sale
    public boolean sell(String name, String category, String dosage, int qty) {
        for (Medicine m : meds) {
            boolean match = (name == null || m.getName().equalsIgnoreCase(name))
                    && (category == null || m.getCategory().equalsIgnoreCase(category))
                    && (dosage == null || m.getDosage().equalsIgnoreCase(dosage));
            if (match) {
                if (m.getStock() >= qty) {
                    m.setStock(m.getStock() - qty);
                    sales.add(new Sale(m.getName(), m.getCategory(), m.getDosage(), qty));
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    // Alerts
    public List<Medicine> expiryAlert() {
        List<Medicine> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Medicine m : meds)
            if (!m.getExpiryDate().isAfter(today.plusDays(30))) list.add(m);
        return list;
    }

    public List<Medicine> lowStock() {
        List<Medicine> list = new ArrayList<>();
        for (Medicine m : meds) if (m.getStock() <= 30) list.add(m);
        return list;
    }

    public List<Medicine> highStock() {
        List<Medicine> list = new ArrayList<>();
        for (Medicine m : meds) if (m.getStock() >= 100) list.add(m);
        return list;
    }

    // Load medicines from file
    public void loadFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                int serial = Integer.parseInt(p[0].trim());
                String name = p[1].trim();
                String category = p[2].trim();
                String dosage = p[3].trim();
                String expiry = p[4].trim();
                int stock = Integer.parseInt(p[5].trim());
                double price = Double.parseDouble(p[6].trim());
                add(new Medicine(serial, name, category, dosage, expiry, stock, price));
            }
        } catch (Exception e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }
}
