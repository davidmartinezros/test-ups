package gov.max.service.file.config.dbmigrations;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates the initial database setup
 */
@ChangeLog(order = "001")
public class InitialMigrationsSetup {


    private Map<String, String>[] authoritiesUser = new Map[]{new HashMap<>()};

    private Map<String, String>[] authoritiesAdminAndUser = new Map[]{new HashMap<>(), new HashMap<>()};

    {
        authoritiesUser[0].put("_id", "ROLE_USER");

        authoritiesAdminAndUser[0].put("_id", "ROLE_USER");
        authoritiesAdminAndUser[1].put("_id", "ROLE_ADMIN");
    }

    @ChangeSet(order = "01", author = "initiator", id = "01-addAuthorities")
    public void addAuthorities(DB db) {
        DBCollection authorityCollection = db.getCollection("max_authority");
        authorityCollection.insert(
                BasicDBObjectBuilder.start()
                        .add("_id", "ROLE_ADMIN")
                        .get());
        authorityCollection.insert(
                BasicDBObjectBuilder.start()
                        .add("_id", "ROLE_USER")
                        .get());
    }
}