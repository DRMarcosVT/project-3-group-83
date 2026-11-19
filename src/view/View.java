package view;

import java.util.ArrayList;
import model.Order;
import model.Trade;

/**
 * What the controller needs from a view: one way to read a line and four ways
 * to show something. ConsoleView implements it for the terminal; a GUI or a
 * test's fake view can implement it without changing OrderController.
 */
public interface View {

    /**
     * Shows a prompt and reads one line.
     *
     * @param prompt
     *            the text to show before the user types
     * @return the line the user typed, unchanged, or null when the input has
     *         ended
     */
    String ask(String prompt);


    /**
     * Shows both waiting lines.
     *
     * @param bids
     *            the waiting buy orders, best price first
     * @param asks
     *            the waiting sell orders, best price first
     */
    void showBook(ArrayList<Order> bids, ArrayList<Order> asks);


    /**
     * Shows a list of trades.
     *
     * @param trades
     *            the trades to show, oldest first
     */
    void showTrades(ArrayList<Trade> trades);


    /**
     * Shows an error message.
     *
     * @param message
     *            what went wrong
     */
    void showError(String message);


    /**
     * Shows a confirmation.
     *
     * @param message
     *            what happened
     */
    void showMessage(String message);
}
