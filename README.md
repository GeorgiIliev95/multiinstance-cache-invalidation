# Getting Started

### Prerequisite
Install and start local kubernetes cluster.
`minikube start`

### Installation

1. Compile `service-a` 
```
cd service-a && ./gradlew clean bootJar && cd ..
```
2. Build `service-a`
```
cd service-a && docker build --platform=linux/amd64 -t service-a:1.0 . && cd ..
```
3. Compile `orchestrator-service`
```
cd orchestrator-service && ./gradlew clean bootJar && cd ..
```
4. Build `orchestrator-service`
```
cd service-a && docker build --platform=linux/amd64 -t orchestrator-service:1.0 . && cd ..
```
5. Deploy to kubernetes
```
kubectl apply -f deployment.yaml
```
