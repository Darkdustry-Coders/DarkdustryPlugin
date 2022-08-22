<div align="center">
    <h1>Darkdustry Plugin</h1>
    <p>Основной плагин для серверов Darkdustry.</p>
</div>

<br>

## Contributing

Все необходимые правила и советы расписаны в [CONTRIBUTING](CONTRIBUTING.md).

## Компиляция

Gradle может потребоваться до нескольких минут для загрузки файлов. <br>
После сборки выходной jar-файл должен находиться в каталоге `/build/libs/DarkdustryPlugin.jar`.

Сначала убедитесь, что у вас установлен JDK 16-17. Откройте терминал в каталоге проекта и выполните следующие команды:

### Windows

_Компиляция:_ `gradlew jar`  

### Linux/Mac OS

_Компиляция:_ `./gradlew jar`  

### Устранение неполадок

#### Permission Denied

Если терминал выдает `Permission denied` или `Command not found` на Mac/Linux, выполните `chmod +x ./gradlew` перед запуском `./gradlew`. *Это одноразовая процедура.*
