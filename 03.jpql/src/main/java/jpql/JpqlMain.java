package jpql;

import jakarta.persistence.*;

public class JpqlMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql");
        EntityManager em = emf.createEntityManager();
        //code

        em.close();
        emf.close();
    }
}
