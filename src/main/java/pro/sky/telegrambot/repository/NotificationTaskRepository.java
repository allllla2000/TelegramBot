package pro.sky.telegrambot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.model.TaskStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {


    List<NotificationTask> findByStatusAndNotifyAtLessThanEqual(
            TaskStatus status, java.time.LocalDateTime moment);

    List<NotificationTask> findByChatIdAndStatus(Long chatId, TaskStatus status);

    List<NotificationTask> findByStatusAndNotifyAt(
            TaskStatus status, LocalDateTime notifyAt);



}
//