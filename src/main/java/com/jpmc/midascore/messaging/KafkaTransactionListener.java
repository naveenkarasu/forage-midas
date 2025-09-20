package com.jpmc.midascore.messaging;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaTransactionListener.class);

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public KafkaTransactionListener(TransactionService transactionService,
                                    UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    // Topic from application.yml → general.kafka-topic
    @KafkaListener(topics = "${general.kafka-topic}" , groupId = "midas-core")
    public void onMessage(@Payload Transaction tx) {
        boolean recorded = transactionService.process(tx);
        log.info("Processed tx senderId={} recipientId={} amount={} -> {}",
                tx.getSenderId(), tx.getRecipientId(), tx.getAmount(),
                recorded ? "RECORDED" : "DISCARDED");

        // Log Waldorf’s balance after each message
        UserRecord waldorf = userRepository.findByName("wilbur");
        if (waldorf != null) {
            log.warn("Current balance of wilbur: {}", waldorf.getBalance());
        } else {
            log.warn("User 'wilbur' not found");
        }
    }
}
