package fr.insset.jean_luc;

/*
 * Plug-in Maven de transformation MDA.
 * Le plug-in utilise un fichier de configuration séparée pour faciliter
 * le portage vers d'autes environnements.
 */

import com.jldeleage.mda.transfo.ApplicateurTemplate;
import com.jldeleage.mda.transfo.EnrichissementXml;
import com.jldeleage.mda.transfo.ExceptionTransformation;
import com.jldeleage.mda.modele.LecteurXmi;
import com.jldeleage.mda.transfo.Transformation;
import com.jldeleage.mda.transfo.TransformationAbstraite;
import com.jldeleage.mda.transfo.TransformationWrapper;
import com.jldeleage.mda.transfo.TransformationXSLT;
import com.jldeleage.mda.util.Dump;
import com.jldeleage.util.ChargeurRessource;
import java.beans.FeatureDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.MapELResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;






/**
 * Ce "goal" travaille en amont du projet. Il enrichit le mod&egrave;le
 * initial et g&eacute;n&egrave;re des codes sources.
 * 
 */
@Mojo( name = "ete_old",
       defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class EteMojoOK extends /* AbstractMojo */ EteMojo {


    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}",
                property = "outputDir",
                required = true )
    private File outputDirectory;

    @Parameter(defaultValue = "${basedir}",
               property = "base")
    private File base;

    @Parameter(defaultValue = "src/main/mda/",
               property = "chemin")
    private String chemin;

    @Parameter(property="ete.model",
                defaultValue = "model.xml",
                required = true)
    private String nomModele;

    @Parameter(property="ete.config",
                defaultValue="config-ete.xml")
    private String nomFichierConfiguration;

    @Parameter(property="dossier-cible",
                defaultValue = "generated-sources/ete")
    private String nomDossierCible;

    @Parameter(property="dossier-ressources",
                defaultValue = "generated-resources/ete/webapp")
    private String nomDossierPages;

    @Parameter(defaultValue="${project}")
    private MavenProject project;
    private static MavenProject  projetStatique;

    /**
     * @parameter
     */
    @Parameter
    private Map<String,String> plugins;


    public final static String ENSEMBLE_TRANSFORMATION = "transformation-set";

    public final static String TEMPLATE                = "template";
    public final static String XSLT                    = "xslt";
    public final static String OPAQUE                  = "opaque";
    public final static String MODIFY_XML              = "modifyXml";
    public final static String DUMP                    = "dump";
    public final static String MODULE                  = "module";
    public final static String IF                      = "if";
    public final static String CHOOSE                  = "choose";
    public final static String WHEN                    = "when";
//    public final static String LECTURE_MOF             = "mof";


    /**
     * "Enregistre" les diff&eacute;rentes impl&eacute;mentations
     * des transformations :<br/>
     * Le plug-in lit un fichier de configuration dans lequel des
     * "pas" sont décrits.
     * Chaque pas correspond à l'exécution de la méthode
     * <code>transforme</code> d'une classe impl&eacute;mentant
     * l'interface <code>Transformation</code>.<br/>
     * On associe la description du fichier de configuration
     * &agrave; la classe d'impl&eacute;mentation via la
     * m&eacute;thode <code>enregistre</code>.<br/>
     * Cela permet &eacute;ventuellement de construire
     * une sous-classe utilisant d'autres impl&eacute;mentations.
     */
    public EteMojoOK() {
        enregistre(TEMPLATE, new FabriqueSimple(ApplicateurTemplate.class));
        enregistre(OPAQUE, new FabriqueWrapper());
        enregistre(XSLT, new FabriqueSimple(TransformationXSLT.class));
        enregistre(MODIFY_XML, new FabriqueSimple(EnrichissementXml.class));
        enregistre(DUMP, new FabriqueSimple(Dump.class));
//        enregistre(MODULE, new FabriqueTransformationModule());
//        enregistre(IF, new FabriqueTransformationIf(this));
    }



    //========================================================================//
    // I N I T I A L I S A T I O N
    //========================================================================//


    // TODO : remettre
