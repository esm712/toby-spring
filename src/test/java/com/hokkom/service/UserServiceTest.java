package com.hokkom.service;

import com.hokkom.dao.TestDaoFactory;
import com.hokkom.dao.UserDao;
import com.hokkom.domain.Level;
import com.hokkom.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

import static com.hokkom.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static com.hokkom.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestDaoFactory.class)
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    @Autowired
    UserDao userDao;

    List<User> users;

    @BeforeEach
    void setUp() {
        users = Arrays.asList(
                new User("user1", "유저1", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "user1@test.com"),
                new User("user2", "유저2", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "user2@test.com"),
                new User("user3", "유저3", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD-1, "user3@test.com"),
                new User("user4", "유저4", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD, "user4@test.com"),
                new User("user5", "유저5", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "user5@test.com")
        );
    }

    static class TestUserService extends UserServiceImpl {
        private String id;

        private TestUserService(UserDao userDao, MailSender mailSender, String id) {
            super(userDao, mailSender);
            this.id = id;
        }

        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException {}

    @Test
    public void bean() {
        assertNotNull(userService);
    }

    @Test
    @DirtiesContext
    public void upgradeLevels() {
        userDao.deleteAll();
        for(User user : users){
            userDao.add(user);
        }
        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getEmail());
        assertThat(request.get(1)).isEqualTo(users.get(3).getEmail());
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel().nextLevel());
        }
        else {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel());
        }
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel()).isEqualTo(userWithLevel.getLevel());
        assertThat(userWithoutLevelRead.getLevel()).isEqualTo(Level.BASIC);
    }

    @Test
    public void upgradeAllOrNothing() {
        UserServiceImpl testUserService = new TestUserService(this.userDao, this.mailSender, users.get(3).getId());

        UserServiceTx txUserService = new UserServiceTx(transactionManager, testUserService);

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try{
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }
        catch (TestUserServiceException e) {}

        checkLevelUpgraded(users.get(1), false);
    }
}