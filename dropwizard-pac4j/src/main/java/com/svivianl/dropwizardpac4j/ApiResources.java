package com.svivianl.dropwizardpac4j;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jax.rs.annotations.Pac4JCallback;
import org.pac4j.jax.rs.annotations.Pac4JLogout;
import org.pac4j.jax.rs.annotations.Pac4JProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ApiResources {
    private static final Logger logger = LoggerFactory.getLogger(ApiResources.class);

    @GET
    @Path("/user")
    public ResponseEntity<?> getUser(
            @Pac4JProfile Optional<CommonProfile> user,
             Principal principal // ,
            // ServletRequest request
    ) {
        System.out.print("--------------------- [GET /users] Principal principal: " + principal);
        System.out.print("\n --------------------- [GET /users] Optional<CommonProfile> user: " + user + "\n");

        if (user == null) {
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return ResponseEntity.ok().body(user);
        }
    }

    @GET
    @Path("/groups")
    public List<Object> getGroups(Principal principal) {
        return new ArrayList<>();
    }

}
