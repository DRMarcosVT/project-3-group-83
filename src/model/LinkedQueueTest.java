package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests LinkedQueue with no view and no controller.
 */
public class LinkedQueueTest {

    private LinkedQueue<String> queue;

    /**
     * Makes an empty queue before each test.
     */
    @BeforeEach
    public void setUp() {
        queue = new LinkedQueue<String>();
    }


    /**
     * enqueue adds at the tail.
     */
    @Test
    public void enqueueAddsAtTail() {
        queue.enqueue("a");
        queue.enqueue("b");
        assertEquals(Arrays.asList("a", "b"), queue.toList());
        assertFalse(queue.isEmpty());
    }


    /**
     * enqueue(null) throws and leaves the queue as it was.
     */
    @Test
    public void enqueueNullThrows() {
        queue.enqueue("a");
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(
            null));
        assertEquals(Arrays.asList("a"), queue.toList());
    }


    /**
     * dequeue returns items in arrival order.
     */
    @Test
    public void dequeueReturnsOldestFirst() {
        queue.enqueue("a");
        queue.enqueue("b");
        assertEquals("a", queue.dequeue());
        assertEquals("b", queue.dequeue());
        assertTrue(queue.isEmpty());
    }


    /**
     * dequeue on an empty queue throws.
     */
    @Test
    public void dequeueEmptyThrows() {
        assertThrows(NoSuchElementException.class, () -> queue.dequeue());
    }


    /**
     * A queue that was emptied accepts new items; this fails if dequeue
     * leaves tail pointing at the removed node.
     */
    @Test
    public void enqueueAfterEmptying() {
        queue.enqueue("a");
        queue.dequeue();
        queue.enqueue("b");
        assertEquals(Arrays.asList("b"), queue.toList());
        assertEquals("b", queue.peek());
    }


    /**
     * peek returns the head and leaves it in place.
     */
    @Test
    public void peekLeavesHeadInPlace() {
        queue.enqueue("a");
        queue.enqueue("b");
        assertEquals("a", queue.peek());
        assertEquals("a", queue.peek());
        assertEquals(Arrays.asList("a", "b"), queue.toList());
    }


    /**
     * peek on an empty queue throws.
     */
    @Test
    public void peekEmptyThrows() {
        assertThrows(NoSuchElementException.class, () -> queue.peek());
    }


    /**
     * isEmpty is true on a new queue, false after an enqueue, and true again
     * after the matching dequeue.
     */
    @Test
    public void isEmptyFollowsContents() {
        assertTrue(queue.isEmpty());
        queue.enqueue("a");
        assertFalse(queue.isEmpty());
        queue.dequeue();
        assertTrue(queue.isEmpty());
    }


    /**
     * toList copies the items head first and does not change the queue.
     */
    @Test
    public void toListCopiesInOrder() {
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
        assertEquals(Arrays.asList("a", "b", "c"), queue.toList());
        assertEquals("a", queue.dequeue());
    }


    /**
     * toList on an empty queue gives an empty list.
     */
    @Test
    public void toListEmpty() {
        assertTrue(queue.toList().isEmpty());
    }


    /**
     * remove takes an item from the middle and keeps the others in order.
     */
    @Test
    public void removeFromMiddle() {
        fillABC();
        assertTrue(queue.remove("b"));
        assertEquals(Arrays.asList("a", "c"), queue.toList());
    }


    /**
     * Removing the head makes the next item the head.
     */
    @Test
    public void removeHead() {
        fillABC();
        assertTrue(queue.remove("a"));
        assertEquals("b", queue.peek());
    }


    /**
     * Removing the tail and then enqueueing keeps the new item; this fails if
     * remove leaves tail on the removed node.
     */
    @Test
    public void removeTailThenEnqueue() {
        fillABC();
        assertTrue(queue.remove("c"));
        queue.enqueue("d");
        assertEquals(Arrays.asList("a", "b", "d"), queue.toList());
    }


    /**
     * Removing the only item empties the queue, which then accepts new items.
     */
    @Test
    public void removeOnlyItem() {
        queue.enqueue("a");
        assertTrue(queue.remove("a"));
        assertTrue(queue.isEmpty());
        queue.enqueue("b");
        assertEquals(Arrays.asList("b"), queue.toList());
    }


    /**
     * Removing an item that is not there returns false and changes nothing.
     */
    @Test
    public void removeMissingItem() {
        fillABC();
        assertFalse(queue.remove("z"));
        assertEquals(Arrays.asList("a", "b", "c"), queue.toList());
        assertFalse(new LinkedQueue<String>().remove("a"));
    }


    /**
     * remove(null) throws and changes nothing.
     */
    @Test
    public void removeNullThrows() {
        fillABC();
        assertThrows(IllegalArgumentException.class, () -> queue.remove(null));
        assertEquals(Arrays.asList("a", "b", "c"), queue.toList());
    }


    /**
     * Fills the queue with a, b, c.
     */
    private void fillABC() {
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
    }


    /**
     * Changing the returned list does not change the queue.
     */
    @Test
    public void toListIsACopy() {
        queue.enqueue("a");
        ArrayList<String> copy = queue.toList();
        copy.clear();
        assertEquals(Arrays.asList("a"), queue.toList());
    }
}
