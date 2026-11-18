package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import model.Order;
import model.OrderBook;
import model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.ConsoleView;

/**
 * Tests the bad input OrderController catches, by running whole command
 * sessions through a fake view. Nothing is typed at a keyboard and nothing is
 * printed: FakeView hands the controller a fixed list of lines and records
 * what the controller asks it to show.
 */
public class OrderControllerTest {

    /**
     * A ConsoleView that reads from a list of lines in place of the keyboard
     * and records every prompt, error, message, book and trade list in place
     * of printing them. Its constructor still runs ConsoleView's, which opens
     * a Scanner on System.in, but no method here reads from it.
     */
    private static class FakeView extends ConsoleView {

        private final ArrayList<String> lines;
        private int next;
        private final ArrayList<String> prompts = new ArrayList<String>();
        private final ArrayList<String> errors = new ArrayList<String>();
        private final ArrayList<String> messages = new ArrayList<String>();
        private ArrayList<Order> shownBids;
        private ArrayList<Order> shownAsks;
        private ArrayList<Trade> shownTrades;

        /**
         * Stores the lines the "user" will type, in order.
         */
        FakeView(String... typed) {
            lines = new ArrayList<String>(Arrays.asList(typed));
            next = 0;
        }


        /**
         * Returns the next typed line, or null once the lines run out, which
         * is what ConsoleView.ask returns when the input has ended.
         */
        @Override
        public String ask(String prompt) {
            prompts.add(prompt);
            if (next >= lines.size()) {
                return null;
            }
            String line = lines.get(next);
            next++;
            return line;
        }


        @Override
        public void showBook(ArrayList<Order> bids, ArrayList<Order> asks) {
            shownBids = bids;
            shownAsks = asks;
        }


        @Override
        public void showTrades(ArrayList<Trade> trades) {
            shownTrades = trades;
        }


        @Override
        public void showError(String message) {
            errors.add(message);
        }


        @Override
        public void showMessage(String message) {
            messages.add(message);
        }
    }

    private OrderBook book;

    /**
     * Makes an empty book before each test.
     */
    @BeforeEach
    public void setUp() {
        book = new OrderBook();
    }


    /**
     * Runs one controller over the given typed lines and returns the view
     * that recorded what happened.
     */
    private FakeView session(String... typed) {
        FakeView view = new FakeView(typed);
        new OrderController(book, view).run();
        return view;
    }


    /**
     * A valid starting trader ID is used for the first order.
     */
    @Test
    public void startAcceptsTraderId() {
        FakeView view = session("1", "sell", "100", "25", "quit");
        assertTrue(view.errors.isEmpty());
        assertEquals(1, book.getAsks().size());
        assertEquals(1, book.getAsks().get(0).getTrader());
        assertTrue(view.messages.contains("Order #1 placed."));
    }


    /**
     * Text, 0, a negative number and a blank line are each rejected as the
     * starting trader ID, and the prompt repeats until a valid one arrives.
     */
    @Test
    public void startRejectsBadTraderIds() {
        FakeView view = session("ten", "0", "-4", "", "1", "sell", "10", "25",
            "quit");
        assertEquals(4, view.errors.size());
        assertEquals(1, book.getAsks().get(0).getTrader());
    }


    /**
     * quit at the trader prompt ends the program before any command.
     */
    @Test
    public void quitAtTraderPrompt() {
        FakeView view = session("quit");
        assertEquals(1, view.prompts.size());
        assertTrue(book.getAsks().isEmpty());
        assertTrue(book.getBids().isEmpty());
    }


    /**
     * A buy that crosses a waiting sell trades, and with fewer than five
     * trades the whole log is shown with no "Showing the newest" line.
     */
    @Test
    public void buyFillsWaitingSell() {
        FakeView view = session("1", "sell", "100", "25", "switch 2", "buy",
            "40", "25", "quit");
        assertTrue(view.errors.isEmpty());
        assertEquals(1, view.shownTrades.size());
        Trade trade = view.shownTrades.get(0);
        assertEquals(2, trade.getBuyer());
        assertEquals(1, trade.getSeller());
        assertEquals(40, trade.getQuantity());
        for (String message : view.messages) {
            assertFalse(message.startsWith("Showing the newest"));
        }
    }


    /**
     * A quantity of 0, a negative one, one above 10,000, text, and a number
     * too big for an int are each rejected before the price is asked for.
     */
    @Test
    public void badQuantityRejectedBeforePrice() {
        FakeView view = session("1", "buy", "0", "buy", "-5", "buy", "10001",
            "buy", "ten", "buy", "99999999999", "quit");
        assertEquals(5, view.errors.size());
        assertFalse(view.prompts.contains("price in whole dollars: "));
        assertTrue(book.getBids().isEmpty());
        assertTrue(view.messages.isEmpty());
    }


    /**
     * A price of 0, a negative one, one above 10,000, and a decimal are each
     * rejected and leave the book empty.
     */
    @Test
    public void badPriceRejected() {
        FakeView view = session("1", "buy", "10", "0", "buy", "10", "-5",
            "buy", "10", "10001", "buy", "10", "10.5", "quit");
        assertEquals(4, view.errors.size());
        assertTrue(book.getBids().isEmpty());
    }