//    @Override
//    protected final void enregistre(String nomType, FabriquePas fabrique) {
//        typesTransformations.put(nomType, fabrique);
//    }


    //========================================================================//
    // A C C E S S E U R S
    //========================================================================//



    public Map<String, FabriqueTransformation> getTypesTransformations() {
//        return typesTransformations;
        return null;
    }

    public File getBase() {
        return base;
    }

    public String getChemin() {
        return chemin;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }



    //========================================================================//
    // E X E C U T I O N   D U   G O A L
    //========================================================================//


    public void execute() throws MojoExecutionException {
        localThis.set(this);
        projetStatique = project;
        System.out.println("Projet ; " + projetStatique);
        Log log = getLog();
//        if (dejaFait) {
//            log.warn("DEJA FAIT");
//            return;
//        }
//        dejaFait = true;
        log.info(" EXECUTION DU PLUG-IN ETE");
        Map pluginContext = this.getPluginContext();
        System.out.println("Contexte du plug-in : ");
        if (pluginContext != null) {
            for (Object clef : pluginContext.keySet()) {
                System.out.println("  " + clef + pluginContext.get(clef));
            }
        }
        if (plugins != null) {
            for (String clef : plugins.keySet()) {
                String nomClasseFabrique = plugins.get(clef);
                System.out.println("Essai de chargement du plugin " + nomClasseFabrique);
                ClassLoader classLoader = getClass().getClassLoader();
                String classpath = System.getProperty("java.class.path");
                System.out.println("ClassPath " + classpath);
                System.out.println("ClassLoaders normaux");
                while (classLoader != null) {
                    System.out.println(classLoader);
                    classLoader = classLoader.getParent();
                }
                classLoader = Thread.currentThread().getContextClassLoader();
                System.out.println("ClassLoaders currentThread");
                while (classLoader != null) {
                    System.out.println(classLoader);
                    classLoader = classLoader.getParent();
                }
                try {
                    Class<?> classeFabrique = null;
                    try {
                        classeFabrique = Thread.currentThread().getContextClassLoader().loadClass(nomClasseFabrique);
                    }
                    catch (Exception e1) {
                        classeFabrique = Class.forName(nomClasseFabrique);
                    }
                    FabriquePas fabrique = (FabriquePas) classeFabrique.newInstance();
//                    enregistre(clef, fabrique);
                    System.out.println("Enregistrement de la fabrique " + clef + " : " + classeFabrique.getName());
                }
                catch (Exception e2) {
                    System.out.println("IMPOSSIBLE D'ENREGISTRER LE PLUG-IN " + clef + " : " + e2);
                }
            }
        }

        // a- lire le fichier de configuration eventuel
        // Le fichier de configuration est passe en parametre
        // Cela permet d'avoir des transformation generiques dans le projet
        // parent et des transformations specifiques dans les modules.
        File fichierConfiguration = chercheNomFichierConfig();
        Document configuration = lisConfig(fichierConfiguration);

        // b- Créer le dossier où placer les codes générés
        // Si on "décommente" l'instruction, le plug-in ne fait rien si le
        // dossier existe déjà
        if (!construisDossier()) {
//            return;
        }

        // c- parcourir le fichier de configuration et executer chacun des
        // ensembles de transformations.
        // En effet, le fichier de configuration peut demander le traitement
        // de plusieurs modeles. Chacun d'eux "subit" un ensemble de
        // transformations.
        // Un ensemble de transformations est constitue de "pas".
        // Un pas est l'application d'un template, une transformation
        // "opaque", un "dump", un "module"...
        // Un module est lui-meme un ensemble de pas;
        executeLesEnsemblesDeTransformations(configuration);
    }   // execute


    protected File chercheNomFichierConfig() throws MojoExecutionException {
        if (chemin != null && ! chemin.endsWith("/")) {
            chemin += '/';
        }
        File fichierConfiguration = getFichierInProjectOrParent(base, chemin + nomFichierConfiguration);
        if (fichierConfiguration == null) {
            throw new MojoExecutionException("Pas de fichier de configuration pour Ete");
        }
        return fichierConfiguration;
    }

    protected Document lisConfig(File fichierConfiguration) throws MojoExecutionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(EteMojoOK.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoExecutionException("Impossible de lire un DOM", ex);
        }
        Document configuration = null;
        try {
            configuration = builder.parse(fichierConfiguration);
        } catch (Exception ex) {
            Logger.getLogger(EteMojoOK.class.getName()).log(Level.SEVERE, 
                    "Impossible de lire le fichier de configuration", ex);
            throw new MojoExecutionException("Impossible de lire le fichier de configuration", ex);
        }        
        return configuration;
    }


    protected void executeLesEnsemblesDeTransformations(Document configuration) throws MojoExecutionException {
        // c- parcourir le fichier de configuration et executer chacune des
        // transformations
        NodeList noeudsTransformations = configuration.getElementsByTagName(ENSEMBLE_TRANSFORMATION);
        for (int i=0 ; i<noeudsTransformations.getLength() ; i++) {
            Node transfoSuivante = noeudsTransformations.item(i);
            try {
                executeUneTransformation(transfoSuivante);
            } catch (Exception ex) {
                Logger.getLogger(EteMojoOK.class.getName()).log(Level.SEVERE, null, ex);
                throw new MojoExecutionException("Transformation Ete échouée");
            }
        }
    }


    protected boolean construisDossier() {
        File fichier = new File(outputDirectory, nomDossierCible);
        Log log = getLog();
        if (fichier.exists()) {
//            log.info("LE DOSSIER EXISTE DEJA : abandon");
            return false;
        }
//        log.info("GENERATION DU DOSSIER " + fichier);
        fichier.mkdirs();
        // Dans le cas de test, on n'a pas de projet
        if (null != project) {
            project.addCompileSourceRoot(fichier.getAbsolutePath());
        }
        // S'il y a des ressources
        if (nomDossierPages != null) {
            fichier = new File(outputDirectory, nomDossierPages);
            fichier.mkdirs();
        }
        return true;
    }


    /**
     * Part du dossier &lt;dossier&gt; d'un projet et cherche dans ce dossier
     * le fichier
     * &lt;dossier&gt;/src/main/&lt;fichier&gt;
     * 
     * @return 
     */
    protected File getFichierInProjectOrParent(File inDossier, String nomFichier) {
//        Log log = getLog();
//        log.info("Recherche de " + nomFichier+ " dans " + inDossier.getAbsolutePath());
        File fichier = new File(inDossier, nomFichier);
        if (fichier.exists()) {
            return fichier;
        }
        inDossier = inDossier.getParentFile();
        if (null != inDossier && inDossier.exists()) {
            return getFichierInProjectOrParent(inDossier, nomFichier);
        }
        return null;
    }



    //========================================================================//
    // E X E C U T I O N   D ' U N E   T R A N S F O R MA T I O N 
    //========================================================================//


    /**
     * Le fichier de configuration contient des transformations.<br/>
     * Chacune porte sur un mod&egrave;le (ce qui permet de construire
     * un projet &agrave; partir de plusieurs mod&egrave;les).<br/>
     * La transformation contient des "templates", des transformations
     * XSLT, des op&eacute;rations opaques.<br/>
     * Cette m&eacute;thode lit le mod&egrave;le sur lequel porte la
     * transformation et les pas &agrave; appliquer sur ce mod&egrave;le
     * et ex&eacute;cute ceux-ci.<br/>
     * Cette d&eacute;marche utilise un contexte propre &agrave; une
     * transformation.<br/>
     * De ce fait, on peut parall&eacute;liser les transformations.<br/>
     * Le contexte a en charge en particulier de ne pas appliquer deux fois
     * la m&ecirc;me transformation &agrave; un m&ecirc;me mod&egrave;le.
     * 
     * @param transfo : &eacute;l&eacute;ment de nom "transformation-set".
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws ExceptionTransformation 
     */
    protected void executeUneTransformation(Node transfo) throws ExceptionTransformation {
        Log log = getLog();
        Element transformation = (Element) transfo;

        // a- retrouver le modele
        String nomModeleConfig = getNomModele(transformation);

        try {
            // b- lire le modele
            Document modele = getModele(nomModeleConfig);

            // c- retrouver chacun des pas a appliquer
            executeLesPas(modele, transformation);
        } catch (ExceptionTransformation ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExceptionTransformation(ex);
        }
    }


    protected String getNomModele(Element inElement) {
        // a- retrouver le modele
        Element transformation = (Element) inElement;
        NodeList liste = transformation.getElementsByTagName("modele");
        String nomModeleConfig;
        if (liste != null  && liste.getLength() > 0) {
            Node noeudModele = liste.item(0);
            nomModeleConfig = noeudModele.getFirstChild().getNodeValue();
        }
        else {
            nomModeleConfig = nomModele;
        }
        return nomModeleConfig;
    }


    protected Document getModele(String nomModele)
                    throws ParserConfigurationException, SAXException,
                            IOException, FileNotFoundException,
                            TransformerException,
                            TransformerConfigurationException,
                            ExceptionTransformation {
        Log log = getLog();
        LecteurXmi lecteurXmi = new LecteurXmi();
        File model = getFichierInProjectOrParent(new File(base,chemin), nomModele);
        if (model == null) {
            // TODO : faut-il plutôt lever une exception ?
            log.info("modèle introuvable : " + nomModele);
            return null;
        }
        Document modele = lecteurXmi.lisXmi(model);
        return modele;
    }


    protected Document executeLesPas(Document modele, Element transformation) throws InstantiationException, IllegalAccessException, ExceptionTransformation, ExceptionTransformation {
        contexte.set(new Contexte(modele));
        TransformationModule transfo = new TransformationModule(transformation, getLog(), typesTransformations, base, chemin, outputDirectory);
        modele = transfo.executeLesPas(modele, transformation);
        contexte.remove();
        return modele;
    }

    //========================================================================//
    // A C C E S S E U R S   V A R I A B L E S   D ' I N S T A N C E
    //========================================================================//
    public String getNomModele() {
        return nomModele;
    }

    public void setNomModele(String nomModele) {
        this.nomModele = nomModele;
    }

    public String getNomFichierConfiguration() {
        return nomFichierConfiguration;
    }

    public void setNomFichierConfiguration(String nomFichierConfiguration) {
        this.nomFichierConfiguration = nomFichierConfiguration;
    }

    public Stack<Map<String, String>> getPileParametres() {
        return pileParametres;
    }

    public void setPileParametres(Stack<Map<String, String>> inPileParametres) {
        pileParametres = inPileParametres;
    }




    //========================================================================//
    // V A R I A B L E S   D ' I N S T A N C E
    //========================================================================//


    /**
     * "Ouverture/Fermeture" du plugin : les transformations sont
     * ex&eacute;cut&eacute;es par des instances de classes enregistr&eacute;es.<br/>
     * Cela permet de facilement rajouter des classes, donc des types de
     * transformations.
     */
    private final Map<String, FabriquePas> typesTransformations = new HashMap<>();

    private static final ThreadLocal<Contexte> contexte = new ThreadLocal<>();

    private static final ThreadLocal<EteMojoOK> localThis = new ThreadLocal<>();

    /**
     * 
     */
    private Stack<Map<String, String>> pileParametres = new Stack<>();




    //========================================================================//
    // F A B R I Q U E   T R A N S F O R M A T I O N   M O D U L E
    //========================================================================//
    // Un module est un ensemble de pas.
    // L'execution d'un pas de type "module" declenche chacun des pas du
    // module.
    // Le processus peut etre recursif.


    private abstract class FabriqueAbstraiteTransformation implements FabriquePas<TransformationModule> {

        public EteMojoOK getMojo() {
            return mojo;
        }

        public void setMojo(EteMojoOK mojo) {
            this.mojo = mojo;
        }

        public abstract TransformationModule newPas(Element data) throws ExceptionTransformation;

        protected Document getDocument(Element elt) throws ParserConfigurationException, SAXException, IOException {
            String nom = elt.getAttribute("nom");
            if (! chemin.endsWith("/") && ! nom.startsWith("/")) {
                nom = '/' + nom;
            }
            nom = chemin + nom;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(nom);
        }

        private EteMojoOK     mojo;

    }       

    private class FabriqueTransformationModule extends FabriqueAbstraiteTransformation {
        
        public TransformationModule newPas(Element data) throws ExceptionTransformation {
            try {
                // a- retrouver le fichier de description du module
                Document doc = getDocument((Element) data);
//                getLog().info("DOCUMENT DU MODULE : " + doc);

                // b- retrouver le noeud "transformation-set"
//                NodeList ensembles = doc.getElementsByTagName(ENSEMBLE_TRANSFORMATION);
                Element elt = (Element) doc.getElementsByTagName(ENSEMBLE_TRANSFORMATION).item(0);

                // c- executer chacun des pas
                return new TransformationModule(elt, getLog(), typesTransformations,
                        base, chemin, outputDirectory);
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(EteMojo.class.getName()).log(Level.SEVERE, null, ex);
                throw new ExceptionTransformation(ex);
            }
        }

        // TODO : Supprimer
        @Override
        public void setMojo(EteMojo inMojo) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }



    }


    //========================================================================//
    // T R A N S F O R M A T I O N   C O M P L E X E
    //========================================================================//

    private static abstract class TransformationComplexe extends TransformationAbstraite {


        private     Map<String, FabriquePas> typesTransformations;
        private     File    base;
        private     String  chemin;
        private     File    outputDirectory;


        public TransformationComplexe(Map<String, FabriquePas> typesTransformations, File base, String chemin, File outputDirectory) {
            this.typesTransformations = typesTransformations;
            this.base = base;
            this.chemin = chemin;
            this.outputDirectory = outputDirectory;
        }


        protected Document executeLesPas(Document modele, Element transformation) throws ExceptionTransformation {
            NodeList noeudsPas = transformation.getElementsByTagName("pas")
                                        .item(0).getChildNodes();
            return executeLesPas(modele, noeudsPas);
        }


        protected Document executeLesPas(Document modele, NodeList noeudsPas) throws ExceptionTransformation {
            for (int i=0 ; i<noeudsPas.getLength() ; i++) {
                Node noeudDeCePas = noeudsPas.item(i);
                if (! (noeudDeCePas instanceof Element)) {
                    continue;
                }
                modele = executeUnPas(modele, (Element)noeudDeCePas);
            }    //  parcours de la boucle des pas
            return modele;
        }


        protected Document executeUnPas(Document inModele, Element pas) throws ExceptionTransformation {
            String type = pas.getNodeName();
            FabriquePas fabrique = typesTransformations.get(type);
            try {
                fabrique.setMojo(localThis.get());
                Transformation transformation = fabrique.newPas(pas);
                File dossierMda = new File(base, chemin);
                URL urlSource = dossierMda.toURI().toURL();
                String urlSourceAsString = urlSource.toString();
                transformation.setUrlSource(urlSource);
                URL urlCible = outputDirectory.toURI().toURL();
                transformation.setUrlCible(urlCible);
                Map<String, String> parametres = new HashMap<String, String>();
                // Valeurs par défaut des paramètres "standard"
                parametres.put("dossier_cible", urlCible.toString());
                NamedNodeMap attributes = pas.getAttributes();
                for (int i = 0 ; i < attributes.getLength() ; i++) {
                    Node item = attributes.item(i);
                    parametres.put(item.getNodeName(), item.getNodeValue());
                }
                String nomTemplate = parametres.get("template");
                if (null != nomTemplate) {
                    int indiceSlash = nomTemplate.lastIndexOf('/');
                    String cheminTemplate = nomTemplate.substring(0, indiceSlash+1);
                    parametres.put("workingdir", urlSourceAsString + cheminTemplate);
                }
                // Paramètres fournis dans le fichier de configuration
                ajouteParametres(parametres, pas);

                transformation.setParametres(parametres);
                inModele = transformation.transforme(inModele);
            } catch (ExceptionTransformation ex) {
                Logger.getLogger(EteMojo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(EteMojo.class.getName()).log(Level.SEVERE, null, ex);
            }
            return inModele;
        }       // executeUnPas


        protected void ajouteParametres(Map<String, String> inoutParametres, Element pas) {
            // Ajout des paramètres définis dans le POM
            if (projetStatique != null) {
                Properties pomProperties = projetStatique.getProperties();
                for (Object clef : pomProperties.keySet()) {
                    Object valeur = pomProperties.get(clef);
                    if (clef instanceof String && valeur instanceof String) {
                       inoutParametres.put((String)clef, (String)pomProperties.get(clef));
                    }
                }
            }
            // Ajout des paramètres de la configuration du pas de transformation
            NodeList enfants = pas.getChildNodes();
            for (int i=0 ; i<enfants.getLength() ; i++) {
                Node suivant = enfants.item(i);
                if (suivant.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element elt = (Element) suivant;
                String nomParam = elt.getNodeName();
                NodeList noeuds = elt.getChildNodes();
                if (noeuds.getLength() != 1) {
                    continue;
                }
                Node noeud = noeuds.item(0);
                String valeur = noeud.getNodeValue().trim();
                inoutParametres.put(nomParam, valeur);
            }   // for
        }   // ajouteParametres

    };      // TransformationComplexe


    //========================================================================//
    // T R A N S F O R M A T I O N   M O D U L E
    //========================================================================//


    private static class TransformationModule extends TransformationComplexe {

        private     Log     log;
        private     Element elt;
        private     String  nom;

        public TransformationModule(Element elt, Log log, Map<String, FabriquePas> typesTransformations, File base, String chemin, File outputDirectory) {
            super(typesTransformations, base, chemin, outputDirectory);
            this.elt = elt;
            this.log = log;
            this.nom = elt.getAttribute("nom");
        }

        public Document transforme(Document doc) throws ExceptionTransformation {
            return executeLesPas(doc, elt);
        }

        public String getNomTransfo() {
            return "Transformation d'un module anonyme";
        }
        
    }       // classe TransformationModule


    //========================================================================//
    // F A B R I Q U E   T R A N S F O R M A T I O N   I F                    //
    //========================================================================//
    // Un element if dans le fichier de configuration permet de ne faire      //
    // certaines transformations que sous condition                           //
    // Par exemple copier des librairies.                                     //
    // TODO : double emploi avec les profils Maven ?                          //
    //========================================================================//
    


    private class FabriqueTransformationIf implements FabriquePas<TransformationIf> {

        public FabriqueTransformationIf(EteMojoOK mojo) {
            this.mojo = mojo;
        }

        @Override
        public TransformationIf newPas(Element data) throws ExceptionTransformation {            
            Element elt = (Element)data;
            String attribute = elt.getAttribute("test");
            if (attribute != null) {
                // TODO : évaluer l'attribut
                System.out.println("Evaluation de l'attribut test : " + attribute);
                ExpressionFactory factory = ExpressionFactory.newInstance();
                
                EteELResolver resolver = new EteELResolver(null);
                
            }
            return new TransformationIf(elt.getChildNodes(), typesTransformations,
                    base, chemin, outputDirectory);
        }

        public EteMojoOK getMojo() {
            return mojo;
        }

        public void setMojo(EteMojoOK mojo) {
            this.mojo = mojo;
        }


        EteMojoOK     mojo;

        // TODO : supprimer
        @Override
        public void setMojo(EteMojo inMojo) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }


    private static class EteELResolver extends ELResolver {

        public EteELResolver(Map<Object, Object> userMap) {
            this.userMap = userMap;
        }

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            if(base==null) {
                base = userMap;
            }
            return delegate.getValue(context, base, property);
        }

        @Override
        public Class<?> getType(ELContext elc, Object o, Object o1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setValue(ELContext elc, Object o, Object o1, Object o2) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isReadOnly(ELContext elc, Object o, Object o1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elc, Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Class<?> getCommonPropertyType(ELContext elc, Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private ELResolver          delegate = new MapELResolver();
        private Map<Object, Object> userMap;

    }       // EteELResolver


    //========================================================================//


    private static class TransformationIf extends TransformationComplexe {

        public TransformationIf(NodeList noeudsPas,
                Map<String, FabriquePas> typesTransformations,
                File base, String chemin, File outputDirectory) {
            super(typesTransformations, base, chemin, outputDirectory);
            this.noeudsPas = noeudsPas;
            this.modele = modele;
        }


        @Override
        public Document transforme(Document doc) throws ExceptionTransformation {
            Document resultat = doc;
            for (int i=0 ; i<noeudsPas.getLength() ; i++) {
                Node noeudDeCePas = noeudsPas.item(i);
                if (! (noeudDeCePas instanceof Element)) {
                    continue;
                }
                modele = executeUnPas(modele, (Element)noeudDeCePas);
            }    //  parcours de la boucle des pas
            return resultat;
        }

        @Override
        public String getNomTransfo() {
            return "if";
        }

        private NodeList        noeudsPas;
        private Document        modele;
    }       // TransformationIf

}   // Classe EteMojo

