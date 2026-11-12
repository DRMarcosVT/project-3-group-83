# Scope Document

## Project Name

No Cutting!

## What are you building?

An order book simulator for one stock meant for students learning how exchanges decide how orders get filled. Traders submit orders to buy or sell shares at specific prices, and if no one is on the other side yet, the order waits in line. When a sell or buy order arrives, it is given to the highest bidder and lowest ask respectively. Big orders can fill partially, with the rest left waiting. The screen shows both waiting lines and a running trade log.

## MVP

A console app modelling an order book.

1. The user inputs an order (trader, BUY/SELL, quantity, price).
2. The app validates input and re-prompts if invalid.
3. The order fills against the best priced orders on the other side, oldest first within a price, until it's filled or no waiting order meets its price; the unfilled remainder of the order waits.
4. App prints both sides of the book and the trade log.
5. The user enters another order or quits.

## Stretch Goals

1. Order cancellation so a user can cancel a wrong order.
2. A Swing or JavaFX GUI so the user can visualise the order book.
3. Stats like volume traded and average wait before a fill so the user can fit their orders to the market.
4. A market making model that tries to profit off the bid ask spread so the user can practice market-making in a simulated environment.

## The Queue's role

Resting orders are kept on two sides: bids and asks. Each side is a TreeMap, keys: price, values: FIFO queue of orders at that price, keys are sorted so the first key is the best price. A single FIFO line for all bids would fill an older, lower bid before a newer better bid; sorting by price first avoids this, so FIFO breaks ties within a price. FIFO is right for those ties because among traders offering the same price, the first to commit has the stronger claim; any other rule would mean cutting in line. We'll write a generic singly linked `LinkedQueue<T>` with head and tail pointers: O(1) to enqueue and dequeue, no fixed capacity or resizing, no deque since nothing joins the front or leaves the back.

## Model / View split

The model (`Order`, `Trade`, `LinkedQueue`, `OrderBook`) holds all order book state and matching logic and is testable with no UI. The view displays snapshots of the book and trade log and collects input. The controller passes the order to the model, which updates the book, then the controller requests a snapshot from the model and passes it to the view.

## Behavior / recovery from bad input

A rejected input never changes the book. The app explains the problem and re-prompts.

Adversarial cases: prices or quantity of 0, negative, text ("ten"), cancelling a filled order ID (stretch).

Careless cases: order or price above a set max (overflow), a side other than buy/sell, a blank trader name; self-trades (same trader on both sides, blocked), canceling a nonexistent order ID (stretch).

## What we don't know

- How do we cancel an order in the middle of a singly linked queue without changing others?
- Should we use a Swing or Java FX UI?
- How do we plan a git workflow for all three team members?

## Work Attribution

| Section | Team member(s) | How they contributed |
|---|---|---|
| Project Name | Riya Shah, Wayne Clue, Marcos Salas, Raghul | Brainstormed together |
| What are you building? | Riya Shah, Wayne Clue, Marcos Salas, Raghul | Brainstormed together |
| MVP | Riya, Marcos | Both drafted and edited |
| Stretch Goals | Riya, Marcos | Both drafted and edited |
| The Queue's role | Riya, Marcos | Both drafted and edited |
| Model / View split | Riya, Marcos | Riya drafted and Marcos edited |
| Bad input | Riya, Marcos | Riya drafted and Marcos edited |
| What you don't know | Riya, Marcos | Riya drafted and Marcos edited |

## GenAI note

We used Claude to brainstorm FIFO project ideas, then chose the order book as a team. We also used it for high level design ideas such as splitting the order book into two TreeMaps, which we kept. We also used it for a "fourth opinion" to check for consistency and factual correctness in our writing, which we kept. It also suggested a single Priority queue for each side of the book but we rejected it because it would lack the price and share information we need.
