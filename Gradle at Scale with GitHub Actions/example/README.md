# Why not `gradle/actions/setup-gradle`

We are changing some default settings, like enabling dependency graph submission.
We discussed enabling this for everyone with upstream here https://github.com/gradle/actions/issues/174.

# Inputs

We are not supporting all `setup-gradle` configuration options. If you need anything - open an issue https://github.com/allegro-actions/setup-gradle/issues

# Usage

```
name: Pull Request
on: [pull_request]
jobs:
  build:
    runs-on: [ubuntu-latest]
    steps:
      - uses: allegro/setup-gradle@v1
      - run: ./gradlew build
```
