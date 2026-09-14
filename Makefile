.PHONY: test test-debug lint format build clean check help

help:
	@echo "Available commands:"
	@echo "  make test        - Run all unit tests"
	@echo "  make test-debug  - Run debug unit tests"
	@echo "  make lint        - Check code formatting with ktlint"
	@echo "  make format      - Auto-format code with ktlint"
	@echo "  make build       - Build debug APK"
	@echo "  make clean       - Clean build artifacts"
	@echo "  make check       - Run lint and debug unit tests"

test:
	./gradlew test

test-debug:
	./gradlew :app:testDebugUnitTest

lint:
	./gradlew ktlintCheck

format:
	./gradlew ktlintFormat

build:
	./gradlew assembleDebug

clean:
	./gradlew clean

check: lint test-debug
