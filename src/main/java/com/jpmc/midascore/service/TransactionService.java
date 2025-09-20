package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository,
                              RestTemplate restTemplate,
                              @Value("${incentive.url:http://localhost:8080/incentive}") String incentiveUrl) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
        this.incentiveUrl = incentiveUrl;
    }

    @Transactional
    public boolean process(Transaction dto) {
        // Validate sender/recipient existence
        UserRecord sender = userRepository.findById(dto.getSenderId());
        UserRecord recipient = userRepository.findById(dto.getRecipientId());
        if (sender == null || recipient == null) {
            return false;
        }

        float amount = dto.getAmount();
        // Validate sufficient funds
        if (sender.getBalance() < amount) {
            return false;
        }

        // Build request body with the exact field names the external API expects
        Map<String, Object> req = new HashMap<>();
        req.put("sender", dto.getSenderId());
        req.put("recipient", dto.getRecipientId());
        req.put("amount", amount);

        // Call incentive API
        Incentive incentive = restTemplate.postForObject(incentiveUrl, req, Incentive.class);
        float incentiveAmt = (incentive != null ? incentive.getAmount() : 0.0f);
        if (incentiveAmt < 0) {
            incentiveAmt = 0.0f; // safety
        }

        // Apply balances (incentive only adds to recipient)
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmt);

        // Persist
        userRepository.save(sender);
        userRepository.save(recipient);
        transactionRepository.save(new TransactionRecord(sender, recipient, amount, incentiveAmt));

        return true;
    }
}
