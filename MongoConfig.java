package gov.max.service.file.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.store.Downloader;

import de.flapdoodle.embed.process.store.IArtifactStore;
import de.flapdoodle.embed.process.store.StaticArtifactStoreBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

//@Configuration
public class MongoConfig {

    private final Logger LOG = LoggerFactory.getLogger(MongoConfig.class);

    private static final String MONGO_DB_URL = "localhost";
    private static final int MONGO_DB_PORT = 8082;

    private MongodExecutable mongodExecutable;

    private
    @Value("${spring.data.location}")
    String dataLocation;

    private
    @Value("${spring.mongo.location:none}")
    String mongoLocation;

    @Bean
    public MongoDbFactory mongoDbFactory() throws UnknownHostException {
        Mongo mongo = new MongoClient(MONGO_DB_URL, MONGO_DB_PORT);
        UserCredentials userCredentials = new UserCredentials("", ""); // username and password
        return new SimpleMongoDbFactory(mongo, "store", userCredentials);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        return new MongoTemplate(mongoDbFactory());
    }

    @PostConstruct
    public void construct() throws IOException {
        if (!mongoLocation.equals("none")) {
            LOG.info("using custom server: " + mongoLocation);

            IRuntimeConfig runtimeConfig = extractedFileSetConfig();
            Storage replication = new Storage(dataLocation, null, 0);
            IMongoCmdOptions mongoCmdOptions = new MongoCmdOptionsBuilder()
                    // Use storage space
                    .useSmallFiles(true)
                    // noprealloc may hurt performance in many applications
                    .useNoPrealloc(false)
                    // set verbose mode at execution (debug purpose).
                    .verbose(false)
                    // Build command Options
                    .defaultSyncDelay()
                    .build();

            IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(Version.V2_6_11)//Version.Main.PRODUCTION)
                    .net(new Net(MONGO_DB_URL, MONGO_DB_PORT, true))
                    .cmdOptions(mongoCmdOptions)
                    .replication(replication)
                    .build();

            mongodExecutable = MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();
        }
    }

    private IRuntimeConfig extractedArchiveConfig() throws IOException {

        File storeFile = new File(mongoLocation);
        IDirectory artifactStorePath;
        if (storeFile.exists() && storeFile.isDirectory() && storeFile.canWrite()) {
            artifactStorePath = new FixedPath(mongoLocation);
        } else {
            //use java tmp dir instead of the default user.home
            artifactStorePath = new FixedPath(System.getProperty("java.io.tmpdir") + "/.embeddedmongo");
        }

        Command command = Command.MongoD;
        ITempNaming executableNaming = new UUIDTempNaming();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                    .defaults(command)
                    .downloader(new Downloader())
                    .download(new DownloadConfigBuilder()
                        .defaultsForCommand(command)
                        .downloadPath(new File(mongoLocation).toURI().toString())
                        .artifactStorePath(artifactStorePath))
                    .executableNaming(executableNaming))
                .build();

        return runtimeConfig;
    }

    private IRuntimeConfig extractedFileSetConfig() throws IOException {
        Distribution distribution= Distribution.detectFor(Version.V2_6_11);
        File baseDir= new File(mongoLocation + "/bin");
        IExtractedFileSet fileSet= ImmutableExtractedFileSet.builder(baseDir)
                .baseDirIsGenerated(false)
                .executable(new File("mongod"))
                .build();

        IArtifactStore store = new StaticArtifactStoreBuilder()
                .fileSet(distribution, fileSet)
                .build();

        Command command = Command.MongoD;
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(store)
                .build();

        return runtimeConfig;
    }

    @PreDestroy
    public void destroy() {
        LOG.info("shutting down mongo server: " + mongoLocation);
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }
}