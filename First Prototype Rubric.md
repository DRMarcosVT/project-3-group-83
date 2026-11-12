# First Prototype Rubric

**CS 2114 · Software Design & Data Structures · Fall 2026**
**Project 3: Designing for Change · Deliverable 3 — First Prototype**
Due at your lab session

## Deliverable 3 - First Prototype

A first working slice, where the model stands on its own. The point of this checkpoint is to prove your core logic works before any interface complicates it. Each item below is one rubric point.

### Model works

Your Queue-backed logic runs and is exercisable from a tiny driver or a test, with no view required.

### Thin or stubbed view

Just enough to see something happen. The model must not depend on it.

### Runs without crashing

Runs without crashing on the MVP path.

### Reflection

In your `README.md`, under a "Reflection" heading, describe what changed between your specification and what you actually built. Address:

- **What changed from your specification?** Classes, method signatures, where validation lives, test cases — what did the spec say, what did you build instead, and why?
- **Did the model really stand alone?** Was there anything you had to move across the model/view line to make it run, or anything in the model that still knows about the view?
- **What happened to your unknowns?** Of the things you listed in your scope as not knowing how to do, which did you figure out, and which are still open?
- **What doesn't work yet?** MVP features, bad-input cases, or tests that are missing or failing.

### Next Steps

In your `README.md`, under a "Next Steps" heading, lay out your plan for Deliverable 4. Address:

- **What is left to finish the MVP?** The remaining features and fixes, in the order you will do them.
- **Your change feature.** Deliverable 4 requires you to add one non-trivial feature that was not in your MVP. Name it, then predict which classes it will touch (model, view, controller) and which ones your design says should not need to change. You will compare this prediction to what actually happened.
- **Which stretch goals will you attempt?** The ones from your scope you are committing to, and the ones you are dropping.
- **What new tests will you need?** The methods and bad-input cases the added work must cover.
- **Who does what?** Which team member owns each remaining item.

### Attribution (REQUIRED)

In your `README.md`, a table with one row per class and test file listing who worked on it and how (wrote, revised, reviewed, tested, GenAI-assisted, …), plus a GenAI note on how (or that) you used GenAI. Group members not contributing substantively to at least one class or test file will receive a 50% deduction on their group's deliverable.

### What counts as a non-trivial feature?

It adds or changes model state or logic, and a user can reach it through the controller and view. Examples: a priority lane or a "skip/requeue" action, undo of the last serve, estimated wait time per entry, saving and restoring the line, or a second queue with a routing rule. Not acceptable: a reskin, a new label or color, or a button wired to logic you already have. It may be one of your stretch goals. Your TA will judge whether it is truly non-trivial.

**Meets** — the Queue-backed model runs and can be exercised independently of the view, a minimal view exists without the model depending on it, the MVP path runs without crashing, the README has a reflection on what changed and a next-steps plan that names one non-trivial feature with a prediction of what it will touch, and every class and test file is attributed with a GenAI note.

## Submit

Your source so far, with a `README.md` containing:

- how to run the model (driver or tests) and the thin view,
- your "Reflection" section,
- your "Next Steps" section, including your change feature, and
- your attribution table and GenAI note.

It is fine for the view to be ugly; it is not fine for the model to need the view to run.

> 🤖 **Working with GenAI Tip!** Before committing to your change feature, describe your current classes to GenAI and ask which ones it thinks the feature would force you to edit. If its list crosses your model/view line, either your boundary or your feature choice needs another look. Note what you asked in your GenAI note.

Due at the start of your lab session. Graded EMRN — aim for at least Meets. Bring your work to the grading session ready to defend it; treat it as an oral exam.

Full project context, learning objectives, and the EMRN table are on the *Project 3: Designing for Change - Overview* page.

---

## Rubric: Project 3 - Deliverable 3 - First Prototype

Each criterion below uses the same scale unless noted:

| Rating | Points |
|---|---|
| Exceeds | 11 |
| Meets | 8.5 |
| Revisable — missing one or more of the Meets criteria | 7.5 |
| Not There Yet — missing significant components | 5 |
| Missing | 0 |

### 1. Model Works (/11 pts)

**Meets (8.5 pts)**
- ☐ Queue-backed logic runs
- ☐ Can be exercised from a tiny driver or a test
- ☐ No view required
- ☐ README says how to run the model (driver or tests)

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Core Queue methods already have passing JUnit tests

### 2. Thin or Stubbed View (/11 pts)

**Meets (8.5 pts)**
- ☐ Just enough to see something happen
- ☐ Model does not depend on the view
- ☐ README says how to run the thin view

**Exceeds (11 pts)** — All of Meets plus:
- ☐ View already goes through a controller

### 3. Runs Without Crashing (/11 pts)

**Meets (8.5 pts)**
- ☐ MVP path runs end to end
- ☐ No crash on the MVP path

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Also survives the bad-input cases named in the scope

### 4. Reflection (/11 pts)

**Meets (8.5 pts)**
- ☐ Under a "Reflection" heading in `README.md`
- ☐ What changed from the spec (classes, signatures, validation, tests): what it said, what you built, and why
- ☐ Whether the model really stood alone
- ☐ What happened to the unknowns from the scope (figured out vs. still open)
- ☐ What doesn't work yet

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Each change is traced to what prompted it

### 5. Next Steps (/11 pts)

**Meets (8.5 pts)**
- ☐ Under a "Next Steps" heading in `README.md`
- ☐ What's left to finish the MVP, in order
- ☐ Names one non-trivial change feature not in the MVP
- ☐ Predicts which classes it will touch and which should not change
- ☐ Stretch goals committed vs. dropped
- ☐ New tests needed
- ☐ Owner for each remaining item

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Prediction names specific methods, not just classes

### 6. Attribution + GenAI Note (/0 pts)

**Complete (0 pts)**
- ☐ Table in `README.md` with one row per class and test file
- ☐ Each row lists who worked on it and how (wrote, revised, reviewed, tested, GenAI-assisted, …)
- ☐ GenAI note on how GenAI was used, or that it wasn't

**Incomplete (0 pts)** — One or more of the Complete items is missing.
