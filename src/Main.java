import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Main extends JFrame {
    // Uygulama ekrani buyukluk olculeri
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;

    // Dosyaları seçmek için bir iletişim kutusu nesnesi. Bileşen başlangıçta oluşturulmaz çünkü Kullanıcı, verileri bir dosyaya kaydetmeyi düşünmüyorsa buna ihtiyaç duymayabilir
    // Dosya kaydetme yeri seçme ekranı
    private JFileChooser fileChooser = null;
    // Menü öğeleri, farklı yerlerden değiştirilmeleri gerektiğinden sınıf veri alanlarına yerleştirilir.
    private JMenuItem saveToTextMenuItem;
    private JMenuItem searchValueMenuItem;
    private final JMenuItem infoMenuItem;

    private JCheckBoxMenuItem showColumnMenuItem;

    // Değişken değerlerini okumak için giriş alanları
    private JTextField textFieldFrom;
    private JTextField textFieldTo;
    private JTextField textFieldStep;
    private Box hBoxResult;

    // Визуализатор ячеек таблицы
    private FunctionalTableCellRenderer renderer = new FunctionalTableCellRenderer();

    // Hesaplama sonuçlarını içeren veri modeli
    private FunctionTableModel data;

    private JTable table;
    private TableColumn bool_column;
    private Double param = -1.0;

    public Main(){
        // Обязательный вызов конструктора предка
        super("Табулирование функции на отрезке");
        // Ekran boyutunun ayarlanması
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        // Uygulama ekranını ortala.
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);

        // Menü oluştur.
        JMenuBar menuBar = new JMenuBar();
        //Menüyü uygulamanın ana menü olarak ayarlayın.
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        JMenu tableMenu = new JMenu("Таблица");
        menuBar.add(tableMenu);
        JMenu infoMenu = new JMenu("Справка");
        menuBar.add(infoMenu);
        // Bir metin dosyasına kaydetmek için yeni bir "eylem" oluşturun
        Action saveToTextAction = new AbstractAction( "Сохранить в текстовый файл") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    // Если экземпляр диалогового окна "Открыть файл" ещѐ не создан,
                    // то создать его
                    //"Dosya Aç" iletişim kutusunun bir örneği henüz oluşturulmadıysa, onu oluşturun
                    fileChooser = new JFileChooser();
                    // ve geçerli dizinle başlatın.(и инициализировать текущей директорией)
                    fileChooser.setCurrentDirectory(new File("."));
                }
                // İletişim kutusunu göster
                if (fileChooser.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION){
                    // Görüntüleme sonucu başarılıysa verileri bir metin dosyasına kaydedin.
                    saveToTextFile(fileChooser.getSelectedFile());
                }
            }
        };

        // "Файл" Kısmının oluşturulması.
        saveToTextMenuItem = fileMenu.add(saveToTextAction);
        fileMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                // Varsayılan olarak menü öğesi devre dışıdır (henüz veri yok)
                if (data == null) saveToTextMenuItem.setEnabled(false);
                else saveToTextMenuItem.setEnabled(true);
            }
            @Override
            public void menuDeselected(MenuEvent e) { }
            @Override
            public void menuCanceled(MenuEvent e) { }
        });

        // Polinom değerlerini bulmak için yeni bir eylem oluşturun
        Action searchValueAction = new AbstractAction("Найти значение функции") {
            public void actionPerformed(ActionEvent event) {
                // Kullanıcıdan arama dizesini girmesini isteyin
                String value = JOptionPane.showInputDialog(Main.this, "Введите значение для поиска", "Поиск значения", JOptionPane.QUESTION_MESSAGE);
                // Girilen değeri iğne olarak ayarlayın
                renderer.setNeedle(value);
                // Tabloyu yenile.
                getContentPane().repaint();
            }
        };
        // "Таблица" Menüsüne eylemler ekle.
        searchValueMenuItem = tableMenu.add(searchValueAction);
        // Varsayılan olarak menü öğesi devre dışıdır (henüz veri yok)
        tableMenu.add(new JSeparator());
        showColumnMenuItem = new JCheckBoxMenuItem("Показать третий столбец", true);
        tableMenu.add(showColumnMenuItem);

        // 3. sütunun gösterilmesi ile ilgili bool.
        showColumnMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == 2) {
                    bool_column = table.getColumnModel().getColumn(2);
                    table.removeColumn(bool_column);
                }if(e.getStateChange() == 1){
                    table.addColumn(bool_column);
                }
            }
        });

        Action aboutProgrammAction = new AbstractAction("О программе") {
            public void actionPerformed(ActionEvent event) {
                info_ info1 = new info_();
                JLabel info = new JLabel(info1.toString());
                info.setHorizontalTextPosition(JLabel.CENTER);
                info.setVerticalTextPosition(JLabel.BOTTOM);
                info.setIconTextGap(10);
                JOptionPane.showMessageDialog(Main.this, info, "О программе", JOptionPane.PLAIN_MESSAGE);
            }
        };
        infoMenuItem = infoMenu.add(aboutProgrammAction);
        // ilk değerler ve yazım alanlarının oluşturulması.
        textFieldFrom = new JTextField("0.0", 10);
        // предотвратить увеличение размера поля ввода
        textFieldFrom.setMaximumSize(textFieldFrom.getPreferredSize());
        textFieldTo = new JTextField("1.0", 10);
        textFieldTo.setMaximumSize(textFieldTo.getPreferredSize());
        textFieldStep = new JTextField("0.1", 10);
        textFieldStep.setMaximumSize(textFieldStep.getPreferredSize());
        // Создать контейнер 1 типа "коробка с горизонтальной укладкой"
        Box hboxXRange = Box.createHorizontalBox();
        // Задать для контейнера тип рамки "объѐмная"
        hboxXRange.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Настройки:"));
        hboxXRange.add(Box.createHorizontalGlue());
        hboxXRange.add(new JLabel("X изменяется на интервале от:"));
        hboxXRange.add(Box.createHorizontalStrut(10));
        hboxXRange.add(textFieldFrom);
        hboxXRange.add(Box.createHorizontalStrut(20)); //yazılar arası boşluk
        hboxXRange.add(new JLabel("до:"));
        hboxXRange.add(Box.createHorizontalStrut(10));
        hboxXRange.add(textFieldTo);
        hboxXRange.add(Box.createHorizontalStrut(20));
        hboxXRange.add(new JLabel("с шагом:"));
        hboxXRange.add(Box.createHorizontalStrut(10));
        hboxXRange.add(textFieldStep);
        hboxXRange.add(Box.createHorizontalStrut(20));
        hboxXRange.add(Box.createHorizontalGlue());
        // Yerleşim sırasında alanın hiç sıkışmaması için tercih edilen alan boyutunu minimum boyutun iki katına ayarlayın
        hboxXRange.setPreferredSize(new Dimension((int)(hboxXRange.getMaximumSize().getWidth()),
                (int)(hboxXRange.getMinimumSize().getHeight()*1.5)));
        // Установить область в верхнюю (северную) часть компоновки
        getContentPane().add(hboxXRange, BorderLayout.NORTH);


        // Tuş oluştur "Вычислить"
        JButton buttonCalc = new JButton("Вычислить");
        //  "Вычислить" e bir eylem oluştur ve butona bağla
        buttonCalc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    // Bir segmentin başlangıç ve bitiş değerlerini okuyun
                    showColumnMenuItem.setState(true);
                    Double from = Double.parseDouble(textFieldFrom.getText());
                    Double to = Double.parseDouble(textFieldTo.getText());
                    Double step = Double.parseDouble(textFieldStep.getText());
                    // Okunan verilere dayanarak tablo modelinin yeni bir örneğini oluşturun
                    data = new FunctionTableModel(from, to, step, param);
                    // Yeni bir tablo örneği oluşturun
                    table = new JTable(data);
                    // Установить в качестве визуализатора ячеек для класса Double разработанный визуализатор
                    // Yeni bir tablo örneği oluşturun Geliştirilen görselleştiriciyi Double sınıfı için hücre oluşturucu olarak ayarlayın
                    table.setDefaultRenderer(Double.class, renderer);
                    // Tablo satır boyutunu 30 piksele ayarla
                    table.setRowHeight(30);
                    // İç içe geçmiş tüm öğeleri hBoxResult kapsayıcısından kaldırın
                    hBoxResult.removeAll();
                    // Добавить в hBoxResult таблицу, "обёрнутую" в панель с полосами прокрутки
                    // hBoxResult'a kaydırma çubukları olan bir panelde "sarılmış" bir tablo ekleyin
                    hBoxResult.add(new JScrollPane(table));
                    // (Обновить) Ana pencere içerik alanını yenile
                    hBoxResult.revalidate();

                }

                // Sayıların yanlış formatta olması durumunda hata mesajı.
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Main.this,
                            "Ошибка в формате записи числа с плавающей точкой",
                            "Ошибочный формат числа", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        // "Очистить поля" tuşu oluştur.
        JButton buttonReset = new JButton("Очистить поля");
        // Temizleme tuşuna basılınca yapılacak eylemleri ekle.
        buttonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                // İlk gösterilen sayıların tekrar yazılması
                textFieldFrom.setText("0.0");
                textFieldTo.setText("1.0");
                textFieldStep.setText("0.1");
                // hBoxResult ın içine konulmuş bütün elemanları sil.
                hBoxResult.removeAll();
                // Boş panel ekle.
                hBoxResult.repaint();
                data = null;
            }
        });

        // Поместить созданные кнопки в контейнер
        Box hboxButtons = Box.createHorizontalBox();
        hboxButtons.setBorder(BorderFactory.createEtchedBorder());
        hboxButtons.add(Box.createHorizontalGlue());
        hboxButtons.add(buttonCalc);
        hboxButtons.add(Box.createHorizontalStrut(30));
        hboxButtons.add(buttonReset);
        hboxButtons.add(Box.createHorizontalGlue());

        // Yeterli alan ayarlama: hesaplama ve temizleme tuşlarınınn alanı için.
        hboxButtons.setPreferredSize(new Dimension((int)(hboxButtons.getMaximumSize().getWidth()),
                (int)(hboxButtons.getMinimumSize().getHeight() * 1.5)));
        // Ekranın altına yerleştir.
        getContentPane().add(hboxButtons, BorderLayout.SOUTH);
        // Çıkış alanı şimdilik boş
        hBoxResult = Box.createHorizontalBox();
        // Установить контейнер hBoxResult в главной (центральной) области граничной компоновки
        getContentPane().add(hBoxResult, BorderLayout.CENTER);
    }

    protected void saveToTextFile(File selectedFile) {// Создать  символьный поток вывода, направленный в указанный файл
        try{

            PrintStream out = new PrintStream(selectedFile); // Çıkış dosyasına dosyanın genel işeriğinin tanıtılması. hagi aralıklar vs.
            out.println("Результаты табулирования функции:");
            out.println("Интервал от " + data.getFrom() + " до " + data.getTo()+ " с шагом " +
                    data.getStep() + " и параметром " + data.getParameter());

            for (int i = 0; i < data.getRowCount(); i++)// Noktalardaki değerlerin çıkış dosyasına yazdırılması.
            {
                out.println("Значение в точке " + data.getValueAt(i,0)  + " равно " + data.getValueAt(i,1));
            }
            out.close();// Закрыть поток вывода
        } catch (FileNotFoundException e){
            // File oluşturduğumuz ve açmadığımız için file bulunamadı hatası oluşturmamıza gerek yok.
        }
    }

    public static void main(String[] args){ // Создать экземпляр главного окна, передав ему коэффициенты
        Main frame = new Main();
        // Pencereni kapatılması durumunda uygulanacak eylemi ayarla.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}