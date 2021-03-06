package cn.gjing.tools.excel.valid;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * Default value verifier
 *
 * @author Gjing
 **/
public class DefaultNumericValidation implements ExcelValidation {
    @Override
    public void valid(NumericValid numericValid, Sheet sheet, int firstRow, int firstCol, int lastCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint numericConstraint = helper.createNumericConstraint(numericValid.validationType().getType(),
                numericValid.operatorType().getType(),
                numericValid.expr1(),
                "".equals(numericValid.expr2()) ? null : numericValid.expr2());
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, numericValid.boxLastRow() == 0 ? firstRow : numericValid.boxLastRow() + firstRow,
                firstCol, lastCol);
        DataValidation dataValidation = helper.createValidation(numericConstraint, regions);
        dataValidation.setShowErrorBox(numericValid.showErrorBox());
        dataValidation.setShowPromptBox(numericValid.showPromptBox());
        dataValidation.setErrorStyle(numericValid.rank().getRank());
        dataValidation.createErrorBox(numericValid.errorTitle(), numericValid.errorContent());
        dataValidation.createErrorBox(numericValid.errorTitle(), numericValid.errorContent());
        sheet.addValidationData(dataValidation);
    }

}
