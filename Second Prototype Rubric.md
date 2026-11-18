# Second Prototype Rubric

**CS 2114 · Software Design & Data Structures · Fall 2026**
**Project 3: Designing for Change · Deliverable 4 — Second Prototype + Tests**
Due at the start of your lab session

## Deliverable 4 - Second Prototype + Tests

The view meets the model, your design absorbs a change, and tests lock in the model's behavior. This is the second prototype checkpoint — the new escalation in P3.

### View connected to the model

User actions flow through the controller into the model, and the view reflects the model's current state.

### One substantial feature added

Implement the change feature you named in your Deliverable 3 Next Steps. It must add or change model state or logic and be reachable by the user through the controller and view. If you switched features, say so and why in your reflection.

### JUnit tests that run

At least one normal and one bad-input case per core model/Queue method, including any model methods your new feature added, and they pass. Tests exercise the model without going through the UI.

### Bad input survives

The prototype does not crash on the cases you named in your scope, or on any new bad input your feature introduces.

### Reflection

In your `README.md`, under a "Reflection" heading, address:

- **How did your design absorb the change?** List the classes your feature actually touched. Compare that with the prediction in your D3 Next Steps: what matched, what surprised you?
- **Did the change cross the model/view line?** Did the view have to learn anything new about the model? If a change rippled through layers it shouldn't have, where and why?
- **What changed from your D3 plan?** Features, signatures, validation, or tests that differ from what you said you'd do, and why.
- **What doesn't work yet?** MVP features, bad-input cases, or tests that are missing or failing.

### Next Steps

In your `README.md`, under a "Next Steps" heading, address:

- **What is left for the final build?** Remaining features and fixes, in order.
- **Which stretch goals are you committing to, and which are you dropping?**
- **What will your 3-minute demo show?** The path you'll walk through, including the new feature.
- **Who does what?** Which team member owns each remaining item.

### Attribution

In your `README.md`, a table with one row per class and test file listing who worked on it and how (wrote, revised, reviewed, tested, GenAI-assisted, …), plus a GenAI note on how (or that) you used GenAI. Group members not contributing substantively to at least one class or test file will receive a 50% deduction on their group's deliverable.

### Meets

The view is driven through the controller and reflects the model; one substantial feature is implemented through the model and reachable from the view; the JUnit tests run with normal and bad-input coverage on the model, including the new feature; bad input is handled without crashing; the README reflects on how the design absorbed the change and lays out next steps; and every class and test file is attributed with a GenAI note.

## Submit

Your source and tests together, with a `README.md` containing:

- how to compile and run the program and the tests,
- your "Reflection" section,
- your "Next Steps" section, and
- your attribution table and GenAI note.

The tests must actually execute and must exercise the model independently of the view. A test file that doesn't run counts as not present.

> 🤖 **Working with GenAI Tip!** GenAI can draft tests for the Queue quickly — dequeue on empty, peek on empty, order after interleaved enqueues. Ask for candidates, then read each one and confirm it tests the model's contract, not an accident of your current view. Keep only the ones you understand.

Due at the start of your lab session. Graded EMRN — aim for at least Meets. Bring your work to the grading session ready to defend it; treat it as an oral exam.

Full project context, learning objectives, and the EMRN table are on the Project 3: Designing for Change - Overview page.

## Grading Criteria (EMRN)

Each criterion is worth 11 pts: Exceeds 11, Meets 8.5, Revisable 7.5, Not There Yet 5, Missing 0. Revisable means one or more Meets items are missing; Not There Yet means significant components are missing.

### View Connected to the Model

- **Exceeds (11):** All of Meets plus:
  - [ ] View keeps no copy of model state
- **Meets (8.5):**
  - [ ] User actions flow through the controller into the model
  - [ ] The view reflects the model's current state
  - [ ] README says how to compile and run the program

### Change Feature

- **Exceeds (11):** All of Meets plus:
  - [ ] No class the D3 prediction said should stay unchanged had to change
- **Meets (8.5):**
  - [ ] Implements the feature named in D3 Next Steps (or the switch is explained in the reflection)
  - [ ] Adds or changes model state or logic
  - [ ] User reaches it through the controller and view

### JUnit Tests

- **Exceeds (11):** All of Meets plus:
  - [ ] Tests check order after interleaved enqueues and dequeues
- **Meets (8.5):**
  - [ ] At least one normal and one bad-input case per core model/Queue method
  - [ ] Covers model methods the new feature added
  - [ ] Tests run and pass
  - [ ] Tests exercise the model without going through the UI
  - [ ] README says how to run the tests

### Bad Input Survives

- **Exceeds (11):** All of Meets plus:
  - [ ] Each bad-input case has a test
- **Meets (8.5):**
  - [ ] No crash on the cases named in the scope
  - [ ] No crash on new bad input the feature introduces

### Reflection

- **Exceeds (11):** All of Meets plus:
  - [ ] Says what design change would make the next feature cheaper
- **Meets (8.5):**
  - [ ] Under a "Reflection" heading in README.md
  - [ ] Classes the feature actually touched, compared with the D3 prediction (what matched, what surprised you)
  - [ ] Whether the change crossed the model/view line, and where and why if it rippled
  - [ ] What changed from the D3 plan, and why
  - [ ] What doesn't work yet

### Next Steps

- **Exceeds (11):** All of Meets plus:
  - [ ] Demo path is timed to fit 3 minutes
- **Meets (8.5):**
  - [ ] Under a "Next Steps" heading in README.md
  - [ ] What's left for the final build, in order
  - [ ] Stretch goals committed vs. dropped
  - [ ] What the 3-minute demo will show, including the new feature
  - [ ] Owner for each remaining item

### Attribution + GenAI Note

Graded Complete / Incomplete (0 pts).

- **Complete:**
  - [ ] Table in README.md with one row per class and test file
  - [ ] Each row lists who worked on it and how (wrote, revised, reviewed, tested, GenAI-assisted, …)
  - [ ] GenAI note on how GenAI was used, or that it wasn't
- **Incomplete:** One or more of the Complete items is missing
