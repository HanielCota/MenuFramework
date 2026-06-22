# Publicação no Maven Central

Este projeto está configurado para publicar pelo Sonatype Central Portal usando o plugin Gradle Maven Publish da Vanniktech.

## Coordenadas

```text
io.github.hanielcota:menu-framework:1.0.0
```

Os packages Java continuam em `com.hanielfialho.menuframework`. O `groupId` Maven usa `io.github.hanielcota` porque a Sonatype aceita namespaces pessoais vinculados ao GitHub.

## Configuração Única na Sonatype

1. Entre no [Central Portal](https://central.sonatype.com/) com a conta GitHub `HanielCota`.
2. Verifique ou crie o namespace `io.github.hanielcota`.
3. Gere um user token no Central Portal.
4. Crie uma chave GPG e publique a chave pública para que o Maven Central consiga validar as assinaturas.

O repositório GitHub ainda está privado. Para um release público saudável, torne o repositório público antes da primeira publicação ou confirme que você quer manter apenas o source jar publicado no Maven Central.

Secrets necessários no GitHub:

```text
MAVEN_CENTRAL_USERNAME
MAVEN_CENTRAL_PASSWORD
SIGNING_IN_MEMORY_KEY
SIGNING_KEY_ID
SIGNING_IN_MEMORY_KEY_PASSWORD
```

Exporte a chave privada de assinatura com:

```bash
gpg --export-secret-keys --armor <key-id>
```

Use o bloco exportado inteiro como valor de `SIGNING_IN_MEMORY_KEY`.

## Release

Atualize `version` em `gradle.properties`, depois crie e envie uma tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

O workflow `Publish` do GitHub Actions executa:

```bash
./gradlew publishAndReleaseToMavenCentral
```

Para um upload local/manual, configure os mesmos valores como propriedades Gradle ou variáveis de ambiente e execute:

```bash
./gradlew publishToMavenCentral
```

Esse comando envia uma deployment para publicação manual no Central Portal. O workflow de CI usa release automático.

Para testar o pacote localmente sem assinatura GPG:

```bash
./gradlew publishToMavenLocal
```
