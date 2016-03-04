package gov.max.service.file.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@PropertySource({"classpath:data-source.properties"})
@ComponentScan(basePackages = {"gov.max.service.file.services"})
@EnableMongoRepositories(basePackages = "gov.max.service.file.domain.repositories")
public class DataSourceConfig extends AbstractMongoConfiguration {

	@Autowired
	private Environment env;

	@Override
	public String getDatabaseName(){
		return env.getRequiredProperty("mongo.name");
	}

	@Override
	@Bean
	public Mongo mongo() throws Exception {
		ServerAddress serverAddress = new ServerAddress(env.getRequiredProperty("mongo.host"));
//		List<MongoCredential> credentials = new ArrayList<>();
//		return new MongoClient(serverAddress, credentials);
		return new MongoClient(serverAddress);
	}

}
