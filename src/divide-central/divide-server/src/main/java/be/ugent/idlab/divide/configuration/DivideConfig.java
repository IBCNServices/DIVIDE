package be.ugent.idlab.divide.configuration;

import be.ugent.idlab.divide.configuration.legacy.DivideQueryAsRspQlOrSparqlConfig;
import be.ugent.idlab.divide.configuration.legacy.DivideQueryConfig;
import be.ugent.idlab.divide.configuration.util.CustomJsonConfiguration;
import be.ugent.idlab.divide.rsp.RspQueryLanguage;
import be.ugent.idlab.kb.jena3.KnowledgeBaseType;
import be.ugent.idlab.util.io.IOUtilities;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Configuration of the DIVIDE server (including engine, DIVIDE API
 * and Knowledge Base API configuration parameters).
 */
public class DivideConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DivideConfig.class);

    private static final String SERVER_HOST = "server.host";
    private static final String SERVER_PORT_DIVIDE = "server.port.divide";
    private static final String SERVER_PORT_KB = "server.port.kb";

    private static final String DIVIDE_KB_TYPE = "divide.kb.type";
    private static final String DIVIDE_KB_BASE_IRI = "divide.kb.baseIri";

    private static final String DIVIDE_ENGINE_STOP_RSP_ENGINE_STREAMS_ON_CONTEXT_CHANGES =
            "divide.engine.stopRspEngineStreamsOnContextChanges";
    private static final String DIVIDE_ENGINE_PARSER_PROCESS_UNMAPPED_VARIABLE_MATCHES =
            "divide.engine.parser.processUnmappedVariableMatches";
    private static final String DIVIDE_ENGINE_PARSER_VALIDATE_UNBOUND_VARIABLES_IN_RSP_QL_QUERY_BODY =
            "divide.engine.parser.validateUnboundVariablesInRspQlQueryBody";

    private static final String DIVIDE_REASONER_HANDLE_TBOX_DEFINITIONS_IN_CONTEXT =
            "divide.reasoner.handleTboxDefinitionsInContext";

    private static final String DIVIDE_ONTOLOGY_DIRECTORY = "divide.ontology.dir";
    private static final String DIVIDE_ONTOLOGY_FILES = "divide.ontology.files";

    private static final String DIVIDE_QUERIES_DIVIDE = "divide.queries.divide";
    private static final String DIVIDE_QUERIES_SPARQL = "divide.queries.sparql";
    private static final String DIVIDE_QUERIES_RSP_QL = "divide.queries.rspql";

    private static final String MONITOR_ACTIVE = "monitor.active";
    private static final String MONITOR_TASK_QUERIES = "monitor.task_queries";
    private static final String MONITOR_LOCAL_MONITOR_JAR_PATH = "monitor.local_monitor_jar";

    private static final String CENTRAL_RSP_ENGINE_ACTIVE = "central_rsp_engine.active";
    private static final String CENTRAL_RSP_ENGINE_QUERY_LANGUAGE = "central_rsp_engine.query_language";
    private static final String CENTRAL_RSP_ENGINE_SERVER_PROTOCOL = "central_rsp_engine.server.protocol";
    private static final String CENTRAL_RSP_ENGINE_SERVER_HOST = "central_rsp_engine.server.host";
    private static final String CENTRAL_RSP_ENGINE_SERVER_PORT = "central_rsp_engine.server.port";
    private static final String CENTRAL_RSP_ENGINE_SERVER_WS_STREAM_PORT = "central_rsp_engine.server.ws_stream_port";

    private static final String DEVICE_NETWORK_IP = "device.network_ip";

    private final JSONConfiguration config;
    private final String configFileDirectory;

    private DivideConfig(String propertiesFilePath) throws ConfigurationException, FileNotFoundException {
        config = new CustomJsonConfiguration(propertiesFilePath);
        configFileDirectory = new File(propertiesFilePath)
                .getAbsoluteFile().getParentFile().getAbsolutePath();
    }

    /**
     * Creates a DIVIDE config object based on the given properties file.
     *
     * @param propertiesFile path to properties file
     * @return an instantiated DIVIDE config object which can be used to retrieve
     *         the configuration parameters of the DIVIDE server
     * @throws ConfigurationException if the properties file is invalid
     * @throws FileNotFoundException if the properties file does not exist
     *
     */
    public static DivideConfig getInstance(String propertiesFile)
            throws ConfigurationException, FileNotFoundException {
        return new DivideConfig(propertiesFile);
    }

    /**
     * @return host on which the DIVIDE API & Knowledge Base API should be exposed
     *         (default: 'localhost')
     */
    public String getHost() {
        return config.getString(SERVER_HOST, "localhost");
    }

    /**
     * @return port on which the DIVIDE API should be exposed (default: 5000)
     */
    public int getDivideServerPort() {
        return config.getInt(SERVER_PORT_DIVIDE, 5000);
    }

    /**
     * @return port on which the Knowledge Base API should be exposed
     *         (default: 5001)
     */
    public int getKnowledgeBaseServerPort() {
        return config.getInt(SERVER_PORT_KB, 5001);
    }

    /**
     * @return {@link KnowledgeBaseType} representing the type of knowledge base
     *         that should be instantiated for the DIVIDE engine
     *         (default: {@link KnowledgeBaseType#JENA})
     */
    public KnowledgeBaseType getKnowledgeBaseType() {
        KnowledgeBaseType defaultType = KnowledgeBaseType.JENA;
        KnowledgeBaseType configType =
                KnowledgeBaseType.fromString(config.getString(DIVIDE_KB_TYPE));
        return configType != null ? configType : defaultType;
    }

    /**
     * @return base IRI of the knowledge base that should be used for resolving
     *         IRIs used within the context of the started DIVIDE engine
     *         (default: 'http://idlab.ugent.be/divide')
     */
    public String getBaseIriOfKnowledgeBase() {
        return config.getString(DIVIDE_KB_BASE_IRI, "http://idlab.ugent.be/divide");
    }

    /**
     * @return whether DIVIDE should pause RSP engine streams on a component
     *         when context changes are detected that trigger the DIVIDE query
     *         derivation for that component (default: true)
     */
    public boolean shouldStopRspEngineStreamsOnContextChanges() {
        return config.getBoolean(DIVIDE_ENGINE_STOP_RSP_ENGINE_STREAMS_ON_CONTEXT_CHANGES, true);
    }

    /**
     * @return whether the DIVIDE query parser should process unmapped variable matches in the
     *         query input (i.e., identical variable names occurring in both the stream and
     *         final query, that are not explicitly defined in the variable mapping file of
     *         the input of that query) - if true, matching variable names are considered as
     *         variable mappings; if false, matching variable names are considered coincidence
     *         and are not treated as mappings (default: false)
     */
    public boolean shouldProcessUnmappedVariableMatchesInParser() {
        return config.getBoolean(DIVIDE_ENGINE_PARSER_PROCESS_UNMAPPED_VARIABLE_MATCHES, false);
    }

    /**
     * @return whether the DIVIDE query parser should validate variables in the RSP-QL query
     * body generated by the DIVIDE query parser, should be validated (= checked for occurrence
     * in the WHERE clause of the query or in the set of input variables that will be substituted
     * during the DIVIDE query derivation) during parsing (default: true)
     */
    public boolean shouldValidateUnboundVariablesInRspQlQueryBodyInParser() {
        return config.getBoolean(
                DIVIDE_ENGINE_PARSER_VALIDATE_UNBOUND_VARIABLES_IN_RSP_QL_QUERY_BODY, true);
    }

    /**
     * @return whether DIVIDE should allow to specify TBox definitions in the
     *         context updates sent for the query derivation; if true, this means
     *         that DIVIDE should scan all context updates for new OWL-RL axioms
     *         and rules upon each query derivation call, heavily impacting the
     *         derivation of queries upon context updates (default: false)
     */
    public boolean shouldHandleTBoxDefinitionsInContext() {
        return config.getBoolean(DIVIDE_REASONER_HANDLE_TBOX_DEFINITIONS_IN_CONTEXT, false);
    }

    /**
     * @return list of canonical path names of files containing the ontology (TBox) data
     *         used by this DIVIDE engine (default: empty list)
     */
    public List<String> getDivideOntologyFilePaths() {
        String ontologyDirectoryPath = config.getString(DIVIDE_ONTOLOGY_DIRECTORY, ".");
        if (ontologyDirectoryPath != null && !Paths.get(ontologyDirectoryPath).isAbsolute()) {
            ontologyDirectoryPath = Paths.get(configFileDirectory, ontologyDirectoryPath).toString();
        }

        String[] ontologyFileNames = config.getStringArray(DIVIDE_ONTOLOGY_FILES);

        List<String> ontologyFilePaths = new ArrayList<>();
        for (String ontologyFileName : ontologyFileNames) {
            try {
                File ontologyFile = new File(ontologyFileName);
                if (!ontologyFile.isAbsolute()) {
                    ontologyFile = new File(ontologyDirectoryPath, ontologyFileName);
                }

                String canonicalPath = ontologyFile.getCanonicalPath();
                if (IOUtilities.isValidFile(canonicalPath)) {
                    ontologyFilePaths.add(canonicalPath);
                } else {
                    throw new IOException(String.format("%s is not a valid file", canonicalPath));
                }
            } catch (IOException e) {
                LOGGER.error("Error with finding ontology file {}", ontologyFileName, e);
                throw new RuntimeException(e);
            }
        }

        return ontologyFilePaths;
    }

    /**
     * @return list of path names of files that each specify the properties of a
     *         single DIVIDE query that should be loaded into the DIVIDE engine;
     *         these properties can be read with the {@link DivideQueryConfig} class
     *         (default: empty list)
     */
    public List<String> getDivideQueryPropertiesFiles() {
        String[] queries = config.getStringArray(DIVIDE_QUERIES_DIVIDE);
        if (queries == null) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(queries)
                    .filter(Objects::nonNull)
                    .map(path -> !Paths.get(path).isAbsolute() ?
                            Paths.get(configFileDirectory, path).toString() : path)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @return list of path names of files that each specify the properties of a
     *         single DIVIDE query that should be loaded into the DIVIDE engine;
     *         the specification is based on a series of 1 or more chained SPARQL queries;
     *         these properties can be read with the {@link DivideQueryAsRspQlOrSparqlConfig}
     *         class (default: empty list)
     */
    public List<String> getDivideQueryAsSparqlPropertiesFiles() {
        String[] queries = config.getStringArray(DIVIDE_QUERIES_SPARQL);
        if (queries == null) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(queries)
                    .filter(Objects::nonNull)
                    .map(path -> !Paths.get(path).isAbsolute() ?
                            Paths.get(configFileDirectory, path).toString() : path)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @return list of path names of files that each specify the properties of a
     *         single DIVIDE query that should be loaded into the DIVIDE engine;
     *         the specification is based on a single RSP-QL query;
     *         these properties can be read with the {@link DivideQueryAsRspQlOrSparqlConfig}
     *         class (default: empty list)
     */
    public List<String> getDivideQueryAsRspQlPropertiesFiles() {
        String[] queries = config.getStringArray(DIVIDE_QUERIES_RSP_QL);
        if (queries == null) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(queries)
                    .filter(Objects::nonNull)
                    .map(path -> !Paths.get(path).isAbsolute() ?
                            Paths.get(configFileDirectory, path).toString() : path)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @return whether the DIVIDE monitor should be active (default: false)
     */
    public boolean shouldMonitorBeActivated() {
        return config.getBoolean(MONITOR_ACTIVE, false);
    }

    /**
     * @return list of path names of files that each contain a task query for
     *         the DIVIDE monitor (default: empty list)
     */
    public List<String> getMonitorTaskQueries() {
        String[] queries = config.getStringArray(MONITOR_TASK_QUERIES);
        if (queries == null) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(queries)
                    .filter(Objects::nonNull)
                    .map(path -> !Paths.get(path).isAbsolute() ?
                            Paths.get(configFileDirectory, path).toString() : path)
                    .collect(Collectors.toList());
        }
    }

    /**
     * @return path of JAR file that can be used to remotely deploy the local monitor
     *         (default: null)
     */
    public String getLocalMonitorJarPath() {
        return config.getString(MONITOR_LOCAL_MONITOR_JAR_PATH, null);
    }

    /**
     * @return whether the DIVIDE engine works with a central RSP engine, allowing
     *         queries to be moved between the local engines and this central engine
     *         (default: false)
     */
    public boolean hasCentralRspEngine() {
        return config.getBoolean(CENTRAL_RSP_ENGINE_ACTIVE, false);
    }

    /**
     * @return {@link RspQueryLanguage} representing the query language of
     *         the central RSP engine associated to the DIVIDE engine
     *         (default: {@link RspQueryLanguage#RSP_QL})
     */
    public RspQueryLanguage getCentralRspEngineQueryLanguage() {
        RspQueryLanguage defaultType = RspQueryLanguage.RSP_QL;
        RspQueryLanguage configType =
                RspQueryLanguage.fromString(config.getString(CENTRAL_RSP_ENGINE_QUERY_LANGUAGE));
        return configType != null ? configType : defaultType;
    }

    /**
     * @return Protocol of the API server of the central RSP engine
     *         (default: null)
     */
    public String getCentralRspEngineServerProtocol() {
        return config.getString(CENTRAL_RSP_ENGINE_SERVER_PROTOCOL, null);
    }

    /**
     * @return Host of the API server of the central RSP engine
     *         (default: device network IP)
     */
    public String getCentralRspEngineServerHost() {
        return config.getString(CENTRAL_RSP_ENGINE_SERVER_HOST, getDeviceNetworkIp());
    }

    /**
     * @return Port of the API server of the central RSP engine
     *         (default: -1)
     */
    public int getCentralRspEngineServerPort() {
        return config.getInt(CENTRAL_RSP_ENGINE_SERVER_PORT, -1);
    }

    /**
     * @return Port of the WebSocket stream server of the central RSP engine
     *         (default: -1)
     */
    public int getCentralRspEngineServerWebSocketStreamPort() {
        return config.getInt(CENTRAL_RSP_ENGINE_SERVER_WS_STREAM_PORT, -1);
    }

    public String getDeviceNetworkIp() {
        return config.getString(DEVICE_NETWORK_IP, null);
    }

}
