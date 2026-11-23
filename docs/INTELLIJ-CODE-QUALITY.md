# IntelliJ code style and quality gates

## Formatting

1. Open the repository root as the IntelliJ project.
2. Confirm **Settings > Editor > Code Style > Scheme = Project**.
3. Confirm **Settings > Editor > Code Style > Enable EditorConfig support**.
4. Use **Code > Reformat Code** or `Ctrl+Alt+L`.
5. Optional: enable **Tools > Actions on Save > Reformat code** and **Optimize imports**.

The project uses a 180-column IDE margin to minimize line breaks. Checkstyle accepts existing generated/query-heavy lines up to 400 columns; normal application code should remain within the IDE margin. Compact does not mean compressed: do not combine unrelated statements just to save vertical space.

## IntelliJ plugins

Install these plugins from **Settings > Plugins > Marketplace**:

- CheckStyle-IDEA — select `config/checkstyle/checkstyle.xml` as the project configuration.
- PMD IntelliJ — select `config/pmd/ruleset.xml` where the plugin supports a custom ruleset.
- SpotBugs — use the project Maven goal as the source of truth if the plugin is unavailable for the current IntelliJ version.

IDE plugins provide fast feedback; Maven remains authoritative so local builds and CI enforce identical quality gates.

## Maven commands

| Purpose | Command |
|---|---|
| Check style | `mvn checkstyle:check` |
| PMD and duplicated code | `mvn pmd:check pmd:cpd-check` |
| SpotBugs | `mvn compile spotbugs:check` |
| Complete gate | `mvn verify` |

Reports are generated under `target/checkstyle-*`, `target/pmd.*`, `target/cpd.*` and `target/spotbugs*` depending on the invoked goal.
