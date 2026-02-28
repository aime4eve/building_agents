---
name: prd
description: |
  Generate comprehensive Product Requirements Documents (PRDs) for product managers. 
  Use this skill when users ask to:
  (1) Create/write/generate a PRD, product requirements, or feature specification
  (2) Document a feature or product requirements
  (3) Structure product specifications or requirements documents
  (4) Review/evaluate/optimize an existing PRD
  (5) Convert ideas/notes into formal requirements
  
  Triggers: "create a PRD", "write product requirements", "document a feature", 
  "PRD", "产品需求文档", "需求文档", "产品规格", "feature spec", "requirements doc",
  "product requirements", "功能需求", "需求规格"
---
# PRD Generator

Generate comprehensive, well-structured Product Requirements Documents (PRDs) that follow industry best practices.

## Core Workflow

When a user requests to create a PRD, follow this workflow:

### Step 1: Gather Context

Before generating the PRD, collect essential information through discovery:

**Required Information:**
- Feature/Product Name
- Problem Statement
- Target Users
- Business Goals
- Success Metrics
- Timeline/Constraints

**Discovery Questions:**
1. What problem are you trying to solve?
2. Who is the primary user/audience?
3. What are the key business objectives?
4. Any technical constraints?
5. What does success look like?
6. What's the timeline?
7. What's explicitly out of scope?

**Note:** Skip questions if user provides detailed brief upfront. Always clarify missing critical information.

### Step 2: Generate PRD Structure

Use the standard template from `references/prd_template.md`. Core sections:

1. Executive Summary
2. Problem Statement
3. Goals & Objectives
4. User Personas
5. User Stories & Requirements
6. Success Metrics
7. Scope (In/Out)
8. Technical Considerations
9. Design & UX Requirements
10. Timeline & Milestones
11. Risks & Mitigation
12. Dependencies & Assumptions
13. Open Questions

### Step 3: Create User Stories

For each major requirement, use standard format:

```
As a [user type],
I want to [action],
So that [benefit/value].

Acceptance Criteria:
- [Specific, testable criterion 1]
- [Specific, testable criterion 2]
```

See `references/user_story_examples.md` for patterns by domain.

### Step 4: Define Success Metrics

Use appropriate frameworks:
- **AARRR**: Acquisition, Activation, Retention, Revenue, Referral
- **HEART**: Happiness, Engagement, Adoption, Retention, Task Success
- **North Star**: Single key metric representing core value
- **OKRs**: Objectives and Key Results

See `references/metrics_frameworks.md` for detailed guidance.

### Step 5: Validate (Optional)

Run validation script to check PRD completeness:

```bash
scripts/validate_prd.sh <prd_file.md>
```

Checks for: required sections, user story format, metrics defined, scope clarity, no placeholders.

---

## Choosing PRD Type

| Type | When to Use | Length |
|------|-------------|--------|
| **Standard** | Major features, new products | 10-20 pages |
| **Lean** | Small features (< 2 weeks) | 2-5 pages |
| **One-Pager** | Bug fixes, minor improvements | 1-2 pages |

For lean/one-pager PRDs, use `references/lean_prd_template.md`.

---

## Reference Navigation

When generating PRDs, access these resources as needed:

### PRD Templates
- **Full template**: `references/prd_template.md` - Complete structure for major initiatives
- **Lean template**: `references/lean_prd_template.md` - Streamlined for small features

### User Stories by Domain
Search `references/user_story_examples.md` for:
- E-commerce: search, checkout, payments
- SaaS/B2B: collaboration, analytics, admin
- Mobile: notifications, offline mode
- Auth & Security: 2FA, password reset
- Content & Media: upload, sharing

### Metrics Frameworks
Search `references/metrics_frameworks.md` for:
- AARRR funnel metrics
- HEART UX metrics
- North Star Metric examples
- OKR templates

### Best Practices
Search `references/best_practices.md` for:
- Writing quality requirements (SMART)
- User story best practices (INVEST)
- Scope management
- Common issues & solutions

### Usage Patterns
Search `references/usage_patterns.md` for:
- New Feature PRD pattern
- Product Enhancement pattern
- New Product PRD pattern
- Quick PRD/One-Pager pattern

### Special Scenarios
Search `references/scenarios.md` for:
- Customer feature requests
- Strategic initiatives
- Technical debt PRDs
- Compliance/regulatory PRDs
- Platform migrations
- A/B tests

---

## Quick Quality Checklist

Before finalizing, verify:

- [ ] Problem is clear and specific
- [ ] Users/personas identified
- [ ] Success metrics defined with targets
- [ ] Scope bounded (in/out clearly stated)
- [ ] User stories have acceptance criteria
- [ ] Timeline validated with engineering
- [ ] Risks identified with mitigation

---

## Resources

### scripts/
- **validate_prd.sh** - Validates PRD completeness and quality
  - Note: Shell script requires Unix-like environment (Linux/macOS/WSL)

### references/
| File | Purpose |
|------|---------|
| prd_template.md | Full PRD structure template |
| lean_prd_template.md | Streamlined template for small features |
| user_story_examples.md | User story patterns by domain |
| metrics_frameworks.md | AARRR, HEART, North Star, OKRs |
| best_practices.md | Writing quality requirements |
| usage_patterns.md | PRD patterns by use case |
| scenarios.md | Special situation guidance |

---

## Common Triggers & Responses

| User Request | Action |
|--------------|--------|
| "Create a PRD for X" | Gather context → Generate full PRD |
| "Write requirements for X" | Clarify scope → Choose template → Generate |
| "Document this feature" | Ask for feature details → Generate PRD |
| "Review this PRD" | Analyze against checklist → Provide feedback |
| "Convert notes to PRD" | Parse notes → Fill gaps → Generate PRD |
| "Small feature PRD" | Use lean template |
| "产品需求文档" | Same as "create a PRD" |
