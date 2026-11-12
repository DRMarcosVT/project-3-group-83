package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests OrderBook, Order and Trade with no view and no controller.
 */
public class OrderBookTest {

    private OrderBook book;

    /**
     * Makes an empty book before each test.
     */
    @BeforeEach
    public void setUp() {
        book = new OrderBook();
    }


    /**
     * An order with nothing to trade against waits on its own side.
     */
    @Test
    public void submitOrderWaits() {
        assertEquals(1, book.submit(1, Side.SELL, 100, 25));

        ArrayList<Order> asks = book.getAsks();
        assertEquals(1, asks.size());
        Order order = asks.get(0);
        assertEquals(1, order.getId());
        assertEquals(1, order.getTrader());
        assertEquals(Side.SELL, order.getSide());
        assertEquals(100, order.getRemaining());
        assertEquals(25, order.getPrice());
        assertTrue(book.getBids().isEmpty());
        assertTrue(book.getTrades().isEmpty());
    }


    /**
     * Every out-of-range value throws and leaves the book, the trade log and
     * the ID counter as they were.
     */
    @Test
    public void submitRejectsBadValues() {
        book.submit(9, Side.BUY, 10, 5);

        assertRejected(1, Side.SELL, 0, 25);
        assertRejected(1, Side.SELL, -5, 25);
        assertRejected(1, Side.SELL, 10001, 25);
        assertRejected(1, Side.SELL, 100, 0);
        assertRejected(1, Side.SELL, 100, -5);
        assertRejected(1, Side.SELL, 100, 10001);
        assertRejected(0, Side.SELL, 100, 25);
        assertRejected(-3, Side.SELL, 100, 25);
        assertRejected(1, null, 100, 25);

        assertEquals(1, book.getBids().size());
        assertEquals(10, book.getBids().get(0).getRemaining());
        assertTrue(book.getAsks().isEmpty());
        assertTrue(book.getTrades().isEmpty());
        assertEquals(2, book.submit(1, Side.SELL, 100, 25));
    }


    /**
     * The largest allowed quantity and price are accepted.
     */
    @Test
    public void submitAcceptsMaximums() {
        book.submit(1, Side.SELL, OrderBook.MAX_QUANTITY, OrderBook.MAX_PRICE);
        assertEquals(10000, book.getAsks().get(0).getRemaining());
        assertEquals(10000, book.getAsks().get(0).getPrice());
    }


    /**
     * A bigger order fills the waiting one at the waiting order's price, and
     * its remainder waits.
     */
    @Test
    public void submitFillsAndRemainderWaits() {
        book.submit(1, Side.SELL, 100, 25);
        assertEquals(2, book.submit(2, Side.BUY, 150, 30));

        ArrayList<Trade> trades = book.getTrades();
        assertEquals(1, trades.size());
        assertTrade(trades.get(0), 2, 1, 25, 100);

        assertTrue(book.getAsks().isEmpty());
        ArrayList<Order> bids = book.getBids();
        assertEquals(1, bids.size());
        assertEquals(2, bids.get(0).getId());
        assertEquals(2, bids.get(0).getTrader());
        assertEquals(50, bids.get(0).getRemaining());
        assertEquals(30, bids.get(0).getPrice());
    }


    /**
     * A smaller order fills part of the waiting one, which keeps its place.
     */
    @Test
    public void submitPartlyFillsWaitingOrder() {
        book.submit(1, Side.BUY, 100, 25);
        book.submit(2, Side.SELL, 40, 20);

        assertTrade(book.getTrades().get(0), 1, 2, 25, 40);
        assertEquals(60, book.getBids().get(0).getRemaining());
        assertTrue(book.getAsks().isEmpty());
    }


    /**
     * A trader cannot trade with their own waiting order; the rejected order
     * changes nothing.
     */
    @Test
    public void submitRejectsSelfTrade() {
        book.submit(1, Side.SELL, 100, 25);
        assertThrows(IllegalArgumentException.class, () -> book.submit(1,
            Side.BUY, 10, 25));

        assertEquals(1, book.getAsks().size());
        assertEquals(100, book.getAsks().get(0).getRemaining());
        assertTrue(book.getBids().isEmpty());
        assertTrue(book.getTrades().isEmpty());
    }


    /**
     * The same trader may wait on both sides when the prices cannot trade.
     */
    @Test
    public void submitAllowsSameTraderAtPricesThatDoNotMatch() {
        book.submit(1, Side.SELL, 100, 25);
        book.submit(1, Side.BUY, 10, 24);

        assertEquals(1, book.getAsks().size());
        assertEquals(1, book.getBids().size());
        assertTrue(book.getTrades().isEmpty());
    }


