
eval $(minikube docker-env)

kubectl apply -f tsaas-backend-statefulset.yml
