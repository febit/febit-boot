
.PHONY: test
test:
	./gradlew test

.PHONY: check
check:
	./gradlew check

.PHONY: clean
clean:
	./gradlew clean

.PHONY: licenseFormat
licenseFormat:
	./gradlew licenseFormat

.PHONY: build
build:
	./gradlew build

.PHONY: build-fast
build-fast:
	./gradlew build -x check -x test
