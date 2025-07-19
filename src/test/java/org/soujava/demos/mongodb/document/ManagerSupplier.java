package org.soujava.demos.mongodb.document;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentManager;

import java.util.function.Supplier;

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
public class ManagerSupplier implements Supplier<MongoDBDocumentManager> {

    @Produces
    public MongoDBDocumentManager get() {
        return DocumentDatabase.INSTANCE.get("database");
    }
}