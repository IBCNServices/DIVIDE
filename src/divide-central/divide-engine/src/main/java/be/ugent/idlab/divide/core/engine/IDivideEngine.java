package be.ugent.idlab.divide.core.engine;

import be.ugent.idlab.divide.core.component.IComponent;
import be.ugent.idlab.divide.core.context.ContextEnrichment;
import be.ugent.idlab.divide.core.exception.DivideInitializationException;
import be.ugent.idlab.divide.core.exception.DivideInvalidInputException;
import be.ugent.idlab.divide.core.exception.DivideNotInitializedException;
import be.ugent.idlab.divide.core.exception.DivideQueryDeriverException;
import be.ugent.idlab.divide.core.query.IDivideQuery;
import be.ugent.idlab.divide.core.query.parser.IDivideQueryParser;
import be.ugent.idlab.divide.monitor.IDivideGlobalMonitor;
import be.ugent.idlab.divide.monitor.MonitorException;
import be.ugent.idlab.divide.monitor.metamodel.IDivideMetaModel;
import be.ugent.idlab.divide.rsp.RspQueryLanguage;
import be.ugent.idlab.kb.IKnowledgeBase;
import org.apache.jena.rdf.model.Model;

import java.util.Collection;
import java.util.List;

/**
 * Main engine of DIVIDE.
 *
 * Takes care of the handling of DIVIDE queries, and contains references to
 * instances of {@link IDivideQueryDeriver} for the query derivation.
 *
 * It also is responsible for managing the components within the DIVIDE system.
 * It keeps track of a collection of registered {@link IComponent} instances,
 * and provides methods to register, unregister and retrieve them.
 * It registers itself as observer to the {@link IKnowledgeBase<Model>} of
 * this engine, to trigger the query derivation for the relevant components
 * on context updates.
 */
public interface IDivideEngine {

    /**
     * Initializes the DIVIDE engine.
     * Keeps track of the given {@link IDivideQueryDeriver} to be used for the
     * query derivation when relevant knowledge base changes are observed.
     * Changes to the knowledge base are only observed after this initialization
     * method has successfully returned.
     * Loads the ontology to the engine based on the list of ontology files.
     * Creates a DIVIDE component manager to manage the DIVIDE components of this
     * engine, and observe changes of the given knowledge base.
     *
     * @param divideQueryDeriver {@link IDivideQueryDeriver} used for the query derivation
     *                           performed by this engine
     * @param knowledgeBase {@link IKnowledgeBase<Model>} that should be observed for changes
     *                      to know when the query derivation should be triggered
     * @param divideOntology model representing all statements in the ontology that is used
     *                       by DIVIDE for the query derivation, i.e., in the TBox of the
     *                       knowledge base
     * @param pauseRspEngineStreamsOnContextChanges boolean representing whether RSP engine
     *                                              streams on a component should be paused
     *                                              when context changes are detected that
     *                                              trigger the DIVIDE query derivation for
     *                                              that component
     * @param processUnmappedVariableMatchesInParser boolean representing whether variable
     *                                               matches in the input for the DIVIDE query
     *                                               parser that are not defined as mappings,
     *                                               should be considered as mappings by default
     * @param validateUnboundVariablesInRspQlQueryBodyInParser boolean representing whether variables
     *                                                         in the RSP-QL query body generated by
     *                                                         the DIVIDE query parser, should be
     *                                                         validated (= checked for occurrence in
     *                                                         the WHERE clause of the query or in the
     *                                                         set of input variables that will be
     *                                                         substituted during the DIVIDE query
     *                                                         derivation) during parsing
     * @throws DivideInitializationException if something goes wrong during the initialization
     *                                       process, which prevents the DIVIDE engine from
     *                                       functioning as it should
     * @throws DivideInvalidInputException when the specified ontology contains invalid
     *                                     statements, i.e., statements which cannot be loaded
     *                                     by the query deriver
     */
    void initialize(IDivideQueryDeriver divideQueryDeriver,
                    IKnowledgeBase<Model> knowledgeBase,
                    Model divideOntology,
                    boolean pauseRspEngineStreamsOnContextChanges,
                    boolean processUnmappedVariableMatchesInParser,
                    boolean validateUnboundVariablesInRspQlQueryBodyInParser)
            throws DivideInitializationException, DivideInvalidInputException;

