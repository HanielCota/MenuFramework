# Usando MenuFramework no seu Plugin

O `MenuFramework` é uma biblioteca Java para criar menus inventário no Paper/Spigot. Como ele **não é um plugin standalone** (não possui `plugin.yml`), você deve embutir as classes dele dentro do JAR do seu plugin usando o **Shadow Plugin**.

---

## Requisitos

- Java 21+
- Paper 1.21.1+ (ou compatível)
- Gradle com Shadow Plugin

---

## 1. Adicione o JitPack e a dependência

No `build.gradle` do **seu plugin**:

```groovy
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

repositories {
    mavenCentral()
    maven { url = 'https://repo.papermc.io/repository/maven-public/' }
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
    
    // MenuFramework já inclui Caffeine e FastUtil (fat-jar)
    implementation 'com.github.HanielCota:MenuFramework:v1.0.0'
}
```

> **Dica:** Substitua `v1.0.0` pela versão desejada. Veja as releases em:  
> `https://github.com/HanielCota/MenuFramework/releases`

---

## 2. Configure o Shadow com Relocation

**Sempre use `relocate`** para evitar conflitos caso outro plugin também embuta o MenuFramework:

```groovy
shadowJar {
    relocate 'com.github.hanielcota.menuframework', 'seuplugin.libs.menuframework'
    archiveClassifier.set('')
}

tasks.build.dependsOn tasks.shadowJar
```

> Troque `seuplugin` pelo package do seu plugin (ex: `me.haniel.minhaloja`).
>
> **Não precisa mais fazer relocate de `caffeine` ou `fastutil`** — eles já vêm embutidos dentro do MenuFramework.

---

## 3. Exemplo completo de uso

```java
package me.haniel.minhaloja;

import com.github.hanielcota.menuframework.MenuFramework;
import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.builder.MenuBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MinhaLojaPlugin extends JavaPlugin {

    private MenuService menuService;

    @Override
    public void onEnable() {
        // Inicializa o framework vinculado ao seu plugin
        MenuFramework framework = MenuFramework.create(this);
        this.menuService = framework.service();

        getLogger().info("MenuFramework inicializado!");
    }

    public void abrirLoja(Player player) {
        MenuBuilder.builder()
            .id("loja-principal")
            .title("<green>Loja de Itens")
            .rows(3)
            .slot(10, new ItemStack(Material.DIAMOND_SWORD), ctx -> {
                ctx.player().sendMessage("§aVocê comprou uma Espada de Diamante!");
                ctx.player().getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                ctx.close();
            })
            .slot(13, new ItemStack(Material.GOLDEN_APPLE), ctx -> {
                ctx.player().sendMessage("§aVocê comprou uma Maçã Dourada!");
                ctx.player().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            })
            .slot(16, new ItemStack(Material.BARRIER), ClickContext::close)
            .build(menuService)
            .open(player);
    }
}
```

---

## 4. Compile com Shadow

```bash
./gradlew shadowJar
```

O JAR final estará em:
```
build/libs/seuplugin-1.0.0.jar
```

Ele já contém o MenuFramework + Caffeine + FastUtil embutidos.

---

## Como funciona?

| Componente | Função |
|-----------|--------|
| `implementation` | Inclui o MenuFramework (já contém Caffeine e FastUtil embutidos) |
| `shadowJar` | Empacota o MenuFramework dentro do seu JAR |
| `relocate` | Renomeia o pacote do MenuFramework para evitar conflitos com outros plugins |

---

## Problemas comuns

### `ClassNotFoundException: com.github.hanielcota.menuframework...`
- Você esqueceu de rodar `./gradlew shadowJar` e está usando o JAR normal (sem dependências).

### Conflito com outro plugin
- Outro plugin também embutiu o MenuFramework sem `relocate`. A solução é **sempre usar relocate** nos dois plugins.

### Não encontra a dependência no JitPack
- Verifique se a release/tag existe no GitHub.
- Ou use a versão `-SNAPSHOT` com `implementation 'com.github.HanielCota:MenuFramework:master-SNAPSHOT'`.
