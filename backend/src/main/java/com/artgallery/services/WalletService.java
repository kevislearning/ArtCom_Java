package com.artgallery.services;

import com.artgallery.domain.Commission;
import com.artgallery.domain.User;
import com.artgallery.domain.WalletTransaction;
import com.artgallery.repositories.UserRepository;
import com.artgallery.repositories.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    public static class WalletResult {
        public final User user;
        public final WalletTransaction transaction;

        public WalletResult(User user, WalletTransaction transaction) {
            this.user = user;
            this.transaction = transaction;
        }
    }

    @Transactional
    public WalletResult deposit(UUID userId, double amount, String description) {
        return deposit(userId, amount, description, null);
    }

    @Transactional
    public WalletResult deposit(UUID userId, double amount, String description, Commission reference) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setWalletBalance(user.getWalletBalance() + amount);
        user = userRepository.save(user);

        WalletTransaction tx = WalletTransaction.builder()
                .user(user)
                .amount(amount)
                .type("deposit")
                .reference(reference)
                .description(description)
                .build();

        tx = transactionRepository.save(tx);
        return new WalletResult(user, tx);
    }

    @Transactional
    public WalletResult withdraw(UUID userId, double amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getWalletBalance() < amount) {
            throw new IllegalArgumentException("Số dư khả dụng không đủ!");
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        user = userRepository.save(user);

        WalletTransaction tx = WalletTransaction.builder()
                .user(user)
                .amount(-amount)
                .type("withdraw")
                .description(description)
                .build();

        tx = transactionRepository.save(tx);
        return new WalletResult(user, tx);
    }

    @Transactional
    public WalletResult escrowHold(UUID userId, double amount, String description, Commission reference) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getWalletBalance() < amount) {
            throw new IllegalArgumentException("Số dư tài khoản không đủ để đặt cọc!");
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        user = userRepository.save(user);

        WalletTransaction tx = WalletTransaction.builder()
                .user(user)
                .amount(-amount)
                .type("escrow_hold")
                .reference(reference)
                .description(description)
                .build();

        tx = transactionRepository.save(tx);
        return new WalletResult(user, tx);
    }

    @Transactional
    public WalletResult escrowRelease(UUID artistId, double amount, String description, Commission reference) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        artist.setWalletBalance(artist.getWalletBalance() + amount);
        artist = userRepository.save(artist);

        WalletTransaction tx = WalletTransaction.builder()
                .user(artist)
                .amount(amount)
                .type("escrow_release")
                .reference(reference)
                .description(description)
                .build();

        tx = transactionRepository.save(tx);
        return new WalletResult(artist, tx);
    }

    @Transactional
    public WalletResult escrowRefund(UUID clientId, double amount, String description, Commission reference) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        client.setWalletBalance(client.getWalletBalance() + amount);
        client = userRepository.save(client);

        WalletTransaction tx = WalletTransaction.builder()
                .user(client)
                .amount(amount)
                .type("escrow_refund")
                .reference(reference)
                .description(description)
                .build();

        tx = transactionRepository.save(tx);
        return new WalletResult(client, tx);
    }
}
