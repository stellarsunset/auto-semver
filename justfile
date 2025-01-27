# List all available tasks
default:
  just --list

# Run the tests for the project
test:
  ./gradlew test
  ./gradlew functionalTest