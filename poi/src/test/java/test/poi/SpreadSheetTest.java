package test.poi;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class SpreadSheetTest {

    @Test
    public void checkSheetWhetherIsEmpty() throws Throwable {
        Workbook book = open("simple");
        assertThat(book.getNumberOfSheets(), equalTo(3));

        assertThat(book.getSheetAt(0).getPhysicalNumberOfRows(), equalTo(1));
        assertThat(book.getSheetAt(1).getPhysicalNumberOfRows(), equalTo(0));
        assertThat(book.getSheetAt(1).getPhysicalNumberOfRows(), equalTo(0));
    }

    @Test
    public void returnNullWhenOutOfRange() throws Throwable {
        Sheet sheet = open("simple").getSheetAt(0);
        Row first = sheet.getRow(0);


        assertThat(sheet.getRow(sheet.getPhysicalNumberOfRows()), is(nullValue()));
        assertThat(first.getCell(first.getPhysicalNumberOfCells()), is(nullValue()));
    }

    @Test
    public void retrievesSheetNames() throws Throwable {
        Workbook book = open("pages");
        List<String> names = new ArrayList<>();

        book.sheetIterator().forEachRemaining(it -> names.add(it.getSheetName()));

        assertThat(names, equalTo(asList("page1", "page2")));
    }

    @Test
    public void readCellValue() throws Throwable {
        Sheet sheet = open("simple").getSheetAt(0);
        assertThat(table(sheet), equalTo(new String[][]{{"1", "2", "3"}}));
    }


    @Test
    public void readSpanVerticalValue() throws Throwable {
        Sheet sheet = open("span-vertical").getSheetAt(0);

        assertThat(table(sheet), equalTo(new String[][]{
                {"1", "2"},
                {"1", "3"}
        }));
    }

    @Test
    public void readCellValuesWithDifferentSizedCells() throws Throwable {
        Sheet sheet = open("diff-cells").getSheetAt(0);

        assertThat(table(sheet), equalTo(new String[][]{
                {"1", "2"},
                {"3"}
        }));
    }

    @Test
    public void readSpanHorizontalValue() throws Throwable {
        Sheet sheet = open("span-horizontal").getSheetAt(0);

        assertThat(table(sheet), equalTo(new String[][]{
                {"1", "1"},
                {"2", "3"}
        }));
    }

    private String[][] table(Sheet sheet) {
        List<CellRangeAddress> regions = sheet.getMergedRegions();
        DataFormatter formatter = new DataFormatter();
        String[][] table = new String[sheet.getPhysicalNumberOfRows()][];

        for (int i = 0; i < table.length; i++) {
            Row row = sheet.getRow(i);
            table[i] = new String[row.getPhysicalNumberOfCells()];

            for (int j = 0; j < table[i].length; j++) {
                int cell = j;
                table[i][j] = region(regions, row.getCell(j)).map(it -> table[it.getFirstRow()][it.getFirstColumn()]).orElseGet(() -> formatter.formatCellValue(row.getCell(cell)));
            }
        }

        return table;
    }

    private Optional<CellRangeAddress> region(List<CellRangeAddress> regions, Cell cell) {
        for (CellRangeAddress region : regions) {
            if (region.isInRange(cell)) return Optional.of(region);
        }
        return Optional.empty();
    }

    private Workbook open(String name) throws IOException, InvalidFormatException {
        return WorkbookFactory.create(ClassLoader.getSystemResourceAsStream(name + ".xlsx"));
    }
}
