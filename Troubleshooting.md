# Country Info Service — Kubernetes Troubleshooting Guide

## 1. Pod shows `ImagePullBackOff` or `ErrImageNeverPull`

**Possible cause:** Kubernetes cannot find or retrieve `country-info-service:1.0`.

Check that the image exists:

```powershell
docker image ls country-info-service
```

Inspect the pod events:

```powershell
kubectl describe pod -n country-info -l app=country-info-service
```

Confirm that the manifest contains:

```yaml
image: country-info-service:1.0
imagePullPolicy: IfNotPresent
```

If the image was built after the pod was created, rebuild it and restart the deployment:

```powershell
docker build -t country-info-service:1.0 .
kubectl rollout restart deployment/country-info-service -n country-info
kubectl rollout status deployment/country-info-service -n country-info
```

If Docker Desktop's Kubernetes runtime cannot see the local image, check the Docker Desktop Kubernetes and container image-store configuration. Alternatively, push the image to a registry accessible to the cluster and update the manifest to use that registry image.

## 2. MySQL pod does not become Ready

Inspect the pod and logs:

```powershell
kubectl get pods -n country-info
kubectl describe pod -n country-info -l app=mysql
kubectl logs -n country-info deployment/mysql
```

Look for:

* An invalid or missing Secret.
* Database initialization errors.
* Storage provisioning problems.
* Insufficient disk space.

Check the PVC:

```powershell
kubectl get pvc -n country-info
kubectl describe pvc mysql-data -n country-info
```

The PVC should eventually show `Bound`. If it remains `Pending`, check the cluster's available StorageClasses:

```powershell
kubectl get storageclass
```

## 3. Application fails with a database connection error

Typical errors include `Communications link failure`, `Connection refused`, or `Access denied`.

Confirm the MySQL Service and endpoints exist:

```powershell
kubectl get service mysql -n country-info
kubectl get endpoints mysql -n country-info
```

Check MySQL logs:

```powershell
kubectl logs -n country-info deployment/mysql
```

Check the application environment variable names and values without printing the password:

```powershell
kubectl exec -n country-info deployment/country-info-service -- printenv SPRING_DATASOURCE_URL
kubectl exec -n country-info deployment/country-info-service -- printenv SPRING_DATASOURCE_USERNAME
```

The expected URL is:

```text
jdbc:mysql://mysql:3306/country_info
```

Inside Kubernetes, `mysql` resolves to the MySQL Service. Do not use `localhost` for the database URL; inside the application container, that refers to the application container itself.

If credentials have changed after MySQL initialized its persistent data directory, changing the Secret alone does not automatically change the existing MySQL user's password. Update the database account to match the Secret, or reset the database in a disposable assessment environment if retaining its data is unnecessary.

## 4. Application pod repeatedly restarts

Inspect logs from the current and previous container instances:

```powershell
kubectl logs -n country-info deployment/country-info-service
kubectl logs -n country-info deployment/country-info-service --previous
kubectl describe deployment country-info-service -n country-info
```

Check for:

* Incorrect datasource configuration.
* Missing Java dependencies or incompatible Java versions.
* An incorrect JAR path in the Dockerfile.
* Spring configuration errors.
* Database connection failures.

Verify that the Docker image uses a Java runtime compatible with the version used to compile the application.

## 5. REST API returns `Connection refused`

Ensure the application pod is Ready:

```powershell
kubectl get pods -n country-info
kubectl get service country-info-service -n country-info
```

Start port forwarding again:

```powershell
kubectl port-forward -n country-info svc/country-info-service 8080:80
```

Keep that terminal open and try the request from a second terminal:

```powershell
Invoke-RestMethod http://localhost:8080/api/countries
```

If port `8080` is already in use on Windows, use another local port:

```powershell
kubectl port-forward -n country-info svc/country-info-service 8081:80
```

Then access `http://localhost:8081/api/countries`.

## 6. SOAP integration fails

The application must be able to reach the external SOAP service from inside its pod.

Inspect logs:

```powershell
kubectl logs -n country-info deployment/country-info-service
```

Check for timeouts, DNS errors, SOAP faults, or XML parsing exceptions.

Verify that:

* Docker Desktop has working internet connectivity.
* The external SOAP endpoint is reachable and responding.
* The SOAP operation names, namespaces, and request fields match the working SoapUI requests.
* The SOAP service has not changed or become unavailable.

A SOAP request that succeeds from Windows does not guarantee that a request will succeed from inside Kubernetes.

## 7. API returns HTTP 500

Inspect the application logs to identify whether the failure comes from the database, SOAP client, entity persistence, or response mapping.

```powershell
kubectl logs -n country-info deployment/country-info-service --tail=200
```

Test a simple GET request before testing the SOAP synchronization endpoint. A GET failure and a SOAP synchronization failure may have different causes.

## 8. Changes to the image are not appearing

Rebuild the image:

```powershell
docker build -t country-info-service:1.0 .
```

Then restart the deployment:

```powershell
kubectl rollout restart deployment/country-info-service -n country-info
kubectl rollout status deployment/country-info-service -n country-info
```

For a more reproducible workflow, assign a new image tag to each build and update the Deployment manifest to match.

## 9. Useful diagnostic commands

```powershell
kubectl get all -n country-info
kubectl get events -n country-info --sort-by=.metadata.creationTimestamp
kubectl describe deployment country-info-service -n country-info
kubectl describe deployment mysql -n country-info
kubectl logs -n country-info deployment/country-info-service
kubectl logs -n country-info deployment/mysql
```

These commands are usually the best starting point when a pod fails to start, a deployment does not become Ready, or a request fails.
