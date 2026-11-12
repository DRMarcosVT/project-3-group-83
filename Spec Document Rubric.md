# Spec Document Rubric

**CS 2114 · Software Design & Data Structures · Fall 2026**
**Project 3: Designing for Change · Deliverable 2 — Specification**
Week 11 (Nov 2–6) · due at your lab session

## Deliverable 2 - Specification

The heart of the project. A specification turns your scope into a blueprint detailed enough that a teammate, or a coding assistant, could build from it without guessing — especially about where one layer ends and the next begins. It should answer each of the following:

### Class design, split by role

Which classes are model (state and logic, including your Queue), which are view (presentation), and which are controller (mediating the two)? No class spanning two roles.

### System diagram (required)

A single picture showing the main pieces of your system and how they connect: which parts talk to which, and what moves between them. It does not have to be UML or any formal notation — boxes and arrows are fine, and a photo of a legible whiteboard or hand sketch counts. The test is whether a reader can see the shape of your system at a glance without reading the rest of the spec. Group the boxes by MVC role so the model/view boundary is a visible line on the page.

### The model / view boundary

What does the view know about the model, and what does it deliberately not know? How does the view learn that the model changed? For an example MVC see these slides and this example code.

### Data & state

Where the Queue lives, what it holds, and the fields each class owns and their types.

### Method signatures

The public API of the model (including the Queue: `enqueue`, `dequeue`, `peek`, `isEmpty`) and of the controller: name, parameters and types, return type, one-line behavior.

### Where validation lives

Take each bad-input case from your scope: which class/method catches it, and what happens (re-prompt, exception, default)?

### Test plan

For each public model method, one normal and one bad-input case. The plan should make clear the model is testable without the view.

### Division of work

Who owns the model, the view, and the controller, and how you integrate them into one working program?

### Revised scope

What changed since your scope doc and why, and where each change originated (group discussion, GenAI, your own reflection)?

### Work Attribution (REQUIRED)

Every section of this document must be attributed. Include a table with one row per section listing who worked on it and how (drafted, revised, reviewed, diagrammed, GenAI-assisted, …). Add a GenAI note: specifically how you used GenAI, or that you did not. Unattributed sections are treated as incomplete, and group members not contributing substantively to at least one section will receive a 50% deduction on their group's deliverable.

Example attribution table:

| Section | Team member(s) | Contribution (how) |
|---|---|---|
| Class design | A. Student | Drafted; revised after group review |
| System diagram | B. Student | Sketched on whiteboard, photographed |
| Method signatures | A. & C. Student | Drafted with GenAI; checked against the diagram by hand |
| … | … | … |

Division of work is your plan for who builds what. Work Attribution records who wrote this document. Both are required.

**Meets** — every class has one clear MVC role; a system diagram groups the parts by role; the model/view boundary is explicit; data and state are defined; public methods are specified by signature and behavior; every named bad-input case maps to a response; the test plan shows the model is testable on its own; ownership of model, view, and controller is assigned with an integration plan; changes from your scope are explained; every section is attributed; and the GenAI note states specifically how (or that) GenAI was used.

## Submit

A 5–10 page document covering each item above, with the system diagram embedded or attached. The attribution table and GenAI note are required but do not count toward the page range. Lengths will differ by project, so aim for completeness and internal consistency.

> 🤖 **Working with GenAI Tip!** Use GenAI to probe your boundary: describe the model and view separately, then ask what the view would have to assume about the model to work. If the answer is "a lot," your line is in the wrong place. Record what you asked and what you changed in your GenAI note.

Due at the start of your lab session. Graded EMRN — aim for at least Meets. Bring your work to the grading session ready to defend it; treat it as an oral exam.

Full project context, learning objectives, and the EMRN table are on the *Project 3: Designing for Change - Overview* page.

---

## Rubric: Project 3 - Deliverable 2 - Specification

Each criterion below uses the same scale unless noted:

| Rating | Points |
|---|---|
| Exceeds | 11 |
| Meets | 8.5 |
| Revisable — missing one or more of the Meets criteria | 7.5 |
| Not There Yet — missing significant components | 5 |
| Missing | 0 |

### 1. Class Design by Role (/11 pts)

**Meets (8.5 pts)**
- ☐ Each class is labeled model, view, or controller
- ☐ The Queue sits in the model
- ☐ No class spans two roles

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Each class has a one-line responsibility

### 2. System Diagram (/11 pts)

**Meets (8.5 pts)**
- ☐ One picture of the main pieces and how they connect
- ☐ Shows which parts talk to which and what moves between them
- ☐ Boxes grouped by MVC role, so the model/view line is visible
- ☐ Legible; the shape is clear at a glance without reading the spec

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Names in the diagram match the class design and method signatures

### 3. Model / View Boundary (/11 pts)

**Meets (8.5 pts)**
- ☐ What the view knows about the model
- ☐ What the view deliberately does not know
- ☐ How the view learns the model changed

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Explains why that update mechanism fits this system

### 4. Data & State (/11 pts)

**Meets (8.5 pts)**
- ☐ Where the Queue lives
- ☐ What the Queue holds
- ☐ Fields each class owns, with types

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Marks which fields can change after construction

### 5. Method Signatures (/11 pts)

**Meets (8.5 pts)**
- ☐ Public API of the model, including `enqueue`, `dequeue`, `peek`, `isEmpty`
- ☐ Public API of the controller
- ☐ Name, parameters and types, and return type for each
- ☐ One-line behavior for each

**Exceeds (11 pts)** — All of Meets plus:
- ☐ States what each method does on bad input

### 6. Where Validation Lives (/11 pts)

**Meets (8.5 pts)**
- ☐ Every bad-input case from the scope is listed
- ☐ Each names the class/method that catches it
- ☐ Each says what happens (re-prompt, exception, default)

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Explains why each check lives in that layer

### 7. Test Plan (/11 pts)

**Meets (8.5 pts)**
- ☐ One normal case per public model method
- ☐ One bad-input case per public model method
- ☐ Makes clear the model is testable without the view

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Expected result stated for every case

### 8. Division of Work (/11 pts)

**Meets (8.5 pts)**
- ☐ Owner named for the model
- ☐ Owner named for the view
- ☐ Owner named for the controller
- ☐ Plan for integrating them into one working program

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Says when the pieces are first joined and who does it

### 9. Revised Scope (/11 pts)

**Meets (8.5 pts)**
- ☐ What changed since the scope doc
- ☐ Why
- ☐ Where each change came from (group discussion, GenAI, own reflection)

**Exceeds (11 pts)** — All of Meets plus:
- ☐ Says what each change costs or saves in the build

### 10. Work Attribution + GenAI Note (/0 pts)

**Complete (0 pts)**
- ☐ Table with one row per section
- ☐ Each row lists who worked on it and how (drafted, revised, reviewed, diagrammed, GenAI-assisted, …)
- ☐ GenAI note says specifically how GenAI was used, or that it wasn't

**Incomplete (0 pts)** — One or more of the Complete items is missing.
