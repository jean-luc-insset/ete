package fr.insset.jean_luc;



import java.io.File;
import java.lang.reflect.Field;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
 *
 * @author jldeleage
 */
public class EteMojoTest {



    public EteMojoTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class EteMojo.
     */
    @Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        EteMojo instance = new EteMojo();
        initialiseMojo(instance);
//        instance.execute();
        // TODO review the generated test code and remove the default call to fail.
    }

    protected void initialiseMojo(EteMojo mojo) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        set(mojo, "outputDirectory", new File("target"));
        set(mojo, "base", new File("."));
        set(mojo, "chemin", "src/test/mda");
        set(mojo, "nomModele", "modele.xml");
        set(mojo, "nomFichierConfiguration", "config-ete.xml");
        set(mojo, "nomDossierCible", "generated-sources/ete");
        set(mojo, "nomDossierPages", "generated-resources/ete/webapp");
//        set(mojo, "project", new MavenProject());
    }


    protected void set(EteMojo mojo, String nom, Object valeur) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<? extends EteMojo> classe = mojo.getClass();
        Field field = classe.getDeclaredField(nom);
        field.setAccessible(true);
        field.set(mojo, valeur);
    }

}
