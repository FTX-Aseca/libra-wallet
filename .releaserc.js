// .releaserc.js
module.exports = {
  branches: [
    'main',
    { name: 'dev', prerelease: 'dev', channel: 'dev' } // Creates pre-releases like v1.0.0-dev.1 from the dev branch
  ],
  plugins: [
    '@semantic-release/commit-analyzer', // Analyzes commit messages to determine release type
    '@semantic-release/release-notes-generator', // Generates release notes from commits
    [
      '@semantic-release/changelog', // Updates CHANGELOG.md
      {
        changelogFile: 'CHANGELOG.md',
      },
    ],
    [
      '@semantic-release/exec', // Executes custom commands
      {
        // Updates the version in build.gradle
        // This command assumes your version line is like: version = '1.0.0'
        // It will be updated to, e.g., version = '1.2.3' or version = '1.2.3-dev.1'
        prepareCmd: 'sed -i "s/version = \'.*\'/version = \'${nextRelease.version}\'/" build.gradle',
      },
    ],
    [
      '@semantic-release/git', // Commits updated files like CHANGELOG.md and build.gradle
      {
        assets: ['CHANGELOG.md', 'build.gradle'],
        message: 'chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}',
      },
    ],
    [
      '@semantic-release/github', // Creates GitHub releases and uploads assets
      {
        // You can specify assets to upload here if you have build artifacts
        // assets: [
        //   { path: 'build/libs/*.jar', label: 'MyProject JAR' }
        // ]
      }
    ],
  ],
}; 