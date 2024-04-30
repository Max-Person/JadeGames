Проект по реализации простых игр на основе [Jade](https://jade.tilab.com/) и агентно-ориентированного программирования. За основу взят проект [Start-Jade](https://startjade.gitlab.io/), больше информации можно просмотреть там.

# Как запускать
Склонировать репозиторий, подтянуть зависимости maven, радоваться. (В проекте используется maven, Jade подтягивается из репозитория [Jade Fork](https://jade-project.gitlab.io/page/download/) с помощью jitpack, поэтому никаких дополнительнных действий больше не нужно.)

Main-классом является класс jade_players.Principal, который и нужно запускать. При запуске, проект откроет дебаг-окна Sniffer-агента и RMA, и будет ждать вашего ввода в консоли. Это время дается, чтобы включить слежку за объектами в Sniffer-е до общего запуска агентов. Для продолжения программы нажмите enter в консоли.

# Как реализовать своих игроков
Агенты-игроки реализовываются, наследуясь от абстрактного класса jade_players.gameplay.PlayerAgent. Добавить их в разворачиваемый jade-контейнер можно через метод jade_players.Principal.createAgents().

```java
//В createAgents()
createOneAgent( // Организует игры между агентами-игроками
    containerController,
    "MM",
    MatchMakerAgent.class.getName(),
    agentList,
    new Object[0]
);

createOneAgent( // Первый игрок
    containerController,
    "FirstPlayer",
    YourPlayerClass.class.getName(),
    agentList,
    new Object[0]
);

createOneAgent( // Второй игрок
    containerController,
    "SecondPlayer",
    YourPlayerClass.class.getName(),
    agentList,
    new Object[0]
);
```