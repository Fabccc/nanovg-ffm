
## Github Flow

```mermaid
gitGraph
    commit id: "Initial commit"
    branch feature/my-feature
    checkout feature/my-feature
    commit id: "Work in progress"
    commit id: "Feature complete"
    checkout main
    merge feature/my-feature id: "Pull Request Merge"
    commit id: "Release preparation" tag: "v0.1.0"
    branch feature/next-fix
    checkout feature/next-fix
    commit id: "Fix bug"
    checkout main
    merge feature/next-fix id: "Bugfix Merge"
    commit id: "Bugfix release" tag: "v0.1.1"
```

### Release Process
1. Push your changes to `main`.
2. Create a new **Release** on GitHub.
3. Use a semantic version tag (e.g., `v1.2.3`).
4. Publishing the release will trigger the `release` workflow which builds native libraries and publishes to GitHub Packages.