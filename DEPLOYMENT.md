# Country Info Service — Kubernetes Deployment Guide

## Prerequisites

* Docker Desktop installed and running.
* Kubernetes enabled in Docker Desktop.
* `kubectl` installed and connected to the Docker Desktop cluster.
* Docker image `country-info-service:1.0` built locally.
* The application configured to listen on port `8080`.

Verify the tools:

```powershell
docker version
kubectl config current-context
kubectl get nodes
docker image ls country-info-service
```

The Kubernetes context should normally be `docker-desktop`, and the node should be `Ready`.

## Step 1: Create the namespace

Run from the project root:

```powershell
kubectl apply -f k8s/00-namespace.yaml
```

## Step 2: Create database credentials

Create a Kubernetes Secret. Replace these example passwords with your own values:

```powershell
kubectl create secret generic country-db-secret `
  --namespace country-info `
  --from-literal=mysql-root-password='ReplaceWithAStrongRootPassword' `
  --from-literal=db-password='ReplaceWithAStrongAppPassword'
```

If the Secret already exists, update it with:

```powershell
kubectl create secret generic country-db-secret `
  --namespace country-info `
  --from-literal=mysql-root-password='ReplaceWithAStrongRootPassword' `
  --from-literal=db-password='ReplaceWithAStrongAppPassword' `
  --dry-run=client -o yaml | kubectl apply -f -
```

Do not commit real passwords to source control.

## Step 3: Deploy MySQL

```powershell
kubectl apply -f k8s/01-mysql.yaml
kubectl rollout status deployment/mysql -n country-info --timeout=180s
kubectl get pods -n country-info
```

Wait for the MySQL pod to become `Running` and `Ready` before proceeding.

## Step 4: Deploy the application

```powershell
kubectl apply -f k8s/02-app.yaml
kubectl rollout status deployment/country-info-service -n country-info --timeout=180s
```

Check deployments, pods, and services:

```powershell
kubectl get deployments,pods,services,pvc -n country-info
```

The application image uses `imagePullPolicy: IfNotPresent` so Kubernetes can use the locally available image. If the pod reports `ImagePullBackOff`, see the troubleshooting guide.

## Step 5: Access the REST API

Forward your local port `8080` to the Kubernetes Service:

```powershell
kubectl port-forward -n country-info svc/country-info-service 8080:80
```

Keep this terminal open. In another terminal, test:

```powershell
Invoke-RestMethod http://localhost:8080/api/countries
```

The service is exposed locally through port forwarding; it is not published externally by a LoadBalancer or NodePort.

## Step 6: Test the SOAP-to-database flow

Submit a country name using the endpoint implemented by the application:

```powershell
$body = @{ name = "Tanzania" } | ConvertTo-Json

Invoke-RestMethod `
  -Uri http://localhost:8080/api/countries/sync `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Then verify the saved country:

```powershell
Invoke-RestMethod http://localhost:8080/api/countries
```

Use the other CRUD endpoints from the assessment to verify retrieval, update, and deletion.

## Step 7: View application logs

```powershell
kubectl logs -n country-info deployment/country-info-service
```

Follow logs continuously:

```powershell
kubectl logs -n country-info deployment/country-info-service -f
```

## Cleanup

Delete the assessment deployment and its data when you no longer need them:

```powershell
kubectl delete namespace country-info
```

**Warning:** deleting the namespace also deletes its Kubernetes resources, Secret, and PVC. Depending on the storage provisioner and reclaim policy, the underlying database storage may also be deleted.
