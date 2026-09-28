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

    public static void generateTheatreReportPdf(
            String filePath,
            LocalDate date,
            List<TheatreReportResponse> reports) throws Exception {

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(filePath)
        );

        document.open();

        // ---------- TITLE ----------
        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                18
        );

        Paragraph title = new Paragraph(
                "Daily Theatre Booking Report",
                titleFont
        );

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        // ---------- DATE ----------
        Paragraph reportDate = new Paragraph(
                "Date: " + date
        );

        reportDate.setAlignment(Element.ALIGN_CENTER);

        document.add(reportDate);

        document.add(new Paragraph(" "));

        // ---------- TABLE ----------
        PdfPTable table = new PdfPTable(5);

        table.setWidthPercentage(100);

        addHeaderCell(table, "Theatre ID");
        addHeaderCell(table, "Theatre");
        addHeaderCell(table, "Bookings");
        addHeaderCell(table, "Seats Sold");
        addHeaderCell(table, "Revenue");

        // ---------- DATA ----------
        for (TheatreReportResponse report : reports) {

            table.addCell(
                    String.valueOf(report.getTheatreId())
            );

            table.addCell(
                    report.getTheatreName()
            );

            table.addCell(
                    String.valueOf(report.getTotalBookings())
            );

            table.addCell(
                    String.valueOf(report.getSeatsSold())
            );

            table.addCell(
                    String.valueOf(report.getTotalRevenue())
            );
        }

        document.add(table);

        document.close();
    }

    private static void addHeaderCell(PdfPTable table, String text) {

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        PdfPCell cell = new PdfPCell(
                new Phrase(text, font)
        );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        table.addCell(cell);
    }

    public static void generateMovieReportPdf(
            String filePath,
            LocalDate date,
            List<MovieReportResponse> reports) throws Exception {

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(filePath)
        );

        document.open();

        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                18
        );

        Paragraph title = new Paragraph(
                "Daily Movie Booking Report",
                titleFont
        );

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        Paragraph reportDate = new Paragraph(
                "Date: " + date
        );

        reportDate.setAlignment(Element.ALIGN_CENTER);

        document.add(reportDate);

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);

        table.setWidthPercentage(100);

        addHeaderCell(table, "Movie ID");
        addHeaderCell(table, "Movie");
        addHeaderCell(table, "Bookings");
        addHeaderCell(table, "Seats Sold");
        addHeaderCell(table, "Revenue");

        // ---------- DATA ----------
        for (MovieReportResponse report : reports) {

            table.addCell(
                    String.valueOf(report.getMovieId())
            );

            table.addCell(
                    report.getMovieTitle()
            );

            table.addCell(
                    String.valueOf(report.getTotalBookings())
            );

            table.addCell(
                    String.valueOf(report.getSeatsSold())
            );

            table.addCell(
                    String.valueOf(report.getTotalRevenue())
            );
        }

        document.add(table);

        document.close();
    }

    public static void generateOverallReportPdf(String filePath, LocalDate date, OverallReportResponse report) throws Exception {

        Document document = new Document();

        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        document.add(new Paragraph("OVERALL DAILY REPORT"));
        document.add(new Paragraph("Date: " + report.getReportDate()));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Bookings: " + report.getTotalBookings()));
        document.add(new Paragraph("Seats Sold: " + report.getSeatsSold()));
        document.add(new Paragraph("Total Revenue: " + report.getTotalRevenue()));

        document.close();

    }

    public static void generateMovieTheatreReport(
            List<MovieTheatreReportResponse> reports,
            String filePath) throws Exception {

        Document document = new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(filePath)
        );

        document.open();

        document.add(
                new Paragraph("MOVIE - THEATRE REPORT")
        );

        document.add(new Paragraph(" "));

        for (MovieTheatreReportResponse report : reports) {

            document.add(new Paragraph(
                    "Movie: " + report.getMovieTitle()
            ));

            document.add(new Paragraph(
                    "Theatre: " + report.getTheatreName()
            ));

            document.add(new Paragraph(
                    "Total Bookings: " + report.getTotalBookings()
            ));

            document.add(new Paragraph(
                    "Seats Sold: " + report.getSeatsSold()
            ));

            document.add(new Paragraph(
                    "Total Revenue: ₹" + report.getTotalRevenue()
            ));

            document.add(new Paragraph(
                    "----------------------------------------"
            ));
        }

        document.close();
    }
}
