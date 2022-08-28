A lib + intellij plugin that converts spock tests to junit + mockito.

The lib targets jdk11, however, the build requires jdk17.

Setup:
1. Build the project `./gradlew clean build`.
2. Install the intellij plugin from disc - _nospock/intellij-plugin/build/distributions/intellij-plugin.zip_.
3. Select bundled jar to use for conversion. In settings (Tools -> NoSpock Settings) the 'Executable jar path' should point to the absolute path of the bundled jar -  _nospock/cmd/build/libs/cmd.jar_.
4. Within the project tree select a spock file or a directory that you want to convert. Use the _Refactor -> Spock to Java ☕_ action (linux <kbd>alt+shift+j</kbd>; macos <kbd>option+shift+j</kbd>)
