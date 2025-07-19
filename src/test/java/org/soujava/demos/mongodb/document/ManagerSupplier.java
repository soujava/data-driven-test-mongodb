package org.soujava.demos.mongodb.document;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfiguration;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentManager;

import java.util.function.Supplier;

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
public class ManagerSupplier implements Supplier<MongoDBDocumentManager> {

    @Produces
    public MongoDBDocumentManager get() {
        Settings settings = Settings.builder().put("credential", "value").build();
        MongoDBDocumentConfiguration configuration = new MongoDBDocumentConfiguration();
        var factory = configuration.apply(settings);
        return factory.apply("database");
    }
}