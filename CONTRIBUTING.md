# Contributing to NRI Plot Sentinel — Command Center

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to this project.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Branch Strategy](#branch-strategy)
- [Commit Message Convention](#commit-message-convention)
- [Pull Request Process](#pull-request-process)
- [Code Style Guide](#code-style-guide)
- [Component Architecture](#component-architecture)
- [Design System Rules](#design-system-rules)

---

## ✅ Code of Conduct

This project follows a respectful and professional code of conduct. All contributors are expected to:

- Be inclusive and welcoming.
- Provide constructive feedback only.
- Respect differing viewpoints.
- Focus on what is best for the project and its users (NRI plot owners).

---

## 🤝 How to Contribute

There are many ways to contribute:

1. **Report Bugs** — Open a GitHub Issue with a detailed description, steps to reproduce, and your environment.
2. **Suggest Features** — Open a GitHub Issue tagged `enhancement` with your use case.
3. **Submit Code Changes** — Fork the repo, create a branch, make changes, and open a Pull Request.
4. **Improve Documentation** — Typos, clarity improvements, or missing sections are always welcome.
5. **UI/UX Improvements** — If you see a visual inconsistency or have a design refinement, raise it.

---

## 🛠️ Development Setup

### Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Node.js | 18.x |
| npm | 9.x |
| Git | Any |

### Steps

```bash
# Fork the repository on GitHub, then:

# 1. Clone your fork
git clone https://github.com/YOUR-USERNAME/app.git
cd app

# 2. Add the upstream remote
git remote add upstream https://github.com/RootsSecure/app.git

# 3. Install dependencies
npm install

# 4. Start the dev server
npm run dev
# → App runs at http://localhost:5173/

# 5. Run the linter before committing
npm run lint
```

---

## 🌿 Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable, production-ready code. Protected. |
| `develop` | Integration branch for pending features. |
| `feature/<name>` | New features (branch from `develop`) |
| `fix/<name>` | Bug fixes (branch from `develop` or `main` for hotfixes) |
| `docs/<name>` | Documentation-only changes |

**Example:**
```bash
git checkout -b feature/websocket-real-time-alerts
git checkout -b fix/provisioning-token-reset
git checkout -b docs/api-integration-guide
```

---

## 📝 Commit Message Convention

Follow the **Conventional Commits** specification:

```
<type>(<scope>): <short summary>

[optional body]

[optional footer]
```

### Types

| Type | When to Use |
|------|-------------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only changes |
| `style` | CSS/UI changes that don't affect logic |
| `refactor` | Code restructuring without feature change |
| `perf` | Performance improvements |
| `test` | Adding or fixing tests |
| `chore` | Build process or tooling changes |

### Examples

```
feat(timeline): add real-time WebSocket event feed
fix(provisioning): prevent token field from being editable
docs(readme): add WebSocket integration guide
style(dashboard): improve metric card hover animation
```

---

## 📬 Pull Request Process

1. **Keep PRs small and focused** — One feature or fix per PR. Large PRs are hard to review.
2. **Fill in the PR template** thoroughly, including:
   - What changed and why
   - Screenshots for any UI changes
   - How to test the change
3. **All CI checks must pass** before merge.
4. **At least one approval** from a maintainer is required.
5. **Squash commits** when merging to keep the history clean.

---

## 🎨 Code Style Guide

### JavaScript / JSX

- Use **functional components** with React Hooks only. No class components.
- Use `const` for all declarations unless reassignment is unavoidable.
- Use **named exports** for components (not default exports where possible, but default is acceptable for page-level components).
- Avoid inline styles. All styles go into the corresponding `.css` file.
- Keep component files under **150 lines**. If larger, split into sub-components.

```jsx
// ✅ Good
const MetricCard = ({ label, value, unit, status }) => {
  return (
    <div className={`glass-panel metric-card status-${status}`}>
      <h2 className="metric-value">{value} <span>{unit}</span></h2>
      <p className="metric-label">{label}</p>
    </div>
  );
};

// ❌ Bad — inline styles, not reusable
const MetricCard = ({ value }) => (
  <div style={{ background: '#1E1E1E', padding: '24px' }}>
    <h2>{value}</h2>
  </div>
);
```

### CSS

- Use **CSS Custom Properties (variables)** from `index.css` for all colors, fonts, and spacing.
- Never use hardcoded hex colors directly in component CSS files. Always reference a token.
- Component-specific styles go in a `.css` file with the same name as the component.
- Global utilities (`.flex-1`, `.mb-8`, etc.) go in `index.css` only.

```css
/* ✅ Good — uses design tokens */
.alert-badge {
  background: rgba(var(--accent-critical-rgb), 0.15);
  color: var(--accent-critical);
}

/* ❌ Bad — hardcoded values */
.alert-badge {
  background: rgba(239, 68, 68, 0.15);
  color: #EF4444;
}
```

---

## 🏛️ Component Architecture

```
src/
├── components/
│   ├── [ComponentName].jsx     ← Logic + JSX
│   └── [ComponentName].css     ← Component-scoped styles
├── App.jsx                     ← Shell, routing, global state
├── index.css                   ← Global design system tokens
└── main.jsx                    ← Entry point (do not modify)
```

### Adding a New Screen

1. Create `src/components/MyScreen.jsx`
2. Create `src/components/MyScreen.css`
3. Import and render conditionally in `App.jsx` based on `activeTab` state.
4. Add a nav item to the `bottom-nav` in `App.jsx`.

---

## 🧪 Testing

Currently, this project uses manual browser testing. When adding features:

1. Test on both **desktop** (1280px+) and **mobile** (390px) viewports.
2. Verify no horizontal overflow using browser DevTools.
3. Check the browser console for errors or warnings — there should be **zero** before submitting a PR.

---

## 📮 Questions?

Open a [GitHub Discussion](https://github.com/RootsSecure/app/discussions) for general questions, or a [GitHub Issue](https://github.com/RootsSecure/app/issues) for bugs and feature requests.
