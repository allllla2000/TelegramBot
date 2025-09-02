package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import com.pengrad.telegrambot.request.SendMessage;
import javax.annotation.PostConstruct;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern REMIND_PATTERN = Pattern.compile(
            "^(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)$"
    );
    private static final DateTimeFormatter REMIND_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository taskRepository;


    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            var msg = update.message();
            if (msg == null || msg.text() == null) return;

            long chatId = msg.chat().id();
            String text = msg.text().trim();

            if (text.equals("/start") || text.startsWith("/start@") || text.startsWith("/start ")) {
                String welcome = "Привет! Отправь в виде:\n01.09.2025 10:00 Пора делать домашку!";
                telegramBot.execute(new SendMessage(chatId, welcome));
                return;
            }

            Matcher m = REMIND_PATTERN.matcher(text);
            if (m.matches()) {
                String dateTimeStr = m.group(1);
                String reminderText = m.group(3);

                try {
                    LocalDateTime when = LocalDateTime.parse(dateTimeStr, REMIND_FORMAT);

                    if (when.isBefore(LocalDateTime.now())) {
                        telegramBot.execute(new SendMessage(chatId,
                                "Это время уже прошло. Укажи будущее: 01.09.2025 10:00 Текст"));
                        return;
                    }

                    var task = new pro.sky.telegrambot.model.NotificationTask(chatId, reminderText, when);
                    task.setStatus(pro.sky.telegrambot.model.TaskStatus.PENDING); // на всякий случай
                    taskRepository.save(task);

                    telegramBot.execute(new SendMessage(
                            chatId,
                            "Готово! Напомню " + dateTimeStr + "\nТекст: " + reminderText
                    ));
                } catch (DateTimeParseException e) {
                    telegramBot.execute(new SendMessage(chatId,
                            "Упс! Неясные дата/время. Формат: 01.09.2025 10:00 Текст"));
                }
            }
            //telegramBot.execute(new SendMessage(chatId, "Формат: 01.09.2025 10:00 Текст напоминания"));
        });

        return com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
//
