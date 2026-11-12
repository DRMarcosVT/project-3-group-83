package controller;

import model.Order;
import model.OrderBook;
import model.Side;
import view.ConsoleView;

/**
 * The controller. It remembers which trader is typing, turns typed commands
 * into calls on the order book, and tells the view what to show.
 */
public class OrderController {

    private static final String COMMANDS =
        "Type buy, sell, cancel <order ID>, mine, switch <trader ID> or quit.";

    private final OrderBook book;
    private final ConsoleView view;
    private int currentTrader;

    /**
     * Stores the book and the view this controller connects.
     *
     * @param book
     *            the order book
     * @param view
     *            the console view
     */
    public OrderController(OrderBook book, ConsoleView view) {
        this.book = book;
        this.view = view;
        this.currentTrader = 0;
    }


    /**
     * Asks for a trader ID, then reads commands until the user types quit or
     * the input ends. Bad input gets an error message and another prompt; it
     * never changes the book or the current trader.
     */
    public void run() {
        boolean running = askForTrader();
        while (running) {
            String line = view.ask("trader " + currentTrader
                + " > buy, sell, cancel <id>, mine, switch <id> or quit: ");
            if (line == null || line.trim().equalsIgnoreCase("quit")) {
                running = false;
            }
            else {
                handle(line.trim());
            }
        }
    }


    /**
     * Makes one book, one view and one controller, then runs the loop.
     *
     * @param args
     *            ignored
     */
    public static void main(String[] args) {
        OrderController controller = new OrderController(new OrderBook(),
            new ConsoleView());
        controller.run();
    }


    /**
     * Asks for the starting trader ID until it gets a positive whole number.
     *
     * @return false if the user quit or the input ended first
     */
    private boolean askForTrader() {
        boolean chosen = false;
        boolean quit = false;
        while (!chosen && !quit) {
            String line = view.ask(
                "Your trader ID (a positive whole number), or quit: ");
            if (line == null || line.trim().equalsIgnoreCase("quit")) {
                quit = true;
            }
            else {
                try {
                    currentTrader = parseTraderId(line);
                    chosen = true;
                }
                catch (IllegalArgumentException e) {
                    view.showError(e.getMessage());
                }
            }
        }
        return chosen;
    }


    /**
     * Carries out one command line that is not quit.
     */
    private void handle(String text) {
        String[] words = text.split("\\s+");
        try {
            if (words[0].equalsIgnoreCase("switch")) {
                if (words.length != 2) {
                    throw new IllegalArgumentException(
                        "Type switch and one trader ID, for example:"
                            + " switch 2");
                }
                currentTrader = parseTraderId(words[1]);
            }
            else if (words[0].equalsIgnoreCase("cancel")) {
                if (words.length != 2) {
                    throw new IllegalArgumentException(
                        "Type cancel and one order ID, for example:"
                            + " cancel 3");
                }
                int orderId = parseNumber(words[1], "Order ID");
                Order cancelled = book.cancel(currentTrader, orderId);
                view.showMessage("Cancelled order #" + orderId + ", "
                    + cancelled.getRemaining() + " shares unfilled.");
                view.showBook(book.getBids(), book.getAsks());
                view.showTrades(book.getTrades());
            }
            else if (text.equalsIgnoreCase("mine")) {
                view.showBook(book.getBids(currentTrader), book.getAsks(
                    currentTrader));
            }
            else {
                Side side = parseSide(text);
                int quantity = parseNumber(view.ask("quantity: "), "Quantity");
                int price = parseNumber(view.ask("price in whole dollars: "),
                    "Price");
                int orderId = book.submit(currentTrader, side, quantity,
                    price);
                view.showMessage("Order #" + orderId + " placed.");
                view.showBook(book.getBids(), book.getAsks());
                view.showTrades(book.getTrades());
            }
        }
        catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        }
    }


    /**
     * Turns typed text into BUY or SELL.
     */
    private Side parseSide(String text) {
        try {
            return Side.valueOf(text.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("\"" + text
                + "\" is not a command. " + COMMANDS);
        }
    }


    /**
     * Turns typed text into a whole number.
     */
    private int parseNumber(String text, String what) {
        if (text == null) {
            throw new IllegalArgumentException("The input ended before "
                + what.toLowerCase() + " was typed.");
        }
        try {
            return Integer.parseInt(text.trim());
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(what
                + " must be a whole number, got \"" + text + "\".");
        }
    }


    /**
     * Turns typed text into a trader ID of 1 or more.
     */
    private int parseTraderId(String text) {
        int id = parseNumber(text, "Trader ID");
        if (id < 1) {
            throw new IllegalArgumentException(
                "Trader ID must be 1 or more, got " + id + ".");
        }
        return id;
    }
}
