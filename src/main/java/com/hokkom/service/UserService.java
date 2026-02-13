package com.hokkom.service;

import com.hokkom.dao.UserDao;
import com.hokkom.domain.Level;
import com.hokkom.domain.User;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

public class UserService {

    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
    public static final int MIN_RECCOMEND_FOR_GOLD = 30;

    private PlatformTransactionManager transactionManager;
    UserDao userDao;

    public UserService(UserDao userDao, PlatformTransactionManager transactionManager) {
        this.userDao = userDao;
        this.transactionManager = transactionManager;
    }

    public void upgradeLevels() {
        TransactionStatus staus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            List<User> users = userDao.getAll();
            for(User user : users) {
                if (canUpgradeLevel(user)) {
                   upgradeLevel(user);
                }
            }
            transactionManager.commit(staus);
        } catch (RuntimeException e) {
            transactionManager.rollback(staus);
            throw e;
        }
    }

    private boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();
        return switch (currentLevel) {
            case BASIC -> (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
            case SILVER -> (user.getRecommend() >= MIN_RECCOMEND_FOR_GOLD);
            case GOLD -> false;
            default -> throw new IllegalStateException("Unknown value: " + currentLevel);
        };
    }

    protected void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
    }

    public void add(User user) {
        if (user.getLevel() == null) user.setLevel(Level.BASIC);
        userDao.add(user);
    }

}
