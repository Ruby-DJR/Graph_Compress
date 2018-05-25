package main.java.com.company;

import org.apache.jena.rdf.model.*;

public class get_type {

    public static String getType(Model model, Resource subject){
        Property Type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        StmtIterator iter = model.listStatements(subject, Type, (RDFNode) null);
        while (iter.hasNext()){
            Statement stmt = iter.next();
            return stmt.getObject().toString();
        }
        return null;
    }

}
