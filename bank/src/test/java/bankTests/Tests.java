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
import java.util.List;

import static org.junit.Assert.*;

public class Tests {

    private EntityManagerFactory emf;
    private EntityManager em;
    private DatabaseCleaner dbc;

    public Tests() {
    }

    @Before
    public void setUp() throws SQLException {
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
    public void breakDown() throws SQLException {
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
    public void Opg2() throws SQLException {
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
    public void Opg3() throws SQLException {
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
    public void Opg4() throws SQLException {
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
    public void Opg5() throws SQLException {
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

    @Test
    public void Opg6() {
        Account acc = new Account(1L);
        Account acc2 = new Account(2L);
        Account acc9 = new Account(9L);

        //scenario 1
        Long balance1 = 100L;
        em.getTransaction().begin();
        em.persist(acc);
        acc.setBalance(balance1);
        em.getTransaction().commit();
        //TODO: voeg asserties toe om je verwachte waarde van de attributen te verifieren.
        assertEquals(balance1, acc.getBalance());
        //TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.
        Long accID = acc.getId();
        EntityManager em2 = emf.createEntityManager();
        Account accTemp = em2.find(Account.class, accID);
        assertEquals(acc.getBalance(), accTemp.getBalance());

        // scenario 2
        Long balance2a = 211L;
        acc = new Account(2L);
        em.getTransaction().begin();
        acc9 = em.merge(acc);
        acc.setBalance(balance2a);
        acc9.setBalance(balance2a + balance2a);
        em.getTransaction().commit();
        //TODO: voeg asserties toe om je verwachte waarde van de attributen te verifiëren.
        assertEquals(balance2a, acc.getBalance());
        Long temp = balance2a + balance2a;
        assertEquals(acc9.getBalance(), temp);
        assertEquals(acc.getAccountNr(), acc9.getAccountNr());
        //TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id. 
        // HINT: gebruik acccountDAO.findByAccountNr
        AccountDAOJPAImpl accountDao = new AccountDAOJPAImpl(emf.createEntityManager());
        Account accTest = accountDao.findByAccountNr(acc.getAccountNr());
        assertNotSame(accTest.getBalance(), acc.getBalance());

        // scenario 3
        Long balance3b = 322L;
        Long balance3c = 333L;
        acc = new Account(3L);
        em.getTransaction().begin();
        acc2 = em.merge(acc);
        assertFalse(em.contains(acc)); //  acc is gemerged waardoor die niet meer in de entity manager bestaat
        assertTrue(em.contains(acc2)); // Door de merge bestaat acc2 wel in de entity manager
        assertNotSame(acc, acc2);  // acc is het orgineel en acc2 is de terug gegeven waarde waardoor ze niet helemaal hetzelfde zijn.
        acc2.setBalance(balance3b);
        acc.setBalance(balance3c);
        em.getTransaction().commit();
        //TODO: voeg asserties toe om je verwachte waarde van de attributen te verifiëren.
        assertEquals(acc.getBalance(), balance3c);
        assertEquals(acc2.getBalance(), balance3b);
        //TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.
        assertTrue(em.contains(acc2));
        assertFalse(em.contains(acc));

        em2 = emf.createEntityManager();
        Account Test2 = em2.find(Account.class, acc2.getId());
        assertEquals(acc2.getBalance(), Test2.getBalance());

        // scenario 4
        Account account = new Account(114L);
        account.setBalance(450L);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(account);
        em.getTransaction().commit();

        Account account2 = new Account(114L);
        Account tweedeAccountObject = account2;
        tweedeAccountObject.setBalance(650l);
        assertEquals((Long) 650L, account2.getBalance());  //account2.getBalance is 650L dit wordt op de regel hierboven namelijk gedaan
        account2.setId(account.getId());
        em.getTransaction().begin();
        account2 = em.merge(account2);
        assertSame(account, account2);  //
        assertTrue(em.contains(account2));  //De transactions zijn nog niet gecommit en dus bestaat account2 nog in de entity manager
        assertFalse(em.contains(tweedeAccountObject));  //Door de merge bestaat tweedeAccountObject niet meer in de entity manager
        tweedeAccountObject.setBalance(850l);
        assertEquals((Long) 650L, account.getBalance());  //account en account2 zijn hetzelfde en op regel 250 is de balance gezet naar 650L
        assertEquals((Long) 650L, account2.getBalance());  //account en account2 zijn hetzelfde en op regel 250 is de balance gezet naar 650L
        em.getTransaction().commit();
        em.close();

    }

    @Test
    public void opg7() {
        Account acc1 = new Account(77L);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();
        //Database bevat nu een account.

        // scenario 1        
        Account accF1;
        Account accF2;
        accF1 = em.find(Account.class, acc1.getId());
        accF2 = em.find(Account.class, acc1.getId());
        assertSame(accF1, accF2);

        // scenario 2        
        accF1 = em.find(Account.class, acc1.getId());
        em.clear();
        accF2 = em.find(Account.class, acc1.getId());
        assertNotSame(accF1, accF2); //veranderd van assertSame naar assertNotSame
        //TODO verklaar verschil tussen beide scenario's
        //In het 2e scenario wordt de entity manager leeggemaakt, waardoor de 2 accounts niet hetzelfde zijn
    }

    @Test
    public void opg8() {
        Account acc1 = new Account(88L);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();
        Long id = acc1.getId();
        //Database bevat nu een account.

        em.remove(acc1);
        assertEquals(id, acc1.getId());
        //het id en acc1ID zijn tussendoor niet meer veranderd waardoor deze nog hetzelfde zijn
        Account accFound = em.find(Account.class, id);
        assertNull(accFound);
        //TODO: verklaar bovenstaande asserts
//        Het account is alleen nog te vinden in de database, want het is verwijderd uit de entity manager
    }

    @Test
    public void opg9() {
//        IDENTITY Indicates that the persistence provider must assign primary keys for the entity using a database identity column.
//        Sequence Indicates that the persistence provider must assign primary keys for the entity using a database sequence.
//        TABLE Indicates that the persistence provider must assign primary keys for the entity using an underlying database table to ensure uniqueness.  
//        Bron: http://www.objectdb.com/api/java/jpa/GenerationType

//        Bij TABLE blijft de applicatie hangen
//        Het verschil is de bron waarop de primary keys worden gebasseerd
        

    }

}
