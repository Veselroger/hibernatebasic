# <a name="Home"></a> Hibernate Basic

## Table of Content:
- [Java Persistence API](#jpa)
- [Bootstrap](#bootstrap)
- [Entity](#entity)
    - [ID Generation](#id)
    - [Equals and HashCode](#equalsHash)
    - [Lifecycle](#lifecycle)
- [Second Level Cache](#cache)
- [Mapping](#mapping)
- [Associations](#associations)
	- [One-to-One](#onetoone)
    - [One-to-Many & Many-to-One](#onetomany)
    - [Many-to-Many](#manytomany)
- [Lazy](#lazy)
- [JPQL](#jpql)
- [Java Application](#application)
    - [Логирование](#logging)
    - [Persistence Unit (persistence.xml)](#persistence)
    - [Domain object](#domainobject)
    - [Unit Test](#test)

------------

## [↑](#Home) <a name="jpa"></a> Java Persistence API (JPA)
Существует понятие **"Three-tier architecture"** (трёхуровневая архитектура). Данная архитектура разграничивает приложение на три слоя/уровня:

![](./img/0_Schema.png)

Уровень "Представление" (**Presentation**) отвечает за представление данных пользователю, а так же через это же представление позволяет пользователю взаимодействовать с приложением. Можно сказать, что это такой интерфейс взаимодействия приложения с пользователем.
Уровень "Бизнес логика" (**Business Layer**) занимается манипуляцией с данными. Именно этот слой знает, по каким правилам нужно создавать/удалять/изменять данные.
Уровень "Доступа к данным" (**Data Access Layer**) отвечает за непосредственно сохранение/удаление/изменение в источник данных.

**JPA** призван решить некоторые проблемы на уровне доступа к данным:
- Удобно соотнести Java классы с данными в БД
- Уменьшить связанность с конкретными технологиями хранения данных

Удобство работы с БД решается при помощи использования технологии ORM.
**ORM** - это Object-Relational Mapping. Благодаря этой технологии класс проецируется на таблицу в БД и наоборот:

![](./img/1_ORM.png)

Проблема тесной связи с технологиями хранения данных решается при помощи введения более верхнеуровневого (более абстрактного) API. То есть вместо "Сохрани в базу данных" приложение теперь просто говорит "Сохрани данные".

**Java Persistence API (JPA)** — это спецификация, которая описывает работу с Java объектами как с объектами из базы данных и наоборот. Спецификация JPA гласит:
> The technical objective of this work is to provide an object/relational mapping facility for the Java application developer using a Java domain model to manage a relational database. 

JPA - это часть набора спецификаций **Java EE**. Со временем поддержка Java EE перешла от Oracle к Eclipse Foundation и **Java EE** стала **Jakarta EE**, что отразилось и на спецификации.

JPA 2.2 спецификацию можно найти на сайте Oracle, а JPA 3.0 - на сайте Jakarta:
- [JSR 338: JavaTM Persistence API, Version 2.2](https://download.oracle.com/otn-pub/jcp/persistence-2_2-mrel-spec/JavaPersistence.pdf)
- [Jakarta Persistence 3.0](https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html)

JPA - это спецификация (т.е. некоторый API). У JPA есть различные реализации. Тот, кто реализует спецификацию, называется **провайдером** (**JPA provider**). Одним из jpa провайдеров является **[Hibernate](https://hibernate.org/orm/)**.

Видео про применение Hibernate: **"[Thorben Janssen: Should You Use JPA and Hibernate for Your next Project?](https://www.youtube.com/watch?v=uVLujq7_35E&list=PL50BZOuKafAYFT_F4Yris5Vj2ApwzUfmR&index=1)"**.

В документации Hibernate можно найти пункт **"[The Hibernate Modules/Artifacts](https://docs.jboss.org/hibernate/orm/current/quickstart/html_single/#_the_hibernate_modulesartifacts)"**, который гласит, что самое его сердце называется **hibernate core**.

------------

## [↑](#Home) <a name="bootstrap"></a> Bootstrap
Hibernate - это реализация JPA, а следовательно есть два "стиля": **native** (собственный API Hibernate) и **jpa** (по специфкиации).

Подробнее про запуск при помощи каждого из них см. следующие материалы:
- [How to use Hibernate’s native bootstrapping API](https://thorben-janssen.com/hibernate-tips-use-hibernates-native-bootstrapping-api)
- [JPA’s Bootstrapping API](https://thorben-janssen.com/hibernate-getting-started/#JPA8217s_Bootstrapping_API)

При использовании native Hibernate подхода или подхода JPA мы должны предоставить конфигурационный файл. Где располагать файл конфигурации так же зависит от того, какой подход выбран. Например, JPA подход будет искать файл "META-INF/persistence.xml", в то время как Hibernate будет искать конфигурационный файл в ресурсах.

Реализация Hibernate и спецификация JPA имеют разные термины.

Hibernate оперирует понятиями **SessionFactory** и **Session**, в то время, как JPA описывает **EntityManagerFactory** и **EntityManager**.

Спецификация JPA вводит понятие **Persistence Unit**, который по сути представляет один источник данных. JPA описывает все persistence unit внутри единого **persistence.xml**, в то время как в Hibernate каждый persistence unit имеет отдельный конфигурационный файл.

При использовании Dependency Injection оба эти подхода так же отличаются. JPA позволяет получать зависимость через аннотацию "@PersistenceContext", которая позволяет ссылаться на определённый Persistence Unit, в то время как при использовании Hibernate напрямую необходимо использовать средства реализации используемого фрэймворка, такого как Spring.

При ручном создании синтаксис очень похож:
```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();
```
и
```java
SessionFactory sf1 = new Configuration().configure("a.cfg.xml").buildSessionFactory();
Session mySession = sf1.openSession();
Transaction tx = mySession.beginTransaction();
```

**ВАЖНО** помнить, что EntityManagerFactory и SessionFactory являются "immutable", т.е. неизменяемыми. Таким образом они являются thread safe, т.е. потокобезопасными. Однако их создание - тяжёлая операция.

------------

## [↑](#Home) <a name="entity"></a> Entity (сущности)
Спецификация JPA оперирует термином "сущность" (**entity**). Это же понятие использует и Hibernate.

Спецификация говорит, то **Entity** - это некий сохраняемый объект доменной области.
Например, у нас есть понятие доменной области - **Account**.

Чтобы сущность из мира Java стала сущностью в мире JPA нужна аннотация **@Entity**.
Также к сущностям предъявляются следующие требования:
- должен быть конструктор без аргументов (при этом можно иметь и другие конструкторы). Он должен быть public или protected
- сущность выражена верхнеуровневым классом (т.е. не вложенным)
- класс не должен быть final и не должны быть final переменные/методы, которые используются для изменения состояния сущности

Есть ещё важное требование. Для каждой сущности должно быть указано, как её можно однозначно идентифицировать в базе данных. Для этого используется аннотация **@Id**. На стороне базы данных для этого используется **"Первичный Ключ"**, он же **Primary Key** (**PK**).

Аннотация **@Id** влияет на то, каким образом выполняется доступ к состоянию сущности (**[Access strategies](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#access)**).

Существует две стратегии:
- **Property access** (через геттер и сеттер)
- **Field access** (напрямую через поля при помощи reflection)

Если аннотацию **@Id** поставить над геттером - будет property acсess, а если над полем - field access. Кроме того, на этот факт можно повлиять при помощи аннотации **"[@Access](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#_overriding_the_default_access_strategy)"**.

------------

### [↑](#Home) <a name="id"></a> ID Generation
Как ранее было сказано, по полю/методу, помеченному как @Id, JPA Provider может отличить одну сущность от другой.\
Чтобы облегчить работу с ID мы можем для него сгенерировать (**Generate**) значение (**Value**) при помощи аннотации:
```java
@Id
@GeneratedValue
private Long id;
```

Существует несколько стратегий генерации ID. Стратегия может быть изменена при помощи аннотации **GenerationType**:
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_gen")
private Long id;
```

Существуют следующие стратегии:
- **GenerationType.AUTO** - JPA Provider сам выберет стратегию (на основе БД)
- **GenerationType.SEQUENCE** - JPA Provider будет использовать Database Sequence
- **GenerationType.IDENTITY** - JPA Provider будет использовать отдельный столбец для ID
- **GenerationType.TABLE** - JPA Provider будет использовать отдельную таблицу

Они разные и у всех есть свои плюсы и минусы.

**GenerationType.TABLE** является устаревшим и самым не оптимальным выбором. Данная стратегия использует отдельную таблицу для хранения ID. Из-за необходимости поддерживать корректную работу нескольких потоков страдает производительность. Подробнее можно прочитать здесь:
**[Why you should never use the TABLE identifier generator with JPA and Hibernate](https://vladmihalcea.com/why-you-should-never-use-the-table-identifier-generator-with-jpa-and-hibernate/)**

**GenerationType.AUTO** является тоже не очень хорошим выбором. При этой стратегии JPA Provider сам выбирает стратегию на основе того, какая БД используется. И этот выбор не всегда является правильным. Например, Hibernate JPA Provider версии 5 на базах MySQL не имея возможности использовать SEQUENCE вместо IDENTITY выбирал TABLE, а это плохо, как мы видели ранее.
Подробнее можно прочитать здесь:
**[Why should not use the AUTO JPA GenerationType with MySQL and Hibernate](https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/)**

**GenerationType.IDENTITY** использует автоинкрементируемый столбец в таблице сущности. Особенность этого автоинкремента заключается в том, что инкрементация значения выполняется вне текущей транзакции, поэтому мы не можем узнать значения до выполнения INSERT выражения. Это приводит к тому, что Hibernate отключает пакетное выполнение SQL запросов (JDBC batch support).
Подробнее читать здесь:
**[Hibernate disabled insert batching when using an identity identifier generator](https://stackoverflow.com/questions/27697810/hibernate-disabled-insert-batching-when-using-an-identity-identifier-generator)**

**GenerationType.SEQUENCE** является самой оптимальной стратегией.
Она использует Database Sequence. Пример использоания:
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_gen")
@SequenceGenerator(name = "acc_gen", sequenceName = "acc_seq")
private Long id;
```

Интересной особенностью является то, что Hibernate чтобы постоянно не запрашивать следующий номер последовательности получается 2 следующих числа последовательности. Например, если шаг для sequence равен 50, то Hibernate получит значения 1 и 50. Таким образом на первые 50 новых значений будет сделано всего 2 запроса sequence.

Подробнее про маппинг и генерацию первичного ключа можно прочитать здесь:
- **[5 Primary Key Mappings for JPA and Hibernate Every Developer Should Know](https://thoughts-on-java.org/primary-key-mappings-jpa-hibernate/)**
- **[Hibernate Tip: How does Hibernate’s native ID generator work?](https://www.youtube.com/watch?v=QfuAMZLSvwo)**.

**ВАЖНО** помнить, что при генерируемом ID присваивать вручную ID нельзя, в противном случае будет получен exception:
> PersistentObjectException: detached entity passed to persist


### [↑](#home) <a id="equalsHash"></a> Equals and HashCode
Важно понимать, как должны быть реализованы Equals и HashCode.\
Более подробно можно прочитать в материалах:
- **"[Ultimate Guide to Implementing equals() and hashCode() with Hibernate](https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate/)"**
- **"[How to implement equals and hashCode using the JPA entity identifier](https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/)"**

Стоит придерживаться основного требования:
> Equals and hashCode must behave consistently across all entity state transitions.

Кроме того, говоря про Lombok стоит помнить про нюансы, описанные в материале **"[Lombok & Hibernate: How to Avoid Common Pitfalls](https://thorben-janssen.com/lombok-hibernate-how-to-avoid-common-pitfalls/)"**. Например, не стоит использовать аннотацию **@Data**.


### [↑](#Home) <a name="lifecycle"></a> Entity Lifecycle
Очень важным вопросом является жизненный цикл (lifecycle).
Жизненный цикл описан в спецификации в разделе **"3.2 Entity Instance’s Life Cycle"**.
Про lifecycle можно так же прочитать непосредственно у Hibernate: **"[Persistence Context](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#pc)"**. Кроме того можно прочитать статью **"[Entity Lifecycle Model in JPA & Hibernate](https://thorben-janssen.com/entity-lifecycle-model/)"**.

JPA Provider начинает отслеживать любые изменения в instance сущности только тогда, когда этот instance находится под контролем JPA Provider'а. Такая зона зона контроля JPA Provider'а называется **Persistence Context**. Напрямую к Persistence Context'у доступа нет, но есть посредник, через которого выполняется работа с Persistence Context. Такими посредниками являются **Entity Manager** и **[Session](https://docs.jboss.org/hibernate/orm/current/javadocs/org/hibernate/Session.html)**.

Чтобы сущность стала **managed/persistent** необходимо выполнить **entityManager#persist** или **session#save**. Более подробно можно прочитать в статье Thorben Janssen: **"[What’s the difference between persist, save, merge and update? Which one should you use?](https://thorben-janssen.com/persist-save-merge-saveorupdate-whats-difference-one-use/)"**. Если упростить, то **save** выполняет сохранение сразу всегда.

Жизненный цикл можно отобразить следующим образом:

![](./img/2_Lifecycle.png)

Если сущность уже под управлением JPA Provider'а, то её можно перевести в состояние **detached**. Для этого достаточно вызвать методы **entityManager#detach** или **session#evict** (они равнозначны). Либо при выполнении очистки **clear** или закрытии **close**.

Кроме того, сущности могут быть возвращены в persistence контекст при выполнении методов **entityManager#merge** или **session#update**. Подробнее про разницу можно прочитать в разборе **"[Updating a detached entity](https://thorben-janssen.com/persist-save-merge-saveorupdate-whats-difference-one-use/#Updating_a_detached_entity)"**. Если кратко, то merge подразумевает слияние состояния detached сущности и того, что в контексте. Update же предполагает обновление сущности, которой нет в persistence context, а в случае её наличия там - бросает исключение.

Интересно, что Hibernate предоставляет ещё один метод - **refresh**, который обновляет сущность самым актуальным состояние из базы данных, то есть по сути работает противоположно методу **merge**.

Кроме того, мы можем получить persisted сущность без обращения к БД - при помощи метода **entityManager#load** или **session#getReference**. Данный метод возвращает **proxy**, которое обратиться к БД при обращении НЕ к PK полю. Подробнее можно прочитать в обсуждении **"[When to use EntityManager.find() vs EntityManager.getReference() with JPA](https://stackoverflow.com/questions/1607532/when-to-use-entitymanager-find-vs-entitymanager-getreference-with-jpa)"**. 

Для получение persisted сущностей из базы используются методы **entityManager#find** и **session#get**. В отличии от **load/getReference** данные методы действительно обращаются в БД для получения инициализированной сущности.

Если что-то не должно быть частью сущности, то не сохраняемые (Non persistent) поля помечаются **@Transient**. По умолчанию static и final поля считаются transient и не сохраняются в БД. Подробнее см. **"[Using @Transient in Jpa or Hibernate to ignore fields](https://javabydeveloper.com/transient-annotation-non-persistent-field-using-jpa-hibernate)"**.

Сущность может быть так же удалена из persistence context. См **"[Hibernate Tips: How to remove entities from the persistence context before doing bulk operations](https://thorben-janssen.com/hibernate-tips-remove-entities-persistence-context/)"**.

------------

## [↑](#Home) <a name="cache"></a> Second Level Cache

Persistence Context часто называют кэшем первого уровня. Существует возможность подключить ещё и кэш второго уровня.\
Более подробно про это в материале **"[Baeldung: Hibernate Second-Level Cache](https://www.baeldung.com/hibernate-second-level-cache)"**. 

Пример описания кэширования для сущности:
```java
@Entity
@Table(name = "ADDRESS")
@Cache(usage=CacheConcurrencyStrategy.READ_ONLY, region="employee")
public class Address {
```
Более подробно можно прочитать в tutorial **"[Hibernate EHCache - Hibernate Second Level Cache](https://www.digitalocean.com/community/tutorials/hibernate-ehcache-hibernate-second-level-cache)"**.

**ВАЖНО** помнить, что коллекции по умолчанию не кешируются и нужно их аннотировать дополнительно.

Кроме того, помимо кэша второго уровня есть ещё и кэш запросов. Про особенности кэша запросов можно прочитать в статье "[How does Hibernate Query Cache work](https://vladmihalcea.com/how-does-hibernate-query-cache-work/)".

------------

## [↑](#Home) <a name="mapping"></a> Mapping
JPA - это про отображение Java объектов на базу данных и наоборот. Это называется **Object-Relational Mapping**.

Во-первых, классы которые являются сущностями (т.е. аннотированы @Entity) отображаются на таблицы в БД. Без дополнительных указаний JPA Provider (например, Hibernate) отобразит сущность на таблицу с таким же названием. Но этим можно управлять, указав аннотацию **@Table**:
```java
@Entity
@Table(name = "ACCOUNT")
public class Account {
```

Говоря про маппинг таблиц стоит не забывать про то, что в Java у нас есть наследование. А следовательно, его надо как-то отображать на мир баз данных. Проблемы здесь добавляет то, что в мире реляционных баз данных нет понятия наследования. Поэтому, могут быть разные способы (стратегии), при помощи которых можно отобразить наследования. Для этого есть аннотация **@Inheritance**.

По умолчанию, если ничего не указано, то для Entity будет указана одноимённая таблица. Что будет совпадать со следующим:
```java
@Entity
@Table(name = "ACCOUNT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Account {
```

Более подробно про стратегии наследования можно прочитать здесь:
- [Inheritance Strategies with JPA and Hibernate – The Complete Guide](https://thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/)
- [Mapping class inheritance in Hibernate 5](https://marcin-chwedczuk.github.io/mapping-inheritance-in-hibernate)

Если мы смогли соотнести таблицу с сущностью, то далее нужно соотнести поля сущности и столбцы, то есть колонки. По умолчанию JPA Provider будет соотносить поля с такими же по названию колонками, но на это можно повлиять аннотацией **@Column**:
```java
@Column(name = "ACC_NAME")
private String name;
```

Интересно, что аннотация @Column имеет возможность указать различные ограничения. Но стоит помнить, что эти ограчения будут работать только тогда, когда по аннотациям JPA Provider будет создавать структуру БД. Поэтому лучше воспользоваться реализацией **BeanValidation specification (JSR 303)**. Для этого можно воспользоваться реализацией этой спецификации. Например: **hibernate-validator**. Однако, аннотация @Column имеет полезные свойства, вроде insertable и updatable.

Подробнее можно прочитать здесь:
- [Hibernate Tips: What’s the difference between @Column(nullable = false) and @NotNull](https://thoughts-on-java.org/hibernate-tips-whats-the-difference-between-column-nullable-false-and-notnull/)
- [Difference Between @NotNull, @NotEmpty, and @NotBlank Constraints in Bean Validation](https://www.baeldung.com/java-bean-validation-not-null-empty-blank)

По умолчанию JPA Provider (например, Hibernate) умеет правильно выполнять "маппинг" различных Java типов на типы в БД. Но есть некоторые типы, которые не так очевидны.

Например, **Enum** в Java можно по-разному соотнести с колонками в БД. По умолчанию, Hibernate сохраняет Enum в БД как число, соответствующее **ordinal value**. На это можно повлиять при помощи аннотации **@Enumerated**.
Подробнее описано в материалах c **THOUGHTS ON JAVA**:
- [Hibernate Tips: How to map an Enum to a database column](https://thoughts-on-java.org/hibernate-tips-map-enum-database-column/)
- [Enum Mappings with Hibernate – The Complete Guide](https://thoughts-on-java.org/hibernate-enum-mappings/)
- [The best way to map an Enum Type with JPA and Hibernate](https://vladmihalcea.com/the-best-way-to-map-an-enum-type-with-jpa-and-hibernate/)

Другим специфическим типом данных являются даты. Связано это с тем, что дату можно представить как дату (DATE), время (TIME) или вместе (TIMESTAMP). В JPA для уточнения этой информации есть аннотацией **@Temporal**. Данная аннотация применима только к Java типам **java.util.Date** и **java.util.Calendar**.
Типы из **Date and Time API** "мапятся" без **@Temporal**.
Подробнее можно прочитать здесь:
- [Date and Time Mappings with Hibernate and JPA](https://thoughts-on-java.org/hibernate-jpa-date-and-time/)
- [How To Map The Date And Time API with JPA 2.2](https://thoughts-on-java.org/map-date-time-api-jpa-2-2/)
- [How to persist LocalDate and LocalDateTime with JPA 2.1](https://thoughts-on-java.org/persist-localdate-localdatetime-jpa/)

------------

## [↑](#Home) <a name="associations"></a> Associations
**Object-Relational Mapping** описывает связи одних сущностей с другими сущностями.

Каждая связь представлена двумя сторонами:
- **Owner**
Это сторона связи, на которой определён **Foreign Key**. То есть эта та сторона, которая "владеет" ссылкой на другую сторону ассоциации.
- **Reference side**
Это сторона связи, на котору ссылаются. Она **НЕ** содержит FK на другую часть ассоциации.

Таблицы в БД имеют как столбцы с **Primary Key** (т.е. столбец по которому можно однозначно идентифицировать запись), например ID. И есть столбцы с **Foreign Key**, то есть столбцы, которые содержат идентификатор записи из другой таблицы, с которой связана та или иная запись данной таблицы. Например, в таблице Course столбцом с Foreign Key может служить столбец professor_id.

Подробнее про описание связей таблиц см. **"[Map Associations with JPA and Hibernate – The Ultimate Guide](https://thorben-janssen.com/ultimate-guide-association-mappings-jpa-hibernate/)"**.


### [↑](#Home) <a name="onetoone"></a> **One-to-One**
Самая простая и мало используемая ассоциация - один к одному или **One-to-One**.
Однако на ней проще всего понять основную идею ассоциаций.

Во первых, если у нас есть аккаунт, то нужно создать для него связь. Например, со студентом. Добавим новую сущность студента:
```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "stud_gen")
    @SequenceGenerator(name = "stud_gen",sequenceName = "stud_seq")
    private Long id;

    private String name;

    private Account account;
}
```

**One-to-One** позволяет сказать, что One Student относится только к One Account:
```java
@OneToOne
private Account account;
```
Таким образом мы установили **One directional** связь (т.е. одностороннюю), при которой только Student знает про связанный с ним аккаунт. Кроме того, Student является Owner'ом, т.к. в таблице сущности Student содержится Foreign Key (FK), т.е. данные о том, какой ID аккаунта с каким студентом связан. Если на стороне owner'а необходимо изменить название столбца, необходимо использовать аннотацию **@JoinColumn**.

Аналогичную связь можно сделать и на другой стороне (т.е. на стороне Account):
```java
@OneToOne
private Student student;
```

Чтобы связь стала двунаправленной (т.е. **Bidirectional**) необходимо указать особый аттрибут - **mappedBy**.\
JavaDoc метода говорит, что указывается ``The field that owns the relationship``, т.е. данное поле указывает поле, которое "владеет" связью.

Например, таблица 1 (студенты) ссылается на таблицу 2 (аккаунты). Ссылка в таблице 1 хранится в столбце. А таблица 2 не содержит ссылку.
Таким образом, на стороне где **НЕТ ССЫЛОК** нужно сказать, как ссылку **ВЫЧИСЛИТЬ**:

![](./img/3_MappedBy.png)

То есть таблица STUDENTS содержит столбец со ссылкой на ACCOUNT. А вот таблица ACCOUNT не имеет ссылок на STUDENTS. Поэтому, в сущности Students ничего дополнительно указывать не надо, там есть столбец. А вот в Account поле студента есть, но в реальности в БД такого поля нет. Поэтому мы и говорим Hibernate, что поле students надо определить по маппингу из Student по полю account, которое есть в каждом Student. Или проще говоря: Ищи по FK из поля account в сущности Student.

Поэтому, указываем ассоциацию:
```java
@OneToOne(mappedBy = "account")
private Student student;
```

Кроме этого, для любой Bidirectional (двусторонней) связи нужен метод, который будет эту ассоциацию указывать с двух сторон. Сам Hibernate это не делает.
Например, добавим метод в сущность Account:
```java
public void setAccount(Account acc) {
	this.account = acc;
	acc.setStudent(this);
}
```
Подробнее про управление Bidirectional связью:
- [How to synchronize bidirectional entity associations with JPA and Hibernate](https://vladmihalcea.com/jpa-hibernate-synchronize-bidirectional-entity-associations/)
- [Hibernate Tips: Easiest way to manage bi-directional associations](https://thoughts-on-java.org/hibernate-tips-easiest-way-to-manage-bi-directional-associations/)

Кроме того, **One-To-One** обладает ещё одной удобной особенностью. Данная ассоциация предлагает возможно использовать один Id для нескольких сущностей. Так называемый **Shared Primary Key**. Для этого используется аннотация **@MapsId**.
Для примера, можно объяснить MapsId на этом примере:

![](./img/4_MapsId.png)

Вспоминая mappedBy, мы понимаем, что mappedBy находится на той стороне, где нет FK в базе данных, но поле в сущности есть. То есть в базе нет ссылки на post_id.
@MapsId работает в обратную сторону. Он ставится там, где есть FK. Он как бы говорит, что "используй FK чтобы получить PK". Таким образом, Post Details, по FK найдёт, post. Найдёт там ID и использует его как свой собственный ID.
Подробнее можно прочитать в статье **"[The best way to map a @OneToOne relationship with JPA and Hibernate](https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/)"**.

Кроме этого, в примере видна одна из общих для всех ассоциаций черт - **Fetch Type**.
**Fetch Type** - тип получения зависимых сущностей. Может быть два разных типа:
- **FetchType.EAGER** - принудительная загрузка всех зависимых сущностей
- **FetchType.LAZY** - загрузка только по требованию

Подробнее можно прочитать в материале: **"[Entity Mappings: Introduction to JPA FetchTypes](https://thoughts-on-java.org/entity-mappings-introduction-jpa-fetchtypes/)"**

И ещё одно интересное свойство - **cascade**. То есть как водопад идёт каскадами по нижестоящим ступенькам, так и операции каскадом в Hibernate спускаются от родительских сущностей к дочерним.
Например, мы можем сделать так, что если сохраняется Account, то сохраняется и зависимый студент:
```java
@OneToOne(mappedBy = "account", cascade = CascadeType.PERSIST)
private Student student;
```
Подробнее можно прочитать здесь:
- [Hibernate Tips: How to cascade a persist operation to child entities](https://thoughts-on-java.org/hibernate-tips-cascade-persist-operation-child-entities/)
- [A beginner’s guide to JPA and Hibernate Cascade Types](https://vladmihalcea.com/a-beginners-guide-to-jpa-and-hibernate-cascade-types/)

Кроме того, остаётся последний атрибут, который не менее интересен.
**Orphan** - это английское слово, которое переводится как "сирота". В Hibernate же есть параметр **orphanRemoval**.
Подробнее про него можно прочитать здесь: "[Hibernate Tips: How to delete child entities from a many-to-one association](https://thoughts-on-java.org/hibernate-tips-how-to-delete-child-entities/)". А так же можно прочитать обсуждение здесь: "[What is the difference between cascade and orphan removal from DB?](https://stackoverflow.com/questions/18813341/what-is-the-difference-between-cascade-and-orphan-removal-from-db/18813411)".


### [↑](#Home) <a name="onetomany"></a> **One-to-Many** & **Many-to-One**
Пожалуй, самые распространённые ассоциации - **One-to-Many** и **Many-to-One**.

Указание ассоциации начинается с описания текущей сущности и заканчивается описанием сущности, на которую мы ссылаемся. Например:
```java
@OneToMany
List<Student> students = new ArrayList<>();
```
То есть по сути это описание: Курс One, а студентов Many.

Если мы хотим иметь возможность получить не только всех студентов курса, но и курс, назначенный студенту, то следует добавить аналогичное и с другой стороны ассоциации, то есть на стороне сущности Student:
```java
@ManyToOne
private Course course;
```
То есть описание вида: Many студентов на One курс.

Таким образом у нас появились однонаправленные (т.е. **Unidirectional**) связи на каждой стороне. Такие связи несут риск неэффективной работы Hibernate. Подробнее о причинах можно прочитать здесь: **"[Hibernate Tips: Map an Unidirectional One-to-Many Association Without a Junction Table](https://thoughts-on-java.org/hibernate-tips-unidirectional-one-to-many-association-without-junction-table/)"**.

Чтобы связь стала двунаправленной (т.е. **Bidirectional**) необходимо указать особый аттрибут - **mappedBy**. Данный атрибут указывает поле, которое "владеет" связью. Как и в случае One-to-One указывается там, где нет FK, но связь нужно как-то описать. Логично, что в случае сущности Student данная сущность представляет таблицу STUDENT, в которой есть столбец COURSE, который содержит ссылку (то есть FK) на курс.
А вот 1 курс содержит коллекцию студентов. Соответственно, FK нет. Значит тут и указываем аттрибут mappedBy:
```java
@OneToMany(mappedBy = "course")
private List< Student> students = new ArrayList<>();
```

При Biderectional связях крайне важно указывать **mappedBy** чтобы недопускать лишних действий (т.е. лишних запросов в БД) со стороны Hibernate, т.к. это скорей всего негативно скажется на производительности приложения.

Обычно, One-to-many является referencing стороной в bidirectional связи. Однако, иногда можно встретить его как самостоятельную unidirectional связь. В этом случае необходимо указать аннотацией JoinColumn где находится FK столбец, иначе Hibernate будет использовать отдельную таблицу для связи сущностей.

Стоит помнить, что bidirectional связи не обслуживаются автоматически и для их обслуживания необходимо создать метод вручную.

По теме Many-to-One и One-to-many есть отличные материалы:
- [Hibernate Tip: How to Map a Bidirectional Many-to-One Association](https://www.youtube.com/watch?v=cI4jYr_iv3Y)
- [Best Practices for Many-To-One and One-To-Many Association Mappings](https://www.youtube.com/watch?v=tciSOIQngig)
- [Ultimate Guide – Association Mappings with JPA and Hibernate](https://thoughts-on-java.org/ultimate-guide-association-mappings-jpa-hibernate/)
- [How to synchronize bidirectional entity associations with JPA and Hibernate](https://vladmihalcea.com/jpa-hibernate-synchronize-bidirectional-entity-associations/)
- [How do Set and List collections behave with JPA and Hibernate](https://vladmihalcea.com/hibernate-facts-favoring-sets-vs-bags/)

Стоит помнить, что сохраняя сущность с коллекцией других сущностей, по умолчанию мы не будем сохранять сущности из этой коллекции. По умолчанию, ни одно действие с сущностью не распространяется на его дочерние сущности. Но это можно организовать при помощи настройки **cascade**.
Подробнее про эту возможность можно прочитать здесь:
- [Hibernate Tips: How to cascade a persist operation to child entities](https://thoughts-on-java.org/hibernate-tips-cascade-persist-operation-child-entities/)
- [Why you should avoid CascadeType.REMOVE for to-many associations and what to do instead](https://thoughts-on-java.org/avoid-cascadetype-delete-many-assocations/)


### [↑](#Home) <a name="manytomany"></a> **Many-to-Many**
В базе данных **Many-to-many** может выглядеть следующим образом:

![](./img/5_ManyToMany.png)

Данная связь похожа на расширенную версию **One-to-Many**. Каждая сторона имеет аннотацию не **One-to-Many**, а **Many-to-Many**.
Кроме того, рекомендуется хранить коллекцию типа Set. Аналогично One-To-Many нам так же понадобится вспомогательный метод обновления обеих сторон для добавления новой сущности в коллекцию.
Для Bidirectional связи аттрибут mappedBy можно выставить на любой стороне.

Кроме этого, мы можем указать дополнительные настройки:

![](./img/6_JoinTable.png)

Подробнее можно прочитать в статье:
- [Best Practices for Many-to-Many Associations with Hibernate and JPA](https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/)
- [Hibernate Tip: Many-to-Many Association with additional Attributes](https://thoughts-on-java.org/hibernate-tip-many-to-many-association-with-additional-attributes/)
- [The best way to use the @ManyToMany annotation with JPA and Hibernate](https://vladmihalcea.com/the-best-way-to-use-the-manytomany-annotation-with-jpa-and-hibernate/)

------------

## [↑](#Home) <a name="lazy"></a> **Lazy**
Hibernate предоставляет возможность "ленивой" загрузки сущностей. Это означает, что вместо настоящей коллекции Hibernate инициализирует прокси. Этот прокси будет ожидать обращения к себе и на такое обращение выполнит обращение к БД за получением данных.

На эту тему есть статья от Vlad Mihalcea: **"[JPA Default Fetch Plan](https://vladmihalcea.com/jpa-default-fetch-plan/)"**.

По умолчанию, связи @OneToMany and @ManyToMany используют FetchType.LAZY, чтобы не подгружать сразу коллекции.

Стоит учитывать этот факт, т.к. это может порождать "[N+1 проблему](https://vladmihalcea.com/n-plus-1-query-problem)". При итерировании по коллекции сущностей и обращению к lazy полям мы получим N запросов на каждую итерацию. А это может быть очень затратно.

------------

### [↑](#Home) <a name="jpql"></a> **JPQL**
**JPA** - это не только про автоматический маппинг. Но это ещё и про запросы.

JPA предоставляет свой язык запросов - Java Persistence Query Language, JPQL.\
Это своего рода симбиоз JPA и SQL. Подробнее см. **"[JPQL – How to Define Queries in JPA and Hibernate](https://thorben-janssen.com/jpql/)"**.

Кроме этого JPA позволяет выполнять SQL Query, они называются **Native Query**.\
Подробнее см **"[Native Queries – How to call native SQL queries with JPA & Hibernate](https://thoughts-on-java.org/jpa-native-queries/)"**.

Кроме того будет полезно прочитать про механизм **AUTOFLUSH**: **"[How does AUTO flush strategy work in JPA and Hibernate](https://vladmihalcea.com/how-does-the-auto-flush-work-in-jpa-and-hibernate/)"**.

Например, в тест сохранения сущности можем добавить проверку существования сущности через JPQL:
```java
// JPQL
TypedQuery<Account> studentQuery;
studentQuery = em.createQuery("SELECT a FROM Account a", Account.class);
List<Account> resultList = studentQuery.getResultList();
Assert.assertEquals(1, resultList.size());
```

Кроме того, JPQL позволяет использовать различные JOIN'ы, о чём подробнее можно прочитать в обзоре: [Hibernate Tips: What’s the Difference between JOIN, LEFT JOIN and JOIN FETCH](https://thoughts-on-java.org/hibernate-tips-difference-join-left-join-fetch-join/).

Есть отличный материал на тему JPQL:
- [How to Use Named Queries with Spring Data JPA](https://thorben-janssen.com/spring-data-jpa-named-queries#Defining_a_Named_JPL_Query)
- [Ultimate Guide to JPQL Queries with JPA and Hibernate](https://thoughts-on-java.org/jpql/)
- [Using the Optimal Query Approach and Projection for JPA and Hibernate](https://thoughts-on-java.org/optimal-query-and-projection-jpa-hibernate/)
- [Hibernate Tips: How to downcast entities in JPQL queries](https://thoughts-on-java.org/hibernate-tips-downcast-entities-jpql-queries/)
- [Hibernate Tips: How to use pagination with JPQL](https://thoughts-on-java.org/hibernate-tips-use-pagination-jpql/)

------------

## [↑](#Home) <a name="application"></a> Java Application
Наша цель - создать Java приложение, которое будет использовать Hibernate. Собирать это всё вручную каждый раз неудобно и долго. Мир для этого давно используется автоматические системы сборки проектов, такие как **"[Maven](https://maven.apache.org/)"** и **"[Gradle](https://gradle.org/)"**.

**Gradle** позволяет выполнять различные действия над проектами благодаря своим плагинам. Часть из них устанавливается вместе с Gradle. Эта группа плагинов называется **"[Gradle core plugins](https://docs.gradle.org/current/userguide/plugin_reference.html)"**.
Именно в эту группу входит нужный нам плагин - **"[Gradle Build init plugin](https://docs.gradle.org/current/userguide/build_init_plugin.html)"**.

Плагин **"Build init"** добавляет gradle task, выполнив который мы инициализируем (**initialize**) проект. Название у него соответствующее: **init**.

**Создадим** [Java Application](https://docs.gradle.org/current/userguide/build_init_plugin.html#sec:java_application) из командной строки (Для ОС Windows: win + R, cmd):
> gradle init --type java-application

**[Build Script](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html)** - это то место, для используя систему сборки мы должны описывать все нюансы сборки проекта.
В данном файле описываются основные моменты: компиляций, подготовка артефактов, настройки. И самое главное - какие другие **jar** архивы с библиотеками/фрэймворками мы собираемся использовать. Т.к. наш проект начинает от них зависеть, то такие **jar** становятся зависимостями, т.е. **dependencies**:
```
dependencies {
    implementation 'javax.persistence:javax.persistence-api:2.2'
    implementation 'org.hibernate:hibernate-core:5.4.8.Final'
    testImplementation 'junit:junit:4.12'
}
```
Таким образом мы подключили использование JPA API (версии 2.2), а так же её реализацию - Hibernate.\
Кроме этого, мы собираемся использовать JUnit - библиотеку для написания юнит тестов.

Нам понадобятся геттеры и сеттеры для полей + конструкторы.\
Избавимся от этого **boilerplate code** при помощи **[Lombok](https://projectlombok.org/setup/gradle)**:
```
compileOnly 'org.projectlombok:lombok:1.18.10'
annotationProcessor 'org.projectlombok:lombok:1.18.10'
```

Данная библиотека позволяет аннотациями указать, что мы хотим добавить. А самим добавлением будет заниматься Lombok в момент компиляции кода.
Для этого используется так называемый **Annotation Processor**. То есть перед тем, как будет получен финальный **.class** файл с байт-кодом Java процессор аннотаций добавит нужные нам вещи (например, геттеры и сеттеры).

Настроим IDE, т.к. по умолчанию IDE не знает про то, что надо использовать процессоры аннотаций. Для IntelliJ IDEA нажимаем **CTRL+ALT+S** и в поиске находим **annotation**:

![](./img/AnnotationProcessor.png)


### [↑](#Home) <a name="logging"></a> Логирование
Настроим логирование. Для логирования воспользуемся библиотека **[log4j](https://logging.apache.org/log4j/2.x/maven-artifacts.html)**.\
Согласно документации log4j добавим две новые зависимости:
```
implementation 'org.apache.logging.log4j:log4j-api:2.12.1'
implementation 'org.apache.logging.log4j:log4j-core:2.12.1'
```

Далее остаётся настроить log4j, например при помощи [Configuration with XML](https://logging.apache.org/log4j/2.x/manual/configuration.html#XML).\
Для этого создадим файл **log4j2.xml** в каталоге **src/main/resources**.

По примеру **"[Hibernate 5 + Log4j 2 configuration example](https://www.boraji.com/hibernate-5-log4j-2-configuration-example)"** напишем:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
	<Appenders>
		<!-- Console Appender -->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MMM-dd HH:mm:ss a} [%t] %-5level %logger{36} - %msg%n" />
        </Console>
    </Appenders>
    <Loggers>
        <!-- Log everything in hibernate -->
        <Logger name="org.hibernate" level="info" additivity="false">
            <AppenderRef ref="Console" />
        </Logger>
        <!-- Log SQL statements -->
        <Logger name="org.hibernate.SQL" level="debug" additivity="false">
            <AppenderRef ref="Console" />
        </Logger>
        <!-- Log JDBC bind parameters -->
        <Logger name="org.hibernate.type.descriptor.sql" level="trace" additivity="false">
            <AppenderRef ref="Console" />
        </Logger>
        <Root level="error">
            <AppenderRef ref="Console" />
        </Root>
    </Loggers>
</Configuration>
```
Теперь логирование будет идти через **log4j**.
Если мы снова выполним тест, то мы увидим, что всё логирование идёт на консоль в указанном нами формате.

Подробнее про логирование:
**"[Hibernate Logging Guide – Use the right config for development and production](https://thoughts-on-java.org/hibernate-logging-guide/)"**.


### [↑](#Home) <a name="persistence"></a> Persistence Unit (persistence.xml)
Настроим Persistence Unit - единицу работы с Data Source.

**Persistence Unit** в терминах JPA - это самодостаточная единица, с определёнными настройками, со знанием того как и куда сохранять. Persistence Unit - это своего рода некая область, которая объединяет управляемые JPA провайдером классы и их настройки.
Иногда можно встретить такой перевод, как "Единицы постоянства" =)

**persistence.xml** - это файл, в котором описываются доступные Persistent Unit'ы.
Как сказано в главе **8.2.1 persistence.xml file** спецификации JPA:
> The persistence.xml file is located in the META-INF directory

То есть persistence.xml должен быть в подкаталоге **META-INF**, который должен быть на classpath нашего Java приложения.\
Так где должен тогда быть **META-INF**?

У каждой системы сборки есть свой **"project layout"**.
У Gradle благодаря плагину "java" есть свой [Java Project Layout](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_project_layout).
Т.к. каталог **src/main/resources**, согласно **project layout**, находится на **classpath**, то подкаталог **META-INF** следует создать там.

Создадим в данном подкаталоге файл **persistence.xml**.
Заполним его по образу того, как это указано в спецификации JPA в разделе **8.3 persistence.xml Schema**, а описание Persistence Unit можно взять из раздела **8.2.1 persistence.xml file**:
```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
 http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">
    <persistence-unit name="SimpleUnit">
        <description>Simple Persistence Unit</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    </persistence-unit>
</persistence>
```

Теперь нужно подключить наш Persistence Unit к БД.\
Для этого добавим в наш проект зависимость от базы данных **[H2 Database](http://www.h2database.com/html/cheatSheet.html)**:
```
implementation 'com.h2database:h2:1.4.200'
```

Осталось только описать подключение для Persistence Unit (после атрибута provider) в persistence.xml:
```
<properties>
	<property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
	<property name="javax.persistence.jdbc.url" value="jdbc:h2:mem:test"/>
	<property name="javax.persistence.jdbc.user" value="sa"/>
	<property name="javax.persistence.jdbc.password" value="sa"/>
	<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
    <property name="javax.persistence.schema-generation.database.action" value="create"/>
</properties>
```

Для тестирования мы указали **javax.persistence.schema-generation.database.action**, чтобы JPA Provider сам создавал базу данных по JPA аннотациям. В настоящих проектах это считается плохой практикой. Но для учебных и тестовых проектов вроде нашего это допустимо и упрощает жизнь.

Подробнее про persistence.xml:
**"[THOUGHTS ON JAVA: A Beginner’s Guide to JPA’s persistence.xml](https://thoughts-on-java.org/jpa-persistence-xml)"**


### [↑](#Home) <a name="domainobject"></a> Domain object
Необходимо описать доменную модель приложения.

В **[JPA Specification](https://github.com/javaee/jpa-spec/blob/master/jsr338-MR/JavaPersistence.pdf)** в главе **"Chapter 1 Introduction"** сказано, что есть такое понятие, как **domain model** (доменная модель). Она в свою очередь состоит из **domain object**, т.е. доменных объектов или объектов доменной области. Доменную область ещё называют "предметной областью".

Каждая программа под собой имеет некоторую предметную область, проблемы которой она решает. Если это машины, то программа будет работать с такими понятиями предметной область, как "машина", "дорога", "светофор" и т.д. Если программа написана для организации учёта учеников, преподавателей и курсов, то понятиями предметной области будут "студент", "учитель" и т.д.

Создадим отдельный пакет **model** и создадим там Java класс, представляющий domain object для "Аккаунт студента":
```java
public class Account {
    private String name;
}
```

Теперь нужно из обычного класса сделать сущность, т.е. Entity. Адаптируем наш код для Account в соответствии с требованиями спецификации JPA:
```java
@Data // Добавит геттер + сеттер
@NoArgsConstructor // Для JPA
@Entity
public class Account {
    @Id
    private Long id;

    private String name;
}
```


### [↑](#Home) <a name="test"></a> Unit Test
Чтобы проверить, что оно всё работает, напишем простые юнит тесты.

Воспользуемся рекомендацией от Vlad Mihalcea из его статьи **"[JPA test case templates](https://in.relation.to/2016/01/14/hibernate-jpa-test-case-template/)"**. Добавим в тест два метода, один из которых выполняется ДО каждого теста, а другой ПОСЛЕ каждого теста:
```java
public class AppTest {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager em;

    @Before
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("SimpleUnit");
        em = entityManagerFactory.createEntityManager();
    }

    @After
    public void destroy() {
        em.close();
        entityManagerFactory.close();
    }
```

Напишем наш тест проверки сохранения сущности в БД:
```java
@Test
public void shouldPersistEntity() {
	Account acc = new Account();
	acc.setId(1L);
	acc.setName("ADMIN");
	em.getTransaction().begin();
	em.persist(acc);
	em.getTransaction().commit();
}
```

Но если мы сейчас выполним тест, то получим ошибку про неизвестную сущность Account:
> java.lang.IllegalArgumentException: Unknown entity

Стоит обратить внимание на некоторый нюанс поиска сущностей JPA Provider'ом.\
По умолчанию JPA провайдер ищет сущности в том месте, в котором найден persistence.xml (см. главу спецификации **8.2 Persistence Unit Packaging**).
И тут вступают в силу нюансы различных систем сборок и различных артефактов.
Например, случай запуска тестов через Gradle (при помощи gradle test) описан в обсуждении **"[JPA entity classes are not discovered automatically with Gradle](https://discuss.gradle.org/t/jpa-entity-classes-are-not-discovered-automatically-with-gradle/11339/6)"** и решается добавлением в конец gradle build script настройки:
> sourceSets.main.output.resourcesDir = sourceSets.main.output.classesDirs.getSingleFile()

Данное решение поможет выполнять тесты через gradle test. Но не поможет вызывать тесты в самой IntelliJ Idea через контекстное меню методов. Потому что IntelliJ Idea имеет свои правила запуска тестов. Придётся пойти другим путём.
Есть альтернативный способ, можно явно указать классы сущностей в persistence.xml:
```xml
<persistence-unit name="SimpleUnit">
    <description>Simple Persistence Unit</description>
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <class>hibernatebasic.model.Account</class>
```

Теперь при выполнении теста ошибок больше не будет.

Сначала тест создаёт java объект и сохраняет его в Persistence Context при помощи ``em.persist``.\
На этом этапе сохранения в БД ещё не происходит, но сущность становится **persisted** и любые изменения теперь будут ослеживаться.

Для сохранения всех изменений в БД необходимо сделать commit транзакции (своего рода сеанс общения с источником данных):
```java
// Commit transaction
em.getTransaction().begin();
em.getTransaction().commit();
```

При завершении транзакции будет выполнен **FLUSH** всех изменений. При этом у JPA Provider'а будет запущен механизм проверки изменений, называемый **Dirty-check**.
Чтобы увидеть его в логе по умолчанию необходимо добавить новый логгер:
```xml
<!-- Dirty check -->
<Logger name="org.hibernate.event.internal.AbstractFlushingEventListener" level="trace" additivity="false">
	<AppenderRef ref="Console" />
</Logger>
```
Подробнее про данный механизм можно прочитать здесь:
- [The anatomy of Hibernate dirty checking mechanism](https://vladmihalcea.com/the-anatomy-of-hibernate-dirty-checking/)

После каждого теста мы закрываем Entity Manager, то есть завершаем сессию работы с Persistence Context. Это приводит к тому, что все сущности из Persistence Context переходят в статус Detached:
```java
@After
public void destroy() {
	em.close();
```

Кроме того, Entity Manager позволяет выполнять и другие действия.
Чтобы получить снова persisted инстанс мы можем сделать 2 вещи:
- Выполнить поиск сущности по ID:
```java
Professor entity2 = em.find(Professor.class, 1L);
```
- Выполнить мерж изменений:
```java
Professor entity2 = em.merge(entity);
```
Интересно, что в этом случае entity так и останется detached, а entity2 будет persisted.

Так же JPA позволяет удалить сущность при помощи метода **em.remove**.

Про методы работы с entity:
- [JPA vs Hibernate : The difference between save, persist, merge and update](https://www.youtube.com/watch?v=SH29O-bcQlc&t=453s)
- [How do find and getReference EntityManager methods work when using JPA and Hibernate](https://vladmihalcea.com/entitymanager-find-getreference-jpa/)