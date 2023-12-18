//МОДЕЛЬ ТАБЛИЦЫ
//

import javax.swing.table.AbstractTableModel;

public class FunctionTableModel extends AbstractTableModel{

    private Double from, to, step, parameter;

    public FunctionTableModel(Double from, Double to, Double step, Double parameter) {
        this.from = from;
        this.to = to;
        this.step = step;
        this.parameter = parameter;
    }

    public Double getParameter() { return parameter; }

    public Double getFrom() {
        return from;
    }

    public Double getTo() {
        return to;
    }

    public Double getStep() {
        return step;
    }

    @Override
    // Вычислить количество точек между началом и концом отрезка
    // исходя из шага табулирования
    //Tablolama adımına göre bir segmentin başlangıcı ve bitişi arasındaki noktaların sayısını hesaplayın
    public int getRowCount() {
        return (int)(Math.ceil((to - from) / step)) + 1;
    }

    @Override
    // Bizim modelde 3 sütun olacak.
    public int getColumnCount() {
        return 3;
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        //File a yazdırırken her satırdaki sayıyı almak için
        // Вычислить значение X как НАЧАЛО_ОТРЕЗКА + ШАГ*НОМЕР_СТРОКИ
        Double x = from + step * rowIndex;

        Double y=parameter+x*2;
        Boolean z = y > 0;
        switch (columnIndex){
            case 0: return x;
            case 1: return y;
            case 2: return z;
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex == 2) return Boolean.class;
        return Double.class;
    }

    @Override
    public String getColumnName(int column) { //Sütunların adlandırılması.
        switch (column){

            // 1. sütun
            case 0: return "Значение Х";

            // 2. sütun
            case 1: return "Значение многочлена";

            // 3. sütun
            case 2: return "Значение многочлена >0?";
        }
        return "";
    }
}