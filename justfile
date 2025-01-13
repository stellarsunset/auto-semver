# List all available tasks
default:
  just --list

# Run the tests for the project
test:
  ./gradlew test
  ./gradlew functionalTest

# Perform a manual release of the project using the next version step of the specified type
release step="patch":
  ./gradlew release -P{{step}}
  ./gradlew publish