    /**
     * The best price fills first, and the oldest order fills first within a
     * price.
     */
    @Test
    public void submitFillsBestPriceThenOldest() {
        book.submit(1, Side.SELL, 50, 26);
        book.submit(3, Side.SELL, 50, 25);
        book.submit(4, Side.SELL, 50, 25);
        book.submit(2, Side.BUY, 120, 26);

        ArrayList<Trade> trades = book.getTrades();
        assertEquals(3, trades.size());
        assertTrade(trades.get(0), 2, 3, 25, 50);
        assertTrade(trades.get(1), 2, 4, 25, 50);
        assertTrade(trades.get(2), 2, 1, 26, 20);

        ArrayList<Order> asks = book.getAsks();
        assertEquals(1, asks.size());
        assertEquals(1, asks.get(0).getId());
        assertEquals(30, asks.get(0).getRemaining());
        assertTrue(book.getBids().isEmpty());
    }


    /**
     * A waiting order priced worse than the new order's limit is left alone.
     */
    @Test
    public void submitStopsAtLimitPrice() {
        book.submit(1, Side.SELL, 50, 30);
        book.submit(2, Side.BUY, 50, 29);

        assertTrue(book.getTrades().isEmpty());
        assertEquals(1, book.getAsks().size());
        assertEquals(1, book.getBids().size());
    }


    /**
     * Accepted orders are numbered 1, 2, 3, including one that fills
     * completely and never waits.
     */
    @Test
    public void submitNumbersAcceptedOrders() {
        assertEquals(1, book.submit(1, Side.SELL, 10, 25));
        assertEquals(2, book.submit(2, Side.BUY, 10, 25));
        assertEquals(3, book.submit(3, Side.BUY, 10, 20));
    }


    /**
     * A rejected order uses no number.
     */
    @Test
    public void submitRejectedOrderUsesNoNumber() {
        assertEquals(1, book.submit(1, Side.SELL, 10, 25));
        assertRejected(2, Side.BUY, 0, 25);
        assertEquals(2, book.submit(2, Side.BUY, 10, 20));
    }


    /**
     * getBids lists the highest price first and the oldest first within a
     * price.
     */
    @Test
    public void getBidsOrdersByPriceThenAge() {
        book.submit(1, Side.BUY, 10, 24);
        book.submit(2, Side.BUY, 10, 26);
        book.submit(3, Side.BUY, 10, 26);

        ArrayList<Order> bids = book.getBids();
        assertEquals(3, bids.size());
        assertEquals(2, bids.get(0).getTrader());
        assertEquals(3, bids.get(1).getTrader());
        assertEquals(1, bids.get(2).getTrader());
    }


    /**
     * getBids on an empty book is empty, and clearing the returned list does
     * not change the book.
     */
    @Test
    public void getBidsReturnsACopy() {
        assertTrue(book.getBids().isEmpty());
        book.submit(1, Side.BUY, 10, 24);
        book.getBids().clear();
        assertEquals(1, book.getBids().size());
    }


    /**
     * getAsks lists the lowest price first and the oldest first within a
     * price.
     */
    @Test
    public void getAsksOrdersByPriceThenAge() {
        book.submit(1, Side.SELL, 10, 27);
        book.submit(2, Side.SELL, 10, 25);
        book.submit(3, Side.SELL, 10, 25);

        ArrayList<Order> asks = book.getAsks();
        assertEquals(3, asks.size());
        assertEquals(2, asks.get(0).getTrader());
        assertEquals(3, asks.get(1).getTrader());
        assertEquals(1, asks.get(2).getTrader());
    }


    /**
     * getAsks on an empty book is empty, and clearing the returned list does
     * not change the book.
     */
    @Test
    public void getAsksReturnsACopy() {
        assertTrue(book.getAsks().isEmpty());
        book.submit(1, Side.SELL, 10, 27);
        book.getAsks().clear();
        assertEquals(1, book.getAsks().size());
    }


    /**
     * getTrades on a new book is empty, and clearing the returned list does
     * not change the log.
     */
    @Test
    public void getTradesReturnsACopy() {
        assertTrue(book.getTrades().isEmpty());
        book.submit(1, Side.SELL, 10, 25);
        book.submit(2, Side.BUY, 10, 25);
        book.getTrades().clear();
        assertEquals(1, book.getTrades().size());
    }


    /**
     * Cancelling a waiting order returns it and empties its side.
     */
    @Test
    public void cancelWaitingOrder() {
        book.submit(1, Side.SELL, 100, 25);
        Order cancelled = book.cancel(1, 1);
        assertEquals(1, cancelled.getId());
        assertEquals(100, cancelled.getRemaining());
        assertTrue(book.getAsks().isEmpty());
    }


