# List all available tasks
default:
  just --list

# Run the tests for the project
test:
  ./gradlew test functionalTest

release type='patch': test
  ./gradlew release -P{{type}}
  git push origin tag $(git describe --tags --abbrev=0)
  ./gradlew publish