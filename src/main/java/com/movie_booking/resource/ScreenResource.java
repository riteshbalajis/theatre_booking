package com.movie_booking.resource;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.request.ScreenCreateRequest;
import com.movie_booking.dto.response.CreateResponse;
import com.movie_booking.dto.response.MessageResponse;
import com.movie_booking.dto.response.ScreenResponse;
import com.movie_booking.exception.ScreenNotFoundException;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.model.Screen;
import com.movie_booking.service.ScreenService;
import com.movie_booking.service.ScreenServiceImpl;

@Path("/screens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ScreenResource {

    private ScreenService screenService;
    public ScreenResource() {
        this.screenService = new ScreenServiceImpl();
    }


    @GET
    public List<ScreenResponse> getScreens() throws SQLException {

        List<Screen> screens;
        screens = screenService.getAllScreens();
        return screens.stream()
                .map(screen -> toScreenResponse(screen))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/")
    public List<ScreenResponse> getScreensWithTrailingSlash() throws SQLException {
        return getScreens();
    }

    @GET
    @Path("/{screenId}")
    public ScreenResponse getScreen(@PathParam("screenId") int screenId) throws SQLException {
        Screen screen = screenService.getScreenById(screenId);
        if (screen == null) {
            throw new ScreenNotFoundException("Screen not found");
        }
        return toScreenResponse(screen);
    }


    @POST
    public CreateResponse addScreen(ScreenCreateRequest request) throws SQLException {
        Screen screen = toScreen(request);
        int screenId = screenService.addScreen(screen, getAuthenticatedUserId());
        return new CreateResponse(screenId, "Screen added successfully");
    }

    @POST
    @Path("/")
    public CreateResponse addScreenWithTrailingSlash(ScreenCreateRequest request) throws SQLException {
        return addScreen(request);
    }

    @PUT
    @Path("/{screenId}")
    public Response updateScreen(@PathParam("screenId") int screenId, ScreenCreateRequest request) throws SQLException {
        Screen screen = toScreen(request);
        screen.setScreenId(screenId);
        if (!screenService.updateScreen(screen, getAuthenticatedUserId())) {
            throw new ScreenNotFoundException("Screen not found");
        }
        return Response.ok(new MessageResponse("Screen updated successfully")).build();
    }

    @DELETE
    @Path("/{screenId}")
    public Response deleteScreen(@PathParam("screenId") int screenId) throws SQLException {
        if (!screenService.deactivateScreen(screenId, getAuthenticatedUserId())) {
            throw new ScreenNotFoundException("Screen not found");
        }
        return Response.ok(new MessageResponse("Screen deleted successfully")).build();
                
    }




    private Screen toScreen(ScreenCreateRequest screenCreateRequest) {
        Screen screen = new Screen();
        screen.setTheatreId(screenCreateRequest.getTheatreId());
        screen.setName(screenCreateRequest.getName());
        screen.setCapacity(screenCreateRequest.getCapacity());
        return screen;
                
    }


    @Context
    private HttpServletRequest httpRequest;


    private int getAuthenticatedUserId(){
        
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthorizedException("User is not logged in.");
        }
        return (int) session.getAttribute("userId");
    }
    





    public ScreenResponse toScreenResponse(Screen screen) {
        
        ScreenResponse response = new ScreenResponse();
        response.setScreenId(screen.getScreenId());
        response.setTheatreId(screen.getTheatreId());
        response.setName(screen.getName());
        response.setCapacity(screen.getCapacity());
        response.setStatus(screen.getStatus());
        return response;
    }


    
}
