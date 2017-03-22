.PHONY: run-state run-stream run-web

touch-logs:
	touch state-docker-publish.out stream-docker-publish.out  web-docker-publish.out

build-state-image: state-docker-publish.out
	cd state
	sbt docker:publishLocal | tee ../state-docker-publish.out 
	cd -

build-stream-image: stream-docker-publish.out
	cd stream
	sbt docker:publishLocal | tee ../stream-docker-publish.out 
	cd -

build-web-image: web-docker-publish.out
	cd web
	sbt docker:publishLocal | tee ../web-docker-publish.out 
	cd -

run-web: build-web-image
	docker-compose run --rm --service-ports web
