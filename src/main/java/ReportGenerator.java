import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReportGenerator {
    static class TaskRunnable implements Runnable {
        private final String path;
        private double totalCost;
        private int totalAmount;
        private int totalDiscountSum;
        private int totalLines;
        private Product mostExpensiveProduct;
        private double highestCostAfterDiscount;

        public TaskRunnable(String path) {
            this.path = path;
            this.totalCost = 0;
            this.totalAmount = 0;
            this.totalDiscountSum = 0;
            this.totalLines = 0;
            this.highestCostAfterDiscount = 0;
            this.mostExpensiveProduct = null;
        }

        @Override
        public void run() {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length != 3) continue;

                    int productId = Integer.parseInt(parts[0].trim());
                    int amount = Integer.parseInt(parts[1].trim());
                    int discount = Integer.parseInt(parts[2].trim());

                    Product product = findProductById(productId);
                    if (product == null) continue;

                    double originalCost = product.getPrice() * amount;
                    double discountAmount = originalCost * discount / 100;
                    double costAfterDiscount = originalCost - discountAmount;

                    totalCost += costAfterDiscount;
                    totalAmount += amount;
                    totalDiscountSum += discount;
                    totalLines++;

                    if (costAfterDiscount > highestCostAfterDiscount) {
                        highestCostAfterDiscount = costAfterDiscount;
                        mostExpensiveProduct = product;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + path);
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.err.println("Error parsing numbers in file: " + path);
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.err.println("File not found: " + path);
            }
        }

        private Product findProductById(int productId) {
            for (Product product : productCatalog) {
                if (product != null && product.getProductID() == productId) {
                    return product;
                }
            }
            return null;
        }

        public void makeReport() {
            System.out.println("\n=== Order Report: " + path + " ===");
            System.out.printf("Total cost: $%.2f%n", totalCost);
            System.out.println("Total items bought: " + totalAmount);

            double averageDiscount = totalLines > 0 ? (double) totalDiscountSum / totalLines : 0;
            System.out.printf("Average discount: %.2f%%%n", averageDiscount);

            if (mostExpensiveProduct != null) {
                System.out.println("\nMost expensive purchase after discount:");
                System.out.printf("  Product: %s (ID: %d)%n",
                        mostExpensiveProduct.getProductName(),
                        mostExpensiveProduct.getProductID());
                System.out.printf("  Original price: $%.2f%n", mostExpensiveProduct.getPrice());
                System.out.printf("  Final cost: $%.2f%n", highestCostAfterDiscount);
            } else {
                System.out.println("No purchases found in this order.");
            }
            System.out.println("========================================");
        }
    }

    static class Product {
        private final int productID;
        private final String productName;
        private final double price;

        public Product(int productID, String productName, double price) {
            this.productID = productID;
            this.productName = productName;
            this.price = price;
        }

        public int getProductID() {
            return productID;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }
    }

    private static final String[] ORDER_FILES = {
            "2021_order_details.txt",
            "2022_order_details.txt",
            "2023_order_details.txt",
            "2024_order_details.txt"
    };

    static Product[] productCatalog = new Product[10];

    public static void loadProducts() throws IOException {
        try (InputStream is = ReportGenerator.class.getClassLoader().getResourceAsStream("Products.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            int index = 0;
            while ((line = reader.readLine()) != null && index < productCatalog.length) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                int productId = Integer.parseInt(parts[0].trim());
                String productName = parts[1].trim();
                double price = Double.parseDouble(parts[2].trim());

                productCatalog[index++] = new Product(productId, productName, price);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            System.out.println("Loading product catalog...");
            loadProducts();

            System.out.println("Processing order files...");
            TaskRunnable[] tasks = new TaskRunnable[ORDER_FILES.length];
            Thread[] threads = new Thread[ORDER_FILES.length];


            for (int i = 0; i < ORDER_FILES.length; i++) {
                tasks[i] = new TaskRunnable(ORDER_FILES[i]);
                threads[i] = new Thread(tasks[i]);
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("\nGenerating reports...");
            for (TaskRunnable task : tasks) {
                task.makeReport();
            }

            System.out.println("\nAll reports generated successfully!");
        } catch (IOException e) {
            System.err.println("Error loading products or processing files");
            e.printStackTrace();
        }
    }
}