    /**
     * Activates the DIVIDE monitoring.
     * Initializes and starts the given DIVIDE Global Monitor, and uses the Local Monitor JAR
     * on the provided path to remotely deploy the Local Monitor to all known DIVIDE components.
     *
     * @param divideGlobalMonitor instance of the DIVIDE Global Monitor to run on the DIVIDE
     *                            engine server
     * @param divideLocalMonitorJarPath path to the JAR file of the DIVIDE Local Monitor which can be
     *                                  used to remotely deploy to all known DIVIDE components
     */
    void activateMonitor(IDivideGlobalMonitor divideGlobalMonitor,
                         String divideLocalMonitorJarPath,
                         String deviceNetworkIp)
            throws DivideNotInitializedException, DivideInvalidInputException, MonitorException;

    /**
     * Updates the central RSP engine to be used by DIVIDE.
     * If a central RSP engine is configured, queries can be moved between the local RSP engines
     * of the DIVIDE components and this central RSP engine, e.g. triggered by the Monitor.
     *
     * For now, this method can only be called once during the lifetime of the DIVIDE server.
     *
     * @param rspQueryLanguage RSP query language used by the central RSP engine
     * @param serverHost host/IP which should be used for communication with the central RSP engine
     * @param serverPort port of the server API of the central RSP engine
     * @param webSocketServerPort port of the WebSocket server to which input stream data can be sent
     * @throws DivideInvalidInputException if the RSP engine URL is no valid URL
     * @throws DivideInitializationException if a central RSP engine has already been configured
     */
    void configureCentralRspEngine(RspQueryLanguage rspQueryLanguage,
                                   String serverProtocol,
                                   String serverHost,
                                   int serverPort,
                                   int webSocketServerPort)
            throws DivideInvalidInputException, DivideInitializationException;

    /**
     * Register a new DIVIDE query to this DIVIDE engine.
     * The required format and language of the input parameters depends on the type
     * of query deriver used - if any of the input parameters is invalid according
     * to this query deriver, a {@link DivideInvalidInputException} will be thrown.
     *
     * @param name name of the new DIVIDE query
     * @param queryPattern generic RSP-QL query pattern of this query
     * @param sensorQueryRule sensor query rule to be used for the query derivation
     * @param goal goal to be used for the query derivation
     * @return the newly created {@link IDivideQuery} that is registered to
     *         the DIVIDE engine (or null if a DIVIDE query with the given name
     *         is already registered to the engine)
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     * @throws DivideQueryDeriverException when something goes wrong when registering the new
     *                                     DIVIDE query to the {@link IDivideQueryDeriver} of this
     *                                     engine, which prevents it from performing the query
     *                                     derivation for this query - this error has nothing to
     *                                     do with invalid parameters
     * @throws DivideInvalidInputException when the registration fails because any of the new
     *                                     DIVIDE query parameters is invalid
     */
    IDivideQuery addDivideQuery(String name,
                                String queryPattern,
                                String sensorQueryRule,
                                String goal,
                                ContextEnrichment contextEnrichment)
            throws DivideNotInitializedException, DivideQueryDeriverException, DivideInvalidInputException;

    /**
     * Removes an {@link IDivideQuery} with the given name from the list
     * of queries registered to this DIVIDE engine.
     *
     * @param name name of query to remove from the DIVIDE engine (if no query
     *             with the given name is registered, nothing is done)
     * @param unregisterQueries specifies whether all queries associated to this
     *                          DIVIDE query should be unregistered on the components
     *                          currently known by the system
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    void removeDivideQuery(String name,
                           boolean unregisterQueries) throws DivideNotInitializedException;

    /**
     * Retrieve list of {@link IDivideQuery} instances registered to this
     * DIVIDE engine.
     *
     * @return list of DIVIDE queries registered to the engine
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    Collection<IDivideQuery> getDivideQueries() throws DivideNotInitializedException;

    /**
     * Retrieve {@link IDivideQuery} with the given name that is registered
     * to this DIVIDE engine.
     *
     * @param name name of the DIVIDE query to retrieve
     * @return the {@link IDivideQuery} registered to the DIVIDE engine with the
     *         given name (null if no query with that name is registered)
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    IDivideQuery getDivideQueryByName(String name) throws DivideNotInitializedException;

    /**
     * Creates and registers a new {@link IComponent}.
     * After successful registration (no exception is thrown or null is returned),
     * this {@link IDivideEngine} performs the following task: when a
     * change to any of the ABox IRIs specified as mainContextIri or
     * additionalContextIris is observed, the query derivation at the associated
     * {@link IDivideEngine} is triggered for this registered component.
     *
     * @param contextIris IRIs of the ABoxes in a knowledge base that represents the
     *                    context associated to the new {@link IComponent}
     * @param localRspEngineQueryLanguage RSP query language used by the RSP engine running on
     *                         the created component
     * @param localRspEngineServerPort URL which should be used for communicating with the RSP engine
     *                     running on the created component, and which will also be mapped
     *                     to a unique ID for the created component
     * @return the new {@link IComponent} that is registered (or null if a component
     *         is already registered with the specified rspEngineUrl)
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     * @throws DivideInvalidInputException if any of the specified context IRIs is not valid
     *                                     (either the mainContextIri or an entry of the
     *                                     additionalContextIris list), OR if the
     *                                     rspEngineUrl is no valid URL
     */
    IComponent registerComponent(String ipAddress,
                                 List<String> contextIris,
                                 RspQueryLanguage localRspEngineQueryLanguage,
                                 int localRspEngineServerPort)
            throws DivideNotInitializedException, DivideInvalidInputException;

