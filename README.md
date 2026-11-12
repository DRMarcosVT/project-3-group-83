# No Cutting!

An order book simulator for one stock. Traders send orders to buy or sell shares at a price. An order fills against the best price on the other side, oldest order first within a price, and any unfilled remainder waits in line.

This is the Deliverable 3 prototype: the model, its JUnit tests, a driver that runs the model alone, and a thin console view that goes through a controller. Order cancellation, our Deliverable 4 change feature, was also built on November 11, after its prediction was written; "Built early: prediction against the result" under Next Steps compares the two.

> Draft of November 11, 2026. It describes what is in this folder and was run on that date.

## How to run

You need JDK 17. Run every command from this folder.

### The model, with no view

**Driver.** This compiles the five model classes and the driver only; `view` and `controller` are not on the command line.

```
javac -d bin src/model/LinkedQueue.java src/model/Side.java src/model/Order.java src/model/Trade.java src/model/OrderBook.java src/driver/ModelDriver.java
java -cp bin driver.ModelDriver
```

The driver sends five good orders and four bad ones to an `OrderBook` and prints each answer, then both sides of the book and the trade log. The last lines should show three trades: 50 at $25, 50 at $25, 20 at $26.

**JUnit tests.** In Eclipse: File > Open Projects from File System, choose this folder, then right-click `src/model` > Run As > JUnit Test. The project uses JUnit 5. Expected: 44 tests, 0 failures (17 in `LinkedQueueTest`, 27 in `OrderBookTest`). Both test classes create only model objects.

### The thin view

```
javac -d bin -sourcepath src src/controller/OrderController.java
java -cp bin controller.OrderController
```

Type a trader ID, then commands:

| Command | What it does |
|---|---|
| `buy` or `sell` | Asks for a quantity and a price in whole dollars, sends the order as the current trader, prints its order ID, the book and the trade log. |
| `cancel <order ID>` | Cancels one of the current trader's waiting orders, prints how many shares were left unfilled, then the book and the trade log. |
| `mine` | Prints only the current trader's waiting orders. |
| `switch <id>` | Changes the current trader, so one person can type both sides of a trade. |
| `quit` | Ends the program. |

Three scripted sessions are in `demo/`. `mvp-session.txt` walks the MVP path; `bad-input-session.txt` types every bad-input case from our scope and ends without `quit`; `cancel-session.txt` cancels a partly filled order and tries every bad cancel.

```
java -cp bin controller.OrderController < demo/mvp-session.txt
java -cp bin controller.OrderController < demo/bad-input-session.txt
java -cp bin controller.OrderController < demo/cancel-session.txt
```

In PowerShell, `<` is not available; use `Get-Content demo\mvp-session.txt | java -cp bin controller.OrderController`. Typed input is not echoed when it comes from a file, so several prompts appear on one line.

### Where things are

| Path | Role | Contents |
|---|---|---|
| `src/model` | Model | `LinkedQueue`, `Side`, `Order`, `Trade`, `OrderBook`, and the two test classes |
| `src/view` | View | `ConsoleView` |
| `src/controller` | Controller | `OrderController`, which holds `main` |
| `src/driver` | Driver | `ModelDriver` |
| `demo` | | The three scripted sessions |
| `Preliminary Spec.md` | | The spec, revised on November 11 to match this build and to plan Deliverable 4 |
| `Cancellation Proposal.md` | | The options we weighed for the change feature |

## Reflection

"The spec" below means the Deliverable 2 text, which is in git at commit `e55516d`.

### What changed from the specification

