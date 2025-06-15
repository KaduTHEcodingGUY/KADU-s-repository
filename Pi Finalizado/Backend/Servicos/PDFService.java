package Backend.Servicos;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Font;
import com.itextpdf.text.Element;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import Backend.Servicos.IngressoService.DetalheIngresso;
import java.time.format.DateTimeFormatter;

public class PDFService {

    public static boolean gerarIngressoPDF(DetalheIngresso ingresso) {
        String downloadsPath = System.getProperty("user.home") + "/Downloads/";
        String fileName = "Ingresso_" + ingresso.getCodigoIngresso() + ".pdf";
        Path filePath = Paths.get(downloadsPath, fileName);

        Document document = new Document();
        try {
            Files.createDirectories(filePath.getParent()); // Garante que o diretório de downloads existe
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            // Cores e Fontes
            BaseColor textColor = BaseColor.BLACK;
            BaseColor accentColor = new BaseColor(220, 53, 69); // Cor do botão de compra

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, textColor);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, accentColor);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, textColor);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textColor);
            Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 16, BaseColor.DARK_GRAY);

            // Título Principal
            Paragraph title = new Paragraph("Ingresso para o Evento", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Código do Ingresso (destaque)
            Paragraph code = new Paragraph("CÓDIGO: " + ingresso.getCodigoIngresso(), codeFont);
            code.setAlignment(Element.ALIGN_CENTER);
            code.setSpacingAfter(10);
            document.add(code);

            // Seção de Detalhes do Ingresso
            Paragraph pecaTitle = new Paragraph(ingresso.getPecaTitulo(), subtitleFont);
            pecaTitle.setAlignment(Element.ALIGN_LEFT);
            pecaTitle.setSpacingBefore(10);
            document.add(pecaTitle);

            document.add(new Paragraph("Data/Hora: " + ingresso.getDataHoraSessao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
            document.add(new Paragraph("Assentos: " + ingresso.getAssentosComprados(), normalFont));

            // Adicionar desconto de fidelidade se aplicável
            if (ingresso.isMembroFidelidade()) {
                Paragraph descontoFidelidade = new Paragraph("Desconto de Fidelidade (10%): -R$ " + 
                    String.format("%.2f", ingresso.getDescontoFidelidade()), normalFont);
                descontoFidelidade.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(40, 167, 69))); // Verde
                document.add(descontoFidelidade);
            }

            // Adicionar valor total pago
            Paragraph valorTotalPago = new Paragraph("Valor Total Pago: R$ " + String.format("%.2f", ingresso.getPagamento()), normalFont);
            valorTotalPago.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(valorTotalPago);

            document.add(new Paragraph("ID da Reserva: " + ingresso.getReservaId(), normalFont));
            document.add(new Paragraph("Adquirido em: " + ingresso.getDataCompra().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));

            // Imagem (se disponível)
            if (ingresso.getPecaImagemUrl() != null && !ingresso.getPecaImagemUrl().isEmpty()) {
                try {
                    Image img = Image.getInstance(new URL(ingresso.getPecaImagemUrl()));
                    img.scaleToFit(150, 150);
                    img.setAlignment(Element.ALIGN_RIGHT);
                    document.add(img);
                } catch (IOException e) {
                    System.err.println("[ERRO PDFService] Erro ao carregar imagem: " + e.getMessage());
                }
            }

            // Linha Divisória
            Paragraph separator = new Paragraph(new Chunk("----------------------------------------------------------------------", normalFont));
            separator.setAlignment(Element.ALIGN_CENTER);
            separator.setSpacingBefore(20);
            separator.setSpacingAfter(20);
            document.add(separator);

            // Mensagem de Agradecimento
            Paragraph thankYou = new Paragraph("Obrigado por sua compra!", boldFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            document.close();
            System.out.println("[DEBUG PDFService] PDF gerado com sucesso em: " + filePath.toString());
            return true;
        } catch (DocumentException | IOException e) {
            System.err.println("[ERRO PDFService] Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }
}