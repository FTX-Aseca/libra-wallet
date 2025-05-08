# How to Update CHANGELOG.md

1. **When to Update**:
   - Update the CHANGELOG.md as you develop new features
   - Add entries under the `[Unreleased]` section
   - Group changes by type (Added, Changed, Deprecated, Removed, Fixed, Security)

2. **How to Update**:
   - Add new entries at the top of each section
   - Use bullet points for main features
   - Use sub-bullets for details (as shown in the example)
   - Keep entries clear and concise

3. **When to Release**:
   - When you're ready to release, the workflow will:
     - Create a release with the current version
     - Move all `[Unreleased]` changes to a new version section
     - Create a new empty `[Unreleased]` section

4. **Best Practices**:
   - Write entries in present tense
   - Be specific about what changed
   - Group related changes together
   - Include both user-facing and technical changes

For example, if you're adding the money transfer feature:
1. Update CHANGELOG.md as shown above
2. Commit and push to main
3. The release workflow will:
   - Create a release (e.g., v0.1.0)
   - Update the version to 0.1.1-SNAPSHOT
   - Create a PR with the version bump