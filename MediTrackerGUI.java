package meditracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MediTrackerGUI extends JFrame {
    private final MedicineManager manager = new MedicineManager();
    private final DefaultTableModel tableModel;

    public MediTrackerGUI(){
        setTitle("MediTracker"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(900,550); setLocationRelativeTo(null); setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(new Color(10,38,71));
        JLabel title = new JLabel("MediTracker"); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif",Font.BOLD,22));
        header.add(title,BorderLayout.WEST); add(header,BorderLayout.NORTH);

        // Table
        String[] cols = {"Serial","Name","Category","Dosage","Expiry","Stock","Price"};
        tableModel = new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table = new JTable(tableModel); table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false); table.getTableHeader().setBackground(new Color(220,230,242));
        table.getTableHeader().setForeground(Color.BLACK); add(new JScrollPane(table),BorderLayout.CENTER);

        // Action bar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER,10,8)); bar.setBackground(Color.WHITE);
        addButton(bar,"Add", e->openAddDialog());
        addButton(bar,"Search", e->openSearchDialog());
        addButton(bar,"Update", e->openUpdateDialog());
        addButton(bar,"Delete", e->openDeleteDialog());
        addButton(bar,"Sell", e->openSellDialog());
        addButton(bar,"View Sales", e->viewSalesDialog());
        addButton(bar,"Expiry", e->refreshTable(manager.expiryAlert()));
        addButton(bar,"Low stock", e->refreshTable(manager.lowStock()));
        addButton(bar,"High stock", e->refreshTable(manager.highStock()));
        addButton(bar,"View all", e->refreshTable(manager.getAll()));
        addButton(bar,"Load file", e->handleLoadFile());
        add(bar,BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addButton(JPanel p,String text,java.awt.event.ActionListener l){
        JButton b = new JButton(text); b.setFont(new Font("SansSerif",Font.BOLD,12)); b.setForeground(Color.BLACK);
        b.setBackground(new Color(220,230,242)); b.addActionListener(l); p.add(b);
    }

    private void refreshTable(List<Medicine> meds){ tableModel.setRowCount(0); for(Medicine m:meds) tableModel.addRow(new Object[]{m.getSerialNo(),m.getName(),m.getCategory(),m.getDosage(),m.getExpiryString(),m.getStock(),m.getPrice()});}

    private void showForm(String title,int w,int h,String[] labels, Function<Map<String,JTextField>,Boolean> onOk){
        JDialog d=new JDialog(this,title,true); d.setSize(w,h); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout());
        JPanel p=new JPanel(); p.setBackground(Color.WHITE); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); d.add(p,BorderLayout.CENTER);
        Map<String,JTextField> fields=new HashMap<>();
        for(String lbl:labels){ JLabel l=new JLabel(lbl); l.setForeground(Color.BLACK); l.setFont(new Font("SansSerif",Font.PLAIN,12)); p.add(l); JTextField tf=new JTextField(); tf.setFont(new Font("SansSerif",Font.PLAIN,12)); tf.setForeground(Color.BLACK); tf.setBackground(Color.WHITE); p.add(tf); fields.put(lbl,tf);}
        JButton ok=new JButton("OK"), cancel=new JButton("Cancel"); ok.setForeground(Color.BLACK); cancel.setForeground(Color.BLACK);
        ok.addActionListener(e->{boolean close=Boolean.TRUE.equals(onOk.apply(fields)); if(close)d.dispose();});
        cancel.addActionListener(e->d.dispose());
        JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.add(ok); btns.add(cancel); d.add(btns,BorderLayout.SOUTH); d.setVisible(true);
    }

    // ===== Dialogs =====
    private void openAddDialog(){ String[] labels={"Serial no:","Name:","Category:","Dosage:","Expiry (dd/MM/yyyy):","Stock (packs):","Price per pack:"};
        showForm("Add medicine",350,380,labels,f->{ Integer serial=parseInt(f.get("Serial no:").getText()); if(serial==null){error("Enter a valid serial number."); return false;}
            if(!manager.search(serial,null,null).isEmpty()){error("Serial already exists."); return false;}
            String name=textOrNull(f.get("Name:")), category=textOrNull(f.get("Category:")), dosage=textOrNull(f.get("Dosage:")), expiry=textOrNull(f.get("Expiry (dd/MM/yyyy):"));
            Integer stock=parseInt(f.get("Stock (packs):").getText()); Double price=parseDouble(f.get("Price per pack:").getText());
            if(name==null||category==null||dosage==null||expiry==null||stock==null||price==null){error("Fill all fields correctly."); return false;}
            try{manager.add(new Medicine(serial,name,category,dosage,expiry,stock,price)); refreshTable(manager.getAll()); info("Medicine added."); return true;}
            catch(Exception ex){error("Error: "+ex.getMessage()); return false;}
        }); }

    private void openSearchDialog(){ String[] labels={"Serial no (optional):","Name (optional):","Category (optional):"};
        showForm("Search medicine",320,260,labels,f->{ Integer serial=parseInt(f.get("Serial no (optional):").getText());
            String name=textOrNull(f.get("Name (optional):")); String cat=textOrNull(f.get("Category (optional):"));
            if(serial==null&&name==null&&cat==null){error("Enter at least one search field."); return false;}
            List<Medicine> res=manager.search(serial,name,cat); if(res.isEmpty()) info("No medicines found."); refreshTable(res); return true;
        }); }

    private void openUpdateDialog(){ String[] labels={"Serial (or leave to use name):","Name (optional id):","New dosage (optional):","New expiry dd/MM/yyyy (opt):","New stock (optional):","New price (optional):"};
        showForm("Update medicine",350,320,labels,f->{ Integer serial=parseInt(f.get("Serial (or leave to use name):").getText());
            String name=textOrNull(f.get("Name (optional id):")); if(serial==null&&name==null){error("Provide serial or name."); return false;}
            String dosage=textOrNull(f.get("New dosage (optional):")); String expiry=textOrNull(f.get("New expiry dd/MM/yyyy (opt):"));
            Integer stock=parseIntNullable(f.get("New stock (optional):").getText()); Double price=parseDoubleNullable(f.get("New price (optional):").getText());
            if(dosage==null&&expiry==null&&stock==null&&price==null){error("Enter at least one field."); return false;}
            boolean ok=manager.update(serial,name,dosage,expiry,stock,price); if(ok){refreshTable(manager.getAll()); info("Medicine updated."); return true;} else {error("No matching medicine found."); return false;}
        }); }

    private void openDeleteDialog(){ String[] labels={"Serial (optional):","Name (optional):","Category (optional):"};
        showForm("Delete medicine",320,260,labels,f->{ Integer serial=parseInt(f.get("Serial (optional):").getText()); String name=textOrNull(f.get("Name (optional):")); String cat=textOrNull(f.get("Category (optional):"));
            if(serial==null&&name==null&&cat==null){error("Enter at least one field."); return false;}
            int confirm=JOptionPane.showConfirmDialog(this,"Delete matching medicine(s)?","Confirm delete",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(confirm!=JOptionPane.YES_OPTION) return false;
            if(manager.delete(serial,name,cat)){refreshTable(manager.getAll()); info("Medicine(s) deleted."); return true;} else {error("No matching medicine."); return false;}
        }); }

    private void openSellDialog(){ String[] labels={"Name (optional):","Category (optional):","Dosage (optional):","Quantity:"};
        showForm("Sell medicine",330,260,labels,f->{ String name=textOrNull(f.get("Name (optional):")); String cat=textOrNull(f.get("Category (optional):")); String dosage=textOrNull(f.get("Dosage (optional):")); Integer qty=parseInt(f.get("Quantity:").getText());
            if(qty==null||qty<=0){error("Enter a valid positive quantity."); return false;} if(name==null&&cat==null&&dosage==null){error("Provide at least one filter."); return false;}
            int confirm=JOptionPane.showConfirmDialog(this,"Sell "+qty+" pack(s) of matching medicine?","Confirm sale",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if(confirm!=JOptionPane.YES_OPTION) return false;
            if(manager.sell(name,cat,dosage,qty)){refreshTable(manager.getAll()); info("Sale completed."); return true;} else {error("Sale failed."); return false;}
        }); }

    // ===== View Sales =====
    private void viewSalesDialog(){
        List<Sale> sales=manager.getSales();
        String[] cols={"Name","Category","Dosage","Quantity","Date/Time"};
        DefaultTableModel model=new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        for(Sale s:sales) model.addRow(new Object[]{s.getName(),s.getCategory(),s.getDosage(),s.getQuantity(),s.getDateTimeString()});
        JTable table=new JTable(model); table.setRowHeight(22);
        JOptionPane.showMessageDialog(this,new JScrollPane(table),"Sales Records",JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleLoadFile(){ JFileChooser chooser=new JFileChooser(); int res=chooser.showOpenDialog(this); if(res==JFileChooser.APPROVE_OPTION){
        File f=chooser.getSelectedFile(); try{manager.loadFromFile(f.getAbsolutePath()); refreshTable(manager.getAll()); info("Loaded dataset from: "+f.getName());}catch(Exception ex){error("Could not load file: "+ex.getMessage());}}}

    private void info(String msg){JOptionPane.showMessageDialog(this,msg,"Info",JOptionPane.INFORMATION_MESSAGE);}
    private void error(String msg){JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);}
    private String textOrNull(JTextField tf){String s=tf.getText(); return (s==null||s.trim().isEmpty())?null:s.trim();}
    private Integer parseInt(String s){try{return s==null?null:Integer.parseInt(s.trim());}catch(Exception e){return null;}}
    private Integer parseIntNullable(String s){return parseInt(s);}
    private Double parseDouble(String s){try{return s==null?null:Double.parseDouble(s.trim());}catch(Exception e){return null;}}
    private Double parseDoubleNullable(String s){return parseDouble(s);}
}