    /**
     * A word that is not a command, a blank line and two sides on one line
     * are each rejected with the list of commands.
     */
    @Test
    public void unknownCommandsRejected() {
        FakeView view = session("1", "hold", "", "BUY SELL", "quit");
        assertEquals(3, view.errors.size());
        for (String error : view.errors) {
            assertTrue(error.contains("is not a command"));
        }
    }


    /**
     * switch with a valid ID changes who the next order belongs to.
     */
    @Test
    public void switchChangesTrader() {
        FakeView view = session("1", "switch 2", "sell", "10", "25", "quit");
        assertTrue(view.errors.isEmpty());
        assertEquals(2, book.getAsks().get(0).getTrader());
    }


    /**
     * switch with no ID, text, 0, a negative ID or two IDs is rejected and
     * the current trader stays the same.
     */
    @Test
    public void switchRejectsBadInput() {
        FakeView view = session("1", "switch", "switch two", "switch 0",
            "switch -1", "switch 2 3", "sell", "10", "25", "quit");
        assertEquals(5, view.errors.size());
        assertEquals(1, book.getAsks().get(0).getTrader());
    }


    /**
     * cancel with a valid ID removes the order and reports the shares left.
     */
    @Test
    public void cancelRemovesOrder() {
        FakeView view = session("1", "sell", "100", "25", "cancel 1", "quit");
        assertTrue(view.errors.isEmpty());
        assertTrue(book.getAsks().isEmpty());
        assertTrue(view.messages.contains(
            "Cancelled order #1, 100 shares unfilled."));
    }


    /**
     * cancel with no ID, text or two IDs is rejected before the book is
     * called, and the order stays.
     */
    @Test
    public void cancelRejectsBadInput() {
        FakeView view = session("1", "sell", "100", "25", "cancel",
            "cancel two", "cancel 1 2", "quit");
        assertEquals(3, view.errors.size());
        assertEquals(1, book.getAsks().size());
    }


    /**
     * The book's own cancel errors (another trader's order, an ID never
     * issued) reach the user through showError, and the order stays.
     */
    @Test
    public void cancelShowsBookErrors() {
        FakeView view = session("1", "sell", "100", "25", "switch 2",
            "cancel 1", "cancel 99", "quit");
        assertEquals(2, view.errors.size());
        assertTrue(view.errors.get(0).contains("belongs to another trader"));
        assertTrue(view.errors.get(1).contains("does not exist"));
        assertEquals(1, book.getAsks().size());
    }


    /**
     * A self-trade is shown as an error and changes nothing.
     */
    @Test
    public void selfTradeShownAsError() {
        FakeView view = session("1", "sell", "100", "25", "buy", "10", "25",
            "quit");
        assertEquals(1, view.errors.size());
        assertEquals(100, book.getAsks().get(0).getRemaining());
        assertTrue(book.getBids().isEmpty());
        assertTrue(book.getTrades().isEmpty());
    }


    /**
     * mine hands the view only the current trader's orders.
     */
    @Test
    public void mineShowsOnlyCurrentTrader() {
        FakeView view = session("1", "sell", "10", "25", "switch 2", "sell",
            "10", "26", "mine", "quit");
        assertEquals(1, view.shownAsks.size());
        assertEquals(2, view.shownAsks.get(0).getTrader());
        assertTrue(view.shownBids.isEmpty());
    }


    /**
     * When the input ends without quit, run returns instead of throwing, and
     * the orders already placed stay.
     */
    @Test
    public void inputEndsBeforeQuit() {
        FakeView view = session("1", "sell", "10", "25");
        assertTrue(view.errors.isEmpty());
        assertEquals(1, book.getAsks().size());
    }


    /**
     * When the input ends in the middle of an order, the half-typed order is
     * rejected with an error and run returns.
     */
    @Test
    public void inputEndsMidOrder() {
        FakeView view = session("1", "buy", "10");
        assertEquals(1, view.errors.size());
        assertTrue(view.errors.get(0).contains("input ended"));
        assertTrue(book.getBids().isEmpty());
    }


    /**
     * With seven trades, the view is handed only the newest five, oldest
     * first, and a line says how many were left out. The book keeps all
     * seven.
     */
    @Test
    public void tradeLogShowsNewestFive() {
        FakeView view = session("1", "sell", "1", "21", "sell", "1", "22",
            "sell", "1", "23", "sell", "1", "24", "sell", "1", "25", "sell",
            "1", "26", "sell", "1", "27", "switch 2", "buy", "7", "27", "quit");
        assertEquals(7, book.getTrades().size());
        assertEquals(5, view.shownTrades.size());
        assertEquals(23, view.shownTrades.get(0).getPrice());
        assertEquals(27, view.shownTrades.get(4).getPrice());
        assertTrue(view.messages.contains("Showing the newest 5 of 7 trades."));
    }
}
