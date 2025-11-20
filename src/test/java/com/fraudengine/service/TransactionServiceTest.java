package com.fraudengine.service;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private RuleEvaluationService evaluationService;

    @InjectMocks
    private TransactionService service;

    @Test
    void shouldProcessTransactionSuccessfully() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .build();

        when(repository.existsById("T1")).thenReturn(false);

        service.processTransaction(tx);

        verify(repository).save(tx);
        verify(evaluationService).evaluate(tx);
    }

    @Test
    void shouldThrow_WhenTransactionExistsAlready() {
        when(repository.existsById("T1")).thenReturn(true);

        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .build();

        assertThrows(IllegalStateException.class,
                () -> service.processTransaction(tx));
    }

    @Test
    void shouldGetTransaction() {
        Transaction tx = Transaction.builder().transactionId("T1").build();

        when(repository.findById("T1")).thenReturn(Optional.of(tx));

        var result = service.getTransaction("T1");

        assertEquals("T1", result.getTransactionId());
    }

    @Test
    void shouldThrow_WhenTransactionNotFound() {
        when(repository.findById("T1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getTransaction("T1"));
    }
}