    /**
     * After the only order at a price is cancelled, an order at that price
     * finds nothing to trade with and waits.
     */
    @Test
    public void cancelRemovesEmptyPrice() {
        book.submit(1, Side.SELL, 100, 25);
        book.cancel(1, 1);
        book.submit(2, Side.BUY, 10, 25);
        assertTrue(book.getTrades().isEmpty());
        assertEquals(1, book.getBids().size());
        assertEquals(10, book.getBids().get(0).getRemaining());
    }


    /**
     * Cancelling a partly filled order cancels only the unfilled shares; the
     * earlier trade stays.
     */
    @Test
    public void cancelPartlyFilledOrder() {
        book.submit(1, Side.SELL, 100, 25);
        book.submit(2, Side.BUY, 40, 25);
        assertEquals(60, book.cancel(1, 1).getRemaining());
        assertEquals(1, book.getTrades().size());
        assertTrade(book.getTrades().get(0), 2, 1, 25, 40);
        assertTrue(book.getAsks().isEmpty());
    }


    /**
     * Cancelling the older of two orders at one price lets the newer one fill
     * first.
     */
    @Test
    public void cancelOlderOrderAtSamePrice() {
        book.submit(1, Side.SELL, 50, 25);
        book.submit(3, Side.SELL, 50, 25);
        book.cancel(1, 1);
        book.submit(2, Side.BUY, 10, 25);
        assertTrade(book.getTrades().get(0), 2, 3, 25, 10);
        assertEquals(40, book.getAsks().get(0).getRemaining());
    }


    /**
     * A trader cannot cancel another trader's order; it stays in the book.
     */
    @Test
    public void cancelRejectsOtherTrader() {
        book.submit(1, Side.SELL, 100, 25);
        assertThrows(IllegalArgumentException.class, () -> book.cancel(2, 1));
        assertEquals(1, book.getAsks().size());
        assertEquals(100, book.getAsks().get(0).getRemaining());
    }


    /**
     * IDs that were never issued are rejected.
     */
    @Test
    public void cancelRejectsIdsNeverIssued() {
        assertThrows(IllegalArgumentException.class, () -> book.cancel(1, 0));
        assertThrows(IllegalArgumentException.class, () -> book.cancel(1, 99));
        book.submit(1, Side.SELL, 10, 25);
        assertThrows(IllegalArgumentException.class, () -> book.cancel(1, 2));
        assertEquals(1, book.getAsks().size());
    }


    /**
     * An order that filled completely cannot be cancelled.
     */
    @Test
    public void cancelRejectsFilledOrder() {
        book.submit(1, Side.SELL, 10, 25);
        book.submit(2, Side.BUY, 10, 25);
        assertThrows(IllegalArgumentException.class, () -> book.cancel(1, 1));
        assertThrows(IllegalArgumentException.class, () -> book.cancel(2, 2));
    }


    /**
     * The same order cannot be cancelled twice.
     */
    @Test
    public void cancelRejectsSecondCancel() {
        book.submit(1, Side.BUY, 10, 25);
        book.cancel(1, 1);
        assertThrows(IllegalArgumentException.class, () -> book.cancel(1, 1));
    }


    /**
     * A cancelled order no longer blocks its trader as a self-trade.
     */
    @Test
    public void cancelClearsSelfTradeBlock() {
        book.submit(1, Side.SELL, 10, 25);
        book.cancel(1, 1);
        assertEquals(2, book.submit(1, Side.BUY, 10, 25));
        assertTrue(book.getTrades().isEmpty());
    }


    /**
     * getBids(traderId) keeps only that trader's orders, in getBids() order.
     */
    @Test
    public void getBidsForOneTrader() {
        book.submit(1, Side.BUY, 10, 24);
        book.submit(2, Side.BUY, 10, 26);
        book.submit(1, Side.BUY, 10, 26);

        ArrayList<Order> mine = book.getBids(1);
        assertEquals(2, mine.size());
        assertEquals(3, mine.get(0).getId());
        assertEquals(1, mine.get(1).getId());
    }


    /**
     * getAsks(traderId) is empty for a trader with no waiting orders.
     */
    @Test
    public void getAsksForTraderWithNone() {
        book.submit(1, Side.SELL, 10, 25);
        assertTrue(book.getAsks(7).isEmpty());
        assertEquals(1, book.getAsks(1).size());
    }


    /**
     * Checks that one order is rejected.
     */
    private void assertRejected(int trader, Side side, int quantity,
        int price) {
        assertThrows(IllegalArgumentException.class, () -> book.submit(trader,
            side, quantity, price));
    }


    /**
     * Checks every field of one trade.
     */
    private void assertTrade(Trade trade, int buyer, int seller, int price,
        int quantity) {
        assertEquals(buyer, trade.getBuyer());
        assertEquals(seller, trade.getSeller());
        assertEquals(price, trade.getPrice());
        assertEquals(quantity, trade.getQuantity());
    }
}
