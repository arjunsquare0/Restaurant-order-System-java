package qwert;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MenuItem {
    private String name;
    private double price;

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }

    public String getLabel() {
        return String.format("%s (₹%.2f)", name, price);
    }
}

class OrderItem {
    private MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    
    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }

    public String getBillLine() {
        return String.format("%-15s x%-4d ₹%-10.2f\n",
                item.getName(), quantity, getTotalPrice());
    }
}

interface Discount {
    double applyDiscount(double subtotal);
    String getDescription();
}

class NoDiscount implements Discount {
    @Override
    public double applyDiscount(double subtotal) {
        return 0;
    }
    @Override
    public String getDescription() {
        return "No Discount";
    }
    @Override
    public String toString() {
        return "No Offer Available";
    }
}

class PercentageDiscount implements Discount {
    private double percentage;

    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double applyDiscount(double subtotal) {
        return subtotal * percentage;
    }
    @Override
    public String getDescription() {
        return String.format("Discount (%.0f%%)", percentage * 100);
    }
    @Override
    public String toString() {
        return String.format("%.0f%% Off", percentage * 100);
    }
}

class FixedAmountDiscount implements Discount {
    private double amount;

    public FixedAmountDiscount(double amount) {
        this.amount = amount;
    }

    @Override
    public double applyDiscount(double subtotal) {
        return Math.min(amount, subtotal);
    }
    @Override
    public String getDescription() {
        return String.format("Fixed Discount (₹%.2f)", amount);
    }
    @Override
    public String toString() {
        return String.format("₹%.2f Off", amount);
    }
}

interface Tax {
    double calculateTax(double amount);
    String getDescription();
}

class PercentageTax implements Tax {
    private String name;
    private double rate;

    public PercentageTax(String name, double rate) {
        this.name = name;
        this.rate = rate;
    }

    @Override
    public double calculateTax(double amount) {
        return amount * rate;
    }

    @Override
    public String getDescription() {
        return String.format("%s (%.1f%%)", name, rate * 100);
    }
}

public class RestaurantOrderSystemOOP extends JFrame implements ActionListener {

    private List<MenuItem> menuItems;
    private Map<MenuItem, JSpinner> itemSpinners;
    private List<Tax> taxes;
    private List<String> orderHistory; 

    private JTextArea billArea;
    private JButton calculateButton;
    private JComboBox<Discount> discountSelector;
    
    private JTextArea orderHistoryArea;
    private JButton confirmOrderButton;
    private JLabel discountLabel;
    private JButton cancelOrderButton;

    public RestaurantOrderSystemOOP() {
        setTitle("Restaurant Order System (OOP Version)");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initializeData();

        JPanel menuPanel = createMenuPanel();
        add(new JScrollPane(menuPanel), BorderLayout.CENTER);

        JPanel billAndHistoryPanel = createBillAndHistoryPanel();
        add(billAndHistoryPanel, BorderLayout.EAST);

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void initializeData() {
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Burger", 150.00));
        menuItems.add(new MenuItem("Pizza", 350.00));
        menuItems.add(new MenuItem("Fries", 80.00));
        menuItems.add(new MenuItem("Soda", 40.00));
        menuItems.add(new MenuItem("Salad", 120.00));
        menuItems.add(new MenuItem("Coffee", 60.00));

        itemSpinners = new HashMap<>();
        
        taxes = new ArrayList<>();
        taxes.add(new PercentageTax("SGST", 0.025));
        taxes.add(new PercentageTax("CGST", 0.025));
        
        orderHistory = new ArrayList<>();
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(menuItems.size(), 3, 10, 10));
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu Items"));

        for (MenuItem item : menuItems) {
            JLabel itemLabel = new JLabel(item.getLabel());
            JLabel quantityLabel = new JLabel("Quantity:");
            quantityLabel.setHorizontalAlignment(JLabel.RIGHT);

            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
            
            itemSpinners.put(item, spinner);

            menuPanel.add(itemLabel);
            menuPanel.add(quantityLabel);
            menuPanel.add(spinner);
        }
        return menuPanel;
    }

    private JPanel createBillAndHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bill & Order History"));

        billArea = new JTextArea(15, 30);
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane billScrollPane = new JScrollPane(billArea);
        billScrollPane.setBorder(BorderFactory.createTitledBorder("Current Bill"));

        orderHistoryArea = new JTextArea(10, 30);
        orderHistoryArea.setEditable(false);
        orderHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane historyScrollPane = new JScrollPane(orderHistoryArea);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Confirmed Orders"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, billScrollPane, historyScrollPane);
        splitPane.setResizeWeight(0.6);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        discountSelector = new JComboBox<>();
        discountSelector.addItem(new NoDiscount());
        discountSelector.addItem(new PercentageDiscount(0.10));
        discountSelector.addItem(new PercentageDiscount(0.20));
        discountSelector.addItem(new FixedAmountDiscount(50.0));

        discountLabel = new JLabel();
        updateDiscountUI();
        
        controlPanel.add(discountLabel);
        controlPanel.add(discountSelector);

