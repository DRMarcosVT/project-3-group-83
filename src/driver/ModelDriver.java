package driver;

import model.Order;
import model.OrderBook;
import model.Side;
import model.Trade;

/**
 * Runs the model with no view and no controller. It sends a fixed list of
 * orders to an OrderBook, including bad ones, and prints the book and the
 * trade log with plain System.out calls.
 */
public class ModelDriver {

    /**
     * Runs the fixed list of orders.
     *
     * @param args
     *            ignored
     */
    public static void main(String[] args) {
        OrderBook book = new OrderBook();

        send(book, 1, Side.SELL, 50, 26);
        send(book, 3, Side.SELL, 50, 25);
        send(book, 4, Side.SELL, 50, 25);
        send(book, 2, Side.BUY, 120, 26);
        send(book, 2, Side.BUY, 40, 24);

        send(book, 5, Side.BUY, 0, 25);
        send(book, 5, Side.BUY, 10, 10001);
        send(book, 0, Side.SELL, 10, 25);
        send(book, 1, Side.BUY, 10, 26);

        print(book);
    }


    /**
     * Sends one order and prints what the book answered.
     */
    private static void send(
        OrderBook book,
        int trader,
        Side side,
        int quantity,
        int price) {
        String text = "trader " + trader + " " + side + " " + quantity
            + " at $" + price;
        try {
            int id = book.submit(trader, side, quantity, price);
            System.out.println("accepted as order #" + id + ": " + text);
        }
        catch (IllegalArgumentException e) {
            System.out.println("rejected: " + text + " -> " + e.getMessage());
        }
    }


    /**
     * Prints both sides of the book and the trade log.
     */
    private static void print(OrderBook book) {
        System.out.println();
        System.out.println("asks, lowest price first:");
        for (Order order : book.getAsks()) {
            System.out.println("  #" + order.getId() + " trader " + order
                .getTrader() + " " + order.getRemaining() + " at $" + order
                    .getPrice());
        }
        System.out.println("bids, highest price first:");
        for (Order order : book.getBids()) {
            System.out.println("  #" + order.getId() + " trader " + order
                .getTrader() + " " + order.getRemaining() + " at $" + order
                    .getPrice());
        }
        System.out.println("trades, oldest first:");
        for (Trade trade : book.getTrades()) {
            System.out.println("  trader " + trade.getBuyer() + " bought "
                + trade.getQuantity() + " from trader " + trade.getSeller()
                + " at $" + trade.getPrice());
        }
    }
}
