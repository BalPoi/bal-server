# Гарантии доставки (Delivery Semantics)

# /!\ Эти гарантии доставки относятся именно к ОБРАБОТКЕ сообщений в консюмере

## 1. At-Most-Once (Максимум один раз)(<=1)
Сообщение может быть потеряно, но никогда не обрабатывается повторно  
Когда использовать: Метрики, логи, данные которые не критично потерять

```yaml
spring:
  kafka:
    producer:
      acks: 0 #❌ Не ждем подтверждения
      retries: 0 #❌ Без повторных отправок
      properties:
        enable.idempotence: false #❌ Отключаем ненужную идемпотентность для оптимизации [Default: true]
    consumer:
      enable-auto-commit: true #✅ Автокоммит
```
Тут гарантия обработки At-Most-Once за счёт следующего:  
- у продюсера нет возможности отправить как-либо дубль в брокер  
- а в консюмере сообщение комитится сразу при получении и не важно будет далее при обработке исклчение или нет

## 2. At-Least-Once (Минимум один раз)(>=1)
Сообщение никогда не теряется, но может быть обработано повторно  
Когда использовать: Финансовые транзакции, заказы, любые данные где потеря недопустима

```yaml
spring:
  kafka:
    producer:
      acks: all #✅ Ждем подтверждения от всех реплик
      retries: 3 #✅ Повторные отправки при ошибках
      properties:
        enable.idempotence: true #✅ Включаем идеимпотентность во избежание дублей в брокере
    consumer:
      enable-auto-commit: false
    listener:
      ack-mode: manual_immediate #✅ Ручной коммит
```
Тут гарантия обработки At-Least-Once за счёт следующего:  
- продюсер не породит дубль, т.к. он идемпотентный + ретраями добивается получения оброкером сообщения  
/!\ Единственные кейсы, когда дубли возможны это когда:  
  - продюсер упал в окно между отправкой и получением ответа от брокера и, к примеру, этот продюсер вызывается консюмером и из-за падения оффсет не закомитился и обработка пошла второй раз
  - то же самое, но со стороны Kafka брокера, т.к. брокер хранит инфу для идемпотентных продюсеров в памяти. И если упадёт брокер, он её теряет и продюсер при ретрае отправит то же сообщение и брокер примет его  
  Взято из [Андрей Серебрянский — Exactly once в Kafka: как все сломать (и починить)](https://www.youtube.com/watch?v=8Ef2V9UacfU)
- а в консюмере сообщение комитится только в конце обработки, при которой может произойти исключение 
и запись не закомитится и консюмер будет обрабатывать это сообщение снова  
/!\ Важно помнить что KafkaListenerErrorHandler и CommonErrorHandler могут закомитить оффсет при обработке исключения!
```
@KafkaListener(topics = "orders")
public void listen(Order order, Acknowledgment ack) {
    try {
        // ✅ Обработка сообщения
        processOrder(order);
        // ✅ Коммитим только после успешной обработки
        ack.acknowledge();
    } catch (Exception e) {
        // ❌ Не коммитим - сообщение будет обработано повторно
        log.error("Failed to process order", e);
    }
}
```

## 3. Exactly-Once (Ровно один раз)(=1)
Сообщение обрабатывается ровно один раз без потерь и дублей  
Когда использовать: Критически важные операции (банковские транзакции, биллинг)  
У Kafka Connect хорошая поддержка Exactly-Once см. [Андрей Серебрянский — Exactly once в Kafka: как все сломать (и починить)](https://www.youtube.com/watch?v=8Ef2V9UacfU)  

Тут есть два подхода:  
- ### Идемпотентный консюмер (рекомендуется):  
Используем конфигурацию At-Least-Once и в коде консюмера выполняем проверку, что это сообщение ещё не обрабатывалось.  
Выполняем обработку сообщения в БД транзакции, чтобы не осталось промежуточное состояние при падении до комита оффсета.  
(!) БД транзакции в 2-10 раз производительней чем транзакции Kafka

- ### Kafka Transactions


```java
@Component
public class IdempotentOrderProcessor {
    @Transactional
    @KafkaListener(topics = "orders")
    public void process(Order order, Acknowledgment ack,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        // ✅ Проверяем, не обрабатывали ли уже это сообщение
        if (orderService.isAlreadyProcessed(key)) {
            ack.acknowledge();  // Просто коммитим и пропускаем
            return;
        }

        // Обработка
        orderService.process(order);

        // Сохраняем факт обработки
        orderService.markAsProcessed(key); // Тут может прозойти ошибка, поэтому выполняем в @Transactional

        ack.acknowledge();
    }
}
```

```yaml
spring:
  kafka:
    producer:
      acks: all
      retries: 3
      transaction-id-prefix: tx-producer- #✅ Используем транзакции /!\ У каждой поды сервиса эта пропертя должа быть своя!
    consumer:
      enable-auto-commit: false
      isolation.level: read_committed
    listener:
      ack-mode: manual_immediate #✅ Ручной коммит
```
```java
@Bean
public KafkaTransactionManager<String, String> kafkaTransactionManager() {
    return new KafkaTransactionManager<>(producerFactory());
}
```
```java
@Service
public class OrderService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional("kafkaTransactionManager")
    public void processOrderTransactionally(Order order) {
        // ✅ Сохраняем в БД
        orderRepository.save(order);
        
        // ✅ Отправляем в Kafka - в одной транзакции
        kafkaTemplate.send("order-events", order.getId(), order.toJson());
        
        // ✅ Если что-то пойдет не так - откатятся и БД и Kafka
    }
}
```

# В общем Exactly-Once очень глубокая тема и нужно либо что-то мутить, чтобы полный Exactly-Once был, либо использовать Kafka Streams или Apache Flink
