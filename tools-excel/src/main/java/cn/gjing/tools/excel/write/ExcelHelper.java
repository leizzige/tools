package cn.gjing.tools.excel.write;

import cn.gjing.tools.excel.*;
import cn.gjing.tools.excel.util.BeanUtils;
import cn.gjing.tools.excel.util.ParamUtils;
import cn.gjing.tools.excel.util.TimeUtils;
import cn.gjing.tools.excel.valid.DateValid;
import cn.gjing.tools.excel.valid.ExplicitValid;
import cn.gjing.tools.excel.valid.NumericValid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Gjing
 **/
class ExcelHelper {
    private Workbook workbook;
    private Sheet sheet;
    private MetaObject metaObject;

    public ExcelHelper(Workbook workbook, Sheet sheet, MetaObject metaObject) {
        this.workbook = workbook;
        this.sheet = sheet;
        this.metaObject = metaObject;
    }
    
    public void setVal(List<?> data, List<Field> headFieldList, Sheet sheet, Row row, boolean changed, int offset) {
        Cell cell;
        int validIndex = 0;
        if (changed) {
            for (int i = 0; i < headFieldList.size(); i++) {
                cell = row.createCell(i);
                cell.setCellStyle(this.metaObject.getMetaStyle().getHeadStyle());
                Field field = headFieldList.get(i);
                ExcelField excelField = field.getAnnotation(ExcelField.class);
                cell.setCellValue(excelField.value());
                sheet.setColumnWidth(i, excelField.width());
                if (data == null || data.isEmpty()) {
                    validIndex = this.addValid(field, row, i, validIndex, Type.XLSX);
                }
            }
            offset++;
        }
        if (data == null || data.isEmpty()) {
            return;
        }
        ExcelField excelField;
        Field field;
        Object value = null;
        Map<Object, ExcelModel> excelModelMap = new HashMap<>(16);
        ExcelModel excelModel;
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            Object o = data.get(i);
            row = sheet.createRow(offset + i);
            for (int j = 0, headSize = headFieldList.size(); j < headSize; j++) {
                field = headFieldList.get(j);
                excelField = field.getAnnotation(ExcelField.class);
                field.setAccessible(true);
                try {
                    value = field.get(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (excelField.autoMerge()) {
                    String key = i + "-" + j;
                    if (i == 0) {
                        this.putExcelModel(row, value, excelModelMap, key);
                    } else {
                        String oldKey = (i - 1) + "-" + j;
                        excelModel = excelModelMap.get(oldKey);
                        if (excelModel != null) {
                            if (ParamUtils.equals(value, excelModel.getOldValue())) {
                                if (i == dataSize - 1) {
                                    this.sheet.addMergedRegion(new CellRangeAddress(excelModel.getRowIndex(), row.getRowNum(), j, j));
                                } else {
                                    excelModelMap.put(key, excelModel);
                                }
                            } else {
                                if (excelModel.getRowIndex() + 1 < row.getRowNum()) {
                                    this.sheet.addMergedRegion(new CellRangeAddress(excelModel.getRowIndex(), row.getRowNum() - 1, j, j));
                                }
                                if (i != dataSize - 1) {
                                    this.putExcelModel(row, value, excelModelMap, key);
                                }
                            }
                        }
                    }
                }
                this.setCellVal(excelField, field, row, value, j, this.metaObject);
            }
        }
    }

    public void putExcelModel(Row row, Object value, Map<Object, ExcelModel> excelModelMap, String key) {
        excelModelMap.put(key, ExcelModel.builder()
                .oldValue(value)
                .rowIndex(row.getRowNum())
                .build());
    }

    @SuppressWarnings("unchecked")
    public void setCellVal(ExcelField excelField, Field field, Row row, Object value, int index, MetaObject metaObject) {
        Cell valueCell = row.createCell(index);
        valueCell.setCellStyle(metaObject.getMetaStyle().getBodyStyle());
        if (value == null) {
            valueCell.setCellValue("");
        } else {
            if (ParamUtils.equals("", excelField.pattern())) {
                if (field.getType().isEnum()) {
                    ExcelEnumConvert excelEnumConvert = field.getAnnotation(ExcelEnumConvert.class);
                    Objects.requireNonNull(excelEnumConvert, "Enum convert cannot be null");
                    Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) field.getType();
                    try {
                        EnumConvert<Enum<?>, ?> enumConvert = (EnumConvert<Enum<?>, ?>) excelEnumConvert.convert().newInstance();
                        valueCell.setCellValue(enumConvert.toExcelAttribute(BeanUtils.getEnum(enumType, value.toString())).toString());
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    valueCell.setCellValue(value.toString());
                }
            } else {
                valueCell.setCellValue(TimeUtils.dateToString((Date) value, excelField.pattern()));
            }
        }
    }

    public int addValid(Field field, Row row, int i, int validIndex, Type type) {
        ExplicitValid ev = field.getAnnotation(ExplicitValid.class);
        DateValid dv = field.getAnnotation(DateValid.class);
        NumericValid nv = field.getAnnotation(NumericValid.class);
        if (ev != null) {
            String[] values = metaObject.getExplicitValues().get(field.getName());
            try {
                ev.validClass().newInstance().valid(ev, workbook, sheet, row.getRowNum() + 1, i, i, validIndex, values);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (values == null) {
                return validIndex;
            }
            validIndex++;
            return validIndex;
        }
        if (type == Type.XLS) {
            if (dv != null) {
                try {
                    dv.validClass().newInstance().valid(dv, sheet, row.getRowNum() + 1, i, i);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return validIndex;
            }
        }
        if (nv != null) {
            try {
                nv.validClass().newInstance().valid(nv, sheet, row.getRowNum() + 1, i, i);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return validIndex;
        }
        return validIndex;
    }
}
