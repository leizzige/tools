package cn.gjing.tools.excel.valid;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * Default time verifier
 *
 * @author Gjing
 **/
public class DefaultDateValidation implements ExcelValidation {
    @Override
    public void valid(DateValid dateValid, Sheet sheet, int firstRow, int firstCol, int lastCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = helper.createDateConstraint(dateValid.operatorType().getType(), dateValid.expr1(),
                dateValid.expr2(), dateValid.pattern());
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, dateValid.boxLastRow() == 0 ? firstRow : dateValid.boxLastRow() + firstRow,
                firstCol, lastCol);
        DataValidation dataValidation = helper.createValidation(dvConstraint, regions);
        dataValidation.setShowErrorBox(dateValid.showErrorBox());
        dataValidation.setShowPromptBox(dateValid.showPromptBox());
        dataValidation.setErrorStyle(dateValid.rank().getRank());
        dataValidation.createErrorBox(dateValid.errorTitle(), dateValid.errorContent());
        sheet.addValidationData(dataValidation);
    }
}
