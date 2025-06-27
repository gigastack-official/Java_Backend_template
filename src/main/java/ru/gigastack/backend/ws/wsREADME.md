WebSocket подсистема: внутренний README

Этот файл описывает как пользоваться и расширять WebSocket-слой бэкенда (Spring Boot 3.5) изнутри проекта. Он предназначен для разработчиков, а не для внешних клиентов.

⸻

1. Быстрый старт
    1.	Запусти Postgres (порт, база, пароль — см. application-dev.yml).
    2.	Выполни ./gradlew bootRun -Pprofiles=dev.
    3.	Подключись к сокету:

websocat "ws://localhost:8080/ws?token=<JWT>"

JWT можно сгенерировать через /auth/login.

Если соединение установлено, сервер сразу отправит системное сообщение {"type":"SYSTEM","payload":"CONNECTED"}.

⸻

2. Архитектура на уровне кода

ws/
├─ config/           →  Handshake + CORS + STOMP-free routing
├─ gateway/          →  WebSocketGateway (точка входа)
├─ session/          →  SessionRegistry (все активные сессии)
├─ handler/
│   ├─ BaseHandler   →  💡 абстрактный класс-шаблон
│   └─ *Handler      →  по одному на каждый тип сообщения
├─ model/
│   ├─ WsRequest     →  общий DTO входящих пакетов
│   └─ WsResponse    →  общий DTO исходящих пакетов
└─ service/
└─ BroadcastingService →  вспомогательный фасад для push-событий

Ключевые идеи
•	OOP — любой новый тип сообщения = новый класс-обработчик, реализующий MessageHandler<T>.
•	Автодетекция — WebSocketGateway на старте сканирует контекст, строит мапу type → handler.
•	Single Responsibility — вся бизнес-логика остаётся в сервисах, обработчик лишь трансформирует вход ↔ выход.

⸻

3. Жизненный цикл сессии
    1.	Handshake — Custom HandshakeInterceptor извлекает JWT, кладёт UserPrincipal в WebSocketSession.
    2.	Register — SessionRegistry добавляет сессию в карту userId → Set<WebSocketSession>.
    3.	In — При получении фрейма Gateway:
    1.	десериализует в WsRequest;
    2.	находит обработчик по type;
    3.	передаёт payload в бизнес-слой.
    4.	Out — Ответ сериализуется в JSON и пишется в исходящую очередь сессии.
    5.	Cleanup — при закрытии соединения Registry убирает сессию.

⸻

4. Добавление нового типа сообщения
    1.	DTO  создай в ws.model.*:

data class Ping(val timestamp: Long)


	2.	Handler:

@Component
class PingHandler(private val timeSvc: TimeService) : MessageHandler<Ping> {
override val type = "PING"
override suspend fun handle(msg: Ping, ctx: WsContext): WsResponse {
return ok("PONG @ ${timeSvc.now()}")
}
}


	3.	Клиент отправляет:

{"type":"PING","payload":{"timestamp":172432423}}

Получает ответ {"type":"PING","payload":"PONG @ …"}.

Советы
•	Имя типа регистру нечувствительно — приводим к UPPER_CASE.
•	Не запускай блокирующие операции в обработчике; используй withContext(Dispatchers.IO).

⸻

5. Серверные push-уведомления

@Service
class CommentService(
private val repo: CommentRepo,
private val broadcaster: BroadcastingService
) {
fun addComment(dto: NewCommentDto) {
val comment = repo.save(dto.toEntity())
broadcaster.broadcastToUser(dto.postAuthorId, NewCommentEvent(comment))
}
}

BroadcastingService умеет:
•	broadcastToUser(userId, event) — всем сессиям юзера;
•	broadcast(event) — всем онлайн-пользователям;
•	dispatch(sessionId, event) — конкретной сессии.

⸻

6. Настройка и параметры

В application.yml (-dev / -prod):

ws:
path: /ws            # URL эндпоинта
allowed-origins: "*" # CORS для dev, ужесточь в prod
buffer-size: 8192    # bytes
idle-timeout: 60s

Изменил — WebSocketConfig подхватит на старте.

⸻

7. Безопасность
   •	Handshake — только с валидным JWT.
   •	При ревоке токена нужно закрыть активные сессии: SessionRegistry.closeAll(userId).
   •	Все ошибки превращаются в WsError и отправляются клиенту: {type:"ERROR", payload:{code, message}}.

⸻

8. Тестирование
   •	Unit — тестируй обработчики отдельно, подставляя фиктивный WsContext.
   •	Integration — используем WebSocketClient из org.springframework.web.socket.client.standard + тестовый контейнер Postgres.
   •	E2E — websocat, wscat или Cypress для фронта.

⸻

9. Распространённые ошибки и их решение
   •	CloseStatus(4401) — неверный JWT → обнови токен.
   •	CloseStatus(1009) — превышен buffer-size → уменьши payload или увеличь параметр.
   •	Handler не найден → убедись, что класс аннотирован @Component и type совпадает.

⸻

10. Расширение на будущее
    •	Поддержка протокола STOMP (если понадобится frame-routing).
    •	Канал для staff-уведомлений → выделить Topic-based broadcasting.
    •	Выделить отдельный metrics-interceptor → метрики в Prometheus по latency.

⸻

11. Полезные сниппеты

# Проверка живости всех сессий (dev-endpoint)
curl -H "Authorization: Bearer <admin_token>" http://localhost:8080/actuator/ws/sessions

# Локальное отключение зависшей сессии
curl -X DELETE "http://localhost:8080/actuator/ws/sessions/{sessionId}" \
-H "Authorization: Bearer <admin_token>"


⸻

Готово! Если появятся вопросы или понадобятся дополнительные примеры — пиши!