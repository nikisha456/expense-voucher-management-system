package com.abc.expensevoucher.service;

import com.abc.expensevoucher.dto.voucher.VoucherResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Color INDIGO = new Color(49, 46, 129);
    private static final Color TEAL = new Color(13, 148, 136);
    private static final Color LINE = new Color(226, 232, 240);

    private final VoucherService voucherService;
    private final FileStorageService fileStorageService;

    public byte[] generate(Long id) {
        VoucherResponse voucher = voucherService.getById(id);
        try {
            return render(voucher);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to generate PDF for voucher {}", id, ex);
            throw new IllegalStateException("Could not generate the voucher PDF");
        }
    }

    private byte[] render(VoucherResponse voucher) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(document, output);
            document.open();

            Font brand = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, INDIGO);
            Font subtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, TEAL);
            Font label = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));
            Font value = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

            Paragraph company = new Paragraph("ABC COMPANY", brand);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);
            Paragraph title = new Paragraph("Expense Voucher", subtitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12);
            document.add(title);

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.addCell(metaCell("Voucher Number", voucher.getVoucherNumber(), label, bold));
            meta.addCell(metaCell("Status", voucher.getStatus().getLabel(), label, bold));
            meta.addCell(metaCell("Voucher Date", voucher.getVoucherDate().format(DATE), label, value));
            meta.addCell(metaCell("Expense Date", voucher.getExpenseDate().format(DATE), label, value));
            document.add(meta);
            document.add(spacer());

            PdfPTable details = new PdfPTable(2);
            details.setWidthPercentage(100);
            details.addCell(metaCell("Employee", voucher.getEmployeeName(), label, value));
            details.addCell(metaCell("Employee ID", voucher.getEmployeeCode() == null ? "—" : voucher.getEmployeeCode(), label, value));
            details.addCell(metaCell("Department", voucher.getDepartmentName(), label, value));
            details.addCell(metaCell("Category", voucher.getCategoryLabel(), label, value));
            PdfPCell titleCell = metaCell("Expense Title", voucher.getExpenseTitle(), label, value);
            titleCell.setColspan(2);
            details.addCell(titleCell);
            String description = voucher.getExpenseDescription() == null ? "—" : voucher.getExpenseDescription();
            PdfPCell descriptionCell = metaCell("Description", description, label, value);
            descriptionCell.setColspan(2);
            details.addCell(descriptionCell);
            document.add(details);
            document.add(spacer());

            Paragraph amount = new Paragraph("Amount: INR " + voucher.getAmount().toPlainString(), bold);
            amount.setSpacingAfter(4);
            document.add(amount);
            Paragraph words = new Paragraph("In words: " + voucher.getAmountInWords(), value);
            words.setSpacingAfter(8);
            document.add(words);

            if (voucher.getRejectionReason() != null) {
                Paragraph reason = new Paragraph("Rejection reason: " + voucher.getRejectionReason(),
                        FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(190, 18, 60)));
                reason.setSpacingAfter(10);
                document.add(reason);
            }

            PdfPTable signatures = new PdfPTable(3);
            signatures.setWidthPercentage(100);
            signatures.setSpacingBefore(18);
            signatures.addCell(signatureCell("Employee", voucher.getEmployeeName(), voucher.getEmployeeSignatureFileName(), label, value));
            signatures.addCell(signatureCell("Director", voucher.getDecisionByName() == null ? "Director" : voucher.getDecisionByName(),
                    voucher.getDirectorSignatureFileName(), label, value));
            signatures.addCell(signatureCell("Accounts", "For reimbursement", null, label, value));
            document.add(signatures);

            Paragraph footer = new Paragraph("Generated by ExpenseFlow for ABC Company.", label);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(24);
            document.add(footer);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw ex;
        }
    }

    private PdfPCell metaCell(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(LINE);
        cell.setPadding(8);
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value == null ? "—" : value, valueFont));
        return cell;
    }

    private PdfPCell signatureCell(String role, String name, String fileName, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(LINE);
        cell.setPadding(8);
        cell.setFixedHeight(110);
        cell.addElement(new Paragraph(role, labelFont));
        if (fileName != null) {
            try {
                Path path = fileStorageService.resolve(fileName);
                if (Files.exists(path)) {
                    Image image = Image.getInstance(path.toAbsolutePath().toString());
                    image.scaleToFit(140, 48);
                    cell.addElement(image);
                }
            } catch (Exception ex) {
                cell.addElement(new Paragraph("Signature on file", valueFont));
            }
        } else {
            cell.addElement(new Paragraph(" ", valueFont));
            cell.addElement(new Paragraph(" ", valueFont));
        }
        cell.addElement(new Paragraph(name == null ? "" : name, valueFont));
        return cell;
    }

    private Paragraph spacer() {
        Paragraph paragraph = new Paragraph(" ");
        paragraph.setSpacingAfter(6);
        return paragraph;
    }
}
