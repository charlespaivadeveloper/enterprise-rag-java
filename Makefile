.PHONY: infra infra-obs infra-down run test quality checkstyle pmd spotbugs \
        kind-cluster kind-delete docker-build kind-load \
        helm-install helm-uninstall helm-lint \
        kind-up kind-down

KIND_CLUSTER ?= rag-platform
IMAGE_NAME   ?= enterprise-rag
IMAGE_TAG    ?= 0.1.1

# ── Local dev ────────────────────────────────────────────────
infra:
	docker compose up -d postgres

infra-obs:
	docker compose --profile observability up -d

infra-down:
	docker compose --profile observability down

run:
	SPRING_PROFILES_ACTIVE=local-stub mvn spring-boot:run

test:
	mvn verify
quality:
	mvn verify
checkstyle:
	mvn checkstyle:check
pmd:
	mvn pmd:check pmd:cpd-check
spotbugs:
	mvn compile spotbugs:check

# ── Kind / Helm ───────────────────────────────────────────────
kind-cluster:
	kind create cluster --name $(KIND_CLUSTER) --config kind-config.yaml

kind-delete:
	kind delete cluster --name $(KIND_CLUSTER)

docker-build:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .

kind-load: docker-build
	kind load docker-image $(IMAGE_NAME):$(IMAGE_TAG) --name $(KIND_CLUSTER)

helm-lint:
	helm lint helm/enterprise-rag

helm-install:
	helm upgrade --install enterprise-rag helm/enterprise-rag \
	  --set app.env.openaiApiKey=$(OPENAI_API_KEY) \
	  --set app.env.ragApiKey=$(RAG_API_KEY) \
	  --wait --timeout 5m

helm-uninstall:
	helm uninstall enterprise-rag

kind-up: kind-cluster kind-load helm-install

kind-down: helm-uninstall kind-delete
