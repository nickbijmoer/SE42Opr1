package bankTests;

import bank.dao.AccountDAOJPAImpl;
import bank.domain.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.DatabaseCleaner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class Tests {
    private EntityManagerFactory emf;
    private EntityManager em;
    private DatabaseCleaner dbc;


    public Tests() {
    }

    @Before
    public void setUp() throws SQLException
    {
        try {
        emf = Persistence.createEntityManagerFactory("bankPU");
        em = emf.createEntityManager();
        dbc = new DatabaseCleaner(this.em);
        dbc.clean();
        em = emf.createEntityManager(); 
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        

    }

    @After
    public void breakDown() throws SQLException
    {
    }


    @Test
    public void Opg1() throws SQLException {
        
        
        /* 1. eerste assert = null, want nog geen commit
              tweede assert komt true want hij is gecommit dus getid word groter dan 0
              eerste println komt de id, die kan je van te voren niet weten want AI
        
        
        */
        Account account = new Account(111L);
        em.getTransaction().begin();
        em.persist(account);
        
        //Nog geen commit gedaan dus hij bestaat nog niet in de database dus krijg je null terug.
        assertNull(account.getId());
        
        
        //INSERT INTO ACCOUNT (ACCOUNTNR,BALANCE,THRESHOLD) VALUES (?,?,?) Bind(111,0,0)
        em.getTransaction().commit();
        
        
        //hier is de commit doorgevoerd dus het account is bekend in de database
        //SELECT account FROM ACCOUNT WHERE accountnr = 111
        System.out.println("AccountId: " + account.getId());
        assertTrue(account.getId() > 0L);
        
        //eindresultaat, er zit een account in de database met accountnr 111 en id AI
    }
    
    /*
    1e assert returnt null omdat er nog geen commit is uitgevoerd
    2e assert is true, want na de rollback staan er geen accounts meer in de database dus = 0
    */
     @Test
    public void Opg2() throws SQLException 
    {
    Account account = new Account(111L);
    em.getTransaction().begin();
    em.persist(account);
    assertNull(account.getId());
    em.getTransaction().rollback();
    // TODO code om te testen dat table account geen records bevat. Hint: bestudeer/gebruik AccountDAOJPAImpl
        AccountDAOJPAImpl acccDAO = new AccountDAOJPAImpl(em);
        //select count(a) from Account as a (staat in account.java)
        assertTrue(acccDAO.count() == 0);
    }
    
    /*
    1e assert returnt null omdat er nog geen commit is uitgevoerd
    2e assert is true, want na de rollback staan er geen accounts meer in de database dus = 0
    
    INSERT INTO ACCOUNT (ACCOUNTNR,BALANCE,THRESHOLD) VALUES (?,?,?) Bind(111,0,0)
    select id from Account as a where a.accountNr = ? bind(111)
    
    eindresultaat een rij bij account tabel met id = 1 of hoger, acountnr = 111 balance = 0 , threshold = 0
    */
     @Test
    public void Opg3() throws SQLException 
    {
    Long expected = -100L;
    Account account = new Account(111L);
    account.setId(expected);
    em.getTransaction().begin();
    em.persist(account);
    //id is geset naar -100 dus assertequals is waar
    assertEquals(expected, account.getId());
    em.flush();
    //TODO: verklaar en pas eventueel aan
    //data is nu wel gesynct met database waar AI van pas is gekomen dus nu is id niet -100
    assertNotEquals(expected, account.getId());
    em.getTransaction().commit();

    }
    
    
    /*
    Eerste assert waarde is geset naar 400, gecommit dus getbalance is ook 400
    tweede assert is true want hij zoekt naar het vorige account in de database dus balance is 400
    
    hier word een update toegepast en een select
    
    eindresultaat = id = 1 ,accountnr is 114 balance is 400 threshold = 0
    */
     @Test
    public void Opg4() throws SQLException 
    {
    Long expectedBalance = 400L;
    Account account = new Account(114L);
    em.getTransaction().begin();
    em.persist(account);
    account.setBalance(expectedBalance);
    em.getTransaction().commit();
    assertEquals(expectedBalance, account.getBalance());
    //TODO: verklaar de waarde van account.getBalance
    // zie hierboven
    Long acId = account.getId();
    account = null;
    EntityManager em2 = emf.createEntityManager();
    em2.getTransaction().begin();
    Account found = em2.find(Account.class, acId);
    //TODO: verklaar de waarde van found.getBalance
    //zie hierboven
    assertEquals(expectedBalance, found.getBalance());


    }
    /*
    De eerste waarde returnt true, de verwachte waarde is 400 en hij geeft ook 400
    De tweede waarde is ook true, omdat je hem gecommit en gerefresht hebt 
    
    UPDATE ACCOUNT SET BALANCE = 650 WHERE (ID = 20)
    SELECT ID,ACCOUNTNR,BALANCE,THRESHOLD FROM ACCOUNT WHERE (ID = 20)
    
    eindresultaat id = 1 accountnr = 114 balance = 650 threshold = 0
    */
    @Test
    public void Opg5() throws SQLException 
    {
    Long expectedBalance = 400L;
    Account account = new Account(114L);
    em.getTransaction().begin();
    em.persist(account);
    account.setBalance(expectedBalance);
    em.getTransaction().commit();
    assertEquals(expectedBalance, account.getBalance());
   
    Long acId = account.getId();
    EntityManager em2 = emf.createEntityManager();
    em2.getTransaction().begin();
    Account found = em2.find(Account.class, acId);
    assertEquals(expectedBalance, found.getBalance());
    
    Long newExpected = 650L;
    found.setBalance(newExpected);
    em2.getTransaction().commit();
    em.refresh(account);
    assertEquals(newExpected, account.getBalance());


    }
    
    
}