| The spec said | We built | Why | What prompted it |
|---|---|---|---|
| A trader is a name: `submit(String trader, ...)`, `String getTrader()`, `String getBuyer()`, `String getSeller()`. | A trader is a positive `int` ID in all four places. | Cancellation has to check that the trader asking owns the order. Two `int`s compare with `==`; two names can differ by case or spacing and still mean the same person. | Planning the Deliverable 4 change feature on November 11. Marcos proposed the switch before any code existed, so it cost spec edits and no rework. |
| Orders had no ID. `submit` returned `void`. | `OrderBook` numbers each accepted order from one counter, `nextOrderId`. `Order` has `getId()`. `submit` returns the ID. | A user needs a way to name the order to cancel. | The same planning session. The spec's stretch row already mentioned `cancel(int orderId)` but gave orders no ID to match it. |
| `submit` rejected a blank trader name. | `submit` rejects a trader ID below 1. `run` also rejects it when it is typed. | A number cannot be blank. Text typed as an ID is caught in the controller by `Integer.parseInt`, where the spec already caught text typed as a quantity or price. | Follows from the first row. |
| `run` asked for trader, side, quantity and price for every order, and ended on `quit` at the trader prompt. | `run` asks for a trader ID once and keeps it in a new field, `currentTrader`. It then reads commands: `buy`, `sell`, `switch <id>`, `quit`. | Self-trades are blocked, so one person typing both sides needs to change trader. A command prompt also gives Deliverable 4 a place to add `cancel` and `mine`. | GenAI (Claude) pointed out that with one fixed ID a person testing by hand could never get a fill. Marcos accepted `switch`. |
| `ask` returns the line the user types. | `ask` returns `null` when the input has ended, and `run` treats `null` as `quit`. | Without it the program crashed. | Running the first build with input piped from a file that did not end in `quit` threw `NoSuchElementException` from `Scanner.nextLine`, at `ConsoleView.ask`. |
| Seven classes in three packages. | An eighth class, `driver.ModelDriver`, in a fourth package. | The model has to be runnable from a driver with no view. Keeping the driver outside `model` keeps every print statement out of the model package. | The Deliverable 3 rubric. |
| The test plan named traders alice, bob, carol and dave. | The same cases with trader IDs 1, 2, 3 and 4, each also checking the order ID. | Follows from the first two rows. | Follows from the first two rows. |
| The test plan had one normal and one bad-input case per method. | Seven more cases: enqueue after the queue was emptied, changing the list `toList` returned, a small order partly filling a waiting one, the same trader waiting on both sides at prices that cannot trade, a waiting order priced past the new order's limit, the maximum quantity and price, and order numbering with a rejected order in between. | The planned cases did not reach these branches: `dequeue` resetting `tail`, the self-trade scan stopping at the first price that does not match, the fill loop stopping at the limit price, and the upper edge of each range check. | Writing those branches and seeing that no planned test would fail if they were wrong. |

Unchanged from the spec: `LinkedQueue`'s five methods and its private `Node`, the two TreeMaps of queues, the matching rules, trading at the waiting order's price, the 10,000 maximums, `getBids`, `getAsks` and `getTrades` returning copies, `ConsoleView`'s four method names, and which class catches each bad input.

### Did the model stand alone?

Yes. Three checks anyone can repeat:

- The five model source files import only `java.util` classes. `grep "^import" src/model/LinkedQueue.java src/model/Side.java src/model/Order.java src/model/Trade.java src/model/OrderBook.java` shows it.
- The driver command under "How to run" compiles and runs the model with `view` and `controller` absent from the command line.
- `LinkedQueueTest` and `OrderBookTest` pass and create only model objects.

Nothing had to move across the model/view line to make it run. The one piece of new state that could have gone on either side, which trader is typing, went in the controller. `OrderBook` takes the trader ID as a parameter and never stores it, so the tests can act as several traders with no setup.

The view knows three model classes, as the spec planned: it imports `Order` and `Trade` and reads their getters, and it prints the `Side` that `Order.getSide()` returns. It holds no `OrderBook`.

One thing in the model is written for the screen. The messages inside `submit`'s `IllegalArgumentException`s, such as "Quantity must be from 1 to 10000, got 0.", reach the user word for word: `run` passes `e.getMessage()` to `showError`. The model does not call or import the view to do this, but the wording a user reads is decided in `OrderBook`. A GUI that wanted different wording would have to map the exceptions itself.