        calculateButton = new JButton("Calculate Total Bill");
        calculateButton.addActionListener(this);
        controlPanel.add(calculateButton);

        confirmOrderButton = new JButton("Confirm Order");
        confirmOrderButton.addActionListener(this);
        controlPanel.add(confirmOrderButton);

        cancelOrderButton = new JButton("Cancel Order");
        cancelOrderButton.addActionListener(this);
        controlPanel.add(cancelOrderButton);

        return controlPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == calculateButton) {
            generateBillText(false);
        } else if (e.getSource() == confirmOrderButton) {
            generateBillText(true);
        } else if (e.getSource() == cancelOrderButton) {
            cancelOrder();
        }
    }

    private void generateBillText(boolean isConfirming) {
        StringBuilder billText = new StringBuilder();
        billText.append("     ~~~ YOUR RECEIPT ~~~     \n\n");
        billText.append(String.format("%-15s %-5s %-10s\n", "Item", "Qty", "Price"));
        billText.append("--------------------------------\n");

        List<OrderItem> currentOrder = new ArrayList<>();
        double subtotal = 0.0;

        for (MenuItem item : menuItems) {
            int quantity = (int) itemSpinners.get(item).getValue();
            if (quantity > 0) {
                OrderItem orderItem = new OrderItem(item, quantity);
                currentOrder.add(orderItem);
                subtotal += orderItem.getTotalPrice();
                billText.append(orderItem.getBillLine());
            }
        }
        
        if (currentOrder.isEmpty()) {
            billArea.setText("Please select at least one item.");
            return;
        }

        billText.append("--------------------------------\n");
        billText.append(String.format("%-22s ₹%-10.2f\n", "Subtotal:", subtotal));

        Discount selectedDiscount = (Discount) discountSelector.getSelectedItem();
        double discountAmount = selectedDiscount.applyDiscount(subtotal);
        
        if (discountAmount > 0) {
             billText.append(String.format("%-22s -₹%-10.2f\n",
                     selectedDiscount.getDescription() + ":", discountAmount));
        }

        double taxableAmount = subtotal - discountAmount;
        if (discountAmount > 0) {
            billText.append("--------------------------------\n");
            billText.append(String.format("%-22s ₹%-10.2f\n", "Taxable Amount:", taxableAmount));
        }
        billText.append("--------------------------------\n");

        double totalTax = 0;
        for (Tax tax : taxes) {
            double taxAmount = tax.calculateTax(taxableAmount);
            totalTax += taxAmount;
            billText.append(String.format("%-22s +₹%-10.2f\n",
                    tax.getDescription() + ":", taxAmount));
        }

        double grandTotal = taxableAmount + totalTax;
        billText.append("================================\n");
        billText.append(String.format("%-22s ₹%-10.2f\n", "GRAND TOTAL:", grandTotal));
        billText.append("================================\n\n");
        billText.append("    Thank you for your order!   \n");
        
        if (isConfirming) {
            orderHistory.add(billText.toString());
            
            updateHistoryArea();
            
            if (!(selectedDiscount instanceof NoDiscount)) {
                discountSelector.removeItem(selectedDiscount);
                updateDiscountUI();
            }
            
            resetOrderForm();
            
            billArea.setText("--- Order Confirmed! ---\n\n" + billText.toString());
            
        } else {
            billArea.setText(billText.toString());
        }
    }

    private void cancelOrder() {
        boolean itemsSelected = false;
        for (JSpinner spinner : itemSpinners.values()) {
            if ((int) spinner.getValue() > 0) {
                itemsSelected = true;
                break;
            }
        }
        
        if (!itemsSelected) {
            billArea.setText("Nothing to cancel.");
            return;
        }

        generateBillText(false);
        
        String currentBillText = billArea.getText();

        String canceledRecord = "--- ORDER CANCELED ---\n" + currentBillText;
        
        orderHistory.add(canceledRecord);
        updateHistoryArea();
        
        resetOrderForm();
        
        billArea.setText("Order Canceled.\nSee history for details.");
    }

    private void updateHistoryArea() {
        StringBuilder historyText = new StringBuilder();
        for (int i = orderHistory.size() - 1; i >= 0; i--) {
            historyText.append("--- ORDER #").append(i + 1).append(" ---\n");
            historyText.append(orderHistory.get(i));
            historyText.append("\n\n");
        }
        orderHistoryArea.setText(historyText.toString());
        orderHistoryArea.setCaretPosition(0);
    }

    private void resetOrderForm() {
        for (JSpinner spinner : itemSpinners.values()) {
            spinner.setValue(0);
        }
        if (discountSelector.getItemCount() > 0) {
            discountSelector.setSelectedIndex(0);
        }
    }
    
    private void updateDiscountUI() {
        if (discountSelector.getItemCount() <= 1) {
            discountLabel.setText("No Offers Available:");
            discountSelector.setEnabled(false);
        } else {
            discountLabel.setText("Select Discount:");
            discountSelector.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantOrderSystemOOP app = new RestaurantOrderSystemOOP();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}