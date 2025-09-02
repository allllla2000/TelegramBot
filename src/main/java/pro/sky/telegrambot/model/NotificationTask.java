package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "notification_task")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "notify_at", nullable = false)
    private LocalDateTime notifyAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected NotificationTask() {}

    public NotificationTask(Long chatId, String message, LocalDateTime notifyAt) {
        this.chatId = chatId;
        this.message = message;
        this.notifyAt = notifyAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (status == null) status = TaskStatus.PENDING;
    }

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public String getMessage() { return message; }
    public LocalDateTime getNotifyAt() { return notifyAt; }
    public TaskStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setMessage(String message) { this.message = message; }
    public void setNotifyAt(LocalDateTime notifyAt) { this.notifyAt = notifyAt; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
//