### What happened to our unknowns

| Unknown from the scope | Status |
|---|---|
| How do we cancel an order in the middle of a singly linked queue without changing the others? | Figured out and built. `LinkedQueue.remove(T item)` walks from `head` keeping a reference to the previous node, links the previous node to the one after the match, and moves `head` or `tail` when the match is first or last. Its six tests pass, including removing the tail and then enqueueing, which fails if `tail` is left on the removed node. |
| Should we use a Swing or JavaFX UI? | Still open, and no longer blocking: the prototype uses the console. We are keeping a GUI as a proposed next step after cancellation, and will choose the toolkit then. See Stretch goals. |
| How do we plan a git workflow for all three team members? | Planned in the spec: one branch per piece (`model`, `view`, `controller`), merged into `main` by its owner. Still unproven: on November 11 the repository had only `main` and `preliminary-spec`, and this prototype had not yet been through those branches. |

### What doesn't work yet

- No JUnit test runs `OrderController` or `ConsoleView`. They have been checked only by the three scripted sessions in `demo/`, read by eye.
- A quantity that is a number but out of range (0, -5, 10,001) is reported only after the user has also typed a price, because the range check is in `submit`. Text such as "ten" is reported at once.
- The whole trade log is printed after every order and every cancel, so a long session scrolls.
- Every bad-input case in the scope is handled, including the two about cancelling.
- No test is failing: 44 of 44 pass.

## Next Steps

### What is left to finish the MVP

All five MVP steps in the scope run today. What remains is review and three fixes, in this order:

1. Riya reviews `ConsoleView` and Raghul reviews `OrderController`, and both agree to the signature changes in "Revised after Deliverable 2" in the spec. Our own integration rule requires all three of us to agree to a signature change.
2. Each owner commits their piece on their branch and merges it into `main`, as the spec's integration plan says.
3. Add `OrderControllerTest`, which feeds typed lines through `System.setIn` and checks the book afterwards, so the controller is no longer checked only by eye.
4. Report an out-of-range quantity before asking for the price: `run` compares the quantity with `OrderBook.MAX_QUANTITY` before the price prompt. `submit` keeps its own check.
5. Decide whether `showTrades` should print only the newest trades.

### Our change feature: order cancellation

A trader can cancel one of their own waiting orders by its ID, and can list their own waiting orders. It was stretch goal 1 in our scope and is not in the MVP.

It qualifies as a change feature on both counts in the rubric. It changes model logic and state: `OrderBook` gains a way to take an order out of the book, with three checks, and `LinkedQueue` gains its first operation that removes from the middle. A user reaches it through the controller and view with two new commands, `cancel <orderId>` and `mine`.

**Prediction: what it will touch**

| Class | Role | Methods we expect to add or change |
|---|---|---|
| `LinkedQueue` | Model | Add `boolean remove(T item)`. |
| `OrderBook` | Model | Add `Order cancel(int traderId, int orderId)`, `ArrayList<Order> getBids(int traderId)` and `ArrayList<Order> getAsks(int traderId)`. |
| `OrderController` | Controller | Change `run` and its private helper `handle` to read `cancel <orderId>` and `mine`, and to call `showMessage` after `buy`, `sell` and `cancel`. |
| `ConsoleView` | View | Add `void showMessage(String message)`. |

**Prediction: what should not need to change**

| Class or method | Why our design says it stays |
|---|---|
| `OrderBook.submit`, `getBids()`, `getAsks()`, `getTrades()` | A cancelled order leaves its queue at once, so every order these methods meet is still waiting. |
| `LinkedQueue.enqueue`, `dequeue`, `peek`, `isEmpty`, `toList` | `remove` is a new method beside them. |
| `ConsoleView.ask`, `showBook`, `showTrades`, `showError` | `mine` hands filtered lists to the existing `showBook`. |
| `Order`, `Trade`, `Side` | `Order` already has `getId()` and `getTrader()`. A cancel makes no trade. |
| `ModelDriver`, and every existing test | No existing signature changes. |

