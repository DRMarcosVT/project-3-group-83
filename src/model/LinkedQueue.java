package model;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * A first-in, first-out queue built from singly linked nodes. Items join at
 * the tail and leave from the head, both in constant time.
 *
 * @param <T>
 *            the type of item held
 */
public class LinkedQueue<T> {

    private Node<T> head;
    private Node<T> tail;

    /**
     * Makes an empty queue.
     */
    public LinkedQueue() {
        head = null;
        tail = null;
    }


    /**
     * Adds an item at the tail.
     *
     * @param item
     *            the item to add
     * @throws IllegalArgumentException
     *             if item is null; the queue is unchanged
     */
    public void enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot enqueue null.");
        }
        Node<T> node = new Node<T>(item);
        if (tail == null) {
            head = node;
        }
        else {
            tail.next = node;
        }
        tail = node;
    }


    /**
     * Removes and returns the head item, the one that has waited longest.
     *
     * @return the removed item
     * @throws NoSuchElementException
     *             if the queue is empty
     */
    public T dequeue() {
        if (head == null) {
            throw new NoSuchElementException("The queue is empty.");
        }
        T item = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        return item;
    }


    /**
     * Returns the head item and leaves it in place.
     *
     * @return the item that has waited longest
     * @throws NoSuchElementException
     *             if the queue is empty
     */
    public T peek() {
        if (head == null) {
            throw new NoSuchElementException("The queue is empty.");
        }
        return head.data;
    }


    /**
     * Says whether the queue holds no items.
     *
     * @return true when the queue is empty
     */
    public boolean isEmpty() {
        return head == null;
    }


    /**
     * Removes the first item equal to the given one, wherever it is in the
     * queue. The other items keep their arrival order.
     *
     * @param item
     *            the item to remove
     * @return true if an item was removed, false if none was equal
     * @throws IllegalArgumentException
     *             if item is null; the queue is unchanged
     */
    public boolean remove(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot remove null.");
        }
        Node<T> previous = null;
        Node<T> current = head;
        while (current != null && !current.data.equals(item)) {
            previous = current;
            current = current.next;
        }
        if (current == null) {
            return false;
        }
        if (previous == null) {
            head = current.next;
        }
        else {
            previous.next = current.next;
        }
        if (current == tail) {
            tail = previous;
        }
        return true;
    }


    /**
     * Copies the items into a new list, head first. The queue is unchanged,
     * and changing the list does not change the queue.
     *
     * @return a new list of the items in arrival order
     */
    public ArrayList<T> toList() {
        ArrayList<T> items = new ArrayList<T>();
        Node<T> current = head;
        while (current != null) {
            items.add(current.data);
            current = current.next;
        }
        return items;
    }

    /**
     * Links one item to the item that arrived after it.
     *
     * @param <T>
     *            the type of item held
     */
    private static class Node<T> {

        private final T data;
        private Node<T> next;

        /**
         * Makes a node with nothing after it.
         *
         * @param data
         *            the item this node holds
         */
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
}
