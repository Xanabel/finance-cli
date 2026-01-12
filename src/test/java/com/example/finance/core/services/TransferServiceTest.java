package com.example.finance.core.services;

import com.example.finance.core.model.OperationType;
import com.example.finance.core.model.User;
import com.example.finance.core.model.Wallet;
import com.example.finance.core.ports.WalletStorage;
import com.example.finance.infra.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransferServiceTest {

    private WalletService walletService;
    private FakeWalletStorage walletStorage;
    private InMemoryUserRepository userRepo;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService();
        walletStorage = new FakeWalletStorage();
        userRepo = new InMemoryUserRepository();
        transferService = new TransferService(userRepo, walletStorage, walletService);
    }

    @Test
    void transfer_success_createsExpenseAndIncome() {
        User from = new User("xana", "hash");
        User to = new User("den", "hash");

        from.setWallet(new Wallet());
        walletStorage.saveWallet("xana", from.getWallet());
        walletStorage.saveWallet("den", new Wallet());

        userRepo.save(from);
        userRepo.save(to);

        transferService.transfer(from, "den", 1000, "gift");

        double out = walletService.sumByCategories(from.getWallet(), OperationType.EXPENSE, java.util.List.of("Перевод"));
        assertEquals(1000, out);

        Wallet denWallet = walletStorage.loadWallet("den");
        double in = walletService.sumByCategories(denWallet, OperationType.INCOME, java.util.List.of("Перевод"));
        assertEquals(1000, in);
    }

    @Test
    void transfer_toSelf_throws() {
        User from = new User("xana", "hash");
        from.setWallet(new Wallet());

        userRepo.save(from);
        walletStorage.saveWallet("xana", from.getWallet());

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(from, "xana", 10, ""));
    }

    @Test
    void transfer_negativeAmount_throws() {
        User from = new User("xana", "hash");
        User to = new User("den", "hash");

        from.setWallet(new Wallet());
        userRepo.save(from);
        userRepo.save(to);

        walletStorage.saveWallet("xana", from.getWallet());
        walletStorage.saveWallet("den", new Wallet());

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(from, "den", -1, ""));
    }

    @Test
    void transfer_zeroAmount_throws() {
        User from = new User("xana", "hash");
        User to = new User("den", "hash");

        from.setWallet(new Wallet());
        userRepo.save(from);
        userRepo.save(to);

        walletStorage.saveWallet("xana", from.getWallet());
        walletStorage.saveWallet("den", new Wallet());

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(from, "den", 0, ""));
    }

    @Test
    void transfer_receiverNotFound_throws() {
        User from = new User("xana", "hash");
        from.setWallet(new Wallet());

        userRepo.save(from);
        walletStorage.saveWallet("xana", from.getWallet());

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(from, "nope", 10, ""));
    }

    private static class FakeWalletStorage implements WalletStorage {
        private final Map<String, Wallet> map = new HashMap<>();

        @Override
        public Wallet loadWallet(String login) {
            return map.getOrDefault(login, new Wallet());
        }

        @Override
        public void saveWallet(String login, Wallet wallet) {
            map.put(login, wallet);
        }
    }
}
