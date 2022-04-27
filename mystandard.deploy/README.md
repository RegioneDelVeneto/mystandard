Deploy MyStandard

Gli ambienti di rilascio supportati in questa versione sono:


env-1: Overlay di Esempio con orchestratore K8s

Per generare i descrittori completi:
```
kustomize build overlays/<ambiente>
```
Per applicarli:

```
kustomize build overlays/<ambiente> | kubectl apply -f -
```

Per aggiornare le config owl, usare gli script update-config.sh nei vari overlay.
