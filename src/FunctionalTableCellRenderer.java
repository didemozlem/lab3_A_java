//ВИЗУАТОР ЯЧЕЕК
//Igneyi bulma ve rengini boyama işlemi,
//virgülden sonra basamak sayısı ve virgülün ayarlanması

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

class FunctionalTableCellRenderer implements TableCellRenderer {
    private JPanel panel = new JPanel();
    private JLabel label = new JLabel();

    // Igne ile (needle) çakışan boşlukları arıyoruz.
    private String needle = null;
    private DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();

    public FunctionalTableCellRenderer() {
        panel.add(label);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Virgulden sonra sadece 5 basamak goster.
        formatter.setMaximumFractionDigits(5);

        //Gruplandirmayi kullanma (yani 1000 leri virgül veya boşlukla ayırma)
        formatter.setGroupingUsed(false);

        // Sayinin virgğllğ kısmını ayiracağın isareti sec.
        DecimalFormatSymbols dottedDouble = formatter.getDecimalFormatSymbols();
        dottedDouble.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(dottedDouble);
        // Разместить надпись внутри панели
    }

    public void setNeedle(String needle) {
        this.needle = needle;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean  isSelected, boolean hasFocus, int row, int col) {

        // (форматировщик) Biçimlendiriciyi kullanarak double'ı dizeye dönüştürün
        String formattedDouble = formatter.format(value);

        // Установить текст надписи равным строковому представлению числа
        label.setText(formattedDouble);
        if (col == 1 && needle != null && needle.equals(formattedDouble)) {
            // Sutun numarası = 1 (yani 2. sutun) + иголка не null // (значит что-то ищем) +
            // Eger iğnenin değeri o boşlukla  uyuyorsa o satırı mavi boya.
            panel.setBackground(Color.RED);
        } else {
            // Değilse sadece beyaz
            panel.setBackground(Color.WHITE);
        }
        return panel;
    }
}