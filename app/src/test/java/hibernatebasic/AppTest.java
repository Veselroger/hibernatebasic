package hibernatebasic;

import hibernatebasic.model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class AppTest {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager em;

    @Before
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("SimpleUnit");
        em = entityManagerFactory.createEntityManager();
    }

    @After
    public void destroy() {
        em.close();
        entityManagerFactory.close();
    }

    @Test
    public void shouldPersistEntity() {
        Account acc = new Account();
        acc.setId(1L);
        acc.setName("ADMIN");
        em.getTransaction().begin();
        em.persist(acc);
        em.getTransaction().commit();
        // JPQL
        TypedQuery<Account> studentQuery;
        studentQuery = em.createQuery("SELECT a FROM Account a WHERE a.name = ?1", Account.class);
        studentQuery.setParameter(1, "ADMIN");
        List<Account> resultList = studentQuery.getResultList();
        Assert.assertEquals(1, resultList.size());
    }
}
