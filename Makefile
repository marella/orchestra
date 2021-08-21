name ?= nginx
image ?= nginx:alpine
replicas ?= 2

.PHONY: build test

build:
	./gradlew build

test:
	./gradlew test

up:
	make build
	docker-compose up

down:
	docker stop `docker ps -qf 'label=io.github.marella.orchestra.core.Pod'` || true # stop all containers created by orchestra
	docker-compose down --volumes

get:
	docker exec 'orchestra_api01_1' curl -sX GET 'http://localhost:8080/deployments'

deploy:
	docker exec 'orchestra_api01_1' curl -sX PUT 'http://localhost:8080/deployments' -H 'Content-Type: application/json' -d '{"name":"$(name)","image":"$(image)","replicas":$(replicas)}'

delete:
	docker exec 'orchestra_api01_1' curl -sX DELETE 'http://localhost:8080/deployments/$(name)'
