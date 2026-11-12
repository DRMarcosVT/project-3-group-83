package view;

import java.util.ArrayList;
import java.util.Scanner;
import model.Order;
import model.Trade;

/**
 * The console view. It prints what the controller hands it and returns what
 * the user types. It holds no reference to the order book and reads orders
 * and trades only through their getters.
 */
public class ConsoleView {

    private final Scanner in;

    /**
     * Opens a Scanner on System.in.
     */
    public ConsoleView() {
        in = new Scanner(System.in);
    }


    /**
     * Prints a prompt and reads one line.
     *
     * @param prompt
     *            the text to show before the user types
     * @return the line the user typed, unchanged, or null when the input has
     *         ended (a file piped in ran out, or the user closed the input)
     */
    public String ask(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            System.out.println();
            return null;
        }
        return in.nextLine();
    }


    /**
     * Prints both waiting lines, one order per row.
     *
     * @param bids
     *            the waiting buy orders, best price first
     * @param asks
     *            the waiting sell orders, best price first
     */
    public void showBook(ArrayList<Order> bids, ArrayList<Order> asks) {
        System.out.println();
        System.out.println("SELL orders waiting (lowest price first)");
        showOrders(asks);
        System.out.println("BUY orders waiting (highest price first)");
        showOrders(bids);
    }


    /**
     * Prints the trade log.
     *
     * @param trades
     *            every trade so far, oldest first
     */
    public void showTrades(ArrayList<Trade> trades) {
        System.out.println("Trades (oldest first)");
        if (trades.isEmpty()) {
            System.out.println("  (none)");
        }
        for (Trade trade : trades) {
            System.out.println("  trader " + trade.getBuyer() + " bought "
                + trade.getQuantity() + " from trader " + trade.getSeller()
                + " at $" + trade.getPrice());
        }
        System.out.println();
    }


    /**
     * Prints an error message.
     *
     * @param message
     *            what went wrong
     */
    public void showError(String message) {
        System.out.println("Error: " + message);
    }


    /**
     * Prints a confirmation.
     *
     * @param message
     *            what happened
     */
    public void showMessage(String message) {
        System.out.println(message);
    }


    /**
     * Prints one side of the book.
     */
    private void showOrders(ArrayList<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("  (none)");
        }
        for (Order order : orders) {
            System.out.println("  order #" + order.getId() + "  trader "
                + order.getTrader() + "  " + order.getSide() + " " + order
                    .getRemaining() + " shares at $" + order.getPrice());
        }
    }
}