The feature touches classes in all three roles, and each edit stays inside its own role: the model decides whether a cancel is allowed and removes the order, the controller reads the command, and the view prints one line of text it is handed. The view does not learn how the book stores orders, and the model does not print. If building it forces a change to any row of the second table, we will say which one and why in Deliverable 4.

The full plan, with the steps of `cancel`, its validation and its tests, is in the spec under "Deliverable 4 change feature: order cancellation".

### Built early: prediction against the result

We built cancellation on November 11, after writing the prediction above and without changing it. Outside the two test classes, which gained new tests and kept their old ones unchanged, every edit is in the four classes listed below.

| Predicted | What happened |
|---|---|
| `LinkedQueue` adds `remove(T item)` | As predicted. |
| `OrderBook` adds `cancel`, `getBids(int)`, `getAsks(int)` | As predicted, plus two private helpers: `findWaiting`, which scans both sides for an order ID, and `onlyTrader`, which filters a list by trader. Neither is visible outside the class. |
| `OrderController` changes `run` and `handle` | As predicted. One more change: `OrderController` now imports `model.Order`, because it reads the shares cancelled from the `Order` that `cancel` returns. Before this the controller used only `OrderBook` and `Side`. Both are model classes, so the controller still talks only to the model and the view. |
| `ConsoleView` adds `showMessage` | As predicted. |
| No change to `submit`, `getBids()`, `getAsks()`, `getTrades()`, the five other queue methods, `ask`, `showBook`, `showTrades`, `showError`, `Order`, `Trade`, `Side`, `ModelDriver` or any existing test | Held for every row. The 27 tests written before cancellation pass unchanged. |

The tests also go beyond the plan in two places. `cancelClearsSelfTradeBlock` checks that a cancelled order no longer blocks its trader as a self-trade; this is the bug lazy removal would have caused. `removeMissingItem` also calls `remove` on an empty queue.

### Stretch goals

| Stretch goal from the scope | Decision | Reason |
|---|---|---|
| 1. Order cancellation | Committing. It is the change feature. | The spec already places its validation, and it answers our first unknown. |
| 2. Swing or JavaFX GUI | Proposed as a next step, after cancellation is merged. Not committed. | The model would not change: a GUI replaces `ConsoleView`. Two things would. `OrderController` holds a `ConsoleView` field, so it needs a view interface or a second controller, and its `run` loop blocks on `ask`, which a GUI's button events cannot drive. We have not chosen between Swing and JavaFX. |
| 3. Stats: volume traded, average wait before a fill | Attempting part of it, after cancellation is merged: `getVolume()` and `getSpread()` on `OrderBook`. Dropping average wait. | Volume and spread are sums and a subtraction over what the book already holds. Average wait needs a timestamp on every `Order`. |
| 4. Market-making model | Dropping. | It needs other traders to trade against, which the program does not have. |

One idea is not in the scope: randomly generated orders, so a single user has a market to trade against, with a setting for a busy or a thin market. We will attempt it only if cancellation and the two stats are merged with time to spare.

### New tests we will need

Written and passing on November 11:

- For `LinkedQueue.remove`, in `LinkedQueueTest`: removing from the middle, the head and the tail; enqueueing after the tail was removed; removing the only item; an item that is not in the queue; and `null`.
- For `OrderBook.cancel`, in `OrderBookTest`: cancelling a waiting order; cancelling the only order at a price and then sending an order that would have matched it; cancelling a partly filled order; cancelling the older of two orders at one price and checking the newer one fills first. Bad input: another trader's order, an ID of 0, an ID that was never issued, an order that already filled, and the same order twice.
- For `getBids(int)` and `getAsks(int)`: a book with two traders' orders, and a trader with none.

