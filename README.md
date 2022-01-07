# Apiman Developer Portal

## Diagram of Components
![Diagram](./docu/component-diagram.svg)


## Local Development

* You have to create a copy of the `src/assets/config.json5` called `src/assets/local-config.json5`
* Adapt the `endpoint` and `auth.url` to match you apiman and keycloak setup
* Execute `npm run start`

E.g

```bash
cp src/assets/config.json5 src/assets/local-config.json5
npm run start
```
