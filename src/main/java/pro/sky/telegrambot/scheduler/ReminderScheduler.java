package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.model.TaskStatus;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final NotificationTaskRepository repo;
    private final TelegramBot bot;

    public ReminderScheduler(NotificationTaskRepository repo, TelegramBot bot) {
        this.repo = repo;
        this.bot = bot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void pickDueTasks() {
        LocalDateTime nowMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> due = repo.findByStatusAndNotifyAt(TaskStatus.PENDING, nowMinute);

        if (!due.isEmpty()) {
            log.info("Found {} tasks due at {}: {}",
                    due.size(),
                    nowMinute,
                    due.stream().map(NotificationTask::getId).collect(Collectors.toList()));
        } else {
            log.debug("No tasks due at {}", nowMinute);
        }

        for (NotificationTask t : due) {
            SendResponse resp = bot.execute(new SendMessage(t.getChatId(), t.getMessage()));
            if (resp.isOk()) {
                t.setStatus(TaskStatus.SENT);   // помечаем как отправленное
                repo.save(t);
                log.info("Reminder sent: id={} chat={}", t.getId(), t.getChatId());
            } else {
                log.warn("Failed to send id={} chat={} code={} desc={}",
                        t.getId(), t.getChatId(), resp.errorCode(), resp.description());
            }
        }
    }
}