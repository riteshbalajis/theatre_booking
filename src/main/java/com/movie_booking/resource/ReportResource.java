package com.movie_booking.resource;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.dto.response.TheatreReportResponse;
import com.movie_booking.exception.UnauthorizedException;
import com.movie_booking.service.ReportService;
import com.movie_booking.service.ReportServiceImpl;
import com.movie_booking.util.PdfReportGenerator;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {

    private final ReportService reportService;

    public ReportResource() {
        this.reportService = new ReportServiceImpl();
    }

    @GET
    @Path("/theatre")
    public Response getTheatreReport(
            @QueryParam("date") String date)
            throws SQLException {

        int authenticatedUserId = getAuthenticatedUserId();

        LocalDate reportDate = LocalDate.parse(date);

        List<TheatreReportResponse> report
                = reportService.getTheatreReport(
                        reportDate,
                        authenticatedUserId
                );

        return Response.ok(report).build();
    }

    @GET
    @Path("/movie")
    public Response getMovieReport(
            @QueryParam("date") String date)
            throws SQLException {

        int authenticatedUserId = getAuthenticatedUserId();

        LocalDate reportDate = LocalDate.parse(date);

        List<MovieReportResponse> report
                = reportService.getMovieReport(
                        reportDate,
                        authenticatedUserId
                );

        return Response.ok(report).build();
    }

    @GET
    @Path("/theatre/pdf")
    @Produces("application/pdf")
    public Response generateTheatreReportPdf(
            @QueryParam("date") String date) throws Exception {

        LocalDate reportDate = LocalDate.parse(date);

        List<TheatreReportResponse> reports
                = reportService.getTheatreReport(
                        reportDate,
                        getAuthenticatedUserId()
                );

        String filePath
                = "D:/movie_booking/theatre-report-" + date + ".pdf";

        PdfReportGenerator.generateTheatreReportPdf(
                filePath,
                reportDate,
                reports
        );

        File file = new File(filePath);

        return Response.ok(file)
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"theatre-report-" + date + ".pdf\""
                )
                .build();
    }

    @GET
    @Path("/movie/pdf")
    @Produces("application/pdf")
    public Response generateMovieReportPdf(
            @QueryParam("date") String date) throws Exception {

        LocalDate reportDate = LocalDate.parse(date);

        List<MovieReportResponse> reports
                = reportService.getMovieReport(
                        reportDate,
                        getAuthenticatedUserId()
                );

        String filePath
                = "D:/movie_booking/movie-report-" + date + ".pdf";

        PdfReportGenerator.generateMovieReportPdf(
                filePath,
                reportDate,
                reports
        );

        File file = new File(filePath);

        return Response.ok(file)
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"movie-report-" + date + ".pdf\""
                )
                .build();
    }

    @Context
    private HttpServletRequest httpRequest;

    private int getAuthenticatedUserId() {

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null) {

            throw new UnauthorizedException(
                    "User is not logged in."
            );
        }

        return (int) session.getAttribute("userId");
    }
}
