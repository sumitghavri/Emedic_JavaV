package com.emedic.management.services.patient;

import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Serializes patient phone lookups and inserts across application instances
 * without changing the finalized schema.
 */
@Service
public class PatientPhoneLockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatientPhoneLockService.class);
    private static final int LOCK_WAIT_SECONDS = 10;
    private static final String LOCK_PREFIX = "emedic:patient-phone:";

    private final JdbcTemplate jdbcTemplate;

    public PatientPhoneLockService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void acquire(String phone) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Patient phone locking requires an active transaction");
        }

        String lockName = lockName(phone);
        Integer acquired = jdbcTemplate.queryForObject(
                "select get_lock(?, ?)", Integer.class, lockName, LOCK_WAIT_SECONDS);
        if (!Integer.valueOf(1).equals(acquired)) {
            throw new IllegalStateException("Could not safely reserve this patient phone number; retry the request");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    jdbcTemplate.queryForObject("select release_lock(?)", Integer.class, lockName);
                } catch (RuntimeException ignored) {
                    LOGGER.error("Failed to release patient phone advisory lock {}", lockName, ignored);
                }
            }
        });
    }

    private String lockName(String phone) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(phone.getBytes(StandardCharsets.UTF_8));
            return LOCK_PREFIX + HexFormat.of().formatHex(digest).substring(0, 40);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
