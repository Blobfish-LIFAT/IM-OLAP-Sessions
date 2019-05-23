
package fr.univ_tours.info.im_olap.data.cubeloadBeans;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.univ_tours.info.im_olap.data.cubeloadBeans package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DateTime_QNAME = new QName("", "Date-Time");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.univ_tours.info.im_olap.data.cubeloadBeans
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MinSessionLength }
     * 
     */
    public MinSessionLength createMinSessionLength() {
        return new MinSessionLength();
    }

    /**
     * Create an instance of {@link GlobalParameters }
     * 
     */
    public GlobalParameters createGlobalParameters() {
        return new GlobalParameters();
    }

    /**
     * Create an instance of {@link NumberOfProfiles }
     * 
     */
    public NumberOfProfiles createNumberOfProfiles() {
        return new NumberOfProfiles();
    }

    /**
     * Create an instance of {@link MaxMeasures }
     * 
     */
    public MaxMeasures createMaxMeasures() {
        return new MaxMeasures();
    }

    /**
     * Create an instance of {@link MinReportSize }
     * 
     */
    public MinReportSize createMinReportSize() {
        return new MinReportSize();
    }

    /**
     * Create an instance of {@link MaxReportSize }
     * 
     */
    public MaxReportSize createMaxReportSize() {
        return new MaxReportSize();
    }

    /**
     * Create an instance of {@link SurprisingQueries }
     * 
     */
    public SurprisingQueries createSurprisingQueries() {
        return new SurprisingQueries();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link GroupBy }
     * 
     */
    public GroupBy createGroupBy() {
        return new GroupBy();
    }

    /**
     * Create an instance of {@link Element }
     * 
     */
    public Element createElement() {
        return new Element();
    }

    /**
     * Create an instance of {@link Hierarchy }
     * 
     */
    public Hierarchy createHierarchy() {
        return new Hierarchy();
    }

    /**
     * Create an instance of {@link Level }
     * 
     */
    public Level createLevel() {
        return new Level();
    }

    /**
     * Create an instance of {@link Predicate }
     * 
     */
    public Predicate createPredicate() {
        return new Predicate();
    }

    /**
     * Create an instance of {@link YearPrompt }
     * 
     */
    public YearPrompt createYearPrompt() {
        return new YearPrompt();
    }

    /**
     * Create an instance of {@link SegregationPredicate }
     * 
     */
    public SegregationPredicate createSegregationPredicate() {
        return new SegregationPredicate();
    }

    /**
     * Create an instance of {@link Measures }
     * 
     */
    public Measures createMeasures() {
        return new Measures();
    }

    /**
     * Create an instance of {@link SelectionPredicates }
     * 
     */
    public SelectionPredicates createSelectionPredicates() {
        return new SelectionPredicates();
    }

    /**
     * Create an instance of {@link SeedQueries }
     * 
     */
    public SeedQueries createSeedQueries() {
        return new SeedQueries();
    }

    /**
     * Create an instance of {@link ProfileParameters }
     * 
     */
    public ProfileParameters createProfileParameters() {
        return new ProfileParameters();
    }

    /**
     * Create an instance of {@link Profile }
     * 
     */
    public Profile createProfile() {
        return new Profile();
    }

    /**
     * Create an instance of {@link Name }
     * 
     */
    public Name createName() {
        return new Name();
    }

    /**
     * Create an instance of {@link MaxSessionLength }
     * 
     */
    public MaxSessionLength createMaxSessionLength() {
        return new MaxSessionLength();
    }

    /**
     * Create an instance of {@link NumberOfSessions }
     * 
     */
    public NumberOfSessions createNumberOfSessions() {
        return new NumberOfSessions();
    }

    /**
     * Create an instance of {@link Benchmark }
     * 
     */
    public Benchmark createBenchmark() {
        return new Benchmark();
    }

    /**
     * Create an instance of {@link Session }
     * 
     */
    public Session createSession() {
        return new Session();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Date-Time")
    public JAXBElement<String> createDateTime(String value) {
        return new JAXBElement<String>(_DateTime_QNAME, String.class, null, value);
    }

}