    /**
     * Unregisters an {@link IComponent} with the given ID.
     * After successful completion of this method (no null is returned), changes to
     * the ABox IRIs in the knowledge base specified as context IRIs of this
     * component do no longer result in triggering the query derivation process for
     * this component.
     *
     * @param id ID of the component to unregister (if no component with the given
     *           ID is registered, nothing is done)
     * @param unregisterQueries specifies whether all queries registered by DIVIDE on
     *                          the RSP engine of this component should be unregistered
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    void unregisterComponent(String id,
                             boolean unregisterQueries) throws DivideNotInitializedException;

    /**
     * Retrieve all {@link IComponent} instances registered to this manager.
     *
     * @return all registered {@link IComponent} instances
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    Collection<IComponent> getRegisteredComponents() throws DivideNotInitializedException;

    /**
     * Retrieve {@link IComponent} with the given ID that is registered
     * to this manager.
     *
     * @param id ID of the {@link IComponent} to retrieve
     * @return the {@link IComponent} registered to this manager with the
     *         given ID (null if no component with that ID is registered)
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    IComponent getRegisteredComponentById(String id) throws DivideNotInitializedException;

    /**
     * Retrieve the {@link IDivideQueryParser} of this DIVIDE engine.
     *
     * @return the DIVIDE query parser of this DIVIDE engine
     */
    IDivideQueryParser getQueryParser();

    /**
     * Update the window parameters of the active queries derived from the given
     * DIVIDE query on the given component.
     *
     * @param componentId ID of the component for which the queries should be updated
     * @param divideQueryName name of the DIVIDE query of which the derived queries
     *                        should be updated
     * @param windowParameters model of the window parameters that should be used for the update
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet
     */
    void updateWindowParameters(String componentId,
                                String divideQueryName,
                                Model windowParameters) throws DivideNotInitializedException;

    /**
     * Update the location of the active queries derived from the given
     * DIVIDE query on the given component.
     *
     * @param componentId ID of the component for which the queries should be updated
     * @param divideQueryName name of the DIVIDE query of which the derived queries
     *                        should be updated
     * @param moveToCentral true if the queries should be moved to the central RSP engine,
     *                      false if they should be moved to the local RSP engine
     * @throws DivideNotInitializedException if {@link #initialize(IDivideQueryDeriver,
     *                                       IKnowledgeBase, Model, boolean, boolean, boolean)}
     *                                       has not been called yet and/or if no central RSP
     *                                       engine has been configured for this DIVIDE engine
     *                                       via the {@link #configureCentralRspEngine(RspQueryLanguage,
     *                                       String, String, int, int)} method
     */
    void updateQueryLocation(String componentId,
                             String divideQueryName,
                             boolean moveToCentral) throws DivideNotInitializedException;

    String getDeviceNetworkIp() throws DivideNotInitializedException;

    String getId();

    /**
     * Method to let the DIVIDE engine know that the DIVIDE Meta Model of the activated
     * DIVIDE monitor has been initialized and can be used by the DIVIDE engine.
     * This method should be called by the DIVIDE Global Monitor after all initialization
     * to the monitor & meta model has been completed.
     */
    void onDivideMetaModelInitialized();

    IDivideMetaModel getDivideMetaModel();

    void shutdown() throws DivideNotInitializedException;

}
