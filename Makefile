
.PHONY: build
build:
	cd plugin_src/ui/js && yarn install && yarn build && rm -Rf ./node_modules
	./gradlew plugin_prepare plugin
