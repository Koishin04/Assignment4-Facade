package restaurant;

import restaurant.facade.AdminFacade;
import restaurant.facade.RestaurantFacade;
import restaurant.chain.ChainProgress;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Path directory = Path.of("data-v2");

        if (args.length > 0) {
            directory = Path.of(args[0]);
        }

        RestaurantFacade restaurant = new RestaurantFacade(directory);
        AdminFacade admin = new AdminFacade(directory);
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("\nRESTAURANT KITCHEN");
            System.out.println("1. New order");
            System.out.println("2. Inspect saved state");
            System.out.println("3. Resume order");
            System.out.println("4. Recover all unfinished orders");
            System.out.println("5. Occupy all stations (demo)");
            System.out.println("6. Release demo stations");
            System.out.println("7. Compensate unpaid order");
            System.out.println("8. Finish confirmed kitchen task");
            System.out.println("0. Exit");

            if (!input.hasNextLine()) {
                break;
            }

            String choice = input.nextLine().trim();

            try {
                if (choice.equals("0")) {
                    break;
                } else if (choice.equals("1")) {
                    String dish = read(
                            input,
                            "Dish: Burger / Pizza / Pasta"
                    );

                    int quantity = Integer.parseInt(
                            read(input, "Quantity:")
                    );

                    String address = read(
                            input,
                            "Delivery address:"
                    );

                    String payment = read(
                            input,
                            "Approve test payment? y / n"
                    );

                    if (!payment.equals("y") && !payment.equals("n")) {
                        throw new IllegalArgumentException("Enter y or n");
                    }

                    int steps = Integer.parseInt(
                            read(
                                    input,
                                    "Stop after: 1 = A, 2 = B, 3 = full chain"
                            )
                    );

                    ChainProgress chain = restaurant.start(
                            dish,
                            quantity,
                            address,
                            payment.equals("y"),
                            steps
                    );

                    print(chain);
                } else if (choice.equals("2")) {
                    System.out.println(admin.inspect());
                } else if (choice.equals("3")) {
                    print(
                            restaurant.resume(
                                    read(input, "Order ID:")
                            )
                    );
                } else if (choice.equals("4")) {
                    restaurant.recoverAll();
                    System.out.println(admin.inspect());
                } else if (choice.equals("5")) {
                    admin.occupyAllStations();
                } else if (choice.equals("6")) {
                    admin.releaseDemoStations();
                } else if (choice.equals("7")) {
                    print(
                            admin.cancel(
                                    read(input, "Order ID:")
                            )
                    );
                } else if (choice.equals("8")) {
                    admin.finishKitchen(
                            read(input, "Order ID:")
                    );
                } else {
                    System.out.println("Unknown option");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());

                System.out.println(
                        "For an interrupted saved order, "
                                + "use recovery after fixing the error."
                );
            }
        }
    }

    private static String read(Scanner input, String prompt) {
        System.out.println(prompt);

        if (!input.hasNextLine()) {
            throw new IllegalStateException("Input closed");
        }

        return input.nextLine().trim();
    }

    private static void print(ChainProgress chain) {
        System.out.println("ID: " + chain.getId());
        System.out.println("Status: " + chain.getStatus());
        System.out.println(chain.getEvents());
    }
}