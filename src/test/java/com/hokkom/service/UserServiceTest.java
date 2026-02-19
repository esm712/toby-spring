package com.hokkom.service;

import com.hokkom.dao.TestDaoFactory;
import com.hokkom.dao.UserDao;
import com.hokkom.domain.Level;
import com.hokkom.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hokkom.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static com.hokkom.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestDaoFactory.class)
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    @Autowired
    UserDao userDao;

    List<User> users;

    @Autowired
    ApplicationContext context;

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

    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList<>();

        private MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated() {
            return this.updated;
        }

        public List<User> getAll() {
            return this.users;
        }

        public void update(User user) {
            updated.add(user);
        }

        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        public User get(String id) {
            throw new UnsupportedOperationException();
        }

        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        public int getCount() {
            throw new UnsupportedOperationException();
        }
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
        MockUserDao mockUserDao = new MockUserDao(this.users);
        MockMailSender mockMailSender = new MockMailSender();
        UserServiceImpl userServiceImpl = new UserServiceImpl(mockUserDao, mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertThat(updated.size()).isEqualTo(2);
        checkUserAndLevel(updated.get(0), "user2", Level.SILVER);
        checkUserAndLevel(updated.get(1), "user4", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getEmail());
        assertThat(request.get(1)).isEqualTo(users.get(3).getEmail());
    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId()).isEqualTo(expectedId);
        assertThat(updated.getLevel()).isEqualTo(expectedLevel);
    }

    @Test
    public void mockUpgradeLevels() {
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        MailSender mockMailSender = mock(MailSender.class);
        UserServiceImpl userServiceImpl = new UserServiceImpl(mockUserDao,mockMailSender);

        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel()).isEqualTo(Level.SILVER);
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel()).isEqualTo(Level.GOLD);

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0]).isEqualTo(users.get(1).getEmail());
        assertThat(mailMessages.get(1).getTo()[0]).isEqualTo(users.get(3).getEmail());
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
    @DirtiesContext
    public void upgradeAllOrNothing() throws Exception {
        TestUserService testUserService = new TestUserService(this.userDao, this.mailSender, users.get(3).getId());

        TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", TxProxyFactoryBean.class);
        txProxyFactoryBean.setTarget(testUserService);
        UserService txUserService = (UserService) txProxyFactoryBean.getObject();

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