package com.movie_booking.util;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import com.movie_booking.dto.response.MovieReportResponse;
import com.movie_booking.dto.response.MovieTheatreReportResponse;
import com.movie_booking.dto.response.OverallReportResponse;
import com.movie_booking.dto.response.TheatreReportResponse;

public final class PdfReportGenerator {

    private PdfReportGenerator() {
    }

    // =========================================================
    // COMMON PDF METHODS
    // =========================================================
    private static Document createDocument(String filePath)
            throws Exception {

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(filePath)
        );

        document.open();

        return document;
    }

    private static void addTitle(
            Document document,
            String title,
            LocalDate date) throws Exception {

        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                18
        );

        Paragraph titleParagraph
                = new Paragraph(title, titleFont);

        titleParagraph.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(titleParagraph);

        Paragraph dateParagraph
                = new Paragraph("Date: " + date);

        dateParagraph.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(dateParagraph);

        document.add(new Paragraph(" "));
    }

    private static void addHeaderCell(
            PdfPTable table,
            String text) {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                10
        );

        PdfPCell cell = new PdfPCell(
                new Phrase(text, font)
        );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        table.addCell(cell);
    }

    // =========================================================
    // 1. THEATRE REPORT
    // =========================================================
    public static void generateTheatreReportPdf(
            String filePath,
            LocalDate date,
            List<TheatreReportResponse> reports)
            throws Exception {

        Document document = createDocument(filePath);

        addTitle(
                document,
                "Daily Theatre Booking Report",
                date
        );

        PdfPTable table = new PdfPTable(5);

        table.setWidthPercentage(100);

        addHeaderCell(table, "Theatre ID");
        addHeaderCell(table, "Theatre");
        addHeaderCell(table, "Bookings");
        addHeaderCell(table, "Seats Sold");
        addHeaderCell(table, "Revenue");

        for (TheatreReportResponse report : reports) {

            table.addCell(String.valueOf(report.getTheatreId()));

            table.addCell(report.getTheatreName());

            table.addCell(String.valueOf(report.getTotalBookings()));

            table.addCell(String.valueOf(report.getSeatsSold()));

            table.addCell(String.valueOf(report.getTotalRevenue()));
        }

        document.add(table);

        document.close();
    }

    // =========================================================
    // 2. MOVIE REPORT
    // =========================================================
    public static void generateMovieReportPdf(
            String filePath,
            LocalDate date,
            List<MovieReportResponse> reports)
            throws Exception {

        Document document = createDocument(filePath);

        addTitle(
                document,
                "Daily Movie Booking Report",
                date
        );

        PdfPTable table = new PdfPTable(5);

        table.setWidthPercentage(100);

        addHeaderCell(table, "Movie ID");
        addHeaderCell(table, "Movie");
        addHeaderCell(table, "Bookings");
        addHeaderCell(table, "Seats Sold");
        addHeaderCell(table, "Revenue");

        for (MovieReportResponse report : reports) {

            table.addCell(String.valueOf(report.getMovieId()));

            table.addCell(report.getMovieTitle());

            table.addCell(String.valueOf(report.getTotalBookings()));

            table.addCell(String.valueOf(report.getSeatsSold()));

            table.addCell(String.valueOf(report.getTotalRevenue()));
        }

        document.add(table);

        document.close();
    }

    // =========================================================
    // 3. OVERALL DAILY REPORT
    // =========================================================
    public static void generateOverallReportPdf(
            String filePath,
            LocalDate date,
            OverallReportResponse report)
            throws Exception {

        Document document = createDocument(filePath);

        addTitle(
                document,
                "Overall Daily Booking Report",
                date
        );

        Font boldFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                12
        );

        Paragraph bookings = new Paragraph(
                "Total Bookings: "
                + report.getTotalBookings(),
                boldFont
        );

        Paragraph seats = new Paragraph(
                "Seats Sold: "
                + report.getSeatsSold(),
                boldFont
        );

        Paragraph revenue = new Paragraph(
                "Total Revenue: ₹"
                + report.getTotalRevenue(),
                boldFont
        );

        document.add(bookings);
        document.add(new Paragraph(" "));
        document.add(seats);
        document.add(new Paragraph(" "));
        document.add(revenue);

        document.close();
    }

    // =========================================================
    // 4. MOVIE + THEATRE REPORT
    // =========================================================
    public static void generateMovieTheatreReport(
            List<MovieTheatreReportResponse> reports,
            String filePath)
            throws Exception {

        Document document = createDocument(filePath);

        LocalDate date = LocalDate.now();

        addTitle(
                document,
                "Movie - Theatre Booking Report",
                date
        );

        PdfPTable table = new PdfPTable(5);

        table.setWidthPercentage(100);

        addHeaderCell(table, "Movie ID");
        addHeaderCell(table, "Movie");
        addHeaderCell(table, "Theatre");
        addHeaderCell(table, "Bookings");
        addHeaderCell(table, "Seats Sold");
        addHeaderCell(table, "Revenue");

        for (MovieTheatreReportResponse report : reports) {

            table.addCell(String.valueOf(report.getMovieId()));

            table.addCell(report.getMovieTitle());

            table.addCell(report.getTheatreName());

            table.addCell(String.valueOf(report.getTotalBookings()));

            table.addCell(String.valueOf(report.getSeatsSold()));

            table.addCell(String.valueOf(report.getTotalRevenue()));
        }

        document.add(table);

        document.close();
    }

    public static void generateDailyReportPdf(
            String filePath,
            LocalDate date,
            OverallReportResponse overallReport,
            List<MovieReportResponse> movieReports,
            List<TheatreReportResponse> theatreReports,
            List<MovieTheatreReportResponse> movieTheatreReports)
            throws Exception {

        // Create ONE PDF document
        Document document = createDocument(filePath);

        // =====================================================
        // MAIN TITLE
        // =====================================================
        addTitle(
                document,
                "Daily Movie Booking Report",
                date
        );

        // =====================================================
        // 1. OVERALL REPORT
        // =====================================================
        Font sectionFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                14
        );

        document.add(
                new Paragraph(
                        "1. Overall Summary",
                        sectionFont
                )
        );

        document.add(new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Total Bookings: "
                        + overallReport.getTotalBookings()
                )
        );

        document.add(
                new Paragraph(
                        "Seats Sold: "
                        + overallReport.getSeatsSold()
                )
        );

        document.add(
                new Paragraph(
                        "Total Revenue: ₹"
                        + overallReport.getTotalRevenue()
                )
        );

        document.add(new Paragraph(" "));

        // =====================================================
        // 2. MOVIE REPORT
        // =====================================================
        document.add(
                new Paragraph(
                        "2. Movie Report",
                        sectionFont
                )
        );

        document.add(new Paragraph(" "));

        PdfPTable movieTable = new PdfPTable(5);

        movieTable.setWidthPercentage(100);

        addHeaderCell(movieTable, "Movie ID");
        addHeaderCell(movieTable, "Movie");
        addHeaderCell(movieTable, "Bookings");
        addHeaderCell(movieTable, "Seats Sold");
        addHeaderCell(movieTable, "Revenue");

        for (MovieReportResponse report : movieReports) {

            movieTable.addCell(
                    String.valueOf(report.getMovieId())
            );

            movieTable.addCell(
                    report.getMovieTitle()
            );

            movieTable.addCell(
                    String.valueOf(report.getTotalBookings())
            );

            movieTable.addCell(
                    String.valueOf(report.getSeatsSold())
            );

            movieTable.addCell(
                    String.valueOf(report.getTotalRevenue())
            );
        }

        document.add(movieTable);

        document.add(new Paragraph(" "));

        // =====================================================
        // 3. THEATRE REPORT
        // =====================================================
        document.add(
                new Paragraph(
                        "3. Theatre Report",
                        sectionFont
                )
        );

        document.add(new Paragraph(" "));

        PdfPTable theatreTable = new PdfPTable(5);

        theatreTable.setWidthPercentage(100);

        addHeaderCell(theatreTable, "Theatre ID");
        addHeaderCell(theatreTable, "Theatre");
        addHeaderCell(theatreTable, "Bookings");
        addHeaderCell(theatreTable, "Seats Sold");
        addHeaderCell(theatreTable, "Revenue");

        for (TheatreReportResponse report : theatreReports) {

            theatreTable.addCell(
                    String.valueOf(report.getTheatreId())
            );

            theatreTable.addCell(
                    report.getTheatreName()
            );

            theatreTable.addCell(
                    String.valueOf(report.getTotalBookings())
            );

            theatreTable.addCell(
                    String.valueOf(report.getSeatsSold())
            );

            theatreTable.addCell(
                    String.valueOf(report.getTotalRevenue())
            );
        }

        document.add(theatreTable);

        document.add(new Paragraph(" "));

        // =====================================================
        // 4. MOVIE - THEATRE REPORT
        // =====================================================
        document.add(
                new Paragraph(
                        "4. Movie - Theatre Report",
                        sectionFont
                )
        );

        document.add(new Paragraph(" "));

        PdfPTable movieTheatreTable
                = new PdfPTable(6);

        movieTheatreTable.setWidthPercentage(100);

        addHeaderCell(movieTheatreTable, "Movie ID");
        addHeaderCell(movieTheatreTable, "Movie");
        addHeaderCell(movieTheatreTable, "Theatre");
        addHeaderCell(movieTheatreTable, "Bookings");
        addHeaderCell(movieTheatreTable, "Seats Sold");
        addHeaderCell(movieTheatreTable, "Revenue");

        for (MovieTheatreReportResponse report
                : movieTheatreReports) {

            movieTheatreTable.addCell(
                    String.valueOf(report.getMovieId())
            );

            movieTheatreTable.addCell(
                    report.getMovieTitle()
            );

            movieTheatreTable.addCell(
                    report.getTheatreName()
            );

            movieTheatreTable.addCell(
                    String.valueOf(report.getTotalBookings())
            );

            movieTheatreTable.addCell(
                    String.valueOf(report.getSeatsSold())
            );

            movieTheatreTable.addCell(
                    String.valueOf(report.getTotalRevenue())
            );
        }

        document.add(movieTheatreTable);

        // =====================================================
        // CLOSE PDF
        // =====================================================
        document.close();
    }
}
