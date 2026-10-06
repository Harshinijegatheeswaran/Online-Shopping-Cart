import java.util.*;

/**
 * Online Shopping Cart - console application
 * Compile: javac ShoppingCartApp.java
 * Run:     java ShoppingCartApp
 */
public class ShoppingCartApp {

    // ---------- Model classes ----------
    static class Product {
        private final int id;
        private final String name;
        private final double price;
        private int stock;

        Product(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        int getId() { return id; }
        String getName() { return name; }
        double getPrice() { return price; }
        int getStock() { return stock; }
        void reduceStock(int qty) { stock -= qty; }
    }

    static class CartItem {
        private final Product product;
        private int quantity;

        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        Product getProduct() { return product; }
        int getQuantity() { return quantity; }
        void setQuantity(int quantity) { this.quantity = quantity; }
        double getTotal() { return product.getPrice() * quantity; }
    }

    // ---------- Shopping cart ----------
    static class ShoppingCart {
        private static final double TAX_RATE = 0.18; // 18% GST
        private final Map<Integer, CartItem> items = new LinkedHashMap<>();
        private double discountPercent = 0;

        boolean addItem(Product p, int qty) {
            if (qty <= 0) {
                System.out.println("Quantity must be at least 1.");
                return false;
            }
            int existing = items.containsKey(p.getId()) ? items.get(p.getId()).getQuantity() : 0;
            if (existing + qty > p.getStock()) {
                System.out.println("Only " + p.getStock() + " in stock for " + p.getName() + ".");
                return false;
            }
            if (existing > 0) {
                items.get(p.getId()).setQuantity(existing + qty);
            } else {
                items.put(p.getId(), new CartItem(p, qty));
            }
            System.out.println(qty + " x " + p.getName() + " added to cart.");
            return true;
        }

        boolean removeItem(int productId) {
            if (items.remove(productId) != null) {
                System.out.println("Item removed from cart.");
                return true;
            }
            System.out.println("That item is not in your cart.");
            return false;
        }

        boolean updateQuantity(int productId, int qty) {
            CartItem item = items.get(productId);
            if (item == null) {
                System.out.println("That item is not in your cart.");
                return false;
            }
            if (qty <= 0) return removeItem(productId);
            if (qty > item.getProduct().getStock()) {
                System.out.println("Only " + item.getProduct().getStock() + " in stock.");
                return false;
            }
            item.setQuantity(qty);
            System.out.println("Quantity updated.");
            return true;
        }

        boolean applyCoupon(String code) {
            switch (code.trim().toUpperCase()) {
                case "SAVE10": discountPercent = 10; break;
                case "SAVE20": discountPercent = 20; break;
                default:
                    System.out.println("Invalid coupon code.");
                    return false;
            }
            System.out.println("Coupon applied: " + discountPercent + "% off.");
            return true;
        }

        double getSubtotal() {
            double sum = 0;
            for (CartItem i : items.values()) sum += i.getTotal();
            return sum;
        }

        double getDiscount() { return getSubtotal() * discountPercent / 100; }
        double getTax() { return (getSubtotal() - getDiscount()) * TAX_RATE; }
        double getGrandTotal() { return getSubtotal() - getDiscount() + getTax(); }
        boolean isEmpty() { return items.isEmpty(); }

        void display() {
            if (items.isEmpty()) {
                System.out.println("\nYour cart is empty.");
                return;
            }
            System.out.println("\n================= YOUR CART =================");
            System.out.printf("%-4s %-20s %8s %5s %10s%n", "ID", "Product", "Price", "Qty", "Total");
            System.out.println("---------------------------------------------");
            for (CartItem i : items.values()) {
                System.out.printf("%-4d %-20s %8.2f %5d %10.2f%n",
                        i.getProduct().getId(), i.getProduct().getName(),
                        i.getProduct().getPrice(), i.getQuantity(), i.getTotal());
            }
            System.out.println("---------------------------------------------");
            System.out.printf("%-31s %13.2f%n", "Subtotal:", getSubtotal());
            if (discountPercent > 0) {
                System.out.printf("%-31s %13.2f%n", "Discount (" + (int) discountPercent + "%):", -getDiscount());
            }
            System.out.printf("%-31s %13.2f%n", "Tax (18%):", getTax());
            System.out.printf("%-31s %13.2f%n", "GRAND TOTAL:", getGrandTotal());
            System.out.println("=============================================");
        }

        void checkout() {
            if (items.isEmpty()) {
                System.out.println("Cart is empty. Nothing to checkout.");
                return;
            }
            display();
            for (CartItem i : items.values()) i.getProduct().reduceStock(i.getQuantity());
            System.out.println("\nOrder placed successfully! Thank you for shopping with us.");
            items.clear();
            discountPercent = 0;
        }
    }

    // ---------- Main program ----------
    private static final Scanner sc = new Scanner(System.in);
    private static final List<Product> catalog = new ArrayList<>();

    public static void main(String[] args) {
        catalog.add(new Product(1, "Laptop", 55000.00, 5));
        catalog.add(new Product(2, "Smartphone", 20000.00, 10));
        catalog.add(new Product(3, "Headphones", 1500.00, 25));
        catalog.add(new Product(4, "Keyboard", 800.00, 30));
        catalog.add(new Product(5, "Mouse", 450.00, 40));
        catalog.add(new Product(6, "USB Flash Drive", 600.00, 50));

        ShoppingCart cart = new ShoppingCart();
        System.out.println("Welcome to the Online Shopping Cart!");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. View products");
            System.out.println("2. Add item to cart");
            System.out.println("3. Remove item from cart");
            System.out.println("4. Update item quantity");
            System.out.println("5. View cart");
            System.out.println("6. Apply coupon (SAVE10 / SAVE20)");
            System.out.println("7. Checkout");
            System.out.println("8. Exit");

            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: showProducts(); break;
                case 2: {
                    showProducts();
                    Product p = findProduct(readInt("Product ID: "));
                    if (p == null) System.out.println("Product not found.");
                    else cart.addItem(p, readInt("Quantity: "));
                    break;
                }
                case 3: cart.removeItem(readInt("Product ID to remove: ")); break;
                case 4: {
                    int id = readInt("Product ID: ");
                    cart.updateQuantity(id, readInt("New quantity: "));
                    break;
                }
                case 5: cart.display(); break;
                case 6:
                    System.out.print("Coupon code: ");
                    cart.applyCoupon(sc.nextLine());
                    break;
                case 7: cart.checkout(); break;
                case 8:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
        sc.close();
    }

    private static void showProducts() {
        System.out.println("\n============== PRODUCTS ==============");
        System.out.printf("%-4s %-20s %10s %7s%n", "ID", "Name", "Price", "Stock");
        System.out.println("--------------------------------------");
        for (Product p : catalog) {
            System.out.printf("%-4d %-20s %10.2f %7d%n", p.getId(), p.getName(), p.getPrice(), p.getStock());
        }
    }

    private static Product findProduct(int id) {
        for (Product p : catalog) if (p.getId() == id) return p;
        return null;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
