
eval $(minikube docker-env)

kubectl create configmap swagger-config --from-file=../oas/api-doc/
kubectl apply -f tsaas-swagger-ui-deployment.yml
