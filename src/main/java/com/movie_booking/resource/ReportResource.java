package com.movie_booking.resource;

import java.io.ByteArrayOutputStream;
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
import com.movie_booking.dto.response.MovieTheatreReportResponse;
import com.movie_booking.dto.response.OverallReportResponse;
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
    @Path("/overall")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOverallReport(
            @QueryParam("date") String date)
            throws SQLException {

        LocalDate reportDate = LocalDate.parse(date);

        OverallReportResponse response
                = reportService.getOverallReport(
                        reportDate,
                        getAuthenticatedUserId()
                );

        return Response.ok(response).build();
    }

    @GET
    @Path("/movie_theatre")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMovieTheatreReport(
            @QueryParam("date") String date)
            throws SQLException {

        LocalDate reportDate = LocalDate.parse(date);

        List<MovieTheatreReportResponse> report
                = reportService.getMovieTheatreReport(
                        reportDate,
                        getAuthenticatedUserId()
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfReportGenerator.generateTheatreReportPdf(
                baos,
                reportDate,
                reports
        );

        return Response.ok(baos.toByteArray())
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

        List<MovieReportResponse> reports = reportService.getMovieReport(reportDate, getAuthenticatedUserId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfReportGenerator.generateMovieReportPdf(
                baos,
                reportDate,
                reports
        );

        return Response.ok(baos.toByteArray())
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"movie-report-" + date + ".pdf\""
                )
                .build();
    }

    @GET
    @Path("/overall/pdf")
    @Produces("application/pdf")
    public Response generateOverallReportPdf(@QueryParam("date") String date) throws Exception {

        LocalDate reportDate = LocalDate.parse(date);

        OverallReportResponse report = reportService.getOverallReport(reportDate, getAuthenticatedUserId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfReportGenerator.generateOverallReportPdf(
                baos,
                reportDate,
                report
        );

        return Response.ok(baos.toByteArray())
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"overall-report-" + date + ".pdf\""
                )
                .build();
    }

    @GET
    @Path("/movie_theatre/pdf")
    @Produces("application/pdf")
    public Response generateMovieTheatreReportPdf(
            @QueryParam("date") String date) throws Exception {

        LocalDate reportDate = LocalDate.parse(date);

        List<MovieTheatreReportResponse> reports
                = reportService.getMovieTheatreReport(
                        reportDate,
                        getAuthenticatedUserId()
                );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfReportGenerator.generateMovieTheatreReport(
                reports,
                baos
        );

        return Response.ok(baos.toByteArray())
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"movie-theatre-report-"
                        + date + ".pdf\""
                )
                .build();
    }

    @GET
    @Path("/daily/pdf")
    @Produces("application/pdf")
    public Response generateDailyPdf(
            @QueryParam("date") String date)
            throws Exception {

        int userId = getAuthenticatedUserId();
        LocalDate reportDate = LocalDate.parse(date);

        ByteArrayOutputStream baos
                = reportService.generateDailyReportPdf(reportDate, userId);

        return Response.ok(baos.toByteArray())
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"daily-report-"
                        + date + ".pdf\""
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