Still needed, in a new `OrderControllerTest`: `cancel` with text for an ID, with no ID, and with two; `mine` with no orders waiting. `demo/cancel-session.txt` types all four today, but only a person reading the output checks them.

The expected result of each case is in the spec.

### Who does what

| Item | Owner |
|---|---|
| Review `ConsoleView`; review and agree to the signature changes | Riya Shah |
| Review `OrderController`; review and agree to the signature changes | Raghul Jayaprakash |
| Merge the three pieces into `main` | Raghul Jayaprakash |
| `OrderControllerTest`; quantity check before the price prompt; review `cancel` and `mine` in `run` | Raghul Jayaprakash |
| Review `showMessage`; the trade log decision | Riya Shah |
| Review `LinkedQueue.remove`, `OrderBook.cancel`, `getBids(int)`, `getAsks(int)`, and their tests | Marcos Salas |
| `getVolume()` and `getSpread()`, if attempted | Marcos Salas |
| Printing the stats, if attempted | Riya Shah |
| Comparing the build with the prediction above | Marcos Salas, Riya Shah, Raghul Jayaprakash |

## Attribution

| Class or test file | Who | How |
|---|---|---|
| `model/LinkedQueue.java` | Marcos Salas | GenAI-assisted: Claude wrote the first draft from the spec at Marcos's direction. |
| `model/Side.java` | Marcos Salas | Same. |
| `model/Order.java` | Marcos Salas | Same. Marcos decided on the `int` trader ID and the order ID. |
| `model/Trade.java` | Marcos Salas | Same. |
| `model/OrderBook.java` | Marcos Salas | Same. Marcos decided on the self-trade rule in the spec and the ID counter. |
| `model/LinkedQueueTest.java` | Marcos Salas | GenAI-assisted: Claude wrote the tests from the spec's test plan and ran them. |
| `model/OrderBookTest.java` | Marcos Salas | Same. |
| `driver/ModelDriver.java` | Marcos Salas | GenAI-assisted: Claude wrote and ran it. |
| `view/ConsoleView.java` | Riya Shah (owner) | First draft by Claude, prompted by Marcos. |
| `controller/OrderController.java` | Raghul Jayaprakash (owner) | First draft by Claude, prompted by Marcos. |

### GenAI note

We used Claude, through Claude Code, in one session on November 11, 2026, run by Marcos.

**Choosing the change feature.** Following the rubric's tip, Marcos gave Claude our scope and spec and asked which stretch goals could be the change feature and which classes each would force us to edit. For cancellation Claude listed `LinkedQueue`, `OrderBook`, `OrderController` and `ConsoleView`. That list has classes on both sides of our model/view line, so we looked again, as the tip says to. Marcos argued that a feature can have a model part and a view part without either doing the other's job; Claude agreed after first arguing the opposite. We kept the feature and the boundary: every predicted edit stays inside its own role. Claude at first said `ConsoleView` would not change, then corrected itself once orders had IDs, because the view has to print them and needs a way to print a confirmation.

**Designing it.** Marcos proposed integer trader IDs, order IDs, a trader ID set before the first command, and a rule that you can cancel only your own order. We kept all four. He also proposed a cancel as its own kind of order held in a queue, and one ID counter per side. On Claude's recommendation we changed both: a cancel is a method call, and there is one counter. Claude suggested the `switch` command, returning the order ID from `submit`, and removing a cancelled order from its queue at once; Marcos accepted them. The reasons are in `Cancellation Proposal.md`.

**Writing the prototype.** Claude wrote the first draft of every class, both test classes and the driver from the revised spec, compiled them, ran the 27 tests, and ran the two scripted sessions. Later the same day, at Marcos's request, Claude built cancellation from the spec's plan, added 17 tests and `demo/cancel-session.txt`, and compared the result with the prediction. Running a session that ended without `quit` exposed the crash described in the Reflection, which Claude fixed. Claude also wrote the first draft of this README and applied the agreed changes to the spec.
