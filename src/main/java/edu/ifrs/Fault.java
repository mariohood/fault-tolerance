package edu.ifrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;



@Path("/fault")
public class Fault {
    int count = 1;
    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Retry(maxRetries = 3, delay = 2000)
    @Fallback(fallbackMethod = "recover")
    @Timeout(7000)
   
    public String getName(@PathParam("name") String name){

        System.out.println("Tentei");

        if (name.equalsIgnoreCase("error")) {
            ResponseBuilderImpl builder = new ResponseBuilderImpl();
            builder.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("The requested was an error");
            Response response = builder.build();
            throw new WebApplicationException(response);
        }

        return name;
    }

    public String recover(String name){
        return "Recuperado";
    }

    @GET
    @Path("/bulkhead/{name}")
    @Produces(MediaType.TEXT_PLAIN)    
    @Bulkhead(2)
    public String bulkhead(@PathParam("name") String name) {
            return name;
    }

    @GET
    @Path("/cb/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, delay = 5000)
    public String cb(@PathParam("name") String name) {
       
        String message = "";
        if(count % 2 == 0) {
            message = "ok PAR";
        } else {
            message = "ok nao par";
            ResponseBuilderImpl builder = new ResponseBuilderImpl();
            builder.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("The requested was an error");
            Response response = builder.build();
            throw new WebApplicationException(response);            
        }
        count++;
        return message;
    }
}
